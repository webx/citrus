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
import java.lang.reflect.Method;
import java.util.List;

import com.alibaba.citrus.codegen.util.MethodSignature;

/**
 * 代表一个{@link Method}或{@link Constructor}的信息。
 * 
 * @author Michael Zhou
 */
public interface MethodInfo extends GenericDeclarationInfo {
    /**
     * 是否为构造函数？
     */
    boolean isConstructor();

    /**
     * 取得方法，如果不是方法，则返回<code>null</code>。
     */
    Method getMethod();

    /**
     * 取得构造函数，如果不是构造函数，则返回<code>null</code>。
     */
    Constructor<?> getConstructor();

    /**
     * 取得当前方法所在的类型。
     */
    TypeInfo getDeclaringType();

    /**
     * 取得当前方法或构造函数的签名。
     * <p>
     * 签名只包含方法的名称和参数信息。
     * </p>
     */
    MethodSignature getSignature();

    /**
     * 取得方法或构造函数的访问修饰符。
     */
    int getModifiers();

    /**
     * 取得返回类型。
     */
    TypeInfo getReturnType();

    /**
     * 取得方法的名称，对于构造函数，则返回<code>&lt;init&gt;</code>。
     */
    String getName();

    /**
     * 取得参数类型表。
     */
    List<TypeInfo> getParameterTypes();

    /**
     * 取得异常类型表。
     */
    List<TypeInfo> getExceptionTypes();

    /**
     * 取得有效异常类型表，即非从<code>RuntimeException</code>和<code>Error</code>派生的异常，<br>
     * 并剔除相重的异常，例如<code>Exception</code>和<code>IOException</code>同时出现，则删除
     * <code>IOException</code>。
     */
    List<TypeInfo> getEffectiveExceptionTypes();

    /**
     * 在指定上下文中分析方法的返回值、参数类型、异常类型的实际类型。
     * <p>
     * 相当于{@link resolve(context, true)}。
     * </p>
     */
    MethodInfo resolve(GenericDeclarationInfo context);

    /**
     * 在指定上下文中分析方法的返回值、参数类型、异常类型的实际类型。
     * <p>
     * 如果<code>includeBaseType==false</code>，那么解析类型变量时，将不会取得其baseType。 。
     * </p>
     * <p>
     * 参见：{@link TypeInfo.resolve()}。
     * </p>
     */
    MethodInfo resolve(GenericDeclarationInfo context, boolean includeBaseType);
}
