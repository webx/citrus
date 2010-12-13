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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.Condition;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.PipelineInvocationHandle;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 当条件满足时，执行循环体。
 * 
 * @author Michael Zhou
 */
public class WhileLoopValve extends LoopValve {
    private Condition condition;

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    protected void init() throws Exception {
        super.init();
        assertNotNull(condition, "no condition");
    }

    @Override
    public void invoke(PipelineContext pipelineContext) throws Exception {
        assertInitialized();

        PipelineInvocationHandle handle = initLoop(pipelineContext);

        while (condition.isSatisfied(handle)) { // 注意：condition的上下文为handle而非当前pipelineContext
            invokeBody(handle);

            if (handle.isBroken()) {
                break;
            }
        }

        pipelineContext.invokeNext();
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("condition", condition);
        mb.append("loopBody", getLoopBody());

        return new ToStringBuilder().append("WhileLoopValve").append(mb).toString();
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<WhileLoopValve> {
        @Override
        protected final void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "maxLoopCount", "loopCounterName");
            builder.addPropertyValue("loopBody", parsePipeline(element, parserContext));

            Object condition = parseCondition(element, parserContext, builder);

            if (condition != null) {
                builder.addPropertyValue("condition", condition);
            }
        }
    }
}
