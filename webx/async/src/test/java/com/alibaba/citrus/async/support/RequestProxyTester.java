/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.async.support;

import static com.alibaba.citrus.test.TestUtil.*;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.service.requestcontext.util.RequestContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class RequestProxyTester implements Valve {
    private static final Logger log = LoggerFactory.getLogger(RequestProxyTester.class);

    @Autowired
    private HttpServletRequest request;

    public void invoke(PipelineContext pipelineContext) throws Exception {
        String key = getFieldValue(null, RequestContextUtil.class, "REQUEST_CONTEXT_KEY", String.class);

        // 如果RequestContextChainingService.bind未执行，这里会报错。
        log.info("requestContext: " + request.getAttribute(key));

        pipelineContext.invokeNext();
    }
}
