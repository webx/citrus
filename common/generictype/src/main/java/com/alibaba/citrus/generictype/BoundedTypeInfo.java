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

import java.util.List;

/**
 * 代表一个包含边界信息的未确定类型信息。包含以下子类：
 * <ul>
 * <li>{@link TypeVariableInfo}</li>
 * <li>{@link WildcardTypeInfo}</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public interface BoundedTypeInfo extends TypeInfo {
    /**
     * 取得基类型。
     */
    TypeInfo getBaseType();

    /**
     * 取得上界（基类和接口）。
     */
    List<TypeInfo> getUpperBounds();

    /**
     * 取得下界（子类和子接口）。
     */
    List<TypeInfo> getLowerBounds();
}
