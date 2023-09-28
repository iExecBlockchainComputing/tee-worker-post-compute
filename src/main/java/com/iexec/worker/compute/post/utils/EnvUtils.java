package com.iexec.worker.compute.post.utils;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.worker.compute.post.PostComputeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvUtils {

    private EnvUtils() {

    }

    public static String getEnvVar(String envVarName) {
        String envVar = System.getenv(envVarName);
        if (envVar == null || envVar.isEmpty()) {
            return "";
        }
        return envVar;
    }

    public static String getEnvVarOrThrow(String envVarName, ReplicateStatusCause statusCauseIfMissing) throws PostComputeException {
        String envVar = System.getenv(envVarName);
        if (envVar == null || envVar.isEmpty()) {
            throw new PostComputeException(statusCauseIfMissing, String.format("Required env var is empty [name:%s]", envVarName));
        }
        return envVar;
    }
}