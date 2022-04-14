package com.iexec.worker.tee.post.compute.worflow;

import com.iexec.common.result.ComputedFile;
import com.iexec.common.tee.TeeEnclaveChallengeSignature;
import com.iexec.common.utils.FileHelper;
import com.iexec.common.utils.HashUtils;
import com.iexec.common.utils.IexecFileHelper;
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.worker.tee.post.compute.PostComputeException;
import com.iexec.worker.tee.post.compute.signer.SignerService;
import com.iexec.worker.tee.post.compute.utils.EnvUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.iexec.common.replicate.ReplicateStatusCause.*;
import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY;
import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_WORKER_ADDRESS;

@Slf4j
public class FlowManager {

    public static final String WORKER_HOST = "worker:13100";

    /*
     * 1 - readComputedFile
     *
     * */
    public static ComputedFile readComputedFile(String taskId) throws PostComputeException {
        log.info("ReadComputedFile stage started");

        ComputedFile computedFile = IexecFileHelper.readComputedFile(taskId, FileHelper.SLASH_IEXEC_OUT);
        if (computedFile == null) {
            final String cause = "computed.json missing";
            log.error("ReadComputedFile failed (" + cause + ")");
            throw new PostComputeException(POST_COMPUTE_COMPUTED_FILE_NOT_FOUND, cause);
        }

        log.info("ReadComputedFile stage completed");
        return computedFile;
    }

    /*
     * 2 - buildResultDigestInComputedFile
     *
     * */
    public static void buildResultDigestInComputedFile(ComputedFile computedFile, boolean isCallbackMode) throws PostComputeException {
        log.info("ResultDigest stage started [mode:{}]", isCallbackMode ? "web3" : "web2");

        String resultDigest;
        if (isCallbackMode) {
            resultDigest = ResultUtils.computeWeb3ResultDigest(computedFile);
        } else {
            resultDigest = ResultUtils.computeWeb2ResultDigest(computedFile);
        }

        if (resultDigest.isEmpty()) {
            final String cause = "empty resultDigest";
            log.error("ResultDigest stage failed (" + cause + ")");
            throw new PostComputeException(POST_COMPUTE_RESULT_DIGEST_COMPUTATION_FAILED, cause);
        }

        computedFile.setResultDigest(resultDigest);
        log.info("ResultDigest stage completed");
    }

    /*
     * 3 - signComputedFile
     *
     * */
    public static void signComputedFile(ComputedFile computedFile) throws PostComputeException {
        log.info("Signer stage started");

        String workerAddress = EnvUtils.getEnvVarOrThrow(RESULT_SIGN_WORKER_ADDRESS, POST_COMPUTE_MISSING_WORKER_ADDRESS);
        String resultHash = HashUtils.concatenateAndHash(computedFile.getTaskId(), computedFile.getResultDigest());
        String resultSeal = HashUtils.concatenateAndHash(workerAddress, computedFile.getTaskId(), computedFile.getResultDigest());
        String messageHash = TeeEnclaveChallengeSignature.getMessageHash(resultHash, resultSeal);

        String teeChallengePrivateKey = EnvUtils.getEnvVarOrThrow(RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY, POST_COMPUTE_MISSING_TEE_CHALLENGE_PRIVATE_KEY);

        String enclaveSignature = SignerService.signEnclaveChallenge(messageHash, teeChallengePrivateKey);

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
    public static void sendComputedFileToHost(ComputedFile computedFile) throws PostComputeException {
        log.info("Send ComputedFile stage started [computedFile:{}]", computedFile);
        HttpEntity<ComputedFile> request = new HttpEntity<>(computedFile);
        String baseUrl = String.format("http://%s/iexec_out/%s/computed",
                WORKER_HOST, computedFile.getTaskId());
        ResponseEntity<String> response = new RestTemplate()
                .postForEntity(baseUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Send ComputedFile stage failed [taskId:{}, status:{}]",
                    computedFile.getTaskId(), response.getStatusCode());
            throw new PostComputeException(POST_COMPUTE_SEND_COMPUTED_FILE_FAILED);
        }
        log.info("Send ComputedFile stage completed");
    }

}
