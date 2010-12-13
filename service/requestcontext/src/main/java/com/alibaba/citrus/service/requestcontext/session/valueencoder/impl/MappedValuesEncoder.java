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
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Map;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.requestcontext.session.valueencoder.AbstractSessionValueEncoder;
import com.alibaba.citrus.service.requestcontext.util.QueryStringParser;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

/**
 * 将一个<code>Map</code>编码成字符串，或反之。支持加密。
 * 
 * @author Michael Zhou
 */
public class MappedValuesEncoder extends AbstractSessionValueEncoder {
    private Class<?> valueType;

    public void setValueType(Class<?> type) {
        this.valueType = type;
    }

    @Override
    protected boolean doURLEncode() {
        return false;
    }

    /**
     * 复杂值压缩，取得的字符串较短。
     */
    @Override
    protected boolean doCompress() {
        return true;
    }

    protected String getEqualSign() {
        return ":";
    }

    @Override
    protected String encodeValue(Object value) throws Exception {
        assertTrue(value instanceof Map, "wrong session attribute type: " + value.getClass());

        Map<?, ?> map = (Map<?, ?>) value;
        Map<String, String> encodedMap = createLinkedHashMap();
        TypeConverter converter = getTypeConverter();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String encodedValue = convertToString(valueType, entry.getValue(), converter);

            encodedMap.put(key, encodedValue);
        }

        return new QueryStringParser(getCharset()).setEqualSign(getEqualSign()).append(encodedMap).toQueryString();
    }

    @Override
    protected Object decodeValue(String encodedValue) throws Exception {
        final Map<String, Object> map = createLinkedHashMap();
        final TypeConverter converter = getTypeConverter();

        new QueryStringParser(getCharset()) {
            @Override
            protected void add(String key, String encodedValue) {
                map.put(key, convertToType(valueType, encodedValue, converter));
            }
        }.setEqualSign(getEqualSign()).parse(encodedValue);

        return map;
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<MappedValuesEncoder> implements
            ContributionAware {
        private ConfigurationPoint encrypterConfigurationPoint;

        public void setContribution(Contribution contrib) {
            this.encrypterConfigurationPoint = getSiblingConfigurationPoint("services/request-contexts/session/"
                    + "encrypters", contrib);
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "valueType", "charset");

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
