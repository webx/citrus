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
package com.alibaba.citrus.service.requestcontext.session.valueencoder.impl;

import static com.alibaba.citrus.service.configuration.support.PropertyEditorRegistrarsSupport.*;
import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.requestcontext.session.valueencoder.AbstractSessionValueEncoder;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

/**
 * 将一个简单类型编码成字符串，或反之。支持加密。
 * 
 * @author Michael Zhou
 */
public class SimpleValueEncoder extends AbstractSessionValueEncoder {
    private Class<?> type;

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    protected boolean doURLEncode() {
        return true;
    }

    /**
     * 简单值不压缩，取得的字符串较短。
     */
    @Override
    protected boolean doCompress() {
        return false;
    }

    @Override
    protected String encodeValue(Object value) throws Exception {
        return convertToString(type, value, getTypeConverter());
    }

    @Override
    protected Object decodeValue(String encodedValue) throws Exception {
        return convertToType(type, encodedValue, getTypeConverter());
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<SimpleValueEncoder> implements
            ContributionAware {
        private ConfigurationPoint encrypterConfigurationPoint;

        public void setContribution(Contribution contrib) {
            this.encrypterConfigurationPoint = getSiblingConfigurationPoint("services/request-contexts/session/"
                    + "encrypters", contrib);
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "type", "charset");

            builder.addPropertyValue("propertyEditorRegistrars", parseRegistrars(element, parserContext, builder));

            for (Element subElement : subElements(element)) {
                BeanDefinitionHolder encrypter = parseConfigurationPointBean(subElement, encrypterConfigurationPoint,
                        parserContext, builder);

                if (encrypter != null) {
                    builder.addPropertyValue("encrypter", encrypter);
                    break;
                }
            }
        }
    }
}
