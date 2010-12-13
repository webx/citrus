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
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;

/**
 * 用来中断当前pipeline。
 * <p>
 * 和java语言中的break不同，java break只能中断并跳出循环体，而break valve则中断当前pipeline，无论是if
 * block还是循环体。
 * </p>
 * 
 * @author Michael Zhou
 */
public class BreakValve extends AbstractValve {
    private int levels;
    private String toLabel;

    public int getLevels() {
        return levels;
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }

    public String getToLabel() {
        return toLabel;
    }

    public void setToLabel(String toLabel) {
        this.toLabel = trimToNull(toLabel);
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        if (toLabel != null) {
            pipelineContext.breakPipeline(toLabel);
        } else {
            pipelineContext.breakPipeline(levels);
        }

        pipelineContext.invokeNext();
    }

    @Override
    public String toString() {
        return "BreakValve[" + parametersToString() + "]";
    }

    protected String parametersToString() {
        if (toLabel != null) {
            return "toLabel=" + toLabel;
        } else {
            return "levels=" + levels;
        }
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<BreakValve> {
        @Override
        protected final void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "levels", "toLabel");
        }
    }
}
