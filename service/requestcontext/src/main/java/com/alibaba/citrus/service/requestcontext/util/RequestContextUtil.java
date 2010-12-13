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
package com.alibaba.citrus.service.requestcontext.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.citrus.service.requestcontext.RequestContext;

/**
 * 和<code>RequestContext</code>相关的辅助类。
 * 
 * @author Michael Zhou
 */
public class RequestContextUtil {
    private static final String REQUEST_CONTEXT_KEY = "_outer_webx3_request_context_";

    /**
     * 取得和当前request相关联的<code>RequestContext</code>对象。
     * 
     * @param request 要检查的request
     * @return <code>RequestContext</code>对象，如果没找到，则返回<code>null</code>
     */
    public static RequestContext getRequestContext(HttpServletRequest request) {
        return (RequestContext) request.getAttribute(REQUEST_CONTEXT_KEY);
    }

    /**
     * 将<code>RequestContext</code>对象和request相关联。
     * 
     * @param requestContext <code>RequestContext</code>对象
     */
    public static void setRequestContext(RequestContext requestContext) {
        HttpServletRequest request = requestContext.getRequest();

        request.setAttribute(REQUEST_CONTEXT_KEY, requestContext);
    }

    /**
     * 将<code>RequestContext</code>对象和request脱离关联。
     */
    public static void removeRequestContext(HttpServletRequest request) {
        request.removeAttribute(REQUEST_CONTEXT_KEY);
    }

    /**
     * 在指定的request context及其级联的request context中找到一个指定类型的request context。
     * 
     * @param request 从该<code>HttpServletRequest</code>中取得request context
     * @param requestContextInterface 要查找的类
     * @return <code>RequestContext</code>对象，如果没找到，则返回<code>null</code>
     */
    public static <R extends RequestContext> R findRequestContext(HttpServletRequest request,
                                                                  Class<R> requestContextInterface) {
        return findRequestContext(getRequestContext(request), requestContextInterface);
    }

    /**
     * 在指定的request context及其级联的request context中找到一个指定类型的request context。
     * 
     * @param requestContext 要搜索的request context
     * @param requestContextInterface 要查找的类
     * @return <code>RequestContext</code>对象，如果没找到，则返回<code>null</code>
     */
    public static <R extends RequestContext> R findRequestContext(RequestContext requestContext,
                                                                  Class<R> requestContextInterface) {
        do {
            if (requestContextInterface.isInstance(requestContext)) {
                break;
            }

            requestContext = requestContext.getWrappedRequestContext();
        } while (requestContext != null);

        return requestContextInterface.cast(requestContext);
    }

    /**
     * 注册spring <code>ServletRequestAttributes</code>中的析构回调方法，这些方法将在request
     * context被提交之后依次调用。
     */
    public static void registerRequestDestructionCallback(String name, Runnable callback) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        attributes.registerDestructionCallback(name, callback, RequestAttributes.SCOPE_REQUEST);
    }
}
