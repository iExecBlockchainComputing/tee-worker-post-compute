package com.iexec.worker.tee.post.compute.encrypter;

import com.iexec.common.utils.BytesUtils;
import com.iexec.common.utils.FileHelper;
import com.iexec.worker.tee.post.compute.utils.FilesUtils;
import org.junit.Before;
import org.junit.Test;
import org.web3j.crypto.Hash;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.iexec.common.utils.FileHelper.readFile;
import static com.iexec.common.utils.FileHelper.readFileBytes;
import static org.junit.Assert.assertEquals;

public class EncryptionServiceTests {

    private final static String DOT_SLASH = "./src/test/resources/encrypter/";
    private String plainTextRsaPublicKey;
    private String plainTextRsaPrivateKey;

    @Before
    public void before(){
        plainTextRsaPublicKey = readFile(DOT_SLASH + "test_rsa_key.pub");
        plainTextRsaPrivateKey = readFile(DOT_SLASH + "test_rsa_key");
    }


    private void removeOldFiles() {
        FileHelper.deleteFolder(DOT_SLASH + "encrypted-result-0xabc");
        FileHelper.deleteFile(DOT_SLASH + "clear-result-0xabc.zip");
    }

    @Test
    public void shouldEncryptAndDecrypt() {
        removeOldFiles();
        String inDataFileName = "result-0xabc.zip";

        // Encryption side
        String originalDataHash = BytesUtils.bytesToString(Hash.sha3(FilesUtils.readAllBytes(DOT_SLASH + inDataFileName)));
        String encryptedResultFolder = EncryptionService.encryptData(DOT_SLASH + inDataFileName, plainTextRsaPublicKey, false);

        // Decryption side
        String clearDataPath = EncryptionService.decryptData(encryptedResultFolder + "/" + inDataFileName + ".aes", plainTextRsaPrivateKey);
        String clearDataHash = BytesUtils.bytesToString(Hash.sha3(FilesUtils.readAllBytes(clearDataPath)));

        removeOldFiles();// comment this if you want to see created files
        assertEquals(originalDataHash, clearDataHash);
    }


}
