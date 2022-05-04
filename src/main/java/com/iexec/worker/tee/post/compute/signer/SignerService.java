package com.iexec.worker.tee.post.compute.signer;

import com.iexec.common.security.Signature;
import com.iexec.common.utils.CredentialsUtils;
import com.iexec.worker.tee.post.compute.PostComputeException;
import lombok.extern.slf4j.Slf4j;

import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_INVALID_TEE_SIGNATURE;
import static com.iexec.common.utils.SignatureUtils.isExpectedSignerOnSignedMessageHash;
import static com.iexec.common.utils.SignatureUtils.signMessageHashAndGetSignature;

@Slf4j
public class SignerService {


    public static String signEnclaveChallenge(String messageHash, String enclaveChallengePrivateKey) throws PostComputeException {
        Signature enclaveChallengeSignature = signMessageHashAndGetSignature(messageHash, enclaveChallengePrivateKey);

        boolean isSignatureValid = isExpectedSignerOnSignedMessageHash(messageHash, enclaveChallengeSignature,
                CredentialsUtils.getAddress(enclaveChallengePrivateKey));

        if (!isSignatureValid) {
            throw new PostComputeException(
                    POST_COMPUTE_INVALID_TEE_SIGNATURE,
                    "Failed to verify TeeEnclaveChallenge signature (exiting)"
            );
        }

        return enclaveChallengeSignature.getValue();
    }

}