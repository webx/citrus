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
package com.alibaba.citrus.webx.handler;

import static com.alibaba.citrus.util.ServletUtil.*;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.alibaba.citrus.util.internal.webpagelite.ServletRequestContext;
import com.alibaba.citrus.webx.ResourceNotFoundException;

/**
 * 这个类提供给<code>RequestHandler</code>相关的上下文信息，使<code>RequestHandler</code>和
 * <code>RequestProcessor</code>整合起来。
 * 
 * @author Michael Zhou
 */
public abstract class RequestHandlerContext extends ServletRequestContext {
    private final String internalBaseURL;

    /**
     * 创建context。
     */
    public RequestHandlerContext(HttpServletRequest request, HttpServletResponse response,
                                 ServletContext servletContext, String internalBaseURL, String baseURL,
                                 String resourceName) {
        super(request, response, servletContext, baseURL, resourceName);
        this.internalBaseURL = internalBaseURL + '/';
    }

    /**
     * 取得当前调用的<code>RequestHandler</code>对象。
     */
    public abstract RequestHandler getRequestHandler();

    /**
     * 取得logger。
     */
    public abstract Logger getLogger();

    /**
     * 当请求的资源找不到时，方法被调用。子类可以修改此行为。
     */
    @Override
    public void resourceNotFound(String resourceName) throws IOException {
        throw new ResourceNotFoundException("Resource Not Found: " + resourceName);
    }

    /**
     * 取得相对于internal目录的URL。
     */

    public final String getInternalResourceURL(String resourceName) {
        return normalizeURI(internalBaseURL + resourceName);
    }
}
