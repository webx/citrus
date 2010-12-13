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
package com.alibaba.citrus.service.requestcontext.support;

import static com.alibaba.citrus.util.Assert.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextException;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * <code>RequestContext</code>包装器的默认实现。
 * <p>
 * 这里，<code>toString()</code>方法可以列出所有级联的<code>RequestContext</code>对象。
 * </p>
 * 
 * @author Michael Zhou
 */
public abstract class AbstractRequestContextWrapper implements RequestContext {
    private final RequestContext wrappedContext;
    private final ServletContext servletContext;
    private HttpServletRequest request;
    private HttpServletResponse response;

    /**
     * 包装一个<code>RequestContext</code>对象。
     * 
     * @param wrappedContext 被包装的<code>RequestContext</code>
     */
    public AbstractRequestContextWrapper(RequestContext wrappedContext) {
        this.wrappedContext = assertNotNull(wrappedContext, "wrappedContext");
        this.servletContext = wrappedContext.getServletContext();
        this.request = wrappedContext.getRequest();
        this.response = wrappedContext.getResponse();
    }

    /**
     * 取得被包装的context。
     * 
     * @return 被包装的<code>RequestContext</code>对象
     */
    public RequestContext getWrappedRequestContext() {
        return wrappedContext;
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
     * 设置request对象。
     * 
     * @param request <code>HttpServletRequest</code>对象
     */
    protected void setRequest(HttpServletRequest request) {
        this.request = request;
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
     * 设置response对象。
     * 
     * @param response <code>HttpServletResponse</code>对象
     */
    protected void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * 开始一个请求。
     */
    public void prepare() {
    }

    /**
     * 结束一个请求。
     * 
     * @throws RequestContextException 如果失败
     */
    public void commit() throws RequestContextException {
    }

    /**
     * 显示当前的<code>RequestContext</code>以及所有级联的<code>RequestContext</code>。
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return new ToStringBuilder().append(thisToString()).start().append(getWrappedRequestContext()).end().toString();
    }

    /**
     * 显示当前<code>RequestContext</code>对象本身的信息。
     */
    protected String thisToString() {
        return super.toString();
    }
}
