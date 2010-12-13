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
package com.alibaba.citrus.service.jsp.impl;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 避免在JSP中修改content type、locale和charset。作为模板系统，JSP不应该控制content type和输出字符集。
 * 
 * @author Michael Zhou
 */
public class JspResponse extends HttpServletResponseWrapper {
    public JspResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setContentType(String contentType) {
        // do nothing
    }

    @Override
    public void setLocale(Locale locale) {
        // do nothing
    }

    @Override
    public void setCharacterEncoding(String charset) {
        // do nothing
    }
}
