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
package com.alibaba.citrus.springext.util;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;

public class SpringExtUtil_ProxyTests {
    private MyObjectFactory factory;
    private MyInterfaceImpl actualObject;
    private MyInterface proxy;

    @Before
    public void init() {
        actualObject = new MyInterfaceImpl("hello");
        factory = new MyObjectFactory(actualObject);
        proxy = createProxy(MyInterface.class, factory);
    }

    @Test
    public void noInterface() {
        try {
            createProxy(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no interface"));
        }
    }

    @Test
    public void noObjectFactory() {
        try {
            createProxy(MyInterface.class, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no ObjectFactory"));
        }
    }

    @Test
    public void methodNameConflict() {
        try {
            createProxy(MyInterface2.class, factory);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Method name conflict: interface ", "$MyInterface2.getObject()"));
        }
    }

    @Test
    public void proxyClass() {
        Class<?> proxyClass = proxy.getClass();

        // 对于同一个interface，总是返回完全相同的class
        assertSame(createProxy(MyInterface.class, factory).getClass(), proxyClass);
        assertSame(createProxy(MyInterface.class, new MyObjectFactory(actualObject)).getClass(), proxyClass);
        assertSame(createProxy(MyInterface.class, new MyObjectFactory(new MyInterfaceImpl("world"))).getClass(),
                proxyClass);

        // 对于不同的interface，则返回不同的class
        assertNotSame(createProxy(List.class, factory).getClass(), proxyClass);

        // Class name
        assertTrue(proxyClass.getName().startsWith(MyInterface.class.getName()));
    }

    @Test
    public void defaultInterceptor() {
        // hashCode
        assertTrue(proxy.hashCode() != actualObject.hashCode());

        // equals
        assertTrue(!proxy.equals(actualObject));

        // toString
        assertEquals("hello", proxy.toString());

        // toString exception
        MyInterfaceImpl.toStringException.set(new IllegalArgumentException("wrong!"));
        assertEquals("MyInterface[IllegalArgumentException: wrong!]", proxy.toString());
        MyInterfaceImpl.toStringException.remove();

        // getObject
        assertSame(actualObject, ((ObjectFactory) proxy).getObject());
    }

    @Test
    public void proxiedInterceptor() {
        assertEquals("hello", proxy.getName());
    }

    @Test
    public void proxyHashCodeAndEquals() {
        // same factory
        assertHashCodeAndEquals(createProxy(MyInterface.class, factory), true);

        // not same but equivalent factory
        assertHashCodeAndEquals(createProxy(MyInterface.class, new MyObjectFactory(actualObject)), true);

        // not equivalent factory
        assertHashCodeAndEquals(createProxy(MyInterface.class, new MyObjectFactory(new MyInterfaceImpl("world"))),
                false);
    }

    private void assertHashCodeAndEquals(Object other, boolean equals) {
        if (equals) {
            assertEquals(proxy.hashCode(), other.hashCode());
            assertEquals(proxy, other);
        } else {
            assertTrue(proxy.hashCode() != other.hashCode());
            assertTrue(!proxy.equals(other));
        }
    }

    @Test
    public void assertProxy_() {
        // 接受null
        assertProxy(null);

        // 非proxy
        try {
            assertProxy(actualObject);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("expects a proxy delegating to a real object, but got an object of type "
                    + MyInterfaceImpl.class.getName()));
        }

        // proxy
        assertSame(factory, assertProxy(factory)); // 只要实现了ObjectFactory接口，就认可
        assertSame(proxy, assertProxy(proxy));

    }

    @Test
    public void getProxiedObject_() {
        // null
        assertNull(getProxyTarget(null));

        // 非proxy
        assertSame(actualObject, getProxyTarget(actualObject));

        // proxy
        assertSame(actualObject, getProxyTarget(factory)); // 只要实现了ObjectFactory接口，就认可
        assertSame(actualObject, getProxyTarget(proxy));

        // getObject error
        MyObjectFactory.objectException.set(new IllegalArgumentException("wrong!"));
        assertNull(getProxyTarget(proxy));
        MyObjectFactory.objectException.remove();
    }

    public static class MyObjectFactory implements ObjectFactory {
        private final static ThreadLocal<RuntimeException> objectException = new ThreadLocal<RuntimeException>();
        private final Object object;

        public MyObjectFactory(Object object) {
            this.object = object;
        }

        public Object getObject() throws BeansException {
            if (objectException.get() != null) {
                throw objectException.get();
            }

            return object;
        }

        @Override
        public int hashCode() {
            return 31 + (object == null ? 0 : object.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            MyObjectFactory other = (MyObjectFactory) obj;

            if (object == null) {
                if (other.object != null) {
                    return false;
                }
            } else if (!object.equals(other.object)) {
                return false;
            }

            return true;
        }
    }

    public static interface MyInterface {
        String getName();
    }

    public static class MyInterfaceImpl implements MyInterface {
        private final static ThreadLocal<RuntimeException> toStringException = new ThreadLocal<RuntimeException>();
        private String name;

        public MyInterfaceImpl(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            RuntimeException exception = toStringException.get();

            if (exception != null) {
                throw exception;
            } else {
                return name;
            }
        }
    }

    public static interface MyInterface2 {
        String getObject();
    }
}
