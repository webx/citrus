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

import javax.servlet.http.HttpSession;

public interface SessionLifecycleListener extends SessionInterceptor {
    /**
     * 当session第一次被创建以后，被调用。
     */
    void sessionCreated(HttpSession session);

    /**
     * 当session被作废后以后，被调用。
     */
    void sessionInvalidated(HttpSession session);

    /**
     * 当session被访问的时候，被调用。
     */
    void sessionVisited(HttpSession session);
}
