package com.iexec.worker.tee.post.compute.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvUtils {

    //TODO: move these fields to common
    public static final String TASK_ID = "TASK_ID";
    public static final String WORKER_ADDRESS = "WORKER_ADDRESS";
    public static final String IEXEC_REQUESTER_RESULT_ENCRYPTION = "IEXEC_REQUESTER_RESULT_ENCRYPTION";
    public static final String IEXEC_REQUESTER_STORAGE_LOCATION = "IEXEC_REQUESTER_STORAGE_LOCATION";
    public static final String IEXEC_REQUESTER_STORAGE_PROXY = "IEXEC_REQUESTER_STORAGE_PROXY";
    public static final String BENEFICIARY_PUBLIC_KEY_BASE64 = "BENEFICIARY_PUBLIC_KEY_BASE64";
    public static final String REQUESTER_STORAGE_TOKEN = "REQUESTER_STORAGE_TOKEN";
    public static final String IEXEC_REQUESTER_SHOULD_CALLBACK = "IEXEC_REQUESTER_SHOULD_CALLBACK";
    public static final String TEE_CHALLENGE_PRIVATE_KEY = "TEE_CHALLENGE_PRIVATE_KEY";


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

    public static boolean shouldCallback() {
        return EnvUtils.getEnvVar(IEXEC_REQUESTER_SHOULD_CALLBACK).equals("yes");
    }

}