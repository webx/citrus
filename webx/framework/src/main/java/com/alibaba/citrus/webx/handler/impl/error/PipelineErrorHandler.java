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
package com.alibaba.citrus.webx.handler.impl.error;

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.webx.handler.RequestHandler;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;

/**
 * 用pipeline来处理异常。
 * 
 * @author Michael Zhou
 */
public class PipelineErrorHandler implements RequestHandler {
    private final Pipeline exceptionPipeline;

    public PipelineErrorHandler(Pipeline pipeline) {
        this.exceptionPipeline = assertNotNull(pipeline, "pipeline");
    }

    public void handleRequest(RequestHandlerContext context) throws Exception {
        exceptionPipeline.newInvocation().invoke();
    }
}
