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

import java.lang.reflect.GenericDeclaration;
import java.util.List;

import com.alibaba.citrus.generictype.GenericDeclarationInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.TypeVariableInfo;

/**
 * 对{@link GenericDeclarationInfo}的抽象实现。
 * 
 * @author Michael Zhou
 */
abstract class AbstractGenericDeclarationInfo implements GenericDeclarationInfo {
    protected final GenericDeclaration declaration;
    private List<TypeVariableInfo> parameters;

    AbstractGenericDeclarationInfo(GenericDeclaration declaration) {
        this.declaration = assertNotNull(declaration, "declaration");
    }

    void init(TypeVariableInfo[] vars) {
        this.parameters = unmodifiableList(asList(vars));
    }

    public boolean isGeneric() {
        return !parameters.isEmpty();
    }

    public List<TypeVariableInfo> getTypeParameters() {
        return parameters;
    }

    public List<TypeInfo> getActualTypeArguments() {
        List<TypeVariableInfo> vars = getTypeParameters();
        List<TypeInfo> actualArgs = createArrayList(vars.size());

        for (TypeVariableInfo var : vars) {
            actualArgs.add(TypeInfoFactory.findNonBoundedType(var));
        }

        return actualArgs;
    }

    public TypeInfo getActualTypeArgument(String name) {
        for (TypeVariableInfo var : getTypeParameters()) {
            if (var.getName().equals(name)) {
                return TypeInfoFactory.findNonBoundedType(var);
            }
        }

        return null;
    }

    /**
     * 取得hash值。
     */
    @Override
    public int hashCode() {
        return getClass().hashCode() ^ declaration.hashCode();
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

        AbstractGenericDeclarationInfo otherDecl = (AbstractGenericDeclarationInfo) other;

        return declaration.equals(otherDecl.declaration);
    }

    /**
     * 转换成字符串。
     */
    @Override
    public abstract String toString();

    /**
     * 取得参数表的字符串表示。
     */
    protected int appendTypeParameters(StringBuilder buf) {
        int length = buf.length();

        if (isGeneric()) {
            buf.append("<");
            join(buf, parameters, ", ");
            buf.append(">");
        }

        return buf.length() - length;
    }
}
