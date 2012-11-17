/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.service.requestcontext.impl;

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;

/**
 * 保存commit的状态，用来防止重复commit。
 * 也作为synchronized监视器，避免多线程同时提交。
 *
 * @author Michael Zhou
 */
public class CommitMonitor implements HeaderCommitter {
    private final RequestContextChainingService service;
    private       RequestContext                topRequestContext;
    private       boolean                       headersCommitted;
    private       boolean                       committed;

    public CommitMonitor(RequestContextChainingService service) {
        this.service = assertNotNull(service, "service");
    }

    public void setTopRequestContext(RequestContext topRequestContext) {
        this.topRequestContext = topRequestContext;
    }

    public boolean isHeadersCommitted() {
        return headersCommitted || committed;
    }

    public void setHeadersCommitted(boolean headersCommitted) {
        this.headersCommitted = headersCommitted;
    }

    public boolean isCommitted() {
        return committed;
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    /** 实现内部接口：<code>HeaderCommitter</code>。 */
    public void commitHeaders() {
        if (isHeadersCommitted() || topRequestContext == null) {
            return;
        }

        service.commitHeaders(topRequestContext);
    }
}
