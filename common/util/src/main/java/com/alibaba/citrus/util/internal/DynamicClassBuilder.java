/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.util.internal;

import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.lang.reflect.Modifier.*;

import java.lang.reflect.Method;

import com.alibaba.citrus.util.ClassLoaderUtil;
import net.sf.cglib.asm.Type;
import net.sf.cglib.core.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class DynamicClassBuilder {
    private final static int    PUBLIC_STATIC_MODIFIERS = PUBLIC | STATIC;
    protected final      Logger log                     = LoggerFactory.getLogger(getClass());
    private final ClassLoader classLoader;

    public DynamicClassBuilder() {
        this(null);
    }

    public DynamicClassBuilder(ClassLoader cl) {
        this.classLoader = cl;
    }

    public ClassLoader getClassLoader() {
        return classLoader == null ? ClassLoaderUtil.getContextClassLoader() : classLoader;
    }

    protected Signature getSignature(Method method, String rename) {
        String name = defaultIfNull(trimToNull(rename), method.getName());
        Type returnType = Type.getType(method.getReturnType());
        Type[] paramTypes = Type.getArgumentTypes(method);

        return new Signature(name, returnType, paramTypes);
    }

    protected boolean isPublicStatic(Method method) {
        return (method.getModifiers() & PUBLIC_STATIC_MODIFIERS) == PUBLIC_STATIC_MODIFIERS;
    }

    protected boolean isEqualsMethod(Method method) {
        if (!"equals".equals(method.getName())) {
            return false;
        }

        Class<?>[] paramTypes = method.getParameterTypes();

        return paramTypes.length == 1 && paramTypes[0] == Object.class;
    }

    protected boolean isHashCodeMethod(Method method) {
        return "hashCode".equals(method.getName()) && method.getParameterTypes().length == 0;
    }

    protected boolean isToStringMethod(Method method) {
        return "toString".equals(method.getName()) && method.getParameterTypes().length == 0;
    }
}
