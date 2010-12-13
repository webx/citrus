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
package com.alibaba.citrus.webx.config.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.configuration.support.AbstractConfigurationDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;
import com.alibaba.citrus.webx.config.impl.WebxConfigurationImpl.ComponentConfigImpl;
import com.alibaba.citrus.webx.config.impl.WebxConfigurationImpl.ComponentsConfigImpl;
import com.alibaba.citrus.webx.impl.WebxControllerImpl;

/**
 * 用来解析webx configuration的parser。
 * 
 * @author Michael Zhou
 */
public class WebxConfigurationDefinitionParser extends AbstractConfigurationDefinitionParser<WebxConfigurationImpl> {
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        BeanDefinitionBuilder components = BeanDefinitionBuilder.genericBeanDefinition(ComponentsConfigImpl.class);
        Element componentsElement = theOnlySubElement(element, and(sameNs(element), name("components")));

        if (componentsElement != null) {
            attributesToProperties(componentsElement, components, "autoDiscoverComponents",
                    "componentConfigurationLocationPattern", "defaultControllerClass", "defaultComponent");

            // root controller
            Element rootControllerElement = theOnlySubElement(componentsElement,
                    and(sameNs(element), name("rootController")));

            if (rootControllerElement != null) {
                components.addPropertyValue("rootController",
                        parseBean(rootControllerElement, parserContext, components.getRawBeanDefinition()));
            }

            // components
            Map<Object, Object> specifiedComponents = createManagedMap(element, parserContext);

            for (Element componentElement : subElements(componentsElement,
                    and(sameNs(componentsElement), name("component")))) {
                String name = assertNotNull(trimToNull(componentElement.getAttribute("name")), "no component name");

                BeanDefinitionBuilder componentBuilder = BeanDefinitionBuilder
                        .genericBeanDefinition(ComponentConfigImpl.class);

                attributesToProperties(componentElement, componentBuilder, "name", "path");

                // controller
                Element controllerElement = theOnlySubElement(componentElement,
                        and(sameNs(componentElement), name("controller")));
                Object controllerBD;

                if (controllerElement == null) {
                    // default controller
                    controllerBD = BeanDefinitionBuilder.genericBeanDefinition(WebxControllerImpl.class)
                            .getBeanDefinition();
                } else {
                    // specified controller
                    controllerBD = parseBean(controllerElement, parserContext, components.getRawBeanDefinition());
                }

                componentBuilder.addPropertyValue("controller", controllerBD);
                specifiedComponents.put(name, componentBuilder.getBeanDefinition());
            }

            components.addPropertyValue("components", specifiedComponents);
        }

        builder.addPropertyValue("componentsConfig", components.getBeanDefinition());
    }

    @Override
    protected ElementSelector getPropertyElementSelector() {
        return and(any(), not(name("components")));
    }

    @Override
    protected String getDefaultName() {
        return WebxConfigurationImpl.DEFAULT_NAME;
    }
}
