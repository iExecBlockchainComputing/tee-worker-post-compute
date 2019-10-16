package com.iexec.uploader.dropbox.signer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexec.common.security.Signature;
import com.iexec.common.tee.TeeEnclaveChallengeSignature;
import com.iexec.common.utils.CredentialsUtils;
import com.iexec.common.utils.HashUtils;
import org.web3j.crypto.Hash;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.iexec.common.utils.BytesUtils.bytesToString;
import static com.iexec.common.utils.SignatureUtils.isExpectedSignerOnSignedMessageHash;
import static com.iexec.common.utils.SignatureUtils.signMessageHashAndGetSignature;

public class SignerService {

    public static boolean signEnclaveChallengeAndWriteSignature(String resultFilePath, String enclaveChallengePrivateKey,
                                                                String taskId, String workerAddress) {
        String resultDigest = SignerService.getResultDigestOfFile(resultFilePath); //TODO: change that to uploaded.iexec file

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

        boolean isSignatureFileCreated = writeTeeEnclaveChallengeSignatureFile(enclaveChallengeSignature);
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

    private static boolean writeTeeEnclaveChallengeSignatureFile(TeeEnclaveChallengeSignature enclaveChallengeSignature) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(enclaveChallengeSignature);
            System.out.println(json);
            Files.write(Paths.get("/iexec_out/enclaveSig.iexec"), json.getBytes());
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
            System.out.println("Failed to getResultDigestOfFile");
        }
        return bytesToString(Hash.sha256(content));
    }

}