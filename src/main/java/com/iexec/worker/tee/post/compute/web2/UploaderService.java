package com.iexec.worker.tee.post.compute.web2;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import com.iexec.common.result.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class UploaderService {

    public static String uploadToDropBox(String localFilePath, String dropboxAccessToken, String remoteFilename) {
        //TODO check new File(localFilePath) not null
        DbxRequestConfig config = DbxRequestConfig.newBuilder("").build();
        DbxClientV2 client = new DbxClientV2(config, dropboxAccessToken);

        FullAccount account;
        try {
            account = client.users().getCurrentAccount();
            String loginMessage = String.format("Uploading file with token [localFile:%s, remoteFile:%s, tokenOwner:%s(%s)]",
                    localFilePath, remoteFilename, account.getEmail(), account.getName().getDisplayName());
            log.info(loginMessage);
        } catch (DbxException e) {
            System.err.println("Can't log to Dropbox with provided token (exiting)");
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

        String response = new RestTemplate().postForObject(baseUrl, request, String.class);

        if (response != null && !response.isEmpty()) {
            return response;
        }

        return "";

    }


}