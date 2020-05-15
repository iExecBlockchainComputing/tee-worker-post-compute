package com.iexec.worker.tee.post.compute.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static com.iexec.common.utils.FileHelper.SLASH_IEXEC_OUT;

@Slf4j
public class FilesUtils {

    public static final String PROTECTED_IEXEC_OUT = "/scone" + SLASH_IEXEC_OUT;
    public static final String UNPROTECTED_IEXEC_OUT = SLASH_IEXEC_OUT;

    // TODO: move from worker to common and use it here
    //public static final String SLASH_ENCLAVE_SIG_FILE = File.separator + "enclaveSig.iexec";
    //public static final String SLASH_CALLBACK_FILE = File.separator + "callback.iexec";
    //public static final String SLASH_COMPUTED_FILE = File.separator + "completed-compute.iexec";
    public static final String SLASH_COMPUTED_FILE = File.separator + "computed.json";

    public static boolean isCompletedComputeFilePresent() {
        System.out.println(PROTECTED_IEXEC_OUT + SLASH_COMPUTED_FILE);
        File completedComputeFile = new File(PROTECTED_IEXEC_OUT + SLASH_COMPUTED_FILE);
        return completedComputeFile.exists();
    }

 /*
    public static boolean copyComputedToUnprotected() {
        try {
            FileUtils.copyFile(
                    new File(PROTECTED_IEXEC_OUT + SLASH_COMPUTED_FILE),
                    new File(UNPROTECTED_IEXEC_OUT + SLASH_COMPUTED_FILE)
            );
            return true;
        } catch (IOException e) {
            log.error("copyComputedToUnprotected failed");
        }

        return false;
    }
    */

    /*
     * TODO 1
     * Use worker method moved to common
     * TODO 2
     * Enable non-deterministic app for trust > 0 with :
     * if (determinismFilePath.toFile().exists()) { return getHashFromDeterminismIexecFile(determinismFilePath); }
     */
    /*
    public static String getIexecOutZipDigest(String zipPath) {
        byte[] content = new byte[0];
        try {
            content = Files.readAllBytes(Paths.get(zipPath));
        } catch (IOException e) {
            log.info("Failed to getIexecOutZipDigest");
        }

        return bytesToString(Hash.sha256(content));
    }
    */




}