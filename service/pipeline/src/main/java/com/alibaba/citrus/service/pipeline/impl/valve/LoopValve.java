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
package com.alibaba.citrus.service.pipeline.impl.valve;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.PipelineInvocationHandle;
import com.alibaba.citrus.service.pipeline.TooManyLoopsException;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 用来反复执行同一个子pipeline。
 * 
 * @author Michael Zhou
 */
public class LoopValve extends AbstractValve {
    private final static int DEFAULT_MAX_LOOP = 10;
    private final static String DEFAULT_LOOP_COUNTER_NAME = "loopCount";
    private Pipeline loopBody;
    private Integer maxLoopCount;
    private String loopCounterName;

    public Pipeline getLoopBody() {
        return loopBody;
    }

    public void setLoopBody(Pipeline loopBody) {
        this.loopBody = loopBody;
    }

    public int getMaxLoopCount() {
        return maxLoopCount == null ? DEFAULT_MAX_LOOP : maxLoopCount;
    }

    public void setMaxLoopCount(int maxLoopCount) {
        this.maxLoopCount = maxLoopCount <= 0 ? 0 : maxLoopCount;
    }

    public String getLoopCounterName() {
        return defaultIfNull(loopCounterName, DEFAULT_LOOP_COUNTER_NAME);
    }

    public void setLoopCounterName(String loopCounterName) {
        this.loopCounterName = trimToNull(loopCounterName);
    }

    @Override
    protected void init() throws Exception {
        assertNotNull(loopBody, "no loop body");
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        assertInitialized();

        PipelineInvocationHandle handle = initLoop(pipelineContext);

        do {
            invokeBody(handle);
        } while (!handle.isBroken());

        pipelineContext.invokeNext();
    }

    protected PipelineInvocationHandle initLoop(PipelineContext pipelineContext) {
        PipelineInvocationHandle handle = getLoopBody().newInvocation(pipelineContext);
        handle.setAttribute(getLoopCounterName(), 0);
        return handle;
    }

    protected void invokeBody(PipelineInvocationHandle handle) {
        String loopCounterName = getLoopCounterName();
        int loopCount = (Integer) handle.getAttribute(loopCounterName);
        int maxLoopCount = getMaxLoopCount();

        // maxLoopCount<=0，意味着没有循环次数的限制。
        if (maxLoopCount > 0 && loopCount >= maxLoopCount) {
            throw new TooManyLoopsException("Too many loops: exceeds the maximum count: " + maxLoopCount);
        }

        handle.invoke();
        handle.setAttribute(loopCounterName, ++loopCount);
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append("LoopValve").start("[", "]").append(loopBody).end().toString();
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<LoopValve> {
        @Override
        protected final void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "maxLoopCount", "loopCounterName");
            builder.addPropertyValue("loopBody", parsePipeline(element, parserContext));
        }
    }
}
