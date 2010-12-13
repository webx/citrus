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
package com.alibaba.citrus.codegen.util;

import static com.alibaba.citrus.asm.Opcodes.*;

import com.alibaba.citrus.asm.ClassWriter;
import com.alibaba.citrus.asm.Opcodes;
import com.alibaba.citrus.asm.Type;

/**
 * 和二进制码生成相关的常量。
 * 
 * @author Michael Zhou
 */
public final class CodegenConstant {
    /** 默认的<code>ClassWriter</code>标志位。 */
    public final static int DEFAULT_CLASS_WRITER_FLAGS = ClassWriter.COMPUTE_MAXS;

    /** 默认的class文件版本。 */
    public final static int DEFAULT_CLASS_VERSION = Opcodes.V1_5;

    /** 默认的class源文件名。 */
    public final static String DEFAULT_SOURCE = "<generated>";

    /** 默认的class包名。 */
    public final static String DEFAULT_PACKAGE_NAME = "generated";

    /** <code>java.lang.Object</code>对应的<code>Type</code>对象。 */
    public final static Type OBJECT_TYPE = Type.getType(Object.class);

    /** 常量修饰符。 */
    public final static int ACC_CONSTANT = ACC_STATIC | ACC_FINAL;

    /** 构造函数的名称。 */
    public final static String CONSTRUCTOR_NAME = "<init>";

    /** 静态构造函数的名称。 */
    public final static String STATIC_CONSTRUCTOR_NAME = "<clinit>";
}
