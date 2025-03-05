/*
 * Copyright 2022-2025 IEXEC BLOCKCHAIN TECH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iexec.worker.compute.post.signer;

import com.iexec.commons.poco.utils.HashUtils;
import com.iexec.commons.poco.utils.SignatureUtils;
import com.iexec.worker.compute.post.PostComputeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_INVALID_TEE_SIGNATURE;
import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.SIGN_TEE_CHALLENGE_PRIVATE_KEY;
import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.SIGN_WORKER_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(SystemStubsExtension.class)
@ExtendWith(MockitoExtension.class)
class SignerServiceTests {

    private static final String CHAIN_TASK_ID = "0x123456789abcdef";
    private static final String WORKER_ADDRESS = "0xabcdef123456789";
    private static final String ENCLAVE_CHALLENGE_PRIVATE_KEY = "0xdd3b993ec21c71c1f6d63a5240850e0d4d8dd83ff70d29e49247958548c1d479";
    private static final String MESSAGE_HASH = "0x5cd0e9c5180dd35e2b8285d0db4ded193a9b4be6fbfab90cbadccecab130acad";
    private static final String EXPECTED_CHALLENGE = "0xfcc6bce5eb04284c2eb1ed14405b943574343b1abda33628fbf94a374b18dd16541c6ebf63c6943d8643ff03c7aa17f1cb17b0a8d297d0fd95fc914bdd0e85f81b";

    @Spy
    SignerService signerService = new SignerService();

    @Test
    void shouldSignEnclaveChallenge() {
        final String actualChallenge = Assertions.assertDoesNotThrow(() -> signerService.signEnclaveChallenge(MESSAGE_HASH, ENCLAVE_CHALLENGE_PRIVATE_KEY));
        assertEquals(EXPECTED_CHALLENGE, actualChallenge);
    }

    @Test
    void shouldNotSignEnclaveChallengeSinceInvalidTeeSignature() {
        try (MockedStatic<SignatureUtils> signatureUtils = Mockito.mockStatic(SignatureUtils.class)) {
            signatureUtils.when(() -> SignatureUtils.isExpectedSignerOnSignedMessageHash(any(), any(), any()))
                    .thenReturn(false);

            final PostComputeException exception = assertThrows(PostComputeException.class, () -> signerService.signEnclaveChallenge(MESSAGE_HASH, ENCLAVE_CHALLENGE_PRIVATE_KEY));
            assertEquals(POST_COMPUTE_INVALID_TEE_SIGNATURE, exception.getStatusCause());
            assertEquals("Failed to verify TeeEnclaveChallenge signature (exiting)", exception.getMessage());
        }
    }

    @Test
    void shouldGetChallenge(EnvironmentVariables environment) throws PostComputeException {
        environment.set(SIGN_WORKER_ADDRESS.name(), WORKER_ADDRESS);
        environment.set(SIGN_TEE_CHALLENGE_PRIVATE_KEY.name(), ENCLAVE_CHALLENGE_PRIVATE_KEY);

        try (final MockedStatic<HashUtils> hashUtilsMock = mockStatic(HashUtils.class)) {
            hashUtilsMock.when(() -> HashUtils.concatenateAndHash(CHAIN_TASK_ID, WORKER_ADDRESS))
                    .thenReturn(MESSAGE_HASH);

            doReturn(EXPECTED_CHALLENGE)
                    .when(signerService)
                    .signEnclaveChallenge(MESSAGE_HASH, ENCLAVE_CHALLENGE_PRIVATE_KEY);

            final String actualChallenge = signerService.getChallenge(CHAIN_TASK_ID);
            assertEquals(EXPECTED_CHALLENGE, actualChallenge);
        }
    }

}
