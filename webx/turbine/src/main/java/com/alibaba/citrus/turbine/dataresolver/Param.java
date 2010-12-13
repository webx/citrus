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
package com.alibaba.citrus.turbine.dataresolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来标识一个参数，使之从request parameters中取得值。
 * <p>
 * 用法如下：
 * </p>
 * <ol>
 * <li>仅指定参数名称：<code>@Param("name")</code>。</li>
 * <li>指定参数名称，以及单个默认值：<code>@Param(name="name", defaultValue="123")</code>。</li>
 * <li>指定参数名称，以及一组默认值：
 * <code>@Param(name="name", defaultValues={"1", "2", "3"})</code>。</li>
 * </ol>
 * 
 * @author Michael Zhou
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface Param {
    /**
     * 用于标识param的名称。
     * <p>
     * 此参数用于简化的形式：<code>@Param("paramName")</code>。
     * </p>
     */
    String value() default "";

    /**
     * 用于标识param的名称。
     * <p>
     * 此参数用于有多个参数的形式：<code>@Param(name="paramName", defaultValue="123")</code>。
     * </p>
     */
    String name() default "";

    /**
     * 指定参数的默认值。
     */
    String defaultValue() default "";

    /**
     * 指定参数的默认值数组。
     */
    String[] defaultValues() default {};
}
