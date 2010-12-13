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
import static com.alibaba.citrus.util.ServletUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ServletUtilTests implements Cloneable {
    private HttpServletRequest request;
    private final boolean isPrefixServletMapping;
    private final String servletPath;
    private final String pathInfo;
    private final String resourcePath;
    private final String baseURL;
    private final String servletResourcePath;
    private final String servletBaseURL;
    private final String fullURL;

    public ServletUtilTests(boolean isPrefixServletMapping, String servletPath, String pathInfo, String resourcePath,
                            String baseURL, String servletResourcePath, String servletBaseURL, String fullURL) {
        this.isPrefixServletMapping = isPrefixServletMapping;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.resourcePath = resourcePath;
        this.baseURL = baseURL;
        this.servletResourcePath = servletResourcePath;
        this.servletBaseURL = servletBaseURL;
        this.fullURL = fullURL;
    }

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data = createLinkedList();

        // Ç°×ºÓ³Éä£¬pathInfo²»Îª¿Õ
        add(data, true, "/turbine", "/aaa/bbb", //
                "/turbine/aaa/bbb", "http://localhost:8080/myapp", //
                "/aaa/bbb", "http://localhost:8080/myapp/turbine", //
                "http://localhost:8080/myapp/turbine/aaa/bbb");

        add(data, true, "/hello.world", "/turbine", //
                "/hello.world/turbine", "http://localhost:8080/myapp", //
                "/turbine", "http://localhost:8080/myapp/hello.world", //
                "http://localhost:8080/myapp/hello.world/turbine");

        add(data, true, " hello.world ", " turbine/ ", //
                "/hello.world/turbine/", "http://localhost:8080/myapp", //
                "/turbine/", "http://localhost:8080/myapp/hello.world", //
                "http://localhost:8080/myapp/hello.world/turbine/");

        add(data, true, null, " / ", //
                "/", "http://localhost:8080/myapp", //
                "/", "http://localhost:8080/myapp", //
                "http://localhost:8080/myapp/");

        // Ç°×ºÓ³Éä£¬µ«pathInfoÎª¿Õ
        add(data, true, "/turbine", null, //
                "/turbine", "http://localhost:8080/myapp", //
                "", "http://localhost:8080/myapp/turbine", //
                "http://localhost:8080/myapp/turbine");

        add(data, true, "/turbine", "", //
                "/turbine", "http://localhost:8080/myapp", //
                "", "http://localhost:8080/myapp/turbine", //
                "http://localhost:8080/myapp/turbine");

        add(data, true, "/hello.world/turbine", null, //
                "/hello.world/turbine", "http://localhost:8080/myapp", //
                "", "http://localhost:8080/myapp/hello.world/turbine", //
                "http://localhost:8080/myapp/hello.world/turbine");

        add(data, true, "/hello.world/turbine", "", //
                "/hello.world/turbine", "http://localhost:8080/myapp", //
                "", "http://localhost:8080/myapp/hello.world/turbine", //
                "http://localhost:8080/myapp/hello.world/turbine");

        add(data, true, "  hello.world/turbine/ ", " ", //
                "/hello.world/turbine/", "http://localhost:8080/myapp", //
                "", "http://localhost:8080/myapp/hello.world/turbine", //
                "http://localhost:8080/myapp/hello.world/turbine/");

        // ºó×ºÓ³Éä
        add(data, false, "/aaa/bbb.htm", null, //
                "/aaa/bbb.htm", "http://localhost:8080/myapp", //
                "/aaa/bbb.htm", "http://localhost:8080/myapp", //
                "http://localhost:8080/myapp/aaa/bbb.htm");

        add(data, false, "/aaa/bbb.htm", "", //
                "/aaa/bbb.htm", "http://localhost:8080/myapp", //
                "/aaa/bbb.htm", "http://localhost:8080/myapp", //
                "http://localhost:8080/myapp/aaa/bbb.htm");

        add(data, false, " aaa/bbb.htm ", " ", //
                "/aaa/bbb.htm", "http://localhost:8080/myapp", //
                "/aaa/bbb.htm", "http://localhost:8080/myapp", //
                "http://localhost:8080/myapp/aaa/bbb.htm");

        return data;
    }

    private static void add(List<Object[]> data, Object... values) {
        data.add(values);
    }

    @Before
    public void init() {
        request = createMock(HttpServletRequest.class);

        expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost:8080/test")).anyTimes();
        expect(request.getContextPath()).andReturn(" myapp ").anyTimes();
        expect(request.getServletPath()).andReturn(servletPath).anyTimes();
        expect(request.getPathInfo()).andReturn(pathInfo).anyTimes();

        replay(request);
    }

    @Test
    public void isPrefixServletMapping_() {
        assertEquals(isPrefixServletMapping, isPrefixServletMapping(request));
    }

    @Test
    public void getResourcePath_() {
        assertEquals(resourcePath, getResourcePath(request));
    }

    @Test
    public void getBaseURL_() {
        assertEquals(baseURL, getBaseURL(request));
    }

    @Test
    public void getServletResourcePath_() {
        assertEquals(servletResourcePath, getServletResourcePath(request));
    }

    @Test
    public void getServletBaseURL_() {
        assertEquals(servletBaseURL, getServletBaseURL(request));
    }

    @Test
    public void getFullURL_() {
        assertEquals(fullURL, getBaseURL(request) + getResourcePath(request));

        String servletFullURL = getServletBaseURL(request) + getServletResourcePath(request);
        assertTrue(fullURL.equals(servletFullURL) || fullURL.equals(servletFullURL + "/"));
    }

    @Test
    public void normalizePath_() {
        assertEquals("", normalizePath(null));
        assertEquals("", normalizePath("  "));
        assertEquals("/aa/bb", normalizePath("  //aa/bb "));

        assertEquals("/", normalizePath("  /"));
        assertEquals("/", normalizePath("  // "));
        assertEquals("/aa/bb/", normalizePath("  //aa/bb/ "));
        assertEquals("/aa/bb/", normalizePath("  //aa/bb/ "));
    }

    @Test
    public void normalizePath_removeTrailingSlash() {
        assertEquals("", normalizePath(null, false));
        assertEquals("", normalizePath("  ", false));
        assertEquals("/aa/bb", normalizePath("  //aa/bb ", false));

        assertEquals("", normalizePath("  /", false));
        assertEquals("", normalizePath("  // ", false));
        assertEquals("/aa/bb", normalizePath("  //aa/bb/ ", false));
    }

    private String normalizePath(String path) {
        return FileUtil.normalizeAbsolutePath(path, false);
    }

    private String normalizePath(String path, boolean reserveTrailingSlash) {
        return FileUtil.normalizeAbsolutePath(path, !reserveTrailingSlash);
    }

    @Test
    public void normalizeURI_() {
        assertEquals("", normalizeURI(null));
        assertEquals("", normalizeURI(" "));

        assertEquals("aaa/bbb/ccc", normalizeURI("aaa/bbb/ccc"));
        assertEquals("aaa/ccc", normalizeURI("aaa///bbb/../ccc"));
        assertEquals("/aaa/bbb/ccc", normalizeURI("/aaa/bbb/ccc"));
        assertEquals("/aaa/bbb/ccc/", normalizeURI("/aaa/bbb/ccc/"));

        assertEquals("http://localhost:8080/bbb/ccc/", normalizeURI("http://localhost:8080//aaa//../bbb/ccc/"));
    }
}
