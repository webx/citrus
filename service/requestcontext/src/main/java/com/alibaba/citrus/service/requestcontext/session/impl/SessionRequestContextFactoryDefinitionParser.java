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
package com.alibaba.citrus.service.requestcontext.session.impl;

import static com.alibaba.citrus.service.requestcontext.session.SessionConfig.StoreMappingsConfig.*;
import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.requestcontext.session.impl.SessionRequestContextFactoryImpl.AttributePattern;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

public class SessionRequestContextFactoryDefinitionParser extends
        AbstractSingleBeanDefinitionParser<SessionRequestContextFactoryImpl> implements ContributionAware {
    private ConfigurationPoint generatorsConfigurationPoint;
    private ConfigurationPoint storesConfigurationPoint;
    private ConfigurationPoint sessionModelEncodersConfigurationPoint;
    private ConfigurationPoint sessionInterceptorsConfigurationPoint;

    public void setContribution(Contribution contrib) {
        generatorsConfigurationPoint = getSiblingConfigurationPoint("services/request-contexts/session/idgens", contrib);
        storesConfigurationPoint = getSiblingConfigurationPoint("services/request-contexts/session/stores", contrib);
        sessionModelEncodersConfigurationPoint = getSiblingConfigurationPoint(
                "services/request-contexts/session/model-encoders", contrib);
        sessionInterceptorsConfigurationPoint = getSiblingConfigurationPoint(
                "services/request-contexts/session/interceptors", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        attributesToProperties(element, "config.", builder);

        ElementSelector idSelector = and(sameNs(element), name("id")); // request-contexts:id
        ElementSelector storesSelector = and(sameNs(element), name("stores")); // request-contexts:stores
        ElementSelector storeMappingsSelector = and(sameNs(element), name("store-mappings")); // request-contexts:store-mappings
        ElementSelector sessionModelEncodersSelector = and(sameNs(element), name("session-model-encoders")); // request-contexts:session-model-encoders
        ElementSelector sessionInterceptorsSelector = and(sameNs(element), name("interceptors")); // request-contexts:interceptors

        for (Element subElement : subElements(element)) {
            if (idSelector.accept(subElement)) {
                parseId(subElement, parserContext, builder);
            } else if (storesSelector.accept(subElement)) {
                parseStores(subElement, parserContext, builder);
            } else if (storeMappingsSelector.accept(subElement)) {
                parseStoreMappings(subElement, parserContext, builder);
            } else if (sessionModelEncodersSelector.accept(subElement)) {
                parseSessionModelEncoders(subElement, parserContext, builder);
            } else if (sessionInterceptorsSelector.accept(subElement)) {
                parseSessionInterceptors(subElement, parserContext, builder);
            }
        }
    }

    private void parseId(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        attributesToProperties(element, "config.id.", builder, "cookieEnabled", "urlEncodeEnabled");

        ElementSelector cookieSelector = and(sameNs(element), name("cookie")); // request-contexts:cookie
        ElementSelector urlEncodeSelector = and(sameNs(element), name("url-encode")); // request-contexts:url-encode
        boolean hasGenerator = false;

        for (Element subElement : subElements(element)) {
            if (cookieSelector.accept(subElement)) {
                attributesToProperties(subElement, "config.id.cookie.", builder);
            } else if (urlEncodeSelector.accept(subElement)) {
                attributesToProperties(subElement, "config.id.urlEncode.", builder);
            } else if (!hasGenerator) {
                Object generator = parseConfigurationPointBean(subElement, generatorsConfigurationPoint, parserContext,
                        builder);

                if (generator != null) {
                    builder.addPropertyValue("config.id.generator", generator);
                    hasGenerator = true;
                }
            }
        }
    }

    private void parseStores(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        Map<Object, Object> storeMap = createManagedMap(element, parserContext);

        for (Element subElement : subElements(element)) {
            BeanDefinitionHolder store = parseConfigurationPointBean(subElement, storesConfigurationPoint,
                    parserContext, builder);
            storeMap.put(store.getBeanName(), store);
        }

        builder.addPropertyValue("config.stores.stores", storeMap);
    }

    private void parseStoreMappings(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        ElementSelector matchSelector = and(sameNs(element), name("match")); // request-contexts:match
        ElementSelector matchRegexSelector = and(sameNs(element), name("matchRegex")); // request-contexts:matchRegex
        List<Object> patterns = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            if (matchSelector.accept(subElement)) {
                patterns.add(parseMatch(subElement, parserContext, false));
            } else if (matchRegexSelector.accept(subElement)) {
                patterns.add(parseMatch(subElement, parserContext, true));
            }
        }

        builder.addPropertyValue("config.storeMappings.patterns", patterns);
    }

    private BeanDefinition parseMatch(Element element, ParserContext parserContext, boolean regex) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(AttributePattern.class);
        String name = trimToNull(element.getAttribute("name"));
        String patternName = trimToNull(element.getAttribute("pattern"));
        String storeName = trimToNull(element.getAttribute("store"));

        if (regex) {
            builder.getRawBeanDefinition().setFactoryMethodName("getRegexPattern");
            builder.addConstructorArgValue(storeName);
            builder.addConstructorArgValue(patternName);
        } else {
            if (MATCHES_ALL_ATTRIBUTES.equals(name)) {
                builder.getRawBeanDefinition().setFactoryMethodName("getDefaultPattern");
                builder.addConstructorArgValue(storeName);
            } else {
                builder.getRawBeanDefinition().setFactoryMethodName("getExactPattern");
                builder.addConstructorArgValue(storeName);
                builder.addConstructorArgValue(name);
            }
        }

        return builder.getBeanDefinition();
    }

    private void parseSessionModelEncoders(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Object> encoders = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            encoders.add(parseConfigurationPointBean(subElement, sessionModelEncodersConfigurationPoint, parserContext,
                    builder));
        }

        builder.addPropertyValue("config.sessionModelEncoders", encoders);
    }

    private void parseSessionInterceptors(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Object> interceptors = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            interceptors.add(parseConfigurationPointBean(subElement, sessionInterceptorsConfigurationPoint,
                    parserContext, builder));
        }

        builder.addPropertyValue("config.sessionInterceptors", interceptors);
    }
}
