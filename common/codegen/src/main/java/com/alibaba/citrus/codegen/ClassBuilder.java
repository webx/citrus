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

import java.lang.reflect.Method;

import com.alibaba.citrus.asm.ClassVisitor;
import com.alibaba.citrus.asm.ClassWriter;
import com.alibaba.citrus.asm.Type;

/**
 * 用来生成一个类或接口的工具。
 * 
 * @author Michael Zhou
 */
public abstract class ClassBuilder {
    private final ClassWriter cw;
    private final ClassVisitor cv; // decorated cw
    private final boolean isInterface;
    private final String className;
    private final Type classType;
    private final Type superType;
    private final Type[] interfaceTypes;
    private Member lastMember;

    /**
     * 创建一个<code>ClassBuilder</code>。
     * 
     * @param cw <code>ClassWriter</code>对象。
     * @param access 访问性，如果是<code>-1</code>，则取默认值<code>public</code>。
     * @param isInterface 是否为接口。
     * @param className 要生成的类名
     * @param superclass 父类
     * @param interfaces 接口
     * @param classVersion 二进制版本，如果是<code>-1</code>，则取默认值。
     * @param source 源文件名，如果是<code>null</code>，则取默认值。
     */
    public ClassBuilder(ClassWriter cw, int access, boolean isInterface, String className, Class<?> superclass,
                        Class<?>[] interfaces, int classVersion, String source) {
        // class writer/visitor
        this.cw = cw;
        this.cv = decorate(cw);

        // access
        if (access < 0) {
            access = ACC_PUBLIC; // 默认值
        }

        access |= ACC_SUPER; // for backward-compatibility

        if (isInterface) {
            access |= ACC_ABSTRACT | ACC_INTERFACE;
        }

        // isInterface
        this.isInterface = isInterface;

        // className
        this.className = assertNotNull(className, "className");
        this.classType = getTypeFromClassName(className);

        // superclass
        if (superclass == null) {
            this.superType = OBJECT_TYPE;
        } else {
            this.superType = getTypeFromClass(superclass);
        }

        // interfaces
        this.interfaceTypes = getTypes(interfaces);

        // classVersion
        if (classVersion < 0) {
            classVersion = DEFAULT_CLASS_VERSION;
        }

        // source
        if (source == null) {
            source = DEFAULT_SOURCE;
        }

        cv.visit(classVersion, access, classType.getInternalName(), null, superType.getInternalName(),
                getInternalNames(interfaceTypes));

        if (source != null) {
            cv.visitSource(source, null);
        }
    }

    /**
     * 取得<code>ClassVisitor</code>。
     */
    public ClassVisitor getClassVisitor() {
        return cv;
    }

    /**
     * 取得当前的类名。
     */
    public String getClassName() {
        return className;
    }

    /**
     * 判断是否为接口。
     */
    public boolean isInterface() {
        return isInterface;
    }

    /**
     * 取得当前的类型信息。
     */
    public Type getType() {
        return classType;
    }

    /**
     * 取得父类的类型信息。
     */
    public Type getSuperType() {
        return superType;
    }

    /**
     * 创建一个public常量field。
     */
    public FieldBuilder addConstantField(Class<?> fieldType, String fieldName, Object value) {
        return addField(ACC_PUBLIC | ACC_CONSTANT, fieldType, fieldName, value);
    }

    /**
     * 创建一个常量field。
     */
    public FieldBuilder addConstantField(int access, Class<?> fieldType, String fieldName, Object value) {
        return addField(access | ACC_CONSTANT, fieldType, fieldName, value);
    }

    /**
     * 创建一个private field。
     */
    public FieldBuilder addField(Class<?> fieldType, String fieldName, Object value) {
        return addField(-1, fieldType, fieldName, value);
    }

    /**
     * 创建一个field。
     */
    public FieldBuilder addField(int access, Class<?> fieldType, String fieldName, Object value) {
        FieldBuilder fb = setMember(new FieldMember(this, access, fieldType, fieldName, value));

        return fb;
    }

    /**
     * 创建public构造函数。
     */
    public MethodBuilder addConstructor(Class<?>[] parameterTypes, Class<?>[] exceptionTypes) {
        return addMethod(-1, null, CONSTRUCTOR_NAME, parameterTypes, exceptionTypes);
    }

    /**
     * 创建构造函数。
     */
    public MethodBuilder addConstructor(int access, Class<?>[] parameterTypes, Class<?>[] exceptionTypes) {
        return addMethod(access, null, CONSTRUCTOR_NAME, parameterTypes, exceptionTypes);
    }

    /**
     * 创建静态构造函数。
     */
    public MethodBuilder addStaticConstructor() {
        return addMethod(-1, null, STATIC_CONSTRUCTOR_NAME, null, null);
    }

    /**
     * 创建public方法。
     */
    public MethodBuilder addMethod(Method method) {
        return addMethod(-1, method.getReturnType(), method.getName(), method.getParameterTypes(),
                method.getExceptionTypes());
    }

    /**
     * 创建public方法。
     */
    public MethodBuilder addMethod(Class<?> returnType, String methodName, Class<?>[] parameterTypes,
                                   Class<?>[] exceptionTypes) {
        return addMethod(-1, returnType, methodName, parameterTypes, exceptionTypes);
    }

    /**
     * 创建方法。
     */
    public MethodBuilder addMethod(int access, Class<?> returnType, String methodName, Class<?>[] parameterTypes,
                                   Class<?>[] exceptionTypes) {
        MethodBuilder mb = setMember(new MethodMember(this, access, returnType, methodName, parameterTypes,
                exceptionTypes));

        return mb;
    }

    /**
     * 生成class。
     */
    public final Class<?> toClass() {
        setMember(null);
        cv.visitEnd();
        byte[] bytes = cw.toByteArray();

        return defineClass(getClassName(), bytes);
    }

    /**
     * 给子类一个机会包装class writer。
     */
    protected ClassVisitor decorate(ClassVisitor cv) {
        return cv;
    }

    /**
     * 定义并装载类。
     */
    protected abstract Class<?> defineClass(String className, byte[] bytes);

    /**
     * 包括<code>end</code>方法的class成员。
     */
    private interface Member {
        void end();
    }

    /**
     * 结束前一个成员，开始新的成员。
     */
    private <M extends Member> M setMember(M member) {
        if (lastMember != null) {
            lastMember.end();
        }

        lastMember = member;

        return member;
    }

    /**
     * 可自动结束的<code>FieldBuilder</code>。
     */
    private class FieldMember extends FieldBuilder implements Member {
        protected FieldMember(ClassBuilder cb, int access, Class<?> fieldType, String fieldName, Object value) {
            super(cb, access, fieldType, fieldName, value);
        }

        public void end() {
            endField();
        }
    }

    /**
     * 可自动结束的<code>MethodBuilder</code>。
     */
    private class MethodMember extends MethodBuilder implements Member {
        protected MethodMember(ClassBuilder cb, int access, Class<?> returnType, String methodName,
                               Class<?>[] parameterTypes, Class<?>[] exceptionTypes) {
            super(cb, access, returnType, methodName, parameterTypes, exceptionTypes);
        }

        public void end() {
            endMethod();
        }
    }
}
