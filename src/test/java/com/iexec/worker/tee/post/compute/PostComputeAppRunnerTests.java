/*
 * Copyright 2022 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.tee.post.compute;

import com.iexec.common.replicate.ReplicateStatusCause;
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

import static com.iexec.common.worker.result.ResultUtils.RESULT_TASK_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SystemStubsExtension.class)
class PostComputeAppRunnerTests {
    private static final String CHAIN_TASK_ID = "0x0";

    @Spy
    PostComputeAppRunner postComputeAppRunner = new PostComputeAppRunner();

    @BeforeEach
    void openMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void noTaskId() throws Exception {
        final int exitStatus = postComputeAppRunner.start();
        assertEquals(3, exitStatus);
    }

    @Test
    void postComputeSuccess(EnvironmentVariables environment) throws Exception {
        environment.set(RESULT_TASK_ID, CHAIN_TASK_ID);

        PostComputeApp postComputeApp = mock(PostComputeApp.class);
        when(postComputeAppRunner.createPostComputeApp(CHAIN_TASK_ID)).thenReturn(postComputeApp);

        final int exitStatus = postComputeAppRunner.start();

        assertEquals(0, exitStatus);
    }

    @Test
    void knownCauseTransmitted(EnvironmentVariables environment) throws Exception {
        environment.set(RESULT_TASK_ID, CHAIN_TASK_ID);

        PostComputeApp postComputeApp = mock(PostComputeApp.class);
        doThrow(new PostComputeException(ReplicateStatusCause.POST_COMPUTE_COMPUTED_FILE_NOT_FOUND))
                .when(postComputeApp).runPostCompute();

        WorkerApiClient workerApiClient = mock(WorkerApiClient.class);

        when(postComputeAppRunner.createPostComputeApp(CHAIN_TASK_ID)).thenReturn(postComputeApp);

        try (MockedStatic<WorkerApiManager> workerApiManager = Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient)
                    .thenReturn(workerApiClient);
            final int exitStatus = postComputeAppRunner.start();
            assertEquals(1, exitStatus);
        }
    }

    @Test
    void unknownCauseTransmitted(EnvironmentVariables environment) throws Exception {
        environment.set(RESULT_TASK_ID, CHAIN_TASK_ID);

        PostComputeApp postComputeApp = mock(PostComputeApp.class);
        doThrow(new RuntimeException("Unknown cause")).when(postComputeApp).runPostCompute();

        WorkerApiClient workerApiClient = mock(WorkerApiClient.class);

        when(postComputeAppRunner.createPostComputeApp(CHAIN_TASK_ID)).thenReturn(postComputeApp);
        try (MockedStatic<WorkerApiManager> workerApiManager = Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient)
                    .thenReturn(workerApiClient);
            final int exitStatus = postComputeAppRunner.start();
            assertEquals(1, exitStatus);
        }
    }

    @Test
    void causeNotTransmitted(EnvironmentVariables environment) throws Exception {
        environment.set(RESULT_TASK_ID, CHAIN_TASK_ID);

        PostComputeApp postComputeApp = mock(PostComputeApp.class);
        doThrow(new PostComputeException(ReplicateStatusCause.POST_COMPUTE_COMPUTED_FILE_NOT_FOUND))
                .when(postComputeApp).runPostCompute();

        WorkerApiClient workerApiClient = mock(WorkerApiClient.class);
        doThrow(FeignException.NotFound.class)
                .when(workerApiClient).sendExitCauseForPostComputeStage(eq(CHAIN_TASK_ID), any());

        when(postComputeAppRunner.createPostComputeApp(CHAIN_TASK_ID)).thenReturn(postComputeApp);
        try (MockedStatic<WorkerApiManager> workerApiManager = Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient)
                    .thenReturn(workerApiClient);
            final int exitStatus = postComputeAppRunner.start();
            assertEquals(2, exitStatus);
        }
    }
}
