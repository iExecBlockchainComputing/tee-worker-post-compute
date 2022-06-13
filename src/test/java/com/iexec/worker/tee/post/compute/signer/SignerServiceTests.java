package com.iexec.worker.tee.post.compute.signer;

import com.iexec.common.utils.SignatureUtils;
import com.iexec.worker.tee.post.compute.PostComputeException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_INVALID_TEE_SIGNATURE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class SignerServiceTests {

    SignerService signerService = new SignerService();

    @Test
    void shouldSignEnclaveChallenge() {
        final String messageHash = "0x5cd0e9c5180dd35e2b8285d0db4ded193a9b4be6fbfab90cbadccecab130acad";
        final String enclaveChallengePrivateKey = "0xdd3b993ec21c71c1f6d63a5240850e0d4d8dd83ff70d29e49247958548c1d479";

        final String expectedChallenge = "0xfcc6bce5eb04284c2eb1ed14405b943574343b1abda33628fbf94a374b18dd16541c6ebf63c6943d8643ff03c7aa17f1cb17b0a8d297d0fd95fc914bdd0e85f81b";

        final String actualChallenge = assertDoesNotThrow(() -> signerService.signEnclaveChallenge(messageHash, enclaveChallengePrivateKey));
        assertEquals(expectedChallenge, actualChallenge);
    }

    @Test
    void shouldNotSignEnclaveChallengeSinceInvalidTeeSignature() {
        final String messageHash = "0x5cd0e9c5180dd35e2b8285d0db4ded193a9b4be6fbfab90cbadccecab130acad";
        final String enclaveChallengePrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";

        try (MockedStatic<SignatureUtils> signatureUtils = Mockito.mockStatic(SignatureUtils.class)) {
            signatureUtils.when(() -> SignatureUtils.isExpectedSignerOnSignedMessageHash(any(), any(), any()))
                    .thenReturn(false);

            final PostComputeException exception = assertThrows(PostComputeException.class, () -> signerService.signEnclaveChallenge(messageHash, enclaveChallengePrivateKey));
            assertEquals(POST_COMPUTE_INVALID_TEE_SIGNATURE, exception.getStatusCause());
            assertEquals("Failed to verify TeeEnclaveChallenge signature (exiting)", exception.getMessage());
        }
    }

}