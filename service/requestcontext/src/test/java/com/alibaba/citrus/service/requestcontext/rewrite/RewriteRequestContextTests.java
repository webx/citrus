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
package com.alibaba.citrus.service.requestcontext.rewrite;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.service.requestcontext.rewrite.impl.RewriteRule;

public class RewriteRequestContextTests extends AbstractRequestContextsTests<RewriteRequestContext> {
    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-rewrite.xml");
    }

    protected final void initRequest(String uri) throws Exception {
        initRequest(uri, null, null, -1);
    }

    protected final void initRequest(String uri, String beanName, String serverName, int serverPort) throws Exception {
        client.getClientProperties().setAutoRedirect(false);

        invokeNoopServlet(uri);

        if (serverName != null) {
            ((MyHttpRequest) request).setServerName(serverName);
        }

        if (serverPort > 0) {
            ((MyHttpRequest) request).setServerPort(serverPort);
        }

        initRequestContext(beanName);
    }

    @Test
    public void missingParserRequestContext() throws Exception {
        try {
            initRequest("/servlet/test.htm", "rewrite_missing_parser", null, -1);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("ParserRequestContext"));
        }
    }

    @Test
    public void emptyRules() throws Exception {
        initRequest("/servlet/test.htm", "rewrite_empty", null, -1);

        // 无规则，无变化
        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test.htm", newRequest.getPathInfo());

        assertEquals("/servlet/test.htm", newRequest.getRequestURI());
        assertEquals("http://www.taobao.com/servlet/test.htm", newRequest.getRequestURL().toString());

    }

    @Test
    public void test1_redirect_301() throws Exception {
        initRequest("/servlet/test1/hello.htm");

        commitToClient();

        assertEquals("http://www.taobao.com/servlet/test1/hello.htm", clientResponse.getURL().toString());
        assertEquals(301, clientResponse.getResponseCode());
        assertEquals("http://www.taobao.com/servlet/test1/new_hello.htm", clientResponse.getHeaderField("location"));
    }

    @Test
    public void test2_redirect_default() throws Exception {
        initRequest("/servlet/test2/hello.htm");

        commitToClient();

        assertEquals("http://www.taobao.com/servlet/test2/hello.htm", clientResponse.getURL().toString());
        assertEquals(302, clientResponse.getResponseCode());
        assertEquals("http://www.taobao.com/servlet/test2/new_hello.htm", clientResponse.getHeaderField("location"));
    }

    @Test
    public void test2_redirect_default_change_port() throws Exception {
        initRequest("/servlet/test2/hello.htm?a=1", null, null, 8080);

        commitToClient();

        // 由于httpunit并不知道端口的修改，所以这里还是80端口
        assertEquals("http://www.taobao.com/servlet/test2/hello.htm?a=1", clientResponse.getURL().toString());
        assertEquals(302, clientResponse.getResponseCode());
        assertEquals("http://www.taobao.com:8080/servlet/test2/new_hello.htm",
                clientResponse.getHeaderField("location"));
    }

    @Test
    public void test2_4_redirect_default_qsa() throws Exception {
        initRequest("/servlet/test2.4/hello.htm?a=1");

        commitToClient();

        assertEquals("http://www.taobao.com/servlet/test2.4/hello.htm?a=1", clientResponse.getURL().toString());
        assertEquals(302, clientResponse.getResponseCode());
        assertEquals("http://www.taobao.com/servlet/test2.4/new_hello.htm?a=1",
                clientResponse.getHeaderField("location"));
    }

    @Test
    public void test2_5_redirect_absolute() throws Exception {
        initRequest("/servlet/test2.5/hello.htm");

        commitToClient();

        assertEquals("http://www.taobao.com/servlet/test2.5/hello.htm", clientResponse.getURL().toString());
        assertEquals(302, clientResponse.getResponseCode());
        assertEquals("http://www.microsoft.com/test2.5/new_hello.htm", clientResponse.getHeaderField("location"));
    }

    @Test
    public void test3_internal_redirect_prefix_mapping() throws Exception {
        // Servlet为前缀映射：/servlet/*
        initRequest("/servlet/test3/hello.htm");

        // 重写以后，保持原有的servletPath。
        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test3/new_hello.htm", newRequest.getPathInfo());

        // Request URI代表的是HTTP请求的信息，故不变。
        assertEquals("/servlet/test3/hello.htm", newRequest.getRequestURI());

        // Request URL也不变。
        assertEquals("http://www.taobao.com/servlet/test3/hello.htm", newRequest.getRequestURL().toString());
    }

    @Test
    public void test3_5_internal_redirect_prefix_mapping() throws Exception {
        // Servlet为前缀映射：/servlet/*
        initRequest("/servlet/test3.5/hello.htm");

        // 重写以后，servletPath被改变。
        assertEquals("", newRequest.getServletPath());
        assertEquals("/new_servlet/test3.5/new_hello.htm", newRequest.getPathInfo());

        // Request URI代表的是HTTP请求的信息，故不变。
        assertEquals("/servlet/test3.5/hello.htm", newRequest.getRequestURI());

        // Request URL也不变。
        assertEquals("http://www.taobao.com/servlet/test3.5/hello.htm", newRequest.getRequestURL().toString());
    }

    @Test
    public void test3_internal_redirect_suffix_mapping() throws Exception {
        // Servlet为后缀映射：*.do
        initRequest("/test3/hello.do");

        assertEquals("/test3/new_hello.do", newRequest.getServletPath());
        assertEquals(null, newRequest.getPathInfo());

        // Request URI代表的是HTTP请求的信息，故不变。
        assertEquals("/test3/hello.do", newRequest.getRequestURI());

        // Request URL也不变。
        assertEquals("http://www.taobao.com/test3/hello.do", newRequest.getRequestURL().toString());
    }

    @Test
    public void test4_no_changes() throws Exception {
        initRequest("/servlet/test4/hello.htm");

        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test4/hello.htm", newRequest.getPathInfo());

        assertEquals("/servlet/test4/hello.htm", newRequest.getRequestURI());
        assertEquals("http://www.taobao.com/servlet/test4/hello.htm", newRequest.getRequestURL().toString());
    }

    @Test
    public void test5_clear_parameters() throws Exception {
        initRequest("/servlet/test5/hello.htm?x=1&y=2&z=3");

        // 路径不变
        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test5/hello.htm", newRequest.getPathInfo());

        // 参数被清除
        assertNull(newRequest.getParameter("x"));
        assertNull(newRequest.getParameter("y"));
        assertNull(newRequest.getParameter("z"));

        assertArrayEquals(new String[] { "htm" }, newRequest.getParameterValues("ext"));
        assertArrayEquals(new String[] { "taobao" }, newRequest.getParameterValues("host"));
        assertArrayEquals(new String[] { "1", "2", "3" }, newRequest.getParameterValues("count"));
    }

    @Test
    public void test6_reserve_parameters() throws Exception {
        initRequest("/servlet/test6/hello.htm?x=1&y=2&z=3");

        // 路径不变
        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test6/hello.htm", newRequest.getPathInfo());

        // 参数被保留
        assertNull(newRequest.getParameter("x"));
        assertArrayEquals(new String[] { "2" }, newRequest.getParameterValues("y"));
        assertArrayEquals(new String[] { "3" }, newRequest.getParameterValues("z"));

        assertArrayEquals(new String[] { "htm" }, newRequest.getParameterValues("ext"));
        assertArrayEquals(new String[] { "taobao" }, newRequest.getParameterValues("host"));
        assertArrayEquals(new String[] { "1", "2", "3" }, newRequest.getParameterValues("count"));
    }

    @Test
    public void test7_chaining() throws Exception {
        // htm后缀被通过
        initRequest("/servlet/test7/hello.htm");

        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test7/new_hello.htm", newRequest.getPathInfo());

        // do后缀被中止
        initRequest("/servlet/test7/hello.do");

        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test7/hello.do", newRequest.getPathInfo());
    }

    @Test
    public void test8_handlers_normalizeURL() throws Exception {
        initRequest("/servlet/test8/HelloWorld/INDEX.htm");

        // 路径变成小写加下划线
        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test_8/hello_world/new_index.htm", newRequest.getPathInfo());
    }

    @Test
    public void test9_negative_patterns() throws Exception {
        // path =~ !/test9/skip  &&  x =~ !1  &&  y =~ 2
        initRequest("/servlet/test9/hello.htm?x=2&y=2");

        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test9/new_hello.htm", newRequest.getPathInfo());
    }

    @Test
    public void test9_negative_patterns_rule_not_match() throws Exception {
        // path =~ !/test9/skip
        initRequest("/servlet/test9/skip.htm");

        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test9/skip.htm", newRequest.getPathInfo());
    }

    @Test
    public void test9_negative_patterns_condition_not_match() throws Exception {
        // path =~ !/test9/skip  &&  x =~ !1  &&  y =~ 2
        initRequest("/servlet/test9/hello.htm?x=1&y=2");

        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test9/hello.htm", newRequest.getPathInfo());
    }

    @Test
    public void test10_ornext_conditions() throws Exception {
        // x =~ 1  &&  x =~ 2
        initRequest("/servlet/test10/hello.htm?x=1");

        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test10/new_hello.htm", newRequest.getPathInfo());

        // x =~ 1  &&  x =~ 2
        initRequest("/servlet/test10/hello.htm?x=2");

        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test10/new_hello.htm", newRequest.getPathInfo());
    }

    @Test
    public void test10_ornext_conditions_not_match() throws Exception {
        // x =~ 1  &&  x =~ 2
        initRequest("/servlet/test10/hello.htm?x=3");

        assertEquals("/servlet", newRequest.getServletPath());
        assertEquals("/test10/hello.htm", newRequest.getPathInfo());
    }

    @Test
    public void _toString() throws Exception {
        initRequest("/servlet/test10/hello.htm");

        RewriteRule[] rules = getFieldValue(requestContext, "rules", RewriteRule[].class);

        for (RewriteRule rule : rules) {
            System.out.println("------------------------");
            System.out.println(rule);
        }
    }
}
