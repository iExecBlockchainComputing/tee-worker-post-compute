package com.iexec.worker.tee.post.compute.web2;

import com.dropbox.core.DbxException;
import com.dropbox.core.util.IOUtil.ProgressListener;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/*
 *
 * Code extracted from official github.com/dropbox/dropbox-sdk-java samples:
 * https://github.com/dropbox/dropbox-sdk-java/blob/master/examples/upload-file/src/main/java/com/dropbox/core/examples/upload_file/Main.java#L48
 *
 * */
@Slf4j
public class DropBoxService {

    /**
     * Uploads a file in a single request. This approach is preferred for small files since it
     * eliminates unnecessary round-trips to the servers.
     * <p>
     * Please go back to the original sample for supporting big file uploads
     */
    public String uploadFile(DbxClientV2 dbxClient, File localFile, String dropboxPath) {
        try (InputStream in = new FileInputStream(localFile)) {
            ProgressListener progressListener = l -> printProgress(l, localFile.length());

            FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.ADD)
                    .withClientModified(new Date(localFile.lastModified()))
                    .uploadAndFinish(in, progressListener);

            log.info(metadata.toStringMultiline());
            return metadata.getPathDisplay();
        } catch (DbxException | IOException ex) {
            log.error("Error uploading to Dropbox [error:{}]", ex.getMessage());
        }
        return "";
    }


    private void printProgress(long uploaded, long size) {
        log.info("Uploaded {} /{} bytes ({}%)\n", uploaded, size, 100 * (uploaded / (double) size));
    }


}