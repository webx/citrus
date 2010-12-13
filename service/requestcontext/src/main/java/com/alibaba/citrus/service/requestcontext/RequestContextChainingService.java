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
 * 将一个个<code>RequestContext</code>对象串联起来的service。
 * <p>
 * 通过它可以实现多重包装的HTTP request和response。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface RequestContextChainingService {
    /**
     * 取得所有的request context的信息。
     */
    RequestContextInfo<?>[] getRequestContextInfos();

    /**
     * 取得<code>RequestContext</code>串。
     * 
     * @param servletContext <code>ServletContext</code>对象
     * @param request <code>HttpServletRequest</code>对象
     * @param response <code>HttpServletResponse</code>对象
     * @return request context
     */
    RequestContext getRequestContext(ServletContext servletContext, HttpServletRequest request,
                                     HttpServletResponse response);

    /**
     * 由外到内地调用<code>requestContext.commit()</code>方法。
     * 
     * @param requestContext 要初始化的request context
     * @throws RequestContextException 如果失败
     */
    void commitRequestContext(RequestContext requestContext);
}
