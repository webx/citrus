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

import static com.alibaba.citrus.generictype.TypeInfo.*;
import static com.alibaba.citrus.util.internal.StringUtil.*;
import static java.lang.reflect.Modifier.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.alibaba.citrus.generictype.MethodInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.introspect.PropertyEvaluationFailureException;
import com.alibaba.citrus.generictype.introspect.PropertyInfo;
import com.alibaba.citrus.generictype.introspect.SimplePropertyInfo;
import com.alibaba.citrus.util.internal.StringUtil;

/**
 * 分析简单的Java Beans属性，也就是：
 * <ul>
 * <li>可读属性：<code>PropertyType getPropertyName()</code>。</li>
 * <li>可读boolean属性：<code>boolean isPropertyName()</code>。</li>
 * <li>可写属性：<code>setPropertyName(PropertyType propertyValue)</code>。</li>
 * </ul>
 * <p>
 * 简单的Java Beans属性的名称不能为空，换言之，<code>get()</code>/<code>set()</code>
 * 方法不会被看作一个property。
 * </p>
 * 
 * @author Michael Zhou
 */
public class SimplePropertiesFinder extends AbstractPropertiesFinder {
    @Override
    public void visitMethod(Method method) {
        if (getAccessQualifier(method) != PUBLIC) {
            return;
        }

        String name = method.getName();
        Class<?>[] paramTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        int paramCount = paramTypes.length;

        PropertyInfo prop = null;

        switch (paramCount) {
            case 0:
                if (name.startsWith(GET_PREFIX)) {
                    if (returnType != void.class) {
                        String propName = uncapitalize(name.substring(GET_PREFIX_LENGTH));

                        prop = createSimpleProperty(method, propName, true, -1);
                    }
                } else if (returnType == boolean.class && name.startsWith(IS_PREFIX)) {
                    String propName = uncapitalize(name.substring(IS_PREFIX_LENGTH));

                    prop = createSimpleProperty(method, propName, true, -1);
                }

                break;

            case 1:
                if (name.startsWith(SET_PREFIX)) {
                    String propName = uncapitalize(name.substring(SET_PREFIX_LENGTH));

                    prop = createSimpleProperty(method, propName, false, 0);
                }

                break;

            default:
                break;
        }

        if (prop != null) {
            addProperty(prop);
        }
    }

    private PropertyInfo createSimpleProperty(Method javaMethod, String propName, boolean read, int paramIndex) {
        if (StringUtil.isEmpty(propName)) {
            return null;
        }

        MethodInfo method = factory.getMethod(javaMethod, getType());
        TypeInfo declaringType = method.getDeclaringType();
        TypeInfo type = paramIndex >= 0 ? method.getParameterTypes().get(paramIndex) : method.getReturnType();
        MethodInfo readMethod = read ? method : null;
        MethodInfo writMethod = read ? null : method;

        return new SimplePropertyImpl(propName, declaringType, type, readMethod, writMethod);
    }

    /**
     * 代表一个简单的属性信息。
     */
    private static class SimplePropertyImpl extends AbstractPropertyInfo implements SimplePropertyInfo {
        private SimplePropertyImpl(String name, TypeInfo declaringType, TypeInfo type, MethodInfo readMethod,
                                   MethodInfo writeMethod) {
            super(name, declaringType, type, readMethod, writeMethod);
        }

        public Object getValue(Object object) {
            try {
                return getReadMethod().getMethod().invoke(object);
            } catch (InvocationTargetException e) {
                throw new PropertyEvaluationFailureException(e.getCause());
            } catch (Exception e) {
                throw new PropertyEvaluationFailureException(e);
            }
        }

        @Override
        public void setValue(Object object, Object value) {
        }

        @Override
        protected String getDescription() {
            return "SimpleProperty";
        }
    }
}
