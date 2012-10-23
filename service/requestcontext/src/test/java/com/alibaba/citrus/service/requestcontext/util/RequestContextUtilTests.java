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

package com.alibaba.citrus.service.requestcontext.util;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.util.internal.OverridedMethodBuilder;
import org.junit.Before;
import org.junit.Test;

public class RequestContextUtilTests {
    private RequestContext      requestContext;
    private HttpServletRequest  request;
    private HttpServletResponse response;
    private Map<String, Object> attrs;

    @Before
    public void init() {
        // mock request
        HttpServletRequest mockRequest = createMock(HttpServletRequest.class);

        // request
        attrs = createHashMap();

        request = (HttpServletRequest) new OverridedMethodBuilder(new Class<?>[] { HttpServletRequest.class }, mockRequest, new Object() {
            public Object getAttribute(String name) {
                return attrs.get(name);
            }

            public void setAttribute(String name, Object o) {
                attrs.put(name, o);
            }

            public void removeAttribute(String name) {
                attrs.remove(name);
            }
        }).toObject();

        // response
        response = createMock(HttpServletResponse.class);

        // request context
        requestContext = createMock(RequestContext.class);

        expect(requestContext.getRequest()).andReturn(request).anyTimes();
        expect(requestContext.getResponse()).andReturn(response).anyTimes();

        replay(requestContext);
    }

    @Test
    public void setRequestContext() {
        RequestContextUtil.setRequestContext(requestContext);
        assertSame(requestContext, attrs.get("_outer_webx3_request_context_"));
    }

    @Test
    public void getRequestContext() {
        assertNull(RequestContextUtil.getRequestContext(request));

        RequestContextUtil.setRequestContext(requestContext);
        assertSame(requestContext, RequestContextUtil.getRequestContext(request));
    }

    @Test
    public void resetRequestContext() {
        RequestContextUtil.setRequestContext(requestContext);
        assertSame(requestContext, attrs.get("_outer_webx3_request_context_"));
        assertSame(null, attrs.get("_outer_webx3_request_context_async_"));

        RequestContextUtil.resetRequestContext(request, false);
        assertSame(null, attrs.get("_outer_webx3_request_context_"));
        assertSame(null, attrs.get("_outer_webx3_request_context_async_"));
    }

    @Test
    public void resetRequestContext_async() {
        RequestContextUtil.setRequestContext(requestContext);
        assertSame(requestContext, attrs.get("_outer_webx3_request_context_"));
        assertSame(null, attrs.get("_outer_webx3_request_context_async_"));

        RequestContextUtil.resetRequestContext(request, true);
        assertSame(null, attrs.get("_outer_webx3_request_context_"));
        assertSame(requestContext, attrs.get("_outer_webx3_request_context_async_"));
    }

    @Test
    public void popRequestContextAsync() {
        RequestContextUtil.setRequestContext(requestContext);
        RequestContextUtil.resetRequestContext(request, true);

        assertSame(null, attrs.get("_outer_webx3_request_context_"));
        assertSame(requestContext, attrs.get("_outer_webx3_request_context_async_"));

        assertSame(requestContext, RequestContextUtil.popRequestContextAsync(request));

        assertSame(null, attrs.get("_outer_webx3_request_context_"));
        assertSame(null, attrs.get("_outer_webx3_request_context_async_"));
    }
}
