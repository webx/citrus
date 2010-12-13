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
package com.alibaba.citrus.webx.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.webx.util.SetLoggingContextHelper;

/**
 * 通过SLF4J MDC来记录用户和请求的信息。
 * <p>
 * 建议在log4j的配置文件中，设置如下pattern layout：
 * </p>
 * 
 * <pre>
 * &lt;layout class="org.apache.log4j.PatternLayout"&gt;
 *     &lt;param name="ConversionPattern" value="%-4r [%d{yyyy-MM-dd HH:mm:ss}] - %X{remoteAddr} %X{requestURI} %X{referrer} %X{userAgent} %X{cookie.名称} - %m%n" /&gt;
 * &lt;/layout&gt;
 * </pre>
 * <p>
 * 下面是logback版本：
 * </p>
 * 
 * <pre>
 * &lt;layout class="ch.qos.logback.classic.PatternLayout"&gt;
 *     &lt;pattern&gt;%-4r [%d{yyyy-MM-dd HH:mm:ss}] - %X{remoteAddr} %X{requestURI} %X{referrer} %X{userAgent} %X{cookie.名称} - %m%n&lt;/pattern&gt;
 * &lt;/layout&gt;
 * </pre>
 * 
 * @see com.alibaba.citrus.webx.util.SetLoggingContextHelper
 * @author Michael Zhou
 */
public class SetLoggingContextFilter extends FilterBean {
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        SetLoggingContextHelper helper = new SetLoggingContextHelper(request);

        try {
            helper.setLoggingContext();

            chain.doFilter(request, response);
        } finally {
            helper.clearLoggingContext();
        }
    }
}
