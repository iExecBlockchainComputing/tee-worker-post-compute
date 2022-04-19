/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.iexec.worker.tee.post.compute;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.common.result.ComputedFile;
import com.iexec.common.utils.FeignBuilder;
import com.iexec.common.worker.api.ExitMessage;
import com.iexec.worker.tee.post.compute.utils.EnvUtils;
import com.iexec.worker.tee.post.compute.web2.Web2ResultManager;
import com.iexec.worker.tee.post.compute.worker.WorkerApiClient;
import feign.FeignException;
import feign.Logger;
import lombok.extern.slf4j.Slf4j;

import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_MISSING_TASK_ID;
import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_UNKNOWN_ISSUE;
import static com.iexec.common.tee.TeeUtils.booleanFromYesNo;
import static com.iexec.common.worker.result.ResultUtils.RESULT_STORAGE_CALLBACK;
import static com.iexec.common.worker.result.ResultUtils.RESULT_TASK_ID;
import static com.iexec.worker.tee.post.compute.worflow.FlowManager.*;

@Slf4j
public class App {

    private App() {
    }

    /**
     * Exits:
     * - 0: Success
     * - 1: Failure; Reported cause (known or unknown)
     * - 2: Failure; Unreported cause since reporting issue
     * - 3: Failure; Unreported cause since missing taskID context
     */
    public static void main(String[] args) {
        log.info("Tee worker post-compute started");

        String chainTaskId = null;
        ReplicateStatusCause exitCause = POST_COMPUTE_UNKNOWN_ISSUE;

        try {
            chainTaskId = EnvUtils.getEnvVarOrThrow(RESULT_TASK_ID, POST_COMPUTE_MISSING_TASK_ID);
        } catch (PostComputeException e) {
            log.error("TEE post-compute cannot go further without taskID context");
            System.exit(3);
        }

        try {
            runPostCompute(chainTaskId);
            log.info("TEE post-compute completed");
            System.exit(0);
        } catch(PostComputeException e) {
            exitCause = e.getStatusCause();
            log.error("TEE post-compute failed with a known exitCause " +
                            "[errorMessage:{}]",
                    e.getStatusCause(), e);
        } catch (Exception e) {
            log.error("TEE post-compute failed without explicit exitCause", e);
        }

        try {
            FeignBuilder
                    .createBuilder(Logger.Level.FULL)
                    .target(WorkerApiClient.class, "http://" + WORKER_HOST)
                    .sendExitCauseForPosComputeStage(chainTaskId,
                            new ExitMessage(exitCause));
            System.exit(1);
        } catch (FeignException e) {
            log.error("Failed to report exitCause [exitCause:{}]", exitCause, e);
            System.exit(2);
        }
    }

    private static void runPostCompute(String taskId) throws PostComputeException {
        boolean shouldCallback = booleanFromYesNo(EnvUtils.getEnvVar(RESULT_STORAGE_CALLBACK));


        ComputedFile computedFile = readComputedFile(taskId);

        buildResultDigestInComputedFile(computedFile, shouldCallback);

        if (!shouldCallback) {
            Web2ResultManager.encryptAndUploadResult(taskId);
        }

        signComputedFile(computedFile);
        sendComputedFileToHost(computedFile);
    }

}
