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

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;
import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.*;
import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.context.request.RequestContextHolder;

import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.test.runner.TestNameAware;
import com.alibaba.citrus.util.io.StreamUtil;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.IllegalRequestParameterException;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.protocol.UploadFileSpec;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.PatchedServletRunner;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/**
 * 用来测试RequestContextChainingService及相关类的基类。
 * 
 * @author Michael Zhou
 */
@RunWith(TestNameAware.class)
public abstract class AbstractRequestContextsTests<RC extends RequestContext> {
    @SuppressWarnings("unchecked")
    protected final Class<RC> requestContextInterface = (Class<RC>) resolveParameter(getClass(),
            AbstractRequestContextsTests.class, 0).getRawType();

    // container
    protected static BeanFactory factory;

    // web client
    protected ServletUnitClient client;
    protected WebResponse clientResponse;

    // servlet request/response
    protected InvocationContext invocationContext;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected ServletConfig config;

    // request contexts
    protected RequestContextChainingService requestContexts;
    protected RC requestContext;
    protected HttpServletRequest newRequest;
    protected HttpServletResponse newResponse;

    /**
     * 创建beanFactory。
     */
    protected final static void createBeanFactory(String configLocation) {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, configLocation)));
    }

    /**
     * 创建web client，注册servlets。
     */
    @Before
    public final void prepareWebClient() throws Exception {
        // Servlet container
        ServletRunner servletRunner = new PatchedServletRunner();

        registerServlets(servletRunner);
        servletRunner.registerServlet("/readfile/*", ReadFileServlet.class.getName());
        servletRunner.registerServlet("/servlet/*", NoopServlet.class.getName());
        servletRunner.registerServlet("*.do", NoopServlet.class.getName());

        // Servlet client
        client = servletRunner.newClient();
    }

    protected void registerServlets(ServletRunner runner) {
    }

    @After
    public final void clearWebEnv() {
        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * 调用noop servlet，取得request/response。
     */
    protected final void invokeNoopServlet(String uri) throws Exception {
        if (uri != null && uri.startsWith("http")) {
            uri = URI.create(uri).normalize().toString(); // full uri
        } else {
            uri = URI.create("http://www.taobao.com/" + trimToEmpty(uri)).normalize().toString(); // partial uri
        }

        invokeNoopServlet(new GetMethodWebRequest(uri));
    }

    /**
     * 调用noop servlet，取得request/response。
     */
    protected final void invokeNoopServlet(WebRequest req) throws Exception {
        invocationContext = client.newInvocation(req);
        request = new MyHttpRequest(invocationContext.getRequest(), req.getURL().toExternalForm());
        response = new MyHttpResponse(invocationContext.getResponse());
        config = invocationContext.getServlet().getServletConfig();
    }

    /**
     * 调用readfile servlet，取得request/response。
     */
    protected final void invokeReadFileServlet(String htmlfile) throws Exception {
        String uri = URI.create("http://www.taobao.com/readfile" + "?file=" + htmlfile).normalize().toString();

        // 取得初始页面和form
        WebResponse response = client.getResponse(new GetMethodWebRequest(uri));

        WebForm form = response.getFormWithName("myform");

        // 取得提交form的request
        WebRequest request = form.getRequest();

        request.setParameter("myparam", new String[] { "hello",
                "&#20013;&#21326;&#20154;&#27665;&#20849;&#21644;&#22269;" });

        try {
            request.setParameter("myfile", new UploadFileSpec[] { //
                    new UploadFileSpec(new File(srcdir, "smallfile.txt")), //
                            new UploadFileSpec(new File(srcdir, "smallfile_.JPG")), //
                            new UploadFileSpec(new File(srcdir, "smallfile.gif")), //
                            new UploadFileSpec(new File(srcdir, "smallfile")), //
                    });
        } catch (IllegalRequestParameterException e) {
        }

        client.putCookie("mycookie", "mycookievalue");

        invocationContext = client.newInvocation(request);

        this.request = new MyHttpRequest(invocationContext.getRequest(), uri);

        // 因为页面的content type是text/html; charset=UTF-8，
        // 所以应该以UTF-8方式解析request。
        this.request.setCharacterEncoding("UTF-8");

        this.response = new MyHttpResponse(invocationContext.getResponse());
        this.config = invocationContext.getServlet().getServletConfig();
    }

    /**
     * 取得request context。
     */
    protected final void initRequestContext() throws Exception {
        initRequestContext(null);
    }

    /**
     * 取得request context。
     */
    protected final void initRequestContext(String beanName) throws Exception {
        if (beanName == null) {
            beanName = getDefaultBeanName();
        }

        requestContexts = (RequestContextChainingService) factory.getBean(beanName);

        RequestContext topRC = requestContexts.getRequestContext(config.getServletContext(), request, response);

        assertNotNull(topRC);

        requestContext = findRequestContext(topRC, requestContextInterface);

        assertNotNull(requestContextInterface.getName(), requestContext);

        newRequest = requestContext.getRequest();
        newResponse = requestContext.getResponse();

        afterInitRequestContext();
    }

    protected void afterInitRequestContext() throws Exception {
    }

    /**
     * 将服务端response提交到client。
     */
    protected final void commitToClient() throws Exception {
        clientResponse = client.getResponse(invocationContext);
    }

    /**
     * 从request context interface中取得默认bean名称。
     */
    protected String getDefaultBeanName() {
        String name = requestContextInterface.getSimpleName();
        Matcher matcher = Pattern.compile("(\\w+)RequestContext").matcher(name);

        assertTrue(name, matcher.find());

        return com.alibaba.citrus.util.StringUtil.toCamelCase(matcher.group(1));
    }

    /**
     * 不做任何事的servlet。
     */
    public static class NoopServlet extends HttpServlet {
        private static final long serialVersionUID = 3034658026956449398L;

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
            doGet(request, response);
        }
    }

    /**
     * 返回文件内容的servlet。
     */
    public static class ReadFileServlet extends HttpServlet {
        private static final long serialVersionUID = 3689913963685360948L;

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
            response.setContentType("text/html; charset=UTF-8");

            PrintWriter out = response.getWriter();

            String html = StreamUtil.readText(new FileInputStream(new File(srcdir, request.getParameter("file"))),
                    "GBK", true);

            out.println(html);
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
            doGet(request, response);
        }
    }

    /**
     * 由于httpunit目前未实现setCharacterEncoding方法，getQueryString()也实现得有问题，
     * 所以只能将request包装一下。
     */
    public static class MyHttpRequest extends HttpServletRequestWrapper {
        private String charset;
        private String overrideQueryString;
        private String server = "www.taobao.com";
        private int port = 80;
        private boolean sessionCreated;

        public MyHttpRequest(HttpServletRequest request, String uri) {
            super(request);

            if (uri != null) {
                int index = uri.indexOf("?");

                if (index >= 0) {
                    this.overrideQueryString = uri.substring(index + 1);
                }
            }
        }

        @Override
        public String getQueryString() {
            if (overrideQueryString == null) {
                return super.getQueryString();
            } else {
                return overrideQueryString;
            }
        }

        @Override
        public String getCharacterEncoding() {
            return charset;
        }

        @Override
        public void setCharacterEncoding(String charset) throws UnsupportedEncodingException {
            this.charset = charset;
        }

        /**
         * 默认实现总是返回localhost，只好覆盖此方法。
         */
        @Override
        public String getServerName() {
            return server;
        }

        public void setServerName(String server) {
            this.server = server;
        }

        /**
         * 默认实现总是返回0，只好覆盖此方法。
         */
        @Override
        public int getServerPort() {
            return port;
        }

        public void setServerPort(int port) {
            this.port = port;
        }

        /**
         * 监视getSession方法的调用。
         */
        public boolean isSessionCreated() {
            return sessionCreated;
        }

        @Override
        public HttpSession getSession() {
            sessionCreated = true;
            return super.getSession();
        }

        @Override
        public HttpSession getSession(boolean create) {
            if (create) {
                sessionCreated = true;
            }

            return super.getSession(create);
        }
    }

    /**
     * 由于httpunit目前未实现commit以后抛IllegalStateException，所以只能将response包装一下。
     */
    public static class MyHttpResponse extends HttpServletResponseWrapper {
        private boolean committed;

        public MyHttpResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public boolean isCommitted() {
            return super.isCommitted() || committed;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            ensureNotCommited();
            super.sendError(sc, msg);
            committed = true;
        }

        @Override
        public void sendError(int sc) throws IOException {
            ensureNotCommited();
            super.sendError(sc);
            committed = true;
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            ensureNotCommited();
            super.sendRedirect(location);
            committed = true;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            ensureNotCommited();
            return super.getOutputStream();
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            ensureNotCommited();
            return super.getWriter();
        }

        @Override
        public void reset() {
            ensureNotCommited();
            super.reset();
        }

        @Override
        public void resetBuffer() {
            ensureNotCommited();
            super.resetBuffer();
        }

        @Override
        public void setLocale(Locale locale) {
            // 防止unsupported operation exception
        }

        @Override
        public void setContentType(String type) {
            if (type == null || type.indexOf("charset=") == -1) {
                setCharacterEncoding(null);
            }

            super.setContentType(type);
        }

        @Override
        public void setBufferSize(int size) {
            ensureNotCommited();
            super.setBufferSize(size);
        }

        private void ensureNotCommited() {
            if (isCommitted()) {
                throw new IllegalStateException();
            }
        }
    }
}
