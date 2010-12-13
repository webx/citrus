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

import static com.alibaba.citrus.codegen.util.TypeUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static java.lang.reflect.Modifier.*;
import static java.util.Collections.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alibaba.citrus.codegen.util.MethodSignature;
import com.alibaba.citrus.generictype.ClassTypeInfo;
import com.alibaba.citrus.generictype.GenericDeclarationInfo;
import com.alibaba.citrus.generictype.MethodInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.TypeVariableInfo;

/**
 * 对{@link MethodInfo}的实现。
 * 
 * @author Michael Zhou
 */
class MethodImpl extends AbstractGenericDeclarationInfo implements MethodInfo {
    private final static int MODIFIERS_MASK = PRIVATE | PUBLIC | PROTECTED | STATIC;
    private final MethodSignature signature;
    private final int modifiers;
    private ClassTypeInfo declaringType;
    private TypeInfo returnType;
    private List<TypeInfo> parameterTypes;
    private List<TypeInfo> exceptionTypes;
    private List<TypeInfo> effectiveExceptionTypes;

    MethodImpl(Method method) {
        super(method);

        this.signature = getMethodSignature(method);
        this.modifiers = method.getModifiers() & MODIFIERS_MASK;
    }

    MethodImpl(Constructor<?> constructor) {
        super(constructor);

        this.signature = getConstructorSignature(constructor);
        this.modifiers = constructor.getModifiers() & MODIFIERS_MASK;
    }

    void init(TypeVariableInfo[] vars, TypeInfo returnType, TypeInfo[] parameterTypes, TypeInfo[] exceptionTypes,
              ClassTypeInfo declaringType) {
        super.init(vars);

        this.declaringType = declaringType;
        this.returnType = returnType;
        this.parameterTypes = unmodifiableList(asList(parameterTypes));
        this.exceptionTypes = unmodifiableList(asList(exceptionTypes));
        this.effectiveExceptionTypes = getEffectiveExceptionTypes(exceptionTypes);
    }

    /**
     * 忽略unchecked exception：RuntimeException, Error，<br>
     * 排除异常的子类，例如：Exception和IOException同时出现，则删除IOException。
     */
    private List<TypeInfo> getEffectiveExceptionTypes(TypeInfo[] exceptionTypes) {
        ArrayList<TypeInfo> effectiveExceptions = createArrayList(exceptionTypes.length);

        for (TypeInfo exception : exceptionTypes) {
            if (RuntimeException.class.isAssignableFrom(exception.getRawType())
                    || Error.class.isAssignableFrom(exception.getRawType())) {
                continue;
            }

            for (Iterator<TypeInfo> j = effectiveExceptions.iterator(); j.hasNext();) {
                TypeInfo existing = j.next();

                if (exception.getRawType().isAssignableFrom(existing.getRawType())) {
                    j.remove();
                } else if (existing.getRawType().isAssignableFrom(exception.getRawType())) {
                    exception = null;
                    break;
                }
            }

            if (exception != null) {
                effectiveExceptions.add(exception);
            }
        }

        effectiveExceptions.trimToSize();

        return unmodifiableList(effectiveExceptions);
    }

    public boolean isConstructor() {
        return declaration instanceof Constructor<?>;
    }

    public Constructor<?> getConstructor() {
        if (isConstructor()) {
            return (Constructor<?>) declaration;
        }

        return null;
    }

    public Method getMethod() {
        if (!isConstructor()) {
            return (Method) declaration;
        }

        return null;
    }

    public TypeInfo getDeclaringType() {
        return declaringType;
    }

    public MethodSignature getSignature() {
        return signature;
    }

    public int getModifiers() {
        return modifiers;
    }

    public TypeInfo getReturnType() {
        return returnType;
    }

    public String getName() {
        return signature.getName();
    }

    public List<TypeInfo> getParameterTypes() {
        return parameterTypes;
    }

    public List<TypeInfo> getExceptionTypes() {
        return exceptionTypes;
    }

    public List<TypeInfo> getEffectiveExceptionTypes() {
        return effectiveExceptionTypes;
    }

    public MethodInfo resolve(GenericDeclarationInfo context) {
        return resolve(context, true);
    }

    public MethodInfo resolve(GenericDeclarationInfo context, boolean includeBaseType) {
        if (context == null) {
            context = declaringType;
        }

        boolean changed = false;

        TypeInfo[] parameterTypes = new TypeInfo[this.parameterTypes.size()];
        TypeInfo[] exceptionTypes = new TypeInfo[this.exceptionTypes.size()];

        changed |= resolveTypes(this.parameterTypes, parameterTypes, context, includeBaseType);
        changed |= resolveTypes(this.exceptionTypes, exceptionTypes, context, includeBaseType);

        TypeInfo returnType = this.returnType.resolve(context, includeBaseType);

        if (returnType != this.returnType) {
            changed = true;
        }

        MethodImpl resolvedMethod;

        if (changed) {
            if (isConstructor()) {
                resolvedMethod = new MethodImpl(getConstructor());
            } else {
                resolvedMethod = new MethodImpl(getMethod());
            }

            TypeVariableInfo[] vars = getTypeParameters().toArray(new TypeVariableInfo[getTypeParameters().size()]);

            resolvedMethod.init(vars, returnType, parameterTypes, exceptionTypes, declaringType);
        } else {
            resolvedMethod = this;
        }

        return resolvedMethod;
    }

    private boolean resolveTypes(List<TypeInfo> types, TypeInfo[] resolvedTypes, GenericDeclarationInfo context,
                                 boolean includeBaseType) {
        boolean changed = false;

        for (int i = 0; i < resolvedTypes.length; i++) {
            TypeInfo type = types.get(i);
            TypeInfo resolvedType = type.resolve(context, includeBaseType);

            if (type != resolvedType) {
                changed = true;
            }

            resolvedTypes[i] = resolvedType;
        }

        return changed;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        appendIfNotEmpty(buf, Modifier.toString(modifiers), " ");

        if (appendTypeParameters(buf) > 0) {
            buf.append(" ");
        }

        if (!isConstructor()) {
            buf.append(returnType).append(" ");
            buf.append(declaringType.getSimpleName());
            buf.append(".").append(getName());
        } else {
            buf.append(declaringType.getSimpleName());
        }

        buf.append("(");
        join(buf, parameterTypes, ", ");
        buf.append(")");

        if (!effectiveExceptionTypes.isEmpty()) {
            buf.append(" throws ");
            join(buf, effectiveExceptionTypes, ", ");
        }

        return buf.toString();
    }

    private void appendIfNotEmpty(StringBuilder buf, String str, String sep) {
        if (str.length() > 0) {
            buf.append(str).append(sep);
        }
    }
}
