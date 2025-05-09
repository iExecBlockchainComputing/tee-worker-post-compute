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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexec.common.result.ComputedFile;
import com.iexec.common.utils.IexecFileHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;

import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.*;

@Slf4j
@ExtendWith(SystemStubsExtension.class)
class IntegrationTests {

    public static final String RESOURCES_DIR = "src/itest/resources";
    public static final String MOCK_WORKER_DOCKER_COMPOSE_YML = RESOURCES_DIR + "/docker-compose.yml";
    public static final String CONTAINERS_PREFIX = "iexec-";
    private static final String WORKER_SERVICE_NAME = "worker";
    private static final int WORKER_SERVICE_PORT = 13100;

    @Container
    private static final ComposeContainer worker =
            new ComposeContainer(CONTAINERS_PREFIX, new File(MOCK_WORKER_DOCKER_COMPOSE_YML))
                    .withExposedService(WORKER_SERVICE_NAME, WORKER_SERVICE_PORT);

    @BeforeEach
    void before() {
        worker.withLogConsumer(WORKER_SERVICE_NAME, new Slf4jLogConsumer(log).withPrefix(WORKER_SERVICE_NAME))
                .start();
    }

    @AfterEach
    void after() {
        worker.stop();
    }

    @Test
    void shouldHandleCallback(EnvironmentVariables environment) {
        final String taskId = "0x0000000000000000000000000000000000000000000000000000000000000001";
        environment.set(IEXEC_TASK_ID, taskId);
        environment.set(RESULT_STORAGE_CALLBACK, "true");
        environment.set(SIGN_WORKER_ADDRESS, "0x0000000000000000000000000000000000000002");
        environment.set(SIGN_TEE_CHALLENGE_PRIVATE_KEY, "0x000000000000000000000000000000000000000000000000000000000000003");
        environment.set("WORKER_HOST", worker.getServiceHost(WORKER_SERVICE_NAME, WORKER_SERVICE_PORT) + ":" + worker.getServicePort(WORKER_SERVICE_NAME, WORKER_SERVICE_PORT));

        try (MockedStatic<IexecFileHelper> iexecFileHelper = Mockito.mockStatic(IexecFileHelper.class)) {
            final ObjectMapper objectMapper = new ObjectMapper();
            iexecFileHelper.when(() -> IexecFileHelper.readComputedFile(taskId, IexecFileHelper.SLASH_IEXEC_OUT))
                    .thenAnswer(invocation -> {
                        final ComputedFile computedFile = objectMapper.readValue(new File(RESOURCES_DIR + "/iexec_out/computed.json"), ComputedFile.class);
                        computedFile.setTaskId(taskId);
                        return computedFile;
                    });
            final int exitCode = new PostComputeAppRunner().start();
            Assertions.assertEquals(0, exitCode, "Exit code should be 0.");
        }
    }

}
