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
package com.alibaba.citrus.service.requestcontext.session.interceptor;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.logconfig.support.SecurityLogger;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.util.ClassUtil;

/**
 * 用来控制session attributes的使用。
 * 
 * @author Michael Zhou
 */
public class SessionAttributeWhitelist extends AbstractSessionAttributeAccessController {
    private final SecurityLogger log = new SecurityLogger();
    private Map<String, Class<?>> allowedAttributes;

    public void setLogName(String name) {
        log.setLogName(name);
    }

    public void setAllowedAttributes(Map<String, Class<?>> allowedAttributes) {
        this.allowedAttributes = allowedAttributes;
    }

    @Override
    public void init(SessionConfig sessionConfig) {
        super.init(sessionConfig);

        if (allowedAttributes == null) {
            allowedAttributes = createHashMap();
        }

        for (Map.Entry<String, Class<?>> entry : allowedAttributes.entrySet()) {
            if (entry.getValue() == null) {
                entry.setValue(Object.class);
            } else {
                entry.setValue(ClassUtil.getWrapperTypeIfPrimitive(entry.getValue()));
            }
        }
    }

    @Override
    protected boolean allowForAttribute(String name, Class<?> type) {
        Class<?> allowedType = allowedAttributes.get(name);

        if (allowedType == null) {
            return false;
        }

        if (type == null) {
            return true;
        }

        return allowedType.isAssignableFrom(type);
    }

    @Override
    protected Object readInvalidAttribute(String name, Object value) {
        log.getLogger().warn("Attribute to read is not in whitelist: name={}, type={}", name,
                value == null ? "unknwon" : value.getClass().getName());

        return value;
    }

    @Override
    protected Object writeInvalidAttribute(String name, Object value) {
        log.getLogger().warn("Attribute to write is not in whitelist: name={}, type={}", name,
                value == null ? "unknwon" : value.getClass().getName());

        return value;
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<SessionAttributeWhitelist> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder);

            Map<Object, Object> allowedAttrs = createManagedMap(element, parserContext);

            for (Element subElement : subElements(element, and(sameNs(element), name("attribute")))) {
                String name = trimToNull(subElement.getAttribute("name"));
                String type = trimToNull(subElement.getAttribute("type"));

                allowedAttrs.put(name, type);
            }

            builder.addPropertyValue("allowedAttributes", allowedAttrs);
        }
    }
}
