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
package com.alibaba.citrus.service.requestcontext;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.support.AbstractResponseWrapper;

public class ResponseWrapperTests {
    private RequestContext requestContext;
    private HttpServletResponse response;

    @Before
    public void init() {
        requestContext = createMock(RequestContext.class);
        response = createMock(HttpServletResponse.class);
    }

    @Test
    public void _toString() {
        MyResponse responseWrapper = new MyResponse(requestContext, response);

        String expectedResult = "Http response within request context: EasyMock for " + RequestContext.class;

        assertEquals(expectedResult, responseWrapper.toString());
    }
}

class MyResponse extends AbstractResponseWrapper {
    public MyResponse(RequestContext context, HttpServletResponse response) {
        super(context, response);
    }
}
