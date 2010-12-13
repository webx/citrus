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
 * 代表一个支持资源列表操作的loader。
 * 
 * @author Michael Zhou
 */
public interface ResourceLister extends ResourceLoader {
    /**
     * 查找指定资源的子目录或文件名。目录名以<code>/</code>结尾。
     * <p>
     * 如被列表的资源根本不存在，则返回<code>null</code>。
     * </p>
     */
    String[] list(ResourceListerContext context, Set<ResourceLoadingOption> options);
}
