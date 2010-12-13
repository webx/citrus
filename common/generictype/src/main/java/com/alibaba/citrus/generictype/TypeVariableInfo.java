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
import java.lang.reflect.TypeVariable;

/**
 * 和{@link TypeVariable}对应，代表一个类型变量的信息。
 * 
 * @author Michael Zhou
 */
public interface TypeVariableInfo extends BoundedTypeInfo {
    /**
     * 取得变量所在的声明，可能为：
     * <ul>
     * <li>{@link ClassTypeInfo}</li>
     * <li>{@link MethodInfo}，代表{@link Method}或{@link Constructor}</li>
     * </ul>
     */
    GenericDeclarationInfo getGenericDeclaration();
}
