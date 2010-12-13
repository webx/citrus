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

public class HeaderValueInterceptorTests extends AbstractBasicResponseTests {
    @Test
    public void checkHeaderValue_null() {
        createResponse(new HeaderValueInterceptor() {
            public String checkHeaderValue(String name, String value) {
                return null; // reject header
            }
        });

        replayMocks();

        response.addHeader("test", null);
        response.setHeader("test", null);

        verifyMocks();
    }

    @Test
    public void checkHeaderValue_rejected() {
        createResponse(new HeaderValueInterceptor() {
            public String checkHeaderValue(String name, String value) {
                return null; // reject header
            }
        });

        responseMock.addDateHeader("test", 123L);
        responseMock.setDateHeader("test", 123L);

        responseMock.addIntHeader("test", 123);
        responseMock.setIntHeader("test", 123);

        responseMock.setHeader("Location", "http://localhost/");
        expectLastCall().times(2);

        replayMocks();

        response.addHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor
        response.setHeader(" location ", "http://localhost/"); // 特殊header：location，只调用redirect location interceptor

        response.addDateHeader("test", 123L); // 不经过interceptors
        response.setDateHeader("test", 123L); // 不经过interceptors

        response.addIntHeader("test", 123); // 不经过interceptors
        response.setIntHeader("test", 123); // 不经过interceptors

        try {
            response.addHeader("test", "value");
            fail();
        } catch (ResponseHeaderRejectedException e) {
            assertThat(e, exception("HTTP header rejected: test=value"));
        }

        try {
            response.setHeader("test", "value");
            fail();
        } catch (ResponseHeaderRejectedException e) {
            assertThat(e, exception("HTTP header rejected: test=value"));
        }

        verifyMocks();
    }

    @Test
    public void checkHeaderValue_keepUnchanged() {
        createResponse(new HeaderValueInterceptor() {
            public String checkHeaderValue(String name, String value) {
                return value;
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
    public void checkHeaderValue_modifiy() {
        createResponse(new HeaderValueInterceptor() {
            public String checkHeaderValue(String name, String value) {
                return name + "=" + StringUtil.toPascalCase(value);
            }
        });

        responseMock.addDateHeader("test", 123L);
        responseMock.setDateHeader("test", 123L);

        responseMock.addIntHeader("test", 123);
        responseMock.setIntHeader("test", 123);

        responseMock.addHeader("test", "test=Value");
        responseMock.setHeader("test", "test=Value");

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
