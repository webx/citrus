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
package com.alibaba.citrus.springext.support.resolver;

import static com.alibaba.citrus.util.Assert.*;

import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.EntityResolver;

import com.alibaba.citrus.springext.impl.ConfigurationPointsImpl;

/**
 * 用来处理<code>XmlBeanDefinitionReader</code>，添加configuration point的功能。
 * 
 * @author Michael Zhou
 */
public class XmlBeanDefinitionReaderProcessor {
    private final XmlBeanDefinitionReader reader;

    public XmlBeanDefinitionReaderProcessor(XmlBeanDefinitionReader reader) {
        this.reader = assertNotNull(reader, "XmlBeanDefinitionReader");
    }

    public void addConfigurationPointsSupport() {
        ResourceLoader resourceLoader = reader.getResourceLoader();

        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader();
        }

        ClassLoader classLoader = resourceLoader.getClassLoader();

        // schema providers
        ConfigurationPointsImpl cps = new ConfigurationPointsImpl(classLoader);
        SpringPluggableSchemas sps = new SpringPluggableSchemas(resourceLoader);

        // default resolvers
        EntityResolver defaultEntityResolver = new ResourceEntityResolver(resourceLoader);
        NamespaceHandlerResolver defaultNamespaceHanderResolver = new DefaultNamespaceHandlerResolver(classLoader);

        // new resolvers
        EntityResolver entityResolver = new SchemaEntityResolver(defaultEntityResolver, cps, sps);
        NamespaceHandlerResolver namespaceHandlerResolver = new ConfigurationPointNamespaceHandlerResolver(cps,
                defaultNamespaceHanderResolver);

        reader.setEntityResolver(entityResolver);
        reader.setNamespaceHandlerResolver(namespaceHandlerResolver);
    }
}
