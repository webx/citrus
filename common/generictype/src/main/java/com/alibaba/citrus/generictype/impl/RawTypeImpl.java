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
package com.alibaba.citrus.generictype.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.generictype.ClassTypeInfo;
import com.alibaba.citrus.generictype.FieldInfo;
import com.alibaba.citrus.generictype.GenericDeclarationInfo;
import com.alibaba.citrus.generictype.MethodInfo;
import com.alibaba.citrus.generictype.RawTypeInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.util.ClassUtil;
import com.alibaba.citrus.util.internal.LazyLoader;
import com.alibaba.citrus.util.internal.LazyLoader.Loader;

/**
 * 对{@link RawTypeInfo}的实现。
 * 
 * @author Michael Zhou
 */
class RawTypeImpl extends AbstractGenericDeclarationInfo implements RawTypeInfo {
    private static final Map<String, TypeInfo> PRIMITIVE_WRAPPERS = createHashMap();
    private final LazyLoader<Supertypes, Object> supertypesLoader;

    static {
        PRIMITIVE_WRAPPERS.put("boolean", factory.getType(Boolean.class));
        PRIMITIVE_WRAPPERS.put("byte", factory.getType(Byte.class));
        PRIMITIVE_WRAPPERS.put("char", factory.getType(Character.class));
        PRIMITIVE_WRAPPERS.put("double", factory.getType(Double.class));
        PRIMITIVE_WRAPPERS.put("float", factory.getType(Float.class));
        PRIMITIVE_WRAPPERS.put("int", factory.getType(Integer.class));
        PRIMITIVE_WRAPPERS.put("long", factory.getType(Long.class));
        PRIMITIVE_WRAPPERS.put("short", factory.getType(Short.class));
        PRIMITIVE_WRAPPERS.put("void", factory.getType(Void.class));
    }

    RawTypeImpl(Class<?> rawClass) {
        super(rawClass);

        // 延迟遍历并取得所有父类和父接口。
        this.supertypesLoader = LazyLoader.getDefault(new SupertypesLoader());
    }

    public Class<?> getRawType() {
        return (Class<?>) declaration;
    }

    public String getName() {
        return getRawType().getName();
    }

    public String getSimpleName() {
        return ClassUtil.getSimpleClassName(getRawType());
    }

    public boolean isPrimitive() {
        return getRawType().isPrimitive();
    }

    public boolean isArray() {
        return false;
    }

    public boolean isInterface() {
        return getRawType().isInterface();
    }

    public TypeInfo getPrimitiveWrapperType() {
        if (isPrimitive()) {
            String name = getRawType().getName();
            TypeInfo wrapperType = PRIMITIVE_WRAPPERS.get(name);

            return assertNotNull(wrapperType, "Unknown primitive type: %s", name);
        }

        return this;
    }

    public TypeInfo getComponentType() {
        return this;
    }

    public TypeInfo getDirectComponentType() {
        return this;
    }

    public int getDimension() {
        return 0;
    }

    public List<TypeInfo> getInterfaces() {
        return supertypesLoader.getInstance().interfaces;
    }

    public List<TypeInfo> getSuperclasses() {
        return supertypesLoader.getInstance().superclasses;
    }

    public List<TypeInfo> getSupertypes() {
        return supertypesLoader.getInstance().supertypes;
    }

    public TypeInfo getSupertype(Class<?> equivalentClass) {
        return TypeInfoFactory.findSupertype(this, equivalentClass);
    }

    // Implementation of TypeInfo.resolve
    public ClassTypeInfo resolve(GenericDeclarationInfo context) {
        return this;
    }

    // Implementation of TypeInfo.resolve
    public ClassTypeInfo resolve(GenericDeclarationInfo context, boolean includeBaseType) {
        return this;
    }

    // Implementation of ClassTypeInfo
    public FieldInfo getField(String name) {
        return TypeInfoFactory.getField(this, this, name);
    }

    // Implementation of ClassTypeInfo
    public FieldInfo getField(ClassTypeInfo declaringType, String name) {
        return TypeInfoFactory.getField(this, declaringType, name);
    }

    // Implementation of ClassTypeInfo
    public MethodInfo getConstructor(Class<?>... paramTypes) {
        return TypeInfoFactory.getConstructor(this, paramTypes);
    }

    // Implementation of ClassTypeInfo
    public MethodInfo getMethod(String methodName, Class<?>... paramTypes) {
        return TypeInfoFactory.getMethod(this, methodName, paramTypes);
    }

    /**
     * 取得字符串表示。
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(ClassUtil.getSimpleClassName(getRawType()));
        appendTypeParameters(buf);

        return buf.toString();
    }

    /**
     * 父类、接口的信息。
     */
    private static class Supertypes {
        private final List<TypeInfo> supertypes;
        private final List<TypeInfo> interfaces;
        private final List<TypeInfo> superclasses;

        private Supertypes(RawTypeImpl rawType) {
            LinkedList<TypeInfo> supertypes = createLinkedList();
            InterfaceQueue interfaceQueue = new InterfaceQueue();
            Type type = rawType.getRawType();
            Class<?> rawClass = rawType.getRawType();
            boolean hasObject = false;

            // this class and its superclasses
            if (rawClass == Object.class) {
                hasObject = true;
            } else if (rawClass.isInterface()) {
                interfaceQueue.push(rawClass);
                hasObject = true;
            } else {
                supertypes.add(rawType);

                while (true) {
                    // queue intefaces
                    interfaceQueue.push(rawClass.getGenericInterfaces());

                    // superclass
                    type = rawClass.getGenericSuperclass();

                    if (type == Object.class) {
                        hasObject = true;
                        break;
                    }

                    if (type == null) {
                        break;
                    }

                    TypeInfo typeInfo = factory.getType(type);

                    supertypes.add(typeInfo);
                    rawClass = typeInfo.getRawType();
                }
            }

            // interfaces
            int interfaceCount;

            for (interfaceCount = 0; !interfaceQueue.isEmpty(); interfaceCount++) {
                type = interfaceQueue.pop();

                TypeInfo typeInfo = factory.getType(type);

                supertypes.add(typeInfo);
                rawClass = typeInfo.getRawType();
                interfaceQueue.push(rawClass.getGenericInterfaces());
            }

            // Object
            if (hasObject) {
                supertypes.add(TypeInfo.OBJECT);
            }

            // 创建lists
            List<TypeInfo> interfaces = createArrayList(interfaceCount);
            List<TypeInfo> superclasses = createArrayList(supertypes.size() - interfaceCount);

            for (TypeInfo supertype : supertypes) {
                if (supertype.getRawType().isInterface()) {
                    interfaces.add(supertype);
                } else {
                    superclasses.add(supertype);
                }
            }

            assertTrue(interfaces.size() == interfaceCount, "interfaceCount");

            this.supertypes = Collections.unmodifiableList(createArrayList(supertypes));
            this.interfaces = Collections.unmodifiableList(interfaces);
            this.superclasses = Collections.unmodifiableList(superclasses);
        }
    }

    /**
     * 创建supertypes的装载器。
     */
    private class SupertypesLoader implements Loader<Supertypes, Object> {
        public Supertypes load(Object context) {
            return new Supertypes(RawTypeImpl.this);
        }
    }

    /**
     * 一个简单的接口队列，用来遍历所有接口。
     */
    private static class InterfaceQueue {
        private final LinkedList<Type> queue = createLinkedList();
        private final Set<Class<?>> visited = createHashSet();

        public void push(Type[] types) {
            for (Type type : types) {
                push(type);
            }
        }

        public void push(Type type) {
            Class<?> interfaceClass = null;

            if (type instanceof Class<?>) {
                interfaceClass = (Class<?>) type;
            } else if (type instanceof ParameterizedType) {
                interfaceClass = (Class<?>) ((ParameterizedType) type).getRawType();
            } else {
                unreachableCode("Unexpected interface type: %s", type);
            }

            if (!visited.contains(interfaceClass)) {
                queue.addLast(type);
                visited.add(interfaceClass);
            }
        }

        public Type pop() {
            return queue.removeFirst();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }
}
