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

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.support.parser.NamedBeanDefinitionParserMixin.DefaultNameBDParser;

/**
 * 定义一个bean definition，如果未指定id，则使用<code>getDefaultName()</code>所返回的默认名称。
 * <p>
 * 注意，此名称生成机制只对顶级bean有效，对innerBean仍然使用原有的命名机制。
 * </p>
 * 
 * @author Michael Zhou
 */
public abstract class AbstractNamedBeanDefinitionParser<T> extends AbstractSingleBeanDefinitionParser<T> implements
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
    protected final String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
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
}
