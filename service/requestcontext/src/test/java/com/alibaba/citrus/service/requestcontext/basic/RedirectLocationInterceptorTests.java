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
package com.alibaba.citrus.service.requestcontext.basic;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class RedirectLocationInterceptorTests extends AbstractBasicResponseTests {
    @Test
    public void checkRedirectLocation_null() throws IOException {
        createResponse(new RedirectLocationInterceptor() {
            public String checkRedirectLocation(String location) {
                return location;
            }
        });

        replayMocks();

        response.addHeader(" location ", null);
        response.setHeader(" location ", null);

        response.addHeader(" location ", "  ");
        response.setHeader(" location ", "  ");

        try {
            response.sendRedirect(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no redirect location"));
        }

        verifyMocks();
    }

    @Test
    public void checkRedirectLocation_rejected() throws IOException {
        createResponse(new RedirectLocationInterceptor() {
            public String checkRedirectLocation(String location) throws RedirectLocationRejectedException {
                return null;
            }
        });

        replayMocks();

        try {
            response.addHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor
            fail();
        } catch (RedirectLocationRejectedException e) {
            assertThat(e, exception("Redirect location rejected: http://localhost/"));
        }

        try {
            response.setHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor
            fail();
        } catch (RedirectLocationRejectedException e) {
            assertThat(e, exception("Redirect location rejected: http://localhost/"));
        }

        try {
            response.sendRedirect("http://localhost/");
            fail();
        } catch (RedirectLocationRejectedException e) {
            assertThat(e, exception("Redirect location rejected: http://localhost/"));
        }

        verifyMocks();
    }

    @Test
    public void checkRedirectLocation_keepUnchanged() throws IOException {
        createResponse(new RedirectLocationInterceptor() {
            public String checkRedirectLocation(String location) throws ResponseHeaderRejectedException {
                return location;
            }
        });

        responseMock.setHeader("Location", "http://localhost/");
        expectLastCall().times(2);

        responseMock.sendRedirect("http://localhost/");

        replayMocks();

        response.addHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor
        response.setHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor

        response.sendRedirect("http://localhost/");

        verifyMocks();
    }

    @Test
    public void checkRedirectLocation_modifiy() throws IOException {
        createResponse(new RedirectLocationInterceptor() {
            public String checkRedirectLocation(String location) throws ResponseHeaderRejectedException {
                return location + "?hello";
            }
        });

        responseMock.setHeader("Location", "http://localhost/?hello");
        expectLastCall().times(2);

        responseMock.sendRedirect("http://localhost/?hello");

        replayMocks();

        response.addHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor
        response.setHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor

        response.sendRedirect("http://localhost/");

        verifyMocks();
    }

    @Test
    public void checkRedirectLocation_relativeLocation() throws Exception {
        createResponse(new RedirectLocationInterceptor() {
            public String checkRedirectLocation(String location) throws ResponseHeaderRejectedException {
                assertTrue(URI.create(location).isAbsolute());
                return location; // unchanged
            }
        });

        expect(requestMock.getRequestURL()).andReturn(new StringBuffer("http://localhost/test/"));
        responseMock.setHeader("Location", "http://localhost/test/hello");
        replayMocks();
        response.addHeader(" location ", " hello "); // 特殊header：location，只调用redirect location interceptor
        verifyMocks();

        expect(requestMock.getRequestURL()).andReturn(new StringBuffer("http://localhost/test/"));
        responseMock.setHeader("Location", "http://localhost/hello");
        replayMocks();
        response.setHeader(" location ", " /hello "); // 特殊header：location，只调用redirect location interceptor
        verifyMocks();

        expect(requestMock.getRequestURL()).andReturn(new StringBuffer("http://localhost/test/"));
        responseMock.sendRedirect("http://localhost/hello");
        replayMocks();
        response.sendRedirect(" /a/../hello "); // 特殊header：location，只调用redirect location interceptor
        verifyMocks();
    }

    @Test
    public void checkRedirectLocation_illegal() throws Exception {
        createResponse(new RedirectLocationInterceptor() {
            public String checkRedirectLocation(String location) throws ResponseHeaderRejectedException {
                return location; // unchanged
            }
        });

        try {
            response.addHeader(" location ", " hello\r\nworld ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(URISyntaxException.class, "hello\r\nworld"));
        }
    }
}
