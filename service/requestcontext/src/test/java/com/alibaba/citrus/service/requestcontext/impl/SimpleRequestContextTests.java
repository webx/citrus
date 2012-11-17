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

package com.alibaba.citrus.service.requestcontext.impl;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import org.junit.Before;
import org.junit.Test;

public class SimpleRequestContextTests {
    private RequestContextChainingService service;
    private ServletContext                servletContext;
    private HttpServletRequest            request;
    private HttpServletResponse           response;
    private RequestContext                requestContext;
    private SimpleRequestContext          simpleRequestContext;

    @Before
    public void init() {
        service = createMock(RequestContextChainingService.class);
        servletContext = createMock(ServletContext.class);
        request = createMock(HttpServletRequest.class);
        response = createMock(HttpServletResponse.class);
        requestContext = createMock(RequestContext.class);

        simpleRequestContext = new SimpleRequestContext(servletContext, request, response, service);
    }

    @Test
    public void commit_noTopRequestContext() {
        replay(service);
        simpleRequestContext.commitHeaders();
        verify(service);
    }

    @Test
    public void commit_withTopRequestContext() {
        service.commitHeaders(requestContext);
        expectLastCall().once();
        replay(service);
        simpleRequestContext.setTopRequestContext(requestContext);
        simpleRequestContext.commitHeaders();
        verify(service);
    }

    @Test
    public void commit_multipleTimes() {
        service.commitHeaders(requestContext);
        expectLastCall().once();
        replay(service);

        simpleRequestContext.setTopRequestContext(requestContext);
        simpleRequestContext.commitHeaders();
        simpleRequestContext.setHeadersCommitted(true);

        simpleRequestContext.commitHeaders();
        simpleRequestContext.commitHeaders();
        simpleRequestContext.commitHeaders();
        verify(service);
    }

    @Test
    public void headersCommitted() {
        assertEquals(false, simpleRequestContext.isHeadersCommitted());

        simpleRequestContext.setHeadersCommitted(true);
        assertEquals(true, simpleRequestContext.isHeadersCommitted());

        simpleRequestContext.setHeadersCommitted(false);
        assertEquals(false, simpleRequestContext.isHeadersCommitted());

        simpleRequestContext.setCommitted(true);
        assertEquals(true, simpleRequestContext.isHeadersCommitted());

        simpleRequestContext.setCommitted(true);
        simpleRequestContext.setHeadersCommitted(true);
        assertEquals(true, simpleRequestContext.isHeadersCommitted());
    }

    @Test
    public void committed() {
        assertEquals(false, simpleRequestContext.isCommitted());

        simpleRequestContext.setHeadersCommitted(true);
        assertEquals(false, simpleRequestContext.isCommitted());

        simpleRequestContext.setHeadersCommitted(false);
        assertEquals(false, simpleRequestContext.isCommitted());

        simpleRequestContext.setCommitted(true);
        assertEquals(true, simpleRequestContext.isCommitted());

        simpleRequestContext.setCommitted(true);
        simpleRequestContext.setHeadersCommitted(true);
        assertEquals(true, simpleRequestContext.isCommitted());
    }

    @Test
    public void getResponse() {
        assertTrue(simpleRequestContext.getResponse() instanceof CommittingAwareResponse);
    }
}
