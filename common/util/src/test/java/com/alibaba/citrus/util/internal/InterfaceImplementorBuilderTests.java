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

import org.junit.Test;

public class InterfaceImplementorBuilderTests {
    private InterfaceImplementorBuilder builder;

    @Test
    public void classLoader() {
        ClassLoader cl;

        // default class loader
        cl = Thread.currentThread().getContextClassLoader();
        builder = new InterfaceImplementorBuilder().addInterface(Serializable.class);
        assertSame(cl, builder.getClassLoader());
        assertSame(cl, builder.toObject().getClass().getClassLoader());

        // specified class loader
        cl = new URLClassLoader(new URL[0]);
        builder = new InterfaceImplementorBuilder(cl).addInterface(Serializable.class);
        assertSame(cl, builder.getClassLoader());
        assertSame(cl, builder.toObject().getClass().getClassLoader());
    }

    @Test
    public void interfaces() {
        try {
            new InterfaceImplementorBuilder().toObject();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no interface specified"));
        }

        try {
            new InterfaceImplementorBuilder().addInterface(null).toObject();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no interface specified"));
        }
    }

    @Test
    public void baseObject() {
        // no baseObject
        ServletContext sc = (ServletContext) new InterfaceImplementorBuilder().addInterface(ServletContext.class).toObject();

        try {
            sc.getAttribute("key");
            fail();
        } catch (UnsupportedOperationException e) {
            assertThat(e, exception("ServletContext.getAttribute(String)"));
        }

        // wrong type of baseObject
        try {
            new InterfaceImplementorBuilder().addInterface(Runnable.class).setBaseClass(String.class).toObject();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Base class java.lang.String must implement interface java.lang.Runnable"));
        }

        try {
            new InterfaceImplementorBuilder().addInterface(Runnable.class).addInterface(ServletContext.class).setBaseClass(Runnable.class).toObject();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Base class java.lang.Runnable must implement interface javax.servlet.ServletContext"));
        }
    }

    @Test
    public void setOverriderSetProxyObjectMethodName() {
        final Object[] holder = new Object[1];

        // default method
        ServletContext sc = (ServletContext) new InterfaceImplementorBuilder().addInterface(ServletContext.class).toObject(new Object() {
            public void setThisProxy(ServletContext proxy) {
                holder[0] = proxy;
            }
        });

        assertSame(holder[0], sc);

        // specified method name
        sc = (ServletContext) new InterfaceImplementorBuilder().addInterface(ServletContext.class).setOverriderSetProxyObjectMethodName("setMyProxy").toObject(new Object() {
            public void setMyProxy(ServletContext proxy) {
                holder[0] = proxy;
            }
        });

        assertSame(holder[0], sc);

        // super param type
        sc = (ServletContext) new InterfaceImplementorBuilder().addInterface(ServletContext.class).toObject(new Object() {
            public void setThisProxy(Object proxy) {
                holder[0] = proxy;
            }
        });

        assertSame(holder[0], sc);

        // wrong param type
        holder[0] = null;
        sc = (ServletContext) new InterfaceImplementorBuilder().addInterface(ServletContext.class).toObject(new Object() {
            public void setThisProxy(String proxy) {
                holder[0] = proxy;
            }
        });

        assertSame(null, holder[0]);

        // no param type
        holder[0] = null;
        sc = (ServletContext) new InterfaceImplementorBuilder().addInterface(ServletContext.class).toObject(new Object() {
            public void setThisProxy() {
            }
        });

        assertSame(null, holder[0]);

        // call failed
        try {
            sc = (ServletContext) new InterfaceImplementorBuilder().addInterface(ServletContext.class).toObject(new Object() {
                public void setThisProxy(Object proxy) {
                    throw new IllegalArgumentException();
                }
            });
        } catch (Exception e) {
            assertThat(e, exception(IllegalArgumentException.class, "Failed to call ", "setThisProxy(Object)"));
        }

        assertSame(null, holder[0]);
    }

    @Test
    public void toObject() {
        final Object[] holder = new Object[1];

        Object overrider = new Object() {
            public void setThisProxy(Object proxy) {
                holder[0] = proxy;
            }
        };

        builder = new InterfaceImplementorBuilder().addInterface(ServletContext.class).setOverriderClass(overrider.getClass());

        ServletContext sc1 = (ServletContext) builder.toObject(overrider);
        assertSame(sc1, holder[0]);

        ServletContext sc2 = (ServletContext) builder.toObject(overrider);
        assertSame(sc2, holder[0]);

        assertNotSame(sc1, sc2);
        assertSame(sc1.getClass(), sc2.getClass());
    }

    @Test
    public void invokeSuper() {
        Runnable newObject = (Runnable) new InterfaceImplementorBuilder().addInterface(Runnable.class).toObject(null, new Runnable() {
            public void run() {
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
        });

        assertFalse(newObject.equals(""));
        assertFalse(123 == newObject.hashCode());
        assertFalse("haha".equals(newObject.toString()));
    }

    public static interface MyInterface1 {
        String getName();

        void throwException(Throwable e) throws Throwable;
    }

    @Test
    public void invokeBaseObject() {
        MyInterface1 newObject = (MyInterface1) new InterfaceImplementorBuilder().addInterface(MyInterface1.class).toObject(null, new MyInterface1() {
            public String getName() {
                return "myname";
            }

            public void throwException(Throwable e) throws Throwable {
                throw e;
            }
        });

        assertEquals("myname", newObject.getName());

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

    public static class MySuperClass {
        public String sayHello() {
            return "hello";
        }
    }

    @Test
    public void invokeBaseObject_withSuperclass() {
        MyInterface1 newObject = (MyInterface1) new InterfaceImplementorBuilder().setSuperclass(MySuperClass.class).addInterface(MyInterface1.class).toObject(null, new MyInterface1() {
            public String getName() {
                return "myname";
            }

            public void throwException(Throwable e) throws Throwable {
                throw e;
            }
        });

        assertTrue(newObject instanceof MySuperClass);
        assertEquals("hello", ((MySuperClass) newObject).sayHello());

        assertEquals("myname", newObject.getName());

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

    public static interface MyInterface2 {
        String getName();

        String getName(String s);

        String getName(Object o, String s);

        void throwException(Throwable e) throws Throwable;
    }

    @Test
    public void invokeOverrider() {
        MyInterface2 baseObject = new MyInterface2() {
            public String getName() {
                return "myname";
            }

            public String getName(String s) {
                return null;
            }

            public String getName(Object o, String s) {
                return null;
            }

            public void throwException(Throwable e) throws Throwable {
            }
        };

        Object overrider1 = new Object() {
            public String getName(String s) {
                return "" + s;
            }

            public String getName(Integer o, Object s) {
                return "" + o + s;
            }

            public String getName(Object o, Integer s) {
                return "" + o + s;
            }

            public void throwException(Throwable e) throws Throwable {
                throw e;
            }
        };

        MyInterface2 newObject = (MyInterface2) new InterfaceImplementorBuilder().addInterface(MyInterface2.class).toObject(overrider1, baseObject);

        assertEquals("myname", newObject.getName()); // from baseObject
        assertEquals("another name", newObject.getName("another name")); // from overrider
        assertEquals(null, newObject.getName("my", " name")); // no overrider method matched

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

        Object overrider2 = new Object() {
            public String getName(Object o, Object s) {
                return "" + o + s;
            }
        };

        newObject = (MyInterface2) new InterfaceImplementorBuilder().addInterface(MyInterface2.class).toObject(overrider2, baseObject);

        assertEquals("myname", newObject.getName()); // from baseObject
        assertEquals(null, newObject.getName("another name")); // no overrider method matched
        assertEquals("my name", newObject.getName("my", " name")); // from overrider, string parameter as an object
    }
}
