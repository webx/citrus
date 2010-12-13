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
package com.alibaba.citrus.springext.support.parser;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.springframework.beans.factory.config.BeanDefinition.*;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.support.parser.NamedBeanDefinitionParserMixin.DefaultNameBDParser;

/**
 * 用来创建proxy的parser。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractNamedProxyBeanDefinitionParser extends AbstractBeanDefinitionParser implements
        DefaultNameBDParser {
    private final NamedBeanDefinitionParserMixin mixin = new NamedBeanDefinitionParserMixin(this);

    /**
     * 取得bean的默认名称。
     * <p>
     * 可以注册多个默认名，以逗号或空格分开。第二名名称及其后的名称，将被注册成别名。
     * </p>
     */
    protected abstract String getDefaultName();

    /**
     * 从id attribute中取得bean name，假如未指定，则从<code>getDefaultName()</code>中取得默认名。
     */
    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
        return mixin.resolveId(element, definition, parserContext);
    }

    /**
     * 假如当前bean name为默认名，则同时注册默认的aliases。
     */
    @Override
    protected void registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
        mixin.registerBeanDefinition(definition, registry);
    }

    public final String internal_getDefaultName() {
        return getDefaultName();
    }

    public void super_registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
        super.registerBeanDefinition(definition, registry);
    }

    @Override
    protected final AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder proxyBuilder = BeanDefinitionBuilder.genericBeanDefinition(ProxyFactoryBean.class);
        AbstractBeanDefinitionParser realParser = getRealObjectParser();

        // 取得真实的bean，注意，由于设置了containingBean=proxyBean，这个bean不会被注册到registry
        ParserContext realBeanParserContext = new ParserContext(parserContext.getReaderContext(),
                parserContext.getDelegate(), proxyBuilder.getRawBeanDefinition());

        AbstractBeanDefinition realBd = (AbstractBeanDefinition) realParser.parse(element, realBeanParserContext);

        // 检查scope，对于singleton和prototype，不创建proxy，直接返回真实的bean，否则创建proxy bean
        String scope = trimToNull(getScope(element, realBd));

        if (scope == null || scope.equalsIgnoreCase(SCOPE_SINGLETON) || scope.equalsIgnoreCase(SCOPE_PROTOTYPE)) {
            return realBd;
        } else {
            if (scope != null) {
                realBd.setScope(scope);
            }

            // 禁止autowire注入这个实际bean。
            realBd.setAutowireCandidate(false);

            // 将原始bean注册成proxyTarget.*
            String targetBeanName = "proxyTarget." + resolveId(element, realBd, parserContext);
            registerBeanDefinition(new BeanDefinitionHolder(realBd, targetBeanName), parserContext.getRegistry());

            // 创建proxy bean
            proxyBuilder.addConstructorArgValue(getProxyInterface(element));
            proxyBuilder.addConstructorArgValue(targetBeanName);

            return proxyBuilder.getBeanDefinition();
        }
    }

    protected abstract Class<?> getProxyInterface(Element element);

    protected String getScope(Element element, AbstractBeanDefinition realBd) {
        return element.getAttribute("scope");
    }

    protected abstract AbstractBeanDefinitionParser getRealObjectParser();

    public static class ProxyFactoryBean implements FactoryBean, BeanFactoryAware, ResourceLoaderAware {
        private final Class<?> targetBeanType;
        private final String targetBeanName;
        private BeanFactory factory;
        private ClassLoader loader;

        public ProxyFactoryBean(Class<?> targetBeanType, String targetBeanName) {
            this.targetBeanType = targetBeanType;
            this.targetBeanName = targetBeanName;
        }

        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.factory = beanFactory;
        }

        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.loader = resourceLoader.getClassLoader();
        }

        public Class<?> getObjectType() {
            return targetBeanType;
        }

        public boolean isSingleton() {
            return true;
        }

        public Object getObject() throws Exception {
            return createProxy(targetBeanType, loader, new ProxyTargetObjectFactory(targetBeanName, factory));
        }
    }

    private static class ProxyTargetObjectFactory implements ObjectFactory {
        private final String targetBeanName;
        private final BeanFactory factory;

        public ProxyTargetObjectFactory(String targetBeanName, BeanFactory factory) {
            this.targetBeanName = targetBeanName;
            this.factory = factory;
        }

        public Object getObject() throws BeansException {
            return factory.getBean(targetBeanName);
        }
    }
}
