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

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.generictype.MethodInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.introspect.PropertyInfo;
import com.alibaba.citrus.util.internal.StringUtil;

/**
 * 对{@link PropertyInfo}的抽象实现。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractPropertyInfo implements PropertyInfo {
    private final String name;
    private final TypeInfo declaringType;
    private final TypeInfo type;
    protected MethodInfo readMethod;
    protected MethodInfo writeMethod;

    protected AbstractPropertyInfo(String name, TypeInfo declaringType, TypeInfo type, MethodInfo readMethod,
                                   MethodInfo writeMethod) {
        this.name = StringUtil.trimToEmpty(name);
        this.declaringType = assertNotNull(declaringType, "declaringType");
        this.type = assertNotNull(type, "type");
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
    }

    public String getName() {
        return name;
    }

    public TypeInfo getDeclaringType() {
        return declaringType;
    }

    public TypeInfo getType() {
        return type;
    }

    public boolean isReadable() {
        return getReadMethod() != null;
    }

    public boolean isWritable() {
        return getWriteMethod() != null;
    }

    public MethodInfo getReadMethod() {
        return readMethod;
    }

    public MethodInfo getWriteMethod() {
        return writeMethod;
    }

    /**
     * 检查对象的类型，确保其为{@link getDeclaringType()}的子类。
     */
    protected final Object checkType(Object object) {
        assertTrue(getDeclaringType().getRawType().isInstance(object));

        return object;
    }

    /**
     * 比较两个property信息是否全等。
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (other == null || !other.getClass().equals(getClass())) {
            return false;
        }

        PropertyInfo o = (PropertyInfo) other;
        EqualsBuilder equals = new EqualsBuilder();

        equals.append(getName(), o.getName());
        equals.append(getDeclaringType(), o.getDeclaringType());
        equals.append(getType(), o.getType());
        equals.append(getReadMethod(), o.getReadMethod());
        equals.append(getWriteMethod(), o.getWriteMethod());

        return equals.isEquals();
    }

    /**
     * 计算hash值。
     */
    @Override
    public int hashCode() {
        HashCodeBuilder hash = new HashCodeBuilder();

        hash.append(getName());
        hash.append(getDeclaringType());
        hash.append(getType());
        hash.append(getReadMethod());
        hash.append(getWriteMethod());

        return hash.toHashCode();
    }

    /**
     * 转换成字符串。
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        MethodInfo reader = getReadMethod();
        MethodInfo writer = getWriteMethod();

        buf.append(getDescription()).append(" {\n");
        buf.append("  declaringType = ").append(getDeclaringType()).append("\n");
        buf.append("  type          = ").append(getType()).append("\n");
        buf.append("  name          = ").append(getName()).append("\n");

        buf.append("  readable      = ");

        if (isReadable()) {
            buf.append("yes");
            buf.append(", method = ").append(reader == null ? "N/A" : reader).append("\n");
        } else {
            buf.append("no\n");
        }

        buf.append("  writable      = ");

        if (isWritable()) {
            buf.append("yes");
            buf.append(", method = ").append(writer == null ? "N/A" : writer).append("\n");
        } else {
            buf.append("no\n");
        }

        buf.append("}");

        return buf.toString();
    }

    protected abstract String getDescription();
}
