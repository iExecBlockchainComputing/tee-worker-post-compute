package com.iexec.worker.compute.post.web2;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DropBoxServiceTests {
    private final static String DROPBOX_PATH = "DROPBOX_PATH";

    @TempDir
    File tmpFolder;

    DropBoxService dropBoxService = new DropBoxService();

    @Test
    void shouldUploadFile() throws IOException, DbxException {
        final File fileToUpload = Files.writeString(Path.of(tmpFolder.getAbsolutePath(), "fileToUpload.zip"), "Some dummy content").toFile();
        final String pathDisplay = "https://foo.bar/fileToUpload.zip";

        final DbxClientV2 client = mock(DbxClientV2.class);
        final DbxUserFilesRequests files = mock(DbxUserFilesRequests.class);
        final UploadBuilder uploadBuilder = mock(UploadBuilder.class);
        final FileMetadata fileMetadata = mock(FileMetadata.class);

        when(client.files()).thenReturn(files);
        when(files.uploadBuilder(DROPBOX_PATH)).thenReturn(uploadBuilder);
        when(uploadBuilder.withMode(WriteMode.ADD)).thenReturn(uploadBuilder);
        when(uploadBuilder.withClientModified(any())).thenReturn(uploadBuilder);
        when(uploadBuilder.uploadAndFinish(any(), any())).thenReturn(fileMetadata);
        when(fileMetadata.getPathDisplay()).thenReturn(pathDisplay);

        final String actualPathDisplay = assertDoesNotThrow(() -> dropBoxService.uploadFile(client, fileToUpload, DROPBOX_PATH));
        assertEquals(fileMetadata.getPathDisplay(), actualPathDisplay);
    }

    @Test
    void shouldNotUploadFileSinceUploadFailedDbxException() throws IOException, DbxException {
        final File fileToUpload = Files.writeString(Path.of(tmpFolder.getAbsolutePath(), "fileToUpload.zip"), "Some dummy content").toFile();

        final DbxClientV2 client = mock(DbxClientV2.class);
        final DbxUserFilesRequests files = mock(DbxUserFilesRequests.class);
        final UploadBuilder uploadBuilder = mock(UploadBuilder.class);

        when(client.files()).thenReturn(files);
        when(files.uploadBuilder(DROPBOX_PATH)).thenReturn(uploadBuilder);
        when(uploadBuilder.withMode(WriteMode.ADD)).thenReturn(uploadBuilder);
        when(uploadBuilder.withClientModified(any())).thenReturn(uploadBuilder);
        when(uploadBuilder.uploadAndFinish(any(), any())).thenThrow(DbxException.class);

        final String actualPathDisplay = assertDoesNotThrow(() -> dropBoxService.uploadFile(client, fileToUpload, DROPBOX_PATH));
        assertEquals("", actualPathDisplay);
    }

    @Test
    void shouldNotUploadFileSinceUploadFailedIOException() throws IOException, DbxException {
        final File fileToUpload = new File("/this/file/does/not/exist");

        final DbxClientV2 client = mock(DbxClientV2.class);
        final DbxUserFilesRequests files = mock(DbxUserFilesRequests.class);
        final UploadBuilder uploadBuilder = mock(UploadBuilder.class);

        when(client.files()).thenReturn(files);
        when(files.uploadBuilder(DROPBOX_PATH)).thenReturn(uploadBuilder);
        when(uploadBuilder.withMode(WriteMode.ADD)).thenReturn(uploadBuilder);
        when(uploadBuilder.withClientModified(any())).thenReturn(uploadBuilder);
        when(uploadBuilder.uploadAndFinish(any(), any())).thenThrow(IOException.class);

        final String actualPathDisplay = assertDoesNotThrow(() -> dropBoxService.uploadFile(client, fileToUpload, DROPBOX_PATH));
        assertEquals("", actualPathDisplay);
    }
}