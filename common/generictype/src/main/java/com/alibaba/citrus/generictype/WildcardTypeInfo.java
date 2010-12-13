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

import java.lang.reflect.WildcardType;

/**
 * 和{@link WildcardType}对应，代表一个通配符类型变量的信息。
 * 
 * @author Michael Zhou
 */
public interface WildcardTypeInfo extends BoundedTypeInfo {
    /**
     * 判断wildcard是否为“unknown wildcard”。
     * <p>
     * Wildcard有两种：
     * </p>
     * <ol>
     * <li>一种是“unknown wildcard”的，例如：<code>Collection&lt;?&gt;</code>
     * ，意思为：Collection of Unknown。换言之，“<code>?</code>”可以匹配任意类型。</li>
     * <li>另一种是“bounded wildcard”，例如：
     * <code>Collection&lt;? extends Number&gt;</code>。</li>
     * </ol>
     * <p>
     * 对于unknown wildcard，尽管JDK提供的API，仍然返回其upper bound为<code>Object</code>
     * ，然而其事实上的upper bound取决于对应的<code>TypeVariable</code>的upper bound。
     */
    boolean isUnknown();
}
