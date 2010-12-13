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

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.support.MappedTemplateContext;
import com.alibaba.citrus.service.velocity.impl.TemplateContextAdapter;

public class TemplateContextAdapaterTests {
    private TemplateContext context;
    private TemplateContextAdapter adapter;

    @Before
    public void init() {
        context = new MappedTemplateContext();
        adapter = new TemplateContextAdapter(context);

        context.put("aaa", 111);
        context.put("bbb", 222);
        context.put("ccc", 333);
    }

    @Test
    public void get() {
        assertEquals(111, adapter.get("aaa"));
        assertEquals(222, adapter.get("bbb"));
        assertEquals(333, adapter.get("ccc"));
        assertEquals(null, adapter.get("ddd"));
    }

    @Test
    public void put() {
        assertEquals(111, adapter.put("aaa", 1111));
        assertEquals(222, adapter.put("bbb", 2222));
        assertEquals(333, adapter.put("ccc", 3333));
        assertEquals(null, adapter.put("ddd", 4444));

        assertEquals(1111, adapter.get("aaa"));
        assertEquals(2222, adapter.get("bbb"));
        assertEquals(3333, adapter.get("ccc"));
        assertEquals(4444, adapter.get("ddd"));

        assertEquals(1111, context.get("aaa"));
        assertEquals(2222, context.get("bbb"));
        assertEquals(3333, context.get("ccc"));
        assertEquals(4444, context.get("ddd"));
    }

    @Test
    public void containsKey() {
        assertEquals(true, adapter.containsKey("aaa"));
        assertEquals(true, adapter.containsKey("bbb"));
        assertEquals(true, adapter.containsKey("ccc"));
        assertEquals(false, adapter.containsKey("ddd"));
    }

    @Test
    public void getKeys() {
        Object[] keys = adapter.getKeys();
        Arrays.sort(keys);
        assertArrayEquals(new Object[] { "aaa", "bbb", "ccc" }, keys);
    }

    @Test
    public void remove() {
        assertEquals(111, adapter.remove("aaa"));
        assertEquals(222, adapter.remove("bbb"));
        assertEquals(null, adapter.remove("ddd"));

        assertEquals(null, adapter.get("aaa"));
        assertEquals(null, adapter.get("bbb"));
        assertEquals(333, adapter.get("ccc"));
        assertEquals(null, adapter.get("ddd"));

        assertEquals(null, context.get("aaa"));
        assertEquals(null, context.get("bbb"));
        assertEquals(333, context.get("ccc"));
        assertEquals(null, context.get("ddd"));
    }
}
