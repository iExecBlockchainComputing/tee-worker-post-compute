package com.iexec.uploader.dropbox.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvUtils {

    public static String getEnvVarOrExit(String ENV_VAR) {
        String envVar = System.getenv(ENV_VAR);
        if (envVar == null || envVar.isEmpty()) {
            log.error("Missing env variable (exiting) [envVarName:{}]", ENV_VAR);
            System.exit(1);
        }
        return envVar;
    }

}