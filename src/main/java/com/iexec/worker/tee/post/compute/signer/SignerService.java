package com.iexec.worker.tee.post.compute.signer;

import com.iexec.common.security.Signature;
import com.iexec.common.utils.CredentialsUtils;
import com.iexec.worker.tee.post.compute.PostComputeException;
import com.iexec.worker.tee.post.compute.exit.PostComputeExitCode;
import lombok.extern.slf4j.Slf4j;

import static com.iexec.common.utils.SignatureUtils.isExpectedSignerOnSignedMessageHash;
import static com.iexec.common.utils.SignatureUtils.signMessageHashAndGetSignature;

@Slf4j
public class SignerService {


    public static String signEnclaveChallenge(String messageHash, String enclaveChallengePrivateKey) throws PostComputeException {
        Signature enclaveChallengeSignature = signMessageHashAndGetSignature(messageHash, enclaveChallengePrivateKey);

        boolean isSignatureValid = isExpectedSignerOnSignedMessageHash(messageHash, enclaveChallengeSignature,
                CredentialsUtils.getAddress(enclaveChallengePrivateKey));

        if (!isSignatureValid) {
            log.error("Failed to verify TeeEnclaveChallenge signature (exiting)");
            throw new PostComputeException(PostComputeExitCode.INVALID_TEE_SIGNATURE);
        }

        return enclaveChallengeSignature.getValue();
    }

}