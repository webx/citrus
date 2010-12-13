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
package com.alibaba.citrus.util.collection;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * 测试<code>DefaultMapEntry</code>类.
 * 
 * @author Michael Zhou
 */
public class DefaultMapEntryTests {
    private Map.Entry<String, Object> entry1;
    private Map.Entry<String, Object> entry2;

    @Before
    public void init() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(null, null);
        entry1 = map.entrySet().iterator().next();

        map.clear();
        map.put("hello", "baobao");
        entry2 = map.entrySet().iterator().next();
    }

    /**
     * 测试equals方法.
     */
    @Test
    public void equals_() {
        DefaultMapEntry<String, Object> e1 = new DefaultMapEntry<String, Object>(null, null);
        DefaultMapEntry<String, Object> e2 = new DefaultMapEntry<String, Object>("hello", "baobao");

        assertTrue(e1.equals(entry1));
        assertTrue(e2.equals(entry2));

        assertTrue(e1.equals(e1));
        assertFalse(e1.equals(null));
        assertFalse(e1.equals(""));
    }

    /**
     * 测试hashCode方法.
     */
    @Test
    public void hashCode_() {
        assertEquals(entry1.hashCode(), new DefaultMapEntry<String, Object>(null, null).hashCode());

        assertEquals(entry2.hashCode(), new DefaultMapEntry<String, Object>("hello", "baobao").hashCode());
    }

    /**
     * 测试toString方法.
     */
    @Test
    public void toString_() {
        assertEquals(entry1.toString(), new DefaultMapEntry<String, Object>(null, null).toString());

        assertEquals(entry2.toString(), new DefaultMapEntry<String, Object>("hello", "baobao").toString());
    }

    /**
     * 测试setValue方法.
     */
    @Test
    public void setValue() {
        DefaultMapEntry<String, Object> entry = new DefaultMapEntry<String, Object>(null, null);

        assertEquals(null, entry.setValue("hello"));
        assertEquals("hello", entry.getValue());
    }
}
