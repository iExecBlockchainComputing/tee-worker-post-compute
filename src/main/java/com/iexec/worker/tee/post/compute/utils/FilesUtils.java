package com.iexec.worker.tee.post.compute.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class FilesUtils {

    // TODO: use SLASH_IEXEC_OUT of iexec-common

    public static final String PROTECTED_IEXEC_OUT =  "/scone/iexec_out";
    public static final String UNPROTECTED_IEXEC_OUT = "/iexec_out";

    public static final String COMPLETED_COMPUTE_FILE = "/completed-compute.iexec";
    public static final String CALLBACK_FILE = "/callback.iexec";

    public static boolean isCompletedComputeFilePresent() {
        File completedComputeFile = new File(PROTECTED_IEXEC_OUT + COMPLETED_COMPUTE_FILE);
        return completedComputeFile.exists();
    }
}