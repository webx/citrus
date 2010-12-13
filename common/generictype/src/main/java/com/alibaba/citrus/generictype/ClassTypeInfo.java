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

/**
 * 代表一个基于{@link Class}的类型。包含以下子类：
 * <ul>
 * <li>{@link RawTypeInfo}</li>
 * <li>{@link ParameterizedTypeInfo}</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public interface ClassTypeInfo extends TypeInfo, GenericDeclarationInfo {
    /**
     * 在指定上下文中分析实际类型。
     * <p>
     * 注：{@link ClassTypeInfo.resolve()}返回{@link TypeInfo}的子类
     * {@link ClassTypeInfo}。
     * </p>
     */
    ClassTypeInfo resolve(GenericDeclarationInfo context);

    /**
     * 在指定上下文中分析实际类型。
     * <p>
     * 注：{@link ClassTypeInfo.resolve()}返回{@link TypeInfo}的子类
     * {@link ClassTypeInfo}。
     * </p>
     */
    ClassTypeInfo resolve(GenericDeclarationInfo context, boolean includeBaseType);

    /**
     * 取得指定名称的字段。
     */
    FieldInfo getField(String name);

    /**
     * 取得指定类型中的指定名称的字段。
     * <p>
     * 指定类型必须为当前类型或其父类。
     * </p>
     */
    FieldInfo getField(ClassTypeInfo declaringType, String name);

    /**
     * 取得指定参数表对应的构造函数。
     */
    MethodInfo getConstructor(Class<?>... paramTypes);

    /**
     * 取得指定名称和参数表对应的方法。
     */
    MethodInfo getMethod(String methodName, Class<?>... paramTypes);
}
