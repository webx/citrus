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
package com.alibaba.citrus.generictype;

import java.lang.reflect.Field;

/**
 * 代表一个{@link Field}字段的信息。
 * 
 * @author Michael Zhou
 */
public interface FieldInfo {
    /**
     * 取得字段。
     */
    Field getField();

    /**
     * 取得字段所在的类型。
     */
    TypeInfo getDeclaringType();

    /**
     * 取得字段的访问修饰符。
     */
    int getModifiers();

    /**
     * 取得字段类型。
     */
    TypeInfo getType();

    /**
     * 取得字段的名称。
     */
    String getName();

    /**
     * 在指定上下文中分析字段的实际类型。
     * <p>
     * 相当于{@link resolve(context, true)}。
     * </p>
     */
    FieldInfo resolve(GenericDeclarationInfo context);

    /**
     * 在指定上下文中分析字段的实际类型。
     * <p>
     * 如果<code>includeBaseType==false</code>，那么解析类型变量时，将不会取得其baseType。
     * </p>
     * <p>
     * 参见：{@link TypeInfo.resolve()}。
     * </p>
     */
    FieldInfo resolve(GenericDeclarationInfo context, boolean includeBaseType);
}
