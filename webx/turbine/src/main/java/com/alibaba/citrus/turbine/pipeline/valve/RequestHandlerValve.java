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
package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.turbine.util.TurbineUtil.*;
import static com.alibaba.citrus.util.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.web.HttpRequestHandler;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.turbine.TurbineRunData;

/**
 * µ÷ÓÃSpring MVCµÄ<code>HttpRequestHandler</code>¡£
 * 
 * @author yuming.wangym
 * @author dux.fangl
 * @author Michael Zhou
 */
public class RequestHandlerValve extends AbstractValve {
    @Autowired
    private HttpServletRequest request;

    private final HttpRequestHandler handler;

    public RequestHandlerValve(HttpRequestHandler handler) {
        this.handler = assertNotNull(handler, "handler");
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunData rundata = getTurbineRunData(request);
        handler.handleRequest(rundata.getRequest(), rundata.getResponse());
        pipelineContext.invokeNext();
    }

    public static class DefinitonParser extends AbstractValveDefinitionParser<RequestHandlerValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            // arg 1: handler
            Object handler = parseBean(element, parserContext, builder);
            builder.addConstructorArgValue(handler);
        }
    }
}
