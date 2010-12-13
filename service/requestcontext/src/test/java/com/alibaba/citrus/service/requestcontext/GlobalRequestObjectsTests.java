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
package com.alibaba.citrus.service.requestcontext;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextListener;

import com.alibaba.citrus.service.requestcontext.buffered.BufferedRequestContext;
import com.alibaba.citrus.service.requestcontext.lazycommit.LazyCommitRequestContext;
import com.alibaba.citrus.service.requestcontext.locale.SetLocaleRequestContext;
import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.rewrite.RewriteRequestContext;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;
import com.alibaba.citrus.service.requestcontext.session.SessionRequestContext;
import com.alibaba.citrus.springext.support.context.XmlWebApplicationContext;
import com.meterware.servletunit.ServletRunner;

/**
 * 测试全局的request context相关的对象。
 */
public class GlobalRequestObjectsTests extends AbstractRequestContextsTests<RequestContext> {
    private static XmlWebApplicationContext subFactory;
    private static WebGlobals globals;
    private static RequestContextListener listener = new RequestContextListener();

    @BeforeClass
    public static void initGlobal() throws Exception {
        // parent context
        createBeanFactory("services.xml");

        // sub context - web context将会试图重新注册request等对象，但是由于parent中已经存在，被忽略
        subFactory = new XmlWebApplicationContext();
        subFactory.setConfigLocation("empty.xml");
        subFactory.setServletContext(new ServletRunner(new File(srcdir, "WEB-INF/web.xml"), "").newClient()
                .newInvocation("http://localhost/servlet").getServlet().getServletConfig().getServletContext());
        subFactory.setParent((ApplicationContext) factory);
        subFactory.refresh();

        // init global before request，parent context中的singleton proxy将被注入
        globals = new WebGlobals();
        subFactory.getAutowireCapableBeanFactory().autowireBeanProperties(globals,
                AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);

        // listener
        listener = new RequestContextListener();
    }

    @Before
    public void init() throws Exception {
        invokeNoopServlet("/servlet");
        initRequestContext("all");
    }

    protected final AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
        return ((ApplicationContext) factory).getAutowireCapableBeanFactory();
    }

    @After
    public void dispose() {
        clearWebEnvironment();
    }

    private void setupWebEnvironment() {
        listener.requestInitialized(new ServletRequestEvent(requestContext.getServletContext(), requestContext
                .getRequest()));
    }

    private void clearWebEnvironment() {
        listener.requestDestroyed(new ServletRequestEvent(requestContext.getServletContext(), requestContext
                .getRequest()));
    }

    @Test
    public void classNames() throws Exception {
        int i = 0;
        for (Object obj : globals.getObjects()) {
            String className = obj.getClass().getName();
            String interfaceName = globals.getInterfaces()[i++].getName();

            assertTrue(className.startsWith(interfaceName + "$") || className.startsWith("$" + interfaceName + "$"));
        }
    }

    @Test
    public void hashCodeEquals() throws Exception {
        clearWebEnvironment();

        Set<Object> set = createHashSet();

        for (Object obj : globals.getObjects()) {
            set.add(obj);
        }

        assertEquals(10, set.size());

        // 进入web环境
        setupWebEnvironment();

        for (Object obj : globals.getObjects()) {
            assertTrue(set.contains(obj));
        }
    }

    @Test
    public void non_web_environment() throws Exception {
        clearWebEnvironment();

        assertEquals(10, globals.getObjects().length);

        int i = 0;
        for (Object obj : globals.getObjects()) {
            assertNotNull(obj);
            assertThat(obj, instanceOf(ObjectFactory.class));
            assertThat(
                    obj.toString(),
                    containsAll(globals.getInterfaces()[i++].getSimpleName() + "[", "No thread-bound request found",
                            "]"));

            try {
                ((ObjectFactory) obj).getObject();
            } catch (IllegalStateException e) {
                assertThat(e, exception("No thread-bound request found"));
            }

            assertNull(getProxyTarget(obj));
        }
    }

    @Test
    public void web_environment() throws Exception {
        assertEquals(10, globals.getObjects().length);

        int i = 0;
        for (Object obj : globals.getObjects()) {
            assertNotNull(obj);
            assertThat(obj, instanceOf(ObjectFactory.class));
            assertThat(obj.toString(), not(containsString("RequestContextProxy[")));

            assertThat(((ObjectFactory) obj).getObject(), instanceOf(globals.getInterfaces()[i++]));
        }
    }

    @Test
    public void RequestContextUtil_singletonProxy() {
        // null
        assertNull(assertProxy(null));
        assertNull(getProxyTarget(null));

        // not an ObjectFactory
        try {
            assertProxy("not a proxy");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(
                    e,
                    exception("expects a proxy delegating to a real object, but got an object of type java.lang.String"));
        }

        // for each proxy
        int i = 0;
        for (Object obj : globals.getObjects()) {
            assertProxy(obj);

            Object realObj = getProxyTarget(obj);
            assertThat(realObj, instanceOf(globals.getInterfaces()[i++]));

            try {
                assertProxy(realObj);
                fail();
            } catch (IllegalArgumentException e) {
                assertThat(e, exception("expects a proxy delegating to a real object, but got an object of type"));
            }
        }
    }

    @Test
    public void request() throws Exception {
        assertEquals("/servlet", globals.request.getServletPath());
    }

    @Test
    public void response() throws Exception {
        assertEquals("UTF-8", globals.response.getCharacterEncoding());
    }

    @Test
    public void session() throws Exception {
        assertNull(globals.request.getSession(false)); // session尚未创建
        assertNotNull(globals.session.getId()); // 创建session
        assertNotNull(globals.request.getSession(false)); // session已经创建
    }

    @Test
    public void bufferedRC() throws Exception {
        assertEquals(true, globals.bufferedRC.isBuffering());
    }

    @Test
    public void lazyCommitRC() throws Exception {
        newResponse.sendRedirect("http://www.sina.com.cn/");
        assertEquals("http://www.sina.com.cn/", globals.lazyCommitRC.getRedirectLocation());
    }

    @Test
    public void setLocaleRC() throws Exception {
        newResponse.setCharacterEncoding("GBK");
        assertEquals(null, globals.setLocaleRC.getResponseContentType());
        assertEquals("GBK", globals.setLocaleRC.getResponse().getCharacterEncoding());
    }

    @Test
    public void parserRC() throws Exception {
        assertEquals(0, globals.parserRC.getParameters().size());
        globals.parserRC.getParameters().setString("hello", "world");
        assertEquals("world", globals.parserRC.getParameters().getString("hello"));
    }

    @Test
    public void rewriteRC() throws Exception {
        assertNotNull(globals.rewriteRC.getRequest());
    }

    @Test
    public void sessionRC() throws Exception {
        assertFalse(globals.sessionRC.isSessionInvalidated());
    }

    @Test
    public void rundataRC() throws Exception {
        assertEquals("/servlet", globals.rundataRC.getServletPath());
    }

    @Test
    public void performance() {
        long start = System.currentTimeMillis();
        int loop = 100000;

        for (int i = 0; i < loop; i++) {
            globals.rundataRC.getServletPath();
        }

        long duration = System.currentTimeMillis() - start;

        System.out.printf("Calling RunData.getServletPath() %,d times, took %,d ms.\n", loop, duration);
    }

    @Test
    public void performance_raw() {
        RunData rundata = (RunData) ((ObjectFactory) globals.rundataRC).getObject();

        long start = System.currentTimeMillis();
        int loop = 100000;

        for (int i = 0; i < loop; i++) {
            rundata.getServletPath();
        }

        long duration = System.currentTimeMillis() - start;

        System.out.printf("Calling Raw RunData.getServletPath() %,d times, took %,d ms.\n", loop, duration);
    }

    private static class WebGlobals {
        @Autowired
        private HttpServletRequest request;

        @Autowired
        private HttpServletResponse response;

        @Autowired
        private HttpSession session;

        @Autowired
        private BufferedRequestContext bufferedRC;

        @Autowired
        private LazyCommitRequestContext lazyCommitRC;

        @Autowired
        private SetLocaleRequestContext setLocaleRC;

        @Autowired
        private ParserRequestContext parserRC;

        @Autowired
        private RewriteRequestContext rewriteRC;

        @Autowired
        private SessionRequestContext sessionRC;

        @Autowired
        private RunData rundataRC;

        private final Class<?>[] interfaces = new Class<?>[] { HttpServletRequest.class, HttpServletResponse.class,
                HttpSession.class, BufferedRequestContext.class, LazyCommitRequestContext.class,
                SetLocaleRequestContext.class, ParserRequestContext.class, RewriteRequestContext.class,
                SessionRequestContext.class, RunData.class };

        public Object[] getObjects() {
            return new Object[] { request, response, session, bufferedRC, lazyCommitRC, setLocaleRC, parserRC,
                    rewriteRC, sessionRC, rundataRC };
        }

        public Class<?>[] getInterfaces() {
            return interfaces;
        }
    }
}
