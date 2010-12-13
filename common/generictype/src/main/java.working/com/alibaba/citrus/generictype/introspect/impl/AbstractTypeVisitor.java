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

import static java.lang.reflect.Modifier.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.alibaba.citrus.generictype.TypeInfo;

/**
 * 抽象的{@link TypeVisitor}实现。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractTypeVisitor implements TypeVisitor {
    private TypeInfo type;

    public void visit() {
        type = null;
    }

    public void visitType(TypeInfo type) {
        if (this.type == null) {
            this.type = type;
        }
    }

    public void visitField(Field field) {
    }

    public void visitConstructor(Constructor<?> constructor) {
    }

    public void visitMethod(Method method) {
    }

    public void visitEnd() {
    }

    /**
     * 取得当前正在被访问的类型。
     */
    protected final TypeInfo getType() {
        return type;
    }

    /**
     * 取得访问限定符。
     * <p>
     * 可能的值为：<code>PUBLIC | PROTECTED | PRIVATE</code>。
     * </p>
     */
    protected final int getAccessQualifier(Member member) {
        return member.getModifiers() & (PUBLIC | PROTECTED | PRIVATE);
    }
}
