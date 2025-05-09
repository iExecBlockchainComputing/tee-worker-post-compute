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

package com.iexec.worker.compute.post;

import com.iexec.worker.compute.post.web2.Web2ResultService;
import com.iexec.worker.compute.post.workflow.FlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_COMPUTED_FILE_NOT_FOUND;
import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_RESULT_DIGEST_COMPUTATION_FAILED;
import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.RESULT_STORAGE_CALLBACK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SystemStubsExtension.class)
@ExtendWith(MockitoExtension.class)
class PostComputeAppTests {
    private static final String CHAIN_TASK_ID = "0x0";

    @Mock
    FlowService flowService;

    @Mock
    Web2ResultService web2ResultService;

    PostComputeApp postComputeApp;

    @BeforeEach
    void openMocks() {
        postComputeApp = spy(new PostComputeApp(flowService, web2ResultService, CHAIN_TASK_ID));
    }

    @Test
    void postComputeWithCallbackSuccess(EnvironmentVariables environment) {
        environment.set(RESULT_STORAGE_CALLBACK, "yes");

        assertDoesNotThrow(postComputeApp::runPostCompute);
    }

    @Test
    void postComputeWithoutCallbackSuccess(EnvironmentVariables environment) {
        environment.set(RESULT_STORAGE_CALLBACK, "no");

        assertDoesNotThrow(postComputeApp::runPostCompute);
    }

    @Test
    void postComputeFailedSinceReadComputedFileFailed() throws PostComputeException {
        when(flowService.readComputedFile(CHAIN_TASK_ID))
                .thenThrow(new PostComputeException(POST_COMPUTE_COMPUTED_FILE_NOT_FOUND));

        final PostComputeException postComputeException = assertThrows(PostComputeException.class, postComputeApp::runPostCompute);
        assertEquals(POST_COMPUTE_COMPUTED_FILE_NOT_FOUND, postComputeException.getStatusCause());
    }

    @Test
    void postComputeFailedSinceBuildResultDigestInComputedFileFailed() throws PostComputeException {
        doThrow(new PostComputeException(POST_COMPUTE_RESULT_DIGEST_COMPUTATION_FAILED))
                .when(flowService).buildResultDigestInComputedFile(any(), anyBoolean());

        final PostComputeException postComputeException = assertThrows(PostComputeException.class, postComputeApp::runPostCompute);
        assertEquals(POST_COMPUTE_RESULT_DIGEST_COMPUTATION_FAILED, postComputeException.getStatusCause());
    }
}
