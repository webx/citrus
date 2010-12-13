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
package com.alibaba.citrus.service.requestcontext.basic.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;

import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

public class BasicRequestContextFactoryDefinitionParser extends
        AbstractSingleBeanDefinitionParser<BasicRequestContextFactoryImpl> implements ContributionAware {
    private ConfigurationPoint interceptorsConfigurationPoint;

    public void setContribution(Contribution contrib) {
        this.interceptorsConfigurationPoint = getSiblingConfigurationPoint(
                "services/request-contexts/basic/interceptors", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        Element interceptors = theOnlySubElement(element, and(sameNs(element), name("interceptors")));

        if (interceptors != null) {
            doParseInterceptors(interceptors, parserContext, builder);
        }
    }

    private void doParseInterceptors(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Object> interceptors = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            interceptors.add(parseConfigurationPointBean(subElement, interceptorsConfigurationPoint, parserContext,
                    builder));
        }

        builder.addPropertyValue("interceptors", interceptors);
    }
}
