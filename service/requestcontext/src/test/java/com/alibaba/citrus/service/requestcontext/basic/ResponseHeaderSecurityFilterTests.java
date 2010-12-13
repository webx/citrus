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
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.basic.impl.ResponseHeaderSecurityFilter;
import com.alibaba.citrus.util.HumanReadableSize;

public class ResponseHeaderSecurityFilterTests extends AbstractBasicResponseTests {
    private ResponseHeaderSecurityFilter filter;

    @Before
    public void init() {
        filter = new ResponseHeaderSecurityFilter();
        createResponse(filter);
    }

    @Test
    public void checkHeaderName() {
        try {
            response.setHeader("wrong\r\nname", "value");
            fail();
        } catch (ResponseHeaderRejectedException e) {
            assertThat(e, exception("Invalid response header: wrong\\r\\nname"));
        }

        responseMock.addIntHeader("name", 123); // keep original name
        replayMocks();

        response.addIntHeader("name", 123);
        verifyMocks();
    }

    @Test
    public void checkHeaderValue() {
        responseMock.setHeader("name", "value1 value2 value3 ");
        replayMocks();

        response.setHeader("name", "value1\r\nvalue2\rvalue3\n");
        verifyMocks();
    }

    @Test
    public void checkCookieName() {
        Cookie wrongCookie = createMock(Cookie.class);

        expect(wrongCookie.getName()).andReturn("wrong\r\nname").atLeastOnce();
        expect(wrongCookie.getValue()).andReturn("value").atLeastOnce();

        replay(wrongCookie);

        try {
            response.addCookie(wrongCookie);
            fail();
        } catch (CookieRejectedException e) {
            assertThat(e, exception("Cookie rejected: wrong\\r\\nname=value"));
        }

        Cookie cookie = new Cookie("name", "value");

        responseMock.addHeader("Set-Cookie", "name=value"); // keep original cookie
        replayMocks();

        response.addCookie(cookie);
        verifyMocks();
    }

    @Test
    public void checkCookieValue() {
        responseMock.addHeader("Set-Cookie", "name=\"value1 value2 value3 \"; Version=1; Discard");
        replayMocks();

        response.addCookie(new Cookie("name", "value1\r\nvalue2\rvalue3\n"));
        verifyMocks();
    }

    @Test
    public void checkCookieHeaderValue_exceedsMaxSize() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 7 * 512 - 6; i++) {
            sb.append("x");
        }

        String value = sb.toString();
        responseMock.addHeader("Set-Cookie", "test1=" + value);
        responseMock.addHeader("Set-Cookie", "test2=" + value);
        responseMock.addHeader("Set-Cookie", "");
        replayMocks();

        response.addCookie(new Cookie("test1", value));
        response.addCookie(new Cookie("test2", value));
        response.addCookie(new Cookie("test3", "value3"));
        verifyMocks();
    }

    @Test
    public void checkCookieHeaderValue_setMaxSetCookieSize() {
        filter.setMaxSetCookieSize(new HumanReadableSize(20));
        assertEquals(20, filter.getMaxSetCookieSize().getValue());

        responseMock.addHeader("Set-Cookie", "test=value");
        responseMock.addHeader("Set-Cookie", "");
        replayMocks();

        response.addCookie(new Cookie("test", "value"));
        response.addCookie(new Cookie("test1", "value1"));
        verifyMocks();
    }

    @Test
    public void checkRedirectLocation() throws IOException {
        // 正常情况下，包含crlf的location是没有机会被crlfFilter发现的，因为在此之前已经出错。
        // 但为了保障安全，仍作此测试。
        assertEquals("value1 value2 value3 ",
                new ResponseHeaderSecurityFilter().checkRedirectLocation("value1\r\nvalue2\rvalue3\n"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void checkStatusMessage() throws IOException {
        responseMock.setStatus(500, "hello&amp;world");
        responseMock.sendError(404, "hello&amp;world");
        replayMocks();

        response.setStatus(500, "hello&world");
        response.sendError(404, "hello&world");
        verifyMocks();
    }
}
