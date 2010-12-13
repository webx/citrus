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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.service.requestcontext.basic.impl.ResponseHeaderSecurityFilter;

public class BasicRequestContextTests extends AbstractRequestContextsTests<BasicRequestContext> {
    private Object[] interceptors;

    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-basic.xml");
    }

    @Before
    public void init() throws Exception {
        invokeReadFileServlet("form.html");
    }

    @Test
    public void noInterceptors() throws Exception {
        initRequestContext("basic_empty");
        interceptors = requestContext.getResponseHeaderInterceptors();

        assertEquals(1, interceptors.length);
        assertThat(interceptors[0], instanceOf(ResponseHeaderSecurityFilter.class));

        assertHeader("test", "value1\r\nvalue2", "value1 value2");
    }

    @Test
    public void noInterceptors_2() throws Exception {
        initRequestContext("basic_empty2");
        interceptors = requestContext.getResponseHeaderInterceptors();

        assertEquals(1, interceptors.length);
        assertThat(interceptors[0], instanceOf(ResponseHeaderSecurityFilter.class));

        assertHeader("test", "value1\r\nvalue2", "value1 value2");
    }

    @Test
    public void withCrlf() throws Exception {
        initRequestContext("basic_withCrlf");
        interceptors = requestContext.getResponseHeaderInterceptors();

        assertEquals(2, interceptors.length);
        assertThat(interceptors[0], instanceOf(ResponseHeaderSecurityFilter.class));
        assertThat(interceptors[1], instanceOf(MyInterceptor.class));

        ResponseHeaderSecurityFilter responseHeaderSecurityFilter = (ResponseHeaderSecurityFilter) interceptors[0];
        assertEquals(5 * 1024, responseHeaderSecurityFilter.getMaxSetCookieSize().getValue()); // 5K

        assertHeader("test", "value1\r\nvalue2", "hello, value1 value2");
    }

    @Test
    public void noCrlf() throws Exception {
        initRequestContext("basic_noCrlf");
        interceptors = requestContext.getResponseHeaderInterceptors();

        assertEquals(2, interceptors.length);
        assertThat(interceptors[0], instanceOf(MyInterceptor.class));
        assertThat(interceptors[1], instanceOf(ResponseHeaderSecurityFilter.class)); // appended

        assertHeader("test", "value1\r\nvalue2", "hello, value1 value2");
    }

    private void assertHeader(String name, String value, String resultValue) throws Exception {
        newResponse.setHeader(name, value);
        requestContexts.commitRequestContext(requestContext);
        commitToClient();
        assertEquals(resultValue, clientResponse.getHeaderField(name));
    }

    public static class MyInterceptor implements HeaderValueInterceptor {
        public String checkHeaderValue(String name, String value) throws ResponseHeaderRejectedException {
            return "hello, " + value;
        }
    }
}
