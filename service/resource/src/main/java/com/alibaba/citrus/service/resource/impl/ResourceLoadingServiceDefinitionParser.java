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
package com.alibaba.citrus.service.resource.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

/**
 * 用来解析resource-loading服务。
 * 
 * @author Michael Zhou
 */
public class ResourceLoadingServiceDefinitionParser extends
        AbstractNamedBeanDefinitionParser<ResourceLoadingServiceImpl> implements ContributionAware {
    private ConfigurationPoint loadersConfigurationPoint;
    private ConfigurationPoint filtersConfigurationPoint;

    public void setContribution(Contribution contrib) {
        this.loadersConfigurationPoint = getSiblingConfigurationPoint("services/resource-loading/loaders", contrib);
        this.filtersConfigurationPoint = getSiblingConfigurationPoint("services/resource-loading/filters", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        parseBeanDefinitionAttributes(element, parserContext, builder);

        URL configFileURL;

        try {
            configFileURL = parserContext.getReaderContext().getResource().getURL();
        } catch (IOException e) {
            configFileURL = null;
        }

        if (configFileURL != null) {
            builder.addPropertyValue("configLocation", configFileURL);
        }

        ElementSelector resourceSelector = and(sameNs(element), name("resource")); // services:resource
        ElementSelector resourceAliasSelector = and(sameNs(element), name("resource-alias")); // services:resource-alias
        ElementSelector resourceFiltersSelector = and(sameNs(element), name("resource-filters")); // services:resource-filters

        List<Object> resourceMappings = createManagedList(element, parserContext);
        List<Object> filterMappings = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            // <resource>
            if (resourceSelector.accept(subElement)) {
                resourceMappings.add(parseResource(subElement, parserContext));
            }

            // <resource-alias>
            else if (resourceAliasSelector.accept(subElement)) {
                resourceMappings.add(parseAlias(subElement, parserContext));
            }

            // <resource-filters>
            else if (resourceFiltersSelector.accept(subElement)) {
                filterMappings.add(parseFilters(subElement, parserContext));
            }
        }

        builder.addPropertyValue("resourceMappings", resourceMappings);
        builder.addPropertyValue("filterMappings", filterMappings);

        String parentRef = trimToNull(element.getAttribute("parentRef"));

        if (parentRef != null) {
            builder.addPropertyValue("parent", new RuntimeBeanReference(parentRef));
        }
    }

    private BeanDefinition parseResource(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder resourceMapping = BeanDefinitionBuilder
                .genericBeanDefinition(ResourceLoaderMapping.class);

        resourceMapping.addPropertyValue("patternName", trimToNull(element.getAttribute("pattern")));
        attributesToProperties(element, resourceMapping, "internal");

        List<Object> loaders = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            loaders.add(parseConfigurationPointBean(subElement, loadersConfigurationPoint, parserContext,
                    resourceMapping));
        }

        resourceMapping.addPropertyValue("loaders", loaders);

        return resourceMapping.getBeanDefinition();
    }

    private BeanDefinition parseAlias(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder resourceMapping = BeanDefinitionBuilder.genericBeanDefinition(ResourceAlias.class);

        resourceMapping.addPropertyValue("patternName", trimToNull(element.getAttribute("pattern")));
        attributesToProperties(element, resourceMapping, "internal", "name");

        return resourceMapping.getBeanDefinition();
    }

    private BeanDefinition parseFilters(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder resourceFilterMapping = BeanDefinitionBuilder
                .genericBeanDefinition(ResourceFilterMapping.class);

        resourceFilterMapping.addPropertyValue("patternName", trimToNull(element.getAttribute("pattern")));

        List<Object> filters = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            filters.add(parseConfigurationPointBean(subElement, filtersConfigurationPoint, parserContext,
                    resourceFilterMapping));
        }

        resourceFilterMapping.addPropertyValue("filters", filters);

        return resourceFilterMapping.getBeanDefinition();
    }

    @Override
    protected String getDefaultName() {
        return ResourceLoadingServiceImpl.DEFAULT_NAME;
    }
}
