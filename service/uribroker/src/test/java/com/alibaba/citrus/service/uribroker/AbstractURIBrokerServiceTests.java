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
package com.alibaba.citrus.service.uribroker;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.context.request.RequestScope;

import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.service.uribroker.impl.URIBrokerServiceImpl;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public abstract class AbstractURIBrokerServiceTests {
    protected static ApplicationContext factory;
    protected RequestContextChainingService requestContexts;
    protected HttpServletRequest request;
    protected URIBrokerServiceImpl service;

    @BeforeClass
    public static void initFactory() {
        System.setProperty("myserver", "www.taobao.com");
        factory = createContext("services.xml", createContext("services-root.xml", null));
    }

    protected final static ApplicationContext createContext(String configLocation, ApplicationContext parent) {
        XmlApplicationContext factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir,
                configLocation)), parent);
        factory.getBeanFactory().registerScope("request", new RequestScope());
        return factory;
    }

    @Before
    public final void prepareRequest() throws Exception {
        ServletRunner servletRunner = new ServletRunner(new File(srcdir, "WEB-INF/web.xml"), "/myapp");
        ServletUnitClient client = servletRunner.newClient();
        InvocationContext invocationContext = client.newInvocation("http://localhost/myapp/myservlet");

        HttpServletRequest request = invocationContext.getRequest();
        HttpServletResponse response = invocationContext.getResponse();
        ServletConfig config = invocationContext.getServlet().getServletConfig();

        this.requestContexts = (RequestContextChainingService) factory.getBean("requestContexts");
        this.request = this.requestContexts.getRequestContext(config.getServletContext(), request, response)
                .getRequest();

        assertNotNull(request);
    }

    @Before
    public final void prepareURIBrokerService() {
        service = (URIBrokerServiceImpl) factory.getBean("uriBrokerService");

        assertNotNull(service);
        assertSame(service, factory.getBean("uris"));
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
}
