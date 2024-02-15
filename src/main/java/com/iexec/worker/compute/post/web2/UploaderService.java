/*
 * Copyright 2020-2024 IEXEC BLOCKCHAIN TECH
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

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.iexec.common.result.ComputedFile;
import com.iexec.common.result.ResultModel;
import com.iexec.worker.compute.post.PostComputeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.iexec.common.replicate.ReplicateStatusCause.*;

@Slf4j
public class UploaderService {

    private final DropBoxService dropBoxService;

    public UploaderService() {
        this.dropBoxService = new DropBoxService();
    }

    public UploaderService(DropBoxService dropBoxService) {
        this.dropBoxService = dropBoxService;
    }

    //region Dropbox
    public String uploadToDropBox(String localFilePath, String dropboxAccessToken, String remoteFilename) throws PostComputeException {
        if (localFilePath == null || !new File(localFilePath).exists()) {
            throw new PostComputeException(POST_COMPUTE_RESULT_FILE_NOT_FOUND, "Can't uploadToDropBox (localFile issue) (exiting)");
        }

        DbxRequestConfig config = DbxRequestConfig.newBuilder("").build();
        DbxClientV2 client = createDropboxClient(dropboxAccessToken, config);

        try {
            String accountId = client.users().getCurrentAccount().getAccountId();
            boolean isConnected = accountId != null && !accountId.isEmpty();
            log.info("Uploading file with token [localFile:{}, remoteFile:{}, isConnected:{}]",
                    localFilePath, remoteFilename, isConnected);
        } catch (DbxException e) {
            throw new PostComputeException(POST_COMPUTE_DROPBOX_UPLOAD_FAILED, "Can't upload to Dropbox with provided token (exiting)");
        }

        return dropBoxService.uploadFile(client, new File(localFilePath), "/results/" + remoteFilename);
    }

    DbxClientV2 createDropboxClient(String dropboxAccessToken, DbxRequestConfig config) {
        return new DbxClientV2(config, dropboxAccessToken);
    }
    //endregion

    //region IPFS
    public String uploadToIpfsWithIexecProxy(ComputedFile computedFile, String baseUrl, String token, String fileToUploadPath) throws PostComputeException {
        final String taskId = computedFile.getTaskId();
        byte[] fileToUpload;

        try {
            fileToUpload = Files.readAllBytes(Paths.get(fileToUploadPath));
        } catch (IOException e) {
            throw new PostComputeException(
                    POST_COMPUTE_RESULT_FILE_NOT_FOUND,
                    String.format("Can't uploadToIpfsWithIexecProxy (missing filePath to upload) [taskId:%s, fileToUploadPath:%s]", taskId, fileToUploadPath));
        }

        ResultModel resultModel = ResultModel.builder()
                .chainTaskId(taskId)
                .zip(fileToUpload)
                .build();


        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<ResultModel> request = new HttpEntity<>(resultModel, headers);

        HttpStatus statusCode = null;
        try {
            ResponseEntity<String> response = createRestTemplate().postForEntity(baseUrl, request, String.class);
            statusCode = response.getStatusCode();

            if (statusCode.is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            log.error("Can't uploadToIpfsWithIexecProxy (result proxy issue)[taskId:{}]", taskId, e);
        }

        throw new PostComputeException(
                POST_COMPUTE_IPFS_UPLOAD_FAILED,
                String.format("Can't uploadToIpfsWithIexecProxy (result proxy issue)[taskId:%s, status:%s]", taskId, statusCode)
        );
    }

    RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
    //endregion

}