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
package com.alibaba.citrus.service.form.support;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

/**
 * Validator解析器的基类，用于设置message信息。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractValidatorDefinitionParser<V extends AbstractValidator> extends
        AbstractSingleBeanDefinitionParser<V> {
    @Override
    protected final void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        // parse id
        attributesToProperties(element, builder, "id");

        // parse message
        String message = trimToNull(element.getAttribute("message"));

        if (message == null) {
            message = trimToNull(element.getTextContent());
        }

        if (message != null) {
            builder.addPropertyValue("message", message);
        }

        // attributes
        doParseAttributes(element, parserContext, builder);

        // element
        doParseElement(element, parserContext, builder);
    }

    protected void doParseAttributes(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        attributesToProperties(element, builder);
    }

    protected void doParseElement(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    }
}
