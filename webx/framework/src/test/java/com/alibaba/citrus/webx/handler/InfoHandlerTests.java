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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.webx.AbstractWebxTests;

public class InfoHandlerTests extends AbstractWebxTests {
    @Before
    public void init() throws Exception {
        prepareWebClient(null);
    }

    @After
    public void destroy() throws Exception {
        System.clearProperty("productionModeFromSystemProperties");
    }

    @Test
    public void requestInfoPage() throws Exception {
        invokeServlet("/internal/Webx/Info/Request+Info");

        assertEquals(200, clientResponseCode);

        // 包含home
        assertThat(clientResponseContent, containsString("images/home1.gif\" alt=\"Home\" /> Home</a>"));

        // 包含application home
        assertThat(clientResponseContent,
                containsString("images/home2.gif\" alt=\"Application Home\" /> Application Home</a>"));

        // 包含title
        assertThat(clientResponseContent, containsString("<title>Request Info</title>"));
    }

    @Test
    public void environmentVariables() throws Exception {
        invokeServlet("/internal/Webx/Info/Environment+Variables");

        assertEquals(200, clientResponseCode);

        // 包含home
        assertThat(clientResponseContent, containsString("images/home1.gif\" alt=\"Home\" /> Home</a>"));

        // 包含application home
        assertThat(clientResponseContent,
                containsString("images/home2.gif\" alt=\"Application Home\" /> Application Home</a>"));

        // 包含title
        assertThat(clientResponseContent, containsString("<title>Environment Variables</title>"));
    }

    @Test
    public void systemProperties() throws Exception {
        invokeServlet("/internal/Webx/Info/System+Properties");

        assertEquals(200, clientResponseCode);

        // 包含home
        assertThat(clientResponseContent, containsString("images/home1.gif\" alt=\"Home\" /> Home</a>"));

        // 包含application home
        assertThat(clientResponseContent,
                containsString("images/home2.gif\" alt=\"Application Home\" /> Application Home</a>"));

        // 包含title
        assertThat(clientResponseContent, containsString("<title>System Properties</title>"));
    }

    @Test
    public void systemInfo() throws Exception {
        invokeServlet("/internal/Webx/Info/System+Info");

        assertEquals(200, clientResponseCode);

        // 包含home
        assertThat(clientResponseContent, containsString("images/home1.gif\" alt=\"Home\" /> Home</a>"));

        // 包含application home
        assertThat(clientResponseContent,
                containsString("images/home2.gif\" alt=\"Application Home\" /> Application Home</a>"));

        // 包含title
        assertThat(clientResponseContent, containsString("<title>System Info</title>"));
    }
}
