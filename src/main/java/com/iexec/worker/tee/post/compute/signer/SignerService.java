package com.iexec.worker.tee.post.compute.signer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexec.common.security.Signature;
import com.iexec.common.tee.TeeEnclaveChallengeSignature;
import com.iexec.common.utils.CredentialsUtils;
import com.iexec.common.utils.HashUtils;
import lombok.extern.slf4j.Slf4j;
import org.web3j.crypto.Hash;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.iexec.common.utils.BytesUtils.bytesToString;
import static com.iexec.common.utils.SignatureUtils.isExpectedSignerOnSignedMessageHash;
import static com.iexec.common.utils.SignatureUtils.signMessageHashAndGetSignature;

@Slf4j
public class SignerService {

    private static final String ENCLAVE_SIG_DOT_IEXEC = "/iexec_out/enclaveSig.iexec";

    public static boolean signEnclaveChallengeAndWriteSignature(String uploadedResultFile, String enclaveChallengePrivateKey,
                                                                String taskId, String workerAddress) {

        //String resultFilePath = getIexecOutZipPath();
        String resultDigest = SignerService.getResultDigestOfFile(uploadedResultFile); //TODO: change that to uploaded.iexec file

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

        boolean isSignatureFileCreated = writeTeeEnclaveChallengeSignatureFile(enclaveChallengeSignature, ENCLAVE_SIG_DOT_IEXEC);
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


    private static String getResultDigestOfFile(String filePath) {
        byte[] content = new byte[0];
        try {
            content = Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            log.info("Failed to getResultDigestOfFile");
        }
        return bytesToString(Hash.sha256(content));
    }

}