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

import org.junit.Test;

import com.alibaba.citrus.util.StringUtil;

public class HeaderNameInterceptorTests extends AbstractBasicResponseTests {
    @Test
    public void checkHeaderName_null() {
        createResponse(new HeaderNameInterceptor() {
            public String checkHeaderName(String name) {
                return null; // reject header
            }
        });

        replayMocks();

        try {
            response.addDateHeader(null, 123L);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("header name is null"));
        }

        verifyMocks();
    }

    @Test
    public void checkHeaderName_rejected() {
        createResponse(new HeaderNameInterceptor() {
            public String checkHeaderName(String name) {
                return null; // reject header
            }
        });

        responseMock.setHeader("Location", "http://localhost/");
        expectLastCall().times(2);

        replayMocks();

        response.addHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor
        response.setHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor

        try {
            response.addDateHeader("test", 123L);
            fail();
        } catch (ResponseHeaderRejectedException e) {
            assertThat(e, exception("HTTP header rejected: test"));
        }

        try {
            response.setDateHeader("test", 123L);
            fail();
        } catch (ResponseHeaderRejectedException e) {
            assertThat(e, exception("HTTP header rejected: test"));
        }

        try {
            response.addIntHeader("test", 123);
            fail();
        } catch (ResponseHeaderRejectedException e) {
            assertThat(e, exception("HTTP header rejected: test"));
        }

        try {
            response.setIntHeader("test", 123);
            fail();
        } catch (ResponseHeaderRejectedException e) {
            assertThat(e, exception("HTTP header rejected: test"));
        }

        try {
            response.addHeader("test", "value");
            fail();
        } catch (ResponseHeaderRejectedException e) {
            assertThat(e, exception("HTTP header rejected: test"));
        }

        try {
            response.setHeader("test", "value");
            fail();
        } catch (ResponseHeaderRejectedException e) {
            assertThat(e, exception("HTTP header rejected: test"));
        }

        verifyMocks();
    }

    @Test
    public void checkHeaderName_keepUnchanged() {
        createResponse(new HeaderNameInterceptor() {
            public String checkHeaderName(String name) {
                return name;
            }
        });

        responseMock.addDateHeader("test", 123L);
        responseMock.setDateHeader("test", 123L);

        responseMock.addIntHeader("test", 123);
        responseMock.setIntHeader("test", 123);

        responseMock.addHeader("test", "value");
        responseMock.setHeader("test", "value");

        responseMock.setHeader("Location", "http://localhost/");
        expectLastCall().times(2);

        replayMocks();

        response.addHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor
        response.setHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor

        response.addDateHeader("test", 123L);
        response.setDateHeader("test", 123L);

        response.addIntHeader("test", 123);
        response.setIntHeader("test", 123);

        response.addHeader("test", "value");
        response.setHeader("test", "value");

        verifyMocks();
    }

    @Test
    public void checkHeaderName_modifiy() {
        createResponse(new HeaderNameInterceptor() {
            public String checkHeaderName(String name) {
                return StringUtil.toPascalCase(name);
            }
        });

        responseMock.addDateHeader("Test", 123L);
        responseMock.setDateHeader("Test", 123L);

        responseMock.addIntHeader("Test", 123);
        responseMock.setIntHeader("Test", 123);

        responseMock.addHeader("Test", "value");
        responseMock.setHeader("Test", "value");

        responseMock.setHeader("Location", "http://localhost/");
        expectLastCall().times(2);

        replayMocks();

        response.addHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor
        response.setHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor

        response.addDateHeader("test", 123L);
        response.setDateHeader("test", 123L);

        response.addIntHeader("test", 123);
        response.setIntHeader("test", 123);

        response.addHeader("test", "value");
        response.setHeader("test", "value");

        verifyMocks();
    }
}
