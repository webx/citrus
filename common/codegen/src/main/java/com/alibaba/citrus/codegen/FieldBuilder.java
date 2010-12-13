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

import com.alibaba.citrus.asm.AnnotationVisitor;
import com.alibaba.citrus.asm.Attribute;
import com.alibaba.citrus.asm.FieldVisitor;
import com.alibaba.citrus.asm.Type;

/**
 * 用来生成一个field的工具。
 * 
 * @author Michael Zhou
 */
public abstract class FieldBuilder {
    private final ClassBuilder cb;
    private final FieldVisitor fv;
    private final boolean isConstant;
    private final Type fieldType;
    private final String fieldName;

    /**
     * 创建一个<code>FieldBuilder</code>。
     * 
     * @param cb field所在的<code>ClassBuilder</code>对象
     * @param access 访问性，如果是<code>-1</code>，则取默认值。
     * @param fieldType field类型
     * @param fieldName field名称
     * @param value 字段值
     */
    protected FieldBuilder(ClassBuilder cb, int access, Class<?> fieldType, String fieldName, Object value) {
        // class builder
        this.cb = cb;

        // access
        if (access < 0) { // 默认值
            if (cb.isInterface()) {
                access = ACC_PUBLIC | ACC_CONSTANT; // 对interface而言，必须为： public final static
            } else {
                access = ACC_PRIVATE; // 对于class而言，默认为： private
            }
        }

        // isConstant
        this.isConstant = testBits(access, ACC_CONSTANT);

        // fieldType
        this.fieldType = getTypeFromClass(assertNotNull(fieldType, "fieldClass"));

        // fieldName
        this.fieldName = assertNotNull(fieldName, "fieldName");

        // visit field
        this.fv = decorate(cb.getClassVisitor().visitField(access, fieldName, this.fieldType.getDescriptor(), null,
                value));
    }

    /**
     * 取得field所在的<code>ClassBuilder</code>。
     */
    public ClassBuilder getClassBuilder() {
        return cb;
    }

    /**
     * 取得<code>FieldVisitor</code>。
     */
    public FieldVisitor getFieldVisitor() {
        return fv;
    }

    /**
     * 是否为常量。
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * 取得field类型。
     */
    public Type getFieldType() {
        return fieldType;
    }

    /**
     * 取得field名称。
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * 实现接口<code>FieldVisitor</code>。
     */
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return fv.visitAnnotation(desc, visible);
    }

    /**
     * 实现接口<code>FieldVisitor</code>。
     */
    public void visitAttribute(Attribute attr) {
        fv.visitAttribute(attr);
    }

    /**
     * 结束field。
     */
    protected void endField() {
        fv.visitEnd();
    }

    /**
     * 给子类一个机会包装field visitor。
     */
    protected FieldVisitor decorate(FieldVisitor fv) {
        return fv;
    }
}
