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

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextWrapper;

public class RequestContextWrapperTests {
    private RequestContext context;

    @Before
    public void init() {
        context = createMock(RequestContext.class);

        expect(context.getServletContext()).andReturn(createMock(ServletContext.class));
        expect(context.getRequest()).andReturn(createMock(HttpServletRequest.class));
        expect(context.getResponse()).andReturn(createMock(HttpServletResponse.class));

        replay(context);
    }

    @Test
    public void _toString() {
        MyContext context1 = new MyContext(context);
        MyContext context2 = new MyContext(context1);
        MyContext context3 = new MyContext(context2);
        MyContext context4 = new MyContext(context3);

        String expectedResult = "";

        expectedResult += "MyContext[4] {\n";
        expectedResult += "  MyContext[3] {\n";
        expectedResult += "    MyContext[2] {\n";
        expectedResult += "      MyContext[1] {\n";
        expectedResult += "        " + context + "\n";
        expectedResult += "      }\n";
        expectedResult += "    }\n";
        expectedResult += "  }\n";
        expectedResult += "}";

        assertEquals(expectedResult, context4.toString());

        verify(context);
    }
}

class MyContext extends AbstractRequestContextWrapper {
    private static int globalCount = 0;
    private final int count;

    public MyContext(RequestContext wrappedContext) {
        super(wrappedContext);
        count = ++globalCount;
    }

    @Override
    public HttpServletRequest getRequest() {
        return null;
    }

    @Override
    public HttpServletResponse getResponse() {
        return null;
    }

    @Override
    public String thisToString() {
        return "MyContext[" + count + "]";
    }
}
