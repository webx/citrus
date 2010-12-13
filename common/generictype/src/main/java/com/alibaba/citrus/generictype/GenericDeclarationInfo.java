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

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 和{@link GenericDeclaration}对应，代表包含类型变量的声明。包括下列子类：
 * <ul>
 * <li>{@link ClassTypeInfo}，代表{@link Class}</li>
 * <li>{@link MethodInfo}，代表{@link Method}和{@link Constructor}</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public interface GenericDeclarationInfo {
    /**
     * 判断当前声明是否为generic声明，亦即，是否包含类型变量如：<code>List&lt;E&gt;</code>。
     */
    boolean isGeneric();

    /**
     * 取得类型参数表，
     * <p>
     * 例如，<code>Map&lt;K, V&gt;</code>的参数表为<code>[K, V]</code>；<br>
     * 如果不是generic类型，则返回空列表。
     * </p>
     */
    List<TypeVariableInfo> getTypeParameters();

    /**
     * 取得类型参数所对应的实际类型。
     * <p>
     * 例如，{@link RawTypeInfo}：<code>MyClass&lt;A extends Number, B&gt;</code>
     * 对应的实际参数类型为 <code>[Number, Object]</code>；<br>
     * {@link ParameterizedTypeInfo}：<code>List&lt;E=Integer&gt;</code>
     * 对应的实际参数类型为<code>[Integer]</code>。
     * </p>
     * <p>
     * 如果不是generic类型，则返回空列表。
     * </p>
     */
    List<TypeInfo> getActualTypeArguments();

    /**
     * 取得指定参数名称的实际类型。
     * <p>
     * 例如，{@link RawTypeInfo}：<code>MyClass&lt;A extends Number, B&gt;</code>
     * 变量名<code>A</code>对应的实际参数类型为 <code>Number</code>；<br>
     * {@link ParameterizedTypeInfo}：<code>List&lt;E=Integer&gt;</code> 变量
     * <code>E</code>对应的实际参数类型为<code>Integer</code>。
     * </p>
     * <p>
     * 如果变量名不存在，则返回<code>null</code>。
     * </p>
     */
    TypeInfo getActualTypeArgument(String name);
}
