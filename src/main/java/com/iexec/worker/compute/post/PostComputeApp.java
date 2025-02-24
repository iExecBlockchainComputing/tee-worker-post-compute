/*
 * Copyright 2022-2025 IEXEC BLOCKCHAIN TECH
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

import com.iexec.common.result.ComputedFile;
import com.iexec.worker.compute.post.utils.EnvUtils;
import com.iexec.worker.compute.post.web2.Web2ResultService;
import com.iexec.worker.compute.post.workflow.FlowService;

import static com.iexec.common.worker.result.ResultUtils.RESULT_STORAGE_CALLBACK;
import static com.iexec.commons.poco.tee.TeeUtils.booleanFromYesNo;

public class PostComputeApp {
    private final FlowService flowService;
    private final Web2ResultService web2ResultService;
    private final String chainTaskId;

    public PostComputeApp(String chainTaskId) {
        this.flowService = new FlowService();
        this.web2ResultService = new Web2ResultService();
        this.chainTaskId = chainTaskId;
    }

    public PostComputeApp(FlowService flowService,
                          Web2ResultService web2ResultService,
                          String chainTaskId) {
        this.flowService = flowService;
        this.web2ResultService = web2ResultService;
        this.chainTaskId = chainTaskId;
    }

    public void runPostCompute() throws PostComputeException {
        final boolean shouldCallback = booleanFromYesNo(EnvUtils.getEnvVar(RESULT_STORAGE_CALLBACK));

        final ComputedFile computedFile = flowService.readComputedFile(chainTaskId);

        flowService.buildResultDigestInComputedFile(computedFile, shouldCallback);
        flowService.signComputedFile(computedFile);

        if (!shouldCallback) {
            web2ResultService.encryptAndUploadResult(computedFile);
        }

        flowService.sendComputedFileToHost(computedFile);
    }
}
