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
package com.alibaba.citrus.util.internal;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import com.alibaba.citrus.util.ArrayUtil;
import com.alibaba.citrus.util.ObjectUtil;
import com.alibaba.citrus.util.StringEscapeUtil;
import com.alibaba.citrus.util.StringUtil;

public class StaticFunctionDelegatorBuilderTests {
    private StaticFunctionDelegatorBuilder builder;

    @Test
    public void classLoader() {
        ClassLoader cl;

        // default class loader
        cl = Thread.currentThread().getContextClassLoader();
        builder = new StaticFunctionDelegatorBuilder();
        assertSame(cl, builder.getClassLoader());

        // specified class loader
        cl = new URLClassLoader(new URL[0]);
        builder = new StaticFunctionDelegatorBuilder().setClassLoader(cl);
        assertSame(cl, builder.getClassLoader());
    }

    @Test
    public void addClass() {
        builder = new StaticFunctionDelegatorBuilder();
        builder.addClass(Util1.class);
        builder.addClass(Util2.class);

        Object util = builder.toObject();
        Method[] methods = getMethods(util, builder.getMixinInterface());

        assertArrayEquals(new String[] { "func1", "func2" }, getMethodNames(methods));
    }

    @Test
    public void addClassConflict() {
        builder = new StaticFunctionDelegatorBuilder();
        builder.addClass(Util1.class);
        builder.addClass(Util2.class);

        try {
            builder.addClass(Util2Conflict.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(
                    e,
                    exception("Duplicated method signature: func2(I)Ljava/lang/String;",
                            "StaticFunctionDelegatorBuilderTests$Util2.func2(int)"));
        }

        Object util = builder.toObject();
        Method[] methods = getMethods(util, builder.getMixinInterface());

        assertArrayEquals(new String[] { "func1", "func2" }, getMethodNames(methods));
    }

    @Test
    public void addMethod() throws Exception {
        builder = new StaticFunctionDelegatorBuilder();

        // add null
        try {
            builder.addMethod(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Method is null"));
        }

        // not public static
        try {
            builder.addMethod(Util1.class.getDeclaredMethod("nonStatic", String.class, int.class));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Method is not public static: "));
        }

        try {
            builder.addMethod(Util1.class.getDeclaredMethod("nonPublic", String.class, int.class));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Method is not public static: "));
        }

        builder.addMethod(Util1.class.getMethod("func1", String.class, int.class));
        builder.addMethod(Util2.class.getMethod("func2", int.class));

        Object util = builder.toObject();
        Method[] methods = getMethods(util, builder.getMixinInterface());

        assertArrayEquals(new String[] { "func1", "func2" }, getMethodNames(methods));
    }

    @Test
    public void addMethodRename() throws Exception {
        builder = new StaticFunctionDelegatorBuilder();

        builder.addClass(Util1.class);
        builder.addClass(Util2.class);
        builder.addMethod(Util2Conflict.class.getMethod("func2", int.class), "func3");

        Object util = builder.toObject();
        Method[] methods = getMethods(util, builder.getMixinInterface());

        assertArrayEquals(new String[] { "func1", "func2", "func3" }, getMethodNames(methods));

        Method func3 = builder.getMixinInterface().getMethod("func3", int.class);

        assertEquals(1, func3.getExceptionTypes().length);
        assertEquals(IOException.class, func3.getExceptionTypes()[0]);
    }

    @Test
    public void invoke() throws Exception {
        builder = new StaticFunctionDelegatorBuilder();

        builder.addClass(Util1.class);
        builder.addClass(Util2.class);

        Object util = builder.toObject();
        Method func2 = util.getClass().getMethod("func2", int.class);

        assertEquals("4", func2.invoke(util, 2));
    }

    @Test
    public void equals_() throws Exception {
        builder = new StaticFunctionDelegatorBuilder();

        builder.addClass(Util1.class);
        builder.addClass(Util2.class);

        Object util = builder.toObject();

        assertFalse(util.equals(builder.toObject()));
    }

    @Test
    public void toString_() throws Exception {
        builder = new StaticFunctionDelegatorBuilder();

        builder.addClass(Util1.class);
        builder.addClass(Util2.class);

        Object util = builder.toObject();

        assertThat(
                util.toString(),
                containsAll(
                        "$$StaticFunctionDelegatorBuilderByCGLIB", //
                        "[1/2] func1 = public static void com.alibaba.citrus.util.internal.StaticFunctionDelegatorBuilderTests$Util1.func1(java.lang.String,int)",
                        "[2/2] func2 = public static java.lang.String com.alibaba.citrus.util.internal.StaticFunctionDelegatorBuilderTests$Util2.func2(int)"));
    }

    private Method[] getMethods(Object util, Class<?> intfs) {
        assertTrue(intfs.isInstance(util));

        List<Method> methods = createArrayList();

        for (Method method : intfs.getMethods()) {
            methods.add(method);
        }

        Collections.sort(methods, new Comparator<Method>() {
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return methods.toArray(new Method[methods.size()]);
    }

    private String[] getMethodNames(Method[] methods) {
        String[] methodNames = new String[methods.length];

        for (int i = 0; i < methods.length; i++) {
            methodNames[i] = methods[i].getName();
        }

        return methodNames;
    }

    public static class Util1 {
        public static void func1(String s, int i) {
        }

        public void nonStatic(String s, int i) {
        }

        protected static void nonPublic(String s, int i) {
        }
    }

    public static class Util2 {
        public static String func2(int i) {
            return i * 2 + "";
        }
    }

    public static class Util2Conflict {
        public static String func2(int i) throws IOException {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        StaticFunctionDelegatorBuilder builder = new StaticFunctionDelegatorBuilder();

        builder.addClass(Math.class);
        builder.addClass(StringUtil.class);
        builder.addClass(StringEscapeUtil.class);
        builder.addClass(ArrayUtil.class);
        builder.addClass(ObjectUtil.class);

        Object util = builder.toObject();

        Method abs = util.getClass().getMethod("getLength", String.class);

        System.out.println(abs.invoke(util, "abcd"));
    }

}
