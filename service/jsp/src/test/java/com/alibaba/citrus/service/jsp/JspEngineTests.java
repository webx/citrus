/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.service.jsp;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.jsp.impl.JspEngineImpl;
import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateNotFoundException;
import com.alibaba.citrus.service.template.support.MappedTemplateContext;
import com.alibaba.citrus.springext.util.ProxyTargetFactory;
import com.meterware.httpunit.WebResponse;
import org.junit.Test;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.ResourceLoader;

public class JspEngineTests extends AbstractJspEngineTests {
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
        servletContext = createServletContextWrapper(false); // 使getResource("/")返回null
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
        servletContext = createServletContextWrapper(false); // 使getResource("/")返回null

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
        assertEquals(true, engine.exists("/test.jspx"));
        assertEquals(false, engine.exists("/not/exist.jsp"));

        assertEquals(true, templateService.exists("/test.jsp"));
        assertEquals(true, templateService.exists("/test.jspx"));
        assertEquals(false, templateService.exists("/not/exist.jsp"));
    }

    @Test
    public void render_getText() throws Exception {
        render(1, false);
        render(1, true);
    }

    @Test
    public void render_writeToStream() throws Exception {
        render(2, false);
        render(2, true);
    }

    @Test
    public void render_writeToWriter() throws Exception {
        render(3, false);
        render(3, true);
    }

    private void render(int type, boolean jspx) throws Exception {
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

            String jsp = jspx ? "/test.jspx" : "/test.jsp";

            // 渲染
            switch (type) {
                case 1:
                    assertEquals("", templateService.getText(jsp, context));
                    break;

                case 2:
                    templateService.writeTo(jsp, context, (OutputStream) null);
                    break;

                case 3:
                    templateService.writeTo(jsp, context, (Writer) null);
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

    public static interface MockRequestProxy extends HttpServletRequest, ProxyTargetFactory {
    }

    public static interface MockResponseProxy extends HttpServletResponse, ProxyTargetFactory {
    }

    public static class MyServlet extends HttpServlet {
        private static final long serialVersionUID = -4881126944249115409L;
    }

    /** 由于jasper不支持jspx，故做一个假的servlet仅用于测试。在新版的tomcat中，将自动支持jspx。 */
    public static class FakeJspxServlet extends HttpServlet {
        private static final long serialVersionUID = 780039704847320821L;

        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                                                                                                IOException {
            String jspx = (String) request.getAttribute("javax.servlet.include.servlet_path");

            if (jspx.endsWith(".jspx")) {
                jspx = jspx.substring(0, jspx.length() - ".jspx".length()) + ".jsp";
            }

            RequestDispatcher rd = getServletContext().getRequestDispatcher(jspx);

            rd.include(request, response);
        }
    }
}
