package com.iexec.worker.tee.post.compute.web2;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.iexec.common.result.ResultModel;
import com.iexec.worker.tee.post.compute.PostComputeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.iexec.common.replicate.ReplicateStatusCause.*;

@Slf4j
public class UploaderService {

    public static String uploadToDropBox(String localFilePath, String dropboxAccessToken, String remoteFilename) throws PostComputeException {
        if (localFilePath == null || !new File(localFilePath).exists()){
            throw new PostComputeException(POST_COMPUTE_RESULT_FILE_NOT_FOUND, "Can't uploadToDropBox (localFile issue) (exiting)");
        }

        DbxRequestConfig config = DbxRequestConfig.newBuilder("").build();
        DbxClientV2 client = new DbxClientV2(config, dropboxAccessToken);

        try {
            String accountId = client.users().getCurrentAccount().getAccountId();
            boolean isConnected = accountId != null && !accountId.isEmpty();
            log.info("Uploading file with token [localFile:{}, remoteFile:{}, isConnected:{}]",
                    localFilePath, remoteFilename, isConnected);
        } catch (DbxException e) {
            throw new PostComputeException(POST_COMPUTE_DROPBOX_UPLOAD_FAILED, "Can't upload to Dropbox with provided token (exiting)");
        }

        return DropBoxService.uploadFile(client, new File(localFilePath), "/results/" + remoteFilename);
    }


    public static String uploadToIpfsWithIexecProxy(String taskId, String baseUrl, String token, String fileToUploadPath) throws PostComputeException {
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
            ResponseEntity<String> response = new RestTemplate().postForEntity(baseUrl, request, String.class);
            statusCode = response.getStatusCode();

            if (statusCode.is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (HttpClientErrorException e) {
            log.error("Can't uploadToIpfsWithIexecProxy (result proxy issue)[taskId:{}, status:{}]", taskId, e.getStatusCode(), e);
        }

        throw new PostComputeException(
                POST_COMPUTE_IPFS_UPLOAD_FAILED,
                String.format("Can't uploadToIpfsWithIexecProxy (result proxy issue)[taskId:%s, status:%s]", taskId, statusCode)
        );
    }


}