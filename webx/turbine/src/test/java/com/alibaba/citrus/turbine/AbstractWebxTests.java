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
package com.alibaba.citrus.turbine;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.service.requestcontext.util.RequestContextUtil;
import com.alibaba.citrus.turbine.util.TurbineUtil;
import com.alibaba.citrus.util.ServletUtil;
import com.alibaba.citrus.util.io.StreamUtil;
import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.WebxController;
import com.alibaba.citrus.webx.servlet.WebxFrameworkFilter;
import com.alibaba.citrus.webx.util.WebxUtil;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.PatchedServletRunner;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public abstract class AbstractWebxTests {
    // web client
    protected static ServletUnitClient client;

    // controller/component
    protected static WebxComponent component;
    protected static WebxController controller;

    // container
    protected static ApplicationContext factory;

    // servlet context
    protected static ServletContext context;

    // servlet request/response
    protected InvocationContext invocationContext;
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    // request contexts
    protected RequestContextChainingService requestContexts;
    protected TurbineRunDataInternal rundata;
    protected HttpServletRequest newRequest;
    protected HttpServletResponse newResponse;

    @After
    public void clearClient() {
        if (client != null) {
            client.clearContents();
        }
    }

    protected static void prepareServlet() throws Exception {
        // Servlet container
        File webXml = new File(srcdir, "WEB-INF/web.xml");
        ServletRunner servletRunner = new PatchedServletRunner(webXml, "");

        // Servlet client
        client = servletRunner.newClient();
        client.setExceptionsThrownOnErrorStatus(false);
        client.getClientProperties().setAutoRedirect(false);

        // Filter
        WebxFrameworkFilter filter = (WebxFrameworkFilter) client.newInvocation("http://www.taobao.com/app1")
                .getFilter();
        context = filter.getWebxComponents().getParentApplicationContext().getServletContext();

        // Webx Controller
        component = filter.getWebxComponents().getComponent("app1");

        controller = component.getWebxController();
        assertNotNull(controller);

        // ApplicationContext
        factory = component.getApplicationContext();
        assertNotNull(factory);
    }

    /**
     * 调用servlet，取得request/response。
     */
    protected final void getInvocationContext(String uri) throws Exception {
        getInvocationContext(uri, null);
    }

    protected final void getInvocationContext(String uri, WebRequestCallback cb) throws Exception {
        if (uri != null && uri.startsWith("http")) {
            uri = URI.create(uri).normalize().toString(); // full uri
        } else {
            uri = URI.create("http://www.taobao.com/" + trimToEmpty(uri)).normalize().toString(); // partial uri
        }

        WebRequest wr = new GetMethodWebRequest(uri);

        if (cb != null) {
            cb.process(wr);
        }

        invocationContext = client.newInvocation(wr);
        request = invocationContext.getRequest();
        response = invocationContext.getResponse();
    }

    public interface WebRequestCallback {
        void process(WebRequest wr);
    }

    /**
     * 取得request context。
     */
    protected final void initRequestContext() {
        requestContexts = (RequestContextChainingService) factory.getBean("requestContexts");

        RequestContext topRC = requestContexts.getRequestContext(context, request, response);

        assertNotNull(topRC);

        newRequest = topRC.getRequest();
        newResponse = topRC.getResponse();

        rundata = (TurbineRunDataInternal) TurbineUtil.getTurbineRunData(newRequest, true);

        WebxUtil.setCurrentComponent(newRequest, component);

        afterInitRequestContext();
    }

    protected final void commitRequestContext() throws Exception {
        requestContexts.commitRequestContext(RequestContextUtil.getRequestContext(newRequest));
        client.getResponse(invocationContext);
    }

    protected void afterInitRequestContext() {
    }

    public static class ResourceServlet extends HttpServlet {
        private static final long serialVersionUID = -5288195741719029071L;

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String path = ServletUtil.getResourcePath(req);
            URL resource = getServletContext().getResource(path);
            URLConnection conn = resource.openConnection();

            resp.setContentType(conn.getContentType());
            StreamUtil.io(conn.getInputStream(), resp.getOutputStream(), true, false);
        }
    }
}
