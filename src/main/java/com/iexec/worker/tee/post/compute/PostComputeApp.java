/*
 * Copyright 2022 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.tee.post.compute;

import com.iexec.common.result.ComputedFile;
import com.iexec.worker.tee.post.compute.utils.EnvUtils;
import com.iexec.worker.tee.post.compute.web2.Web2ResultService;
import com.iexec.worker.tee.post.compute.worflow.FlowService;

import static com.iexec.common.tee.TeeUtils.booleanFromYesNo;
import static com.iexec.common.worker.result.ResultUtils.RESULT_STORAGE_CALLBACK;

public class PostComputeApp {
    private final String chainTaskId;

    public PostComputeApp(String chainTaskId) {
        this.chainTaskId = chainTaskId;
    }

    public void runPostCompute() throws PostComputeException {
        boolean shouldCallback = booleanFromYesNo(EnvUtils.getEnvVar(RESULT_STORAGE_CALLBACK));

        final FlowService flowService = createFlowService();

        ComputedFile computedFile = flowService.readComputedFile(chainTaskId);

        flowService.buildResultDigestInComputedFile(computedFile, shouldCallback);

        if (!shouldCallback) {
            final Web2ResultService web2ResultService = createWeb2ResultService();
            web2ResultService.encryptAndUploadResult(chainTaskId);
        }

        flowService.signComputedFile(computedFile);
        flowService.sendComputedFileToHost(computedFile);
    }

    FlowService createFlowService() {
        return new FlowService();
    }

    Web2ResultService createWeb2ResultService() {
        return new Web2ResultService();
    }
}
