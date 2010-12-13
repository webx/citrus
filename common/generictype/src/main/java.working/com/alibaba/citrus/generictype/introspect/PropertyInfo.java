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
package com.alibaba.citrus.generictype.introspect;

import com.alibaba.citrus.generictype.MethodInfo;
import com.alibaba.citrus.generictype.TypeInfo;

/**
 * 代表一个property的信息。
 * <p>
 * Property不是一个Java语言的元素，而是一种规范和约定。常见的形式是：
 * </p>
 * 
 * <pre>
 * public String getName();
 * 
 * public void setName(String name);
 * </pre>
 * <p>
 * 以上这对方法定义了一个可读、可写的property，名字叫<code>name</code>，类型为<code>String</code>。
 * </p>
 */
public interface PropertyInfo {
    /**
     * 取得property的名称。
     * <p>
     * 对于indexed或mapped property，其名称可以为空（<code>""</code>）。
     * </p>
     */
    String getName();

    /**
     * 取得当前property所在的类型。
     */
    TypeInfo getDeclaringType();

    /**
     * 取得property的类型。
     */
    TypeInfo getType();

    /**
     * 判断property是否可读。
     */
    boolean isReadable();

    /**
     * 判断property是否可写。
     */
    boolean isWritable();

    /**
     * 取得用来读取property的方法。
     */
    MethodInfo getReadMethod();

    /**
     * 取得用来修改property的方法。
     */
    MethodInfo getWriteMethod();
}
