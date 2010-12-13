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
package com.alibaba.citrus.service.requestcontext.basic;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.basic.impl.BasicResponseImpl;
import com.alibaba.citrus.test.TestEnvStatic;

public abstract class AbstractBasicResponseTests {
    protected RequestContext requestContextMock;
    protected HttpServletRequest requestMock;
    protected HttpServletResponse responseMock;
    protected BasicResponseImpl response;

    static {
        TestEnvStatic.init();
    }

    protected void createResponse(Object... interceptors) {
        requestContextMock = createMock(RequestContext.class);
        requestMock = createMock(HttpServletRequest.class);
        responseMock = createMock(HttpServletResponse.class);

        resetMocks();

        response = new BasicResponseImpl(requestContextMock, responseMock, interceptors);
    }

    protected void resetMocks() {
        reset(requestContextMock, requestMock, responseMock);

        expect(requestContextMock.getRequest()).andReturn(requestMock).anyTimes();
        expect(requestContextMock.getResponse()).andReturn(responseMock).anyTimes();
    }

    protected void replayMocks() {
        replay(requestContextMock, requestMock, responseMock);
    }

    protected void verifyMocks() {
        verify(requestContextMock, requestMock, responseMock);
        resetMocks();
    }
}
