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

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.util.List;

import com.alibaba.citrus.generictype.BoundedTypeInfo;
import com.alibaba.citrus.generictype.TypeInfo;

/**
 * 对{@link BoundedTypeInfo}的抽象实现。
 * 
 * @author Michael Zhou
 */
abstract class AbstractBoundedTypeInfo implements BoundedTypeInfo {
    private final static List<TypeInfo> EMPTY_BOUNDS = emptyList();
    private final TypeInfo baseType;
    private final List<TypeInfo> upperBounds;

    AbstractBoundedTypeInfo(TypeInfo[] upperBounds) {
        assertTrue(!isEmptyArray(upperBounds), "upperBounds is empty");

        this.upperBounds = unmodifiableList(asList(upperBounds));
        this.baseType = this.upperBounds.get(0);
    }

    public TypeInfo getBaseType() {
        return baseType;
    }

    public Class<?> getRawType() {
        return baseType.getRawType();
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isArray() {
        return baseType.isArray();
    }

    public boolean isInterface() {
        return baseType.isInterface();
    }

    public TypeInfo getPrimitiveWrapperType() {
        return baseType.getPrimitiveWrapperType();
    }

    public TypeInfo getComponentType() {
        return baseType.getComponentType();
    }

    public TypeInfo getDirectComponentType() {
        return baseType.getDirectComponentType();
    }

    public int getDimension() {
        return baseType.getDimension();
    }

    public List<TypeInfo> getInterfaces() {
        return baseType.getInterfaces();
    }

    public List<TypeInfo> getSuperclasses() {
        return baseType.getSuperclasses();
    }

    public List<TypeInfo> getSupertypes() {
        return baseType.getSupertypes();
    }

    public TypeInfo getSupertype(Class<?> equivalentClass) {
        return baseType.getSupertype(equivalentClass);
    }

    public List<TypeInfo> getUpperBounds() {
        return upperBounds;
    }

    public List<TypeInfo> getLowerBounds() {
        return EMPTY_BOUNDS;
    }

    /**
     * 取得hash值。
     */
    @Override
    public int hashCode() {
        return getClass().hashCode() ^ upperBounds.hashCode() ^ getLowerBounds().hashCode();
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

        AbstractBoundedTypeInfo otherType = (AbstractBoundedTypeInfo) other;

        return upperBounds.equals(otherType.upperBounds) && getLowerBounds().equals(otherType.getLowerBounds());
    }

    /**
     * 转换成字符串。
     */
    @Override
    public abstract String toString();
}
