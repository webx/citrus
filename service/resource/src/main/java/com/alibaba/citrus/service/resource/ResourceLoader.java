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
 * 代表一个装载资源的loader。
 * 
 * @author Michael Zhou
 */
public interface ResourceLoader {
    /**
     * 初始化loader，并设定loader所在的<code>ResourceLoadingService</code>的实例。
     * <p>
     * 注意，此处只能保存<code>ResourceLoadingService</code>，但不能调用它，因为还没初始化完。否则将抛出
     * <code>IllegalStateException</code>。
     * </p>
     */
    void init(ResourceLoadingService resourceLoadingService);

    /**
     * 查找指定名称的资源，如果未找到，则返回<code>null</code>。
     */
    Resource getResource(ResourceLoaderContext context, Set<ResourceLoadingOption> options);
}
