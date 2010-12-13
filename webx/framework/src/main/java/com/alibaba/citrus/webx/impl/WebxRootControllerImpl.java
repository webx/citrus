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
package com.alibaba.citrus.webx.impl;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.util.ServletUtil;
import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.support.AbstractWebxRootController;
import com.alibaba.citrus.webx.util.WebxUtil;

/**
 * 对<code>WebxRootController</code>的默认实现。
 * 
 * @author Michael Zhou
 */
public class WebxRootControllerImpl extends AbstractWebxRootController {
    @Override
    protected boolean handleRequest(RequestContext requestContext) throws Exception {
        HttpServletRequest request = requestContext.getRequest();

        // Servlet mapping有两种匹配方式：前缀匹配和后缀匹配。
        // 对于前缀匹配，例如：/servlet/aaa/bbb，servlet path为/servlet，path info为/aaa/bbb
        // 对于前缀匹配，当mapping pattern为/*时，/aaa/bbb，servlet path为""，path info为/aaa/bbb
        // 对于后缀匹配，例如：/aaa/bbb.html，servlet path为/aaa/bbb.html，path info为null
        //
        // 对于前缀匹配，取其pathInfo；对于后缀匹配，取其servletPath。
        String path = ServletUtil.getResourcePath(request);

        // 再根据path查找component
        WebxComponent component = getComponents().findMatchedComponent(path);
        boolean served = false;

        if (component != null) {
            try {
                WebxUtil.setCurrentComponent(request, component);
                served = component.getWebxController().service(requestContext);
            } finally {
                WebxUtil.setCurrentComponent(request, null);
            }
        }

        return served;
    }
}
