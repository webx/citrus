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
package com.alibaba.citrus.codegen;

import com.alibaba.citrus.asm.MethodVisitor;
import com.alibaba.citrus.asm.commons.GeneratorAdapter;

/**
 * 用来生成method body代码的工具。
 * 
 * @author Michael Zhou
 */
public class CodeBuilder extends GeneratorAdapter {
    private final MethodBuilder mb;

    /**
     * 创建<code>CodeBuilder</code>。
     */
    public CodeBuilder(MethodBuilder mb, MethodVisitor mv, int access, String name, String desc) {
        super(mv, access, name, desc);
        this.mb = mb;
        mv.visitCode();
    }

    /**
     * 取得当前代码所在<code>MethodBuilder</code>。
     */
    public MethodBuilder getMethodBuilder() {
        return mb;
    }

    /**
     * 取得用来生成代码的<code>MethodVisitor</code>。
     */
    public MethodVisitor getMethodVisitor() {
        return mv;
    }
}
