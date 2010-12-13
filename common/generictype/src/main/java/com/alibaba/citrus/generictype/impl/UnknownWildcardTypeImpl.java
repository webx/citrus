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
import static java.util.Collections.*;

import java.util.List;

import com.alibaba.citrus.generictype.GenericDeclarationInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.WildcardTypeInfo;

/**
 * 用来包装unknown wildcard类型的包装器，返回对应<code>TypeVariable</code>的upper bounds。
 */
class UnknownWildcardTypeImpl implements WildcardTypeInfo {
    private final WildcardTypeInfo wildcard;
    private final List<TypeInfo> upperBounds;
    private final TypeInfo baseType;

    UnknownWildcardTypeImpl(WildcardTypeInfo wildcard, TypeInfo[] upperBounds) {
        if (wildcard instanceof UnknownWildcardTypeImpl) {
            this.wildcard = ((UnknownWildcardTypeImpl) wildcard).wildcard;
        } else {
            this.wildcard = wildcard;
        }

        this.upperBounds = unmodifiableList(asList(upperBounds));

        assertTrue(!this.upperBounds.isEmpty(), "upperBounds is empty");

        this.baseType = this.upperBounds.get(0);
    }

    public String getName() {
        return wildcard.getName();
    }

    public String getSimpleName() {
        return wildcard.getSimpleName();
    }

    public boolean isUnknown() {
        return wildcard.isUnknown();
    }

    public TypeInfo resolve(GenericDeclarationInfo context) {
        return baseType.resolve(context);
    }

    public TypeInfo resolve(GenericDeclarationInfo context, boolean includeBaseType) {
        return baseType.resolve(context, includeBaseType);
    }

    public TypeInfo getBaseType() {
        return baseType;
    }

    public Class<?> getRawType() {
        return baseType.getRawType();
    }

    public boolean isPrimitive() {
        return baseType.isPrimitive();
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
        return wildcard.getLowerBounds();
    }

    /**
     * 取得hash值。
     */
    @Override
    public int hashCode() {
        return wildcard.hashCode();
        //return getClass().hashCode() ^ getUpperBounds().hashCode() ^ getLowerBounds().hashCode();
    }

    /**
     * 判断两个对象是否相同。
     */
    @Override
    public boolean equals(Object other) {
        return wildcard.equals(other);
        //            if (other == this) {
        //                return true;
        //            }
        //
        //            if (other == null || !other.getClass().equals(getClass())) {
        //                return false;
        //            }
        //
        //            WildcardTypeInfo otherType = (WildcardTypeInfo) other;
        //
        //            return getUpperBounds().equals(otherType.getUpperBounds())
        //                    && getLowerBounds().equals(otherType.getLowerBounds());
    }

    /**
     * 取得字符串表示。
     */
    @Override
    public String toString() {
        return wildcard.toString();
    }
}
