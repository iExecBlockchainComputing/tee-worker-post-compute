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

package com.iexec.worker.compute.post.web2;

import com.iexec.common.result.ComputedFile;
import com.iexec.common.security.EncryptionHelper;
import com.iexec.common.utils.IexecFileHelper;
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.worker.compute.post.PostComputeException;
import com.iexec.worker.compute.post.utils.EnvUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.iexec.common.replicate.ReplicateStatusCause.*;
import static com.iexec.common.worker.result.ResultUtils.*;
import static com.iexec.commons.poco.chain.DealParams.DROPBOX_RESULT_STORAGE_PROVIDER;
import static com.iexec.commons.poco.chain.DealParams.IPFS_RESULT_STORAGE_PROVIDER;
import static com.iexec.commons.poco.tee.TeeUtils.booleanFromYesNo;

@Slf4j
public class Web2ResultService {

    public static final String SLASH_POST_COMPUTE_TMP = File.separator + "post-compute-tmp";
    private static final int RESULT_FILE_NAME_MAX_LENGTH = 31;

    private final UploaderService uploaderService;
    private final String iexecOut;

    public Web2ResultService() {
        this.uploaderService = new UploaderService();
        this.iexecOut = IexecFileHelper.SLASH_IEXEC_OUT;
    }

    Web2ResultService(UploaderService uploaderService,
                      String iexecOut) {
        this.uploaderService = uploaderService;
        this.iexecOut = iexecOut;
    }

    /*
     * Manager
     * */
    public void encryptAndUploadResult(ComputedFile computedFile) throws PostComputeException, GeneralSecurityException, IOException {
        // check result file names are not too long
        checkResultFilesName(computedFile.getTaskId(), iexecOut);

        // save zip file to the protected region /post-compute-tmp (temporarily)
        String iexecOutZipPath = ResultUtils.zipIexecOut(iexecOut, SLASH_POST_COMPUTE_TMP);
        if (iexecOutZipPath.isEmpty()) {
            throw new PostComputeException(POST_COMPUTE_OUT_FOLDER_ZIP_FAILED, "zipIexecOut stage failed");
        }
        String resultPath = eventuallyEncryptResult(iexecOutZipPath);
        uploadResult(computedFile, resultPath); //TODO Share result link to beneficiary
    }

    void checkResultFilesName(String taskId, String iexecOutPath) throws PostComputeException {
        final AtomicBoolean failed = new AtomicBoolean(false);
        try {
            Files.walkFileTree(Paths.get(iexecOutPath), new SimpleFileVisitor<>() {
                @NotNull
                @Override
                public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) {
                    final String fileName = file.getFileName().toString();
                    if (fileName.length() > RESULT_FILE_NAME_MAX_LENGTH) {
                        log.error("Too long result file name [chainTaskId:{}, file:{}]", taskId, file);
                        failed.set(true);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            final String errorMessage = String.format("Can't check result files [chainTaskId:%s]", taskId);
            log.error(errorMessage, e);
            throw new PostComputeException(POST_COMPUTE_FAILED_UNKNOWN_ISSUE, errorMessage, e);
        }

        if (failed.get()) {
            final String errorMessage = String.format("Too long result file name [chainTaskId:%s]", taskId);
            throw new PostComputeException(POST_COMPUTE_TOO_LONG_RESULT_FILE_NAME,
                    errorMessage);
        }
    }

    String eventuallyEncryptResult(String inDataFilePath) throws PostComputeException, GeneralSecurityException, IOException {
        log.info("Encryption stage started");
        boolean shouldEncrypt = booleanFromYesNo(EnvUtils.getEnvVar(RESULT_ENCRYPTION));

        if (!shouldEncrypt) {
            log.info("Encryption stage mode: NO_ENCRYPTION");
            return inDataFilePath;
        }

        log.info("Encryption stage mode: ENCRYPTION_REQUESTED");
        final String beneficiaryRsaPublicKeyBase64 = EnvUtils.getEnvVarOrThrow(RESULT_ENCRYPTION_PUBLIC_KEY, POST_COMPUTE_ENCRYPTION_PUBLIC_KEY_MISSING);
        final String plainTextBeneficiaryRsaPublicKey;
        try {
            plainTextBeneficiaryRsaPublicKey = new String(Base64.getDecoder().decode(beneficiaryRsaPublicKeyBase64));
        } catch (Exception e) {
            final String errorMessage = "Result encryption public key base64 decoding failed";
            log.error(errorMessage, e);
            throw new PostComputeException(POST_COMPUTE_MALFORMED_ENCRYPTION_PUBLIC_KEY, errorMessage);
        }

        final String fileToUpload = EncryptionHelper.encryptData(inDataFilePath, plainTextBeneficiaryRsaPublicKey, true);
        if (fileToUpload.isEmpty()) {
            throw new PostComputeException(POST_COMPUTE_ENCRYPTION_FAILED, "Encryption stage failed");
        } else {
            log.info("Encryption stage completed");
        }
        return fileToUpload;
    }

    String uploadResult(ComputedFile computedFile, String fileToUploadPath) throws PostComputeException {
        log.info("Upload stage started");
        String storageProvider = EnvUtils.getEnvVar(RESULT_STORAGE_PROVIDER);
        String storageProxy = EnvUtils.getEnvVar(RESULT_STORAGE_PROXY);
        String storageToken = EnvUtils.getEnvVarOrThrow(RESULT_STORAGE_TOKEN, POST_COMPUTE_STORAGE_TOKEN_MISSING);

        String resultLink = "";
        switch (storageProvider) {
            case DROPBOX_RESULT_STORAGE_PROVIDER:
                log.info("Upload stage mode: DROPBOX_STORAGE");
                String remoteFilename = computedFile.getTaskId() + ".zip";
                resultLink = uploaderService.uploadToDropBox(fileToUploadPath, storageToken, remoteFilename);
                break;
            case IPFS_RESULT_STORAGE_PROVIDER:
            default:
                log.info("Upload stage mode: IPFS_STORAGE");
                resultLink = uploaderService.uploadToIpfsWithIexecProxy(computedFile, storageProxy, storageToken, fileToUploadPath);
                break;
        }

        log.info("Upload stage completed");
        return resultLink;
    }
}
