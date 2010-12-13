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
import static org.junit.Assert.*;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.util.CookieSupport;

public class CookieInterceptorTests extends AbstractBasicResponseTests {
    private Cookie cookie;
    private String cookieHeader;

    @Before
    public void init() {
        cookie = new Cookie("test", "value");
        cookieHeader = null;
    }

    @Test
    public void checkCookie_null() {
        createResponse(new CookieInterceptor() {
            public Cookie checkCookie(Cookie cookie) {
                return null; // reject cookie
            }
        });

        replayMocks();

        try {
            response.addCookie(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no cookie"));
        }

        response.addHeader(" set-cookie ", null);
        response.setHeader(" set-cookie ", null);

        verifyMocks();
    }

    @Test
    public void checkCookie_rejected() {
        createResponse(new CookieInterceptor() {
            public Cookie checkCookie(Cookie cookie) {
                return null; // reject cookie
            }
        });

        replayMocks();

        try {
            response.addCookie(cookie);
            fail();
        } catch (CookieRejectedException e) {
            assertThat(e, exception("Cookie rejected: test=value"));
        }

        verifyMocks();
    }

    @Test
    public void checkCookie_rejected_cookieHeader() {
        createResponse(new CookieHeaderValueInterceptor() {
            public String checkCookieHeaderValue(String name, String value, boolean setHeader) {
                return null; // reject cookie header
            }
        });

        replayMocks();

        try {
            response.addCookie(cookie);
            fail();
        } catch (CookieRejectedException e) {
            assertThat(e, exception("Set-Cookie rejected: test=value"));
        }

        try {
            response.addHeader(" set-cookie ", "test=value");
            fail();
        } catch (CookieRejectedException e) {
            assertThat(e, exception("Set-Cookie rejected: test=value"));
        }

        try {
            response.setHeader(" set-cookie ", "test=value");
            fail();
        } catch (CookieRejectedException e) {
            assertThat(e, exception("Set-Cookie rejected: test=value"));
        }

        verifyMocks();
    }

    @Test
    public void checkCookie_keepUnchanged() {
        createResponse(new MyCookieInterceptor() {
            public Cookie checkCookie(Cookie cookie) {
                return cookie;
            }
        });

        responseMock.addHeader("Set-Cookie", "test=value");

        replayMocks();

        response.addCookie(cookie);
        assertEquals("test=value", cookieHeader);

        verifyMocks();
    }

    @Test
    public void checkCookie_modifiy() {
        final Cookie newCookie = new Cookie("new" + cookie.getName(), cookie.getValue());

        createResponse(new MyCookieInterceptor() {
            public Cookie checkCookie(Cookie cookie) {
                return newCookie;
            }
        });

        responseMock.addHeader("Set-Cookie", "newtest=value");

        replayMocks();

        response.addCookie(cookie);
        assertEquals("newtest=value", cookieHeader);

        verifyMocks();
    }

    @Test
    public void checkCookie_modifiy_cookieSupport() {
        createResponse(new MyCookieInterceptor() {
            public Cookie checkCookie(Cookie cookie) {
                return new CookieSupport(cookie, "new" + cookie.getName());
            }
        });

        responseMock.addHeader("Set-Cookie", "newtest=value");

        replayMocks();

        response.addCookie(cookie);
        assertEquals("newtest=value", cookieHeader);

        verifyMocks();
    }

    @Test
    public void checkCookie_modifiy_cookieHeader() {
        createResponse(new MyCookieInterceptor() {
            public Cookie checkCookie(Cookie cookie) {
                return cookie;
            }

            @Override
            public String checkCookieHeaderValue(String name, String value, boolean setHeader) {
                cookieHeader = "new" + value;
                return cookieHeader;
            }
        });

        responseMock.addHeader("Set-Cookie", "newtest=value");

        replayMocks();

        response.addCookie(cookie);
        assertEquals("newtest=value", cookieHeader);

        verifyMocks();
    }

    @Test
    public void checkCookie_addCookieHeader() {
        createResponse(new CookieHeaderValueInterceptor() {
            public String checkCookieHeaderValue(String name, String value, boolean setHeader) {
                cookieHeader = "new" + value;
                assertFalse(setHeader);
                return cookieHeader;
            }
        });

        responseMock.addHeader("Set-Cookie", "newtest=value");

        replayMocks();

        response.addHeader("Set-Cookie", "test=value");
        assertEquals("newtest=value", cookieHeader);

        verifyMocks();
    }

    @Test
    public void checkCookie_setCookieHeader() {
        createResponse(new CookieHeaderValueInterceptor() {
            public String checkCookieHeaderValue(String name, String value, boolean setHeader) {
                cookieHeader = "new" + value;
                assertTrue(setHeader);
                return cookieHeader;
            }
        });

        responseMock.setHeader("Set-Cookie", "newtest=value");

        replayMocks();

        response.setHeader("Set-Cookie", "test=value");
        assertEquals("newtest=value", cookieHeader);

        verifyMocks();
    }

    private abstract class MyCookieInterceptor implements CookieInterceptor, CookieHeaderValueInterceptor {
        public String checkCookieHeaderValue(String name, String value, boolean setHeader) {
            cookieHeader = value;
            return value;
        }
    }
}
