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

package com.alibaba.citrus.webx.util;

import static org.junit.Assert.*;

import javax.servlet.ServletContext;

import com.alibaba.citrus.webx.AbstractWebxTests;
import com.meterware.servletunit.InvocationContext;
import org.junit.Before;
import org.junit.Test;

public class WebxUtilTests extends AbstractWebxTests {
    @Before
    public void init() throws Exception {
        prepareWebClient(null, "/myapps");
    }

    @Test
    public void getVersion() throws Exception {
        assertEquals("Unknown Version", WebxUtil.getWebxVersion());
    }

    @Test
    public void getServletAPIVersion() throws Exception {
        InvocationContext ic = client.newInvocation("http://localhost/myapps/test");
        ServletContext servletContext = ic.getRequest().getSession().getServletContext();
        assertEquals("2.4", WebxUtil.getServletApiVersion(servletContext)); // http unit version
    }
}
