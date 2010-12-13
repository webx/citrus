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
package com.alibaba.citrus.asm.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.alibaba.citrus.asm.ClassVisitor;
import com.alibaba.citrus.asm.MethodVisitor;
import com.alibaba.citrus.asm.Opcodes;

/**
 * ASMContentHandler unit tests
 * 
 * @author Eric Bruneton
 */
public class ASMContentHandlerUnitTest extends TestCase implements Opcodes {

    ASMContentHandler h;

    ClassVisitor cv;

    MethodVisitor mv;

    @Override
    protected void setUp() throws Exception {
        h = new ASMContentHandler(new ByteArrayOutputStream() {
            @Override
            public void write(final byte[] b) throws IOException {
                throw new IOException();
            }
        }, false);
        cv = new SAXClassAdapter(h, true);
        cv.visit(V1_5, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    }

    protected void methodSetUp() {
        mv = cv.visitMethod(0, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    }

    public void testInvalidOpcode() {
        methodSetUp();
        AttributesImpl attrs = new AttributesImpl();
        try {
            h.startElement("", "opcode", "", attrs);
            h.endElement("", "opcode", "");
            fail();
        } catch (SAXException e) {
        }
    }

    public void testInvalidValueDescriptor() {
        methodSetUp();
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "desc", "desc", "", "desc");
        attrs.addAttribute("", "cst", "cst", "", "");
        try {
            h.startElement("", "LDC", "", attrs);
            h.endElement("", "LDC", "");
            fail();
        } catch (SAXException e) {
        }
    }

    public void testInvalidValue() {
        methodSetUp();
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "desc", "desc", "", "Ljava/lang/String;");
        attrs.addAttribute("", "cst", "cst", "", "\\");
        try {
            h.startElement("", "LDC", "", attrs);
            h.endElement("", "LDC", "");
            fail();
        } catch (SAXException e) {
        }
    }

    public void testIOException() {
        cv.visitEnd();
        try {
            h.endDocument();
        } catch (SAXException e) {
        }
    }
}
