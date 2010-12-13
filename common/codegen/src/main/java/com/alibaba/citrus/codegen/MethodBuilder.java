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
package com.alibaba.citrus.codegen;

import static com.alibaba.citrus.asm.Opcodes.*;
import static com.alibaba.citrus.codegen.util.CodegenConstant.*;
import static com.alibaba.citrus.codegen.util.TypeUtil.*;
import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.asm.MethodVisitor;
import com.alibaba.citrus.asm.Type;

/**
 * 用来生成一个method的工具。
 * 
 * @author Michael Zhou
 */
public abstract class MethodBuilder {
    private final ClassBuilder cb;
    private final MethodVisitor mv;
    private final int access;
    private final String methodName;
    private final Type returnType;
    private final Type[] parameterTypes;
    private final Type[] exceptionTypes;
    private final String methodDesc;
    private CodeBuilder codeBuilder;

    /**
     * 创建<code>MethodBuilder</code>。
     * 
     * @param cb method所在的<code>ClassBuilder</code>对象
     * @param access 访问性，如果是<code>-1</code>，则取默认值。
     * @param returnType 返回类型，为<code>null</code>代表无返回值。
     * @param methodName method名称
     * @param parameterTypes 参数类型
     * @param exceptionTypes 异常类型
     */
    protected MethodBuilder(ClassBuilder cb, int access, Class<?> returnType, String methodName,
                            Class<?>[] parameterTypes, Class<?>[] exceptionTypes) {
        // class builder
        this.cb = cb;

        // methodName
        this.methodName = assertNotNull(methodName, "methodName");

        // access
        if (access < 0) {
            if (isStaticConstructor()) {
                access = ACC_STATIC;
            } else {
                access = ACC_PUBLIC;
            }
        }

        this.access = access;

        // method desc
        this.returnType = getTypeFromClass(returnType == null ? void.class : returnType);
        this.parameterTypes = getTypes(parameterTypes);
        this.methodDesc = Type.getMethodDescriptor(this.returnType, this.parameterTypes);

        // exceptions
        this.exceptionTypes = getTypes(exceptionTypes);

        // visit method
        this.mv = decorate(cb.getClassVisitor().visitMethod(access, this.methodName, this.methodDesc, null,
                getInternalNames(this.exceptionTypes)));
    }

    /**
     * 取得method所在的<code>ClassBuilder</code>。
     */
    public ClassBuilder getClassBuilder() {
        return cb;
    }

    /**
     * 取得<code>MethodVisitor</code>。
     */
    public MethodVisitor getMethodVisitor() {
        return mv;
    }

    /**
     * 取得返回类型。
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * 取得方法名。
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 是否为构造函数。
     */
    public boolean isConstructor() {
        return CONSTRUCTOR_NAME.equals(methodName);
    }

    /**
     * 是否为静态构造函数。
     */
    public boolean isStaticConstructor() {
        return STATIC_CONSTRUCTOR_NAME.equals(methodName);
    }

    /**
     * 取得参数类型。
     */
    public Type[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * 取得异常类型。
     */
    public Type[] getExceptionTypes() {
        return exceptionTypes;
    }

    /**
     * 开始代码。
     */
    public CodeBuilder startCode() {
        if (codeBuilder == null) {
            codeBuilder = new CodeBuilder(this, mv, access, methodName, methodDesc);
        }

        return codeBuilder;
    }

    /**
     * 结束方法。
     */
    protected void endMethod() {
        if (codeBuilder == null) {
            mv.visitEnd(); // 对于interface和抽象方法
        } else {
            codeBuilder.visitMaxs(1, 1); // auto calculate stacks and locals
            codeBuilder.visitEnd();
        }
    }

    /**
     * 给子类一个机会包装method visitor。
     */
    protected MethodVisitor decorate(MethodVisitor mv) {
        return mv;
    }
}
