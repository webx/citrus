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
package com.alibaba.citrus.service.requestcontext.impl;

import static com.alibaba.citrus.util.Assert.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 实现了<code>RequestContext</code>接口，包含request、response和servletContext的信息。
 * 
 * @author Michael Zhou
 */
public class SimpleRequestContext implements RequestContext {
    private final ServletContext servletContext;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    /**
     * 创建一个新的<code>RequestContext</code>对象。
     * 
     * @param servletContext 当前请求所在的<code>ServletContext</code>
     * @param request <code>HttpServletRequest</code>对象
     * @param response <code>HttpServletResponse</code>对象
     */
    public SimpleRequestContext(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
        this.servletContext = assertNotNull(servletContext, "servletContext");
        this.request = assertNotNull(request, "request");
        this.response = assertNotNull(response, "response");
    }

    /**
     * 取得被包装的context。
     * 
     * @return 被包装的<code>RequestContext</code>对象
     */
    public RequestContext getWrappedRequestContext() {
        return null;
    }

    /**
     * 取得servletContext对象。
     * 
     * @return <code>ServletContext</code>对象
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * 取得request对象。
     * 
     * @return <code>HttpServletRequest</code>对象
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * 取得response对象。
     * 
     * @return <code>HttpServletResponse</code>对象
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * 开始一个请求。
     */
    public void prepare() {
    }

    /**
     * 结束一个请求。
     */
    public void commit() {
    }

    /**
     * 显示当前<code>RequestContext</code>的内容。
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("request", getRequest());
        mb.append("response", getResponse());
        mb.append("webapp", getServletContext());

        return new ToStringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }
}
