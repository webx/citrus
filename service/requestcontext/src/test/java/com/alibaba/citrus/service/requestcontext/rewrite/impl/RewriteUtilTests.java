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
package com.alibaba.citrus.service.requestcontext.rewrite.impl;

import static com.alibaba.citrus.service.requestcontext.rewrite.impl.RewriteUtil.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.service.requestcontext.rewrite.RewriteRequestContext;

public class RewriteUtilTests extends AbstractRequestContextsTests<RewriteRequestContext> {
    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-rewrite.xml");
    }

    @Test
    public void _eval() throws Exception {
        invokeNoopServlet("/servlet/hello.htm?a=1&b=2&c=3");
        initRequestContext();

        HttpServletRequest wrappedRequest = requestContext.getWrappedRequestContext().getRequest();

        assertEquals("", eval("", wrappedRequest));
        assertEquals("%{}", eval("%{}", wrappedRequest));

        // =====================================================
        //  Client side of the IP connection
        // =====================================================

        assertEquals("localhost", eval("%{REMOTE_HOST}", wrappedRequest));
        assertEquals("127.0.0.1", eval("%{REMOTE_ADDR}", wrappedRequest));
        assertEquals("", eval("%{REMOTE_USER}", wrappedRequest));
        assertEquals("GET", eval("%{REQUEST_METHOD}", wrappedRequest));
        assertEquals("a=1&b=2&c=3", eval("%{QUERY_STRING}", wrappedRequest));
        assertEquals("1", eval("%{QUERY:a}", wrappedRequest));
        assertEquals("2", eval("%{QUERY:b}", wrappedRequest));
        assertEquals("", eval("%{QUERY:d}", wrappedRequest));
        assertEquals("", eval("%{AUTH_TYPE}", wrappedRequest));

        // =====================================================
        //  HTTP layer details extracted from HTTP headers
        // =====================================================

        assertEquals("www.taobao.com", eval("%{SERVER_NAME}", wrappedRequest));
        assertEquals("80", eval("%{SERVER_PORT}", wrappedRequest));
        assertEquals("HTTP/1.1", eval("%{SERVER_PROTOCOL}", wrappedRequest));

        // =====================================================
        //  HTTP headers
        // =====================================================

        assertTrue(eval("%{HTTP_USER_AGENT}", wrappedRequest).contains("httpunit"));
        assertEquals("", eval("%{HTTP_REFERER}", wrappedRequest));
        assertEquals("", eval("%{HTTP_HOST}", wrappedRequest));
        assertEquals("", eval("%{HTTP_ACCEPT}", wrappedRequest));
        assertEquals("", eval("%{HTTP_COOKIE}", wrappedRequest));

        // =====================================================
        //  Others
        // =====================================================

        assertEquals("/servlet/hello.htm", eval("%{REQUEST_URI}", wrappedRequest));
    }
}
