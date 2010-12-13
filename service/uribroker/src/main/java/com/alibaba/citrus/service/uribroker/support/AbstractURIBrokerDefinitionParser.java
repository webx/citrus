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
package com.alibaba.citrus.service.uribroker.support;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.uribroker.uri.URIBroker;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

/**
 * <code>URIBroker</code>解析器的基类。
 * 
 * @author Michael Zhou
 */
public class AbstractURIBrokerDefinitionParser<U extends URIBroker> extends AbstractSingleBeanDefinitionParser<U>
        implements ContributionAware {
    private ConfigurationPoint uriBrokerInterceptorsConfigurationPoint;

    public void setContribution(Contribution contrib) {
        uriBrokerInterceptorsConfigurationPoint = getSiblingConfigurationPoint("services/uris/interceptors", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        // bean attributes，适合于在spring中直接创建uri broker
        parseBeanDefinitionAttributes(element, parserContext, builder);

        doParseAttributes(element, parserContext, builder);

        doParseElement(element, parserContext, builder);
    }

    protected void doParseAttributes(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        attributesToProperties(element, builder, "charset");
        builder.addPropertyValue("URIType", trimToNull(element.getAttribute("type")));
    }

    protected void doParseElement(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        ElementSelector simplePropertiesSelector = and(
                sameNs(element),
                or(name("serverURI"), name("serverScheme"), name("loginUser"), name("loginPassword"),
                        name("serverName"), name("serverPort"), name("reference"), getSimplePropertiesSelector()));

        ElementSelector querySelector = and(sameNs(element), name("query"));

        Map<Object, Object> queries = createManagedMap(element, parserContext);
        List<Object> interceptors = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            // simple properties
            if (simplePropertiesSelector.accept(subElement)) {
                String name = subElement.getLocalName();
                String value = defaultIfNull(subElement.getTextContent(), EMPTY_STRING);

                builder.addPropertyValue(name, value);
            }

            // query
            else if (querySelector.accept(subElement)) {
                String id = subElement.getAttribute("key");
                String value = subElement.getTextContent();
                queries.put(id, value);
            }

            // interceptors
            else {
                Object interceptor = parseConfigurationPointBean(subElement, uriBrokerInterceptorsConfigurationPoint,
                        parserContext, builder);

                if (interceptor != null) {
                    interceptors.add(interceptor);
                }
            }
        }

        if (!queries.isEmpty()) {
            builder.addPropertyValue("query", queries);
        }

        if (!interceptors.isEmpty()) {
            builder.addPropertyValue("interceptors", interceptors);
        }
    }

    protected ElementSelector getSimplePropertiesSelector() {
        return none();
    }
}
