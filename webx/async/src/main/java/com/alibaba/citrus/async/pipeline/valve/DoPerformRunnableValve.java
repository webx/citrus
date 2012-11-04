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

package com.alibaba.citrus.async.pipeline.valve;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.turbine.pipeline.valve.AbstractInputOutputValve;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 调用pipeline result中的runnable或callable对象，并将其结果作为新的result，并保存在pipeline中。
 *
 * @author Michael Zhou
 */
public class DoPerformRunnableValve extends AbstractInputOutputValve {
    @Override
    protected String getDefaultInputKey() {
        return PerformRunnableAsyncValve.ASYNC_CALLBACK_KEY;
    }

    @Override
    protected boolean filterInputValue(Object inputValue) {
        assertTrue(inputValue instanceof AsyncCallbackAdapter, ExceptionType.ILLEGAL_STATE, "<doPerformRunnable> valve should be inside <performRunnableAsync>");
        return true;
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        AsyncCallbackAdapter callback = (AsyncCallbackAdapter) consumeInputValue(pipelineContext);

        setOutputValue(pipelineContext, callback.call());

        pipelineContext.invokeNext();
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<DoPerformRunnableValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "output");
        }
    }
}
