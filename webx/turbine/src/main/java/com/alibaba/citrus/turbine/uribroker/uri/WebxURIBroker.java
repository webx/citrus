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
package com.alibaba.citrus.turbine.uribroker.uri;

import com.alibaba.citrus.service.uribroker.uri.ServletURIBroker;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;

/**
 * Webx风格的URI。
 * <p>
 * 一个Webx风格的URI包括如下几个部分：
 * </p>
 * 
 * <pre>
 * URI         = SERVER_INFO + PATH + "?" + QUERY_DATA + "#" + REFERENCE
 * SERVER_INFO = scheme://loginUser:loginPassword@serverName:serverPort
 * PATH        = /contextPath/servletPath/PATH_INFO
 * PATH_INFO   = /componentPath/componentPathInfo
 * QUERY_DATA  = queryKey1=value1&queryKey2=value2
 * REFERENCE   = reference
 * </pre>
 * <p>
 * 例如：
 * </p>
 * 
 * <pre>
 * http://user:pass@myserver.com:8080/mycontext/myservlet/mycomponent/content.htm
 * </pre>
 * 
 * @author Michael Zhou
 */
public abstract class WebxURIBroker extends ServletURIBroker {
    protected static final int COMPONENT_PATH_INDEX = PATH_INFO_INDEX;
    private boolean hasComponentPath;

    public String getComponentPath() {
        if (hasComponentPath) {
            return getPathSegmentAsString(COMPONENT_PATH_INDEX);
        } else {
            return null;
        }
    }

    public WebxURIBroker setComponentPath(String componentPath) {
        setPathSegment(COMPONENT_PATH_INDEX, componentPath);
        this.hasComponentPath = true;
        return this;
    }

    @Override
    protected void initDefaults(URIBroker parent) {
        super.initDefaults(parent);

        if (parent instanceof WebxURIBroker) {
            WebxURIBroker parentWebx = (WebxURIBroker) parent;

            if (!hasComponentPath) {
                hasComponentPath = parentWebx.hasComponentPath;
                setPathSegment(COMPONENT_PATH_INDEX, parentWebx.getPathSegment(COMPONENT_PATH_INDEX));
            }
        }
    }

    @Override
    protected void copyFrom(URIBroker parent) {
        super.copyFrom(parent);

        if (parent instanceof WebxURIBroker) {
            WebxURIBroker parentWebx = (WebxURIBroker) parent;

            hasComponentPath = parentWebx.hasComponentPath;
            setPathSegment(COMPONENT_PATH_INDEX, parentWebx.getPathSegment(COMPONENT_PATH_INDEX));
        }
    }
}
