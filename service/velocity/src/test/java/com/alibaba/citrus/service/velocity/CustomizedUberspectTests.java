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
package com.alibaba.citrus.service.velocity;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.Uberspect;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.velocity.impl.CustomizedUberspectImpl;

public class CustomizedUberspectTests {
    private Uberspect uberspect;

    @Before
    public void init() throws Exception {
        uberspect = new CustomizedUberspectImpl();
        uberspect.init();
    }

    @Test
    public void get() throws Exception {
        VelPropertyGet getMethod = uberspect.getPropertyGet(new MyClass1(), "object", new Info("test.vm", 1, 1));
        assertEquals("get", getMethod.getMethodName());
    }

    @Test
    public void map_get() throws Exception {
        VelPropertyGet getMethod = uberspect.getPropertyGet(new MyClass1_1(), "object", new Info("test.vm", 1, 1));
        assertEquals("get", getMethod.getMethodName());
    }

    @Test
    public void isObject() throws Exception {
        VelPropertyGet getMethod = uberspect.getPropertyGet(new MyClass2(), "object", new Info("test.vm", 1, 1));
        assertEquals("isObject", getMethod.getMethodName());
    }

    @Test
    public void isobject() throws Exception {
        VelPropertyGet getMethod = uberspect.getPropertyGet(new MyClass2_1(), "object", new Info("test.vm", 1, 1));
        assertEquals("isobject", getMethod.getMethodName());
    }

    @Test
    public void getObject() throws Exception {
        VelPropertyGet getMethod = uberspect.getPropertyGet(new MyClass3(), "object", new Info("test.vm", 1, 1));
        assertEquals("getObject", getMethod.getMethodName());
    }

    @Test
    public void getobject() throws Exception {
        VelPropertyGet getMethod = uberspect.getPropertyGet(new MyClass3_1(), "object", new Info("test.vm", 1, 1));
        assertEquals("getobject", getMethod.getMethodName());
    }

    public static class MyClass1 {
        public boolean get(String key) {
            return false;
        }
    }

    public static class MyClass1_1 extends HashMap<String, Object> implements Map<String, Object> {
        private static final long serialVersionUID = -7637009372408915420L;
    }

    public static class MyClass2 extends MyClass1 {
        public boolean isObject() {
            return false;
        }
    }

    public static class MyClass2_1 extends MyClass1 {
        public boolean isobject() {
            return false;
        }
    }

    public static class MyClass3 extends MyClass2 {
        public boolean getObject() {
            return false;
        }
    }

    public static class MyClass3_1 extends MyClass2 {
        public boolean getobject() {
            return false;
        }
    }
}
