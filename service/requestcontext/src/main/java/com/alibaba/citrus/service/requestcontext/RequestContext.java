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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 包含了request、response和servletContext几个对象的集合体，用来表示当前HTTP请求的状态。
 * 
 * @author Michael Zhou
 */
public interface RequestContext {
    /**
     * 取得被包装的context。
     * 
     * @return 被包装的<code>RequestContext</code>对象
     */
    RequestContext getWrappedRequestContext();

    /**
     * 取得servletContext对象。
     * 
     * @return <code>ServletContext</code>对象
     */
    ServletContext getServletContext();

    /**
     * 取得request对象。
     * 
     * @return <code>HttpServletRequest</code>对象
     */
    HttpServletRequest getRequest();

    /**
     * 取得response对象。
     * 
     * @return <code>HttpServletResponse</code>对象
     */
    HttpServletResponse getResponse();

    /**
     * 开始一个请求。
     */
    void prepare();

    /**
     * 结束一个请求。
     * 
     * @throws RequestContextException 如果失败
     */
    void commit() throws RequestContextException;
}
