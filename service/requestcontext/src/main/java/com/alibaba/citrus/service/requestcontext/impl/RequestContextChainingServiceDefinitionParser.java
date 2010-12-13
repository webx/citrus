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
package com.alibaba.citrus.service.requestcontext.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;

public class RequestContextChainingServiceDefinitionParser extends
        AbstractNamedBeanDefinitionParser<RequestContextChainingServiceImpl> implements ContributionAware {
    private ConfigurationPoint requestContextsConfigurationPoint;

    public void setContribution(Contribution contrib) {
        this.requestContextsConfigurationPoint = getSiblingConfigurationPoint("services/request-contexts", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        parseBeanDefinitionAttributes(element, parserContext, builder);

        builder.addPropertyValue("sort", defaultIfNull(element.getAttribute("sort"), "true"));

        List<Object> factoryList = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            factoryList.add(parseConfigurationPointBean(subElement, requestContextsConfigurationPoint, parserContext,
                    builder));
        }

        builder.addPropertyValue("factories", factoryList);
    }

    @Override
    protected void registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
        super.registerBeanDefinition(definition, registry);

        // 如果request contexts被注册（顶级bean），则同时注册BeanFactoryPostProcessor
        if (RequestContextChainingServiceImpl.class.getName().equals(definition.getBeanDefinition().getBeanClassName())
                && !definition.getBeanDefinition().isLazyInit()) {
            registerBeanFactoryPostProcessor(definition.getBeanName(), registry);
        }
    }

    /**
     * 创建BeanFactoryPostProcessor。
     */
    private void registerBeanFactoryPostProcessor(String requestContextsName, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(RequestContextBeanFactoryPostProcessor.class);

        builder.addConstructorArgValue(requestContextsName);

        BeanDefinition definition = builder.getBeanDefinition();
        String name = BeanDefinitionReaderUtils.generateBeanName(definition, registry);

        super.registerBeanDefinition(new BeanDefinitionHolder(definition, name), registry);
    }

    @Override
    protected String getDefaultName() {
        return "requestContexts, requestContextChainingService";
    }
}
