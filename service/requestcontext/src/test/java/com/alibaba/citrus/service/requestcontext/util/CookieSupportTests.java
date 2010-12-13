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
package com.alibaba.citrus.service.requestcontext.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.service.requestcontext.RequestContext;

/**
 * ²âÊÔ<code>CookieSupport</code>¡£
 * 
 * @author Michael Zhou
 */
public class CookieSupportTests extends AbstractRequestContextsTests<RequestContext> {
    private DateFormat fmt;
    private CookieSupport cookie;

    @Before
    public void init() throws Exception {
        cookie = new CookieSupport("myname", "myvalue");
        assertCookie("myname=myvalue", cookie);

        fmt = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);
    }

    @Test
    public void copyConstructors() {
        CookieSupport newCookie;

        assertCookie("myname=myvalue", (newCookie = new CookieSupport(cookie)));
        assertCookie("myname=myvalue", (newCookie = new CookieSupport(cookie, "  ")));
        assertCookie("newName=myvalue", (newCookie = new CookieSupport(cookie, " newName ")));

        assertEquals("newName", newCookie.getName());
        assertEquals("myvalue", newCookie.getValue());
        assertNull(newCookie.getDomain());
        assertNull(newCookie.getPath());
    }

    public void getHeaderName() {
        assertEquals("Set-Cookie", cookie.getCookieHeaderName());
    }

    @Test
    public void getHeaderValue_invalid() {
        // °üº¬¿ØÖÆ×Ö·û
        cookie = new CookieSupport("my\"name\"", "my\nvalu\te");

        try {
            cookie.getCookieHeaderValue();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Control character in cookie value, consider BASE64 encoding your value"));
        }

        // escape´íÎó
        cookie = new CookieSupport("myname", "\"my value\\");

        try {
            cookie.getCookieHeaderValue();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Invalid escape character in cookie value"));
        }

        cookie = new CookieSupport("myname", "\"my value\\\"");

        try {
            cookie.getCookieHeaderValue();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Invalid escape character in cookie value"));
        }
    }

    @Test
    public void toString_invalid() {
        // °üº¬¿ØÖÆ×Ö·û
        cookie = new CookieSupport("my\"name\"", "my\nvalu\te");

        assertEquals("Set-Cookie: Control character in cookie value, consider BASE64 encoding your value",
                cookie.toString());

        // escape´íÎó
        cookie = new CookieSupport("myname", "\"my value\\");

        assertEquals("Set-Cookie: Invalid escape character in cookie value.", cookie.toString());
    }

    @Test
    public void cookie_empty_value() {
        cookie = new CookieSupport("myname", "  ");
        assertCookie("myname=\"  \"; Version=1; Discard", cookie);

        cookie = new CookieSupport("myname", "");
        assertCookie("myname=; Expires=Thu, 01-Jan-1970 00:00:10 GMT", cookie);

        cookie = new CookieSupport("myname", null);
        assertCookie("myname=; Expires=Thu, 01-Jan-1970 00:00:10 GMT", cookie);
    }

    @Test
    public void cookie_value_quote() {
        cookie = new CookieSupport("myname", "\\r\\n");
        assertCookie("myname=\"\\r\\n\"; Version=1; Discard", cookie);

        // already quoted
        cookie = new CookieSupport("myname", "\"abc\\\"\"");
        assertCookie("myname=\"abc\\\"\"", cookie);

        // not quoted yet
        cookie = new CookieSupport("myname", "\\rabc\"");
        assertCookie("myname=\"\\rabc\\\"\"; Version=1; Discard", cookie);

        cookie = new CookieSupport("myname", "\\\\rabc\"");
        assertCookie("myname=\"\\\\rabc\\\"\"; Version=1; Discard", cookie);
    }

    @Test
    public void cookie_value() {
        cookie = new CookieSupport("myname", "abc");
        assertCookie("myname=abc", cookie);

        cookie = new CookieSupport("myname", "a b");
        assertCookie("myname=\"a b\"; Version=1; Discard", cookie);
    }

    @Test
    public void cookies_maxage_v0() throws ParseException {
        // maxAge=-1, temp cookie
        cookie.setMaxAge(-1);
        assertCookie("myname=myvalue", cookie);

        // maxAge=0, remove cookie
        cookie.setMaxAge(0);
        assertCookie("myname=myvalue; Expires=Thu, 01-Jan-1970 00:00:10 GMT", cookie);

        // maxAge is 1 second or 1000 milliseconds
        cookie.setMaxAge(1);

        long currentTime = System.currentTimeMillis();

        String result = cookie.getCookieHeaderValue();
        String expectedPrefix = "myname=myvalue; Expires=";
        assertTrue(result.startsWith(expectedPrefix));

        long resultTime = fmt.parse(result, new ParsePosition(expectedPrefix.length())).getTime();
        long diff = resultTime - currentTime;

        assertTrue(diff < 1000);
    }

    @Test
    public void cookies_maxage_v1() throws ParseException {
        cookie.setVersion(1);

        // maxAge=-1, temp cookie
        cookie.setMaxAge(-1);
        assertCookie("myname=myvalue; Version=1; Discard", cookie);

        // maxAge=0, remove cookie, ALWAYS_ADD_EXPIRES
        cookie.setMaxAge(0);
        assertCookie("myname=myvalue; Version=1; Max-Age=0; Expires=Thu, 01-Jan-1970 00:00:10 GMT", cookie);

        // maxAge is 1 second or 1000 milliseconds, ALWAYS_ADD_EXPIRES
        cookie.setMaxAge(1);

        long currentTime = System.currentTimeMillis();

        String result = cookie.getCookieHeaderValue();
        String expectedPrefix = "myname=myvalue; Version=1; Max-Age=1; Expires=";
        assertTrue(result.startsWith(expectedPrefix));

        long resultTime = fmt.parse(result, new ParsePosition(expectedPrefix.length())).getTime();
        long diff = resultTime - currentTime;

        assertTrue(diff < 1000);
    }

    @Test
    public void cookie_comment_v0() {
        cookie.setComment("simple");

        assertCookie("myname=myvalue", cookie);

        cookie.setComment("\"quoted\" comment!");

        assertCookie("myname=myvalue", cookie);
    }

    @Test
    public void cookie_comment_v1() {
        cookie.setVersion(1);
        cookie.setComment("simple");

        assertCookie("myname=myvalue; Version=1; Comment=simple; Discard", cookie);

        cookie.setComment("\"quoted\" comment!");

        assertCookie("myname=myvalue; Version=1; Comment=\"\\\"quoted\\\" comment!\"; Discard", cookie);
    }

    @Test
    public void cookie_domain() {
        cookie.setDomain(".SINA.COM.CN");
        assertEquals(".sina.com.cn", cookie.getDomain());
        assertCookie("myname=myvalue; Domain=.sina.com.cn", cookie);

        cookie.setDomain("SINA.COM.CN");
        assertEquals(".sina.com.cn", cookie.getDomain());
        assertCookie("myname=myvalue; Domain=.sina.com.cn", cookie);

        cookie.setDomain(" ");
        assertEquals("", cookie.getDomain());
        assertCookie("myname=myvalue", cookie);

        cookie.setDomain(null);
        assertEquals("", cookie.getDomain());
        assertCookie("myname=myvalue", cookie);
    }

    @Test
    public void cookie_path() {
        cookie.setPath("/aabb");
        assertCookie("myname=myvalue; Path=/aabb", cookie);

        cookie.setPath("/aa bb");
        assertCookie("myname=myvalue; Path=\"/aa bb\"", cookie);

        cookie.setPath("/aa bb");
        cookie.setVersion(1);
        assertCookie("myname=myvalue; Version=1; Discard; Path=\"/aa bb\"", cookie);
    }

    @Test
    public void cookie_secure() {
        cookie.setSecure(true);

        assertCookie("myname=myvalue; Secure", cookie);
    }

    @Test
    public void cookie_httpOnly() {
        cookie.setHttpOnly(true);

        assertCookie("myname=myvalue; HttpOnly", cookie);
    }

    @Test
    public void cookie_all_params_v0() {
        cookie = new CookieSupport("myname", "myvalue");

        cookie.setMaxAge(0);
        cookie.setComment("hello! comment!");
        cookie.setVersion(0);
        cookie.setDomain("WWW.SINA.COM.CN");
        cookie.setSecure(true);
        cookie.setPath("/aa/bb");
        cookie.setHttpOnly(true);

        assertEquals("myname=myvalue; Domain=.www.sina.com.cn; Expires=Thu, 01-Jan-1970 00:00:10 GMT; "
                + "Path=/aa/bb; Secure; HttpOnly", cookie.getCookieHeaderValue());

        // test copy constructor
        assertEquals("myname=myvalue; Domain=.www.sina.com.cn; Expires=Thu, 01-Jan-1970 00:00:10 GMT; "
                + "Path=/aa/bb; Secure; HttpOnly", new CookieSupport(cookie).getCookieHeaderValue());
    }

    @Test
    public void cookie_all_params_v1() {
        cookie = new CookieSupport("myname", "myvalue");

        cookie.setMaxAge(0);
        cookie.setComment("hello! comment!");
        cookie.setVersion(1);
        cookie.setDomain("WWW.SINA.COM.CN");
        cookie.setSecure(true);
        cookie.setPath("/aa/bb");
        cookie.setHttpOnly(true);

        assertEquals("myname=myvalue; Version=1; Comment=\"hello! comment!\"; "
                + "Domain=.www.sina.com.cn; Max-Age=0; Expires=Thu, 01-Jan-1970 00:00:10 GMT; "
                + "Path=/aa/bb; Secure; HttpOnly", cookie.getCookieHeaderValue());

        // test copy constructor
        assertEquals("myname=myvalue; Version=1; Comment=\"hello! comment!\"; "
                + "Domain=.www.sina.com.cn; Max-Age=0; Expires=Thu, 01-Jan-1970 00:00:10 GMT; "
                + "Path=/aa/bb; Secure; HttpOnly", new CookieSupport(cookie).getCookieHeaderValue());
    }

    @Test
    public void addCookie() throws Exception {
        // request 1: add myname
        invokeNoopServlet("/servlet");

        cookie.addCookie(response);

        commitToClient();

        assertArrayEquals(new String[] { "myname=myvalue" }, clientResponse.getHeaderFields("set-cookie"));

        // request 2: check myname
        invokeNoopServlet("/servlet");

        assertEquals(1, request.getCookies().length);
        cookie = new CookieSupport(request.getCookies()[0]);

        assertCookie("myname=myvalue", cookie);

        commitToClient();

        assertArrayEquals(new String[] {}, clientResponse.getHeaderFields("set-cookie"));
    }

    private void assertCookie(String value, CookieSupport cookie) {
        assertEquals("Set-Cookie", cookie.getCookieHeaderName());
        assertEquals(value, cookie.getCookieHeaderValue());
        assertEquals("Set-Cookie: " + value, cookie.toString());
    }
}
