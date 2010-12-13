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
import static com.alibaba.citrus.util.ClassUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static java.util.Collections.*;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;

import com.alibaba.citrus.generictype.ClassTypeInfo;
import com.alibaba.citrus.generictype.FieldInfo;
import com.alibaba.citrus.generictype.GenericDeclarationInfo;
import com.alibaba.citrus.generictype.MethodInfo;
import com.alibaba.citrus.generictype.ParameterizedTypeInfo;
import com.alibaba.citrus.generictype.RawTypeInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.TypeVariableInfo;
import com.alibaba.citrus.util.internal.LazyLoader;
import com.alibaba.citrus.util.internal.LazyLoader.Loader;

/**
 * 对{@link ParameterizedTypeInfo}的实现。
 * 
 * @author Michael Zhou
 */
class ParameterizedTypeImpl implements ParameterizedTypeInfo {
    private final RawTypeInfo rawType;
    private final LazyLoader<Supertypes, Object> supertypesLoader;
    private List<TypeInfo> actualTypeArguments;
    private boolean resolved;

    ParameterizedTypeImpl(RawTypeInfo rawType) {
        assertTrue(rawType != null && getTypeParameters(rawType).length > 0, "rawType");
        this.rawType = rawType;
        this.supertypesLoader = LazyLoader.getDefault(new SupertypesLoader());
    }

    void init(TypeInfo[] actualTypeArguments) {
        this.actualTypeArguments = unmodifiableList(asList(actualTypeArguments));

        // 检查参数个数是否匹配
        int actualArgs = this.actualTypeArguments.size();
        int expectedParams = getTypeParameters(rawType).length;

        assertTrue(actualArgs == expectedParams, "actual arguments length not match: expected %d, actual %d",
                expectedParams, actualArgs);

        // 检查参数类型是否匹配
        for (int i = 0; i < actualTypeArguments.length; i++) {
            TypeVariable<?> var = getTypeParameters(rawType)[i];
            Class<?> argClass = actualTypeArguments[i].getRawType();

            checkBounds(var, null, argClass, false);
        }
    }

    private static TypeVariable<?>[] getTypeParameters(RawTypeInfo rawType) {
        return rawType.getRawType().getTypeParameters(); // 避免调用rawType.getTypeParameters()以免递归
    }

    /**
     * 检查类型<code>argClass</code>是不是<code>var</code>的bounds的子类。
     */
    private static void checkBounds(TypeVariable<?> var, Type type, Class<?> argClass, boolean array) {
        if (type == null) {
            type = var;
        }

        if (type instanceof Class<?>) {
            Class<?> superclass = (Class<?>) type;

            assertTrue(superclass.isAssignableFrom(argClass),
                    array ? "actual argument of parameter %s should be array of sub-class of %s, but was array of %s"
                            : "actual argument of parameter %s should be sub-class of %s, but was %s", var, superclass,
                    argClass);

            return;
        }

        if (type instanceof TypeVariable<?>) {
            for (Type ub : ((TypeVariable<?>) type).getBounds()) {
                checkBounds(var, ub, argClass, false);
            }

            return;
        }

        if (type instanceof GenericArrayType) {
            assertTrue(argClass.isArray(), "actual argument of parameter %s should be array, but was %s", var, argClass);
            checkBounds(var, ((GenericArrayType) type).getGenericComponentType(), argClass.getComponentType(), true);
            return;
        }

        if (type instanceof ParameterizedType) {
            checkBounds(var, ((ParameterizedType) type).getRawType(), argClass, false);
            return;
        }
    }

    public Class<?> getRawType() {
        return rawType.getRawType();
    }

    public String getName() {
        return rawType.getName();
    }

    public String getSimpleName() {
        return rawType.getSimpleName();
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isInterface() {
        return rawType.isInterface();
    }

    public TypeInfo getPrimitiveWrapperType() {
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

    public boolean isGeneric() {
        return true;
    }

    public List<TypeVariableInfo> getTypeParameters() {
        return rawType.getTypeParameters();
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
        return resolve(context, true);
    }

    // Implementation of TypeInfo.resolve
    public ClassTypeInfo resolve(GenericDeclarationInfo context, boolean includeBaseType) {
        if (resolved) {
            return this;
        } else {
            List<TypeVariableInfo> vars = getTypeParameters();
            List<TypeInfo> actualArgs = getActualTypeArguments();
            TypeInfo[] resolvedArgs = new TypeInfo[vars.size()];
            boolean resolved = true;
            boolean changed = false;

            for (int i = 0; i < resolvedArgs.length; i++) {
                TypeInfo arg = actualArgs.get(i);
                TypeInfo resolvedArg = arg.resolve(context, includeBaseType);

                if (arg != resolvedArg) {
                    changed = true;
                }

                // 优化：如果resolved，以后就不必重新resolved。
                if (resolvedArg instanceof ParameterizedTypeImpl) {
                    resolved &= ((ParameterizedTypeImpl) resolvedArg).resolved;
                } else if (resolvedArg instanceof TypeVariableInfo) {
                    resolved = false;
                }

                resolvedArgs[i] = resolvedArg;
            }

            ParameterizedTypeImpl result = changed ? (ParameterizedTypeImpl) factory.getParameterizedType(rawType,
                    resolvedArgs) : this;

            if (resolved) {
                result.resolved = resolved;
            }

            return result;
        }
    }

    public List<TypeInfo> getActualTypeArguments() {
        return actualTypeArguments;
    }

    public TypeInfo getActualTypeArgument(String name) {
        List<TypeVariableInfo> vars = getTypeParameters();

        for (int i = 0; i < vars.size(); i++) {
            if (vars.get(i).getName().equals(name)) {
                return actualTypeArguments.get(i);
            }
        }

        return null;
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
     * 取得hash值。
     */
    @Override
    public int hashCode() {
        return getClass().hashCode() ^ rawType.hashCode() ^ actualTypeArguments.hashCode();
    }

    /**
     * 判断两个对象是否相同。
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (other == null || !other.getClass().equals(getClass())) {
            return false;
        }

        ParameterizedTypeImpl otherType = (ParameterizedTypeImpl) other;

        return rawType.equals(otherType.rawType) && actualTypeArguments.equals(otherType.actualTypeArguments);
    }

    /**
     * 取得字符串表示。
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(getSimpleClassName(getRawType()));
        buf.append("<");

        for (int i = 0; i < actualTypeArguments.size(); i++) {
            buf.append(rawType.getTypeParameters().get(i)).append("=").append(actualTypeArguments.get(i));

            if (i < actualTypeArguments.size() - 1) {
                buf.append(", ");
            }
        }

        buf.append(">");

        return buf.toString();
    }

    /**
     * 父类、接口的信息。
     */
    private static class Supertypes {
        private final List<TypeInfo> supertypes;
        private final List<TypeInfo> interfaces;
        private final List<TypeInfo> superclasses;

        private Supertypes(ParameterizedTypeImpl parameterizedType) {
            RawTypeInfo rawType = parameterizedType.rawType;
            List<TypeInfo> rawSupertypes = rawType.getSupertypes();
            List<TypeInfo> rawInterfaces = rawType.getInterfaces();
            List<TypeInfo> rawSuperclasses = rawType.getSuperclasses();

            List<TypeInfo> supertypes = createArrayList(rawSupertypes.size());
            List<TypeInfo> interfaces = createArrayList(rawInterfaces.size());
            List<TypeInfo> superclasses = createArrayList(rawSuperclasses.size());

            for (TypeInfo supertype : rawSupertypes) {
                if (supertype instanceof RawTypeInfo) {
                    if (supertype == rawType) {
                        supertypes.add(parameterizedType); // supertype的第一个就是自己（
                        // 类或接口）
                    } else {
                        supertypes.add(supertype);
                    }
                } else if (supertype instanceof ParameterizedTypeInfo) {
                    supertypes.add(supertype);
                } else {
                    unreachableCode("Unexpected super type: %s", supertype);
                }
            }

            // 创建lists
            for (TypeInfo supertype : supertypes) {
                if (supertype.getRawType().isInterface()) {
                    interfaces.add(supertype);
                } else {
                    superclasses.add(supertype);
                }
            }

            assertTrue(supertypes.size() == rawSupertypes.size(), "supertypes size");
            assertTrue(interfaces.size() == rawInterfaces.size(), "interfaces size");
            assertTrue(superclasses.size() == rawSuperclasses.size(), "superclasses size");

            this.supertypes = Collections.unmodifiableList(supertypes);
            this.interfaces = Collections.unmodifiableList(interfaces);
            this.superclasses = Collections.unmodifiableList(superclasses);
        }
    }

    /**
     * 创建supertypes的装载器。
     */
    private class SupertypesLoader implements Loader<Supertypes, Object> {
        public Supertypes load(Object context) {
            return new Supertypes(ParameterizedTypeImpl.this);
        }
    }
}
