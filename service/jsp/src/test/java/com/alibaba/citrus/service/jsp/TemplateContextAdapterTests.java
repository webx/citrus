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
package com.alibaba.citrus.service.jsp;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.jsp.impl.TemplateContextAdapter;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.support.MappedTemplateContext;

public class TemplateContextAdapterTests {
    private HttpServletRequest request;
    private TemplateContext context;
    private TemplateContextAdapter adapter;

    @Before
    public void init() {
        request = createMock(HttpServletRequest.class);
        replay(request);

        final Hashtable<String, Object> map = new Hashtable<String, Object>();

        map.put("aaa", 111);
        map.put("bbb", 222);
        map.put("ccc", 333);

        request = new HttpServletRequestWrapper(request) {
            @Override
            public Object getAttribute(String name) {
                return map.get(name);
            }

            @Override
            public void setAttribute(String name, Object o) {
                map.put(name, o);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return map.keys();
            }

            @Override
            public void removeAttribute(String name) {
                map.remove(name);
            }
        };

        context = new MappedTemplateContext();

        context.put("ccc", 3333);
        context.put("ddd", 4444);

        adapter = new TemplateContextAdapter(request, context);
    }

    @Test
    public void newInstance() {
        try {
            new TemplateContextAdapter(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("request"));
        }

        try {
            new TemplateContextAdapter(request, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("templateContext"));
        }
    }

    @Test
    public void getTemplateContext() {
        assertSame(context, adapter.getTemplateContext());
    }

    @Test
    public void getAttributeNames() {
        List<String> keys = createArrayList();

        for (Enumeration<String> e = adapter.getAttributeNames(); e.hasMoreElements();) {
            keys.add(e.nextElement());
        }

        Collections.sort(keys);

        assertArrayEquals(new String[] { "aaa", "bbb", "ccc", "ddd" }, keys.toArray(new String[keys.size()]));
    }

    @Test
    public void getAttribute() {
        assertEquals(111, adapter.getAttribute("aaa")); // from request
        assertEquals(222, adapter.getAttribute("bbb")); // from request
        assertEquals(3333, adapter.getAttribute("ccc")); // from context
        assertEquals(4444, adapter.getAttribute("ddd")); // from context
        assertEquals(null, adapter.getAttribute("eee")); // not exist
    }

    @Test
    public void setAttribute() {
        adapter.setAttribute("aaa", 1111);
        adapter.setAttribute("bbb", 2222);

        assertEquals(1111, adapter.getAttribute("aaa")); // from request
        assertEquals(2222, adapter.getAttribute("bbb")); // from request
        assertEquals(3333, adapter.getAttribute("ccc")); // from context
        assertEquals(4444, adapter.getAttribute("ddd")); // from context
        assertEquals(null, adapter.getAttribute("eee")); // not exist

        assertEquals(null, context.get("aaa"));
        assertEquals(null, context.get("bbb"));
        assertEquals(3333, context.get("ccc"));
        assertEquals(4444, context.get("ddd"));
        assertEquals(null, context.get("eee"));

        assertEquals(1111, request.getAttribute("aaa"));
        assertEquals(2222, request.getAttribute("bbb"));
        assertEquals(333, request.getAttribute("ccc"));
        assertEquals(null, request.getAttribute("ddd"));
        assertEquals(null, request.getAttribute("eee"));
    }

    @Test
    public void removeAttribute() {
        adapter.removeAttribute("aaa");
        adapter.removeAttribute("bbb");

        assertEquals(null, adapter.getAttribute("aaa")); // not exist
        assertEquals(null, adapter.getAttribute("bbb")); // not exist
        assertEquals(3333, adapter.getAttribute("ccc")); // from context
        assertEquals(4444, adapter.getAttribute("ddd")); // from context
        assertEquals(null, adapter.getAttribute("eee")); // not exist

        assertEquals(null, context.get("aaa"));
        assertEquals(null, context.get("bbb"));
        assertEquals(3333, context.get("ccc"));
        assertEquals(4444, context.get("ddd"));
        assertEquals(null, context.get("eee"));

        assertEquals(null, request.getAttribute("aaa"));
        assertEquals(null, request.getAttribute("bbb"));
        assertEquals(333, request.getAttribute("ccc"));
        assertEquals(null, request.getAttribute("ddd"));
        assertEquals(null, request.getAttribute("eee"));
    }
}
