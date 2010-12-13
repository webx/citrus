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
package com.alibaba.citrus.springext.support.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.springext.support.resolver.XmlBeanDefinitionReaderProcessor;

/**
 * 从XML配置文件中装配的<code>BeanFactory</code>的基类，派生于
 * {@link DefaultListableBeanFactory} ，增加了如下特性：
 * <ul>
 * <li>支持<code>ConfigurationPoint</code>机制。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class XmlBeanFactory extends DefaultListableBeanFactory {
    private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

    public XmlBeanFactory(Resource resource) throws BeansException {
        this(resource, null);
    }

    public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
        super(parentBeanFactory);
        initBeanDefinitionReader(reader);
        reader.loadBeanDefinitions(resource);
        setAllowBeanDefinitionOverriding(false);
    }

    protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
        new XmlBeanDefinitionReaderProcessor(beanDefinitionReader).addConfigurationPointsSupport();
    }
}
