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
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;

/**
 * 除非条件满足，否则中断pipeline。
 * 
 * @author Michael Zhou
 */
public class BreakUnlessValve extends BreakValve {
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
        if (!condition.isSatisfied(pipelineContext)) {
            super.invoke(pipelineContext);
        }

        pipelineContext.invokeNext();
    }

    @Override
    public String toString() {
        return "BreakUnlessValve[" + parametersToString() + ", unless " + condition + "]";
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<BreakUnlessValve> {
        @Override
        protected final void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "levels", "toLabel");

            Object condition = parseCondition(element, parserContext, builder);

            if (condition != null) {
                builder.addPropertyValue("condition", condition);
            }
        }
    }
}
