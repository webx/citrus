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
/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.alibaba.citrus.asm.tree;

import com.alibaba.citrus.asm.MethodVisitor;

/**
 * A node that represents a local variable declaration.
 * 
 * @author Eric Bruneton
 */
public class LocalVariableNode {

    /**
     * The name of a local variable.
     */
    public String name;

    /**
     * The type descriptor of this local variable.
     */
    public String desc;

    /**
     * The signature of this local variable. May be <tt>null</tt>.
     */
    public String signature;

    /**
     * The first instruction corresponding to the scope of this local variable
     * (inclusive).
     */
    public LabelNode start;

    /**
     * The last instruction corresponding to the scope of this local variable
     * (exclusive).
     */
    public LabelNode end;

    /**
     * The local variable's index.
     */
    public int index;

    /**
     * Constructs a new {@link LocalVariableNode}.
     * 
     * @param name the name of a local variable.
     * @param desc the type descriptor of this local variable.
     * @param signature the signature of this local variable. May be
     *            <tt>null</tt>.
     * @param start the first instruction corresponding to the scope of this
     *            local variable (inclusive).
     * @param end the last instruction corresponding to the scope of this local
     *            variable (exclusive).
     * @param index the local variable's index.
     */
    public LocalVariableNode(final String name, final String desc, final String signature, final LabelNode start,
                             final LabelNode end, final int index) {
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.start = start;
        this.end = end;
        this.index = index;
    }

    /**
     * Makes the given visitor visit this local variable declaration.
     * 
     * @param mv a method visitor.
     */
    public void accept(final MethodVisitor mv) {
        mv.visitLocalVariable(name, desc, signature, start.getLabel(), end.getLabel(), index);
    }
}
