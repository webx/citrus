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

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.PipelineInvocationHandle;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 支持try-catch-finally结构。
 * 
 * @author Michael Zhou
 */
public class TryCatchFinallyValve extends AbstractValve {
    private final static String DEFAULT_EXCEPTION_NAME = "exception";
    private Pipeline tryPipeline;
    private Pipeline catchPipeline;
    private Pipeline finallyPipeline;
    private String exceptionName;

    public Pipeline getTry() {
        return tryPipeline;
    }

    public void setTry(Pipeline tryPipeline) {
        this.tryPipeline = tryPipeline;
    }

    public Pipeline getCatch() {
        return catchPipeline;
    }

    public void setCatch(Pipeline catchPipeline) {
        this.catchPipeline = catchPipeline;
    }

    public String getExceptionName() {
        return defaultIfNull(exceptionName, DEFAULT_EXCEPTION_NAME);
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = trimToNull(exceptionName);
    }

    public Pipeline getFinally() {
        return finallyPipeline;
    }

    public void setFinally(Pipeline finallyPipeline) {
        this.finallyPipeline = finallyPipeline;
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        try {
            if (tryPipeline != null) {
                tryPipeline.newInvocation(pipelineContext).invoke();
            }
        } catch (Exception e) {
            if (catchPipeline != null) {
                PipelineInvocationHandle handle = catchPipeline.newInvocation(pipelineContext);
                handle.setAttribute(getExceptionName(), e);
                handle.invoke();
            } else {
                throw e;
            }
        } finally {
            if (finallyPipeline != null) {
                finallyPipeline.newInvocation(pipelineContext).invoke();
            }
        }

        pipelineContext.invokeNext();
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        if (tryPipeline != null) {
            mb.append("try", tryPipeline);
        }

        if (catchPipeline != null) {
            mb.append("catch", catchPipeline);
        }

        if (finallyPipeline != null) {
            mb.append("finally", finallyPipeline);
        }

        if (exceptionName != null) {
            mb.append("exceptionName", exceptionName);
        }

        return new ToStringBuilder().append("TryCatchFinally").append(mb).toString();
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<TryCatchFinallyValve> {
        @Override
        protected final void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            ElementSelector trySelector = and(sameNs(element), name("try"));
            ElementSelector catchSelector = and(sameNs(element), name("catch"));
            ElementSelector finallySelector = and(sameNs(element), name("finally"));

            for (Element subElement : subElements(element)) {
                if (trySelector.accept(subElement)) {
                    builder.addPropertyValue("try", parsePipeline(subElement, element, parserContext));
                } else if (catchSelector.accept(subElement)) {
                    builder.addPropertyValue("catch", parsePipeline(subElement, element, parserContext));
                    attributesToProperties(subElement, builder, "exceptionName");
                } else if (finallySelector.accept(subElement)) {
                    builder.addPropertyValue("finally", parsePipeline(subElement, element, parserContext));
                }
            }
        }
    }
}
