/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.iexec.worker.tee.post.compute;

import com.iexec.worker.tee.post.compute.encrypter.EncryptionService;
import com.iexec.worker.tee.post.compute.uploader.UploaderService;
import com.iexec.worker.tee.post.compute.utils.EnvUtils;
import com.iexec.worker.tee.post.compute.utils.FilesUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Base64;

import static com.iexec.common.utils.FileHelper.zipFolder;
import static com.iexec.worker.tee.post.compute.encrypter.EncryptionService.ENCRYPTION_REQUESTED;
import static com.iexec.worker.tee.post.compute.encrypter.EncryptionService.NO_ENCRYPTION;
import static com.iexec.worker.tee.post.compute.signer.SignerService.signEnclaveChallengeAndWriteSignature;
import static com.iexec.worker.tee.post.compute.uploader.UploaderService.DROPBOX_STORAGE;
import static com.iexec.worker.tee.post.compute.uploader.UploaderService.IPFS_STORAGE;
import static com.iexec.worker.tee.post.compute.utils.FilesUtils.IEXEC_OUT_PATH;

@Slf4j
public class App {

    //TODO: move these fields to common
    private static final String TASK_ID = "TASK_ID";
    private static final String WORKER_ADDRESS = "WORKER_ADDRESS";
    private static final String IEXEC_REQUESTER_RESULT_ENCRYPTION = "IEXEC_REQUESTER_RESULT_ENCRYPTION";
    private static final String IEXEC_REQUESTER_STORAGE_LOCATION = "IEXEC_REQUESTER_STORAGE_LOCATION";
    private static final String IEXEC_REQUESTER_STORAGE_PROXY = "IEXEC_REQUESTER_STORAGE_PROXY";
    private static final String BENEFICIARY_PUBLIC_KEY_BASE64 = "BENEFICIARY_PUBLIC_KEY_BASE64";
    private static final String REQUESTER_STORAGE_TOKEN = "REQUESTER_STORAGE_TOKEN";
    private static final String TEE_CHALLENGE_PRIVATE_KEY = "TEE_CHALLENGE_PRIVATE_KEY";

    public static void main(String[] args) {
        log.info("Tee worker post-compute started");
        String taskId = EnvUtils.getEnvVarOrExit(TASK_ID);

        log.info("DEBUG - env: " + System.getenv().toString());

        String resultPath = prepareResult(IEXEC_OUT_PATH);
        String resultToUpload = eventuallyEncryptResult(resultPath);
        System.out.println(resultToUpload);
        String resultLink = uploadResult(taskId, resultToUpload);
        System.out.println(resultLink);
        signResult(taskId, resultToUpload);

        log.info("Tee worker post-compute completed!");
    }

    private static String prepareResult(String iexecOutPath) {
        if (!FilesUtils.isCompletedComputeFilePresent()) {
            log.error("Preparation stage failed (isCompletedComputeFilePresent)");
            exit();
        }

        File iexecOutZip = zipFolder(iexecOutPath);
        if (iexecOutZip == null) {
            log.error("Preparation stage failed (iexecOutZip)");
            exit();
        }
        return iexecOutZip.getAbsolutePath();
    }

    private static String eventuallyEncryptResult(String inDataFilePath) {
        log.info("Encryption stage started");
        String fileToUpload;
        String resultEncryptionMode = EnvUtils.getEnvVar(IEXEC_REQUESTER_RESULT_ENCRYPTION);

        switch (resultEncryptionMode) {
            case NO_ENCRYPTION:
                log.info("Encryption stage mode: NO_ENCRYPTION");
                fileToUpload = inDataFilePath;
                break;
            case ENCRYPTION_REQUESTED:
            default:
                log.info("Encryption stage mode: ENCRYPTION_REQUESTED");
                String beneficiaryRsaPublicKeyBase64 = EnvUtils.getEnvVarOrExit(BENEFICIARY_PUBLIC_KEY_BASE64);
                String plainTextBeneficiaryRsaPublicKey = new String(Base64.getDecoder().decode(beneficiaryRsaPublicKeyBase64));
                fileToUpload = EncryptionService.encryptData(inDataFilePath, plainTextBeneficiaryRsaPublicKey, true);
                break;
        }
        if (fileToUpload.isEmpty()) {
            log.error("Encryption stage failed");
            exit();
        } else {
            log.info("Encryption stage completed");
        }
        return fileToUpload;
    }

    private static String uploadResult(String taskId, String fileToUploadPath) {
        log.info("Upload stage started");
        String storageLocation = EnvUtils.getEnvVar(IEXEC_REQUESTER_STORAGE_LOCATION);
        String storageProxy = EnvUtils.getEnvVar(IEXEC_REQUESTER_STORAGE_PROXY);
        String storageToken = EnvUtils.getEnvVarOrExit(REQUESTER_STORAGE_TOKEN);

        String resultLink = "";
        switch (storageLocation) {
            case DROPBOX_STORAGE:
                log.info("Upload stage mode: DROPBOX_STORAGE");
                String remoteFilename = taskId + ".zip";
                resultLink = UploaderService.uploadToDropBox(fileToUploadPath, storageToken, remoteFilename);
                break;
            case IPFS_STORAGE:
            default:
                log.info("Upload stage mode: IPFS_STORAGE");
                //String baseUrl = "http://core:18090/results";
                resultLink = UploaderService.uploadToIpfsWithIexecProxy(taskId, storageProxy, storageToken, fileToUploadPath);
                break;
        }
        if (resultLink.isEmpty()) {
            log.error("Upload stage failed");
            exit();
        }

        log.info("Upload stage completed");
        return resultLink;
    }

    //TODO Add result link to signature (uploaded.iexec?)
    private static void signResult(String taskId, String resultToUpload) {
        log.info("Signing stage started");
        String teeChallengePrivateKey = EnvUtils.getEnvVarOrExit(TEE_CHALLENGE_PRIVATE_KEY);
        String workerAddress = EnvUtils.getEnvVarOrExit(WORKER_ADDRESS);
        boolean isSignatureFileCreated = signEnclaveChallengeAndWriteSignature(resultToUpload, teeChallengePrivateKey, taskId, workerAddress);
        if (!isSignatureFileCreated) {
            log.error("Signing stage failed");
            exit();
        } else {
            log.info("Signing stage completed");
        }
    }

    private static void exit() {
        System.exit(1);
    }


}
