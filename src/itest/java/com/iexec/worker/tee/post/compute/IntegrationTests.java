/*
 * Copyright 2020 IEXEC BLOCKCHAIN TECH
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


import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.File;

@Slf4j
class IntegrationTests {

    public static final String RESOURCES_DIR = "src/itest/resources";
    public static final String MOCK_WORKER_DOCKER_COMPOSE_YML =
            RESOURCES_DIR + "/docker-compose.yml";
    public static final String CONTAINERS_PREFIX = "iexec-";
    public static final String WORKER = "worker";
    public static final String POST_COMPUTE = "post-compute";
    public static final String INTERNAL_NETWORK = "iexec-post-compute-net";
    public static final String TEE_WORKER_POST_COMPUTE_IMAGE =
            "nexus.iex.ec/tee-worker-post-compute:dev";

    @ClassRule
    private static final DockerComposeContainer<?> worker =
            new DockerComposeContainer<>(CONTAINERS_PREFIX,
                    new File(MOCK_WORKER_DOCKER_COMPOSE_YML))
                    .withLocalCompose(true);
    @Container
    public GenericContainer<?> postCompute =
            new GenericContainer<>(DockerImageName
                    .parse(TEE_WORKER_POST_COMPUTE_IMAGE));

    @BeforeEach
    void before() {
        worker.withLogConsumer(WORKER, new Slf4jLogConsumer(log) //runtime logs
                .withPrefix(WORKER))
                .start();
        postCompute
                .withLogConsumer(new Slf4jLogConsumer(log)
                        .withPrefix(POST_COMPUTE));
    }

    @AfterEach
    void after() {
        worker.stop();
    }

    @Test
    void shouldHandleCallback() throws InterruptedException {
        postCompute
                .withNetworkMode(INTERNAL_NETWORK)
                .withEnv("RESULT_TASK_ID", "0x0000000000000000000000000000000000000000000000000000000000000001")
                .withEnv("RESULT_STORAGE_CALLBACK", "yes")
                .withEnv("RESULT_SIGN_WORKER_ADDRESS", "0x0000000000000000000000000000000000000002")
                .withEnv("RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY", "0x000000000000000000000000000000000000000000000000000000000000003")
                .withFileSystemBind(RESOURCES_DIR + "/iexec_out", "/iexec_out");

        postCompute.start();

        boolean isLogFound =
                waitForLog("Tee worker post-compute completed!",
                        10);
        Assertions.assertTrue(isLogFound);
    }

    private boolean waitForLog(String wantedOutput, int secondsBeforeTimeout)
            throws InterruptedException {
        boolean ok = false;
        for (int i = 0; i < secondsBeforeTimeout; i++) {
            Thread.sleep(1000);
            if (postCompute.getLogs().contains(wantedOutput)) {
                ok = true;
                break;
            }
        }
        return ok;
    }

}
