package com.iexec.worker.tee.post.compute.encrypter;

import com.iexec.common.security.EncryptionHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptionService {

    /*
     *
     * #1: Large file encryption is made with AES
     * #2: AES key is encrypted with RSA key
     *
     * before
     * └── result-0xabc.zip
     *
     * after
     * ├── result-0xabc.zip
     * ├── encrypted-result-0xabc
     * │   ├── aes-key.rsa
     * │   └── result-0xabc.zip.aes
     * └── encrypted-result-0xabc.zip (if produceZip)
     *
     * Returns: folder or zip path
     *
     * */
    public static String encryptData(String inDataFilePath, String plainTextRsaPub, boolean produceZip) {
        return EncryptionHelper.encryptData(inDataFilePath, plainTextRsaPub, produceZip);
    }

    /*
     *
     * Required: aes-key.rsa file should be found next to encryptedDataFile
     *
     * #1: AES key is decrypted with RSA
     * #2: Data is decrypted with AES key
     *
     * before
     * └── encrypted-result-0xabc.zip
     * with zip content
     * ├── aes-key.rsa
     * └── result-0xabc.zip.aes
     *
     * after
     * ├── encrypted-result-0xabc.zip
     * └── clear-result-0xabc.zip
     *
     * Returns: clear data path (zip here)
     *
     * */
    public static String decryptData(String encryptedDataFilePath, String plainTextRsaPriv) {
        return EncryptionHelper.decryptData(encryptedDataFilePath, plainTextRsaPriv);
    }

}
