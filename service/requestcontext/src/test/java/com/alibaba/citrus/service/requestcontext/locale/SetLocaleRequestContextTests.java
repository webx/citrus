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
package com.alibaba.citrus.service.requestcontext.locale;

import static com.alibaba.citrus.service.requestcontext.locale.SetLocaleRequestContext.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.util.i18n.LocaleUtil;
import com.meterware.httpunit.HttpUnitUtils;

/**
 * 测试<code>SetLocaleRequestContext</code>。
 * 
 * @author Michael Zhou
 */
public class SetLocaleRequestContextTests extends AbstractRequestContextsTests<SetLocaleRequestContext> {
    private boolean useSession;

    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-locale.xml");
    }

    @Before
    public void init() throws Exception {
        invokeReadFileServlet("form.html");
        initRequestContext();
    }

    @After
    public void dispose() {
        // 确保session没有启动
        if (!useSession) {
            assertFalse("sessionCreated", ((MyHttpRequest) request).isSessionCreated());
        }
    }

    @Test
    public void setContentTypeThenSetCharset() throws Exception {
        // 默认为utf8
        newResponse.setContentType("text/plain");
        assertEquals("text/plain; charset=" + CHARSET_DEFAULT, requestContext.getResponseContentType());
        assertEquals("UTF-8", newResponse.getCharacterEncoding());

        requestContext.setResponseCharacterEncoding("GBK");
        assertEquals("text/plain; charset=GBK", requestContext.getResponseContentType());
        assertEquals("GBK", newResponse.getCharacterEncoding());

        requestContext.setResponseCharacterEncoding(null);
        assertEquals("text/plain", requestContext.getResponseContentType());
    }

    @Test
    public void setCharsetThenSetContentType() throws Exception {
        // 在没设置content type之前，charset立即生效（servlet 2.4）
        requestContext.setResponseCharacterEncoding("GBK");
        assertEquals(null, requestContext.getResponseContentType());
        assertEquals("GBK", newResponse.getCharacterEncoding());

        newResponse.setContentType("text/plain");
        assertEquals("text/plain; charset=GBK", requestContext.getResponseContentType());
        assertEquals("GBK", newResponse.getCharacterEncoding());

        requestContext.setResponseCharacterEncoding(null);
        assertEquals("text/plain", requestContext.getResponseContentType());
    }

    @Test
    public void setContentTypeWithoutCharset() throws Exception {
        requestContext.setResponseContentType("image/gif; charset=GBK", false);
        assertEquals("image/gif", requestContext.getResponseContentType());
        assertEquals(HttpUnitUtils.DEFAULT_CHARACTER_SET, newResponse.getCharacterEncoding());

        requestContext.setResponseCharacterEncoding("GBK");
        assertEquals("image/gif; charset=GBK", requestContext.getResponseContentType());
        assertEquals("GBK", newResponse.getCharacterEncoding());

        requestContext.setResponseContentType("image/gif; charset=GBK", false);
        assertEquals("image/gif", requestContext.getResponseContentType());
    }

    @Test
    public void getDefaultLocale() throws Exception {
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertEquals("UTF-8", request.getCharacterEncoding());
        assertEquals("UTF-8", response.getCharacterEncoding());

        assertEquals("zh_CN:UTF-8", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());
    }

    @Test
    public void changeInputCharset() throws Exception {
        invokeNoopServlet("/servlet?_input_charset=GBK");
        initRequestContext();

        assertEquals("GBK", request.getCharacterEncoding());
        assertEquals("UTF-8", response.getCharacterEncoding());

        assertEquals("zh_CN:UTF-8", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());
    }

    @Test
    public void changeOutputCharset() throws Exception {
        invokeNoopServlet("/servlet?_output_charset=GBK");
        initRequestContext();

        assertEquals("UTF-8", request.getCharacterEncoding());
        assertEquals("GBK", response.getCharacterEncoding());

        assertEquals("zh_CN:GBK", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());
    }

    @Test
    public void switchLocaleAndCharset() throws Exception {
        useSession = true;

        // zh_CN:UTF-8 -> zh_HK:Big5
        invokeNoopServlet("/servlet?_lang=zh_HK:Big5");
        initRequestContext();

        assertEquals("UTF-8", request.getCharacterEncoding()); // 输入charset仍按切换前的为准
        assertEquals("Big5", response.getCharacterEncoding());

        assertEquals("zh_HK:Big5", LocaleUtil.getContext().toString());
        assertEquals("zh_HK", LocaleContextHolder.getLocale().toString());

        commitToClient(); // commit response to client

        // 正常访问：zh_HK:Big5被记在session里
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertEquals("Big5", request.getCharacterEncoding());
        assertEquals("Big5", response.getCharacterEncoding());

        assertEquals("zh_HK:Big5", LocaleUtil.getContext().toString());
        assertEquals("zh_HK", LocaleContextHolder.getLocale().toString());

        commitToClient(); // commit response to client

        // 恢复默认值：zh_HK:Big5 -> zh_CN:UTF-8
        invokeNoopServlet("/servlet?_lang=default");
        initRequestContext();

        assertEquals("Big5", request.getCharacterEncoding());
        assertEquals("UTF-8", response.getCharacterEncoding());

        assertEquals("zh_CN:UTF-8", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());

        commitToClient(); // commit response to client

        // 正常访问：zh_CN:UTF-8
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertEquals("UTF-8", request.getCharacterEncoding());
        assertEquals("UTF-8", response.getCharacterEncoding());

        assertEquals("zh_CN:UTF-8", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());

        commitToClient(); // commit response to client

        assertNull(client.getSession(false).getAttribute("_lang"));
    }
}
