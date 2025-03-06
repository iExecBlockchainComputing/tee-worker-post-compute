/*
 * Copyright 2025 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.compute.post.utils;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.common.worker.tee.TeeSessionEnvironmentVariable;
import com.iexec.worker.compute.post.PostComputeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnvUtils {

    public static String getEnvVar(final TeeSessionEnvironmentVariable envVar) {
        return getEnvVar(envVar.name());
    }

    public static String getEnvVar(final String envVarName) {
        String envVar = System.getenv(envVarName);
        if (envVar == null || envVar.isEmpty()) {
            return "";
        }
        return envVar;
    }

    public static String getEnvVarOrThrow(final TeeSessionEnvironmentVariable envVar, final ReplicateStatusCause statusCauseIfMissing) throws PostComputeException {
        return getEnvVarOrThrow(envVar.name(), statusCauseIfMissing);
    }

    public static String getEnvVarOrThrow(final String envVarName, final ReplicateStatusCause statusCauseIfMissing) throws PostComputeException {
        String envVar = System.getenv(envVarName);
        if (envVar == null || envVar.isEmpty()) {
            throw new PostComputeException(statusCauseIfMissing, String.format("Required env var is empty [name:%s]", envVarName));
        }
        return envVar;
    }
}