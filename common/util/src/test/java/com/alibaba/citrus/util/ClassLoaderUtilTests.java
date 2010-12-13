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
package com.alibaba.citrus.util;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.Set;

import org.junit.Test;

/**
 * 测试<code>ClassLoaderUtil</code>。
 * 
 * @author Michael Zhou
 */
public class ClassLoaderUtilTests {
    // ==========================================================================
    // 取得context class loader的方法。 
    // ==========================================================================

    @Test
    public void getContextClassLoader() {
        assertSame(ClassLoaderUtil.getContextClassLoader(), ClassLoaderUtil.getContextClassLoader());
    }

    // ==========================================================================
    // 装入类的方法。 
    // ==========================================================================

    @Test
    public void loadClass() throws Exception {
        // load from context loader
        assertNull(ClassLoaderUtil.loadClass(null));
        assertSame(String.class, ClassLoaderUtil.loadClass("java.lang.String"));

        // load from class referrer
        assertNull(ClassLoaderUtil.loadClass(null, getClass()));
        assertNull(ClassLoaderUtil.loadClass(null, (Class<?>) null));
        assertSame(String.class, ClassLoaderUtil.loadClass("java.lang.String", getClass()));
        assertSame(String.class, ClassLoaderUtil.loadClass("java.lang.String", (Class<?>) null));

        // load from specified loader
        assertNull(ClassLoaderUtil.loadClass(null, getClass().getClassLoader()));
        assertNull(ClassLoaderUtil.loadClass(null, (ClassLoader) null));
        assertSame(String.class, ClassLoaderUtil.loadClass("java.lang.String", getClass().getClassLoader()));
        assertSame(String.class, ClassLoaderUtil.loadClass("java.lang.String", (ClassLoader) null));
    }

    @Test
    public void loadServiceClass() throws Exception {
        // load from context loader
        assertNull(ClassLoaderUtil.loadServiceClass(null));

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notexist");
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notfound");
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("myservice"));

        // load from class referrer
        assertNull(ClassLoaderUtil.loadServiceClass(null, getClass()));
        assertNull(ClassLoaderUtil.loadServiceClass(null, (Class<?>) null));

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notexist", getClass());
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notfound", getClass());
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notexist", (Class<?>) null);
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notfound", (Class<?>) null);
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("myservice", getClass()));
        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("myservice", (Class<?>) null));

        // load from specified loader
        assertNull(ClassLoaderUtil.loadServiceClass(null, getClass().getClassLoader()));
        assertNull(ClassLoaderUtil.loadServiceClass(null, (ClassLoader) null));

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notexist", getClass().getClassLoader());
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notfound", getClass().getClassLoader());
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notexist", (ClassLoader) null);
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notfound", (ClassLoader) null);
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("myservice", getClass().getClassLoader()));
        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("myservice", (ClassLoader) null));

        try {
            assertEquals(MyService.class,
                    ClassLoaderUtil.loadServiceClass("com.alibaba.citrus.util.MyService", getClass().getClassLoader()));
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            assertEquals(MyService.class,
                    ClassLoaderUtil.loadServiceClass("com.alibaba.citrus.util.MyService", (ClassLoader) null));
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }
    }

    @Test
    public void loadServiceClass_ClassNameServiceId() throws Exception {
        // load from context loader
        assertNull(ClassLoaderUtil.loadServiceClass(null, (String) null));

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notexist", "myservice.notexist");
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notfound", "myservice.notfound");
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("myservice", "myservice"));

        // load from class referrer
        assertNull(ClassLoaderUtil.loadServiceClass(null, null, getClass()));
        assertNull(ClassLoaderUtil.loadServiceClass(null, null, (Class<?>) null));

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notexist", "myservice.notexist", getClass());
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notfound", "myservice.notfound", getClass());
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notexist", "myservice.notexist", (Class<?>) null);
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notfound", "myservice.notfound", (Class<?>) null);
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("myservice", "myservice", getClass()));
        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("myservice", "myservice", (Class<?>) null));

        // load from specified loader
        assertNull(ClassLoaderUtil.loadServiceClass(null, null, getClass().getClassLoader()));
        assertNull(ClassLoaderUtil.loadServiceClass(null, null, (ClassLoader) null));

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notexist", "myservice.notexist", getClass().getClassLoader());
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notfound", "myservice.notfound", getClass().getClassLoader());
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notexist", "myservice.notexist", (ClassLoader) null);
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.loadServiceClass("myservice.notfound", "myservice.notfound", (ClassLoader) null);
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        assertEquals(MyService.class,
                ClassLoaderUtil.loadServiceClass("myservice", "myservice", getClass().getClassLoader()));
        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("myservice", "myservice", (ClassLoader) null));

        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("com.alibaba.citrus.util.MyService",
                "com.alibaba.citrus.util.MyService", getClass().getClassLoader()));
        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("com.alibaba.citrus.util.MyService",
                "com.alibaba.citrus.util.MyService", (ClassLoader) null));

        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("com.alibaba.citrus.util.MyService", "abc"));
        assertEquals(MyService.class, ClassLoaderUtil.loadServiceClass("abc", "myservice"));
    }

    // ==========================================================================
    // 装入并实例化类的方法。 
    // ==========================================================================

    @Test
    public void newInstance() throws Exception {
        // load from context loader
        assertNull(ClassLoaderUtil.newInstance(null));
        assertInstance(String.class, ClassLoaderUtil.newInstance("java.lang.String"));

        try {
            ClassLoaderUtil.newInstance("com.alibaba.citrus.util.MyErrorService");
            fail("Expected ClassInstantiationException");
        } catch (ClassInstantiationException e) {
        }

        // load from class referrer
        assertNull(ClassLoaderUtil.newInstance(null, getClass()));
        assertNull(ClassLoaderUtil.newInstance(null, (Class<?>) null));
        assertInstance(String.class, ClassLoaderUtil.newInstance("java.lang.String", getClass()));
        assertInstance(String.class, ClassLoaderUtil.newInstance("java.lang.String", (Class<?>) null));

        try {
            ClassLoaderUtil.newInstance("com.alibaba.citrus.util.MyErrorService", getClass());
            fail("Expected ClassInstantiationException");
        } catch (ClassInstantiationException e) {
        }

        // load from specified loader
        assertNull(ClassLoaderUtil.newInstance(null, getClass().getClassLoader()));
        assertNull(ClassLoaderUtil.newInstance(null, (ClassLoader) null));
        assertInstance(String.class, ClassLoaderUtil.newInstance("java.lang.String", getClass().getClassLoader()));
        assertInstance(String.class, ClassLoaderUtil.newInstance("java.lang.String", (ClassLoader) null));

        try {
            ClassLoaderUtil.newInstance("com.alibaba.citrus.util.MyErrorService", getClass().getClassLoader());
            fail("Expected ClassInstantiationException");
        } catch (ClassInstantiationException e) {
        }
    }

    @Test
    public void newServiceInstance() throws Exception {
        // load from context loader
        assertNull(ClassLoaderUtil.newServiceInstance(null));

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notexist");
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notfound");
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.err");
            fail("Expected ClassInstantiationException");
        } catch (ClassInstantiationException e) {
        }

        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("myservice"));

        // load from class referrer
        assertNull(ClassLoaderUtil.newServiceInstance(null, getClass()));
        assertNull(ClassLoaderUtil.newServiceInstance(null, (Class<?>) null));

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notexist", getClass());
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notfound", getClass());
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notexist", (Class<?>) null);
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notfound", (Class<?>) null);
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.err", getClass());
            fail("Expected ClassInstantiationException");
        } catch (ClassInstantiationException e) {
        }

        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("myservice", getClass()));
        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("myservice", (Class<?>) null));

        // load from specified loader
        assertNull(ClassLoaderUtil.newServiceInstance(null, getClass().getClassLoader()));
        assertNull(ClassLoaderUtil.newServiceInstance(null, (ClassLoader) null));

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notexist", getClass().getClassLoader());
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notfound", getClass().getClassLoader());
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notexist", (ClassLoader) null);
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notfound", (ClassLoader) null);
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.err", getClass().getClassLoader());
            fail("Expected ClassInstantiationException");
        } catch (ClassInstantiationException e) {
        }

        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("myservice", getClass().getClassLoader()));
        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("myservice", (ClassLoader) null));

        try {
            assertEquals(MyService.class, ClassLoaderUtil.newServiceInstance("com.alibaba.citrus.util.MyService",
                    getClass().getClassLoader()));
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            assertEquals(MyService.class,
                    ClassLoaderUtil.newServiceInstance("com.alibaba.citrus.util.MyService", (ClassLoader) null));
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }
    }

    @Test
    public void newServiceInstance_ClassNameServiceId() throws Exception {
        // load from context loader
        assertNull(ClassLoaderUtil.newServiceInstance(null, (String) null));

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notexist", "myservice.notexist");
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notfound", "myservice.notfound");
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.err", "myservice.err");
            fail("Expected ClassInstantiationException");
        } catch (ClassInstantiationException e) {
        }

        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("myservice", "myservice"));

        // load from class referrer
        assertNull(ClassLoaderUtil.newServiceInstance(null, null, getClass()));
        assertNull(ClassLoaderUtil.newServiceInstance(null, null, (Class<?>) null));

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notexist", "myservice.notexist", getClass());
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notfound", "myservice.notfound", getClass());
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notexist", "myservice.notexist", (Class<?>) null);
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notfound", "myservice.notfound", (Class<?>) null);
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.err", "myservice.err", getClass());
            fail("Expected ClassInstantiationException");
        } catch (ClassInstantiationException e) {
        }

        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("myservice", "myservice", getClass()));
        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("myservice", "myservice", (Class<?>) null));

        // load from specified loader
        assertNull(ClassLoaderUtil.newServiceInstance(null, null, getClass().getClassLoader()));
        assertNull(ClassLoaderUtil.newServiceInstance(null, null, (ClassLoader) null));

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notexist", "myservice.notexist", getClass().getClassLoader());
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notfound", "myservice.notfound", getClass().getClassLoader());
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notexist", "myservice.notexist", (ClassLoader) null);
            fail("expected ServiceNotFoundException");
        } catch (ServiceNotFoundException e) {
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.notfound", "myservice.notfound", (ClassLoader) null);
            fail("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertFalse(e instanceof ServiceNotFoundException);
        }

        try {
            ClassLoaderUtil.newServiceInstance("myservice.err", "myservice.err", getClass().getClassLoader());
            fail("Expected ClassInstantiationException");
        } catch (ClassInstantiationException e) {
        }

        assertInstance(MyService.class,
                ClassLoaderUtil.newServiceInstance("myservice", "myservice", getClass().getClassLoader()));
        assertInstance(MyService.class,
                ClassLoaderUtil.newServiceInstance("myservice", "myservice", (ClassLoader) null));

        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("com.alibaba.citrus.util.MyService",
                "com.alibaba.citrus.util.MyService", getClass().getClassLoader()));
        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("com.alibaba.citrus.util.MyService",
                "com.alibaba.citrus.util.MyService", (ClassLoader) null));

        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("com.alibaba.citrus.util.MyService", "abc"));
        assertInstance(MyService.class, ClassLoaderUtil.newServiceInstance("abc", "myservice"));
    }

    private void assertInstance(Class<?> clazz, Object obj) {
        assertTrue(clazz.isInstance(obj));
    }

    // ==========================================================================
    // 装入和查找资源文件的方法。 
    // ==========================================================================

    @Test
    public void getResources() throws Exception {
        // load from context loader
        assertEquals(0, ClassLoaderUtil.getResources(null).length);

        URL[] urls = ClassLoaderUtil.getResources("META-INF/MANIFEST.MF");

        assertUnique(urls);

        // load from class referrer
        assertEquals(0, ClassLoaderUtil.getResources(null, getClass()).length);
        assertEquals(0, ClassLoaderUtil.getResources(null, (Class<?>) null).length);
        urls = ClassLoaderUtil.getResources("META-INF/MANIFEST.MF", getClass());
        assertUnique(urls);
        urls = ClassLoaderUtil.getResources("META-INF/MANIFEST.MF", (Class<?>) null);
        assertUnique(urls);

        // load from specified loader
        assertEquals(0, ClassLoaderUtil.getResources(null, getClass().getClassLoader()).length);
        assertEquals(0, ClassLoaderUtil.getResources(null, (ClassLoader) null).length);
        urls = ClassLoaderUtil.getResources("META-INF/MANIFEST.MF", getClass().getClassLoader());
        assertUnique(urls);
        urls = ClassLoaderUtil.getResources("META-INF/MANIFEST.MF", (ClassLoader) null);
        assertUnique(urls);
    }

    @Test
    public void getResource() throws Exception {
        // load from context loader
        assertNull(ClassLoaderUtil.getResource(null));
        assertNotNull(ClassLoaderUtil.getResource("META-INF/MANIFEST.MF"));

        // load from class referrer
        assertNull(ClassLoaderUtil.getResource(null, getClass()));
        assertNull(ClassLoaderUtil.getResource(null, (Class<?>) null));
        assertNotNull(ClassLoaderUtil.getResource("META-INF/MANIFEST.MF", getClass()));
        assertNotNull(ClassLoaderUtil.getResource("META-INF/MANIFEST.MF", (Class<?>) null));

        // load from specified loader
        assertNull(ClassLoaderUtil.getResource(null, getClass().getClassLoader()));
        assertNull(ClassLoaderUtil.getResource(null, (ClassLoader) null));
        assertNotNull(ClassLoaderUtil.getResource("META-INF/MANIFEST.MF", getClass().getClassLoader()));
        assertNotNull(ClassLoaderUtil.getResource("META-INF/MANIFEST.MF", (ClassLoader) null));
    }

    @Test
    public void getResourceAsStream() throws Exception {
        // load from context loader
        assertNull(ClassLoaderUtil.getResourceAsStream(null));
        assertNotNull(ClassLoaderUtil.getResourceAsStream("META-INF/MANIFEST.MF"));

        // load from class referrer
        assertNull(ClassLoaderUtil.getResourceAsStream(null, getClass()));
        assertNull(ClassLoaderUtil.getResourceAsStream(null, (Class<?>) null));
        assertNotNull(ClassLoaderUtil.getResourceAsStream("META-INF/MANIFEST.MF", getClass()));
        assertNotNull(ClassLoaderUtil.getResourceAsStream("META-INF/MANIFEST.MF", (Class<?>) null));

        // load from specified loader
        assertNull(ClassLoaderUtil.getResourceAsStream(null, getClass().getClassLoader()));
        assertNull(ClassLoaderUtil.getResourceAsStream(null, (ClassLoader) null));
        assertNotNull(ClassLoaderUtil.getResourceAsStream("META-INF/MANIFEST.MF", getClass().getClassLoader()));
        assertNotNull(ClassLoaderUtil.getResourceAsStream("META-INF/MANIFEST.MF", (ClassLoader) null));
    }

    private void assertUnique(URL[] urls) {
        assertTrue(urls != null && urls.length > 0);

        Set<URL> set = createHashSet(urls);

        assertEquals(urls.length, set.size());
    }

    // ==========================================================================
    // 查找class的位置。 
    //
    // 类似于UNIX的which方法。 
    // ==========================================================================

    @Test
    public void whichClasses() throws Exception {
        // load from context loader
        assertEquals(0, ClassLoaderUtil.whichClasses(null).length);
        assertEquals(0, ClassLoaderUtil.whichClasses("aaa.NotExistClass").length);

        URL[] urls = ClassLoaderUtil.whichClasses("java.lang.String");

        assertUnique(urls);

        // load from class referrer
        assertEquals(0, ClassLoaderUtil.whichClasses(null, getClass()).length);
        assertEquals(0, ClassLoaderUtil.whichClasses(null, (Class<?>) null).length);
        assertEquals(0, ClassLoaderUtil.whichClasses("aaa.NotExistClass", getClass()).length);
        assertEquals(0, ClassLoaderUtil.whichClasses("aaa.NotExistClass", (Class<?>) null).length);

        urls = ClassLoaderUtil.whichClasses("java.lang.String", getClass());
        assertUnique(urls);
        urls = ClassLoaderUtil.whichClasses("java.lang.String", (Class<?>) null);
        assertUnique(urls);

        // load from specified loader
        assertEquals(0, ClassLoaderUtil.whichClasses(null, getClass().getClassLoader()).length);
        assertEquals(0, ClassLoaderUtil.whichClasses(null, (ClassLoader) null).length);
        assertEquals(0, ClassLoaderUtil.whichClasses("aaa.NotExistClass", getClass().getClassLoader()).length);
        assertEquals(0, ClassLoaderUtil.whichClasses("aaa.NotExistClass", (ClassLoader) null).length);

        urls = ClassLoaderUtil.whichClasses("java.lang.String", getClass().getClassLoader());
        assertUnique(urls);
        urls = ClassLoaderUtil.whichClasses("java.lang.String", (ClassLoader) null);
        assertUnique(urls);
    }

    @Test
    public void whichClass() throws Exception {
        // load from context loader
        assertNull(ClassLoaderUtil.whichClass(null));
        assertNull(ClassLoaderUtil.whichClass("aaa.NotExistClass"));
        assertNotNull(ClassLoaderUtil.whichClass("java.lang.String"));

        // load from class referrer
        assertNull(ClassLoaderUtil.whichClass(null, getClass()));
        assertNull(ClassLoaderUtil.whichClass(null, (Class<?>) null));
        assertNull(ClassLoaderUtil.whichClass("aaa.NotExistClass", getClass()));
        assertNull(ClassLoaderUtil.whichClass("aaa.NotExistClass", (Class<?>) null));

        assertNotNull(ClassLoaderUtil.whichClass("java.lang.String", getClass()));
        assertNotNull(ClassLoaderUtil.whichClass("java.lang.String", (Class<?>) null));

        // load from specified loader
        assertNull(ClassLoaderUtil.whichClass(null, getClass().getClassLoader()));
        assertNull(ClassLoaderUtil.whichClass(null, (ClassLoader) null));
        assertNull(ClassLoaderUtil.whichClass("aaa.NotExistClass", getClass().getClassLoader()));
        assertNull(ClassLoaderUtil.whichClass("aaa.NotExistClass", (ClassLoader) null));

        assertNotNull(ClassLoaderUtil.whichClass("java.lang.String", getClass().getClassLoader()));
        assertNotNull(ClassLoaderUtil.whichClass("java.lang.String", (ClassLoader) null));
    }
}
