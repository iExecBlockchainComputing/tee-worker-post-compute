package com.iexec.worker.tee.post.compute.encrypter;

import com.iexec.common.utils.BytesUtils;
import com.iexec.common.utils.FileHelper;
import org.junit.Before;
import org.junit.Test;
import org.web3j.crypto.Hash;

import static com.iexec.common.utils.FileHelper.readFile;
import static com.iexec.common.utils.FileHelper.readFileBytes;
import static org.junit.Assert.assertEquals;

public class EncryptionServiceTests {

    private final static String DOT_SLASH = "./src/test/resources/encrypter/";
    private String plainTextRsaPublicKey;
    private String plainTextRsaPrivateKey;

    @Before
    public void before(){
        plainTextRsaPublicKey = new String(readFile(DOT_SLASH + "test_rsa_key.pub"));
        plainTextRsaPrivateKey = new String(readFile(DOT_SLASH + "test_rsa_key"));
    }


    private void removeOldFiles() {
        FileHelper.deleteFolder(DOT_SLASH + "0xtask1-encrypted");
        FileHelper.deleteFile(DOT_SLASH + "clear-0xtask1.zip");
    }

    @Test
    public void shouldEncryptAndDecrypt() {
        removeOldFiles();
        String inDataFileName = "0xtask1.zip";

        // Encryption side
        String originalDataHash = BytesUtils.bytesToString(Hash.sha3(readFileBytes(DOT_SLASH + inDataFileName)));
        String encryptedResultFolder = EncryptionService.encryptData(DOT_SLASH + inDataFileName, plainTextRsaPublicKey);

        // Decryption side
        String clearDataHash = EncryptionService.decryptData(encryptedResultFolder + "/" + inDataFileName + ".aes", plainTextRsaPrivateKey);

        removeOldFiles();// comment this if you want to see created files
        assertEquals(originalDataHash, clearDataHash);
    }


}
