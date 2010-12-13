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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.introspect.Introspector;

/**
 * 用来分析一个类型，被{@link Introspector}调用。
 * <p>
 * Visitor中的方法调用顺序如下：
 * </p>
 * 
 * <pre>
 * visit
 * ( visitType
 *   visitField*
 *   visitConstructor*
 *   visitMethod*
 * )+
 * visitEnd
 * </pre>
 * 
 * @author Michael Zhou
 */
public interface TypeVisitor {
    void visit();

    void visitType(TypeInfo type);

    void visitField(Field field);

    void visitConstructor(Constructor<?> constructor);

    void visitMethod(Method method);

    void visitEnd();
}
