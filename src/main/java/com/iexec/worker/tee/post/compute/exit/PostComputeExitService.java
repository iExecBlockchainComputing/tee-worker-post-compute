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

package com.iexec.worker.tee.post.compute.exit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * On exit, Post-Compute should send its exit message to the worker
 * - either SUCCESS or an error.
 */
@Slf4j
public class PostComputeExitService {
    public static final String WORKER_HOST = "worker:13100";

    public static boolean sendExitMessageToHost(String chainTaskId, PostComputeExitCode exitCode) {
        log.info("Send exit message stage started [exitCode:{}]", exitCode);
        HttpEntity<PostComputeExitMessage> request = new HttpEntity<>(new PostComputeExitMessage(exitCode));
        String baseUrl = String.format("http://%s/post-compute/%s/exit",
                WORKER_HOST, chainTaskId);
        ResponseEntity<String> response = new RestTemplate()
                .postForEntity(baseUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Send exit message stage failed [taskId:{}, exitCode:{}]",
                    chainTaskId, exitCode);
            return false;
        }
        log.info("Send exit message stage completed");
        return true;
    }
}
