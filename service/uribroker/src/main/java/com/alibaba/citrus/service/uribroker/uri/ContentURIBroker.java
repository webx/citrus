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

/**
 * 通用的content URI，用于显示一般的WEB资源（如图片等）。
 * <p>
 * 一个Content URI包括如下几个部分：
 * </p>
 * 
 * <pre>
 * URI         = SERVER_INFO + PATH + "?" + QUERY_DATA + "#" + REFERENCE
 * SERVER_INFO = scheme://loginUser:loginPassword@serverName:serverPort
 * PATH        = /prefixPath/contentPath
 * QUERY_DATA  = queryKey1=value1&queryKey2=value2
 * REFERENCE   = reference
 * </pre>
 * <p>
 * 例如：
 * </p>
 * 
 * <pre>
 * http://user:pass@myserver.com:8080/view.html?id=1#top
 * </pre>
 * 
 * @author Michael Zhou
 * @author dux.fangl
 */
public class ContentURIBroker extends URIBroker {
    protected static final int PREFIX_PATH_INDEX = PATH_INDEX;
    protected static final int CONTENT_PATH_INDEX = PATH_INDEX + 1;
    private boolean hasPrefixPath;
    private boolean hasContentPath;

    /**
     * 取得prefix path。
     */
    public String getPrefixPath() {
        if (hasPrefixPath) {
            return getPathSegmentAsString(PREFIX_PATH_INDEX);
        } else {
            return null;
        }
    }

    /**
     * 设置prefix path。
     */
    public ContentURIBroker setPrefixPath(String prefixPath) {
        setPathSegment(PREFIX_PATH_INDEX, prefixPath);
        hasPrefixPath = true;
        return this;
    }

    /**
     * 取得content path。
     */
    public String getContentPath() {
        if (hasContentPath) {
            return getPathSegmentAsString(CONTENT_PATH_INDEX);
        } else {
            return null;
        }
    }

    /**
     * 设置content path。
     */
    public ContentURIBroker setContentPath(String contentPath) {
        setPathSegment(CONTENT_PATH_INDEX, contentPath);
        hasContentPath = true;
        return this;
    }

    /**
     * 设置content path。
     */
    public ContentURIBroker getURI(String contentPath) {
        return setContentPath(contentPath);
    }

    @Override
    protected URIBroker newInstance() {
        return new ContentURIBroker();
    }

    @Override
    protected void initDefaults(URIBroker parent) {
        super.initDefaults(parent);

        if (parent instanceof ContentURIBroker) {
            ContentURIBroker parentContent = (ContentURIBroker) parent;

            if (!hasPrefixPath) {
                hasPrefixPath = parentContent.hasPrefixPath;
                setPathSegment(PREFIX_PATH_INDEX, parentContent.getPathSegment(PREFIX_PATH_INDEX));
            }

            if (!hasContentPath) {
                hasContentPath = parentContent.hasContentPath;
                setPathSegment(CONTENT_PATH_INDEX, parentContent.getPathSegment(CONTENT_PATH_INDEX));
            }
        }
    }

    @Override
    protected void copyFrom(URIBroker parent) {
        super.copyFrom(parent);

        if (parent instanceof ContentURIBroker) {
            ContentURIBroker parentContent = (ContentURIBroker) parent;

            hasPrefixPath = parentContent.hasPrefixPath;
            setPathSegment(PREFIX_PATH_INDEX, parentContent.getPathSegment(PREFIX_PATH_INDEX));

            hasContentPath = parentContent.hasContentPath;
            setPathSegment(CONTENT_PATH_INDEX, parentContent.getPathSegment(CONTENT_PATH_INDEX));
        }
    }

    @Override
    protected int getPathSegmentCount() {
        return 2;
    }
}
