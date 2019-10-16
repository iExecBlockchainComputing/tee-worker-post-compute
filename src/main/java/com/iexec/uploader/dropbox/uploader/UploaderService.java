package com.iexec.uploader.dropbox.uploader;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

import java.io.File;


public class UploaderService {

    public static boolean uploadToDropBox(String localFilePath, String dropboxAccessToken, String remoteFilename) {
        //TODO check new File(localFilePath) not null
        DbxRequestConfig config = DbxRequestConfig.newBuilder("").build();
        DbxClientV2 client = new DbxClientV2(config, dropboxAccessToken);

        FullAccount account;
        try {
            account = client.users().getCurrentAccount();
            String loginMessage = String.format("Uploading file with token [localFile:%s, remoteFile:%s, tokenOwner:%s(%s)]",
                    localFilePath, remoteFilename, account.getEmail(), account.getName().getDisplayName());
            System.out.println(loginMessage);
        } catch (DbxException e) {
            System.err.println("Can't log to Dropbox with provided token (exiting)");
            System.exit(1);
        }

        return DropBoxService.uploadFile(client, new File(localFilePath), "/results/" + remoteFilename);
    }


}