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
package com.alibaba.citrus.service.requestcontext.rundata;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.meterware.httpunit.HttpUnitUtils;

/**
 * 测试<code>RunData</code>。
 */
public class RunDataTests extends AbstractRequestContextsTests<RunData> {
    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-rundata.xml");
    }

    @Override
    protected String getDefaultBeanName() {
        return "rundata";
    }

    @Before
    public void init() throws Exception {
        invokeReadFileServlet("form.html");
        initRequestContext();
    }

    @Test
    public void missingRequestContext() throws Exception {
        invokeNoopServlet("/servlet/test.htm");
        initRequestContext("rundata_missing_other_request_contexts");

        try {
            requestContext.isBuffering();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Could not find BufferedRequestContext in request context chain"));
        }

        try {
            requestContext.getRedirectLocation();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Could not find LazyCommitRequestContext in request context chain"));
        }

        try {
            requestContext.getContentType();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Could not find SetLocaleRequestContext in request context chain"));
        }

        try {
            requestContext.getParameters();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Could not find ParserRequestContext in request context chain"));
        }
    }

    @Test
    public void getRequestURL() throws Exception {
        // 无参数
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertEquals("http://www.taobao.com/servlet", requestContext.getRequestURL());
        assertEquals("http://www.taobao.com/servlet", requestContext.getRequestURL(true));
        assertEquals("http://www.taobao.com/servlet", requestContext.getRequestURL(false));

        // 有参数
        invokeNoopServlet("/servlet?aaa=111");
        initRequestContext();

        assertEquals("http://www.taobao.com/servlet?aaa=111", requestContext.getRequestURL());
        assertEquals("http://www.taobao.com/servlet?aaa=111", requestContext.getRequestURL(true));
        assertEquals("http://www.taobao.com/servlet", requestContext.getRequestURL(false));
    }

    @Test
    public void urlEncodeForm() throws Exception {
        invokeReadFileServlet("form2.html");
        initRequestContext();

        assertEquals("hello", requestContext.getParameters().getString("myparam"));
        assertEquals("hello", requestContext.getParameters().getStrings("myparam")[0]);
        assertEquals("中华人民共和国", requestContext.getParameters().getStrings("myparam")[1]);
    }

    @Test
    public void multipartForm() throws Exception {
        assertEquals("hello", requestContext.getParameters().getString("myparam"));

        FileItem fileItem = requestContext.getParameters().getFileItem("myfile");

        assertEquals("myfile", fileItem.getFieldName());
        assertEquals(new File(srcdir, "smallfile.txt"), new File(fileItem.getName()));
        assertFalse(fileItem.isFormField());
        assertEquals(new String("中华人民共和国".getBytes("GBK"), "8859_1"), fileItem.getString());
        assertEquals("中华人民共和国", fileItem.getString("GBK"));
        assertTrue(fileItem.isInMemory());
    }

    @Test
    public void cookies() throws Exception {
        assertEquals("mycookievalue", requestContext.getCookies().getString("mycookie"));

        requestContext.getCookies().setCookie("hello", "baobao");

        commitToClient();

        assertEquals("baobao", clientResponse.getNewCookieValue("hello"));
    }

    @Test
    public void contentTypeAndCharset() throws Exception {
        requestContext.setCharacterEncoding(null);

        requestContext.setContentType("text/plain");
        assertEquals("text/plain", requestContext.getContentType());
        assertEquals(HttpUnitUtils.DEFAULT_CHARACTER_SET, requestContext.getCharacterEncoding());

        requestContext.setCharacterEncoding("GBK");
        assertEquals("text/plain; charset=GBK", requestContext.getContentType());
        assertEquals("GBK", requestContext.getCharacterEncoding());

        requestContext.setCharacterEncoding(null);
        assertEquals("text/plain", requestContext.getContentType());
    }

    @Test
    public void charsetAndContentType() throws Exception {
        requestContext.setCharacterEncoding(null);

        // 在没设置content type之前，charset立即生效（servlet 2.4）
        requestContext.setCharacterEncoding("GBK");
        assertEquals(null, requestContext.getContentType());
        assertEquals("GBK", requestContext.getCharacterEncoding());

        requestContext.setContentType("text/plain");
        assertEquals("text/plain; charset=GBK", requestContext.getContentType());
        assertEquals("GBK", requestContext.getCharacterEncoding());

        requestContext.setCharacterEncoding(null);
        assertEquals("text/plain", requestContext.getContentType());
    }

    @Test
    public void contentType() throws Exception {
        requestContext.setContentType("image/gif; charset=GBK", false);
        assertEquals("image/gif", requestContext.getContentType());
        assertEquals(HttpUnitUtils.DEFAULT_CHARACTER_SET, requestContext.getCharacterEncoding());

        requestContext.setCharacterEncoding("GBK");
        assertEquals("image/gif; charset=GBK", requestContext.getContentType());
        assertEquals("GBK", requestContext.getCharacterEncoding());

        requestContext.setContentType("image/gif; charset=GBK", false);
        assertEquals("image/gif", requestContext.getContentType());
    }

    @Test
    public void sendRedirect() throws Exception {
        assertNull(requestContext.getRedirectLocation());

        requestContext.setRedirectLocation("/aaa/bbb");
        assertEquals("/aaa/bbb", requestContext.getRedirectLocation());
    }

    @Test
    public void statusCode() throws Exception {
        assertEquals(0, requestContext.getStatusCode());

        requestContext.setStatusCode(302);
        assertEquals(302, requestContext.getStatusCode());
    }
}
