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

import javax.servlet.http.HttpServletRequest;

/**
 * 支持webapp环境的<code>URIBroker</code>基类。
 * 
 * @author Michael Zhou
 */
public abstract class WebAppURIBroker extends URIBroker {
    protected static final int CONTEXT_PATH_INDEX = PATH_INDEX;
    protected boolean hasContextPath;

    /**
     * 将request中的运行时信息填充到uri broker中。
     */
    @Override
    protected void populateWithRequest(HttpServletRequest request) {
        super.populateWithRequest(request);

        if (!hasContextPath) {
            setContextPath(request.getContextPath());
        }
    }

    /**
     * 取得context path。
     */
    public String getContextPath() {
        if (hasContextPath) {
            return getPathSegmentAsString(CONTEXT_PATH_INDEX);
        } else {
            return null;
        }
    }

    /**
     * 设置context path。
     */
    public WebAppURIBroker setContextPath(String contextPath) {
        setPathSegment(CONTEXT_PATH_INDEX, contextPath);
        hasContextPath = true;
        return this;
    }

    @Override
    protected void initDefaults(URIBroker parent) {
        super.initDefaults(parent);

        if (parent instanceof WebAppURIBroker) {
            WebAppURIBroker parentWebapp = (WebAppURIBroker) parent;

            if (!hasContextPath) {
                hasContextPath = parentWebapp.hasContextPath;
                setPathSegment(CONTEXT_PATH_INDEX, parentWebapp.getPathSegment(CONTEXT_PATH_INDEX));
            }
        }
    }

    @Override
    protected void copyFrom(URIBroker parent) {
        super.copyFrom(parent);

        if (parent instanceof WebAppURIBroker) {
            WebAppURIBroker parentWebapp = (WebAppURIBroker) parent;

            hasContextPath = parentWebapp.hasContextPath;
            setPathSegment(CONTEXT_PATH_INDEX, parentWebapp.getPathSegment(CONTEXT_PATH_INDEX));
        }
    }
}
