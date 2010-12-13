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
package com.alibaba.citrus.test;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.junit.Test;

/**
 * ≤‚ ‘<code>TestUtil</code>°£
 * 
 * @author Michael Zhou
 */
public class TestUtilTests {
    private Throwable t1;
    private Throwable t2;
    private Throwable t3;

    @Before
    public void init() {
        t3 = new IllegalArgumentException("no argument: test");
        t2 = new RuntimeException("wrong", t3);
        t1 = new InvocationTargetException(t2, "invoke failed");
    }

    @Test
    public void exception_messages() {
        assertTrue(exception("no argument", "test").matches(t3));
        assertTrue(exception("wrong").matches(t2));
        assertTrue(exception("invoke failed").matches(t1));

        assertFalse(exception("hello").matches(t1));
    }

    @Test
    public void exception_causes() {
        assertTrue(exception(IllegalArgumentException.class, "no argument", "test").matches(t3));
        assertTrue(exception(IllegalArgumentException.class, "no argument", "test", "wrong").matches(t2));
        assertTrue(exception(IllegalArgumentException.class, "no argument", "test", "invoke failed").matches(t1));

        assertFalse(exception(IOException.class).matches(t1));
    }

    @Test
    public void exception_describe() {
        assertEquals("An exception that is (not null and an instance of java.lang.Throwable)\n"
                + "  and its cause exception is (not null and an instance of java.lang.IllegalArgumentException)\n"
                + "  and its message is (a string containing \"no argument\" and a string containing \"test\")",
                exception(IllegalArgumentException.class, "no argument", "test").toString());

        assertEquals("An exception that is (not null and an instance of java.lang.Throwable)\n"
                + "  and its cause exception is (not null and an instance of java.lang.IllegalArgumentException)",
                exception(IllegalArgumentException.class).toString());

        assertEquals("An exception that is (not null and an instance of java.lang.Throwable)", exception().toString());
    }

    @Test
    public void regex() {
        assertTrue(containsRegex("\\w+").matches(" abc"));
        assertFalse(containsRegex("^\\w+").matches(" abc"));

        assertTrue(containsAllRegex("a+", "b+").matches(" bbb aaa "));
        assertFalse(containsAllRegex("a+", "b+").matches(" bbb "));
    }

    @Test
    public void regex_describe() {
        assertEquals("A string matches regex \\w+", containsRegex("\\w+").toString());
        assertEquals("(A string matches regex a+ and A string matches regex b+)", containsAllRegex("a+", "b+")
                .toString());
    }

    @Test
    public void getFieldValue_() {
        B b = new B();

        b.s = "hello";
        ((A) b).i = 100;

        String s;
        int i;

        s = getFieldValue(b, "s", String.class);
        assertEquals("hello", s);

        i = getFieldValue(b, "i", Integer.class);
        assertEquals(100, i);

        i = getFieldValue(b, A.class, "i", Integer.class);
        assertEquals(100, i);

        // no targetType
        try {
            getFieldValue(null, "s", null);
            fail();
        } catch (AssertionError e) {
            assertThat(e, exception("missing targetType"));
        }

        // field not found
        try {
            getFieldValue(b, "notFound", null);
            fail();
        } catch (AssertionError e) {
            assertThat(e, exception("field notFound not found in " + B.class));
        }

        // wrong return type
        try {
            getFieldValue(b, A.class, "i", String.class);
            fail(s);
        } catch (ClassCastException e) {
        }
    }

    @Test
    public void invokeMethod_() throws Exception {
        B b = new B();

        b.s = "hello";
        ((A) b).i = 100;

        String s;
        int i;

        s = invokeMethod(b, "getS", new Class<?>[] { String.class }, new Object[] { ", world" }, String.class);
        assertEquals("hello, world", s);

        i = invokeMethod(b, "getI", new Class<?>[] { int.class }, new Object[] { 1 }, Integer.class);
        assertEquals(101, i);

        i = invokeMethod(b, A.class, "getI", new Class<?>[] { int.class }, new Object[] { 1 }, Integer.class);
        assertEquals(101, i);

        // no targetType
        try {
            invokeMethod(null, "getS", new Class<?>[] { String.class }, null, null);
            fail();
        } catch (AssertionError e) {
            assertThat(e, exception("missing targetType"));
        }

        // method not found
        try {
            invokeMethod(b, "notFound", new Class<?>[] { String.class }, null, null);
            fail();
        } catch (AssertionError e) {
            assertThat(e, exception("method notFound not found in " + B.class));
        }

        // wrong return type
        try {
            invokeMethod(b, A.class, "getI", new Class<?>[] { int.class }, new Object[] { 1 }, String.class);
            fail(s);
        } catch (ClassCastException e) {
        }
    }

    @SuppressWarnings("unused")
    public static class A {
        private int i;

        private int getI(int j) {
            return i + j;
        }
    }

    @SuppressWarnings("unused")
    public static class B extends A {
        protected String s;

        private String getS(String t) {
            return s + t;
        }
    }
}
