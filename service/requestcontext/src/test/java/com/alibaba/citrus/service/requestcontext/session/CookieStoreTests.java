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

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.service.requestcontext.util.CookieSupport;

/**
 * 测试cookie store。
 * 
 * @author Michael Zhou
 */
public class CookieStoreTests extends AbstractRequestContextsTests<SessionRequestContext> {
    private HttpSession session;
    private boolean noSession;

    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-session-cookie-stores.xml");
    }

    @Override
    protected void afterInitRequestContext() {
        if (!noSession) {
            session = requestContext.getRequest().getSession();
        }

        noSession = false;
    }

    private Matcher<String> cookie(String startsWith, boolean httpOnly, boolean secure) {
        List<Matcher<? extends String>> matchers = createLinkedList();

        matchers.add(startsWith(startsWith));

        if (httpOnly) {
            matchers.add(containsString("; HttpOnly"));
        } else {
            matchers.add(not(containsString("; HttpOnly")));
        }

        if (secure) {
            matchers.add(containsString("; Secure"));
        } else {
            matchers.add(not(containsString("; Secure")));
        }

        return allOf(matchers);
    }

    @Test
    public void simple() throws Exception {
        simple("session_cookie", true, false);
    }

    @Test
    public void simple_override_cookie_settings() throws Exception {
        simple("session_cookie_override_settings", false, true);
    }

    private void simple(String beanName, boolean httpOnly, boolean secure) throws Exception {
        // request 1 - new request
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertEquals(true, session.isNew());

        session.setAttribute("count", 0);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        String[] newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(2, newCookies.length);

        assertThat(newCookies[0], cookie("JSESSIONID=", true, false)); // always
        assertThat(newCookies[1], cookie("myCookieStore0=", httpOnly, secure));

        // request 2 - modify values
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertEquals(false, session.isNew());
        assertEquals(0, session.getAttribute("count"));

        session.setAttribute("count", 1); // modify

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(1, newCookies.length);

        assertThat(newCookies[0], cookie("myCookieStore0=", httpOnly, secure));

        // request 3 - remove values
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertEquals(false, session.isNew());
        assertEquals(1, session.getAttribute("count"));

        session.removeAttribute("count"); // remove

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(1, newCookies.length);

        assertThat(newCookies[0], cookie("myCookieStore0=", httpOnly, secure));

        // request 4 - invalidate
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertEquals(false, session.isNew());

        assertEquals(null, session.getAttribute("count"));
        assertFalse(session.getAttributeNames().hasMoreElements()); // no attributes

        session.invalidate();

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(2, newCookies.length);

        assertThat(newCookies[0], cookie("JSESSIONID=;", true, false));
        assertThat(newCookies[1], cookie("myCookieStore0=;", httpOnly, secure));

        // request 5
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertEquals(true, session.isNew());

        assertEquals(null, session.getAttribute("count"));
        assertFalse(session.getAttributeNames().hasMoreElements()); // no attributes

        requestContexts.commitRequestContext(requestContext);
        commitToClient();
    }

    @Test
    public void survivesInInvalidating() throws Exception {
        // request 1 - new request
        invokeNoopServlet("/servlet");
        initRequestContext("session_survives_in_invalidating");

        assertEquals(true, session.isNew());

        session.setAttribute("count", 0);
        session.setAttribute("loginName", "baobao");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        String[] newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(3, newCookies.length);

        assertTrue(newCookies[0].startsWith("JSESSIONID="));
        assertTrue(newCookies[1].startsWith("c10="));
        assertTrue(newCookies[2].startsWith("c20="));

        // request 2 - invalidate
        invokeNoopServlet("/servlet");
        initRequestContext("session_survives_in_invalidating");

        assertEquals(false, session.isNew());
        assertEquals(0, session.getAttribute("count"));
        assertEquals("baobao", session.getAttribute("loginName"));

        session.invalidate();

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(3, newCookies.length);

        assertTrue(newCookies[0].startsWith("JSESSIONID=;"));
        assertTrue(newCookies[1].startsWith("c10=;"));
        assertTrue(newCookies[2].startsWith("c20=") && !newCookies[1].startsWith("c20=;"));

        // request 3
        invokeNoopServlet("/servlet");
        initRequestContext("session_survives_in_invalidating");

        assertEquals(true, session.isNew());
        assertEquals(null, session.getAttribute("count"));
        assertEquals("baobao", session.getAttribute("loginName")); // 存活到现在

        requestContexts.commitRequestContext(requestContext);
        commitToClient();
    }

    @Test
    public void long_cookies() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie_checksum");

        assertEquals(true, session.isNew());

        StringBuilder buf = new StringBuilder(2500);

        for (int i = 0; i < 2500; i++) {
            buf.append(i);
        }

        session.setAttribute("count", buf.toString());

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        String[] newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(4, newCookies.length);

        assertTrue(newCookies[0].startsWith("JSESSIONID="));
        assertTrue(newCookies[1].startsWith("myCookieStore0="));
        assertTrue(newCookies[1].length() > 3000 && newCookies[1].length() < 4096);
        assertTrue(newCookies[2].startsWith("myCookieStore1="));
        assertTrue(newCookies[3].startsWith("myCookieStoresum="));
        assertTrue(newCookies[3].indexOf("|") > 0);
        assertTrue(newCookies[3].indexOf("|", newCookies[3].indexOf("|") + 1) < 0);

        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie");

        assertEquals(false, session.isNew());
        assertEquals(buf.toString(), session.getAttribute("count"));

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(0, newCookies.length);
    }

    @Test
    public void too_long_cookies() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie_checksum");

        assertEquals(true, session.isNew());

        StringBuilder buf = new StringBuilder(10000);

        for (int i = 0; i < 10000; i++) {
            buf.append(i);
        }

        session.setAttribute("count", buf.toString());

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        String[] newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(1, newCookies.length);

        assertTrue(newCookies[0].startsWith("JSESSIONID="));
    }

    @Test
    public void checksum() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie_checksum");

        assertEquals(true, session.isNew());

        session.setAttribute("count", 0);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        String[] newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(3, newCookies.length);

        assertTrue(newCookies[0].startsWith("JSESSIONID="));
        assertTrue(newCookies[1].startsWith("myCookieStore0="));
        assertTrue(newCookies[2].startsWith("myCookieStoresum="));

        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie_checksum");

        assertEquals(false, session.isNew());
        assertEquals(0, session.getAttribute("count"));

        session.setAttribute("count", 1);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(2, newCookies.length);

        assertTrue(newCookies[0].startsWith("myCookieStore0="));
        assertTrue(newCookies[1].startsWith("myCookieStoresum="));
    }

    @Test
    public void checksum_notMatch() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie");

        assertTrue(isChecksumValid("cookie1"));
        assertEquals(true, session.isNew());

        session.setAttribute("count", 0);

        // 伪造checksum cookie，内容不匹配
        CookieSupport cookie = new CookieSupport("myCookieStoresum", "hello");
        cookie.setDomain(".taobao.com");
        cookie.setPath("/");
        cookie.setMaxAge(10);
        cookie.addCookie(newResponse);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        String[] newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(3, newCookies.length);

        assertTrue(newCookies[0].startsWith("JSESSIONID="));
        assertTrue(newCookies[0].contains("Domain=.taobao.com"));
        assertTrue(newCookies[1].startsWith("myCookieStore0="));
        assertTrue(newCookies[1].contains("Domain=.taobao.com"));
        assertTrue(newCookies[2].startsWith("myCookieStoresum="));
        assertTrue(newCookies[2].contains("Domain=.taobao.com"));

        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie");

        assertFalse(isChecksumValid("cookie1"));
        assertEquals(false, session.isNew());
        assertEquals(0, session.getAttribute("count"));

        session.setAttribute("count", 1);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(2, newCookies.length);

        assertTrue(newCookies[0].startsWith("myCookieStore0="));
        assertTrue(newCookies[1].startsWith("myCookieStoresum=;")); // remove
    }

    @Test
    public void checksum_numberNotMatch() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie_checksum");

        assertTrue(isChecksumValid("cookie2"));
        assertEquals(true, session.isNew());

        session.setAttribute("count", 0);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        String[] newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(3, newCookies.length);

        assertTrue(newCookies[0].startsWith("JSESSIONID="));
        assertTrue(newCookies[1].startsWith("myCookieStore0="));
        assertTrue(newCookies[2].startsWith("myCookieStoresum="));

        String sessionID = clientResponse.getNewCookieValue("JSESSIONID");
        String myCookieStore0 = clientResponse.getNewCookieValue("myCookieStore0");
        String myCookieStoresum = clientResponse.getNewCookieValue("myCookieStoresum");

        assertNotNull(sessionID);
        assertNotNull(myCookieStore0);
        assertNotNull(myCookieStoresum);

        // new request 2
        prepareWebClient();
        invokeNoopServlet("/servlet");
        noSession = true; // 不要创建session，手工建cookies
        initRequestContext("session_cookie");

        // 伪造checksum cookie，数量不匹配
        CookieSupport cookie = new CookieSupport("JSESSIONID", sessionID);
        cookie.setDomain(".taobao.com");
        cookie.setPath("/");
        cookie.setMaxAge(10);
        cookie.addCookie(newResponse);

        cookie = new CookieSupport("myCookieStore0", myCookieStore0);
        cookie.setDomain(".taobao.com");
        cookie.setPath("/");
        cookie.setMaxAge(10);
        cookie.addCookie(newResponse);

        cookie = new CookieSupport("myCookieStoresum", myCookieStoresum + "|hello");
        cookie.setDomain(".taobao.com");
        cookie.setPath("/");
        cookie.setMaxAge(10);
        cookie.addCookie(newResponse);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(3, newCookies.length);

        assertTrue(newCookies[0].startsWith("JSESSIONID="));
        assertTrue(newCookies[1].startsWith("myCookieStore0="));
        assertTrue(newCookies[2].startsWith("myCookieStoresum="));

        // request 3
        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie");

        assertFalse(isChecksumValid("cookie1"));
        assertEquals(false, session.isNew());
        assertEquals(0, session.getAttribute("count"));

        session.setAttribute("count", 1);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(2, newCookies.length);

        assertTrue(newCookies[0].startsWith("myCookieStore0="));
        assertTrue(newCookies[1].startsWith("myCookieStoresum=;")); // remove
    }

    private boolean isChecksumValid(String storeName) throws Exception {
        // session.storeStates
        @SuppressWarnings("unchecked")
        Map<String, Object> storeStates = (Map<String, Object>) getFieldValue(session, "storeStates", Map.class);

        // CookieStoreImpl.State.checksumValid
        Object storeState = storeStates.get(storeName);

        assertNotNull("no store " + storeName + " exists", storeState);

        return getFieldValue(storeState, "checksumValid", Boolean.class);
    }

    @Test
    public void multi_encoders() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext("session_multi_encoders_1");

        assertEquals(true, session.isNew());

        session.setAttribute("count", 0);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext("session_multi_encoders_2");

        assertEquals(false, session.isNew());
        assertEquals(0, session.getAttribute("count"));
    }
}
