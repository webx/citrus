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
package com.alibaba.citrus.webx.handler;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.springext.export.SchemaExporterWEB;
import com.alibaba.citrus.util.io.StreamUtil;
import com.alibaba.citrus.webx.AbstractWebxTests;

/**
 * ≤‚ ‘/internal/Webx/Schema°£
 * 
 * @author Michael Zhou
 */
public class SchemaRequestHandlerTests extends AbstractWebxTests {
    @Before
    public void init() throws Exception {
        prepareWebClient(null);
    }

    @Test
    public void internalRequest_schema() throws Exception {
        // schema - list page
        invokeServlet("/internal/Webx/Schema");

        assertEquals(200, clientResponseCode);
        assertEquals("text/html", clientResponse.getContentType());
        assertThat(clientResponseContent,
                containsAll("/Webx/Schema/services.xsd", "/www.springframework.org/schema/beans/spring-beans.xsd"));

        // schema/services - redirect to services/
        invokeServlet("/internal/Webx/Schema/services");

        assertEquals(302, clientResponseCode);
        assertThat(clientResponse.getHeaderField("location"), containsAll("/internal/Webx/Schema/services/"));

        // schema/services/ - list page
        invokeServlet("/internal/Webx/Schema/services/");

        assertEquals(200, clientResponseCode);
        assertEquals("text/html", clientResponse.getContentType());
        assertThat(clientResponseContent,
                containsAll("/Webx/Schema/services/pipeline.xsd", "/Webx/Schema/services/request-contexts.xsd"));

        // schema/services.xsd - schema page
        invokeServlet("/internal/Webx/Schema/services.xsd");

        assertEquals(200, clientResponseCode);
        assertEquals("text/xml", clientResponse.getContentType());
        assertThat(clientResponseContent,
                containsAll("targetNamespace=\"" + "http://www.alibaba.com/schema/services\""));

        // schema/file.gif - resource page
        invokeServlet("/internal/Webx/Schema/file.gif");

        assertEquals(200, clientResponseCode);
        assertEquals("image/gif", clientResponse.getContentType());

        byte[] content = StreamUtil.readBytes(clientResponse.getInputStream(), true).toByteArray();
        byte[] fileContent = StreamUtil.readBytes(SchemaExporterWEB.class.getResource("file.gif").openStream(), true)
                .toByteArray();

        assertArrayEquals(fileContent, content);
    }
}
