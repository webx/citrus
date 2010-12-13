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

import static com.alibaba.citrus.asm.ClassReader.*;
import static com.alibaba.citrus.asm.Opcodes.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.asm.ClassReader;
import com.alibaba.citrus.asm.ClassVisitor;
import com.alibaba.citrus.asm.Type;
import com.alibaba.citrus.asm.commons.Remapper;
import com.alibaba.citrus.asm.commons.RemappingClassAdapter;
import com.alibaba.citrus.asm.util.ASMifierClassVisitor;
import com.alibaba.citrus.codegen.util.CodegenUtil;
import com.alibaba.citrus.test.TestEnvStatic;

public class ClassGenerationTests {
    private ClassPool pool;
    private File debuggingLocation;

    @Before
    public void init() {
        pool = new ClassPool();
        pool.setDebugging(true);

        debuggingLocation = new File(TestEnvStatic.destdir, "codegen");
        pool.setDebuggingLocation(debuggingLocation);
    }

    @Test
    public void generate() throws Exception {
        ClassBuilder cb = pool.createClass("Test", null, null);
        CodegenUtil.addDefaultConstructor(cb);
        CodegenUtil.addToString(cb, "hello, world");

        MethodBuilder mv = cb.addMethod(String.class, "test", null, null);
        CodeBuilder code = mv.startCode();

        int var1 = code.newLocal(Type.LONG_TYPE);
        int var2 = code.newLocal(Type.getType(String.class));

        code.visitCode();

        code.push(10L);
        code.storeLocal(var1);

        code.loadLocal(var1);
        code.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(J)Ljava/lang/String;");
        code.storeLocal(var2);

        code.loadLocal(var2);
        code.returnValue();

        Class<?> c = cb.toClass();

        Object co = c.newInstance();

        // toString()
        assertEquals("hello, world", co.toString());

        // test()
        Method testMethod = c.getMethod("test");

        assertEquals("10", testMethod.invoke(co));

        System.out.println("===============================");
        System.out.println(dump(MyClass.class, null));
        System.out.println("===============================");
        System.out.println(dump(c, null));
    }

    private String dump(Class<?> clazz, Remapper mapper) throws Exception {
        InputStream is;
        try {
            is = new FileInputStream(new File(debuggingLocation, clazz.getName().replace('.', '/') + ".class"));
        } catch (FileNotFoundException e) {
            is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
        }

        ClassReader cr = new ClassReader(is);
        StringWriter buf = new StringWriter();
        PrintWriter out = new PrintWriter(buf);

        ClassVisitor cv = new ASMifierClassVisitor(out);

        if (mapper != null) {
            cv = new RemappingClassAdapter(cv, mapper);
        }

        cr.accept(cv, SKIP_DEBUG);

        out.flush();
        out.close();
        is.close();

        return buf.toString();
    }
}
