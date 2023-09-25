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

package com.iexec.worker.compute.post.worflow;

import com.iexec.common.result.ComputedFile;
import com.iexec.common.utils.IexecFileHelper;
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.commons.poco.tee.TeeEnclaveChallengeSignature;
import com.iexec.commons.poco.utils.HashUtils;
import com.iexec.worker.compute.post.PostComputeException;
import com.iexec.worker.compute.post.signer.SignerService;
import com.iexec.worker.compute.post.utils.EnvUtils;
import com.iexec.worker.compute.post.worker.WorkerApiClient;
import com.iexec.worker.compute.post.worker.WorkerApiManager;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import static com.iexec.common.replicate.ReplicateStatusCause.*;
import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY;
import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_WORKER_ADDRESS;

@Slf4j
public class FlowService {

    private final SignerService signerService;

    public FlowService() {
        this.signerService = new SignerService();
    }

    public FlowService(SignerService signerService) {
        this.signerService = signerService;
    }

    /*
     * 1 - readComputedFile
     *
     * */
    public ComputedFile readComputedFile(String taskId) throws PostComputeException {
        log.info("ReadComputedFile stage started");

        ComputedFile computedFile = IexecFileHelper.readComputedFile(taskId, IexecFileHelper.SLASH_IEXEC_OUT);
        if (computedFile == null) {
            throw new PostComputeException(POST_COMPUTE_COMPUTED_FILE_NOT_FOUND, "computed.json missing");
        }

        log.info("ReadComputedFile stage completed");
        return computedFile;
    }

    /*
     * 2 - buildResultDigestInComputedFile
     *
     * */
    public void buildResultDigestInComputedFile(ComputedFile computedFile, boolean isCallbackMode) throws PostComputeException {
        log.info("ResultDigest stage started [mode:{}]", isCallbackMode ? "web3" : "web2");

        String resultDigest;
        if (isCallbackMode) {
            resultDigest = ResultUtils.computeWeb3ResultDigest(computedFile);
        } else {
            resultDigest = ResultUtils.computeWeb2ResultDigest(computedFile);
        }

        if (resultDigest.isEmpty()) {
            throw new PostComputeException(POST_COMPUTE_RESULT_DIGEST_COMPUTATION_FAILED, "empty resultDigest");
        }

        computedFile.setResultDigest(resultDigest);
        log.info("ResultDigest stage completed");
    }

    /*
     * 3 - signComputedFile
     *
     * */
    public void signComputedFile(ComputedFile computedFile) throws PostComputeException {
        log.info("Signer stage started");

        String workerAddress = EnvUtils.getEnvVarOrThrow(RESULT_SIGN_WORKER_ADDRESS, POST_COMPUTE_WORKER_ADDRESS_MISSING);
        String resultHash = HashUtils.concatenateAndHash(computedFile.getTaskId(), computedFile.getResultDigest());
        String resultSeal = HashUtils.concatenateAndHash(workerAddress, computedFile.getTaskId(), computedFile.getResultDigest());
        String messageHash = TeeEnclaveChallengeSignature.getMessageHash(resultHash, resultSeal);

        String teeChallengePrivateKey = EnvUtils.getEnvVarOrThrow(RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY, POST_COMPUTE_TEE_CHALLENGE_PRIVATE_KEY_MISSING);

        String enclaveSignature = signerService.signEnclaveChallenge(messageHash, teeChallengePrivateKey);

        computedFile.setEnclaveSignature(enclaveSignature);
        log.info("Signer stage completed");
    }

    /*
     * 4 - sendComputedFileToHost
     *
     * Let's make the ComputedFile available for worker contribute/reveal & core
     * finalize.
     * At this time, for security purposes, a Java enclave must be bundled inside
     * a Scone BinaryFS. With BinaryFS, only Scone volumes (defined in palaemon
     * session) can be shared from one component to another. Untrusted volumes
     * cannot be shared to the host.
     * To solve this, the untrusted volume (here the computed file) is transferred
     * to the host over HTTP.
     * In order to have the feature working, following conditions are required:
     * - iexec-worker and tee-worker-post-compute containers must be in the
     *   same network
     * - iexec-worker within network should be accessible on `worker:13100`
     *   (domain_name:port)
     * */
    public void sendComputedFileToHost(ComputedFile computedFile) throws PostComputeException {
        log.info("Send ComputedFile stage started [computedFile:{}]", computedFile);
        final WorkerApiClient workerApiClient = WorkerApiManager.getWorkerApiClient();

        try {
            workerApiClient.sendComputedFileToHost(computedFile.getTaskId(), computedFile);
        } catch (FeignException e) {
            throw new PostComputeException(
                    POST_COMPUTE_SEND_COMPUTED_FILE_FAILED,
                    String.format("Send ComputedFile stage failed [taskId:%s]", computedFile.getTaskId()),
                    e
            );
        }

        log.info("Send ComputedFile stage completed");
    }
}
