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
package com.alibaba.citrus.springext.contrib;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;

public class ContainerServiceBeanDefinitionParser extends AbstractNamedBeanDefinitionParser<TreeMap<?, ?>> implements
        ContributionAware {
    private ConfigurationPoint toolsConfigurationPoint;

    public void setContribution(Contribution contrib) {
        this.toolsConfigurationPoint = getSiblingConfigurationPoint("services/tools", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        parseBeanDefinitionAttributes(element, parserContext, builder);

        Map<Object, Object> map = createManagedMap(element, parserContext);

        for (Element subElement : subElements(element)) {
            BeanDefinitionHolder bean = parseConfigurationPointBean(subElement, toolsConfigurationPoint, parserContext,
                    builder);
            map.put(bean.getBeanName(), bean);
        }

        builder.addConstructorArgValue(map);
    }

    @Override
    protected String getDefaultName() {
        return "container";
    }
}
