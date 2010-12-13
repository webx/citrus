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
package com.alibaba.citrus.springext.contrib.simple;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;

import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;

public class Base {
    private Object[] objects;

    public Base(Object... objects) {
        this.objects = objects;
    }

    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other.getClass() != getClass()) {
            return false;
        }

        Base otherBase = (Base) other;

        if (objects.length != otherBase.objects.length) {
            return false;
        }

        for (int i = 0; i < objects.length; i++) {
            if (!objects[i].equals(otherBase.objects[i])) {
                return false;
            }
        }

        return true;
    }

    protected static class AbstractDefinitionParser<T extends Base> extends AbstractNamedBeanDefinitionParser<T> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            List<Object> objects = createManagedList(element, parserContext);

            for (Element subElement : subElements(element)) {
                objects.add(parserContext.getDelegate().parseCustomElement(subElement, builder.getRawBeanDefinition()));
            }

            builder.addConstructorArgValue(objects);
        }

        @Override
        protected String getDefaultName() {
            return getBeanClass(null).getSimpleName().toLowerCase();
        }
    }
}
