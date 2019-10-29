package com.iexec.worker.tee.post.compute.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class FilesUtils {



    public static final String SCONE_PATH = "/scone";///tmp
    public static final String IEXEC_OUT_PATH = SCONE_PATH + "/iexec_out";
    public static final String COMPLETED_COMPUTE_IEXEC_FILE_PATH = IEXEC_OUT_PATH + "/completed-compute.iexec";
    public static final String IEXEC_OUT_ZIP_PATH = IEXEC_OUT_PATH + ".zip";

    /*
    public static String getIexecOutZipPath(String taskId) {
        return "/scone/iexec_out/" + taskId + "_result.zip";
    }
    */


    public static boolean isCompletedComputeFilePresent() {
        File completedComputeFile = new File(COMPLETED_COMPUTE_IEXEC_FILE_PATH);
        return completedComputeFile.exists();
    }

    public static String getIexecOutZipPath() {
        return IEXEC_OUT_ZIP_PATH;
    }

}