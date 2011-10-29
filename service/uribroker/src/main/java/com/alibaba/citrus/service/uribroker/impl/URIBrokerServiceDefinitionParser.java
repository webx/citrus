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
 */

package com.alibaba.citrus.service.uribroker.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.uribroker.impl.URIBrokerServiceImpl.URIBrokerInfo;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

public class URIBrokerServiceDefinitionParser extends AbstractNamedBeanDefinitionParser<URIBrokerServiceImpl> implements
        ContributionAware {
    private ConfigurationPoint uriBrokersConfigurationPoint;

    public void setContribution(Contribution contrib) {
        uriBrokersConfigurationPoint = getSiblingConfigurationPoint("services/uris", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        parseBeanDefinitionAttributes(element, parserContext, builder);

        // injecting HttpServletRequest in constructor, required=false
        addConstructorArg(builder, false, HttpServletRequest.class);

        attributesToProperties(element, builder, "requestAware", "defaultCharset");

        // import uris
        ElementSelector importSelector = and(sameNs(element), name("import"));
        List<Object> imports = createManagedList(element, parserContext);

        for (Element subElement : subElements(element, importSelector)) {
            String urisRef = assertNotNull(trimToNull(subElement.getAttribute("uris")), "import uris is empty");
            imports.add(new RuntimeBeanReference(urisRef));
        }

        builder.addPropertyValue("imports", imports);

        // 解析URI Broker集合
        List<Object> brokers = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            Object broker = parseConfigurationPointBean(subElement, uriBrokersConfigurationPoint, parserContext,
                    builder);

            if (broker != null) {
                BeanDefinitionBuilder infoBuilder = BeanDefinitionBuilder.genericBeanDefinition(URIBrokerInfo.class);

                infoBuilder.addConstructorArgValue(subElement.getAttribute("id"));
                infoBuilder.addConstructorArgValue(subElement.getAttribute("extends"));
                infoBuilder.addConstructorArgValue(trimToNull(subElement.getAttribute("exposed")));
                infoBuilder.addConstructorArgValue(broker);

                brokers.add(infoBuilder.getBeanDefinition());
            }
        }

        builder.addPropertyValue("brokers", brokers);
    }

    @Override
    protected String getDefaultName() {
        return "uriBrokerService, uris";
    }
}
