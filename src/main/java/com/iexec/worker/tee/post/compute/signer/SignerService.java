package com.iexec.worker.tee.post.compute.signer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexec.common.security.Signature;
import com.iexec.common.tee.TeeEnclaveChallengeSignature;
import com.iexec.common.utils.CredentialsUtils;
import com.iexec.common.utils.HashUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.iexec.common.utils.SignatureUtils.isExpectedSignerOnSignedMessageHash;
import static com.iexec.common.utils.SignatureUtils.signMessageHashAndGetSignature;
import static com.iexec.worker.tee.post.compute.utils.FilesUtils.*;

@Slf4j
public class SignerService {

    public static boolean signEnclaveChallengeAndWriteSignature(String resultDigest, String enclaveChallengePrivateKey,
                                                                String taskId, String workerAddress) {
        TeeEnclaveChallengeSignature enclaveChallengeSignature =
                generateTeeEnclaveChallengeSignature(enclaveChallengePrivateKey, taskId, workerAddress, resultDigest);
        if (enclaveChallengeSignature == null) {
            System.err.println("Failed to generate TeeEnclaveChallenge signature (exiting)");
            return false;
        }

        boolean isSignatureValid = isSignatureValid(CredentialsUtils.getAddress(enclaveChallengePrivateKey), enclaveChallengeSignature);
        if (!isSignatureValid) {
            System.err.println("Failed to verify TeeEnclaveChallenge signature (exiting)");
            return false;
        }

        boolean isSignatureFileCreated = writeTeeEnclaveChallengeSignatureFile(enclaveChallengeSignature,
                UNPROTECTED_IEXEC_OUT + SLASH_ENCLAVE_SIG_FILE);
        if (!isSignatureFileCreated) {
            System.err.println("Failed to write TeeEnclaveChallenge signature (exiting)");
            return false;
        }

        return true;
    }

    private static TeeEnclaveChallengeSignature generateTeeEnclaveChallengeSignature(
            String enclaveChallengePrivateKey, String taskId, String workerAddress, String resultDigest) {
        String resultHash = HashUtils.concatenateAndHash(taskId, resultDigest);
        String resultSeal = HashUtils.concatenateAndHash(workerAddress, taskId, resultDigest);

        String messageHash = TeeEnclaveChallengeSignature.getMessageHash(resultHash, resultSeal);

        Signature enclaveSignature = signMessageHashAndGetSignature(messageHash, enclaveChallengePrivateKey);

        return TeeEnclaveChallengeSignature.builder()
                .resultDigest(resultDigest)
                .resultHash(resultHash)
                .resultSeal(resultSeal)
                .signature(enclaveSignature)
                .build();
    }

    private static boolean isSignatureValid(String expectedSigner, TeeEnclaveChallengeSignature signature) {
        String messageHash = TeeEnclaveChallengeSignature.getMessageHash(
                signature.getResultHash(),
                signature.getResultSeal());

        // checking the worker will be able to verify it
        return isExpectedSignerOnSignedMessageHash(messageHash,
                signature.getSignature(),
                expectedSigner);
    }

    private static boolean writeTeeEnclaveChallengeSignatureFile(TeeEnclaveChallengeSignature enclaveChallengeSignature, String outputFilePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(enclaveChallengeSignature);
            log.info(json);
            Files.write(Paths.get(outputFilePath), json.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


}