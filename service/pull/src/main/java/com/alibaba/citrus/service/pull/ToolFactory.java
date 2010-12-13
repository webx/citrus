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
 * 创建pull tool的工厂。
 * <p>
 * 工厂本身必须是singleton，以确保性能。
 * </p>
 * <ul>
 * <li>当<code>isSingleton() == true</code> 时，<code>createTool()</code>
 * 方法会在系统初始化时被调用（Pre-pulling）。</li>
 * <li>当<code>isSingleton() == false</code> 时，每一次请求，<code>creteTool()</code>
 * 方法至多被调用一次。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public interface ToolFactory {
    /**
     * Factory所创建的tool是不是singleton？
     */
    boolean isSingleton();

    /**
     * 取得tool实例。
     * <p>
     * 对于非singleton类型，该方法在每次请求时，至多被调用一次。
     * </p>
     * <p>
     * 如返回<code>null</code>，则表示该tool不可用。
     * </p>
     * <p>
     * 注意：对于非singleton类型，<strong>必须</strong>每次返回不同的对象。
     * </p>
     */
    Object createTool() throws Exception;
}
