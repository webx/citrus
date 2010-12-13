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
package com.alibaba.citrus.webx.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

public class SetLoggingContextHelperTests {
    private HttpServletRequest request;
    private Map<String, String> mdc;
    private SetLoggingContextHelper helper1;
    private SetLoggingContextHelper helper2;

    @Before
    public void init() {
        mdc = createHashMap();
        request = createMock(HttpServletRequest.class);

        helper1 = new LocalHelper(request);
        helper2 = new LocalHelper(request);
    }

    private void populateRequestMock(boolean withCookies) {
        reset(request);

        expect(request.getMethod()).andReturn("GET");
        expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/hello"));
        expect(request.getRequestURI()).andReturn("/hello");
        expect(request.getQueryString()).andReturn("a=1&b=2");
        expect(request.getRemoteHost()).andReturn("myhost");
        expect(request.getRemoteAddr()).andReturn("127.0.0.1");
        expect(request.getHeader("User-Agent")).andReturn("IE");
        expect(request.getHeader("Referer")).andReturn("http://othersite/");

        Cookie[] cookies = withCookies ? new Cookie[] { new Cookie("z", "1"), new Cookie("y", "2"),
                new Cookie("x", "3") } : null;

        expect(request.getCookies()).andReturn(cookies);
    }

    @Test
    public void create() {
        try {
            new SetLoggingContextHelper(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("request"));
        }
    }

    @Test
    public void setLoggingContext() {
        populateRequestMock(true);

        expect(request.getAttribute("_flag_mdc_has_already_set")).andReturn(null);
        request.setAttribute("_flag_mdc_has_already_set", helper1);
        expect(request.getAttribute("_flag_mdc_has_already_set")).andReturn(helper1);
        request.removeAttribute("_flag_mdc_has_already_set");
        replay(request);

        helper1.setLoggingContext();

        assertMdc();

        assertEquals("[x, y, z]", mdc.get("cookies"));
        assertEquals("3", mdc.get("cookie.x"));
        assertEquals("2", mdc.get("cookie.y"));
        assertEquals("1", mdc.get("cookie.z"));

        helper1.clearLoggingContext();
        assertTrue(mdc.isEmpty());

        verify(request);
    }

    @Test
    public void setLoggingContext_noCookies() {
        populateRequestMock(false);

        expect(request.getAttribute("_flag_mdc_has_already_set")).andReturn(null);
        request.setAttribute("_flag_mdc_has_already_set", helper1);
        expect(request.getAttribute("_flag_mdc_has_already_set")).andReturn(helper1);
        request.removeAttribute("_flag_mdc_has_already_set");
        replay(request);

        helper1.setLoggingContext();

        assertMdc();

        assertEquals("[]", mdc.get("cookies"));

        helper1.clearLoggingContext();
        assertTrue(mdc.isEmpty());

        verify(request);
    }

    private void assertMdc() {
        assertEquals("GET", mdc.get("method"));

        assertEquals("http://localhost/hello", mdc.get("requestURL"));
        assertEquals("http://localhost/hello?a=1&b=2", mdc.get("requestURLWithQueryString"));

        assertEquals("/hello", mdc.get("requestURI"));
        assertEquals("/hello?a=1&b=2", mdc.get("requestURIWithQueryString"));

        assertEquals("a=1&b=2", mdc.get("queryString"));

        assertEquals("myhost", mdc.get("remoteHost"));
        assertEquals("127.0.0.1", mdc.get("remoteAddr"));

        assertEquals("IE", mdc.get("userAgent"));
        assertEquals("http://othersite/", mdc.get("referrer"));
    }

    @Test
    public void nest() {
        populateRequestMock(true);

        expect(request.getAttribute("_flag_mdc_has_already_set")).andReturn(null);
        request.setAttribute("_flag_mdc_has_already_set", helper1);
        expect(request.getAttribute("_flag_mdc_has_already_set")).andReturn(helper1).times(3);
        request.removeAttribute("_flag_mdc_has_already_set");
        replay(request);

        {
            helper1.setLoggingContext();
            assertFalse(mdc.isEmpty());

            {
                helper2.setLoggingContext(); // 不会set
                assertFalse(mdc.isEmpty());

                helper2.clearLoggingContext(); // 不会clear
                assertFalse(mdc.isEmpty());
            }

            helper1.clearLoggingContext();
            assertTrue(mdc.isEmpty());
        }

        verify(request);
    }

    private class LocalHelper extends SetLoggingContextHelper {
        public LocalHelper(HttpServletRequest request) {
            super(request);
        }

        @Override
        protected Map<String, String> getMDCCopy() {
            return mdc;
        }

        @Override
        protected void setMDC(Map<String, String> mdc) {
        }

        @Override
        protected void clearMDC() {
            mdc.clear();
        }
    }
}
