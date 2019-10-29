package com.iexec.worker.tee.post.compute.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvUtils {

    public static String getEnvVarOrExit(String ENV_VAR) {
        String env = getEnvVar(ENV_VAR);

        if (env.isEmpty()) {
            log.error("Missing env variable (exiting) [envVarName:{}]", ENV_VAR);
            System.exit(1);
        }

        return env;
    }

    public static String getEnvVar(String ENV_VAR) {
        String envVar = System.getenv(ENV_VAR);
        if (envVar == null || envVar.isEmpty()) {
            return "";
        }
        return envVar;
    }

}