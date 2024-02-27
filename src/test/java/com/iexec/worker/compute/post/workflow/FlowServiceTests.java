/*
 * Copyright 2022-2024 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.compute.post.workflow;

import com.iexec.common.result.ComputedFile;
import com.iexec.common.utils.IexecFileHelper;
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.worker.api.WorkerApiClient;
import com.iexec.worker.api.WorkerApiManager;
import com.iexec.worker.compute.post.PostComputeException;
import com.iexec.worker.compute.post.signer.SignerService;
import feign.FeignException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static com.iexec.common.replicate.ReplicateStatusCause.*;
import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY;
import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_WORKER_ADDRESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SystemStubsExtension.class)
class FlowServiceTests {
    private static final String CHAIN_TASK_ID = "0x0";

    @Mock
    SignerService signerService;

    @Spy
    @InjectMocks
    FlowService flowService;

    @BeforeEach
    void openMocks() {
        MockitoAnnotations.openMocks(this);
    }

    // region readComputedFile
    @Test
    void shouldReadComputedFile() {
        try (MockedStatic<IexecFileHelper> iexecFileHelper = Mockito.mockStatic(IexecFileHelper.class)) {
            iexecFileHelper.when(() -> IexecFileHelper.readComputedFile(CHAIN_TASK_ID, IexecFileHelper.SLASH_IEXEC_OUT))
                    .thenReturn(ComputedFile.builder().build());

            Assertions.assertDoesNotThrow(() -> flowService.readComputedFile(CHAIN_TASK_ID));
        }
    }

    @Test
    void shouldNotReadComputedFile() {
        try (MockedStatic<IexecFileHelper> iexecFileHelper = Mockito.mockStatic(IexecFileHelper.class)) {
            iexecFileHelper.when(() -> IexecFileHelper.readComputedFile(CHAIN_TASK_ID, IexecFileHelper.SLASH_IEXEC_OUT))
                    .thenReturn(null);

            final PostComputeException exception = assertThrows(PostComputeException.class, () -> flowService.readComputedFile(CHAIN_TASK_ID));
            assertEquals(POST_COMPUTE_COMPUTED_FILE_NOT_FOUND, exception.getStatusCause());
            assertEquals("computed.json missing", exception.getMessage());
        }
    }
    // endregion

    // region buildResultDigestInComputedFile
    @Test
    void shouldBuildResultDigestInComputedFileForWeb2() {
        final ComputedFile computedFile = ComputedFile.builder().build();
        final String resultDigest = "0x123";

        try (MockedStatic<ResultUtils> resultUtils = Mockito.mockStatic(ResultUtils.class)) {
            resultUtils.when(() -> ResultUtils.computeWeb2ResultDigest(computedFile))
                    .thenReturn(resultDigest);

            assertDoesNotThrow(() -> flowService.buildResultDigestInComputedFile(computedFile, false));
            assertEquals(resultDigest, computedFile.getResultDigest());
        }
    }

    @Test
    void shouldBuildResultDigestInComputedFileForWeb3() {
        final ComputedFile computedFile = ComputedFile.builder().build();
        final String resultDigest = "0x123";

        try (MockedStatic<ResultUtils> resultUtils = Mockito.mockStatic(ResultUtils.class)) {
            resultUtils.when(() -> ResultUtils.computeWeb3ResultDigest(computedFile))
                    .thenReturn(resultDigest);

            assertDoesNotThrow(() -> flowService.buildResultDigestInComputedFile(computedFile, true));
            assertEquals(resultDigest, computedFile.getResultDigest());
        }
    }

    @Test
    void shouldNotBuildResultDigestInComputedFileForWeb2SinceDigestComputationFailed() {
        final ComputedFile computedFile = ComputedFile.builder().build();

        try (MockedStatic<ResultUtils> resultUtils = Mockito.mockStatic(ResultUtils.class)) {
            resultUtils.when(() -> ResultUtils.computeWeb2ResultDigest(computedFile))
                    .thenReturn("");

            final PostComputeException exception = assertThrows(PostComputeException.class, () -> flowService.buildResultDigestInComputedFile(computedFile, false));
            assertEquals(POST_COMPUTE_RESULT_DIGEST_COMPUTATION_FAILED, exception.getStatusCause());
            assertEquals("empty resultDigest", exception.getMessage());
            assertNull(computedFile.getResultDigest());
        }
    }

    @Test
    void shouldNotBuildResultDigestInComputedFileForWeb3SinceDigestComputationFailed() {
        final ComputedFile computedFile = ComputedFile.builder().build();

        try (MockedStatic<ResultUtils> resultUtils = Mockito.mockStatic(ResultUtils.class)) {
            resultUtils.when(() -> ResultUtils.computeWeb3ResultDigest(computedFile))
                    .thenReturn("");

            final PostComputeException exception = assertThrows(PostComputeException.class, () -> flowService.buildResultDigestInComputedFile(computedFile, true));
            assertEquals(POST_COMPUTE_RESULT_DIGEST_COMPUTATION_FAILED, exception.getStatusCause());
            assertEquals("empty resultDigest", exception.getMessage());
            assertNull(computedFile.getResultDigest());
        }
    }
    // endregion

    // region signComputedFile
    @Test
    void shouldSignComputedFile(EnvironmentVariables environment) throws PostComputeException {
        final String privateKey = "0xdd3b993ec21c71c1f6d63a5240850e0d4d8dd83ff70d29e49247958548c1d479";
        final String signature = "enclaveSignature";

        environment.set(RESULT_SIGN_WORKER_ADDRESS, "0x250a3919982ca7CEF58960fF716122dbb4514036");
        environment.set(RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY, privateKey);

        final ComputedFile computedFile = ComputedFile
                .builder()
                .taskId(CHAIN_TASK_ID)
                .resultDigest("0x123")
                .build();

        when(signerService.signEnclaveChallenge(any(), eq(privateKey))).thenReturn(signature);

        assertDoesNotThrow(() -> flowService.signComputedFile(computedFile));
        assertNotNull(computedFile.getEnclaveSignature());
        assertEquals(signature, computedFile.getEnclaveSignature());
    }

    @Test
    void shouldNotSignComputedFileSinceNoWorkerAddress() {
        final ComputedFile computedFile = ComputedFile.builder().build();

        final PostComputeException exception = assertThrows(PostComputeException.class, () -> flowService.signComputedFile(computedFile));
        assertEquals(POST_COMPUTE_WORKER_ADDRESS_MISSING, exception.getStatusCause());
    }

    @Test
    void shouldNotSignComputedFileSinceNoChallengePrivateKey(EnvironmentVariables environment) {
        environment.set(RESULT_SIGN_WORKER_ADDRESS, "0x250a3919982ca7CEF58960fF716122dbb4514036");

        final ComputedFile computedFile = ComputedFile
                .builder()
                .taskId(CHAIN_TASK_ID)
                .resultDigest("0x123")
                .build();

        final PostComputeException exception = assertThrows(PostComputeException.class, () -> flowService.signComputedFile(computedFile));
        assertEquals(POST_COMPUTE_TEE_CHALLENGE_PRIVATE_KEY_MISSING, exception.getStatusCause());
    }
    // endregion

    // region sendComputedFileToHost
    @Test
    void shouldSendComputedFileToHost() {
        final ComputedFile computedFile = ComputedFile.builder().build();

        WorkerApiClient workerApiClient = mock(WorkerApiClient.class);
        try (MockedStatic<WorkerApiManager> workerApiManager = Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient).thenReturn(workerApiClient);

            assertDoesNotThrow(() -> flowService.sendComputedFileToHost(computedFile));
        }
    }

    @Test
    void shouldNotSendComputedFileToHostSinceHttpError() {
        final ComputedFile computedFile = ComputedFile.builder().taskId(CHAIN_TASK_ID).build();

        WorkerApiClient workerApiClient = mock(WorkerApiClient.class);
        doThrow(mock(FeignException.NotFound.class))
                .when(workerApiClient)
                .sendComputedFileToHost(CHAIN_TASK_ID, computedFile);

        try (MockedStatic<WorkerApiManager> workerApiManager = Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient).thenReturn(workerApiClient);

            final PostComputeException exception = assertThrows(PostComputeException.class, () -> flowService.sendComputedFileToHost(computedFile));
            assertEquals(POST_COMPUTE_SEND_COMPUTED_FILE_FAILED, exception.getStatusCause());
        }
    }
    // endregion
}