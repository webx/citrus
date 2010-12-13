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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.introspect.Introspector;
import com.alibaba.citrus.generictype.introspect.PropertyInfo;

/**
 * 实现{@link Introspector}。
 * 
 * @author Michael Zhou
 */
public class IntrospectorImpl extends Introspector {
    private final TypeInfo type;
    private final Map<String, List<PropertyInfo>> props;

    /**
     * 创建一个{@link Introspector}实例。
     */
    IntrospectorImpl(TypeInfo type) {
        this.type = assertNotNull(type, "type");
        this.props = new TypeScanner().scan();
    }

    private TypeVisitor[] getVisitors() {
        return new TypeVisitor[] { new SimplePropertiesFinder(), new IndexedPropertiesFinder(),
                new MappedPropertiesFinder(), new ArrayPropertiesFinder(), new MapPropertiesFinder() };
    }

    @Override
    public Map<String, List<PropertyInfo>> getProperties() {
        return props;
    }

    /**
     * 扫描并分析类型。
     */
    private class TypeScanner {
        private final TypeVisitor[] visitors = getVisitors();
        private final Map<MethodSignature, Method> methods = createHashMap();

        /**
         * 分析一个类，及其基类和接口。
         */
        public Map<String, List<PropertyInfo>> scan() {
            boolean first = true;

            // 开始
            for (TypeVisitor visitor : visitors) {
                visitor.visit();
            }

            // 分析
            for (TypeInfo t : type.getSupertypes()) {
                scanType(t, true, first);
                first = false;
            }

            // 结束
            for (TypeVisitor visitor : visitors) {
                visitor.visitEnd();
            }

            // 收集结果
            PropertiesMap props = new PropertiesMap();

            for (TypeVisitor visitor : visitors) {
                if (visitor instanceof PropertiesFinder) {
                    props.addAll(((PropertiesFinder) visitor).getProperties());
                }
            }

            return unmodifiableMap(props);
        }

        /**
         * 分析一个类。
         */
        private void scanType(TypeInfo type, boolean scanFields, boolean scanConstructors) {
            for (TypeVisitor visitor : visitors) {
                visitor.visitType(type);
            }

            Class<?> clazz = type.getRawType();

            // 扫描class序列中的每一个field，无论其访问性是什么。
            if (scanFields) {
                for (Field field : clazz.getDeclaredFields()) {
                    for (TypeVisitor visitor : visitors) {
                        visitor.visitField(field);
                    }
                }
            }

            // 只扫描第一个class中的constructor，无论其访问性是什么。
            if (scanConstructors) {
                for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                    for (TypeVisitor visitor : visitors) {
                        visitor.visitConstructor(constructor);
                    }
                }
            }

            // 扫描class序列中的每一个method，无论其访问性是什么。
            // 然而，对于public和protected method，只有最顶部的那个实现会被访问。
            for (Method method : clazz.getDeclaredMethods()) {
                MethodSignature sig = new MethodSignature(method);
                boolean accessible = (method.getModifiers() & (PUBLIC | PROTECTED)) != 0;

                if (!accessible || !methods.containsKey(sig)) {
                    if (accessible) {
                        methods.put(sig, method);
                    }

                    for (TypeVisitor visitor : visitors) {
                        visitor.visitMethod(method);
                    }
                }
            }
        }

    }

    /**
     * 代表一个Properties的映射表。
     */
    private static class PropertiesMap extends HashMap<String, List<PropertyInfo>> {
        private static final long serialVersionUID = 3899442980552826145L;

        public void addAll(Map<String, List<PropertyInfo>> props) {
            if (props != null) {
                for (Map.Entry<String, List<PropertyInfo>> entry : props.entrySet()) {
                    String propName = assertNotNull(entry.getKey(), "property name is null: %s", entry);
                    List<PropertyInfo> propsWithSameName = super.get(propName);

                    if (propsWithSameName == null) {
                        propsWithSameName = createLinkedList();
                        super.put(propName, propsWithSameName);
                    }

                    propsWithSameName.addAll(entry.getValue());
                }
            }
        }

        @Override
        public String toString() {
            String[] names = keySet().toArray(new String[size()]);

            sort(names);

            StringBuilder buf = new StringBuilder();
            int i = 0;

            buf.append("Properties:\n");

            for (String name : names) {
                List<PropertyInfo> props = get(name);

                for (PropertyInfo prop : props) {
                    buf.append("\n--- ").append(++i).append(" ------------------------------\n");
                    buf.append(prop).append("\n");
                }
            }

            buf.append("==============================\n");

            return buf.toString();
        }
    }

    /**
     * 创建{@link Introspector}的工厂。
     */
    public static class FactoryImpl implements Factory {
        public Introspector getInstance(TypeInfo type) {
            return new IntrospectorImpl(type);
        }
    }
}
