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
package com.alibaba.citrus.service.resource;

import java.util.Set;

/**
 * 代表一个<code>ResourceLoader</code>调用的上下文信息。
 * 
 * @author Michael Zhou
 */
public interface ResourceLoaderContext extends ResourceMatchResult {
    /**
     * 取得资源。和<code>ResourceLoadingService.getResource()</code>的逻辑不同，该方法是被
     * <code>ResourceLoader</code>内部使用的：
     * <ul>
     * <li>将除去重复的匹配，避免无限循环。</li>
     * <li>不会调用filter。</li>
     * </ul>
     */
    Resource getResource(String newResourceName, Set<ResourceLoadingOption> options);
}
