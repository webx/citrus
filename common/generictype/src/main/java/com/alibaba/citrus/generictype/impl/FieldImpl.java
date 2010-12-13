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
import static java.lang.reflect.Modifier.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.alibaba.citrus.generictype.ClassTypeInfo;
import com.alibaba.citrus.generictype.FieldInfo;
import com.alibaba.citrus.generictype.GenericDeclarationInfo;
import com.alibaba.citrus.generictype.TypeInfo;

/**
 * 对{@link FieldInfo}的实现。
 * 
 * @author Michael Zhou
 */
public class FieldImpl implements FieldInfo {
    private final static int MODIFIERS_MASK = PRIVATE | PUBLIC | PROTECTED | STATIC;
    private final int modifiers;
    private final Field field;
    private final ClassTypeInfo declaringType;
    private final TypeInfo type;

    FieldImpl(Field field, ClassTypeInfo declaringType, TypeInfo type) {
        this.field = assertNotNull(field, "field");
        this.modifiers = field.getModifiers() & MODIFIERS_MASK;
        this.declaringType = declaringType;
        this.type = type;
    }

    public Field getField() {
        return field;
    }

    public TypeInfo getDeclaringType() {
        return declaringType;
    }

    public int getModifiers() {
        return modifiers;
    }

    public TypeInfo getType() {
        return type;
    }

    public String getName() {
        return field.getName();
    }

    public FieldInfo resolve(GenericDeclarationInfo context) {
        return resolve(context, true);
    }

    public FieldInfo resolve(GenericDeclarationInfo context, boolean includeBaseType) {
        TypeInfo resolvedType = type.resolve(context, includeBaseType);

        if (type != resolvedType) {
            return new FieldImpl(field, declaringType, resolvedType);
        } else {
            return this;
        }
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() ^ field.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (other == null || !other.getClass().equals(getClass())) {
            return false;
        }

        FieldImpl otherField = (FieldImpl) other;

        return field.equals(otherField.field);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        String mod = Modifier.toString(modifiers);

        if (mod.length() > 0) {
            buf.append(mod).append(" ");
        }

        buf.append(type).append(" ");
        buf.append(declaringType.getSimpleName()).append(".").append(getName());

        return buf.toString();
    }
}
