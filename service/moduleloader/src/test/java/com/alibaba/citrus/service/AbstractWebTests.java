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
package com.alibaba.citrus.service;

import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.*;
import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public abstract class AbstractWebTests {
    private static ApplicationContext root;
    private static ApplicationContext devContext;
    private static ApplicationContext prodContext;
    protected static ApplicationContext factory;

    // httpunit objects
    protected static ServletUnitClient client;
    protected static ServletConfig config;
    protected InvocationContext invocationContext;
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    // request context objects
    protected RequestContextChainingService requestContexts;
    protected RunData rundata;
    protected HttpServletRequest newRequest;
    protected HttpServletResponse newResponse;

    @BeforeClass
    public static void initParentFactory() {
        root = createContext("base-root.xml", null);
        devContext = createContext("base-dev.xml", root);
        prodContext = createContext("base-production.xml", root);
    }

    protected static ApplicationContext createContext(String name, boolean prod) {
        return createContext(name, prod ? prodContext : devContext);
    }

    protected static ApplicationContext createContext(String name, ApplicationContext parent) {
        return new XmlApplicationContext(new FileSystemResource(new File(srcdir, name)), parent);
    }

    protected static void prepareServlet() throws Exception {
        ServletRunner servletRunner = new ServletRunner();

        servletRunner.registerServlet("/app1/*", MyServlet.class.getName());

        client = servletRunner.newClient();
        client.setExceptionsThrownOnErrorStatus(false);
        client.getClientProperties().setAutoRedirect(false);

        MyServlet servlet = (MyServlet) client.newInvocation("http://www.taobao.com/app1").getServlet();
        assertNotNull(servlet);

        config = servlet.getServletConfig();
    }

    protected final void getInvocationContext(String uri) throws Exception {
        if (uri != null && uri.startsWith("http")) {
            uri = URI.create(uri).normalize().toString(); // full uri
        } else {
            uri = URI.create("http://www.taobao.com/" + trimToEmpty(uri)).normalize().toString(); // partial uri
        }

        invocationContext = client.newInvocation(uri);
        request = invocationContext.getRequest();
        response = invocationContext.getResponse();
        config = invocationContext.getServlet().getServletConfig();
    }

    protected final void initRequestContext() {
        initRequestContext(factory);
    }

    protected final void initRequestContext(ApplicationContext factory) {
        requestContexts = (RequestContextChainingService) factory.getBean("requestContexts");

        RequestContext topRC = requestContexts.getRequestContext(config.getServletContext(), request, response);

        assertNotNull(topRC);

        rundata = findRequestContext(topRC, RunData.class);

        newRequest = topRC.getRequest();
        newResponse = topRC.getResponse();
    }

    public static class MyServlet extends HttpServlet {
        private static final long serialVersionUID = -2900023700608808272L;
    }
}
