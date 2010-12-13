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
package com.alibaba.citrus.util.internal.webpagelite.myprocessor;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.util.internal.webpagelite.AbstractRequestProcessorTests;
import com.alibaba.citrus.util.internal.webpagelite.RequestContext;
import com.alibaba.citrus.util.internal.webpagelite.RequestProcessor;

public class RequestProcessorTests extends AbstractRequestProcessorTests {
    private Map<String, String> textResources;
    private MyProcessor page;

    @Before
    public void init() {
        textResources = createHashMap();

        textResources.put("file1", "file1 content");
        textResources.put("path/to/", "list");
        textResources.put("path/to/file2", "file2 content");

        page = new MyProcessor();
    }

    @Test
    public void requestContext_getWriter() throws IOException {
        RequestContext context = new MyRequest("http://localhost", "/dummy.txt");

        try {
            context.getWriter();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("call getWriter(contentType) first"));
        }

        PrintWriter writer = context.getWriter("text/plain");

        assertSame(writer, context.getWriter());
        assertSame(writer, context.getWriter("text/plain"));
    }

    @Test
    public void requestContext_getOutputStream() throws IOException {
        RequestContext context = new MyRequest("http://localhost", "/dummy.txt");

        try {
            context.getOutputStream();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("call getOutputStream(contentType) first"));
        }

        OutputStream stream = context.getOutputStream("text/plain");

        assertSame(stream, context.getOutputStream());
        assertSame(stream, context.getOutputStream("text/plain"));
    }

    @Test
    public void renderResource() throws IOException {
        page.processRequest(new MyRequest("http://localhost", "/dummy.txt"));

        assertEquals("text/plain", contentType);
        assertEquals("dummy", new String(baos.toByteArray()).trim());
        assertEquals(null, redirectUrl);
        assertEquals(null, resourceNotFound);

        // actually located at superclass' package
        page.processRequest(new MyRequest("http://localhost", "/prototype.js"));

        assertEquals("application/javascript", contentType);
        assertTrue(baos.toByteArray().length > 0);
        assertEquals(null, redirectUrl);
        assertEquals(null, resourceNotFound);
    }

    @Test
    public void renderResourceTemplate() throws IOException {
        page.processRequest(new MyRequest("http://localhost", "/style.txt"));

        assertEquals("text/plain", contentType);
        assertEquals("http://localhost/style.css", sw.toString().trim());
        assertEquals(null, redirectUrl);
        assertEquals(null, resourceNotFound);
    }

    @Test
    public void renderListRedirect() throws IOException {
        page.processRequest(new MyRequest("http://localhost", "/path/to"));

        assertEquals(null, contentType);
        assertEquals("http://localhost/path/to/", redirectUrl);
        assertEquals(null, resourceNotFound);
    }

    @Test
    public void renderList() throws IOException {
        page.processRequest(new MyRequest("http://localhost", "/path/to/"));

        assertEquals("text/plain", contentType);
        assertEquals("list", sw.toString().trim());
        assertEquals(null, redirectUrl);
        assertEquals(null, resourceNotFound);
    }

    @Test
    public void notFound() throws IOException {
        page.processRequest(new MyRequest("http://localhost", "/path/to/notFound"));

        assertEquals(null, contentType);
        assertEquals(null, redirectUrl);
        assertEquals("path/to/notFound", resourceNotFound);
    }

    private class MyProcessor extends RequestProcessor<MyRequest> {
        @Override
        protected void renderPage(MyRequest request, String resourceName) throws IOException {
            PrintWriter out = request.getWriter("text/plain");
            out.println(textResources.get(resourceName));
        }

        @Override
        protected boolean resourceExists(String resourceName) {
            return textResources.containsKey(resourceName);
        }
    }
}
