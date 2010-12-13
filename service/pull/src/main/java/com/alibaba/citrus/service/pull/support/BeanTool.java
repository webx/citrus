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
package com.alibaba.citrus.service.pull.support;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;
import net.sf.cglib.reflect.FastClass;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pull.ToolFactory;
import com.alibaba.citrus.springext.support.BeanSupport;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

/**
 * 创建一个简单bean的tool factory。
 * 
 * @author Michael Zhou
 */
public class BeanTool extends BeanSupport implements ToolFactory, ApplicationContextAware {
    protected final static boolean DEFAULT_SINGLETON = true;
    private ApplicationContext beanFactory;
    private boolean singleton = DEFAULT_SINGLETON;
    private Class<?> beanClass;
    private boolean autowire;
    private FastClass fc;

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public void setBeanFactory(ApplicationContext beanFactory) {
        this.beanFactory = beanFactory;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public boolean isAutowire() {
        return autowire;
    }

    public void setAutowire(boolean autowire) {
        this.autowire = autowire;
    }

    public ApplicationContext getBeanFactory() {
        return beanFactory;
    }

    public void setApplicationContext(ApplicationContext beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    protected void init() throws Exception {
        assertNotNull(beanFactory, "no beanFactory specified");
        assertNotNull(beanClass, "no tool class specified");

        fc = FastClass.create(beanClass);
    }

    public Object createTool() throws Exception {
        Object tool = fc.newInstance();

        AutowireCapableBeanFactory factory = beanFactory.getAutowireCapableBeanFactory();

        if (autowire) {
            factory.autowireBeanProperties(tool, AbstractBeanDefinition.AUTOWIRE_NO, false);
        }

        factory.initializeBean(tool, getBeanName());

        return tool;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + beanClass + "]";
    }

    /**
     * 子类可以调用该方法来解析参数。
     * <ul>
     * <li>参数：<code>scope="request|global"</code>，默认为<code>global</code>。</li>
     * </ul>
     */
    protected static final boolean parseBeanToolScope(Element element, ParserContext parserContext,
                                                      BeanDefinitionBuilder builder) {
        String scopeValue = trimToNull(element.getAttribute("scope"));
        boolean singleton = DEFAULT_SINGLETON;

        if (scopeValue != null) {
            if ("request".equals(scopeValue)) {
                singleton = false;
            } else if ("global".equals(scopeValue)) {
                singleton = true;
            } else {
                throw new IllegalArgumentException("unknown scope: " + scopeValue);
            }

            builder.addPropertyValue("singleton", singleton);
        }

        return singleton;
    }

    /**
     * 子类可以调用该方法来解析参数。
     * <ul>
     * <li>参数：<code>class="..."</code>。</li>
     * </ul>
     */
    protected static final String parseBeanToolClass(Element element, ParserContext parserContext,
                                                     BeanDefinitionBuilder builder) {
        String beanClass = trimToNull(element.getAttribute("class"));

        if (beanClass != null) {
            builder.addPropertyValue("beanClass", beanClass);
        }

        return beanClass;
    }

    /**
     * 子类可以调用该方法来解析参数。
     * <ul>
     * <li>参数：<code>autowire="true|false"</code>，默认为<code>false</code>。</li>
     * </ul>
     */
    protected static final void parseBeanToolAutowire(Element element, ParserContext parserContext,
                                                      BeanDefinitionBuilder builder) {
        attributesToProperties(element, builder, "autowire");
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<BeanTool> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            parseBeanToolScope(element, parserContext, builder);
            parseBeanToolClass(element, parserContext, builder);
            parseBeanToolAutowire(element, parserContext, builder);
        }
    }
}
