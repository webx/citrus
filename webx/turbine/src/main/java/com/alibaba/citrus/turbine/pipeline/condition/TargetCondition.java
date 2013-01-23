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

package com.alibaba.citrus.turbine.pipeline.condition;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.webx.WebxComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 根据target来判断。
 *
 * @author Michael Zhou
 */
public class TargetCondition extends PathCondition {
    @Autowired(required = false)
    private WebxComponent component;

    private boolean withComponentName;

    public void setWithComponentName(boolean withComponentName) {
        this.withComponentName = withComponentName;
    }

    @Override
    protected String getPath() {
        StringBuilder buf = new StringBuilder();

        if (withComponentName) {
            assertNotNull(component, "no component");
            buf.append("/").append(component.getName());
        }

        String target = getRunData().getTarget();

        if (!target.startsWith("/")) {
            buf.append("/");
        }

        buf.append(target);

        return buf.toString();
    }

    @Override
    protected void log(String patternString) {
        log.debug("Target matched pattern: {}", patternString);
    }

    public static class DefinitionParser extends AbstractPathConditionDefinitionParser<TargetCondition> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            super.doParse(element, parserContext, builder);
            attributesToProperties(element, builder, "withComponentName");
        }
    }
}
