/*
 * Copyright 2022 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.tee.post.compute;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.common.worker.api.ExitMessage;
import com.iexec.worker.tee.post.compute.utils.EnvUtils;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_TASK_ID_MISSING;
import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_FAILED_UNKNOWN_ISSUE;
import static com.iexec.common.worker.result.ResultUtils.RESULT_TASK_ID;
import static com.iexec.worker.tee.post.compute.worker.WorkerApiManager.getWorkerApiClient;

@Slf4j
public class PostComputeAppRunner {

    /**
     * Exits:
     * - 0: Success
     * - 1: Failure; Reported cause (known or unknown)
     * - 2: Failure; Unreported cause since reporting issue failed
     * - 3: Failure; Unreported cause since missing taskID context
     */
    public void start() {
        log.info("Tee worker post-compute started");

        String chainTaskId = null;
        ReplicateStatusCause exitCause = null;
        Integer exitCode = null;

        try {
            chainTaskId = EnvUtils.getEnvVarOrThrow(RESULT_TASK_ID, POST_COMPUTE_TASK_ID_MISSING);
        } catch (PostComputeException e) {
            log.error("TEE post-compute cannot go further without taskID context");
            exitCode = 3;
        }

        if (exitCode == null) {
            try {
                final PostComputeApp postComputeApp = createPostComputeApp(chainTaskId);
                postComputeApp.runPostCompute();
                log.info("TEE post-compute completed");
                exitCode = 0;
            } catch(PostComputeException e) {
                exitCause = e.getStatusCause();
                log.error("TEE post-compute failed with a known exitCause " +
                                "[errorMessage:{}]",
                        e.getStatusCause(), e);
            } catch (Exception e) {
                exitCause = POST_COMPUTE_FAILED_UNKNOWN_ISSUE;
                log.error("TEE post-compute failed without explicit exitCause", e);
            }
        }

        if (exitCode == null) {
            try {
                getWorkerApiClient()
                        .sendExitCauseForPostComputeStage(chainTaskId,
                                new ExitMessage(exitCause));
                exitCode = 1;
            } catch (FeignException e) {
                exitCode = 2;
                log.error("Failed to report exitCause [exitCause:{}]", exitCause, e);
            }
        }

        System.exit(exitCode);
    }

    PostComputeApp createPostComputeApp(String chainTaskId) {
        return new PostComputeApp(chainTaskId);
    }
}
