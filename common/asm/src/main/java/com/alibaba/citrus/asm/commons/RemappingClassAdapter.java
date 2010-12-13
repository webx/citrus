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

package com.alibaba.citrus.asm.commons;

import com.alibaba.citrus.asm.AnnotationVisitor;
import com.alibaba.citrus.asm.ClassAdapter;
import com.alibaba.citrus.asm.ClassVisitor;
import com.alibaba.citrus.asm.FieldVisitor;
import com.alibaba.citrus.asm.MethodVisitor;

/**
 * A <code>ClassAdapter</code> for type remapping.
 * 
 * @author Eugene Kuleshov
 */
public class RemappingClassAdapter extends ClassAdapter {

    protected final Remapper remapper;

    protected String className;

    public RemappingClassAdapter(ClassVisitor cv, Remapper remapper) {
        super(cv);
        this.remapper = remapper;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, remapper.mapType(name), remapper.mapSignature(signature, false), remapper
                .mapType(superName), interfaces == null ? null : remapper.mapTypes(interfaces));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor av;
        av = super.visitAnnotation(remapper.mapType(desc), visible);
        return av == null ? null : createRemappingAnnotationAdapter(av);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        FieldVisitor fv = super.visitField(access, remapper.mapFieldName(className, name, desc),
                remapper.mapDesc(desc), remapper.mapSignature(signature, true), remapper.mapValue(value));
        return fv == null ? null : createRemappingFieldAdapter(fv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        String newDesc = remapper.mapMethodDesc(desc);
        MethodVisitor mv = super.visitMethod(access, remapper.mapMethodName(className, name, desc), newDesc, remapper
                .mapSignature(signature, false), exceptions == null ? null : remapper.mapTypes(exceptions));
        return mv == null ? null : createRemappingMethodAdapter(access, newDesc, mv);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(remapper.mapType(name), outerName == null ? null : remapper.mapType(outerName),
                innerName, // TODO should it be changed?
                access);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(remapper.mapType(owner), name == null ? null : remapper.mapMethodName(owner, name, desc),
                desc == null ? null : remapper.mapMethodDesc(desc));
    }

    protected FieldVisitor createRemappingFieldAdapter(FieldVisitor fv) {
        return new RemappingFieldAdapter(fv, remapper);
    }

    protected MethodVisitor createRemappingMethodAdapter(int access, String newDesc, MethodVisitor mv) {
        return new RemappingMethodAdapter(access, newDesc, mv, remapper);
    }

    protected AnnotationVisitor createRemappingAnnotationAdapter(AnnotationVisitor av) {
        return new RemappingAnnotationAdapter(av, remapper);
    }
}
