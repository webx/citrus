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
package com.alibaba.citrus.generictype.introspect.impl;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.List;
import java.util.Map;

import com.alibaba.citrus.generictype.introspect.PropertyInfo;

/**
 * 抽象的{@link PropertiesFinder}实现。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractPropertiesFinder extends AbstractTypeVisitor implements PropertiesFinder {
    protected static final String GET_PREFIX = "get";
    protected static final int GET_PREFIX_LENGTH = GET_PREFIX.length();
    protected static final String SET_PREFIX = "set";
    protected static final int SET_PREFIX_LENGTH = SET_PREFIX.length();
    protected static final String IS_PREFIX = "is";
    protected static final int IS_PREFIX_LENGTH = IS_PREFIX.length();
    private Map<String, List<PropertyInfo>> props;

    @Override
    public void visit() {
        super.visit();
        props = createHashMap();
    }

    public final Map<String, List<PropertyInfo>> getProperties() {
        return props;
    }

    protected final void addProperty(PropertyInfo prop) {
        String propName = prop.getName();
        List<PropertyInfo> propsWithSameName = props.get(propName);

        if (propsWithSameName == null) {
            propsWithSameName = createLinkedList();
            props.put(propName, propsWithSameName);
        }

        // 合并兼容的properties。
        boolean merged = false;

        for (PropertyInfo propWithSameName : propsWithSameName) {
            if (prop.getClass().equals(propWithSameName.getClass())
                    && prop.getType().equals(propWithSameName.getType())) {
                merged = merge(propWithSameName, prop);
                break;
            }
        }

        if (!merged) {
            propsWithSameName.add(prop);
        }
    }

    /**
     * 合并两个同类型、同名的properties。 如果merge成功，则返回<code>true</code>。
     */
    private boolean merge(PropertyInfo target, PropertyInfo with) {
        if (target instanceof AbstractPropertyInfo) {
            AbstractPropertyInfo p = (AbstractPropertyInfo) target;

            if (p.readMethod == null) {
                p.readMethod = with.getReadMethod();
            }

            if (p.writeMethod == null) {
                p.writeMethod = with.getWriteMethod();
            }

            return true;
        }

        return false;
    }
}
