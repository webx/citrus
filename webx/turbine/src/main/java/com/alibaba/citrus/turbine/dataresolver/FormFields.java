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
 * 用来标识一个参数，使之从form service中取得一组<code>Group</code>中的同名<code>Field</code>s对象。
 * 
 * @author Michael Zhou
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface FormFields {
    /**
     * 用于标识field的名称。
     */
    String name();

    /**
     * 用于标识group的名称。
     */
    String group();

    /**
     * 假如表单未验证通过，则跳过模块的执行。默认为<code>true</code>。
     * <p>
     * 注意，只有特定的模块（action）可以被跳过，对于其余的模块，此参数无效。
     * </p>
     */
    boolean skipIfInvalid() default true;
}
