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
package com.alibaba.citrus.springext;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * 扩展Spring原有的<code>ResourceLoader</code>的功能。
 * 
 * @author Michael Zhou
 */
public interface ResourceLoadingExtender {
    /**
     * 取得指定路径名称所代表的资源对象。
     * <p>
     * 如果返回<code>null</code>表示使用原来的装载机制来取得资源。
     * </p>
     */
    Resource getResourceByPath(String path);

    /**
     * 取得用来解析resource pattern的解析器。
     * <p>
     * 如果返回<code>null</code>表示使用原来的装载机制。
     * </p>
     */
    ResourcePatternResolver getResourcePatternResolver();
}
