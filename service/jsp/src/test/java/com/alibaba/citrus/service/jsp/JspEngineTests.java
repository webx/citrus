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
package com.alibaba.citrus.service.jsp;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.io.ResourceLoader;

import com.alibaba.citrus.service.jsp.impl.JspEngineImpl;
import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.service.resource.support.ResourceLoadingSupport;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateNotFoundException;
import com.alibaba.citrus.service.template.TemplateService;
import com.alibaba.citrus.service.template.support.MappedTemplateContext;
import com.alibaba.citrus.springext.support.context.XmlWebApplicationContext;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class JspEngineTests {
    private ServletContext servletContext;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletUnitClient client;
    private InvocationContext ic;
    private XmlWebApplicationContext factory;
    private TemplateService templateService;
    private JspEngineImpl engine;

    private void initServlet(String webXml) throws Exception {
        ServletRunner runner = new ServletRunner(new File(srcdir, webXml), "");
        client = runner.newClient();
        ic = client.newInvocation("http://localhost:8080/app1");

        servletContext = new ServletContextWrapper(ic.getServlet().getServletConfig().getServletContext());
        request = ic.getRequest();
        response = ic.getResponse();
    }

    private void initFactory() {
        factory = new XmlWebApplicationContext();

        factory.setConfigLocation("services.xml");
        factory.setServletContext(servletContext);
        factory.setResourceLoadingExtender(new ResourceLoadingSupport(factory));
        factory.refresh();

        templateService = (TemplateService) factory.getBean("templateService");
        engine = (JspEngineImpl) templateService.getTemplateEngine("jsp");

        assertNotNull(engine);
    }

    @Test
    public void createEngineDirectly() throws Exception {
        initServlet("webapp/WEB-INF/web.xml");

        // no servletContext
        try {
            new JspEngineImpl(null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("servletContext"));
        }

        // no request
        try {
            new JspEngineImpl(servletContext, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("request"));
        }

        // no response
        try {
            new JspEngineImpl(servletContext, createMock(MockRequestProxy.class), null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("response"));
        }

        // request not a proxy
        try {
            new JspEngineImpl(servletContext, request, response);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("expects a proxy delegating to a real object, but got an object of type "
                    + request.getClass().getName()));
        }

        // response not a proxy
        try {
            new JspEngineImpl(servletContext, createMock(MockRequestProxy.class), response);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("expects a proxy delegating to a real object, but got an object of type "
                    + response.getClass().getName()));

        }

        // no resource loader
        JspEngineImpl engine = new JspEngineImpl(servletContext, createMock(MockRequestProxy.class),
                createMock(MockResponseProxy.class));

        try {
            engine.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("resourceLoader"));
        }

        // ok
        engine = new JspEngineImpl(servletContext, createMock(MockRequestProxy.class),
                createMock(MockResponseProxy.class));

        engine.setResourceLoader(createMock(ResourceLoader.class));

        // not inited yet
        try {
            engine.getPathWithinServletContext("/test");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("has not been initialized yet"));
        }

        engine.afterPropertiesSet();
    }

    @Test
    public void contextRoot_getResourceOfRoot() throws Exception {
        // getResource("/")存在
        initServlet("webapp1/WEB-INF/web.xml");
        initFactory();

        assertEquals("/mytemplates/mytemplate.jsp", engine.getPathWithinServletContext("/mytemplate.jsp"));

        try {
            engine.getPathWithinServletContext("/not/exist.jsp");
            fail();
        } catch (TemplateNotFoundException e) {
            assertThat(e, exception("Template", "/not/exist.jsp", "not found"));
        }
    }

    @Test
    public void contextRoot_getResourceOfWebXml() throws Exception {
        // getResource("/")不存在，"WEB-INF/web.xml"存在
        initServlet("webapp1/WEB-INF/web.xml");
        ((ServletContextWrapper) servletContext).setSupportGetResourceOfRoot(false); // 使getResource("/")返回null
        initFactory();

        assertEquals("/mytemplates/mytemplate.jsp", engine.getPathWithinServletContext("/mytemplate.jsp"));

        try {
            engine.getPathWithinServletContext("/not/exist.jsp");
            fail();
        } catch (TemplateNotFoundException e) {
            assertThat(e, exception("Template", "/not/exist.jsp", "not found"));
        }
    }

    @Test
    public void contextRoot_failed() throws Exception {
        // getResource("/")不存在，WEB-INF/web.xml也不存在
        initServlet("webapp2/WEB-INF-2/web.xml");
        ((ServletContextWrapper) servletContext).setSupportGetResourceOfRoot(false); // 使getResource("/")返回null

        try {
            initFactory();
            fail();
        } catch (FatalBeanException e) {
            assertThat(e, exception(IllegalArgumentException.class, "Could not find WEBROOT"));
        }
    }

    @Test
    public void exists() throws Exception {
        // with default path
        initServlet("webapp/WEB-INF/web.xml");
        initFactory();

        assertEquals(true, engine.exists("/test.jsp"));
        assertEquals(false, engine.exists("/not/exist.jsp"));

        assertEquals(true, templateService.exists("/test.jsp"));
        assertEquals(false, templateService.exists("/not/exist.jsp"));
    }

    @Test
    public void render_getText() throws Exception {
        render(1);
    }

    @Test
    public void render_writeToStream() throws Exception {
        render(2);
    }

    @Test
    public void render_writeToWriter() throws Exception {
        render(3);
    }

    private void render(int type) throws Exception {
        initServlet("webapp/WEB-INF/web.xml");
        initFactory();

        RequestContextChainingService requestContexts = (RequestContextChainingService) factory
                .getBean("requestContexts");
        RequestContext rc = null;

        try {
            // 预备request, response
            rc = requestContexts.getRequestContext(servletContext, request, response);

            // 设置contentType和charset，和jsp中的设置不同，以此为准
            rc.getResponse().setContentType("text/plain; charset=GBK"); // JSP: text/html; charset=UTF-8

            // 预备template context
            TemplateContext context = new MappedTemplateContext();
            context.put("hello", "中国");

            // 渲染
            switch (type) {
                case 1:
                    assertEquals("", templateService.getText("/test.jsp", context));
                    break;

                case 2:
                    templateService.writeTo("/test.jsp", context, (OutputStream) null);
                    break;

                case 3:
                    templateService.writeTo("/test.jsp", context, (Writer) null);
                    break;

                default:
                    fail();
                    break;
            }
        } finally {
            // 结束并提交response
            requestContexts.commitRequestContext(rc);
        }

        WebResponse webResponse = client.getResponse(ic);

        assertEquals("text/plain", webResponse.getContentType());
        assertEquals("GBK", webResponse.getCharacterSet());
        assertEquals("hello, 中国!", webResponse.getText().trim());
    }

    public static interface MockRequestProxy extends HttpServletRequest, ObjectFactory {
    }

    public static interface MockResponseProxy extends HttpServletResponse, ObjectFactory {
    }

    public static class MyServlet extends HttpServlet {
        private static final long serialVersionUID = -4881126944249115409L;
    }

    public static class ServletContextWrapper implements ServletContext {
        private final ServletContext servletContext;
        private boolean supportGetResourceOfRoot = true;

        public ServletContextWrapper(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        public void setSupportGetResourceOfRoot(boolean supportGetResourceOfRoot) {
            this.supportGetResourceOfRoot = supportGetResourceOfRoot;
        }

        /**
         * 判断当resource不存在时，返回null。
         */
        public URL getResource(String path) throws MalformedURLException {
            if (("/".equals(path) || isEmpty(path)) && !supportGetResourceOfRoot) {
                return null;
            }

            URL url = servletContext.getResource(path);

            if (url.getProtocol().equals("file")) {
                try {
                    if (!new File(url.toURI()).exists()) {
                        return null;
                    }
                } catch (URISyntaxException e) {
                    return url;
                }
            }

            // 除去末尾的/，配合测试
            String urlstr = url.toExternalForm();

            if (urlstr.endsWith("/")) {
                urlstr = urlstr.substring(0, urlstr.length() - 1);
            }

            return new URL(urlstr);
        }

        public Set<?> getResourcePaths(String path) {
            return servletContext.getResourcePaths(path);
        }

        public Object getAttribute(String name) {
            return servletContext.getAttribute(name);
        }

        public Enumeration<?> getAttributeNames() {
            return servletContext.getAttributeNames();
        }

        public ServletContext getContext(String uripath) {
            return servletContext.getContext(uripath);
        }

        public String getContextPath() {
            return servletContext.getContextPath();
        }

        public String getInitParameter(String name) {
            return servletContext.getInitParameter(name);
        }

        public Enumeration<?> getInitParameterNames() {
            return servletContext.getInitParameterNames();
        }

        public int getMajorVersion() {
            return servletContext.getMajorVersion();
        }

        public String getMimeType(String file) {
            return servletContext.getMimeType(file);
        }

        public int getMinorVersion() {
            return servletContext.getMinorVersion();
        }

        public RequestDispatcher getNamedDispatcher(String name) {
            return servletContext.getNamedDispatcher(name);
        }

        public String getRealPath(String path) {
            return servletContext.getRealPath(path);
        }

        public RequestDispatcher getRequestDispatcher(String path) {
            return servletContext.getRequestDispatcher(path);
        }

        public InputStream getResourceAsStream(String path) {
            return servletContext.getResourceAsStream(path);
        }

        public String getServerInfo() {
            return servletContext.getServerInfo();
        }

        @Deprecated
        public Servlet getServlet(String name) throws ServletException {
            return servletContext.getServlet(name);
        }

        public String getServletContextName() {
            return servletContext.getServletContextName();
        }

        @Deprecated
        public Enumeration<?> getServletNames() {
            return servletContext.getServletNames();
        }

        @Deprecated
        public Enumeration<?> getServlets() {
            return servletContext.getServlets();
        }

        @Deprecated
        public void log(Exception exception, String msg) {
            servletContext.log(exception, msg);
        }

        public void log(String message, Throwable throwable) {
            servletContext.log(message, throwable);
        }

        public void log(String msg) {
            servletContext.log(msg);
        }

        public void removeAttribute(String name) {
            servletContext.removeAttribute(name);
        }

        public void setAttribute(String name, Object object) {
            servletContext.setAttribute(name, object);
        }
    }
}
