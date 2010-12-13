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
package com.alibaba.citrus.generictype.introspect;

import static com.alibaba.citrus.util.Assert.*;

import java.util.List;
import java.util.Map;

import com.alibaba.citrus.generictype.TypeInfo;

/**
 * 用来分析一个类型的反射信息的工具。
 * 
 * @author Michael Zhou
 */
public abstract class Introspector {
    private final static Factory factory = newFactory();

    /**
     * 取得类型对应的{@link Introspector}。
     */
    public static Introspector getInstance(TypeInfo type) {
        return factory.getInstance(type);
    }

    /**
     * 取得所有的properties信息。
     */
    public abstract Map<String, List<PropertyInfo>> getProperties();

    /**
     * 查找指定名称、类型、读写特性的simple property。
     * <p>
     * 如果未指定类型，表示任意类型。如果读写属性为false，表示任意读写属性。
     * </p>
     * <p>
     * 如果找不到，则返回<code>null</code>。
     * </p>
     */
    public final SimplePropertyInfo findSimpleProperty(String propertyName, Class<?> type, boolean readable,
                                                       boolean writable) {
        return findProperty(SimplePropertyInfo.class, propertyName, type, readable, writable);
    }

    /**
     * 查找指定名称、类型、读写特性的indexed property。
     * <p>
     * 如果未指定类型，表示任意类型。如果读写属性为false，表示任意读写属性。
     * </p>
     * <p>
     * 如果找不到，则返回<code>null</code>。
     * </p>
     */
    public final IndexedPropertyInfo findIndexedProperty(String propertyName, Class<?> type, boolean readable,
                                                         boolean writable) {
        return findProperty(IndexedPropertyInfo.class, propertyName, type, readable, writable);
    }

    /**
     * 查找指定名称、类型、读写特性的mapped property。
     * <p>
     * 如果未指定类型，表示任意类型。如果读写属性为false，表示任意读写属性。
     * </p>
     * <p>
     * 如果找不到，则返回<code>null</code>。
     * </p>
     */
    public final MappedPropertyInfo findMappedProperty(String propertyName, Class<?> type, boolean readable,
                                                       boolean writable) {
        return findProperty(MappedPropertyInfo.class, propertyName, type, readable, writable);
    }

    /**
     * 查找指定名称、类型、读写特性的property。
     * <p>
     * 如果未指定类型，表示任意类型。如果读写属性为false，表示任意读写属性。
     * </p>
     * <p>
     * 如果找不到，则返回<code>null</code>。
     * </p>
     */
    private <T extends PropertyInfo> T findProperty(Class<T> propertyType, String propertyName, Class<?> type,
                                                    boolean readable, boolean writable) {
        assertNotNull(propertyType, "propertyType");

        List<PropertyInfo> props = getProperties().get(propertyName);

        if (props != null) {
            for (PropertyInfo prop : props) {
                if (!propertyType.isInstance(prop)) {
                    continue;
                }

                if (type != null && !type.isAssignableFrom(prop.getType().getRawType())) {
                    continue;
                }

                if (readable && !prop.isReadable()) {
                    continue;
                }

                if (writable && !prop.isWritable()) {
                    continue;
                }

                return propertyType.cast(prop);
            }
        }

        return null;
    }

    /**
     * 用于创建{@link Introspector}。
     */
    protected static interface Factory {
        Introspector getInstance(TypeInfo type);
    }

    /**
     * 创建factory，但避免在compile时刻依赖impl package。
     */
    private static Factory newFactory() {
        String factoryImplName = Factory.class.getPackage().getName() + ".impl.IntrospectorImpl$FactoryImpl";
        Factory factoryImpl = null;

        try {
            factoryImpl = (Factory) Factory.class.getClassLoader().loadClass(factoryImplName).newInstance();
        } catch (Exception e) {
            unexpectedException(e, "Failed to create Introspector.Factory");
        }

        return factoryImpl;
    }
}
