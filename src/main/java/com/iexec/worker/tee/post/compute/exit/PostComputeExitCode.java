/*
 * Copyright 2020 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.tee.post.compute.exit;

public enum PostComputeExitCode {
    UNKNOWN_ERROR,
    SUCCESS,
    EMPTY_REQUIRED_ENV_VAR,
    COMPUTED_FILE_NOT_FOUND,
    RESULT_DIGEST_COMPUTATION_FAILED,
    OUT_FOLDER_ZIP_FAILED,
    ENCRYPTION_FAILED,
    RESULT_FILE_NOT_FOUND,
    DROPBOX_UPLOAD_FAILED,
    IPFS_UPLOAD_RAILED,
    INVALID_TEE_SIGNATURE,
    SEND_COMPUTED_FILE_FAILED;
}
