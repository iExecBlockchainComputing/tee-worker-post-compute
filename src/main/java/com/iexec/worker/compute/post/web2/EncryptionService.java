/*
 * Copyright 2019-2024 IEXEC BLOCKCHAIN TECH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iexec.worker.compute.post.web2;

import com.iexec.common.security.EncryptionHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptionService {
    /*
     * #1: Large file encryption is made with AES
     * #2: AES key is encrypted with RSA key
     *
     * before
     * └── result-0xabc.zip
     *
     * after
     * ├── result-0xabc.zip
     * ├── encrypted-result-0xabc
     * │   ├── aes-key.rsa
     * │   └── result-0xabc.zip.aes
     * └── encrypted-result-0xabc.zip (if produceZip)
     *
     * Returns: folder or zip path
     *
     * */
    public String encryptData(String inDataFilePath, String plainTextRsaPub, boolean produceZip) {
        return EncryptionHelper.encryptData(inDataFilePath, plainTextRsaPub, produceZip);
    }
}
