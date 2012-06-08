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

package com.alibaba.citrus.util.templatelite;

/**
 * 如果visitor实现了这个接口，那么当访问visitor方法出错时，接口将被调用，以处理异常。否则，<code>Template</code>
 * 将抛出异常。
 *
 * @author Michael Zhou
 */
public interface VisitorInvocationErrorHandler {
    void handleInvocationError(String desc, Throwable e) throws Exception;
}
