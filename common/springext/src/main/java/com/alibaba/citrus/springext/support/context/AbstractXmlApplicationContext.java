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

import static org.springframework.context.annotation.AnnotationConfigUtils.*;

import java.io.IOException;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.alibaba.citrus.springext.ResourceLoadingExtendable;
import com.alibaba.citrus.springext.ResourceLoadingExtender;
import com.alibaba.citrus.springext.support.resolver.XmlBeanDefinitionReaderProcessor;

/**
 * 从XML配置文件中装配的<code>ApplicationContext</code>的基类，派生于
 * {@link org.springframework.context.support.AbstractXmlApplicationContext}
 * ，增加了如下特性：
 * <ul>
 * <li>支持<code>ConfigurationPoint</code>机制。</li>
 * <li>扩展的resource loading机制。假如<code>ResourceLoadingExtender</code>
 * 被设置，则使用它来装载资源，否则使用默认的装载器。</li>
 * <li>默认打开annotation config，相当于<code>&lt;context:annotation-config/&gt;</code>。
 * </li>
 * <li>假如<code>parentResolvableDependenciesAccessible==true</code>，则支持从parent
 * context中取得预先置入<code>resolvableDependencies</code>中的对象。默认为<code>true</code>。</li>
 * <li>默认<code>allowBeanDefinitionOverriding==false</code>。</li>
 * </ul>
 * <p>
 * 建议所有非WEB应用的application context从该基类派生；对于简单的情形，如单元测试，直接从子类
 * {@link XmlApplicationContext}创建实例。
 * </p>
 * 
 * @author Michael Zhou
 */
public abstract class AbstractXmlApplicationContext extends
        org.springframework.context.support.AbstractXmlApplicationContext implements ResourceLoadingExtendable {
    private ResourceLoadingExtender resourceLoadingExtender;
    private boolean parentResolvableDependenciesAccessible = true;

    public AbstractXmlApplicationContext() {
        super();
        setAllowBeanDefinitionOverriding(false);
    }

    public AbstractXmlApplicationContext(ApplicationContext parent) {
        super(parent);
        setAllowBeanDefinitionOverriding(false);
    }

    /**
     * 是否可访问到parent context中的resolvableDependencies。 默认是可访问。
     */
    public boolean isParentResolvableDependenciesAccessible() {
        return parentResolvableDependenciesAccessible;
    }

    public void setParentResolvableDependenciesAccessible(boolean parentResolvableDependenciesAccessible) {
        this.parentResolvableDependenciesAccessible = parentResolvableDependenciesAccessible;
    }

    public void setResourceLoadingExtender(ResourceLoadingExtender resourceLoadingExtender) {
        if (this.resourceLoadingExtender != null) {
            getApplicationListeners().remove(this.resourceLoadingExtender);
        }

        this.resourceLoadingExtender = resourceLoadingExtender;

        if (resourceLoadingExtender instanceof ApplicationListener) {
            addApplicationListener((ApplicationListener) resourceLoadingExtender);
        }
    }

    @Override
    protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
        new XmlBeanDefinitionReaderProcessor(beanDefinitionReader).addConfigurationPointsSupport();
    }

    /**
     * 打开annotation注入。
     */
    @Override
    protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
        super.customizeBeanFactory(beanFactory);
        registerAnnotationConfigProcessors(beanFactory, null);
    }

    @Override
    protected DefaultListableBeanFactory createBeanFactory() {
        if (isParentResolvableDependenciesAccessible()) {
            return new InheritableListableBeanFactory(getInternalParentBeanFactory());
        } else {
            return super.createBeanFactory();
        }
    }

    /**
     * 扩展<code>ResourceLoader</code>机制，实现自定义的资源装载。
     */
    @Override
    protected Resource getResourceByPath(String path) {
        Resource resource = null;

        if (resourceLoadingExtender != null) {
            resource = resourceLoadingExtender.getResourceByPath(path);
        }

        if (resource == null) {
            resource = super.getResourceByPath(path);
        }

        return resource;
    }

    /**
     * 扩展<code>ResourcePatternResolver</code>机制，实现自定义的资源装载。
     */
    @Override
    protected ResourcePatternResolver getResourcePatternResolver() {
        final ResourcePatternResolver defaultResolver = super.getResourcePatternResolver();

        return new ResourcePatternResolver() {
            public Resource[] getResources(String locationPattern) throws IOException {
                ResourcePatternResolver resolver = null;

                if (resourceLoadingExtender != null) {
                    resolver = resourceLoadingExtender.getResourcePatternResolver();
                }

                if (resolver == null) {
                    resolver = defaultResolver;
                }

                return resolver.getResources(locationPattern);
            }

            public ClassLoader getClassLoader() {
                return defaultResolver.getClassLoader();
            }

            public Resource getResource(String location) {
                return defaultResolver.getResource(location);
            }
        };
    }
}
