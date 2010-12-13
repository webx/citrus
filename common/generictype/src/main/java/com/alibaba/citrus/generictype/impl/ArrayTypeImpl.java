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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.citrus.generictype.ArrayTypeInfo;
import com.alibaba.citrus.generictype.GenericDeclarationInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.util.ClassUtil;
import com.alibaba.citrus.util.internal.LazyLoader;
import com.alibaba.citrus.util.internal.LazyLoader.Loader;

/**
 * 对{@link ArrayTypeInfo}的实现。
 * 
 * @author Michael Zhou
 */
class ArrayTypeImpl implements ArrayTypeInfo {
    private final Class<?> rawType;
    private final TypeInfo componentType;
    private final TypeInfo directComponentType;
    private final int dimension;
    private final LazyLoader<Supertypes, Object> supertypesLoader;

    ArrayTypeImpl(TypeInfo componentType, TypeInfo directComponentType, int dimension, Class<?> rawType) {
        this.componentType = assertNotNull(componentType, "componentType");
        this.directComponentType = assertNotNull(directComponentType, "directComponentType");
        this.dimension = dimension;

        assertTrue(dimension > 0, "dimension: %d", dimension);

        if (rawType == null) {
            this.rawType = ClassUtil.getArrayClass(componentType.getRawType(), dimension);
        } else {
            this.rawType = rawType;
        }

        this.supertypesLoader = LazyLoader.getDefault(new SupertypesLoader());
    }

    public Class<?> getRawType() {
        return rawType;
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
        return true;
    }

    public boolean isInterface() {
        return false;
    }

    public TypeInfo getPrimitiveWrapperType() {
        return this;
    }

    public TypeInfo getComponentType() {
        return componentType;
    }

    public TypeInfo getDirectComponentType() {
        return directComponentType;
    }

    public int getDimension() {
        return dimension;
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
    public TypeInfo resolve(GenericDeclarationInfo context) {
        return resolve(context, true);
    }

    // Implementation of TypeInfo.resolve
    public TypeInfo resolve(GenericDeclarationInfo context, boolean includeBaseType) {
        TypeInfo resolvedComponentType = componentType.resolve(context, includeBaseType);

        if (resolvedComponentType == componentType) {
            return this;
        } else {
            return factory.getArrayType(resolvedComponentType, dimension);
        }
    }

    /**
     * 取得hash值。
     */
    @Override
    public int hashCode() {
        return getClass().hashCode() ^ componentType.hashCode() ^ dimension;
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

        ArrayTypeImpl otherType = (ArrayTypeImpl) other;

        return dimension == otherType.dimension && componentType.equals(otherType.componentType);
    }

    /**
     * 取得字符串表示。
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(getComponentType());

        for (int i = 0; i < dimension; i++) {
            buf.append("[]");
        }

        return buf.toString();
    }

    /**
     * 父类、接口的信息。
     */
    private static class Supertypes {
        private static final TypeInfo[] ARRAY_SUPERTYPES;
        private final List<TypeInfo> supertypes;
        private final List<TypeInfo> interfaces;
        private final List<TypeInfo> superclasses;

        static {
            TypeInfo[] interfaces = factory.getTypes(Object[].class.getInterfaces());
            TypeInfo[] arraySupertypes = new TypeInfo[interfaces.length + 1];

            System.arraycopy(interfaces, 0, arraySupertypes, 0, interfaces.length);
            arraySupertypes[interfaces.length] = TypeInfo.OBJECT;

            ARRAY_SUPERTYPES = arraySupertypes;
        }

        private Supertypes(ArrayTypeImpl arrayType) {
            TypeInfo componentType = arrayType.componentType;
            List<TypeInfo> componentSupertypes = componentType.getSupertypes();
            ArrayList<TypeInfo> supertypes = createArrayList(componentSupertypes.size() * 2);
            ArrayList<TypeInfo> interfaces = createArrayList();
            ArrayList<TypeInfo> superclasses = createArrayList();

            for (TypeInfo componentSupertype : componentSupertypes) {
                supertypes.add(factory.getArrayType(componentSupertype, arrayType.dimension));
            }

            for (int dim = arrayType.dimension - 1; dim > 0; dim--) {
                for (TypeInfo arraySupertype : ARRAY_SUPERTYPES) {
                    supertypes.add(factory.getArrayType(arraySupertype, dim));
                }
            }

            for (TypeInfo arraySupertype : ARRAY_SUPERTYPES) {
                supertypes.add(arraySupertype);
            }

            // 创建lists
            for (TypeInfo supertype : supertypes) {
                if (supertype.getRawType().isInterface()) {
                    interfaces.add(supertype);
                } else {
                    superclasses.add(supertype);
                }
            }

            supertypes.trimToSize();
            interfaces.trimToSize();
            superclasses.trimToSize();

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
            return new Supertypes(ArrayTypeImpl.this);
        }
    }
}
