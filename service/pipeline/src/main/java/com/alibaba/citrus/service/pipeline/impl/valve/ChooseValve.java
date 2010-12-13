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
import static com.alibaba.citrus.util.Assert.*;

import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.Condition;
import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 代表一个多重条件选择。
 * 
 * @author Michael Zhou
 */
public class ChooseValve extends AbstractValve {
    private Condition[] whenConditions;
    private Pipeline[] whenBlocks;
    private Pipeline otherwiseBlock;

    public Condition[] getWhenConditions() {
        return whenConditions;
    }

    public void setWhenConditions(Condition[] whenConditions) {
        this.whenConditions = whenConditions;
    }

    public Pipeline[] getWhenBlocks() {
        return whenBlocks;
    }

    public void setWhenBlocks(Pipeline[] whenBlocks) {
        this.whenBlocks = whenBlocks;
    }

    public Pipeline getOtherwiseBlock() {
        return otherwiseBlock;
    }

    public void setOtherwiseBlock(Pipeline otherwiseBlock) {
        this.otherwiseBlock = otherwiseBlock;
    }

    @Override
    protected void init() throws Exception {
        if (whenConditions == null) {
            whenConditions = new Condition[0];
        }

        if (whenBlocks == null) {
            whenBlocks = new Pipeline[0];
        }

        assertTrue(whenConditions.length == whenBlocks.length,
                "conditions and blocks not match: %d conditions and %d blocks", whenConditions.length,
                whenBlocks.length);

        for (int i = 0; i < whenConditions.length; i++) {
            assertNotNull(whenConditions[i], "when[%d].condition == null", i);
            assertNotNull(whenBlocks[i], "when[%d] == null", i);
        }
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        assertInitialized();
        boolean satisfied = false;

        for (int i = 0; i < whenConditions.length; i++) {
            if (whenConditions[i].isSatisfied(pipelineContext)) {
                satisfied = true;
                whenBlocks[i].newInvocation(pipelineContext).invoke();
                break;
            }
        }

        if (!satisfied && otherwiseBlock != null) {
            otherwiseBlock.newInvocation(pipelineContext).invoke();
        }

        pipelineContext.invokeNext();
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("whenConditions", whenConditions);
        mb.append("whenBlocks", whenBlocks);
        mb.append("otherwiseBlock", otherwiseBlock);

        return new ToStringBuilder().append("ChooseValve").append(mb).toString();
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<ChooseValve> {
        @Override
        protected final void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            List<Object> whenConditions = createManagedList(element, parserContext);
            List<Object> whenBlocks = createManagedList(element, parserContext);

            ElementSelector whenSelector = and(sameNs(element), name("when"));
            ElementSelector otherwiseSelector = and(sameNs(element), name("otherwise"));

            for (Element subElement : subElements(element)) {
                if (whenSelector.accept(subElement)) {
                    whenConditions.add(parseCondition(subElement, parserContext, builder));
                    whenBlocks.add(parsePipeline(subElement, element, parserContext));
                } else if (otherwiseSelector.accept(subElement)) {
                    builder.addPropertyValue("otherwiseBlock", parsePipeline(subElement, element, parserContext));
                }
            }

            builder.addPropertyValue("whenConditions", whenConditions);
            builder.addPropertyValue("whenBlocks", whenBlocks);
        }
    }
}
