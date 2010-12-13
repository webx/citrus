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

import static com.alibaba.citrus.service.requestcontext.session.SessionConfig.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.service.requestcontext.session.impl.SessionImpl;
import com.alibaba.citrus.service.requestcontext.session.impl.SessionModelImpl;
import com.alibaba.citrus.service.requestcontext.session.store.simple.impl.SimpleMemoryStoreImpl;
import com.alibaba.citrus.service.requestcontext.util.CookieSupport;

/**
 * 测试<code>Session</code>。
 */
public class SessionTests extends AbstractRequestContextsTests<SessionRequestContext> {
    private long currentTime;
    private HttpSession session;
    private String sessionID;
    private long lastCreationTime;

    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-session.xml");
    }

    @Override
    protected void afterInitRequestContext() {
        currentTime = System.currentTimeMillis();
        session = requestContext.getRequest().getSession();
    }

    /**
     * 没有配置任何store的情形。
     * <p>
     * 由于SESSION MODEL也需要被保存在session中，因此报错。
     * </p>
     */
    @Test
    public void stores_noStore() throws Exception {
        invokeNoopServlet("/servlet");

        try {
            initRequestContext("session-nostores");
            fail();
        } catch (BeanCreationException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class, "No storage configured for session model",
                            "key=SESSION_MODEL"));
        }
    }

    /**
     * 仅配置了用于保存session model的store的情形。
     * <p>
     * 当setAttribute和removeAttribute的时候报错。
     * </p>
     */
    @Test
    public void stores_modelOnly() throws Exception {
        invokeNoopServlet("/servlet");
        initRequestContext("session-model-only");

        assertEquals(true, session.isNew());

        assertEquals(null, session.getAttribute("count"));

        try {
            session.setAttribute("count", 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("No storage configured for session attribute", "count"));
        }

        try {
            session.removeAttribute("count");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("No storage configured for session attribute", "count"));
        }
    }

    @Test
    public void session_change_model() throws Exception {
        invokeNoopServlet("/servlet");
        initRequestContext();

        try {
            session.setAttribute("SESSION_MODEL", null);
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Cannot call method setAttribute with attribute SESSION_MODEL"));
        }

        try {
            session.removeAttribute("SESSION_MODEL");
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Cannot call method removeAttribute with attribute SESSION_MODEL"));
        }
    }

    @Test
    public void session_exactMatchesOnly() throws Exception {
        invokeNoopServlet("/servlet");

        try {
            initRequestContext("session_exact_match_only_wrong1");
            fail();
        } catch (BeanCreationException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class,
                            "Session store exact1 only support exact matches to attribute names"));
        }

        try {
            initRequestContext("session_exact_match_only_wrong2");
            fail();
        } catch (BeanCreationException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class,
                            "Session store exact2 only support exact matches to attribute names"));
        }

        initRequestContext("session_exact_match_only");

        MyExactMatchesOnlySessionStore store = (MyExactMatchesOnlySessionStore) requestContext.getSessionConfig()
                .getStores().getStore("exact3");

        assertArrayEquals(new String[] { "a", "b", "SESSION_MODEL" }, store.attrNames);
    }

    @Test
    public void session() throws Exception {
        // ======================
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext();

        // session.getServletContext
        assertSame(requestContext.getServletContext(), session.getServletContext());

        // session.isNew
        assertEquals(true, session.isNew());

        // session.getId
        sessionID = session.getId();
        assertNotNull(sessionID);

        // session.getCreationTime
        assertTrue(session.getCreationTime() - currentTime < 1000);
        lastCreationTime = session.getCreationTime();

        // session.getLastAccessedTime
        assertEquals(lastCreationTime, session.getLastAccessedTime());

        // session.getMaxInactiveInterval
        assertEquals(0, session.getMaxInactiveInterval());

        // session.getAttributeNames
        assertEnumeration(new Object[] {}, session.getAttributeNames());

        // session.setAttribute/getAttribute
        session.setAttribute("count", 0);
        assertEquals(0, session.getAttribute("count"));

        // session.getAttributeNames
        assertEnumeration(new Object[] { "count" }, session.getAttributeNames());

        // request.getRequestedSessionId
        assertEquals(null, newRequest.getRequestedSessionId());

        // request.isRequestedSessionIdFromCookie
        assertEquals(false, newRequest.isRequestedSessionIdFromCookie());

        // request.isRequestedSessionIdFromURL
        assertEquals(false, newRequest.isRequestedSessionIdFromURL());

        // request.isRequestedSessionIdValid
        assertEquals(false, newRequest.isRequestedSessionIdValid());

        // response.encodeURL/encodeRedirectURL: unchanged for urlEncodeEnabled=false
        assertEquals("http://localhost/servlet/", newResponse.encodeURL("http://localhost/servlet/"));
        assertEquals("http://localhost/servlet/", newResponse.encodeRedirectURL("http://localhost/servlet/"));

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(sessionID, clientResponse.getNewCookieValue("JSESSIONID")); // new added cookie

        // ======================
        // request 2
        Thread.sleep(10);
        invokeNoopServlet("/servlet");
        initRequestContext();

        // session.isNew
        assertEquals(false, session.isNew());

        // session.getId
        assertEquals(sessionID, session.getId());

        // session.getCreationTime
        assertEquals(lastCreationTime, session.getCreationTime());
        lastCreationTime = session.getCreationTime();

        // session.getLastAccessedTime
        assertTrue(session.getLastAccessedTime() != session.getCreationTime());
        assertTrue(session.getLastAccessedTime() - currentTime < 1000);

        // session.getMaxInactiveInterval
        assertEquals(0, session.getMaxInactiveInterval());

        // session.getAttributeNames
        assertEnumeration(new Object[] { "count" }, session.getAttributeNames());

        // session.removeAttribute/getAttribute
        assertEquals(0, session.getAttribute("count"));
        session.removeAttribute("count");

        // session.getAttributeNames
        assertEnumeration(new Object[] {}, session.getAttributeNames());

        // request.getRequestedSessionId
        assertEquals(sessionID, newRequest.getRequestedSessionId());

        // request.isRequestedSessionIdFromCookie
        assertEquals(true, newRequest.isRequestedSessionIdFromCookie());

        // request.isRequestedSessionIdFromURL
        assertEquals(false, newRequest.isRequestedSessionIdFromURL());

        // request.isRequestedSessionIdValid
        assertEquals(true, newRequest.isRequestedSessionIdValid());

        // response.encodeURL/encodeRedirectURL: unchanged for urlEncodeEnabled=false
        assertEquals("http://localhost/servlet/", newResponse.encodeURL("http://localhost/servlet/"));
        assertEquals("http://localhost/servlet/", newResponse.encodeRedirectURL("http://localhost/servlet/"));

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(null, clientResponse.getNewCookieValue("JSESSIONID")); // no new cookie
    }

    @Test
    public void session_params_default() throws Exception {
        // ======================
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext();

        // session.getServletContext
        assertSame(requestContext.getServletContext(), session.getServletContext());

        // session.isNew
        assertEquals(true, session.isNew());

        // session.getId
        sessionID = session.getId();
        assertNotNull(sessionID);
        assertTrue(sessionID.length() > 10); // uuid

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // 检查cookie
        String prefix = "JSESSIONID=" + sessionID + "; ";
        String path = "; Path=/";
        String httpOnly = "; HttpOnly";
        String secure = "; Secure";
        String setCookie = clientResponse.getHeaderField("set-cookie");

        assertThat(setCookie, startsWith(prefix));
        assertThat(setCookie, containsString(path));
        assertThat(setCookie, containsString(httpOnly));
        assertThat(setCookie, not(containsString(secure)));

        assertEquals(sessionID, clientResponse.getNewCookieValue("JSESSIONID")); // new added cookie
    }

    @Test
    public void session_params() throws Exception {
        // ======================
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext("session_all");

        // session.getServletContext
        assertSame(requestContext.getServletContext(), session.getServletContext());

        // session.isNew
        assertEquals(true, session.isNew());

        // session.getId
        sessionID = session.getId();
        assertNotNull(sessionID);
        assertEquals(20, sessionID.length()); // random id, length=20

        // session.getCreationTime
        assertTrue(session.getCreationTime() - currentTime < 1000);
        lastCreationTime = session.getCreationTime();

        // session.getLastAccessedTime
        assertEquals(lastCreationTime, session.getLastAccessedTime());

        // session.getMaxInactiveInterval
        assertEquals(1, session.getMaxInactiveInterval());

        // session.getAttributeNames
        assertEnumeration(new Object[] {}, session.getAttributeNames());

        // session.setAttribute/getAttribute
        session.setAttribute("count", 0);
        assertEquals(0, session.getAttribute("count"));

        // session.getAttributeNames
        assertEnumeration(new Object[] { "count" }, session.getAttributeNames());

        // request.getRequestedSessionId
        assertEquals(null, newRequest.getRequestedSessionId());

        // request.isRequestedSessionIdFromCookie
        assertEquals(false, newRequest.isRequestedSessionIdFromCookie());

        // request.isRequestedSessionIdFromURL
        assertEquals(false, newRequest.isRequestedSessionIdFromURL());

        // request.isRequestedSessionIdValid
        assertEquals(false, newRequest.isRequestedSessionIdValid());

        // response.encodeURL/encodeRedirectURL: unchanged for urlEncodeEnabled=false
        assertEquals("http://localhost/servlet/", newResponse.encodeURL("http://localhost/servlet/"));
        assertEquals("http://localhost/servlet/", newResponse.encodeRedirectURL("http://localhost/servlet/"));

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // 检查cookie，注意，配置文件中写的是taobao.com，但这里会被规格化成.taobao.com
        String prefix = "JSESSIONID=" + sessionID + "; Domain=.taobao.com; Expires=";
        String path = "; Path=/";
        String httpOnly = "; HttpOnly";
        String secure = "; Secure";
        String setCookie = clientResponse.getHeaderField("set-cookie");

        assertThat(setCookie, startsWith(prefix));
        assertThat(setCookie, containsString(path));
        assertThat(setCookie, not(containsString(httpOnly)));
        assertThat(setCookie, containsString(secure));

        Date date = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US).parse(setCookie, new ParsePosition(
                prefix.length()));
        assertTrue(date.getTime() - currentTime < 10 * 1000);
        assertEquals(sessionID, clientResponse.getNewCookieValue("JSESSIONID")); // new added cookie

        // ======================
        // request 2
        Thread.sleep(10);
        invokeNoopServlet("/servlet");
        initRequestContext("session_all");

        // session.isNew
        assertEquals(false, session.isNew());

        // session.getId
        assertEquals(sessionID, session.getId());

        // session.getCreationTime
        assertEquals(lastCreationTime, session.getCreationTime());
        lastCreationTime = session.getCreationTime();

        // session.getLastAccessedTime
        assertTrue(session.getLastAccessedTime() != session.getCreationTime());
        assertTrue(session.getLastAccessedTime() - currentTime < 1000);

        // session.getMaxInactiveInterval
        assertEquals(1, session.getMaxInactiveInterval());

        // session.getAttributeNames
        assertEnumeration(new Object[] { "count" }, session.getAttributeNames());

        // session.removeAttribute/getAttribute
        assertEquals(0, session.getAttribute("count"));
        session.removeAttribute("count");

        // session.getAttributeNames
        assertEnumeration(new Object[] {}, session.getAttributeNames());

        // request.getRequestedSessionId
        assertEquals(sessionID, newRequest.getRequestedSessionId());

        // request.isRequestedSessionIdFromCookie
        assertEquals(true, newRequest.isRequestedSessionIdFromCookie());

        // request.isRequestedSessionIdFromURL
        assertEquals(false, newRequest.isRequestedSessionIdFromURL());

        // request.isRequestedSessionIdValid
        assertEquals(true, newRequest.isRequestedSessionIdValid());

        // response.encodeURL/encodeRedirectURL: unchanged for urlEncodeEnabled=false
        assertEquals("http://localhost/servlet/", newResponse.encodeURL("http://localhost/servlet/"));
        assertEquals("http://localhost/servlet/", newResponse.encodeRedirectURL("http://localhost/servlet/"));

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(null, clientResponse.getNewCookieValue("JSESSIONID")); // no new cookie
    }

    @Test
    public void session_urlencode() throws Exception {
        // ======================
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext("session_urlencode");

        // session.getServletContext
        assertSame(requestContext.getServletContext(), session.getServletContext());

        // session.isNew
        assertEquals(true, session.isNew());

        // session.getId
        sessionID = session.getId();
        assertNotNull(sessionID);

        // session.getCreationTime
        assertTrue(session.getCreationTime() - currentTime < 1000);
        lastCreationTime = session.getCreationTime();

        // session.getLastAccessedTime
        assertEquals(lastCreationTime, session.getLastAccessedTime());

        // session.getMaxInactiveInterval
        assertEquals(0, session.getMaxInactiveInterval());

        // session.getAttributeNames
        assertEnumeration(new Object[] {}, session.getAttributeNames());

        // session.setAttribute/getAttribute
        session.setAttribute("count", 0);
        assertEquals(0, session.getAttribute("count"));

        // session.getAttributeNames
        assertEnumeration(new Object[] { "count" }, session.getAttributeNames());

        // request.getRequestedSessionId
        assertEquals(null, newRequest.getRequestedSessionId());

        // request.isRequestedSessionIdFromCookie
        assertEquals(false, newRequest.isRequestedSessionIdFromCookie());

        // request.isRequestedSessionIdFromURL
        assertEquals(false, newRequest.isRequestedSessionIdFromURL());

        // request.isRequestedSessionIdValid
        assertEquals(false, newRequest.isRequestedSessionIdValid());

        // response.encodeURL/encodeRedirectURL: unchanged for urlEncodeEnabled=false
        assertEquals("http://localhost/servlet/;JSESSIONID=" + sessionID,
                newResponse.encodeURL("http://localhost/servlet/"));
        assertEquals("http://localhost/servlet/;JSESSIONID=" + sessionID,
                newResponse.encodeRedirectURL("http://localhost/servlet/"));

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(null, clientResponse.getNewCookieValue("JSESSIONID")); // new added cookie

        // ======================
        // request 2
        Thread.sleep(10);
        invokeNoopServlet("/servlet/;JSESSIONID=" + sessionID);
        initRequestContext("session_urlencode");

        // session.isNew
        assertEquals(false, session.isNew());

        // session.getId
        assertEquals(sessionID, session.getId());

        // session.getCreationTime
        assertEquals(lastCreationTime, session.getCreationTime());
        lastCreationTime = session.getCreationTime();

        // session.getLastAccessedTime
        assertTrue(session.getLastAccessedTime() != session.getCreationTime());
        assertTrue(session.getLastAccessedTime() - currentTime < 1000);

        // session.getMaxInactiveInterval
        assertEquals(0, session.getMaxInactiveInterval());

        // session.getAttributeNames
        assertEnumeration(new Object[] { "count" }, session.getAttributeNames());

        // session.removeAttribute/getAttribute
        assertEquals(0, session.getAttribute("count"));
        session.removeAttribute("count");

        // session.getAttributeNames
        assertEnumeration(new Object[] {}, session.getAttributeNames());

        // request.getRequestedSessionId
        assertEquals(sessionID, newRequest.getRequestedSessionId());

        // request.isRequestedSessionIdFromCookie
        assertEquals(false, newRequest.isRequestedSessionIdFromCookie());

        // request.isRequestedSessionIdFromURL
        assertEquals(true, newRequest.isRequestedSessionIdFromURL());

        // request.isRequestedSessionIdValid
        assertEquals(true, newRequest.isRequestedSessionIdValid());

        // response.encodeURL/encodeRedirectURL: unchanged for urlEncodeEnabled=false
        assertEquals("http://localhost/servlet/;JSESSIONID=" + sessionID,
                newResponse.encodeURL("http://localhost/servlet/"));
        assertEquals("http://localhost/servlet/;JSESSIONID=" + sessionID,
                newResponse.encodeRedirectURL("http://localhost/servlet/"));

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(null, clientResponse.getNewCookieValue("JSESSIONID")); // no new cookie
    }

    private void assertEnumeration(Object[] expectedValues, Enumeration<?> e) {
        List<Object> list = createArrayList();

        while (e.hasMoreElements()) {
            list.add(e.nextElement());
        }

        assertArrayEquals(expectedValues, list.toArray(new Object[list.size()]));
    }

    @Test
    public void session_reuseSessionID() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");

        CookieSupport cookie = new CookieSupport("JSESSIONID", "1234567890ABCDEFG");

        cookie.setPath("/");
        cookie.addCookie(response);

        commitToClient();

        assertEquals("1234567890ABCDEFG", clientResponse.getNewCookieValue("JSESSIONID")); // new added cookie

        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertTrue(session.isNew());
        String sessionID = session.getId();
        assertEquals("1234567890ABCDEFG", sessionID);

        session.setAttribute("count", 0);
        assertEquals(0, session.getAttribute("count"));

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(null, clientResponse.getNewCookieValue("JSESSIONID")); // no new cookie

        // request 3
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertEquals(0, session.getAttribute("count"));

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(null, clientResponse.getNewCookieValue("JSESSIONID")); // no new cookie
    }

    @Test
    public void session_noKeepInTouch() throws Exception {
        checkKeepInTouch(null, false);
    }

    @Test
    public void session_keepInTouch() throws Exception {
        checkKeepInTouch("session_all", true);
    }

    private void checkKeepInTouch(String beanName, boolean keepInTouch) throws Exception, InterruptedException {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        session.setAttribute("count", 0);
        assertEquals(0, session.getAttribute("count"));

        session.setMaxInactiveInterval(1);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // request 2 no changes, and not keep in touch
        Thread.sleep(600); // 等0.6s
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertFalse(session.isNew());

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // request 3
        Thread.sleep(600); // 再等0.6s，如果没有keepInTouch，就超过1s过期了。但keepInTouch的话就不会过期。
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        if (!keepInTouch) {
            assertTrue(session.isNew());
            assertEquals(null, session.getAttribute("count"));
        } else {
            assertFalse(session.isNew());
            assertEquals(0, session.getAttribute("count"));
        }

        requestContexts.commitRequestContext(requestContext);
        commitToClient();
    }

    @Test
    public void session_maxInactiveInterval_exceeds() throws Exception {
        // forceExpirationPeriod=0，sleep=1.1, maxInactiveInterval=1
        checkSessionExpired(null, 1, 1100, true);
    }

    @Test
    public void session_forceExpirationPeriod_exceeds() throws Exception {
        // forceExpirationPeriod=2，sleep=2.1, maxInactiveInterval=10
        checkSessionExpired("session-force-expire", 10, 2100, true);
    }

    @Test
    public void session_forceExpirationPeriod_notExceeds() throws Exception {
        // forceExpirationPeriod=2，sleep=1, maxInactiveInterval=0
        checkSessionExpired("session-force-expire", 0, 1000, false);
    }

    private void checkSessionExpired(String beanName, int maxInactiveInterval, int sleep, boolean expired)
            throws Exception, InterruptedException {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertTrue(session.isNew());
        String sessionID = session.getId();
        assertNotNull(sessionID);

        session.setAttribute("count", 0);
        assertEquals(0, session.getAttribute("count"));

        session.setMaxInactiveInterval(maxInactiveInterval);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        Thread.sleep(sleep);

        assertEquals(sessionID, clientResponse.getNewCookieValue("JSESSIONID")); // new added cookie

        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        if (expired) {
            assertTrue(session.isNew());

            assertEquals(null, session.getAttribute("count"));

            requestContexts.commitRequestContext(requestContext);
            commitToClient();

            assertEquals(null, clientResponse.getNewCookieValue("JSESSIONID")); // no new cookie
        } else {
            assertFalse(session.isNew());

            assertEquals(0, session.getAttribute("count"));

            requestContexts.commitRequestContext(requestContext);
            commitToClient();

            assertEquals(null, clientResponse.getNewCookieValue("JSESSIONID")); // no new cookie
        }
    }

    @Test
    public void session_invalidate() throws Exception {
        // ======================
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext();

        // session.isNew
        assertEquals(true, session.isNew());

        // session.getId
        sessionID = session.getId();
        assertNotNull(sessionID);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(sessionID, clientResponse.getNewCookieValue("JSESSIONID")); // new added cookie

        // ======================
        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext();

        // session.isNew
        assertEquals(false, session.isNew());

        // session.getId
        assertEquals(sessionID, session.getId());

        // session.invalidate
        assertFalse(((SessionImpl) session).isInvalidated());
        session.invalidate();
        assertTrue(((SessionImpl) session).isInvalidated());
        checkInvalidate();

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals("", clientResponse.getNewCookieValue("JSESSIONID")); // got cleared cookie
    }

    @Test
    public void session_invalidate_new() throws Exception {
        // ======================
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext();

        // session.isNew
        assertEquals(true, session.isNew());

        // session.getId
        sessionID = session.getId();
        assertNotNull(sessionID);

        // invalidate new session
        assertFalse(((SessionImpl) session).isInvalidated());
        session.invalidate();
        assertTrue(((SessionImpl) session).isInvalidated());
        checkInvalidate(true);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals("", clientResponse.getNewCookieValue("JSESSIONID")); // no new cookie
    }

    private void checkInvalidate() throws Exception {
        checkInvalidate(false);
    }

    private void checkInvalidate(boolean invalidateNewSession) throws Exception {
        // session.getServletContext
        assertSame(requestContext.getServletContext(), session.getServletContext());

        // session.isNew
        checkInvalidate("isNew");

        // session.getId
        sessionID = session.getId();
        assertNotNull(sessionID);

        // session.getCreationTime
        checkInvalidate("getCreationTime");

        // session.getLastAccessedTime
        checkInvalidate("getLastAccessedTime");

        // session.getMaxInactiveInterval
        assertEquals(0, session.getMaxInactiveInterval());

        // session.getAttributeNames
        checkInvalidate("getAttributeNames");

        // session.setAttribute/getAttribute
        checkInvalidate("setAttribute", "count", new Object());
        checkInvalidate("getAttribute", "count");

        // session.getAttributeNames
        checkInvalidate("getAttributeNames");

        // session.invalidate
        checkInvalidate("invalidate");

        // session.clear
        checkInvalidate("clear");

        if (invalidateNewSession) {
            // request.getRequestedSessionId
            assertEquals(null, newRequest.getRequestedSessionId());

            // request.isRequestedSessionIdFromCookie
            assertEquals(false, newRequest.isRequestedSessionIdFromCookie());

            // request.isRequestedSessionIdFromURL
            assertEquals(false, newRequest.isRequestedSessionIdFromURL());

            // request.isRequestedSessionIdValid
            assertEquals(false, newRequest.isRequestedSessionIdValid());
        } else {
            // request.getRequestedSessionId
            assertEquals(sessionID, newRequest.getRequestedSessionId());

            // request.isRequestedSessionIdFromCookie
            assertEquals(true, newRequest.isRequestedSessionIdFromCookie());

            // request.isRequestedSessionIdFromURL
            assertEquals(false, newRequest.isRequestedSessionIdFromURL());

            // request.isRequestedSessionIdValid
            assertEquals(true, newRequest.isRequestedSessionIdValid());
        }

        // response.encodeURL/encodeRedirectURL: unchanged for urlEncodeEnabled=false
        assertEquals("http://localhost/servlet/", newResponse.encodeURL("http://localhost/servlet/"));
        assertEquals("http://localhost/servlet/", newResponse.encodeRedirectURL("http://localhost/servlet/"));
    }

    private void checkInvalidate(String methodName, Object... params) throws Exception {
        Class<?>[] paramTypes;

        if (isEmptyArray(params)) {
            paramTypes = EMPTY_CLASS_ARRAY;
            params = EMPTY_OBJECT_ARRAY;
        } else {
            paramTypes = new Class<?>[params.length];

            for (int i = 0; i < params.length; i++) {
                paramTypes[i] = params[i].getClass();
            }
        }

        Method method = session.getClass().getMethod(methodName, paramTypes);

        try {
            method.invoke(session, params);
            fail(methodName);
        } catch (InvocationTargetException e) {
            assertThat(
                    e,
                    exception(IllegalStateException.class, "Cannot call method", methodName,
                            "the session has already invalidated"));
        }
    }

    @Test
    public void session_models() throws Exception {
        // ======================
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext("session_models");

        SimpleMemoryStoreImpl sms = (SimpleMemoryStoreImpl) requestContext.getSessionConfig().getStores()
                .getStore("s10");

        assertEquals(true, session.isNew());
        sessionID = session.getId();
        session.setAttribute("myObject", "myValue");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(sessionID, clientResponse.getNewCookieValue("JSESSIONID")); // new added cookie

        // 查看session的内容
        Map<String, Object> contentMap = sms.getSession(sessionID);

        assertEquals(2, contentMap.size());
        assertEquals("myValue", contentMap.get("myObject"));

        String model = (String) contentMap.get(MODEL_KEY_DEFAULT);

        assertNotNull(model);

        // ======================
        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext("session_models");

        assertEquals(false, session.isNew());
        assertEquals(sessionID, session.getId());
        assertEquals("myValue", session.getAttribute("myObject"));

        session.setAttribute("myObject", "myValue2");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(null, clientResponse.getNewCookieValue("JSESSIONID")); // no new cookie

        // 查看session的内容
        assertEquals(2, contentMap.size());
        assertEquals("myValue2", contentMap.get("myObject"));
        assertNotSame(model, contentMap.get(MODEL_KEY_DEFAULT));

        model = (String) contentMap.get(MODEL_KEY_DEFAULT);
        assertTrue(model.contains(session.getId()));

        // ======================
        // request 3
        invokeNoopServlet("/servlet");
        initRequestContext("session_models");

        assertEquals(false, session.isNew());
        assertEquals(sessionID, session.getId());
        assertEquals("myValue2", session.getAttribute("myObject"));
    }

    @Test
    public void session_id_not_match() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie");

        assertTrue(session.isNew());
        String sessionID = session.getId();
        assertNotNull(sessionID);

        session.setAttribute("count", 0);
        assertEquals(0, session.getAttribute("count"));

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(sessionID, clientResponse.getNewCookieValue("JSESSIONID")); // new added cookie

        String cookieStoreContent = clientResponse.getNewCookieValue("myCookieStore0");
        assertNotNull(cookieStoreContent);

        // request 2 - new session
        prepareWebClient();
        client.putCookie("JSESSIONID", sessionID + "_changed"); // 改变sessionid
        client.putCookie("myCookieStore0", cookieStoreContent);

        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie");

        assertTrue(session.isNew());
        assertEquals(sessionID + "_changed", session.getId());
    }

    @Test
    public void session_id_not_found_in_model() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie");

        assertTrue(session.isNew());
        String sessionID = session.getId();
        assertNotNull(sessionID);

        session.setAttribute("count", 0);
        assertEquals(0, session.getAttribute("count"));

        Object sessionModel = session.getAttribute("SESSION_MODEL");
        getAccessibleField(SessionModelImpl.class, "sessionID").set(sessionModel, null); // 清除sessionModel中的sessionID

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        assertEquals(sessionID, clientResponse.getNewCookieValue("JSESSIONID")); // new added cookie

        String cookieStoreContent = clientResponse.getNewCookieValue("myCookieStore0");
        assertNotNull(cookieStoreContent);

        // request 2
        prepareWebClient();
        client.putCookie("JSESSIONID", sessionID + "_changed"); // 改变sessionid
        client.putCookie("myCookieStore0", cookieStoreContent);

        invokeNoopServlet("/servlet");
        initRequestContext("session_cookie");

        assertFalse(session.isNew());
        assertEquals(sessionID + "_changed", session.getId());

        assertEquals(0, session.getAttribute("count"));
    }

    public static class MyExactMatchesOnlySessionStore implements ExactMatchesOnlySessionStore {
        private String[] attrNames;

        public String[] getAttributeNames() {
            return attrNames;
        }

        public void initAttributeNames(String[] attrNames) {
            this.attrNames = attrNames;
        }

        public void init(String storeName, SessionConfig sessionConfig) {
        }

        public Iterable<String> getAttributeNames(String sessionID, StoreContext storeContext) {
            return null;
        }

        public Object loadAttribute(String attrName, String sessionID, StoreContext storeContext) {
            return null;
        }

        public void invaldiate(String sessionID, StoreContext storeContext) {
        }

        public void commit(Map<String, Object> attrs, String sessionID, StoreContext storeContext) {
        }
    }
}
