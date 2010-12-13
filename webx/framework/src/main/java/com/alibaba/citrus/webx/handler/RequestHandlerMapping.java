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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 将请求映射到适当的request handler。
 * 
 * @author Michael Zhou
 */
public interface RequestHandlerMapping {
    /**
     * 取得所有可用的request handler的名称。
     */
    String[] getRequestHandlerNames();

    /**
     * 取得request对应的handler及相关信息。
     * <p>
     * 如果返回<code>null</code>代表无对应的<code>RequestHandler</code>。
     * </p>
     */
    RequestHandlerContext getRequestHandler(HttpServletRequest request, HttpServletResponse response);
}
