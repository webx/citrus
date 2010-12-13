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
package com.alibaba.citrus.turbine.uribroker.uri.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.mappingrule.MappingRuleService;
import com.alibaba.citrus.service.uribroker.support.AbstractURIBrokerDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;
import com.alibaba.citrus.turbine.uribroker.uri.TurbineURIBroker;

public class TurbineURIBrokerDefinitionParser extends AbstractURIBrokerDefinitionParser<TurbineURIBroker> {
    @Override
    protected ElementSelector getSimplePropertiesSelector() {
        return or(name("contextPath"), name("servletPath"), name("componentPath"), name("target"), name("action"));
    }

    @Override
    protected void doParseAttributes(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParseAttributes(element, parserContext, builder);

        attributesToProperties(element, builder, "targetMappingRule", "convertTargetCase", "actionParam");

        String mappingRuleServiceRef = trimToNull(element.getAttribute("mappingRuleServiceRef"));

        if (mappingRuleServiceRef != null) {
            // injecting specified mappings, required
            builder.addPropertyReference("mappingRuleService", mappingRuleServiceRef);
        } else {
            // injecting default mappings, optional
            addPropertyRef(builder, "mappingRuleService", "mappingRuleService", MappingRuleService.class, false);
        }
    }
}
