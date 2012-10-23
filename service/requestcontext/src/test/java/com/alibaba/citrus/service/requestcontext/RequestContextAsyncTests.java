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

package com.alibaba.citrus.service.requestcontext;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.citrus.service.requestcontext.impl.RequestContextChainingServiceImpl;
import com.alibaba.citrus.util.internal.OverridedMethodBuilder;
import com.alibaba.citrus.util.internal.Servlet3Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RequestContextAsyncTests {
    private RequestContextChainingServiceImpl service;
    private ServletContext                    servletContext;
    private RequestContext                    requestContext;
    private HttpServletRequest                request;
    private HttpServletResponse               response;
    private Map<String, Object>               attrs;
    private boolean                           isAsyncStarted;
    private boolean                           originalServlet3Disabled;

    @Before
    public void init() throws Exception {
        originalServlet3Disabled = Servlet3Util.setDisableServlet3Features(false);

        // mock request
        HttpServletRequest mockRequest = createMock(HttpServletRequest.class);

        // request
        attrs = createHashMap();

        request = (HttpServletRequest) new OverridedMethodBuilder(new Class<?>[] { HttpServletRequest.class }, mockRequest, new Object() {
            public Locale getLocale() {
                return Locale.CHINA;
            }

            public HttpSession getSession(boolean create) {
                return null;
            }

            public Object getAttribute(String name) {
                return attrs.get(name);
            }

            public void setAttribute(String name, Object o) {
                attrs.put(name, o);
            }

            public void removeAttribute(String name) {
                attrs.remove(name);
            }

            public boolean isAsyncStarted() {
                return isAsyncStarted;
            }
        }).toObject();

        // response
        response = createMock(HttpServletResponse.class);

        // service
        RequestContextFactory<RequestContext> factory = new RequestContextFactory<RequestContext>() {
            @Override
            public RequestContext getRequestContextWrapper(RequestContext wrappedContext) {
                return wrappedContext;
            }

            @Override
            public Class<RequestContext> getRequestContextInterface() {
                return RequestContext.class;
            }

            @Override
            public Class<? extends RequestContext> getRequestContextProxyInterface() {
                return RequestContext.class;
            }

            @Override
            public String[] getFeatures() {
                return new String[0];
            }

            @Override
            public FeatureOrder[] featureOrders() {
                return new FeatureOrder[0];
            }
        };

        List<RequestContextFactory<?>> factories = createArrayList();
        factories.add(factory);

        service = new RequestContextChainingServiceImpl();
        service.setFactories(factories);
        service.afterPropertiesSet();

        // servlet context
        servletContext = createMock(ServletContext.class);
    }

    @After
    public void dispose() {
        Servlet3Util.setDisableServlet3Features(originalServlet3Disabled);
    }

    @Test
    public void getRequestContext() {
        requestContext = service.getRequestContext(servletContext, request, response);
        assertSame(requestContext, attrs.get("_outer_webx3_request_context_"));
    }

    @Test
    public void commitRequestContext() {
        requestContext = service.getRequestContext(servletContext, request, response);
        assertSame(requestContext, attrs.get("_outer_webx3_request_context_"));

        service.commitRequestContext(requestContext);
        assertSame(null, attrs.get("_outer_webx3_request_context_"));
        assertSame(null, attrs.get("_outer_webx3_request_context_async_"));
    }

    @Test
    public void commitRequestContext_async() {
        if (!Servlet3Util.isServlet3()) {
            return;
        }

        requestContext = service.getRequestContext(servletContext, request, response);
        assertSame(requestContext, attrs.get("_outer_webx3_request_context_"));

        isAsyncStarted = true;

        service.commitRequestContext(requestContext);
        assertSame(null, attrs.get("_outer_webx3_request_context_"));
        assertSame(requestContext, attrs.get("_outer_webx3_request_context_async_"));
    }

    @Test
    public void getRequestContext_async() {
        if (!Servlet3Util.isServlet3()) {
            return;
        }

        requestContext = service.getRequestContext(servletContext, request, response);
        isAsyncStarted = true;
        service.commitRequestContext(requestContext);

        assertSame(null, attrs.get("_outer_webx3_request_context_"));
        assertSame(requestContext, attrs.get("_outer_webx3_request_context_async_"));

        assertSame(requestContext, service.getRequestContext(servletContext, request, response));

        assertSame(requestContext, attrs.get("_outer_webx3_request_context_"));
        assertSame(null, attrs.get("_outer_webx3_request_context_async_"));
    }
}
