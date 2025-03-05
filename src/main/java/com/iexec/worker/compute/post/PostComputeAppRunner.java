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

package com.iexec.worker.compute.post;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.common.worker.api.ExitMessage;
import com.iexec.worker.api.WorkerApiManager;
import com.iexec.worker.compute.post.signer.SignerService;
import com.iexec.worker.compute.post.utils.EnvUtils;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_FAILED_UNKNOWN_ISSUE;
import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_TASK_ID_MISSING;
import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.IEXEC_TASK_ID;

@Slf4j
public class PostComputeAppRunner {
    private final SignerService signerService;

    public PostComputeAppRunner() {
        this.signerService = new SignerService();
    }

    /**
     * Exits:
     * - 0: Success
     * - 1: Failure; Reported cause (known or unknown)
     * - 2: Failure; Unreported cause since reporting issue failed
     * - 3: Failure; Unreported cause since missing taskID context
     */
    public int start() {
        log.info("Tee worker post-compute started");

        String chainTaskId = null;

        try {
            chainTaskId = EnvUtils.getEnvVarOrThrow(IEXEC_TASK_ID.name(), POST_COMPUTE_TASK_ID_MISSING);
        } catch (PostComputeException e) {
            log.error("TEE post-compute cannot go further without taskID context");
            return 3;
        }

        ReplicateStatusCause exitCause = null;
        try {
            final PostComputeApp postComputeApp = createPostComputeApp(chainTaskId);
            postComputeApp.runPostCompute();
            log.info("TEE post-compute completed");
            return 0;
        } catch (PostComputeException e) {
            exitCause = e.getStatusCause();
            log.error("TEE post-compute failed with a known exitCause " +
                            "[errorMessage:{}]",
                    e.getStatusCause(), e);
        } catch (Exception e) {
            exitCause = POST_COMPUTE_FAILED_UNKNOWN_ISSUE;
            log.error("TEE post-compute failed without explicit exitCause", e);
        }

        try {
            WorkerApiManager.getWorkerApiClient()
                    .sendExitCauseForPostComputeStage(signerService.getChallenge(chainTaskId),
                            chainTaskId,
                            new ExitMessage(exitCause));
            return 1;
        } catch (PostComputeException e) {
            log.error("Failed to retrieve authorization [taskId:{}]", chainTaskId, e);
            return 2;
        } catch (FeignException e) {
            log.error("Failed to report exitCause [exitCause:{}]", exitCause, e);
            return 2;
        }
    }

    PostComputeApp createPostComputeApp(String chainTaskId) {
        return new PostComputeApp(chainTaskId);
    }
}
