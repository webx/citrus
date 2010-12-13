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
package com.alibaba.citrus.service.configuration.support;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.configuration.Configuration;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

/**
 * 用来解析configuration的parser。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractConfigurationDefinitionParser<T> extends AbstractNamedBeanDefinitionParser<T> {
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        parseBeanDefinitionAttributes(element, parserContext, builder);

        String parentRef = trimToNull(element.getAttribute("parentRef"));

        if (parentRef != null) {
            builder.addConstructorArgValue(new RuntimeBeanReference(parentRef, true));
        }

        subElementsToProperties(element, builder, and(sameNs(element), getPropertyElementSelector()));

        registerProductionModeSensiblePostProcessorIfNecessary(parserContext.getRegistry());
    }

    protected ElementSelector getPropertyElementSelector() {
        return any();
    }

    private void registerProductionModeSensiblePostProcessorIfNecessary(BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition("productionModeSensiblePostProcessor")) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                    .genericBeanDefinition(ProductionModeAwarePostProcessor.class);

            // injecting Configuration in constructor, required=true
            addConstructorArg(builder, true, Configuration.class);

            registry.registerBeanDefinition("productionModeSensiblePostProcessor", builder.getBeanDefinition());
        }
    }
}
