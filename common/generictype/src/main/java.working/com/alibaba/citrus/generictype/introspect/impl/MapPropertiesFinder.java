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

import java.util.Map;

import com.alibaba.citrus.generictype.ClassTypeInfo;
import com.alibaba.citrus.generictype.MethodInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.introspect.IndexedPropertyInfo;
import com.alibaba.citrus.generictype.introspect.PropertyEvaluationFailureException;
import com.alibaba.citrus.generictype.introspect.PropertyInfo;

/**
 * 分析<code>Map</code>，将map看作mapped属性来操作。
 * 
 * @author Michael Zhou
 */
public class MapPropertiesFinder extends SinglePropertyFinder {
    @Override
    protected PropertyInfo createPropertyInfo(TypeInfo type) {
        if (type instanceof ClassTypeInfo && Map.class.isAssignableFrom(type.getRawType())) {
            MethodInfo writeMethod = ((ClassTypeInfo) type).getMethod("put", Object.class, Object.class);
            Class<?> keyType = writeMethod.getParameterTypes().get(0).getRawType();

            if (keyType.isAssignableFrom(String.class)) {
                MethodInfo readMethod = ((ClassTypeInfo) type).getMethod("get", Object.class);
                TypeInfo propertyType = writeMethod.getParameterTypes().get(1);

                return new MapPropertyImpl(readMethod.getDeclaringType(), propertyType, readMethod, writeMethod);
            }
        }

        return null;
    }

    /**
     * 代表一个map的属性信息。
     */
    private static class MapPropertyImpl extends AbstractPropertyInfo implements IndexedPropertyInfo {
        private MapPropertyImpl(TypeInfo declaringType, TypeInfo type, MethodInfo readMethod, MethodInfo writeMethod) {
            super(null, declaringType, type, readMethod, writeMethod);
        }

        public Object getValue(Object object, int index) {
            try {
                return ((Map<?, ?>) object).get(index);
            } catch (Exception e) {
                throw new PropertyEvaluationFailureException(e);
            }
        }

        @Override
        public void setValue(Object object, int index, Object value) {
        }

        @Override
        protected String getDescription() {
            return "MappedProperty (Map)";
        }
    }
}
