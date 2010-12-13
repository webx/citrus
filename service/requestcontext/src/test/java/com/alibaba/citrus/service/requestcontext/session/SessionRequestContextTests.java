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
package com.alibaba.citrus.service.requestcontext.session;

import static org.junit.Assert.*;

import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;

public class SessionRequestContextTests extends AbstractRequestContextsTests<SessionRequestContext> {
    private HttpSession session;

    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-session.xml");
    }

    @Before
    public void init() throws Exception {
        invokeNoopServlet("/servlet");
        initRequestContext();
    }

    @Test
    public void getSessionConfig() {
        assertNotNull(requestContext.getSessionConfig());
    }

    @Test
    public void isSessionInvalidated() {
        assertFalse(requestContext.isSessionInvalidated());

        session = newRequest.getSession();

        assertFalse(requestContext.isSessionInvalidated());

        session.invalidate();

        assertTrue(requestContext.isSessionInvalidated());
    }

    @Test
    public void clear() {
        requestContext.clear();

        session = newRequest.getSession();

        session.setAttribute("hello", "world");
        assertEquals("world", session.getAttribute("hello"));

        requestContext.clear();
        assertEquals(null, session.getAttribute("hello"));

        assertFalse(requestContext.isSessionInvalidated());
    }
}
