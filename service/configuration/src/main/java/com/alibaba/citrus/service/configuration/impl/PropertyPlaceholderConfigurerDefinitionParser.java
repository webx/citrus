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
package com.alibaba.citrus.service.configuration.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.configuration.support.PropertyPlaceholderConfigurer;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

public class PropertyPlaceholderConfigurerDefinitionParser extends
        AbstractSingleBeanDefinitionParser<PropertyPlaceholderConfigurer> {
    @Override
    protected boolean shouldGenerateId() {
        return true;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        attributesToProperties(element, builder, "ignoreUnresolvablePlaceholders");

        // location
        String location = trimToNull(element.getAttribute("location"));

        if (location != null) {
            builder.addPropertyValue("locationNames", location);
        }

        // properties-ref
        List<Object> propsList = createManagedList(element, parserContext);
        String propertiesRef = trimToNull(element.getAttribute("properties-ref"));

        if (propertiesRef != null) {
            propsList.add(new RuntimeBeanReference(propertiesRef));
        }

        // specific properties
        Properties specificProps = parseProperties(element);

        if (specificProps != null) {
            propsList.add(specificProps);
        }

        builder.addPropertyValue("propertiesArray", propsList);

        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    }

    private Properties parseProperties(Element element) {
        Properties props = new Properties();

        for (Element propertyElement : subElements(element, and(sameNs(element), name("property")))) {
            String key = trimToEmpty(propertyElement.getAttribute("key"));
            String value = trimToNull(propertyElement.getTextContent());

            props.setProperty(key, value);
        }

        return props.isEmpty() ? null : props;
    }
}
