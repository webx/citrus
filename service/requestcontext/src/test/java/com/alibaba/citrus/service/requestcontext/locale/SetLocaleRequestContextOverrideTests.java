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

package com.alibaba.citrus.service.requestcontext.locale;

import static org.junit.Assert.*;

import com.alibaba.citrus.util.i18n.LocaleUtil;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

public class SetLocaleRequestContextOverrideTests extends AbstractSetLocaleRequestContextTests {
    @Override
    protected String getDefaultBeanName() {
        return "setLocale_override";
    }

    @Test
    public void changeInputCharset() throws Exception {
        invokeNoopServlet("/servlet?_input_charset=8859_1");
        initRequestContext();

        assertEquals("8859_1", request.getCharacterEncoding());
        assertEquals("GBK", response.getCharacterEncoding());

        assertEquals("zh_CN:GBK", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());
    }

    @Test
    public void changeInputCharset_override_json() throws Exception {
        invokeNoopServlet("/servlet.json");
        initRequestContext();

        assertEquals("UTF-8", request.getCharacterEncoding());
        assertEquals("UTF-8", response.getCharacterEncoding());

        assertEquals("zh_CN:UTF-8", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());

        // 指定input charset参数，更优先
        invokeNoopServlet("/servlet.json?_input_charset=GBK");
        initRequestContext();

        assertEquals("GBK", request.getCharacterEncoding());
        assertEquals("UTF-8", response.getCharacterEncoding());

        assertEquals("zh_CN:UTF-8", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());

        // 指定output charset参数，更优先
        invokeNoopServlet("/servlet.json?_output_charset=GBK");
        initRequestContext();

        assertEquals("UTF-8", request.getCharacterEncoding());
        assertEquals("GBK", response.getCharacterEncoding());

        assertEquals("zh_CN:GBK", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());
    }

    @Test
    public void changeInputCharset_override_js() throws Exception {
        invokeNoopServlet("/servlet.js");
        initRequestContext();

        assertEquals("ISO-8859-1", request.getCharacterEncoding());
        assertEquals("ISO-8859-1", response.getCharacterEncoding());

        assertEquals("zh_CN:ISO-8859-1", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());

        // 指定input charset参数，更优先
        invokeNoopServlet("/servlet.js?_input_charset=GBK");
        initRequestContext();

        assertEquals("GBK", request.getCharacterEncoding());
        assertEquals("ISO-8859-1", response.getCharacterEncoding());

        assertEquals("zh_CN:ISO-8859-1", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());

        // 指定output charset参数，更优先
        invokeNoopServlet("/servlet.js?_output_charset=GBK");
        initRequestContext();

        assertEquals("ISO-8859-1", request.getCharacterEncoding());
        assertEquals("GBK", response.getCharacterEncoding());

        assertEquals("zh_CN:GBK", LocaleUtil.getContext().toString());
        assertEquals("zh_CN", LocaleContextHolder.getLocale().toString());
    }
}
