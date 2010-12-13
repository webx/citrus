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
package com.alibaba.citrus.service.form;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.form.impl.FormServiceImpl;
import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.service.resource.support.context.ResourceLoadingXmlApplicationContext;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.protocol.UploadFileSpec;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public abstract class AbstractFormServiceTests {
    protected static ApplicationContext factory;
    protected FormServiceImpl formService;

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
    protected RequestContext rc;
    protected HttpServletRequest newRequest;
    protected HttpServletResponse newResponse;

    protected final void getFormService(String name) {
        getFormService(name, factory);
    }

    protected final void getFormService(String name, ApplicationContext factory) {
        formService = (FormServiceImpl) factory.getBean(name);
        assertNotNull(formService);
    }

    protected static ApplicationContext createContext(String name) {
        return createContext(name, null);
    }

    protected static ApplicationContext createContext(String name, boolean withUpload) {
        ApplicationContext parent = withUpload ? createContext("services-base-with-upload.xml", null) : createContext(
                "services-base-without-upload.xml", null);

        return createContext(name, parent);
    }

    protected static ApplicationContext createContext(String name, ApplicationContext parent) {
        return new ResourceLoadingXmlApplicationContext(new FileSystemResource(new File(srcdir, name)), parent);
    }

    @Before
    public final void createWebClient() throws Exception {
        // Servlet container
        ServletRunner servletRunner = new ServletRunner();
        servletRunner.registerServlet("/*", NoopServlet.class.getName());

        // Servlet client
        client = servletRunner.newClient();

        // Charset
        HttpUnitOptions.setDefaultCharacterSet("GBK");
    }

    @After
    public final void commit() throws Exception {
        if (rc != null) {
            requestContexts.commitRequestContext(rc);
        }
    }

    protected final void invokeGet(Object[][] args) throws Exception {
        invokeGet(factory, args);
    }

    protected final void invokeGet(ApplicationContext factory, Object[][] args) throws Exception {
        invokeServlet(factory, new GetMethodWebRequest("http://localhost/"), args);
    }

    protected final void invokePost(Object[][] args) throws Exception {
        invokePost(factory, args);
    }

    protected final void invokePost(ApplicationContext factory, Object[][] args) throws Exception {
        invokeServlet(factory, new PostMethodWebRequest("http://localhost/"), args);
    }

    protected final void invokePostMime(Object[][] args) throws Exception {
        invokePostMime(factory, args);
    }

    protected final void invokePostMime(ApplicationContext factory, Object[][] args) throws Exception {
        invokeServlet(factory, new PostMethodWebRequest("http://localhost/", true), args);
    }

    private void invokeServlet(ApplicationContext factory, WebRequest wr, Object[][] args) throws Exception {
        assertNotNull(wr);

        if (args != null) {
            for (Object[] pair : args) {
                assertTrue(pair != null && pair.length == 2);

                String name = (String) pair[0];
                Object value = pair[1];

                if (value instanceof String[]) {
                    wr.setParameter(name, (String[]) value);
                } else if (value instanceof File[] || value instanceof File) {
                    File[] files = value instanceof File ? new File[] { (File) value } : (File[]) value;
                    UploadFileSpec[] specs = new UploadFileSpec[files.length];

                    for (int i = 0; i < files.length; i++) {
                        if (files[i].getName().contains(".")) {
                            specs[i] = new UploadFileSpec(files[i]);
                        } else {
                            specs[i] = new UploadFileSpec(files[i], ""); // 对于无后缀的文件，不设contentType
                        }
                    }

                    wr.setParameter(name, specs);
                } else {
                    wr.setParameter(name, (String) value);
                }
            }
        }

        invocationContext = client.newInvocation(wr);
        request = invocationContext.getRequest();
        response = invocationContext.getResponse();
        config = invocationContext.getServlet().getServletConfig();

        // request context
        requestContexts = (RequestContextChainingService) factory.getBean("requestContexts");
        rc = requestContexts.getRequestContext(config.getServletContext(), request, response);
        assertNotNull(rc);

        newRequest = rc.getRequest();
        newResponse = rc.getResponse();
    }

    protected final void commitToClient() throws Exception {
        clientResponse = client.getResponse(invocationContext);
    }

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
