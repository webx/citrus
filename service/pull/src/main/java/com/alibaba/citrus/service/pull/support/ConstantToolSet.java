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
package com.alibaba.citrus.service.pull.support;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pull.ToolSetFactory;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

/**
 * 在模板中使用constant的tool。
 * <p>
 * 此类既可以以tool-set的方式使用，也可以作为普通的pull tool使用，例如
 * <code>$myconstant.MY_CONSTANT</code>。
 * </p>
 * 
 * @author Michael Zhou
 */
public class ConstantToolSet extends ConstantTool implements ToolSetFactory {
    public Iterable<String> getToolNames() {
        return constants.keySet();
    }

    public Object createTool(String name) {
        return get(name);
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<ConstantToolSet> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "constantClass");

            boolean exposed = true;
            String exposedValue = trimToNull(element.getAttribute("exposed"));

            if (exposedValue != null) {
                exposed = Boolean.parseBoolean(exposedValue);
            }

            if (!exposed) {
                builder.getRawBeanDefinition().setBeanClass(ConstantTool.class);
            }
        }
    }
}
