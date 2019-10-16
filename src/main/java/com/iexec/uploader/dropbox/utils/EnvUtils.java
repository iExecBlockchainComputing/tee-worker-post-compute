package com.iexec.uploader.dropbox.utils;

public class EnvUtils {

    public static String getEnvVarOrExit(String ENV_VAR) {
        String envVar = System.getenv(ENV_VAR);
        if (envVar == null || envVar.isEmpty()) {
            System.err.println(ENV_VAR + " missing (exiting)");
            System.exit(1);
        }
        return envVar;
    }

}