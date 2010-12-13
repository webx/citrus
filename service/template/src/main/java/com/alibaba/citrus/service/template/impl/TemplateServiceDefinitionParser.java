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
package com.alibaba.citrus.service.template.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

/**
 * ½âÎötemplate service¡£
 * 
 * @author Michael Zhou
 */
public class TemplateServiceDefinitionParser extends AbstractNamedBeanDefinitionParser<TemplateServiceImpl> implements
        ContributionAware {
    private ConfigurationPoint templateEnginesConfigurationPoint;

    public void setContribution(Contribution contrib) {
        this.templateEnginesConfigurationPoint = getSiblingConfigurationPoint("services/template/engines", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        parseBeanDefinitionAttributes(element, parserContext, builder);

        Map<Object, Object> engines = createManagedMap(element, parserContext);
        Map<Object, Object> mappings = createManagedMap(element, parserContext);

        ElementSelector engineSelector = ns(templateEnginesConfigurationPoint.getNamespaceUri());
        ElementSelector mappingSelector = and(sameNs(element), name("template-mapping"));

        for (Element subElement : subElements(element)) {
            // engine
            if (engineSelector.accept(subElement)) {
                BeanDefinitionHolder engine = parseConfigurationPointBean(subElement,
                        templateEnginesConfigurationPoint, parserContext, builder);
                engines.put(engine.getBeanName(), engine);
            }

            // mapping
            else if (mappingSelector.accept(subElement)) {
                String ext = normalizeExtension(subElement.getAttribute("extension"));
                String engineName = assertNotNull(trimToNull(subElement.getAttribute("engine")), "engine");

                assertNotNull(ext, "extension");
                assertTrue(!mappings.containsKey(ext), "duplicated extension: %s", ext);

                mappings.put(ext, engineName);
            }
        }

        builder.addPropertyValue("engines", engines);
        builder.addPropertyValue("engineNameMappings", mappings);

        attributesToProperties(element, builder, "defaultExtension", "searchExtensions", "searchLocalizedTemplates",
                "cacheEnabled");
    }

    @Override
    protected String getDefaultName() {
        return "templateService";
    }
}
