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
package com.alibaba.citrus.service.requestcontext.session.store.cookie.impl;

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
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;
import com.alibaba.citrus.springext.util.SpringExtUtil;

public class CookieStoreDefinitionParser extends AbstractSingleBeanDefinitionParser<CookieStoreImpl> implements
        ContributionAware {
    private ConfigurationPoint encodersConfigurationPoint;

    public void setContribution(Contribution contrib) {
        encodersConfigurationPoint = SpringExtUtil.getSiblingConfigurationPoint(
                "services/request-contexts/session/encoders", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        attributesToProperties(element, builder, "maxLength", "maxCount", "checksum");

        ElementSelector cookieSelector = and(sameNs(element), name("cookie")); // request-contexts:cookie
        ElementSelector encodersSelector = and(sameNs(element), name("encoders")); // request-contexts:encoders

        for (Element subElement : subElements(element)) {
            if (cookieSelector.accept(subElement)) {
                attributesToProperties(subElement, builder);
            } else if (encodersSelector.accept(subElement)) {
                parseEncoders(subElement, parserContext, builder);
            }
        }
    }

    private void parseEncoders(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Object> encoders = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            encoders.add(parseConfigurationPointBean(subElement, encodersConfigurationPoint, parserContext, builder));
        }

        builder.addPropertyValue("encoders", encoders);
    }
}
