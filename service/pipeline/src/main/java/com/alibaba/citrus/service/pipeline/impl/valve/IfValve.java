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

import static com.alibaba.citrus.util.Assert.*;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.Condition;
import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 代表一个单重条件选择。
 * 
 * @author Michael Zhou
 */
public class IfValve extends AbstractValve {
    private Condition condition;
    private Pipeline block;

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Pipeline getBlock() {
        return block;
    }

    public void setBlock(Pipeline block) {
        this.block = block;
    }

    @Override
    protected void init() throws Exception {
        assertNotNull(condition, "no condition");
        assertNotNull(block, "no if-block");
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        if (condition.isSatisfied(pipelineContext)) {
            block.newInvocation(pipelineContext).invoke();
        }

        pipelineContext.invokeNext();
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("condition", condition);
        mb.append("block", block);

        return new ToStringBuilder().append("IfValve").append(mb).toString();
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<IfValve> {
        @Override
        protected final void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            Object condition = parseCondition(element, parserContext, builder);

            if (condition != null) {
                builder.addPropertyValue("condition", condition);
            }

            builder.addPropertyValue("block", parsePipeline(element, parserContext));
        }
    }
}
