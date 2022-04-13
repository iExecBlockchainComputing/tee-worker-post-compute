package com.iexec.worker.tee.post.compute.utils;

import com.iexec.worker.tee.post.compute.PostComputeException;
import com.iexec.worker.tee.post.compute.exit.PostComputeExitCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvUtils {

    public static String getEnvVar(String envVarName) {
        String envVar = System.getenv(envVarName);
        if (envVar == null || envVar.isEmpty()) {
            return "";
        }
        return envVar;
    }

    public static String getEnvVarOrThrow(String envVarName) throws PostComputeException {
        String envVar = System.getenv(envVarName);
        if (envVar == null || envVar.isEmpty()) {
            log.error("Required env var is empty [name:{}]", envVarName);
            throw new PostComputeException(PostComputeExitCode.EMPTY_REQUIRED_ENV_VAR);
        }
        return envVar;
    }

    public static void exit() {
        System.exit(1);
    }
}