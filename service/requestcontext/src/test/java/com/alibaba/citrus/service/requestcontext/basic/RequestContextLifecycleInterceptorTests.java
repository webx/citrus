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

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.basic.impl.BasicRequestContextImpl;

public class RequestContextLifecycleInterceptorTests extends AbstractBasicResponseTests {
    private List<Integer> prepares;
    private List<Integer> commits;

    @Before
    public void init() {
        prepares = createLinkedList();
        commits = createLinkedList();
    }

    @Test
    public void prepare_commit() {
        class MyInterceptor implements RequestContextLifecycleInterceptor {
            private int index;

            public MyInterceptor(int index) {
                this.index = index;
            }

            public void prepare() {
                prepares.add(index);
            }

            public void commit() {
                commits.add(index);
            }
        }

        RequestContext wrappedRequestContext = createMock(RequestContext.class);
        HttpServletRequest wrappedRequest = createMock(HttpServletRequest.class);
        HttpServletResponse wrappedResponse = createMock(HttpServletResponse.class);
        ServletContext servletContext = createMock(ServletContext.class);

        expect(wrappedRequestContext.getRequest()).andReturn(wrappedRequest).anyTimes();
        expect(wrappedRequestContext.getResponse()).andReturn(wrappedResponse).anyTimes();
        expect(wrappedRequestContext.getServletContext()).andReturn(servletContext).anyTimes();

        replay(wrappedRequestContext);

        BasicRequestContextImpl requestContext = new BasicRequestContextImpl(wrappedRequestContext, new Object[] {
                new MyInterceptor(1), new MyInterceptor(2), new MyInterceptor(3) });

        requestContext.prepare();
        requestContext.commit();

        assertArrayEquals(new Integer[] { 1, 2, 3 }, prepares.toArray(new Integer[0]));
        assertArrayEquals(new Integer[] { 3, 2, 1 }, commits.toArray(new Integer[0]));
    }
}
