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

import static org.junit.Assert.*;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.service.requestcontext.util.CookieSupport;
import com.alibaba.citrus.util.CollectionUtil;

public class SessionInterceptorTests extends AbstractRequestContextsTests<SessionRequestContext> {
    private HttpSession session;
    private String sessionId;
    private final static ThreadLocal<List<String>> lifecycleActions = new ThreadLocal<List<String>>();
    private final static ThreadLocal<List<String>> sessionAttrsAccess = new ThreadLocal<List<String>>();
    private static SessionConfig config1;
    private static SessionConfig config2;

    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-session-interceptors.xml");

        assertNotNull(config1);
        assertNotNull(config2);
    }

    @Before
    public void initLog() {
        lifecycleActions.set(CollectionUtil.<String> createLinkedList());
        sessionAttrsAccess.set(CollectionUtil.<String> createLinkedList());
    }

    @After
    public void destroy() {
        lifecycleActions.remove();
        sessionAttrsAccess.remove();
    }

    @Override
    protected void afterInitRequestContext() {
        session = requestContext.getRequest().getSession();
        sessionId = session.getId();
    }

    @Test
    public void createNewSessionId() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertTrue(session.isNew());
        assertLifecycle("created", "visited");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertFalse(session.isNew());
        assertLifecycle("visited");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();
    }

    @Test
    public void reuseSessionId() throws Exception {
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
        assertLifecycle("created", "visited");
        assertEquals("1234567890ABCDEFG", sessionId);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // request 3
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertFalse(session.isNew());
        assertLifecycle("visited");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();
    }

    @Test
    public void expired() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertTrue(session.isNew());
        assertLifecycle("created", "visited");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        Thread.sleep(1100L); // expire session

        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertTrue(session.isNew());
        assertLifecycle("invalidated", "created", "visited");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();
    }

    @Test
    public void invalidate() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertTrue(session.isNew());
        assertLifecycle("created", "visited");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertFalse(session.isNew());
        session.invalidate();
        assertLifecycle("visited", "invalidated");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();
    }

    @Test
    public void invalidate_new() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertTrue(session.isNew());
        session.invalidate();
        assertLifecycle("created", "visited", "invalidated");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertTrue(session.isNew());
        assertLifecycle("created", "visited");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();
    }

    private void assertLifecycle(String... values) {
        for (int i = 0; i < values.length; i++) {
            values[i] += " " + sessionId;
        }

        assertArrayEquals(values, lifecycleActions.get().toArray());
        lifecycleActions.get().clear();
    }

    @Test
    public void readWrite() throws Exception {
        // request 1
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertAccess("write SESSION_MODEL=sessionModel");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // request 2
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertEquals(null, session.getAttribute("test1"));

        session.setAttribute("test2", "value2");
        assertEquals("read written value2", session.getAttribute("test2"));

        assertAccess("read SESSION_MODEL=sessionModel", "write SESSION_MODEL=sessionModel", "read test1=null",
                "write test2=value2", "read test2=written value2");

        requestContexts.commitRequestContext(requestContext);
        commitToClient();
    }

    private void assertAccess(String... values) {
        assertArrayEquals(values, sessionAttrsAccess.get().toArray());
        sessionAttrsAccess.get().clear();
    }

    public static class MySessionLifecycleListener implements SessionLifecycleListener {
        public void init(SessionConfig sessionConfig) {
            config1 = sessionConfig;
        }

        public void sessionCreated(HttpSession session) {
            lifecycleActions.get().add("created " + session.getId());
        }

        public void sessionInvalidated(HttpSession session) {
            lifecycleActions.get().add("invalidated " + session.getId());
        }

        public void sessionVisited(HttpSession session) {
            lifecycleActions.get().add("visited " + session.getId());
        }
    }

    public static class MySessionAttributeInterceptor implements SessionAttributeInterceptor {
        public void init(SessionConfig sessionConfig) {
            config2 = sessionConfig;
        }

        public Object onRead(String name, Object value) {
            sessionAttrsAccess.get().add("read " + name + "=" + toStringValue(value));

            if (name.equals("SESSION_MODEL")) {
                return value;
            } else if (value == null) {
                return value;
            }

            return "read " + value;
        }

        public Object onWrite(String name, Object value) {
            sessionAttrsAccess.get().add("write " + name + "=" + toStringValue(value));

            if (name.equals("SESSION_MODEL")) {
                return value;
            } else if (value == null) {
                return value;
            }

            return "written " + value;
        }

        private String toStringValue(Object value) {
            if (value instanceof SessionModel) {
                return "sessionModel";
            } else {
                return String.valueOf(value);
            }
        }
    }
}
