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
package com.alibaba.citrus.service.uribroker.impl.uri;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.uribroker.support.AbstractURIBrokerDefinitionParser;
import com.alibaba.citrus.service.uribroker.uri.TurbineClassicURIBroker;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

public class TurbineClassicURIBrokerDefinitionParser extends AbstractURIBrokerDefinitionParser<TurbineClassicURIBroker> {
    @Override
    protected ElementSelector getSimplePropertiesSelector() {
        return or(name("contextPath"), name("servletPath"), name("page"), name("screen"), name("action"));
    }

    @Override
    protected void doParseElement(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParseElement(element, parserContext, builder);

        ElementSelector pathInfoSelector = and(sameNs(element), name("pathInfo"));
        Map<Object, Object> pathInfos = createManagedMap(element, parserContext);

        for (Element subElement : subElements(element, pathInfoSelector)) {
            String id = subElement.getAttribute("key");
            String value = subElement.getTextContent();
            pathInfos.put(id, value);
        }

        builder.addPropertyValue("pathInfoParams", pathInfos);
    }
}
