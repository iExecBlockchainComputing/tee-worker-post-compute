package com.iexec.worker.tee.post.compute.web2;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import com.iexec.common.result.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class UploaderService {

    public static String uploadToDropBox(String localFilePath, String dropboxAccessToken, String remoteFilename) {
        if (localFilePath == null || !new File(localFilePath).exists()){
            log.error("Can't uploadToDropBox (localFile issue) (exiting)");
            System.exit(1);
        }

        DbxRequestConfig config = DbxRequestConfig.newBuilder("").build();
        DbxClientV2 client = new DbxClientV2(config, dropboxAccessToken);

        try {
            String accountId = client.users().getCurrentAccount().getAccountId();
            boolean isConnected = accountId != null && !accountId.isEmpty();
            log.info("Uploading file with token [localFile:{}, remoteFile:{}, isConnected:{}]",
                    localFilePath, remoteFilename, isConnected);
        } catch (DbxException e) {
            log.error("Can't log to Dropbox with provided token (exiting)");
            System.exit(1);
        }

        return DropBoxService.uploadFile(client, new File(localFilePath), "/results/" + remoteFilename);
    }


    public static String uploadToIpfsWithIexecProxy(String taskId, String baseUrl, String token, String fileToUploadPath) {
        byte[] fileToUpload;

        try {
            fileToUpload = Files.readAllBytes(Paths.get(fileToUploadPath));
        } catch (IOException e) {
            log.error("Can't uploadToIpfsWithIexecProxy (missing filePath to upload) [taskId:{}, fileToUploadPath:{}]", taskId, fileToUploadPath);
            return "";
        }

        ResultModel resultModel = ResultModel.builder()
                .chainTaskId(taskId)
                .zip(fileToUpload)
                .build();


        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<ResultModel> request = new HttpEntity<>(resultModel, headers);

        ResponseEntity<String> response = new RestTemplate().postForEntity(baseUrl, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }

        log.error("Can't uploadToIpfsWithIexecProxy (result proxy issue)[taskId:{}, status:{}]",
                taskId, response.getStatusCode());
        return "";

    }


}