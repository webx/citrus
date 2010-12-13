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
package com.alibaba.citrus.webx.handler;

/**
 * 用来处理request请求的handler。
 * <p>
 * 这是接口是被框架内部使用的，用来显示诸如错误页面等相关页面、图片的。应用程序通常不需要扩展此接口。
 * </p>
 * <p>
 * 线程安全性：<code>RequestHandler</code>对象可被多个request共享，所以必须注意线程安全问题。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface RequestHandler {
    /**
     * 处理请求。
     */
    void handleRequest(RequestHandlerContext context) throws Exception;
}
