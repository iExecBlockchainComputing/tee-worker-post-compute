package com.iexec.worker.tee.post.compute.web2;

import com.iexec.common.utils.FileHelper;
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.worker.tee.post.compute.utils.EnvUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Base64;

import static com.iexec.common.chain.DealParams.DROPBOX_RESULT_STORAGE_PROVIDER;
import static com.iexec.common.chain.DealParams.IPFS_RESULT_STORAGE_PROVIDER;
import static com.iexec.common.tee.TeeUtils.booleanFromYesNo;
import static com.iexec.common.worker.result.ResultUtils.*;
import static com.iexec.worker.tee.post.compute.utils.EnvUtils.exit;

@Slf4j
public class Web2ResultManager {

    public static final String SLASH_SCONE = File.separator + "scone";
    /*
     * Manager
     * */
    public static void encryptAndUploadResult(String taskId) {
        // save zip file to the protected region /scone
        String iexecOutZipPath = ResultUtils.zipIexecOut(FileHelper.SLASH_IEXEC_OUT, SLASH_SCONE);
        if (iexecOutZipPath.isEmpty()) {
            log.error("zipIexecOut stage failed");
            exit();
        }
        String resultPath = eventuallyEncryptResult(iexecOutZipPath);
        String resultLink = uploadResult(taskId, resultPath); //TODO Put resultLink somewhere
    }

    private static String eventuallyEncryptResult(String inDataFilePath) {
        log.info("Encryption stage started");
        String fileToUpload;
        boolean shouldEncrypt = booleanFromYesNo(EnvUtils.getEnvVar(RESULT_ENCRYPTION));

        if (!shouldEncrypt) {
            log.info("Encryption stage mode: NO_ENCRYPTION");
            fileToUpload = inDataFilePath;
        } else {
            log.info("Encryption stage mode: ENCRYPTION_REQUESTED");
            String beneficiaryRsaPublicKeyBase64 = EnvUtils.getEnvVarOrExit(RESULT_ENCRYPTION_PUBLIC_KEY);
            String plainTextBeneficiaryRsaPublicKey = new String(Base64.getDecoder().decode(beneficiaryRsaPublicKeyBase64));
            fileToUpload = EncryptionService.encryptData(inDataFilePath, plainTextBeneficiaryRsaPublicKey, true);
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
            case DROPBOX_RESULT_STORAGE_PROVIDER:
                log.info("Upload stage mode: DROPBOX_STORAGE");
                String remoteFilename = taskId + ".zip";
                resultLink = UploaderService.uploadToDropBox(fileToUploadPath, storageToken, remoteFilename);
                break;
            case IPFS_RESULT_STORAGE_PROVIDER:
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
}
