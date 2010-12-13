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
package com.alibaba.citrus.springext.export;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.util.io.StreamUtil;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.javascript.JavaScript;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class SchemaExporterWEBTests {
    // web client
    protected ServletUnitClient client;
    protected WebResponse clientResponse;
    protected int clientResponseCode;
    protected String clientResponseContent;

    @Before
    public void init() throws Exception {
        // Servlet container
        File webInf = new File(srcdir, "WEB-INF");
        File webXml = new File(webInf, "web.xml");

        ServletRunner servletRunner = new ServletRunner(webXml, "");

        // Servlet client
        client = servletRunner.newClient();
        client.setExceptionsThrownOnErrorStatus(false);
        client.getClientProperties().setAutoRedirect(false);

        // Ignore script error
        JavaScript.setThrowExceptionsOnError(false);
    }

    /**
     * 调用servlet，取得request/response。
     */
    protected final void invokeServlet(String uri) throws Exception {
        if (uri != null && uri.startsWith("http")) {
            uri = URI.create(uri).normalize().toString(); // full uri
        } else {
            uri = URI.create("http://www.taobao.com/" + trimToEmpty(uri)).normalize().toString(); // partial uri
        }

        clientResponse = client.getResponse(uri);
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
}
