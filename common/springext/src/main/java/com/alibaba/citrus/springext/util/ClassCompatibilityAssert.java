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

package com.alibaba.citrus.springext.util;

import static com.alibaba.citrus.util.Assert.*;

import org.springframework.core.SpringVersion;

/**
 * 检查类型兼容性的工具。
 *
 * @author Michael Zhou
 */
public class ClassCompatibilityAssert {
    /** 检查spring版本是否为3.1.x。从这个版本起，和以前的版本在api上有不兼容。 */
    public static void assertSpring3_1_x() {
        ClassLoader cl = SpringVersion.class.getClassLoader();

        try {
            cl.loadClass("org.springframework.core.env.Environment");
        } catch (ClassNotFoundException e) {
            fail("Unsupported Spring version: %s, requires Spring 3.1.x or later", SpringVersion.getVersion());
        }
    }
}
