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
package com.alibaba.citrus.util.internal.webpagelite;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.junit.Test;

import com.alibaba.citrus.test.TestUtil;
import com.alibaba.citrus.util.internal.webpagelite.simple.SimpleComponent;

public class PageComponentTests extends AbstractRequestProcessorTests {
    @Test
    public void getComponentPaths() {
        MyProcessor processor = new MyProcessor();
        assertArrayEquals(new String[] { "simple/" }, processor.getComponentPaths());

        new SimpleComponent(processor, "a");
        new SimpleComponent(processor, "x/c");
        new SimpleComponent(processor, "x/b");
        new SimpleComponent(processor, "x/b/c");
        new SimpleComponent(processor, "x/b/d");

        // ◊¢“‚≈≈–Ú
        assertArrayEquals(new String[] { "simple/", "x/b/c/", "x/b/d/", "x/b/", "x/c/", "a/" },
                processor.getComponentPaths());
    }

    @Test
    public void dupComponent() {
        MyProcessor processor = new MyProcessor();

        try {
            new SimpleComponent(processor, "simple");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, TestUtil.exception("duplicated component", "simple"));
        }
    }

    @Test
    public void getComponent() {
        MyProcessor processor = new MyProcessor();
        SimpleComponent sc = processor.getComponent("simple", SimpleComponent.class);
        assertSame(processor.simple, sc);

        // without type
        sc = processor.getComponent("simple", null);
        assertSame(processor.simple, sc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getComponent_notFound() {
        MyProcessor processor = new MyProcessor();
        processor.getComponent("notFound", SimpleComponent.class);
    }

    @Test(expected = ClassCastException.class)
    public void getComponent_wrongType() {
        MyProcessor processor = new MyProcessor();

        class OtherComponent extends PageComponent {
            public OtherComponent(PageComponentRegistry registry, String componentPath) {
                super(registry, componentPath);
            }
        }

        processor.getComponent("simple", OtherComponent.class);
    }

    @Test
    public void componentPath() throws Exception {
        MyProcessor processor = new MyProcessor();

        processor.processRequest(new MyRequest("http://localhost", ""));

        assertEquals("text/plain", contentType);
        assertEquals("simple template: http://localhost/simple/test.gif\n", sw.toString());
    }

    @Test
    public void findComponentResource() throws Exception {
        MyProcessor processor = new MyProcessor();

        processor.processRequest(new MyRequest("http://localhost", "/simple/test.gif"));

        assertEquals("image/gif", contentType);
        assertEquals("i'm gif", new String(baos.toByteArray()));
    }

    @Test
    public void findComponentResource_fallback() throws Exception {
        MyProcessor processor = new MyProcessor();

        processor.processRequest(new MyRequest("http://localhost", "/simple/dummy2.txt"));

        assertEquals("text/plain", contentType);
        assertEquals("dummy2", new String(baos.toByteArray()));
    }

    @Test
    public void findRawResource() throws Exception {
        MyProcessor processor = new MyProcessor();

        processor.processRequest(new MyRequest("http://localhost", "/dummy2.txt"));

        assertEquals("text/plain", contentType);
        assertEquals("dummy2", new String(baos.toByteArray()));
    }

    @Test
    public void getComponentResources() {
        MyProcessor processor = new MyProcessor();

        List<String> css = processor.getComponentResources("css");

        assertArrayEquals(new Object[] { "simple/simple.css" }, css.toArray());

        List<String> js = processor.getComponentResources(".js");

        assertArrayEquals(new Object[] {}, js.toArray());
    }

    private static class MyProcessor extends RequestProcessor<MyRequest> {
        private final SimpleComponent simple = new SimpleComponent(this, "simple");

        @Override
        protected void renderPage(MyRequest request, String resourceName) throws IOException {
            PrintWriter out = request.getWriter("text/plain");

            simple.visitTemplate(out, request);
        }

        @Override
        protected boolean resourceExists(String resourceName) {
            return true;
        }
    }
}
