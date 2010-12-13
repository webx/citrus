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
package com.alibaba.citrus.service.requestcontext.lazycommit;

import com.alibaba.citrus.service.requestcontext.RequestContext;

/**
 * 延迟提交response。有些方法的调用会导致response被提交，包括：
 * <ul>
 * <li><code>sendError</code></li>
 * <li><code>sendRedirect</code></li>
 * <li><code>flushBuffer</code></li>
 * <li><code>setContentLength()</code>或
 * <code>setHeader("Content-Length", len)</code>，但有些servlet
 * engine不会在这里提交response。</li>
 * </ul>
 * Response一旦提交，就不能修改header了。这对于一些应用（例如cookie-based session）的实现是一个问题。
 * <p>
 * 本类使用延迟提交来支持这些应用。
 * </p>
 * <p>
 * 注意，本类并未处理<code>getWriter()</code>和<code>getOutputStream()</code>
 * 方法所产生的提交。对于这些方法所产生的提交，需要用<code>BufferedRequestContext</code>来处理。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface LazyCommitRequestContext extends RequestContext {
    /**
     * 判断当前请求是否已出错。
     * 
     * @return 如果出错，则返回<code>true</code>
     */
    boolean isError();

    /**
     * 如果<code>sendError()</code>方法曾被调用，则该方法返回一个error状态值。
     * 
     * @return error状态值，若系统正常，则返回<code>0</code>
     */
    int getErrorStatus();

    /**
     * 如果<code>sendError()</code>方法曾被调用，则该方法返回一个error信息。
     * 
     * @return error信息，若系统正常，则返回<code>null</code>
     */
    String getErrorMessage();

    /**
     * 判断当前请求是否已被重定向。
     * 
     * @return 如果重定向，则返回<code>true</code>
     */
    boolean isRedirected();

    /**
     * 取得重定向的URI。
     * 
     * @return 重定向的URI，如果没有重定向，则返回<code>null</code>
     */
    String getRedirectLocation();

    /**
     * 取得最近设置的HTTP status。
     * 
     * @return HTTP status值
     */
    int getStatus();
}
