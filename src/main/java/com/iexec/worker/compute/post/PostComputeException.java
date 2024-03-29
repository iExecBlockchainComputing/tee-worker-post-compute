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

package com.iexec.worker.compute.post;

import com.iexec.common.replicate.ReplicateStatusCause;
import lombok.Getter;

public class PostComputeException extends Exception {

    @Getter
    private final ReplicateStatusCause statusCause;

    public PostComputeException(ReplicateStatusCause statusCause) {
        this(statusCause, statusCause.name());
    }

    public PostComputeException(ReplicateStatusCause statusCause, String message) {
        super(message);
        this.statusCause = statusCause;
    }

    public PostComputeException(ReplicateStatusCause statusCause, String message, Throwable cause) {
        super(message, cause);
        this.statusCause = statusCause;
    }
}
