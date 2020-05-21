package com.iexec.worker.tee.post.compute.worflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexec.common.result.ComputedFile;
import com.iexec.common.tee.TeeEnclaveChallengeSignature;
import com.iexec.common.utils.FileHelper;
import com.iexec.common.utils.HashUtils;
import com.iexec.common.utils.IexecFileHelper;
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.worker.tee.post.compute.signer.SignerService;
import com.iexec.worker.tee.post.compute.utils.EnvUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
     * 4 - copyComputedFileToHost
     * Let's make ComputedFile available for worker contribute/reveal & core finalize
     * */
    public static void copyComputedFileToHost(ComputedFile computedFile) {
        log.info("CopyToHost stage started");
        String outputFilePath = FileHelper.SLASH_OUTPUT + File.separator + IexecFileHelper.COMPUTED_JSON;

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(computedFile);
            log.info(json);
            Files.write(Paths.get(outputFilePath), json.getBytes());
        } catch (IOException e) {
            log.error("CopyToHost stage failed (computed.json copy failed)");
            exit();
        }
        log.info("CopyToHost stage completed");
    }

}
