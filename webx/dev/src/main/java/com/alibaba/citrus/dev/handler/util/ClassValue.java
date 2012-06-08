/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.dev.handler.util;

import com.alibaba.citrus.util.ClassUtil;

public class ClassValue extends StyledValue {
    private final String className;

    public ClassValue(String className) {
        super(className);
        this.className = className;
    }

    public String getPackageName() {
        return ClassUtil.getPackageName(className);
    }

    public String getSimpleName() {
        return ClassUtil.getSimpleClassName(className, false);
    }

    public String getClassName() {
        return className;
    }
}
