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
package com.alibaba.citrus.webx;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;
import com.alibaba.citrus.util.ServletUtil;
import com.alibaba.citrus.util.io.StreamUtil;
import com.alibaba.citrus.webx.handler.RequestHandler;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.pipeline.TestExceptionValve;
import com.alibaba.citrus.webx.pipeline.TestValve;
import com.alibaba.citrus.webx.pipeline.ValveRunner;
import com.alibaba.citrus.webx.support.AbstractWebxRootController;
import com.alibaba.citrus.webx.util.ErrorHandlerHelper;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.javascript.JavaScript;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.PatchedServletRunner;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public abstract class AbstractWebxTests {
    // web client
    protected ServletUnitClient client;
    protected WebResponse clientResponse;
    protected int clientResponseCode;
    protected String clientResponseContent;

    protected final void prepareWebClient(String webXmlName) throws Exception {
        // Servlet container
        File webInf = new File(srcdir, "WEB-INF");
        File webXml = new File(webInf, defaultIfEmpty(webXmlName, "web.xml"));

        ServletRunner servletRunner = new PatchedServletRunner(webXml, "");

        // Servlet client
        client = servletRunner.newClient();
        client.setExceptionsThrownOnErrorStatus(false);
        client.getClientProperties().setAutoRedirect(false);

        // Ignore script error
        JavaScript.setThrowExceptionsOnError(false);
    }

    /**
     * 调用servlet，取得request/response。
     */
    protected final void invokeServlet(String uri) throws Exception {
        if (uri != null && uri.startsWith("http")) {
            uri = URI.create(uri).normalize().toString(); // full uri
        } else {
            uri = URI.create("http://www.taobao.com/" + trimToEmpty(uri)).normalize().toString(); // partial uri
        }

        InvocationContext ic = client.newInvocation(uri);
        ic.getFilter().doFilter(new MyHttpRequest(ic.getRequest(), uri), ic.getResponse(), ic.getFilterChain());

        clientResponse = client.getResponse(ic);
        clientResponseCode = clientResponse.getResponseCode();
        clientResponseContent = clientResponse.getText();
    }

    @Before
    public final void initPipeline() {
        TestValve.runnerHolder.set(new SimpleValveRunner());
        TestExceptionValve.runnerHolder.set(new SimpleValveRunner());
    }

    @After
    public void dispose() {
        TestValve.runnerHolder.remove();
        TestExceptionValve.runnerHolder.remove();
    }

    /**
     * 设置<code>WebxDispatcherServlet.internalHandlerMapping.errorHandler</code>。
     */
    protected void setErrorHandler(AbstractWebxRootController controller, RequestHandler handler) throws Exception {
        Object o1 = getFieldValue(controller, "internalHandlerMapping", null);
        getAccessibleField(o1.getClass(), "errorHandler").set(o1, handler);
    }

    /**
     * 简化的error page handler。
     */
    protected static class SimpleValveRunner implements ValveRunner {
        public void run(RunData rundata, PipelineContext pipelineContext) throws Exception {
            rundata.setContentType("text/plain");
            rundata.getResponse().getWriter().println("hello!");
        }
    }

    /**
     * 简化的error page handler。
     */
    public static class TestErrorHandler implements RequestHandler {
        public void handleRequest(RequestHandlerContext ctx) throws Exception {
            HttpServletResponse response = ctx.getResponse();
            ErrorHandlerHelper helper = ErrorHandlerHelper.getInstance(ctx.getRequest());
            Throwable exception = helper.getException();

            response.setContentType("text/html; charset=UTF-8");

            PrintWriter out = response.getWriter();

            if (exception != null) {
                out.println("<pre>");
                exception.printStackTrace(out);
                out.println("</pre>");
            }

            out.flush();
        }
    }

    public static class ResourceServlet extends HttpServlet {
        private static final long serialVersionUID = -5288195741719029071L;

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String path = ServletUtil.getResourcePath(req);

            if ("".equals(path) || "/".equals(path)) {
                resp.setContentType("text/plain");
                PrintWriter out = resp.getWriter();

                out.print("Homepage");
                out.flush();
            } else {
                URL resource = getServletContext().getResource(path);
                URLConnection conn = resource.openConnection();

                resp.setContentType(conn.getContentType());
                StreamUtil.io(conn.getInputStream(), resp.getOutputStream(), true, false);
            }
        }
    }

    /**
     * 由于httpunit getQueryString()实现得有问题， 所以只能将request包装一下。
     */
    public static class MyHttpRequest extends HttpServletRequestWrapper {
        private String overrideQueryString;

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
    }
}
