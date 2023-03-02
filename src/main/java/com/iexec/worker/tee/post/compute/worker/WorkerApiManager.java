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

package com.iexec.worker.tee.post.compute.worker;

import com.iexec.common.utils.EnvUtils;
import com.iexec.common.utils.FeignBuilder;
import feign.Logger;
import org.apache.commons.lang3.StringUtils;

public class WorkerApiManager {
    private static final String WORKER_HOST_ENV_VAR = "WORKER_HOST";
    private static final String WORKER_HOST = getWorkerHost();

    private static WorkerApiClient workerApiClient;

    private WorkerApiManager() {
        throw new UnsupportedOperationException();
    }

    public static WorkerApiClient getWorkerApiClient() {
        if (workerApiClient == null) {
            workerApiClient = FeignBuilder
                    .createBuilder(Logger.Level.FULL)
                    .target(WorkerApiClient.class, "http://" + WORKER_HOST);
        }
        return workerApiClient;
    }

    public static String getWorkerHost() {
        final String host = EnvUtils.getEnvVar(WORKER_HOST_ENV_VAR);
        return StringUtils.isBlank(host)
                ? "worker:13100"
                : host;
    }
}
