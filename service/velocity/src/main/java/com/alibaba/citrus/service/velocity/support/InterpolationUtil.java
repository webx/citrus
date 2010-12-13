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
package com.alibaba.citrus.service.velocity.support;

import org.apache.velocity.context.Context;

public class InterpolationUtil {
    public static final String INTERPOLATE_KEY = "_INTERPOLATION_";

    /**
     * 如果当前正在解析<code>StringLiteral</code>，则返回<code>true</code>。
     * <p>
     * 此特性需要打开velocity configuration：
     * <code>runtime.interpolate.string.literals.hack == true</code>。
     * </p>
     */
    public static boolean isInInterpolation(Context context) {
        return context.get(INTERPOLATE_KEY) instanceof Boolean;
    }
}
