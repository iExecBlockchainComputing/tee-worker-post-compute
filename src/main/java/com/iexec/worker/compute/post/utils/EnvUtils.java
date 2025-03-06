package com.iexec.worker.compute.post.utils;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.common.worker.tee.TeeSessionEnvironmentVariable;
import com.iexec.worker.compute.post.PostComputeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvUtils {

    private EnvUtils() {

    }

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