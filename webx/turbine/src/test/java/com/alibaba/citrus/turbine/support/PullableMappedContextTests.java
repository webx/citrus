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
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.pull.PullContext;
import com.alibaba.citrus.turbine.Context;

public class PullableMappedContextTests {
    private Context parent;
    private Context context;

    @Before
    public void init() {
        PullContext pullContext = createMock(PullContext.class);

        expect(pullContext.pull("pull1")).andReturn(111).anyTimes();
        expect(pullContext.pull("pull2")).andReturn(222).anyTimes();

        Set<String> names = createHashSet(Arrays.asList("pull1", "pull2"));
        expect(pullContext.getToolNames()).andReturn(names).anyTimes();

        expect(pullContext.pull(org.easymock.EasyMock.<String> anyObject())).andReturn(null).anyTimes();

        replay(pullContext);

        parent = new PullableMappedContext(pullContext);
        parent.put("parent", 333);

        context = new MappedContext(parent);
        context.put("child", 444);
    }

    @Test
    public void create_no_pullContext() {
        context = new PullableMappedContext(null);

        context.put("aaa", 111);

        assertTrue(context.containsKey("aaa"));
        assertEquals(111, context.get("aaa"));

        assertFalse(context.containsKey("bbb"));
        assertNull(context.get("bbb"));
    }

    @Test
    public void containsKey() {
        assertTrue(context.containsKey("pull1"));
        assertTrue(context.containsKey("pull2"));
        assertTrue(context.containsKey("parent"));
        assertTrue(context.containsKey("child"));

        assertFalse(context.containsKey("other"));
    }

    @Test
    public void get() {
        assertEquals(111, context.get("pull1"));
        assertEquals(222, context.get("pull2"));
        assertEquals(333, context.get("parent"));
        assertEquals(444, context.get("child"));

        assertEquals(null, context.get("other"));
    }

    @Test
    public void keySet() {
        assertKeySet(parent, "parent", "pull1", "pull2");
        assertKeySet(context, "child", "parent", "pull1", "pull2");
    }

    private void assertKeySet(Context ctx, String... keys) {
        List<String> keyList = createArrayList(ctx.keySet());
        Collections.sort(keyList);

        assertArrayEquals(keys, keyList.toArray(new String[keyList.size()]));
    }
}
