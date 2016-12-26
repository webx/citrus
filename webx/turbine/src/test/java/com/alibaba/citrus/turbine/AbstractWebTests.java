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

package com.alibaba.citrus.turbine;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.turbine.util.TurbineUtil;
import com.alibaba.citrus.util.internal.Servlet3Util;
import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.util.WebxUtil;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.protocol.UploadFileSpec;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URI;

import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.findRequestContext;
import static com.alibaba.citrus.test.TestEnvStatic.srcdir;
import static com.alibaba.citrus.util.StringUtil.trimToEmpty;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractWebTests {
    protected static ApplicationContext factory;

    // httpunit objects
    protected static ServletUnitClient   client;
    protected static ServletConfig       config;
    protected        InvocationContext   invocationContext;
    protected        HttpServletRequest  request;
    protected        HttpServletResponse response;

    // request context objects
    protected RequestContextChainingService requestContexts;
    protected RunData                       rundata;
    protected HttpServletRequest            newRequest;
    protected HttpServletResponse           newResponse;

    static {
        Servlet3Util.setDisableServlet3Features(true); // 禁用servlet3，因为httpunit还不支持
    }

    protected static ApplicationContext createContext(String name) {
        return new XmlApplicationContext(new FileSystemResource(new File(srcdir, name)), null);
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

    protected final void getInvocationContext(String uri, Object... params) throws Exception {
        if (uri != null && uri.startsWith("http")) {
            uri = URI.create(uri).normalize().toString(); // full uri
        } else {
            uri = URI.create("http://www.taobao.com/" + trimToEmpty(uri)).normalize().toString(); // partial uri
        }

        PostMethodWebRequest post = new PostMethodWebRequest(uri, true);

        assertTrue(params.length % 2 == 0);

        for (int i = 0; i < params.length; i += 2) {
            String key = (String) params[i];
            Object value = params[i + 1];

            if (value instanceof File) {
                post.selectFile(key, (File) value);
            } else if (value instanceof File[]) {
                UploadFileSpec[] specs = new UploadFileSpec[((File[]) value).length];

                for (int j = 0; j < ((File[]) value).length; j++) {
                    specs[j] = new UploadFileSpec(((File[]) value)[j]);
                }

                post.setParameter(key, specs);
            } else {
                post.setParameter(key, (String) value);
            }
        }

        invocationContext = client.newInvocation(post);
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

        // 创建turbine rundata
        TurbineUtil.getTurbineRunData(newRequest, true);

        // 设置当前component
        WebxComponent component = createMock(WebxComponent.class);
        GenericWebApplicationContext webAppContext = new GenericWebApplicationContext();
        webAppContext.setParent(factory);
        webAppContext.refresh();
        expect(component.getApplicationContext()).andReturn(webAppContext).anyTimes();
        expect(component.getName()).andReturn("app1").anyTimes();
        replay(component);
        WebxUtil.setCurrentComponent(newRequest, component);
    }

    public static class MyServlet extends HttpServlet {
        private static final long serialVersionUID = -2900023700608808272L;
    }
}
