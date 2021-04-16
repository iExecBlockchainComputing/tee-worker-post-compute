package com.iexec.worker.tee.post.compute.worflow;

import com.iexec.common.result.ComputedFile;
import com.iexec.common.tee.TeeEnclaveChallengeSignature;
import com.iexec.common.utils.FileHelper;
import com.iexec.common.utils.HashUtils;
import com.iexec.common.utils.IexecFileHelper;
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.worker.tee.post.compute.signer.SignerService;
import com.iexec.worker.tee.post.compute.utils.EnvUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY;
import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_WORKER_ADDRESS;
import static com.iexec.worker.tee.post.compute.utils.EnvUtils.exit;

@Slf4j
public class FlowManager {

    /*
     * 1 - readComputedFile
     *
     * */
    public static ComputedFile readComputedFile(String taskId) {
        log.info("ReadComputedFile stage started");

        ComputedFile computedFile = IexecFileHelper.readComputedFile(taskId, FileHelper.SLASH_IEXEC_OUT);
        if (computedFile == null) {
            log.error("ReadComputedFile failed (computed.json missing)");
            exit();
        }

        log.info("ReadComputedFile stage completed");
        return computedFile;
    }

    /*
     * 2 - buildResultDigestInComputedFile
     *
     * */
    public static void buildResultDigestInComputedFile(ComputedFile computedFile, boolean isCallbackMode) {
        log.info("ResultDigest stage started [mode:{}]", isCallbackMode ? "web3" : "web2");

        String resultDigest;
        if (isCallbackMode) {
            resultDigest = ResultUtils.computeWeb3ResultDigest(computedFile);
        } else {
            resultDigest = ResultUtils.computeWeb2ResultDigest(computedFile);
        }

        if (resultDigest.isEmpty()) {
            log.error("ResultDigest stage failed (empty resultDigest)");
            exit();
        }

        computedFile.setResultDigest(resultDigest);
        log.info("ResultDigest stage completed");
    }

    /*
     * 3 - signComputedFile
     *
     * */
    public static void signComputedFile(ComputedFile computedFile) {
        log.info("Signer stage started");

        String workerAddress = EnvUtils.getEnvVarOrExit(RESULT_SIGN_WORKER_ADDRESS);
        String resultHash = HashUtils.concatenateAndHash(computedFile.getTaskId(), computedFile.getResultDigest());
        String resultSeal = HashUtils.concatenateAndHash(workerAddress, computedFile.getTaskId(), computedFile.getResultDigest());
        String messageHash = TeeEnclaveChallengeSignature.getMessageHash(resultHash, resultSeal);

        String teeChallengePrivateKey = EnvUtils.getEnvVarOrExit(RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY);

        String enclaveSignature = SignerService.signEnclaveChallenge(messageHash, teeChallengePrivateKey);
        if (enclaveSignature.isEmpty()) {
            log.error("Signer stage failed");
            exit();
        }

        computedFile.setEnclaveSignature(enclaveSignature);
        log.info("Signer stage completed");
    }

    /*
     * 4 - sendComputedFileToHost
     * Let's make ComputedFile available for worker contribute/reveal & core finalize
     * Required:
     * - iexec-worker and tee-worker-post-compute containers must be in the same network
     * - iexec-worker within network should be accessible on `worker:13100` (domain_name:port)
     * */
    public static void sendComputedFileToHost(ComputedFile computedFile) {
        log.info("sendComputedFile stage started [computedFile:{}]", computedFile);
        HttpEntity<ComputedFile> request = new HttpEntity<>(computedFile);
        String baseUrl = String.format("http://worker:13100/iexec_out/%s/computed",
                computedFile.getTaskId());
        ResponseEntity<String> response = new RestTemplate()
                .postForEntity(baseUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("sendComputedFile stage failed [taskId:{}, status:{}]",
                    computedFile.getTaskId(), response.getStatusCode());
            exit();
        }
        log.info("sendComputedFile stage completed");
    }

}
