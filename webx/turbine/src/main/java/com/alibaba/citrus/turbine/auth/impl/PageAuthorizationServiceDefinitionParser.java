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

package com.alibaba.citrus.turbine.auth.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Collection;
import java.util.List;

import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class PageAuthorizationServiceDefinitionParser extends
                                                      AbstractNamedBeanDefinitionParser<PageAuthorizationServiceImpl> {

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        // bean attributes
        parseBeanDefinitionAttributes(element, parserContext, builder);

        // default value
        String defaultValue = trimToNull(element.getAttribute("default"));

        if (defaultValue != null) {
            boolean allowByDefault = "allow".equals(defaultValue);
            builder.addPropertyValue("allowByDefault", allowByDefault);
        }

        // <match>
        ElementSelector matchSelector = and(sameNs(element), name("match"));
        List<Object> matches = createManagedList(element, parserContext);

        for (Element matchElement : subElements(element, matchSelector)) {
            matches.addAll(parseMatch(matchElement, parserContext));
        }

        builder.addPropertyValue("matches", matches);
    }

    private Collection<Object> parseMatch(Element element, ParserContext parserContext) {
        String targetStr = assertNotNull(trimToNull(element.getAttribute("target")), "match without target");
        String[] targets = split(targetStr, ", ");

        // <grant>
        ElementSelector grantSelector = and(sameNs(element), name("grant"));
        List<Object> grants = createManagedList(element, parserContext);

        for (Element grantElement : subElements(element, grantSelector)) {
            grants.add(parseGrant(grantElement, parserContext));
        }

        // create match object for each target
        List<Object> matches = createLinkedList();

        for (String target : targets) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(AuthMatch.class);

            builder.addConstructorArgValue(target);
            builder.addConstructorArgValue(grants);

            matches.add(builder.getBeanDefinition());
        }

        return matches;
    }

    private Object parseGrant(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(AuthGrant.class);

        // role, user
        String[] users = split(element.getAttribute("user"), ", ");
        String[] roles = split(element.getAttribute("role"), ", ");

        builder.addPropertyValue("users", users);
        builder.addPropertyValue("roles", roles);

        // allow, deny
        ElementSelector allowSelector = and(sameNs(element), name("allow"));
        ElementSelector denySelector = and(sameNs(element), name("deny"));
        List<Object> allows = createManagedList(element, parserContext);
        List<Object> denies = createManagedList(element, parserContext);

        for (Element subElement : subElements(element, or(allowSelector, denySelector))) {
            String action = trimToNull(subElement.getTextContent());

            if (allowSelector.accept(subElement)) {
                allows.add(action);
            } else {
                denies.add(action);
            }
        }

        builder.addPropertyValue("allow", allows);
        builder.addPropertyValue("deny", denies);

        return builder.getBeanDefinition();
    }

    @Override
    protected String getDefaultName() {
        return "pageAuthorizationService";
    }
}
