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
package com.alibaba.citrus.service.uribroker.uri;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.util.ServletUtil;

/**
 * Servlet风格的URI。
 * <p>
 * 一个Servlet风格的URI包括如下几个部分：
 * </p>
 * 
 * <pre>
 * URI         = SERVER_INFO + PATH + "?" + QUERY_DATA + "#" + REFERENCE
 * SERVER_INFO = scheme://loginUser:loginPassword@serverName:serverPort
 * PATH        = /contextPath/servletPath/PATH_INFO
 * QUERY_DATA  = queryKey1=value1&queryKey2=value2
 * REFERENCE   = reference
 * </pre>
 * <p>
 * 例如：
 * </p>
 * 
 * <pre>
 * http://user:pass@myserver.com:8080/mycontext/myservlet/view?id=1#top
 * </pre>
 * <p>
 * 注意，<code>ServletURIBroker</code>没有提供修改pathInfo的方法。如果要添加、删除、修改path，请直接使用子类
 * <code>GenericServletURIBroker</code>。
 * </p>
 * 
 * @author Michael Zhou
 * @author dux.fangl
 */
public abstract class ServletURIBroker extends WebAppURIBroker {
    protected static final int SERVLET_PATH_INDEX = CONTEXT_PATH_INDEX + 1;
    protected static final int PATH_INFO_INDEX = SERVLET_PATH_INDEX + 1;
    private boolean hasServletPath;

    /**
     * 将request中的运行时信息填充到uri broker中。
     */
    @Override
    protected void populateWithRequest(HttpServletRequest request) {
        boolean savedHasContextPath = hasContextPath; // 该值可能被populateWithRequest()改变
        super.populateWithRequest(request);

        // 必须设置了contextPath，servletPath才会有意义。
        if (!savedHasContextPath && !hasServletPath) {
            // 只有前缀匹配时，才设置servletPath。例如前缀匹配：/myservlet/*。
            // 对于后缀匹配，例如*.htm，设置servletPath没有意义。
            if (ServletUtil.isPrefixServletMapping(request)) {
                setServletPath(request.getServletPath());
            }
        }
    }

    /**
     * 取得servlet path。
     */
    public String getServletPath() {
        if (hasServletPath) {
            return getPathSegmentAsString(SERVLET_PATH_INDEX);
        } else {
            return null;
        }
    }

    /**
     * 设置servlet path。
     */
    public ServletURIBroker setServletPath(String servletPath) {
        setPathSegment(SERVLET_PATH_INDEX, servletPath);
        hasServletPath = true;
        return this;
    }

    /**
     * 取得script名, 就是contextPath加servletName.
     */
    public String getScriptName() {
        if (hasContextPath) {
            return getContextPath() + getServletPath();
        } else {
            return getServletPath();
        }
    }

    /**
     * 取得path info。
     */
    public String getPathInfo() {
        return getAllPathSegmentsAsString(PATH_INFO_INDEX);
    }

    /**
     * 取得一组path info。
     */
    public List<String> getPathInfoElements() {
        return getAllPathSegments(PATH_INFO_INDEX);
    }

    @Override
    protected void initDefaults(URIBroker parent) {
        super.initDefaults(parent);

        if (parent instanceof ServletURIBroker) {
            ServletURIBroker parentServlet = (ServletURIBroker) parent;

            if (!hasServletPath) {
                hasServletPath = parentServlet.hasServletPath;
                setPathSegment(SERVLET_PATH_INDEX, parentServlet.getPathSegment(SERVLET_PATH_INDEX));
            }
        }
    }

    @Override
    protected void copyFrom(URIBroker parent) {
        super.copyFrom(parent);

        if (parent instanceof ServletURIBroker) {
            ServletURIBroker parentServlet = (ServletURIBroker) parent;

            hasServletPath = parentServlet.hasServletPath;
            setPathSegment(SERVLET_PATH_INDEX, parentServlet.getPathSegment(SERVLET_PATH_INDEX));
        }
    }
}
