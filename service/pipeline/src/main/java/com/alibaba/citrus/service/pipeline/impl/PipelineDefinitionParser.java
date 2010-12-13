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
package com.alibaba.citrus.service.pipeline.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;

import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractNamedProxyBeanDefinitionParser;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

public class PipelineDefinitionParser extends AbstractNamedProxyBeanDefinitionParser implements ContributionAware {
    private ConfigurationPoint valvesConfigurationPoint;

    public void setContribution(Contribution contrib) {
        valvesConfigurationPoint = getSiblingConfigurationPoint("services/pipeline/valves", contrib);
    }

    @Override
    protected String getDefaultName() {
        return "pipeline";
    }

    @Override
    protected Class<?> getProxyInterface(Element element) {
        return Pipeline.class;
    }

    @Override
    protected AbstractBeanDefinitionParser getRealObjectParser() {
        return new AbstractSingleBeanDefinitionParser<PipelineImpl>() {
            @Override
            protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
                parseBeanDefinitionAttributes(element, parserContext, builder);

                attributesToProperties(element, builder, "label");

                List<Object> valves = createManagedList(element, parserContext);

                for (Element subElement : subElements(element)) {
                    Object valve = parseConfigurationPointBean(subElement, valvesConfigurationPoint, parserContext,
                            builder);

                    if (valve != null) {
                        valves.add(valve);
                    }
                }

                builder.addPropertyValue("valves", valves);
            }
        };
    }
}
