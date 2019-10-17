package com.iexec.uploader.dropbox.uploader;

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
    public static boolean uploadFile(DbxClientV2 dbxClient, File localFile, String dropboxPath) {
        try (InputStream in = new FileInputStream(localFile)) {
            ProgressListener progressListener = l -> printProgress(l, localFile.length());

            FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.ADD)
                    .withClientModified(new Date(localFile.lastModified()))
                    .uploadAndFinish(in, progressListener);

            log.info(metadata.toStringMultiline());
            return true;
        } catch (DbxException ex) {
            System.err.println("Error uploading to Dropbox " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("Error reading from file \"" + localFile + "\": " + ex.getMessage());
        }
        return false;
    }


    private static void printProgress(long uploaded, long size) {
        System.out.printf("Uploaded %12d / %12d bytes (%5.2f%%)\n", uploaded, size, 100 * (uploaded / (double) size));
    }


}