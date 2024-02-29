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
import com.dropbox.core.v2.users.DbxUserUsersRequests;
import com.dropbox.core.v2.users.FullAccount;
import com.iexec.common.result.ComputedFile;
import com.iexec.worker.compute.post.PostComputeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static com.iexec.common.replicate.ReplicateStatusCause.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UploaderServiceTests {

    private static final String TASK_ID = "0x0";
    private static final ComputedFile COMPUTED_FILE = ComputedFile.builder().taskId(TASK_ID).build();
    private static final String DROPBOX_TOKEN = "DROPBOX_TOKEN";
    private static final String IPFS_TOKEN = "IPFS_TOKEN";
    private static final String REMOTE_FILENAME = "remoteFileName.zip";
    private static final String REMOTE_FILEPATH = "https//foo.bar/remoteFileName.zip";

    @TempDir
    File tmpFolder;

    @Mock
    DropBoxService dropBoxService;

    @Spy
    @InjectMocks
    UploaderService uploaderService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    //region createDropboxClient
    @Test
    void shouldCreateDropboxClient() {
        assertNotNull(uploaderService.createDropboxClient(DROPBOX_TOKEN, DbxRequestConfig.newBuilder("").build()));
    }
    //endregion

    //region uploadToDropBox
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "accountId"})
    void shouldUploadToDropbox(String accountId) throws DbxException, IOException {
        final String fileToUpload = Files.createFile(Path.of(tmpFolder.getAbsolutePath(), "fileToUpload.zip")).toString();

        final DbxClientV2 client = mock(DbxClientV2.class);
        final DbxUserUsersRequests users = mock(DbxUserUsersRequests.class);
        final FullAccount account = mock(FullAccount.class);

        when(client.users()).thenReturn(users);
        when(users.getCurrentAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(accountId);

        doReturn(client).when(uploaderService).createDropboxClient(eq(DROPBOX_TOKEN), any());
        when(dropBoxService.uploadFile(eq(client), any(), eq("/results/" + REMOTE_FILENAME))).thenReturn(REMOTE_FILEPATH);

        final String actualFilePath = assertDoesNotThrow(() -> uploaderService.uploadToDropBox(fileToUpload, DROPBOX_TOKEN, REMOTE_FILENAME));
        assertEquals(REMOTE_FILEPATH, actualFilePath);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"/test/nothing/here/"})
    void shouldNotUploadToDropboxSinceFileDoesNotExist(String wrongPath) {
        final PostComputeException exception = assertThrows(PostComputeException.class, () -> uploaderService.uploadToDropBox(wrongPath, DROPBOX_TOKEN, REMOTE_FILENAME));
        assertEquals(POST_COMPUTE_RESULT_FILE_NOT_FOUND, exception.getStatusCause());
        assertEquals("Can't uploadToDropBox (localFile issue) (exiting)", exception.getMessage());
    }

    @Test
    void shouldNotUploadToDropboxSinceWrongAccount() throws DbxException, IOException {
        final String fileToUpload = Files.createFile(Path.of(tmpFolder.getAbsolutePath(), "fileToUpload.zip")).toString();

        final DbxClientV2 client = mock(DbxClientV2.class);
        final DbxUserUsersRequests users = mock(DbxUserUsersRequests.class);

        when(client.users()).thenReturn(users);
        when(users.getCurrentAccount()).thenThrow(DbxException.class);

        doReturn(client).when(uploaderService).createDropboxClient(eq(DROPBOX_TOKEN), any());

        final PostComputeException exception = assertThrows(PostComputeException.class, () -> uploaderService.uploadToDropBox(fileToUpload, DROPBOX_TOKEN, REMOTE_FILENAME));
        assertEquals(POST_COMPUTE_DROPBOX_UPLOAD_FAILED, exception.getStatusCause());
        assertEquals("Can't upload to Dropbox with provided token (exiting)", exception.getMessage());
    }
    //endregion

    //region createRestTemplate
    @Test
    void shouldCreateRestTemplate() {
        assertNotNull(uploaderService.createRestTemplate());
    }
    //endregion

    //region uploadToIpfsWithIexecProxy
    @Test
    void shouldUploadToIpfsWithIexecProxy() throws IOException {
        final String baseUrl = "baseUrl";
        final String fileToUpload = Files.createFile(Path.of(tmpFolder.getAbsolutePath(), "fileToUpload.zip")).toString();
        final String responseBody = "responseBody";

        final RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.postForEntity(eq(baseUrl), any(), eq(String.class))).thenReturn(ResponseEntity.of(Optional.of(responseBody)));

        when(uploaderService.createRestTemplate()).thenReturn(restTemplate);

        final String actualResponseBody = assertDoesNotThrow(() -> uploaderService.uploadToIpfsWithIexecProxy(COMPUTED_FILE, baseUrl, IPFS_TOKEN, fileToUpload));
        assertEquals(responseBody, actualResponseBody);
    }

    @Test
    void shouldNotUploadToIpfsWithIexecProxySinceCantReadFile() {
        final String baseUrl = "baseUrl";
        final String fileToUpload = "/this/file/does/not/exist";

        final PostComputeException exception = assertThrows(PostComputeException.class, () -> uploaderService.uploadToIpfsWithIexecProxy(COMPUTED_FILE, baseUrl, IPFS_TOKEN, fileToUpload));
        assertEquals(POST_COMPUTE_RESULT_FILE_NOT_FOUND, exception.getStatusCause());
        assertEquals(String.format("Can't uploadToIpfsWithIexecProxy (missing filePath to upload) [taskId:%s, fileToUploadPath:%s]", TASK_ID, fileToUpload), exception.getMessage());
    }

    @Test
    void shouldNotUploadToIpfsWithIexecProxySincePostFailed() throws IOException {
        final String baseUrl = "baseUrl";
        final String fileToUpload = Files.createFile(Path.of(tmpFolder.getAbsolutePath(), "fileToUpload.zip")).toString();

        final RestTemplate restTemplate = mock(RestTemplate.class);
        final ResponseEntity<String> response = ResponseEntity.notFound().build();
        when(restTemplate.postForEntity(eq(baseUrl), any(), eq(String.class))).thenReturn(response);

        when(uploaderService.createRestTemplate()).thenReturn(restTemplate);

        final PostComputeException exception = assertThrows(PostComputeException.class, () -> uploaderService.uploadToIpfsWithIexecProxy(COMPUTED_FILE, baseUrl, IPFS_TOKEN, fileToUpload));
        assertEquals(POST_COMPUTE_IPFS_UPLOAD_FAILED, exception.getStatusCause());
        assertEquals(String.format("Can't uploadToIpfsWithIexecProxy (result proxy issue)[taskId:%s, status:%s]", TASK_ID, response.getStatusCode()), exception.getMessage());
    }
    //endregion
}