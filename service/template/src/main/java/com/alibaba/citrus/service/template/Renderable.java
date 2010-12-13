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
package com.alibaba.citrus.service.template;

/**
 * 代表一个“可被渲染”的对象，用来代替通常所用的<code>toString</code>来渲染对象的方法。实现此接口有如下好处：
 * <ol>
 * <li>在<code>toString</code>中难以处理异常，而该接口提供的方法支持异常处理。</li>
 * <li>通过<code>toString</code>增加调试代码的难度。</li>
 * <li>使用<code>toString</code>难以利用多个步骤来初始化对象，而该接口则更方便。</li>
 * </ol>
 * <p>
 * 对于特定的template engine，需要通过特定的方法来支持此接口。例如，Velocity可通过event cartrige来处理
 * <code>Renderable</code>接口。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface Renderable {
    /**
     * 渲染对象。
     */
    String render();
}
