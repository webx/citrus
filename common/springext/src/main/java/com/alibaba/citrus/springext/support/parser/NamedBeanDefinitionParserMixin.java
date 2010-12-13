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

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.springframework.beans.factory.xml.AbstractBeanDefinitionParser.*;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.util.SpringExtUtil;

class NamedBeanDefinitionParserMixin {
    private final DefaultNameBDParser parser;

    public NamedBeanDefinitionParserMixin(DefaultNameBDParser parser) {
        this.parser = assertNotNull(parser, "parser");
    }

    /**
     * 从id attribute中取得bean name，假如未指定，则从<code>getDefaultName()</code>中取得默认名。
     */
    String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
        String id = trimToNull(element.getAttribute(ID_ATTRIBUTE));

        if (id == null) {
            id = assertNotNull(getDefaultId(), "neither id nor defaultName was specified");
            id = SpringExtUtil.generateBeanName(id, parserContext.getRegistry(), definition, parserContext.isNested());
        }

        return id;
    }

    /**
     * 假如当前bean name为默认名，则同时注册默认的aliases。
     */
    protected void registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
        BeanDefinitionHolder newHolder = definition;

        if (isEmptyArray(definition.getAliases())) {
            String[] defaultAliases = getDefaultAliases(definition.getBeanName());

            if (!isEmptyArray(defaultAliases)) {
                newHolder = new BeanDefinitionHolder(definition.getBeanDefinition(), definition.getBeanName(),
                        defaultAliases);
            }
        }

        parser.super_registerBeanDefinition(newHolder, registry);
    }

    private String getDefaultId() {
        String[] names = getDefaultNames();

        if (names.length > 0) {
            return names[0];
        }

        return null;
    }

    private String[] getDefaultAliases(String id) {
        String[] names = getDefaultNames();

        if (names.length > 1 && isEquals(id, names[0])) {
            String[] aliases = new String[names.length - 1];
            System.arraycopy(names, 1, aliases, 0, aliases.length);
            return aliases;
        }

        return EMPTY_STRING_ARRAY;
    }

    private String[] getDefaultNames() {
        String defaultName = trimToNull(parser.internal_getDefaultName());
        String[] names = EMPTY_STRING_ARRAY;

        if (defaultName != null) {
            names = defaultName.split("(,|\\s)+");
        }

        return names;
    }

    interface DefaultNameBDParser {
        String internal_getDefaultName();

        void super_registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry);
    }
}
