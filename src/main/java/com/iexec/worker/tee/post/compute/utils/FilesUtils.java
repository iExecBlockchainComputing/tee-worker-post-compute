package com.iexec.worker.tee.post.compute.utils;

import com.iexec.common.utils.IexecFileHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static com.iexec.common.utils.FileHelper.SLASH_IEXEC_OUT;

@Slf4j
public class FilesUtils {

    public static final String PROTECTED_IEXEC_OUT = "/scone" + SLASH_IEXEC_OUT;
    public static final String UNPROTECTED_IEXEC_OUT = SLASH_IEXEC_OUT;
    public static final String SLASH_COMPUTED_FILE = File.separator + IexecFileHelper.COMPUTED_JSON;

}