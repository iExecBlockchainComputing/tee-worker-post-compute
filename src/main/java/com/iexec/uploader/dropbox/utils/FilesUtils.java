package com.iexec.uploader.dropbox.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilesUtils {

    public static String getResultFilePath(String taskId) {
        return "/scone/" + taskId + "_result.zip";
    }

}