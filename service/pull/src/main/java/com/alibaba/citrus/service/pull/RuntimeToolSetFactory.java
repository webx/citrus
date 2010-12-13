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
package com.alibaba.citrus.service.pull;

/**
 * 创建一组pull tools的工厂。
 * <p>
 * 工厂本身必须是singleton，以确保性能。但是和<code>ToolSetFactory</code>不同的是，
 * <code>RuntimeToolSetFactory.getToolNames()</code>
 * 方法不是在系统初始化时被调用的，而是在每个请求中至多被调用一次。
 * </p>
 * <p>
 * 该类型的对象的性能不如<code>ToolSetFactory</code>，请尽量使用后者。
 * </p>
 * 
 * @see ToolFactory
 * @see ToolSetFactory
 * @author Michael Zhou
 */
public interface RuntimeToolSetFactory {
    /**
     * 取得toolset实例。
     * <p>
     * 该方法在每次请求时，至多被调用一次。
     * </p>
     * <p>
     * 如返回<code>null</code>，则表示该tool不可用。
     * </p>
     * <p>
     * 注意：每次调用<strong>必须</strong>返回不同的对象。
     * </p>
     */
    Object createToolSet() throws Exception;

    /**
     * 取得tools的名称。
     * <p>
     * 在每个请求中，该方法都会被调用至多一次。
     * </p>
     */
    Iterable<String> getToolNames(Object toolSet);

    /**
     * 取得指定名称的tool实例。
     * <p>
     * 对于非singleton类型，该方法在每次请求时，每个<code>name</code>至多被调用一次。
     * </p>
     * <p>
     * 如返回<code>null</code>，则表示该tool不可用。
     * </p>
     */
    Object createTool(Object toolSet, String name) throws Exception;
}
