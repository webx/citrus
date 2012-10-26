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

package com.alibaba.citrus.util.internal;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;

public class OverridedMethodBuilderTests {
    private OverridedMethodBuilder builder;
    private TestInterface          delegatedObject;
    private TestInterface          newObject;

    @Before
    public void init() {
        delegatedObject = new TestInterfaceImpl();
        builder = new OverridedMethodBuilder(new Class<?>[] { TestInterface.class }, delegatedObject, new Object() {
            public String getFirstName() {
                return "another name";
            }

            public String getFirstName(Object s) {
                return String.valueOf(s);
            }

            public void throwException2(Throwable e) throws Throwable {
                throw e;
            }
        });
        newObject = (TestInterface) builder.toObject();
    }

    @Test
    public void constructor() {
        try {
            new OverridedMethodBuilder(null, new String(), new Object());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("interfaces"));
        }

        // delegated object == null是允许的
        new OverridedMethodBuilder(new Class<?>[] { Serializable.class }, null, new Object());

        try {
            new OverridedMethodBuilder(new Class<?>[] { Serializable.class }, new String(), null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("overrider"));
        }

        try {
            new OverridedMethodBuilder(new Class<?>[] { ServletContext.class }, new String("string"), new Object());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("string is not of interface javax.servlet.ServletContext"));
        }
    }

    @Test
    public void classLoader() {
        ClassLoader cl;

        // default class loader
        cl = Thread.currentThread().getContextClassLoader();
        builder = new OverridedMethodBuilder(new Class<?>[] { Serializable.class }, new String(), new Object());
        assertSame(cl, builder.getClassLoader());

        // specified class loader
        cl = new URLClassLoader(new URL[0]);
        builder = new OverridedMethodBuilder(cl, new Class<?>[] { Serializable.class }, new String(), new Object());
        assertSame(cl, builder.getClassLoader());
        assertSame(cl, builder.toObject().getClass().getClassLoader());
    }

    @Test
    public void invokeSuper() {
        assertFalse(newObject.equals(""));
        assertFalse(123 == newObject.hashCode());
        assertFalse("haha".equals(newObject.toString()));
    }

    @Test
    public void invokeDelegatedObject() {
        assertEquals("Zhou", newObject.getLastName());

        try {
            newObject.throwException(new IllegalArgumentException());
            fail();
        } catch (Throwable e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        try {
            newObject.throwException(new IOException());
            fail();
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void invoke_without_DelegatedObject() {
        builder = new OverridedMethodBuilder(new Class<?>[] { TestInterface.class }, null, new Object() {
            public String getFirstName() {
                return "another name";
            }

            public void throwException2(Throwable e) throws Throwable {
                throw e;
            }
        });
        newObject = (TestInterface) builder.toObject();

        try {
            newObject.getLastName();
        } catch (UnsupportedOperationException e) {
            assertThat(e, exception("getLastName()"));
        }

        assertEquals("another name", newObject.getFirstName());
    }

    @Test
    public void invokeOverridedMethod() {
        assertEquals("another name", newObject.getFirstName());
        assertEquals("myname", newObject.getFirstName("myname")); // string parameter as an object

        try {
            newObject.throwException2(new IllegalArgumentException());
            fail();
        } catch (Throwable e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        try {
            newObject.throwException2(new IOException());
            fail();
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
        }
    }

    public interface TestInterface {
        String getLastName();

        String getFirstName();

        String getFirstName(String s);

        void throwException(Throwable e) throws Throwable;

        void throwException2(Throwable e) throws Throwable;
    }

    public static class TestInterfaceImpl implements TestInterface {
        public String getLastName() {
            return "Zhou";
        }

        public String getFirstName() {
            return "Michael"; // to be overrided
        }

        public String getFirstName(String s) {
            return null;
        }

        public void throwException(Throwable e) throws Throwable {
            throw e;
        }

        public void throwException2(Throwable e) throws Throwable {
            // to be overrided
        }

        @Override
        public int hashCode() {
            return 123;
        }

        @Override
        public boolean equals(Object obj) {
            return true;
        }

        @Override
        public String toString() {
            return "haha";
        }
    }
}
