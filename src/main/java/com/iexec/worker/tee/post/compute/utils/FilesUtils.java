package com.iexec.worker.tee.post.compute.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class FilesUtils {

    public static final String PROTECTED_REGION_PATH = "/scone";
    // TODO: use SLASH_IEXEC_OUT of iexec-common
    public static final String IEXEC_OUT_PATH = PROTECTED_REGION_PATH + "/iexec_out";
    public static final String COMPLETED_COMPUTE_IEXEC_FILE_PATH = IEXEC_OUT_PATH + "/completed-compute.iexec";

    public static boolean isCompletedComputeFilePresent() {
        File completedComputeFile = new File(COMPLETED_COMPUTE_IEXEC_FILE_PATH);
        return completedComputeFile.exists();
    }
}