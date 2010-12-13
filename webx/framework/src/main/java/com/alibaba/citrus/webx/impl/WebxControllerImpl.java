/*
 * Copyright 2010 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.alibaba.citrus.webx.impl;

import org.springframework.beans.BeansException;

import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.pipeline.PipelineInvocationHandle;
import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.webx.support.AbstractWebxController;

public class WebxControllerImpl extends AbstractWebxController {
    private Pipeline pipeline;

    @Override
    public void onRefreshContext() throws BeansException {
        super.onRefreshContext();
        initPipeline();
    }

    private void initPipeline() {
        pipeline = getWebxConfiguration().getPipeline();
        log.debug("Using Pipeline: {}", pipeline);
    }

    public boolean service(RequestContext requestContext) throws Exception {
        PipelineInvocationHandle handle = pipeline.newInvocation();

        handle.invoke();

        // 假如pipeline被中断，则视作请求未被处理。filter将转入chain中继续处理请求。
        return !handle.isBroken();
    }
}
