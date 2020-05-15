package com.iexec.worker.tee.post.compute.signer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexec.common.result.ComputedFile;
import com.iexec.common.security.Signature;
import com.iexec.common.tee.TeeEnclaveChallengeSignature;
import com.iexec.common.utils.CredentialsUtils;
import com.iexec.common.utils.HashUtils;
import com.iexec.worker.tee.post.compute.utils.EnvUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.iexec.common.utils.SignatureUtils.isExpectedSignerOnSignedMessageHash;
import static com.iexec.common.utils.SignatureUtils.signMessageHashAndGetSignature;
import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY;
import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_WORKER_ADDRESS;
import static com.iexec.worker.tee.post.compute.utils.EnvUtils.exit;
import static com.iexec.worker.tee.post.compute.utils.FilesUtils.UNPROTECTED_IEXEC_OUT;

@Slf4j
public class SignerService {



    public static String signEnclaveChallenge(String messageHash, String enclaveChallengePrivateKey) {



        //TeeEnclaveChallengeSignature enclaveChallengeSignature =
        //        generateTeeEnclaveChallengeSignature(enclaveChallengePrivateKey, taskId, workerAddress, resultDigest);

        Signature enclaveChallengeSignature = signMessageHashAndGetSignature(messageHash, enclaveChallengePrivateKey);

        if (enclaveChallengeSignature == null) {
            System.err.println("Failed to generate TeeEnclaveChallenge signature (exiting)");
            return "";
        }

        boolean isSignatureValid = isExpectedSignerOnSignedMessageHash(messageHash, enclaveChallengeSignature,
                CredentialsUtils.getAddress(enclaveChallengePrivateKey));

        if (!isSignatureValid) {
            System.err.println("Failed to verify TeeEnclaveChallenge signature (exiting)");
            return "";
        }

        return enclaveChallengeSignature.getValue();
    }

    /*
    private static TeeEnclaveChallengeSignature generateTeeEnclaveChallengeSignature(
            String enclaveChallengePrivateKey, String taskId, String workerAddress, String resultDigest) {

        Signature enclaveSignature = signMessageHashAndGetSignature(messageHash, enclaveChallengePrivateKey);

        return TeeEnclaveChallengeSignature.builder()
                .resultDigest(resultDigest)
                .resultHash(resultHash)
                .resultSeal(resultSeal)
                .signature(enclaveSignature)
                .build();
    }
    */

    private static boolean isSignatureValid(String expectedSigner, TeeEnclaveChallengeSignature signature) {
        String messageHash = TeeEnclaveChallengeSignature.getMessageHash(
                signature.getResultHash(),
                signature.getResultSeal());

        // checking the worker will be able to verify it
        return isExpectedSignerOnSignedMessageHash(messageHash,
                signature.getSignature(),
                expectedSigner);
    }




}