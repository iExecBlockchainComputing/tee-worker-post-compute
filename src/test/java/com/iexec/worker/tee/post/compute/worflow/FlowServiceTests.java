package com.iexec.worker.tee.post.compute.worflow;

import com.iexec.common.result.ComputedFile;
import com.iexec.common.utils.FileHelper;
import com.iexec.common.utils.IexecFileHelper;
import com.iexec.common.worker.result.ResultUtils;
import com.iexec.worker.tee.post.compute.PostComputeException;
import com.iexec.worker.tee.post.compute.worker.WorkerApiClient;
import com.iexec.worker.tee.post.compute.worker.WorkerApiManager;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static com.iexec.common.replicate.ReplicateStatusCause.*;
import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY;
import static com.iexec.common.worker.result.ResultUtils.RESULT_SIGN_WORKER_ADDRESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(SystemStubsExtension.class)
class FlowServiceTests {
    private static final String CHAIN_TASK_ID = "0x0";

    @Spy
    FlowService flowService;

    @BeforeEach
    void openMocks() {
        MockitoAnnotations.openMocks(this);
    }

    // region readComputedFile
    @Test
    void shouldReadComputedFile() {
        try (MockedStatic<IexecFileHelper> iexecFileHelper = Mockito.mockStatic(IexecFileHelper.class)) {
            iexecFileHelper.when(() -> IexecFileHelper.readComputedFile(CHAIN_TASK_ID, FileHelper.SLASH_IEXEC_OUT))
                    .thenReturn(new ComputedFile());

            assertDoesNotThrow(() -> flowService.readComputedFile(CHAIN_TASK_ID));
        }
    }

    @Test
    void shouldNotReadComputedFile() {
        try (MockedStatic<IexecFileHelper> iexecFileHelper = Mockito.mockStatic(IexecFileHelper.class)) {
            iexecFileHelper.when(() -> IexecFileHelper.readComputedFile(CHAIN_TASK_ID, FileHelper.SLASH_IEXEC_OUT))
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
        final ComputedFile computedFile = new ComputedFile();
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
        final ComputedFile computedFile = new ComputedFile();
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
        final ComputedFile computedFile = new ComputedFile();

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
        final ComputedFile computedFile = new ComputedFile();

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
    void shouldSignComputedFile(EnvironmentVariables environment) {
        environment.set(RESULT_SIGN_WORKER_ADDRESS, "0x250a3919982ca7CEF58960fF716122dbb4514036");
        environment.set(RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY, "0xdd3b993ec21c71c1f6d63a5240850e0d4d8dd83ff70d29e49247958548c1d479");

        final ComputedFile computedFile = ComputedFile
                .builder()
                .taskId(CHAIN_TASK_ID)
                .resultDigest("0x123")
                .build();

        assertDoesNotThrow(() -> flowService.signComputedFile(computedFile));
        assertNotNull(computedFile.getEnclaveSignature());
        assertEquals("0xfcc6bce5eb04284c2eb1ed14405b943574343b1abda33628fbf94a374b18dd16541c6ebf63c6943d8643ff03c7aa17f1cb17b0a8d297d0fd95fc914bdd0e85f81b", computedFile.getEnclaveSignature());
    }

    @Test
    void shouldNotSignComputedFileSinceNoWorkerAddress() {
        final ComputedFile computedFile = new ComputedFile();

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
        final ComputedFile computedFile = new ComputedFile();

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