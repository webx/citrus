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
package com.alibaba.citrus.service.requestcontext.session.encoder.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.requestcontext.session.encoder.AbstractSerializationEncoder;
import com.alibaba.citrus.service.requestcontext.session.encrypter.Encrypter;
import com.alibaba.citrus.service.requestcontext.session.serializer.Serializer;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

/**
 * 通过<code>Serializer</code>提供的序列化机制来编码对象，以及解码字符串。
 * <p>
 * 可设置<code>Serializer</code>和<code>Encrypter</code>， <code>Serializer</code>
 * 的默认值为<code>HessianSerializer</code>。
 * </p>
 * 
 * @author Michael Zhou
 */
public class SerializationEncoder extends AbstractSerializationEncoder {
    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public Encrypter getEncrypter() {
        return encrypter;
    }

    public void setEncrypter(Encrypter encrypter) {
        this.encrypter = encrypter;
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<SerializationEncoder> implements
            ContributionAware {
        private ConfigurationPoint serializerConfigurationPoint;
        private ConfigurationPoint encrypterConfigurationPoint;

        public void setContribution(Contribution contrib) {
            this.serializerConfigurationPoint = getSiblingConfigurationPoint("services/request-contexts/session/"
                    + "serializers", contrib);

            this.encrypterConfigurationPoint = getSiblingConfigurationPoint("services/request-contexts/session/"
                    + "encrypters", contrib);
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            for (Element subElement : subElements(element)) {
                BeanDefinitionHolder serializer = parseConfigurationPointBean(subElement, serializerConfigurationPoint,
                        parserContext, builder);

                if (serializer != null) {
                    builder.addPropertyValue("serializer", serializer);
                }

                BeanDefinitionHolder encrypter = parseConfigurationPointBean(subElement, encrypterConfigurationPoint,
                        parserContext, builder);

                if (encrypter != null) {
                    builder.addPropertyValue("encrypter", encrypter);
                }
            }
        }
    }
}
