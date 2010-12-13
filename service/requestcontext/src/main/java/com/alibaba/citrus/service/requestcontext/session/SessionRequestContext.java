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
package com.alibaba.citrus.service.requestcontext.session;

import com.alibaba.citrus.service.requestcontext.RequestContext;

/**
 * 支持session的<code>RequestContext</code>实现。
 * 
 * @author Michael Zhou
 */
public interface SessionRequestContext extends RequestContext {
    /**
     * 取得<code>SessionConfig</code>实例。
     * 
     * @return <code>SessionConfig</code>实例
     */
    SessionConfig getSessionConfig();

    /**
     * 判断session是否已经作废。
     * 
     * @return 如已作废，则返回<code>true</code>
     */
    boolean isSessionInvalidated();

    /**
     * 清除session。类似<code>invalidate()</code>方法，但支持后续操作，而不会抛出
     * <code>IllegalStateException</code>。
     */
    void clear();
}
