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

package com.alibaba.citrus.async;

/**
 * 在async异步时被调用。
 *
 * @author Michael Zhou
 */
public interface AsyncCallback {
    /** 设置async超时时间，超过这个时间，任务就会被cancel。 */
    long getTimeout();

    /** 设置cancel任务的超时时间。当任务超时后，会被cancel掉。cancel以后，仍然会给出一段时间，让线程进行处理。超过这个时间，请求就会被强制结束，<code>asyncContext.complete()</code>会被调用。 */
    long getCancelingTimeout();
}
