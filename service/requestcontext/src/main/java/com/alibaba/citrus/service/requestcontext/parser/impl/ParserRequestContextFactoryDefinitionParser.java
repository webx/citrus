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
package com.alibaba.citrus.service.requestcontext.parser.impl;

import static com.alibaba.citrus.service.configuration.support.PropertyEditorRegistrarsSupport.*;
import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

public class ParserRequestContextFactoryDefinitionParser extends
        AbstractSingleBeanDefinitionParser<ParserRequestContextFactoryImpl> implements ContributionAware {
    private ConfigurationPoint parserFiltersConfigurationPoint;

    public void setContribution(Contribution contrib) {
        parserFiltersConfigurationPoint = getSiblingConfigurationPoint("services/request-contexts/parser/filters",
                contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        // property editor registrars
        builder.addPropertyValue("propertyEditorRegistrars", parseRegistrars(element, parserContext, builder));

        // filters
        Element filtersElement = theOnlySubElement(element, and(sameNs(element), name("filters")));

        if (filtersElement != null) {
            parseFilters(filtersElement, parserContext, builder);
        }

        // other settings
        attributesToProperties(element, builder, "converterQuiet", "caseFolding", "autoUpload", "unescapeParameters",
                "useServletEngineParser", "useBodyEncodingForURI", "URIEncoding", "trimming", "htmlFieldSuffix");

        // upload service
        String uploadServiceName = trimToNull(element.getAttribute("uploadServiceRef"));

        if (uploadServiceName != null) {
            builder.addPropertyReference("uploadService", uploadServiceName);
        }
    }

    private void parseFilters(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Object> filters = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            filters.add(parseConfigurationPointBean(subElement, parserFiltersConfigurationPoint, parserContext, builder));
        }

        builder.addPropertyValue("parameterParserFilters", filters);
    }
}
