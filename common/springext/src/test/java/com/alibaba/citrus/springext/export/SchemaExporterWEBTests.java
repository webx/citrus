/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.springext.export;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.springext.export.SchemaExporterServlet.RedirectToSchema;
import com.alibaba.citrus.util.io.StreamUtil;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.javascript.JavaScript;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import org.junit.Before;
import org.junit.Test;

public class SchemaExporterWEBTests {
    // web client
    protected ServletUnitClient client;
    protected WebResponse       clientResponse;
    protected int               clientResponseCode;
    protected String            clientResponseContent;

    @Before
    public void init() throws Exception {
        initServletContainer("web.xml");
    }

    private void initServletContainer(String webxml) throws Exception {
        // Servlet container
        File webInf = new File(srcdir, "WEB-INF");
        File webXml = new File(webInf, webxml);

        ServletRunner servletRunner = new ServletRunner(webXml, "");

        // Servlet client
        client = servletRunner.newClient();
        client.setExceptionsThrownOnErrorStatus(false);
        client.getClientProperties().setAutoRedirect(false);

        // Ignore script error
        JavaScript.setThrowExceptionsOnError(false);
    }

    /** 调用servlet，取得request/response。 */
    protected final void invokeServlet(String uri) throws Exception {
        invokeServlet(uri, null);
    }

    protected final void invokeServlet(String uri, Map<String, String> params) throws Exception {
        if (uri != null && uri.startsWith("http")) {
            uri = URI.create(uri).normalize().toString(); // full uri
        } else {
            uri = URI.create("http://www.taobao.com/" + trimToEmpty(uri)).normalize().toString(); // partial uri
        }

        if (params == null || params.isEmpty()) {
            clientResponse = client.getResponse(uri);
        } else {
            GetMethodWebRequest request = new GetMethodWebRequest(uri);

            for (Map.Entry<String, String> entry : params.entrySet()) {
                request.setParameter(entry.getKey(), entry.getValue());
            }

            clientResponse = client.getResponse(request);
        }

        clientResponseCode = clientResponse.getResponseCode();
        clientResponseContent = clientResponse.getText();
    }

    @Test
    public void schemas() throws Exception {
        // schema - list page
        invokeServlet("/schema");

        assertEquals(200, clientResponseCode);
        assertEquals("text/html", clientResponse.getContentType());
        assertThat(clientResponseContent,
                   containsAll("/schema/services.xsd", "/www.springframework.org/schema/beans/spring-beans.xsd"));

        // schema/services - redirect to services/
        invokeServlet("/schema/services");

        assertEquals(302, clientResponseCode);
        assertThat(clientResponse.getHeaderField("location"), containsAll("/schema/services/"));

        // schema/services/ - list page
        invokeServlet("/schema/services/");

        assertEquals(200, clientResponseCode);
        assertEquals("text/html", clientResponse.getContentType());
        assertThat(clientResponseContent,
                   containsAll("/schema/services/container.xsd", "/schema/services/tools/dateformat.xsd"));

        // schema/services.xsd - schema page
        invokeServlet("/schema/services.xsd");

        assertEquals(200, clientResponseCode);
        assertEquals("text/xml", clientResponse.getContentType());
        assertThat(clientResponseContent,
                   containsAll("targetNamespace=\"" + "http://www.alibaba.com/schema/services\""));

        // schema/file.gif - resource page
        invokeServlet("/schema/file.gif");

        assertEquals(200, clientResponseCode);
        assertEquals("image/gif", clientResponse.getContentType());

        byte[] content = StreamUtil.readBytes(clientResponse.getInputStream(), true).toByteArray();
        byte[] fileContent = StreamUtil.readBytes(getClass().getResource("file.gif").openStream(), true).toByteArray();

        assertArrayEquals(fileContent, content);
    }

    @Test
    public void redirect() throws Exception {
        initServletContainer("web_withRedirect.xml");

        RedirectToSchema servlet = (RedirectToSchema) client.newInvocation("http://localhost/").getServlet();
        assertEquals("/schema", getFieldValue(servlet, "prefix", String.class));

        Map<String, String> params;

        // GET /
        invokeServlet("/");
        assertEquals(302, clientResponseCode);
        assertEquals("/schema/", clientResponse.getHeaderField("Location"));

        // GET /?a=1
        params = createLinkedHashMap();
        params.put("a", "1");

        invokeServlet("/", params);
        assertEquals(302, clientResponseCode);
        assertEquals("/schema/?a=1", clientResponse.getHeaderField("Location"));

        // GET /services?a=1
        params = createLinkedHashMap();
        params.put("a", "1");

        invokeServlet("/services", params);
        assertEquals(302, clientResponseCode);
        assertEquals("/schema/services?a=1", clientResponse.getHeaderField("Location"));

        // GET /schema/services/ - list page
        invokeServlet("/schema/services/");

        assertEquals(200, clientResponseCode);
        assertEquals("text/html", clientResponse.getContentType());
        assertThat(clientResponseContent,
                   containsAll("/schema/services/container.xsd", "/schema/services/tools/dateformat.xsd"));
    }

    public static class JavaScriptFilter implements Filter {
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                                                                                                         ServletException {
            if (!((HttpServletRequest) request).getRequestURI().endsWith("scriptaculous.js")) {
                chain.doFilter(request, response);
            }
        }

        public void destroy() {
        }
    }
}
