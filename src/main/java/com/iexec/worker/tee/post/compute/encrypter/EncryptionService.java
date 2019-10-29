package com.iexec.worker.tee.post.compute.encrypter;

import com.iexec.common.utils.BytesUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.web3j.crypto.Hash;

import java.io.File;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;

import static com.iexec.common.security.CipherHelper.*;
import static com.iexec.common.utils.FileHelper.*;

//TODO Move EncryptionService to common when needed
@Slf4j
public class EncryptionService {

    public static final String NO_ENCRYPTION = "none";
    public static final String ENCRYPTION_REQUESTED = "yes";

    /*
     *
     * #1: Large file encryption is made with AES
     * #2: AES key is encrypted with RSA key
     *
     * */
    public static String encryptData(String inDataFilePath, String plainTextRsaPub, boolean produceZip) {
        String inDataFilename = FilenameUtils.getName(inDataFilePath);
        String outEncryptedDataFilename = inDataFilename + ".aes";
        String outEncryptedAesKeyFilename = "aesKey.rsa";

        String workDir = Paths.get(inDataFilePath).getParent().toString(); //"/tmp/scone";
        String outEncDir = workDir + "/" + FilenameUtils.removeExtension(inDataFilename) + "-encrypted"; //location of future encrypted files (./0x1_result-encrypted)

        // Get data to encrypt
        byte[] data = readFileBytes(inDataFilePath);
        if (data == null) {
            log.error("Failed to encryptData (readFile error) [inDataFilePath:{}]", inDataFilePath);
            return "";
        }
        // Generate AES key for data encryption
        byte[] aesKey = generateAesKey();
        if (aesKey == null) {
            log.error("Failed to encryptData (generateAesKey error) [inDataFilePath:{}]", inDataFilePath);
            return "";
        }
        // Encrypt data with AES key
        byte[] encryptedData = aesEncrypt(data, aesKey);
        if (encryptedData == null) {
            log.error("Failed to encryptData (aesEncrypt error) [inDataFilePath:{}]", inDataFilePath);
            return "";
        }
        // Create folder for future outEncryptedData & outEncryptedAesKey
        boolean isOutDirCreated = createFolder(outEncDir);
        if (!isOutDirCreated) {
            log.error("Failed to encryptData (isOutDirCreated error) [inDataFilePath:{}]", inDataFilePath);
            return "";
        }
        // Store encrypted data in ./0xtask1 [outEncDir]
        boolean isEncryptedDataStored = writeFile(outEncDir + "/" + outEncryptedDataFilename, encryptedData);
        if (!isEncryptedDataStored) {
            log.error("Failed to encryptData (isEncryptedDataStored error) [inDataFilePath:{}]", inDataFilePath);
            return "";
        }
        // Get RSA public key
        PublicKey rsaPublicKey = plainText2RsaPublicKey(plainTextRsaPub);
        if (rsaPublicKey == null) {
            log.error("Failed to encryptData (getRsaPublicKey error) [inDataFilePath:{}]", inDataFilePath);
            return "";
        }
        // Encrypt AES key with RSA public key
        byte[] encryptedAesKey = rsaEncrypt(aesKey, rsaPublicKey);
        if (encryptedAesKey == null) {
            log.error("Failed to encryptData (rsaEncrypt error) [inDataFilePath:{}]", inDataFilePath);
            return "";
        }
        // Store encrypted AES key in ./0xtask1 [outEncDir]
        boolean isEncryptedAesKeyStored = writeFile(outEncDir + "/" + outEncryptedAesKeyFilename, encryptedAesKey);
        if (!isEncryptedAesKeyStored) {
            log.error("Failed to encryptData (isEncryptedAesKeyStored error) [inDataFilePath:{}]", inDataFilePath);
            return "";
        }

        if (produceZip) {
            // Zip encrypted files folder
            File outEncZip = zipFolder(outEncDir);
            if (outEncZip == null) {
                log.error("Failed to encryptData (outEncZip error) [inDataFilePath:{}]", inDataFilePath);
                return "";
            }
            return outEncZip.getAbsolutePath();
        }

        return outEncDir;
    }

    /*
     *
     * Required: aesKey.rsa file should be found next to encryptedDataFile
     *
     * #1: AES key is decrypted with RSA
     * #2: Data is decrypted with AES key
     *
     * */
    public static String decryptData(String encryptedDataFilePath, String plainTextRsaPriv) {
        String encryptedResultFolder = Paths.get(encryptedDataFilePath).getParent().toString();
        String aesKeyFilename = "aesKey.rsa";
        String outClearDataFilename = "clear-" + FilenameUtils.getBaseName(encryptedDataFilePath);

        // Get encrypted AES key
        byte[] encryptedAesKey = readFileBytes(encryptedResultFolder + "/" + aesKeyFilename);
        if (encryptedAesKey == null) {
            log.error("Failed to decryptData (encryptedAesKey error) [encryptedDataFilePath:{}]", encryptedDataFilePath);
            return "";
        }
        // Get RSA private key
        PrivateKey rsaPrivateKey = plainText2RsaPrivateKey(plainTextRsaPriv);
        if (rsaPrivateKey == null) {
            log.error("Failed to decryptData (rsaPrivateKey error) [encryptedDataFilePath:{}]", encryptedDataFilePath);
            return "";
        }
        // Decrypt encrypted AES key
        byte[] aesKey = rsaDecrypt(encryptedAesKey, rsaPrivateKey);
        if (aesKey == null) {
            log.error("Failed to decryptData (aesKey error) [encryptedDataFilePath:{}]", encryptedDataFilePath);
            return "";
        }
        // Check is AES data
        boolean isAesExtensionEncryptedData = FilenameUtils.getExtension(encryptedDataFilePath).equals("aes");
        if (!isAesExtensionEncryptedData) {
            log.error("Failed to decryptData (isAesExtensionEncryptedData error) [encryptedDataFilePath:{}]", encryptedDataFilePath);
            return "";
        }
        // Get encrypted data
        byte[] encryptedData = readFileBytes(encryptedDataFilePath);
        if (encryptedData == null) {
            log.error("Failed to decryptData (encryptedData error) [encryptedDataFilePath:{}]", encryptedDataFilePath);
            return "";
        }
        // Decrypt data with AES key
        byte[] clearData = aesDecrypt(encryptedData, aesKey);
        if (clearData == null) {
            log.error("Failed to decryptData (clearData error) [encryptedDataFilePath:{}]", encryptedDataFilePath);
            return "";
        }
        // Store clear data
        boolean isClearDataStored = writeFile(encryptedResultFolder + "/../" + outClearDataFilename, clearData);
        if (!isClearDataStored) {
            log.error("Failed to decryptData (isClearDataStored error) [encryptedDataFilePath:{}]", encryptedDataFilePath);
            return "";
        }

        return BytesUtils.bytesToString(Hash.sha3(clearData));
    }


}
