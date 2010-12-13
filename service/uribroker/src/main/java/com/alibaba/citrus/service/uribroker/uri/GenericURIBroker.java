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

import static java.util.Collections.*;

import java.net.URL;
import java.util.List;

/**
 * 代表一个基本的URI风格。
 * <p>
 * 一个URI包括如下几个部分：
 * </p>
 * 
 * <pre>
 * URI         = SERVER_INFO + PATH + &quot;?&quot; + QUERY_DATA + &quot;#&quot; + REFERENCE
 * SERVER_INFO = scheme://loginUser:loginPassword@serverName:serverPort
 * PATH        = /path/path
 * QUERY_DATA  = queryKey1=value1&amp;queryKey2=value2
 * REFERENCE   = reference
 * </pre>
 * <p>
 * 例如：
 * </p>
 * 
 * <pre>
 * http://user:pass@myserver.com:8080/view?id=1#top
 * </pre>
 * 
 * @author Michael Zhou
 */
public class GenericURIBroker extends URIBroker {
    @Override
    protected void setServerURI(URL uri) {
        setPathElements(singletonList(uri.getPath()));
    }

    /**
     * 设置一组path。
     */
    public void setPathElements(List<String> path) {
        clearPathSegment(PATH_INDEX);

        if (path != null) {
            for (String element : path) {
                addPathSegment(PATH_INDEX, element);
            }
        }
    }

    /**
     * 添加path。
     */
    public GenericURIBroker addPath(String path) {
        addPathSegment(PATH_INDEX, path);
        return this;
    }

    /**
     * 清除所有path。
     */
    public GenericURIBroker clearPath() {
        clearPathSegment(PATH_INDEX);
        return this;
    }

    @Override
    protected URIBroker newInstance() {
        return new GenericURIBroker();
    }

    @Override
    protected void initDefaults(URIBroker parent) {
        super.initDefaults(parent);

        if (!parent.getPathElements().isEmpty()) {
            setPathSegment(PATH_INDEX, parent.getPathElements(), getPathElements());
        }
    }

    @Override
    protected void copyFrom(URIBroker parent) {
        super.copyFrom(parent);

        clearPath();

        if (parent instanceof GenericURIBroker) {
            setPathSegment(PATH_INDEX, parent.getPathElements());
        }
    }

    /**
     * 取得当前URI path分成几段。
     */
    @Override
    protected int getPathSegmentCount() {
        return 1;
    }
}
