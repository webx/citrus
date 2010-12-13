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
 * ASM tests
 * Copyright (c) 2002-2005 France Telecom
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
package com.alibaba.citrus.asm.test.cases;

import java.io.IOException;

import com.alibaba.citrus.asm.ClassWriter;
import com.alibaba.citrus.asm.FieldVisitor;
import com.alibaba.citrus.asm.Label;
import com.alibaba.citrus.asm.MethodVisitor;
import com.alibaba.citrus.asm.attrs.CodeComment;
import com.alibaba.citrus.asm.attrs.Comment;

/**
 * Generates a class with non standard attributes. Covers class, field, method
 * and code attributes. Also covers the V1_3 class version and the SYNTHETIC
 * access flag for classes.
 * 
 * @author Eric Bruneton
 */
public class Attribute extends Generator {

    @Override
    public void generate(final String dir) throws IOException {
        generate(dir, "pkg/Attribute.class", dump());
    }

    public byte[] dump() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        FieldVisitor fv;
        MethodVisitor mv;

        cw.visit(V1_3, ACC_PUBLIC + ACC_SYNTHETIC, "pkg/Attribute", null, "java/lang/Object", null);

        cw.visitAttribute(new Comment());

        fv = cw.visitField(ACC_PUBLIC, "f", "I", null, null);
        fv.visitAttribute(new Comment());
        fv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitAttribute(new Comment());
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");

        /*
         * the following instructions are designed so that this method will be
         * resized by the method resizing test, in order to cover the code that
         * recomputes the code attribute labels in the resizeInstructions method
         * (see MethodWriter).
         */
        Label l0 = new Label();
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(IFEQ, l0);
        // many NOPs will be introduced here by the method resizing test
        mv.visitJumpInsn(GOTO, l0);
        mv.visitLabel(l0);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitAttribute(new CodeComment());
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}
