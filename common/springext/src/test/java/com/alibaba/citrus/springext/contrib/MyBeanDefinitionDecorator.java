/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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

package com.alibaba.citrus.springext.contrib;

import com.alibaba.citrus.springext.contrib.deco.MyDecorator;
import com.alibaba.citrus.util.internal.InterfaceImplementorBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MyBeanDefinitionDecorator implements BeanDefinitionDecorator {
    public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
        String interfaceName = node instanceof Element
                               ? ((Element) node).getAttribute("interface")
                               : ((Attr) node).getValue();

        BeanDefinitionBuilder bd = BeanDefinitionBuilder.genericBeanDefinition(MyDecoratorFactory.class);

        bd.addConstructorArgValue(definition.getBeanDefinition());
        bd.addConstructorArgValue(interfaceName);

        return new BeanDefinitionHolder(bd.getBeanDefinition(), definition.getBeanName(), definition.getAliases());
    }

    public static class MyDecoratorFactory implements FactoryBean<Object> {
        private final Object   object;
        private final Class<?> itfs;

        public MyDecoratorFactory(Object object, Class<?> itfs) {
            this.object = object;
            this.itfs = itfs;
        }

        @Override
        public Object getObject() throws Exception {
            return new InterfaceImplementorBuilder().setSuperclass(object.getClass()).addInterface(itfs).toObject(object);
        }

        @Override
        public Class<?> getObjectType() {
            return MyDecorator.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }
}
