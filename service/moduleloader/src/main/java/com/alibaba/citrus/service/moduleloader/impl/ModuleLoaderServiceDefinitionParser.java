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
package com.alibaba.citrus.service.moduleloader.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.moduleloader.impl.adapter.AbstractDataBindingAdapterFactoryDefinitionParser;
import com.alibaba.citrus.service.moduleloader.impl.adapter.ActionEventAdapterFactory;
import com.alibaba.citrus.service.moduleloader.impl.adapter.DataBindingAdapterFactory;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;

public class ModuleLoaderServiceDefinitionParser extends AbstractNamedBeanDefinitionParser<ModuleLoaderServiceImpl>
        implements ContributionAware {
    private ConfigurationPoint moduleFactoriesConfigurationPoint;
    private ConfigurationPoint moduleAdaptersConfigurationPoint;

    public void setContribution(Contribution contrib) {
        this.moduleFactoriesConfigurationPoint = getSiblingConfigurationPoint("services/module-loader/factories",
                contrib);
        this.moduleAdaptersConfigurationPoint = getSiblingConfigurationPoint("services/module-loader/adapters", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        parseBeanDefinitionAttributes(element, parserContext, builder);
        attributesToProperties(element, builder, "cacheEnabled");

        List<Object> factoryList = createManagedList(element, parserContext);
        List<Object> adapterList = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            BeanDefinitionHolder factory = parseConfigurationPointBean(subElement, moduleFactoriesConfigurationPoint,
                    parserContext, builder);

            if (factory != null) {
                factoryList.add(factory.getBeanDefinition());
            } else {
                BeanDefinitionHolder adapter = parseConfigurationPointBean(subElement,
                        moduleAdaptersConfigurationPoint, parserContext, builder);

                if (adapter != null) {
                    adapterList.add(adapter.getBeanDefinition());
                }
            }
        }

        // 假如includeDefaultAdapters=true或未指定，则加入默认的adapter。
        String includeDefaultAdaptersValue = trimToNull(element.getAttribute("includeDefaultAdapters"));
        boolean includeDefaultAdapters = true;

        if (includeDefaultAdaptersValue != null) {
            includeDefaultAdapters = Boolean.parseBoolean(includeDefaultAdaptersValue);
        }

        if (includeDefaultAdapters) {
            // default adapter: action event adapter
            addDefaultAdapter(adapterList, ActionEventAdapterFactory.class);

            // default adapter: data binding adapter
            addDefaultAdapter(adapterList, DataBindingAdapterFactory.class);
        }

        builder.addPropertyValue("factories", factoryList);
        builder.addPropertyValue("adapters", adapterList);
    }

    private void addDefaultAdapter(List<Object> adapterList, Class<?> beanClass) {
        boolean found = false;

        for (Object defObject : adapterList) {
            String beanName = ((BeanDefinition) defObject).getBeanClassName();

            if (beanClass.getName().equals(beanName)) {
                found = true;
                break;
            }
        }

        if (!found) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
            AbstractDataBindingAdapterFactoryDefinitionParser.initAdapterBeanDefinition(null, builder); // 初始化默认配置
            adapterList.add(builder.getBeanDefinition());
        }
    }

    @Override
    protected String getDefaultName() {
        return "moduleLoaderService";
    }
}
