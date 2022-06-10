package com.iexec.worker.tee.post.compute.web2;

import com.iexec.common.utils.IexecFileHelper;
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.worker.tee.post.compute.PostComputeException;
import com.iexec.worker.tee.post.compute.utils.EnvUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Base64;

import static com.iexec.common.chain.DealParams.DROPBOX_RESULT_STORAGE_PROVIDER;
import static com.iexec.common.chain.DealParams.IPFS_RESULT_STORAGE_PROVIDER;
import static com.iexec.common.replicate.ReplicateStatusCause.*;
import static com.iexec.common.tee.TeeUtils.booleanFromYesNo;
import static com.iexec.common.worker.result.ResultUtils.*;

@Slf4j
public class Web2ResultService {

    public static final String SLASH_POST_COMPUTE_TMP = File.separator + "post-compute-tmp";

    private final UploaderService uploaderService;
    private final EncryptionService encryptionService;

    public Web2ResultService() {
        this.uploaderService = new UploaderService();
        this.encryptionService = new EncryptionService();
    }

    public Web2ResultService(UploaderService uploaderService, EncryptionService encryptionService) {
        this.uploaderService = uploaderService;
        this.encryptionService = encryptionService;
    }

    /*
     * Manager
     * */
    public void encryptAndUploadResult(String taskId) throws PostComputeException {
        // save zip file to the protected region /post-compute-tmp (temporarily)
        String iexecOutZipPath = ResultUtils.zipIexecOut(IexecFileHelper.SLASH_IEXEC_OUT, SLASH_POST_COMPUTE_TMP);
        if (iexecOutZipPath.isEmpty()) {
            throw new PostComputeException(POST_COMPUTE_OUT_FOLDER_ZIP_FAILED, "zipIexecOut stage failed");
        }
        String resultPath = eventuallyEncryptResult(iexecOutZipPath);
        uploadResult(taskId, resultPath); //TODO Share result link to beneficiary
    }

    String eventuallyEncryptResult(String inDataFilePath) throws PostComputeException {
        log.info("Encryption stage started");
        String fileToUpload;
        boolean shouldEncrypt = booleanFromYesNo(EnvUtils.getEnvVar(RESULT_ENCRYPTION));

        if (!shouldEncrypt) {
            log.info("Encryption stage mode: NO_ENCRYPTION");
            fileToUpload = inDataFilePath;
        } else {
            log.info("Encryption stage mode: ENCRYPTION_REQUESTED");
            String beneficiaryRsaPublicKeyBase64 = EnvUtils.getEnvVarOrThrow(RESULT_ENCRYPTION_PUBLIC_KEY, POST_COMPUTE_ENCRYPTION_PUBLIC_KEY_MISSING);
            String plainTextBeneficiaryRsaPublicKey = new String(Base64.getDecoder().decode(beneficiaryRsaPublicKeyBase64));
            fileToUpload = encryptionService.encryptData(inDataFilePath, plainTextBeneficiaryRsaPublicKey, true);
        }

        if (fileToUpload.isEmpty()) {
            throw new PostComputeException(POST_COMPUTE_ENCRYPTION_FAILED, "Encryption stage failed");
        } else {
            log.info("Encryption stage completed");
        }
        return fileToUpload;
    }

    String uploadResult(String taskId, String fileToUploadPath) throws PostComputeException {
        log.info("Upload stage started");
        String storageProvider = EnvUtils.getEnvVar(RESULT_STORAGE_PROVIDER);
        String storageProxy = EnvUtils.getEnvVar(RESULT_STORAGE_PROXY);
        String storageToken = EnvUtils.getEnvVarOrThrow(RESULT_STORAGE_TOKEN, POST_COMPUTE_STORAGE_TOKEN_MISSING);

        String resultLink = "";
        switch (storageProvider) {
            case DROPBOX_RESULT_STORAGE_PROVIDER:
                log.info("Upload stage mode: DROPBOX_STORAGE");
                String remoteFilename = taskId + ".zip";
                resultLink = uploaderService.uploadToDropBox(fileToUploadPath, storageToken, remoteFilename);
                break;
            case IPFS_RESULT_STORAGE_PROVIDER:
            default:
                log.info("Upload stage mode: IPFS_STORAGE");
                resultLink = uploaderService.uploadToIpfsWithIexecProxy(taskId, storageProxy, storageToken, fileToUploadPath);
                break;
        }

        log.info("Upload stage completed");
        return resultLink;
    }
}
