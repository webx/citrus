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
 * PATH_INFO   = /pathInfo
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
 * 
 * @author Michael Zhou
 */
public class GenericServletURIBroker extends ServletURIBroker {
    /**
     * 设置一组path info。
     */
    public void setPathInfoElements(List<String> path) {
        clearPathSegment(PATH_INFO_INDEX);

        if (path != null) {
            for (String element : path) {
                addPathSegment(PATH_INFO_INDEX, element);
            }
        }
    }

    /**
     * 添加path info。
     */
    public ServletURIBroker addPathInfo(String path) {
        addPathSegment(PATH_INFO_INDEX, path);
        return this;
    }

    /**
     * 清除所有path info。
     */
    public ServletURIBroker clearPathInfo() {
        clearPathSegment(PATH_INFO_INDEX);
        return this;
    }

    @Override
    protected URIBroker newInstance() {
        return new GenericServletURIBroker();
    }

    @Override
    protected void initDefaults(URIBroker parent) {
        super.initDefaults(parent);

        if (parent instanceof GenericServletURIBroker) {
            GenericServletURIBroker parentServlet = (GenericServletURIBroker) parent;

            if (!parentServlet.getPathInfoElements().isEmpty()) {
                setPathSegment(PATH_INFO_INDEX, parentServlet.getPathInfoElements(), getPathInfoElements());
            }
        }
    }

    @Override
    protected void copyFrom(URIBroker parent) {
        super.copyFrom(parent);

        if (parent instanceof GenericServletURIBroker) {
            GenericServletURIBroker parentServlet = (GenericServletURIBroker) parent;
            setPathSegment(PATH_INFO_INDEX, parentServlet.getPathInfoElements());
        }
    }

    @Override
    protected int getPathSegmentCount() {
        return 3;
    }
}
