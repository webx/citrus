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

package com.alibaba.citrus.springext.support.resolver;

import static com.alibaba.citrus.util.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;

import com.alibaba.citrus.springext.impl.ConfigurationPointsImpl;

/**
 * 用来处理<code>XmlBeanDefinitionReader</code>，添加configuration point的功能。
 * 
 * @author Michael Zhou
 */
public class XmlBeanDefinitionReaderProcessor {
    private final static String PROPERTY_SKIP_VALIDATION = "skipValidation";
    private final static Logger log = LoggerFactory.getLogger(XmlBeanDefinitionReaderProcessor.class);
    private final XmlBeanDefinitionReader reader;
    private final boolean skipValidation;

    public XmlBeanDefinitionReaderProcessor(XmlBeanDefinitionReader reader) {
        this(reader, Boolean.getBoolean(PROPERTY_SKIP_VALIDATION));
    }

    public XmlBeanDefinitionReaderProcessor(XmlBeanDefinitionReader reader, boolean skipValidation) {
        this.reader = assertNotNull(reader, "XmlBeanDefinitionReader");
        this.skipValidation = skipValidation;
    }

    public void addConfigurationPointsSupport() {
        if (skipValidation) {
            reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
            reader.setNamespaceAware(true); // 为了添加Configuration Point支持，namespace是必须打开的。
            reader.setDocumentReaderClass(DocumentReaderSkippingValidation.class); // 对beans中的参数提供默认值

            log.warn(
                    "XSD validation has been disabled according to the system property: -D{}.  Please be warned: NEVER skipping validation in Production Environment.",
                    PROPERTY_SKIP_VALIDATION);
        }

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

    /**
     * 在有schema验证的情况下，部分attribute的默认值是由schema提供的。当没有schema验证时，这些默认值便不复存在，
     * 这导致spring的行为在<code>-DskipValidation</code>
     * 时和原来不同。本类继承了spring的原有类，但对缺失的默认值提供明确的值。
     */
    public static class DocumentReaderSkippingValidation extends DefaultBeanDefinitionDocumentReader {
        @Override
        protected BeanDefinitionParserDelegate createHelper(XmlReaderContext readerContext, Element root) {
            BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegateSkippingValidation(readerContext);
            delegate.initDefaults(root);
            return delegate;
        }
    }

    private static class BeanDefinitionParserDelegateSkippingValidation extends BeanDefinitionParserDelegate {
        public BeanDefinitionParserDelegateSkippingValidation(XmlReaderContext readerContext) {
            super(readerContext);
        }

        @Override
        public AbstractBeanDefinition parseBeanDefinitionAttributes(Element ele, String beanName,
                                                                    BeanDefinition containingBean,
                                                                    AbstractBeanDefinition bd) {
            setDefaultValueForAttribute(ele, LAZY_INIT_ATTRIBUTE, DEFAULT_VALUE);
            setDefaultValueForAttribute(ele, AUTOWIRE_ATTRIBUTE, DEFAULT_VALUE);
            setDefaultValueForAttribute(ele, DEPENDENCY_CHECK_ATTRIBUTE, DEFAULT_VALUE);
            setDefaultValueForAttribute(ele, AUTOWIRE_CANDIDATE_ATTRIBUTE, DEFAULT_VALUE);

            return super.parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
        }

        @Override
        public void parseQualifierElement(Element ele, AbstractBeanDefinition bd) {
            setDefaultValueForAttribute(ele, TYPE_ATTRIBUTE, Qualifier.class.getName());
            super.parseQualifierElement(ele, bd);
        }

        private void setDefaultValueForAttribute(Element ele, String attrName, String defaultValue) {
            if (!ele.hasAttribute(attrName)) {
                ele.setAttribute(attrName, defaultValue);
            }
        }
    }
}
