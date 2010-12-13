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
package com.alibaba.citrus.service.velocity.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.velocity.support.EscapeSupport;
import com.alibaba.citrus.service.velocity.support.EscapeSupport.EscapeRule;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

public class EscapeSupportDefinitionParser extends AbstractSingleBeanDefinitionParser<EscapeSupport> {
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        attributesToProperties(element, builder, "defaultEscape");

        ElementSelector escapeSelector = and(sameNs(element), name("escape"));
        ElementSelector noescapeSelector = and(sameNs(element), name("noescape"));

        List<Object> rules = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            if (escapeSelector.accept(subElement)) {
                rules.add(parseEscape(subElement, parserContext, builder.getRawBeanDefinition(),
                        trimToNull(subElement.getAttribute("type"))));
            } else if (noescapeSelector.accept(subElement)) {
                rules.add(parseEscape(subElement, parserContext, builder.getRawBeanDefinition(), "noescape"));
            }
        }

        builder.addPropertyValue("escapeRules", rules);
    }

    private BeanDefinition parseEscape(Element element, ParserContext parserContext, BeanDefinition containingBD,
                                       String type) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(EscapeRule.class);

        // arg 1. escapeType
        builder.addConstructorArgValue(type);

        // arg 2. pattern names
        List<Object> patterns = createManagedList(element, parserContext);

        for (Element subElement : subElements(element, and(sameNs(element), name("if-matches")))) {
            patterns.add(subElement.getAttribute("pattern"));
        }

        builder.addConstructorArgValue(patterns);

        return builder.getBeanDefinition();
    }
}
