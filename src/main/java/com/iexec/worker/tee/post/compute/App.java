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

import static com.iexec.common.tee.TeeUtils.booleanFromYesNo;
import static com.iexec.common.utils.FileHelper.zipFolder;
import static com.iexec.common.worker.result.ResultUtils.*;
import static com.iexec.worker.tee.post.compute.encrypter.EncryptionService.ENCRYPTION_REQUESTED;
import static com.iexec.worker.tee.post.compute.encrypter.EncryptionService.NO_ENCRYPTION;
import static com.iexec.worker.tee.post.compute.signer.SignerService.signEnclaveChallengeAndWriteSignature;
import static com.iexec.worker.tee.post.compute.uploader.UploaderService.DROPBOX_STORAGE;
import static com.iexec.worker.tee.post.compute.uploader.UploaderService.IPFS_STORAGE;
import static com.iexec.worker.tee.post.compute.utils.FilesUtils.*;

@Slf4j
public class App {

    public static void main(String[] args) {
        log.info("Tee worker post-compute started");
        String taskId = EnvUtils.getEnvVarOrExit(RESULT_TASK_ID);

        log.info("DEBUG - env: " + System.getenv().toString());

        String resultDigest;
        boolean shouldCallback = booleanFromYesNo(EnvUtils.getEnvVar(RESULT_STORAGE_CALLBACK));

        if (shouldCallback) {
            resultDigest = getCallbackDigest(PROTECTED_IEXEC_OUT + SLASH_CALLBACK_FILE);
        } else {
            String iexecOutZipPath = zipIexecOut(PROTECTED_IEXEC_OUT);
            String resultPath = eventuallyEncryptResult(iexecOutZipPath);
            String resultLink = uploadResult(taskId, resultPath); //TODO Put resultLink somewhere
            resultDigest = getIexecOutZipDigest(resultPath);
        }
        signResult(taskId, resultDigest);

        log.info("Tee worker post-compute completed!");
    }

    private static String getCallbackDigest(String resultPath) {
        log.info("Callback stage started");

        String resultDigest = getCallbackDataFromPath(resultPath);
        if (resultDigest.isEmpty()) {
            log.error("Callback stage failed (empty resultDigest)");
            exit();
        }

        boolean isCallbackCopied = copyCallbackToUnprotected();
        if (!isCallbackCopied) {
            log.error("Callback stage failed (callback copy failed)");
            exit();
        }

        log.info("Callback stage completed");
        return resultDigest;
    }

    private static String zipIexecOut(String iexecOutPath) {
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
        String resultEncryptionMode = EnvUtils.getEnvVar(RESULT_ENCRYPTION);

        // TODO: use true/false instead of encryptionMode (since we have only two options)
        switch (resultEncryptionMode) {
            case NO_ENCRYPTION:
                log.info("Encryption stage mode: NO_ENCRYPTION");
                fileToUpload = inDataFilePath;
                break;
            case ENCRYPTION_REQUESTED:
            default:
                log.info("Encryption stage mode: ENCRYPTION_REQUESTED");
                String beneficiaryRsaPublicKeyBase64 = EnvUtils.getEnvVarOrExit(RESULT_ENCRYPTION_PUBLIC_KEY);
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
        String storageProvider = EnvUtils.getEnvVar(RESULT_STORAGE_PROVIDER);
        String storageProxy = EnvUtils.getEnvVar(RESULT_STORAGE_PROXY);
        String storageToken = EnvUtils.getEnvVarOrExit(RESULT_STORAGE_TOKEN);

        String resultLink = "";
        switch (storageProvider) {
            case DROPBOX_STORAGE:
                log.info("Upload stage mode: DROPBOX_STORAGE");
                String remoteFilename = taskId + ".zip";
                resultLink = UploaderService.uploadToDropBox(fileToUploadPath, storageToken, remoteFilename);
                break;
            case IPFS_STORAGE:
            default:
                log.info("Upload stage mode: IPFS_STORAGE");
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

    //TODO Add result link to signature ?
    private static void signResult(String taskId, String resultDigest) {
        log.info("Signing stage started");
        String teeChallengePrivateKey = EnvUtils.getEnvVarOrExit(RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY);
        String workerAddress = EnvUtils.getEnvVarOrExit(RESULT_SIGN_WORKER_ADDRESS);


        boolean isSignatureFileCreated = signEnclaveChallengeAndWriteSignature(resultDigest, teeChallengePrivateKey, taskId, workerAddress);
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
