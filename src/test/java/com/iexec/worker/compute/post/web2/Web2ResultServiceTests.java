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
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.worker.compute.post.PostComputeException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.io.IOException;

import static com.iexec.common.replicate.ReplicateStatusCause.*;
import static com.iexec.common.worker.result.ResultUtils.*;
import static com.iexec.commons.poco.chain.DealParams.DROPBOX_RESULT_STORAGE_PROVIDER;
import static com.iexec.commons.poco.chain.DealParams.IPFS_RESULT_STORAGE_PROVIDER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class Web2ResultServiceTests {
    private static final String TASK_ID = "0x0";
    private static final ComputedFile COMPUTED_FILE = ComputedFile.builder().taskId(TASK_ID).build();

    private static final String RESULT_ENCRYPTION_PUBLIC_KEY_STRING = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQ0lqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FnOEFNSUlDQ2dLQ0FnRUF2clVtUnVMV3UvMm83ci8xSW9ocQp6RkJTUE93T0xYVlJoZjhBUThDcmZnZWRacE1Ld3huWUk4UGJad09oWEpIMzZLZk1UcnhRVjR3aFhlalZqNjdDCjFaMkFMZjBPcC84dXlKY3JuTlhUYXhhVmY0c1Y0RXB0eTBocTNLSGtuU0J0cTBSOENTV1IxeFI4RGNpR1hJaGgKTkllVkZaazZOS291czZ2Tkt6cWZCbDJWMVorRzJ5eEhCLzNiVE0yWjUyMXgxOUZpWUlkUk91TVlwRFRnVXllagpZTll4Vk5CZlVSWmFHcGhPS1FqYThYWkVuSVR1b0toWVpZclc1NVhuVWM5NHQ4TDgrbzgzVmY0OU9oc1JKQStlCk9IOEFSZGhkN3V0c1lwOVBzcko0bFE3d3N5cFhzNWNpQ0Q3T1c4Y3MvbFFEYk9HRHlPZVlMb0pOeUpWQ1lIUWsKSVR4QTluaWE0aU9iNjdaRUN1UkpCVk01aFYreFBzUkRFdlJERnZKRXA0ZXMwbjhJRDcvOW4reEZFNlZJSFpybgpnUUUrYXA0Vm13Qk8xa3d4K2RhZGNvSlNIdUhyU2FXUGpFRUZ0R0RNNmROTzIxTWdNMlZzeDNxSFdpd2NkbFVzCjI3Ym9HMGhyTlp4d2g2UjdHWmJSNDEwcWN1aXQ5TUw1R1ZSQ0QwaFNpd2lFNDJyb09aRkV1ck9KY2x0K3lGVy8KQW9wV3FtYkkvYmxjZ3VEdk5pT21LRTdCNFkycU9sSC9ma0hZbXN1aDAwOFVRT1ZUcXpYbUFtaTlqNzNiejlmeQpuN1RvS3FabUErYTdkS0pYUTdlNXM2b0VHeDc3Wlc0MzZ4SjF4MTg2MkJVVVgxNGdLOWoyTzVzU0RsTzBadTA5CkdiRUFIZlFUb3EyOTBIUENFeTBydWMwQ0F3RUFBUT09Ci0tLS0tRU5EIFBVQkxJQyBLRVktLS0tLQ";

    @Mock
    UploaderService uploaderService;

    @TempDir
    File tmpFolder;

    Web2ResultService web2ResultService;

    @BeforeEach
    void openMocks() {
        this.web2ResultService = spy(new Web2ResultService(uploaderService, tmpFolder.getAbsolutePath()));
    }

    //region encryptAndUploadResult
    @Test
    void shouldEncryptAndUploadResult(final EnvironmentVariables environment) throws PostComputeException {
        environment.set(
                RESULT_STORAGE_TOKEN, "token"
        );

        final String zipPath = "zipPath";
        final String resultPath = "resultPath";

        doReturn(resultPath).when(web2ResultService).eventuallyEncryptResult(zipPath);  // Using `doReturn().when()` to avoid nasty side effects
        when(web2ResultService.uploadResult(COMPUTED_FILE, resultPath)).thenReturn("");

        try (final MockedStatic<ResultUtils> resultUtils = Mockito.mockStatic(ResultUtils.class)) {
            resultUtils.when(() -> ResultUtils.zipIexecOut(any(), any()))
                    .thenReturn(zipPath);

            assertDoesNotThrow(() -> web2ResultService.encryptAndUploadResult(COMPUTED_FILE));
        }
    }

    @Test
    void shouldNotEncryptAndUploadResultSinceCantZip() {
        final String zipPath = "";

        try (final MockedStatic<ResultUtils> resultUtils = Mockito.mockStatic(ResultUtils.class)) {
            resultUtils.when(() -> ResultUtils.zipIexecOut(any(), any()))
                    .thenReturn(zipPath);

            final PostComputeException exception = assertThrows(PostComputeException.class, () -> web2ResultService.encryptAndUploadResult(COMPUTED_FILE));
            assertEquals(POST_COMPUTE_OUT_FOLDER_ZIP_FAILED, exception.getStatusCause());
            assertEquals("zipIexecOut stage failed", exception.getMessage());
        }
    }
    //endregion

    // region checkResultFilesName
    @Test
    void shouldPassResultFilesNameCheckWhenNoFile() {
        assertDoesNotThrow(() -> web2ResultService.checkResultFilesName(TASK_ID, tmpFolder.getAbsolutePath()));
    }

    @Test
    void shouldPassResultFilesNameCheckWhenCorrectFiles() throws IOException {
        assertTrue(new File(tmpFolder, "result.txt").createNewFile());
        assertTrue(new File(tmpFolder, "computed.json").createNewFile());

        assertDoesNotThrow(() -> web2ResultService.checkResultFilesName(TASK_ID, tmpFolder.getAbsolutePath()));
    }

    @Test
    void shouldFailResultFilesNameCheckWhenWrongFolder() {
        final PostComputeException postComputeException = assertThrows(PostComputeException.class,
                () -> web2ResultService.checkResultFilesName(TASK_ID, "/dummy/folder/that/doesnt/exist"));
        assertEquals(POST_COMPUTE_FAILED_UNKNOWN_ISSUE, postComputeException.getStatusCause());
    }

    @Test
    void shouldFailResultFilesNameCheckWhenFileNameTooLong() throws IOException {
        assertTrue(new File(tmpFolder, "result-0x0000000000000000000.txt").createNewFile());
        assertTrue(new File(tmpFolder, "computed.json").createNewFile());

        final PostComputeException postComputeException = assertThrows(PostComputeException.class,
                () -> web2ResultService.checkResultFilesName(TASK_ID, tmpFolder.getAbsolutePath()));
        assertEquals(POST_COMPUTE_TOO_LONG_RESULT_FILE_NAME, postComputeException.getStatusCause());
    }
    // endregion

    //region eventuallyEncryptResult
    @Test
    void shouldEventuallyEncryptResult(final EnvironmentVariables environment) {
        environment.set(
                RESULT_ENCRYPTION, "yes",
                RESULT_ENCRYPTION_PUBLIC_KEY, RESULT_ENCRYPTION_PUBLIC_KEY_STRING
        );

        final String inDataFilePath = "inDataFile.zip";
        final String fileToUpload = "fileToUpload.zip";

        try (final MockedStatic<EncryptionHelper> encryptionHelper = Mockito.mockStatic(EncryptionHelper.class)) {
            encryptionHelper.when(() -> EncryptionHelper.encryptData(eq(inDataFilePath), any(), eq(true)))
                    .thenReturn(fileToUpload);
            final String actualFileToUpload = assertDoesNotThrow(() -> web2ResultService.eventuallyEncryptResult(inDataFilePath));
            assertEquals(fileToUpload, actualFileToUpload);
        }

    }

    @Test
    void shouldNotEventuallyEncryptResultSinceNoEncryptionNeeded(final EnvironmentVariables environment) {
        environment.set(
                RESULT_ENCRYPTION, "no"
        );

        final String inDataFilePath = "inDataFile.zip";

        final String actualFileToUpload = assertDoesNotThrow(() -> web2ResultService.eventuallyEncryptResult(inDataFilePath));
        assertEquals(inDataFilePath, actualFileToUpload);
    }

    @Test
    void shouldNotEventuallyEncryptResultSinceNoPublicKey(final EnvironmentVariables environment) {
        environment.set(
                RESULT_ENCRYPTION, "yes"
        );

        final String inDataFilePath = "inDataFile.zip";

        final PostComputeException exception = assertThrows(PostComputeException.class, () -> web2ResultService.eventuallyEncryptResult(inDataFilePath));
        assertEquals(POST_COMPUTE_ENCRYPTION_PUBLIC_KEY_MISSING, exception.getStatusCause());
    }

    @Test
    void shouldNotEventuallyEncryptResultSinceMalformedPublicKey(final EnvironmentVariables environment) {
        environment.set(
                RESULT_ENCRYPTION, "yes",
                RESULT_ENCRYPTION_PUBLIC_KEY, "?"
        );

        final String inDataFilePath = "inDataFile.zip";
        final PostComputeException exception = assertThrows(PostComputeException.class, () -> web2ResultService.eventuallyEncryptResult(inDataFilePath));
        assertEquals(POST_COMPUTE_MALFORMED_ENCRYPTION_PUBLIC_KEY, exception.getStatusCause());
    }

    @Test
    void shouldNotEventuallyEncryptResultSinceEncryptionFails(final EnvironmentVariables environment) {
        environment.set(
                RESULT_ENCRYPTION, "yes",
                RESULT_ENCRYPTION_PUBLIC_KEY, RESULT_ENCRYPTION_PUBLIC_KEY_STRING
        );

        final String inDataFilePath = "inDataFile.zip";

        try (final MockedStatic<EncryptionHelper> encryptionHelper = Mockito.mockStatic(EncryptionHelper.class)) {
            encryptionHelper.when(() -> EncryptionHelper.encryptData(eq(inDataFilePath), any(), eq(true)))
                    .thenThrow(new RuntimeException("Encryption failed"));

            final PostComputeException exception = assertThrows(PostComputeException.class,
                    () -> web2ResultService.eventuallyEncryptResult(inDataFilePath));
            assertEquals(POST_COMPUTE_ENCRYPTION_FAILED, exception.getStatusCause());
            assertEquals("Result encryption failed", exception.getMessage());
        }
    }

    @Test
    void shouldNotEventuallyEncryptResultSinceEmptyFileToUpload(final EnvironmentVariables environment) {
        environment.set(
                RESULT_ENCRYPTION, "yes",
                RESULT_ENCRYPTION_PUBLIC_KEY, RESULT_ENCRYPTION_PUBLIC_KEY_STRING
        );

        final String inDataFilePath = "inDataFile.zip";
        final String fileToUpload = "";

        try (final MockedStatic<EncryptionHelper> encryptionHelper = Mockito.mockStatic(EncryptionHelper.class)) {
            encryptionHelper.when(() -> EncryptionHelper.encryptData(eq(inDataFilePath), any(), eq(true)))
                    .thenReturn(fileToUpload);
            final PostComputeException exception = assertThrows(PostComputeException.class, () -> web2ResultService.eventuallyEncryptResult(inDataFilePath));
            assertEquals(POST_COMPUTE_ENCRYPTION_FAILED, exception.getStatusCause());
            assertEquals("Encryption stage failed", exception.getMessage());
        }

    }
    //endregion

    //region uploadResult
    @Test
    void shouldUploadResultOnDropboxWithoutStorageProxy(final EnvironmentVariables environment) throws PostComputeException {
        final String storageToken = "storageToken";
        final String fileToUploadPath = "fileToUpload.zip";
        final String remoteFileName = TASK_ID + ".zip";
        final String resultLink = "https://foo.bar/resultLink";

        environment.set(
                RESULT_STORAGE_PROVIDER, DROPBOX_RESULT_STORAGE_PROVIDER,
                RESULT_STORAGE_TOKEN, storageToken
        );

        when(uploaderService.uploadToDropBox(fileToUploadPath, storageToken, remoteFileName)).thenReturn(resultLink);

        final String actualResultLink = assertDoesNotThrow(() -> web2ResultService.uploadResult(COMPUTED_FILE, fileToUploadPath));
        assertEquals(resultLink, actualResultLink);
    }

    /*
    This should work exactly as `shouldUploadResultOnDropboxWithoutStorageProxy`.
     */
    @Test
    void shouldUploadResultOnDropboxWithStorageProxy(final EnvironmentVariables environment) throws PostComputeException {
        final String storageToken = "storageToken";
        final String fileToUploadPath = "fileToUpload.zip";
        final String remoteFileName = TASK_ID + ".zip";
        final String resultLink = "https://foo.bar/resultLink";

        environment.set(
                RESULT_STORAGE_PROVIDER, DROPBOX_RESULT_STORAGE_PROVIDER,
                RESULT_STORAGE_TOKEN, storageToken,
                RESULT_STORAGE_PROXY, "storageProxy"
        );

        when(uploaderService.uploadToDropBox(fileToUploadPath, storageToken, remoteFileName)).thenReturn(resultLink);

        final String actualResultLink = assertDoesNotThrow(() -> web2ResultService.uploadResult(COMPUTED_FILE, fileToUploadPath));
        assertEquals(resultLink, actualResultLink);
    }

    @Test
    void shouldUploadResultOnIpfsWithoutStorageProxy(@NotNull final EnvironmentVariables environment) throws PostComputeException {
        final String storageToken = "storageToken";
        final String storageProxy = "";
        final String fileToUploadPath = "fileToUpload.zip";
        final String resultLink = "https://foo.bar/resultLink";

        environment.set(
                RESULT_STORAGE_PROVIDER, IPFS_RESULT_STORAGE_PROVIDER,
                RESULT_STORAGE_TOKEN, storageToken
        );

        when(uploaderService.uploadToIpfsWithIexecProxy(COMPUTED_FILE, storageProxy, storageToken, fileToUploadPath)).thenReturn(resultLink);

        final String actualResultLink = assertDoesNotThrow(() -> web2ResultService.uploadResult(COMPUTED_FILE, fileToUploadPath));
        assertEquals(resultLink, actualResultLink);
    }

    @Test
    void shouldUploadResultOnIpfsWithStorageProxy(@NotNull final EnvironmentVariables environment) throws PostComputeException {
        final String storageToken = "storageToken";
        final String storageProxy = "storageProxy";
        final String fileToUploadPath = "fileToUpload.zip";
        final String resultLink = "https://foo.bar/resultLink";

        environment.set(
                RESULT_STORAGE_PROVIDER, IPFS_RESULT_STORAGE_PROVIDER,
                RESULT_STORAGE_TOKEN, storageToken,
                RESULT_STORAGE_PROXY, storageProxy
        );

        when(uploaderService.uploadToIpfsWithIexecProxy(COMPUTED_FILE, storageProxy, storageToken, fileToUploadPath)).thenReturn(resultLink);

        final String actualResultLink = assertDoesNotThrow(() -> web2ResultService.uploadResult(COMPUTED_FILE, fileToUploadPath));
        assertEquals(resultLink, actualResultLink);
    }

    /*
    This should use IPFS; so it should be strictly equivalent to
    `shouldUploadResultOnIpfsWithoutStorageProxy`.
     */
    @Test
    void shouldUploadResultOnDefaultWithoutStorageProxy(@NotNull final EnvironmentVariables environment) throws PostComputeException {
        final String storageToken = "storageToken";
        final String storageProxy = "";
        final String fileToUploadPath = "fileToUpload.zip";
        final String resultLink = "https://foo.bar/resultLink";

        environment.set(
                RESULT_STORAGE_TOKEN, storageToken
        );

        when(uploaderService.uploadToIpfsWithIexecProxy(COMPUTED_FILE, storageProxy, storageToken, fileToUploadPath)).thenReturn(resultLink);

        final String actualResultLink = assertDoesNotThrow(() -> web2ResultService.uploadResult(COMPUTED_FILE, fileToUploadPath));
        assertEquals(resultLink, actualResultLink);
    }

    /*
    This should use IPFS; so it should be strictly equivalent to
    `shouldUploadResultOnIpfsWithStorageProxy`.
     */
    @Test
    void shouldUploadResultOnDefaultsWithStorageProxy(@NotNull final EnvironmentVariables environment) throws PostComputeException {
        final String storageToken = "storageToken";
        final String storageProxy = "storageProxy";
        final String fileToUploadPath = "fileToUpload.zip";
        final String resultLink = "https://foo.bar/resultLink";

        environment.set(
                RESULT_STORAGE_TOKEN, storageToken,
                RESULT_STORAGE_PROXY, storageProxy
        );

        when(uploaderService.uploadToIpfsWithIexecProxy(COMPUTED_FILE, storageProxy, storageToken, fileToUploadPath)).thenReturn(resultLink);

        final String actualResultLink = assertDoesNotThrow(() -> web2ResultService.uploadResult(COMPUTED_FILE, fileToUploadPath));
        assertEquals(resultLink, actualResultLink);
    }

    @Test
    void shouldNotUploadResultSinceNoStorageToken() {
        final String fileToUploadPath = "fileToUpload.zip";

        final PostComputeException exception = assertThrows(PostComputeException.class, () -> web2ResultService.uploadResult(COMPUTED_FILE, fileToUploadPath));
        assertEquals(POST_COMPUTE_STORAGE_TOKEN_MISSING, exception.getStatusCause());
    }
    //endregion
}
