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

import static com.alibaba.citrus.util.Assert.*;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.BeanReferenceFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.util.SpringExtUtil;

/**
 * 用来解析default element的parser。
 * 
 * @author Michael Zhou
 */
public class DefaultElementDefinitionParser extends AbstractBeanDefinitionParser {
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        Object beanOrRef = SpringExtUtil.parseBean(element, parserContext, parserContext.getContainingBeanDefinition());
        AbstractBeanDefinition abd;

        if (beanOrRef instanceof BeanReference) {
            BeanDefinitionBuilder refBean = BeanDefinitionBuilder.genericBeanDefinition(BeanReferenceFactoryBean.class);

            refBean.addPropertyValue("targetBeanName", ((RuntimeBeanReference) beanOrRef).getBeanName());
            abd = refBean.getBeanDefinition();
        } else if (beanOrRef instanceof BeanDefinitionHolder) {
            BeanDefinition bd = ((BeanDefinitionHolder) beanOrRef).getBeanDefinition();
            assertTrue(bd instanceof AbstractBeanDefinition, "unexpected bean definition type: %s", bd);
            abd = (AbstractBeanDefinition) bd;
        } else {
            abd = null;
            unreachableCode("unexpected bean definition type: %s", beanOrRef);
        }

        return abd;
    }
}
