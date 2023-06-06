/*
 * Copyright 2022-2023 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.tee.post.compute.signer;

import com.iexec.commons.poco.security.Signature;
import com.iexec.commons.poco.utils.CredentialsUtils;
import com.iexec.worker.tee.post.compute.PostComputeException;
import lombok.extern.slf4j.Slf4j;

import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_INVALID_TEE_SIGNATURE;
import static com.iexec.commons.poco.utils.SignatureUtils.isExpectedSignerOnSignedMessageHash;
import static com.iexec.commons.poco.utils.SignatureUtils.signMessageHashAndGetSignature;

@Slf4j
public class SignerService {


    public String signEnclaveChallenge(String messageHash, String enclaveChallengePrivateKey) throws PostComputeException {
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