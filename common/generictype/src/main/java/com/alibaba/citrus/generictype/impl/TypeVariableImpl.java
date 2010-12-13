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

import com.alibaba.citrus.generictype.GenericDeclarationInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.TypeVariableInfo;

/**
 * 对{@link TypeVariableInfo}的实现。
 * 
 * @author Michael Zhou
 */
class TypeVariableImpl extends AbstractBoundedTypeInfo implements TypeVariableInfo {
    private final String name;
    private final GenericDeclarationInfo declaration;

    TypeVariableImpl(String name, GenericDeclarationInfo declaration, TypeInfo[] upperBounds) {
        super(upperBounds);

        this.name = assertNotNull(name, "name");
        this.declaration = assertNotNull(declaration, "declaration");
    }

    public String getName() {
        return name;
    }

    public String getSimpleName() {
        return name;
    }

    public GenericDeclarationInfo getGenericDeclaration() {
        return declaration;
    }

    // Implementation of TypeInfo.resolve
    public TypeInfo resolve(GenericDeclarationInfo context) {
        return resolve(context, true);
    }

    // Implementation of TypeInfo.resolve
    public TypeInfo resolve(GenericDeclarationInfo context, boolean includeBaseType) {
        if (context == null) {
            context = declaration;
        }

        return TypeInfoFactory.resolveTypeVariable(this, context, includeBaseType);
    }

    /**
     * 取得hash值。
     */
    @Override
    public int hashCode() {
        return getClass().hashCode() ^ name.hashCode() ^ declaration.hashCode();
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

        TypeVariableImpl otherType = (TypeVariableImpl) other;

        return name.equals(otherType.name) && declaration.equals(otherType.declaration);
    }

    /**
     * 取得字符串表示。
     */
    @Override
    public String toString() {
        return name;
    }
}
