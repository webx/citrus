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
package com.alibaba.citrus.turbine.support;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.turbine.Context;

public class HierarchicalContextTests {
    private Context parentContext;
    private Context context;

    @Before
    public void init() {
        parentContext = new MappedContext();
        parentContext.put("parent1", "111");
        parentContext.put("parent2", "222");
        parentContext.put("parent&child", "333");
        parentContext.put("parentNull", null);

        context = new MappedContext(parentContext);
        context.put("parent&child", "444");
        context.put("child", "555");
    }

    @Test
    public void get() {
        assertGet("parent1", "111", "111");
        assertGet("parent2", "222", "222");
        assertGet("parent&child", "333", "444");
        assertGet("parentNull", null, null);
        assertGet("child", null, "555");
    }

    private void assertGet(String key, String parentValue, String childValue) {
        assertEquals(parentValue, parentContext.get(key));
        assertEquals(childValue, context.get(key));
    }

    @Test
    public void containsKey() {
        assertContainsKey("parent1", true, true);
        assertContainsKey("parent2", true, true);
        assertContainsKey("parent&child", true, true);
        assertContainsKey("parentNull", false, false);
        assertContainsKey("child", false, true);
    }

    private void assertContainsKey(String key, boolean parentValue, boolean childValue) {
        assertEquals(parentValue, parentContext.containsKey(key));
        assertEquals(childValue, context.containsKey(key));
    }

    @Test
    public void keySet() {
        assertKeySet(parentContext, "parent&child", "parent1", "parent2");
        assertKeySet(context, "child", "parent&child", "parent1", "parent2");
    }

    private void assertKeySet(Context ctx, String... keys) {
        List<String> keyList = createArrayList(ctx.keySet());
        Collections.sort(keyList);

        assertArrayEquals(keys, keyList.toArray(new String[keyList.size()]));
    }

    @Test
    public void remove() {
        context.remove("parent1");
        context.remove("parent2");
        context.remove("parent&child");
        context.remove("parentNull");
        context.remove("child");

        assertTrue(context.containsKey("parent1"));
        assertTrue(context.containsKey("parent2"));
        assertTrue(context.containsKey("parent&child"));
        assertFalse(context.containsKey("parentNull"));
        assertFalse(context.containsKey("child"));

        assertNull(context.get("parent1"));
        assertNull(context.get("parent2"));
        assertNull(context.get("parent&child"));
        assertNull(context.get("parentNull"));
        assertNull(context.get("child"));
    }

    @Test
    public void put() {
        context.put("parent1", "1111");
        context.put("parent2", "2222");
        context.put("parent&child", null);
        context.put("parentNull", "nullnull");
        context.put("child", "5555");

        assertTrue(context.containsKey("parent1"));
        assertTrue(context.containsKey("parent2"));
        assertTrue(context.containsKey("parent&child"));
        assertTrue(context.containsKey("parentNull"));
        assertTrue(context.containsKey("child"));

        assertEquals("1111", context.get("parent1"));
        assertEquals("2222", context.get("parent2"));
        assertNull(context.get("parent&child"));
        assertEquals("nullnull", context.get("parentNull"));
        assertEquals("5555", context.get("child"));

        context.put("child", null);
        assertFalse(context.containsKey("child"));
        assertNull(context.get("child"));
    }
}
