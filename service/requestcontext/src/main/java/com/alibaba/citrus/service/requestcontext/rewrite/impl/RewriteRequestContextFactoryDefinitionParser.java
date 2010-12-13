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
package com.alibaba.citrus.service.requestcontext.rewrite.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

public class RewriteRequestContextFactoryDefinitionParser extends
        AbstractSingleBeanDefinitionParser<RewriteRequestContextFactoryImpl> {
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Object> rules = createManagedList(element, parserContext);
        ElementSelector ruleSelector = and(sameNs(element), name("rule")); // request-contexts:rule

        for (Element subElement : subElements(element, ruleSelector)) {
            rules.add(parseRule(subElement, parserContext));
        }

        builder.addPropertyValue("rules", rules);
    }

    /**
     * 解析rule。
     */
    private BeanDefinition parseRule(Element ruleElement, ParserContext parserContext) {
        BeanDefinitionBuilder ruleBuilder = BeanDefinitionBuilder.genericBeanDefinition(RewriteRule.class);

        attributesToProperties(ruleElement, ruleBuilder, "pattern");

        ElementSelector conditionSelector = and(sameNs(ruleElement), name("condition")); // request-contexts:condition
        ElementSelector substitutionSelector = and(sameNs(ruleElement), name("substitution")); // request-contexts:substitution
        ElementSelector handlersSelector = and(sameNs(ruleElement), name("handlers")); // request-contexts:handlers

        List<Object> conditions = createManagedList(ruleElement, parserContext);

        BeanDefinition substitution = null;
        List<Object> handlers = null;

        for (Element subElement : subElements(ruleElement)) {
            if (conditionSelector.accept(subElement)) {
                conditions.add(parseCondition(subElement, parserContext));
            } else if (substitutionSelector.accept(subElement)) {
                substitution = parseSubstitution(subElement, parserContext);
            } else if (handlersSelector.accept(subElement)) {
                handlers = parseHandlers(subElement, parserContext, ruleBuilder.getRawBeanDefinition());
            }
        }

        ruleBuilder.addPropertyValue("conditions", conditions);

        if (substitution != null) {
            ruleBuilder.addPropertyValue("substitution", substitution);
        }

        if (handlers != null) {
            ruleBuilder.addPropertyValue("handlers", handlers);
        }

        return ruleBuilder.getBeanDefinition();
    }

    /**
     * 解析rule/condition。
     */
    private BeanDefinition parseCondition(Element conditionElement, ParserContext parserContext) {
        BeanDefinitionBuilder conditionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RewriteCondition.class);

        attributesToProperties(conditionElement, conditionBuilder, "test", "pattern", "flags");

        return conditionBuilder.getBeanDefinition();
    }

    /**
     * 解析rule/substitution。
     */
    private BeanDefinition parseSubstitution(Element substitutionElement, ParserContext parserContext) {
        BeanDefinitionBuilder substitutionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(RewriteSubstitution.class);

        attributesToProperties(substitutionElement, substitutionBuilder, "uri", "flags");

        List<Object> parameters = createManagedList(substitutionElement, parserContext);

        ElementSelector parameterSelector = and(sameNs(substitutionElement), name("parameter")); // request-contexts:parameter

        for (Element subElement : subElements(substitutionElement, parameterSelector)) {
            parameters.add(parseParameter(subElement, parserContext));
        }

        substitutionBuilder.addPropertyValue("parameters", parameters);

        return substitutionBuilder.getBeanDefinition();
    }

    /**
     * 解析rule/substitution/parameter。
     */
    private BeanDefinition parseParameter(Element parameterElement, ParserContext parserContext) {
        BeanDefinitionBuilder parameterBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(RewriteSubstitution.Parameter.class);

        attributesToProperties(parameterElement, parameterBuilder, "key", "value");

        List<Object> values = createManagedList(parameterElement, parserContext);

        ElementSelector valueSelector = and(sameNs(parameterElement), name("value")); // request-contexts:value

        for (Element subElement : subElements(parameterElement, valueSelector)) {
            values.add(trimToNull(subElement.getTextContent()));
        }

        if (!values.isEmpty()) {
            parameterBuilder.addPropertyValue("values", values);
        }

        return parameterBuilder.getBeanDefinition();
    }

    /**
     * 解析rule/handlers。
     */
    @SuppressWarnings("unchecked")
    private List<Object> parseHandlers(Element handlersElement, ParserContext parserContext,
                                       BeanDefinition containingBeanDefinition) {
        return parserContext.getDelegate().parseListElement(handlersElement, containingBeanDefinition);
    }
}
