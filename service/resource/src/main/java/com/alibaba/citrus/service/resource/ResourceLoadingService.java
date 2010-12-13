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

import static java.util.Collections.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

/**
 * 装载资源的service，可以取得指定名称的资源的URL、文件（File）或输入流（InputStream）。
 * <p>
 * 需要注意的是，不是所有类型的资源都可以同时取得URL、File和InputStream的。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface ResourceLoadingService {
    /**
     * 常用选项：<code>FOR_CREATE</code>。
     */
    Set<ResourceLoadingOption> FOR_CREATE = unmodifiableSet(EnumSet.of(ResourceLoadingOption.FOR_CREATE));

    /**
     * 取得parent装载服务。
     */
    ResourceLoadingService getParent();

    /**
     * 查找指定名称的资源。
     */
    URL getResourceAsURL(String resourceName) throws ResourceNotFoundException;

    /**
     * 查找指定名称的资源。
     */
    File getResourceAsFile(String resourceName) throws ResourceNotFoundException;

    /**
     * 查找指定名称的资源。
     */
    File getResourceAsFile(String resourceName, Set<ResourceLoadingOption> options) throws ResourceNotFoundException;

    /**
     * 查找指定名称的资源。
     */
    InputStream getResourceAsStream(String resourceName) throws ResourceNotFoundException, IOException;

    /**
     * 查找指定名称的资源。
     */
    Resource getResource(String resourceName) throws ResourceNotFoundException;

    /**
     * 查找指定名称的资源。
     */
    Resource getResource(String resourceName, Set<ResourceLoadingOption> options) throws ResourceNotFoundException;

    /**
     * 判断指定名称的资源是否存在。如果存在，则返回<code>true</code>。
     */
    boolean exists(String resourceName);

    /**
     * 跟踪并获取搜索资源的路径。
     * <p>
     * 即使资源无法找到，<code>trace</code>方法也会返回它所尝试过的搜索路径，而不是抛出异常。
     * </p>
     * <p>
     * 该方法主要用于调试和测试服务。
     * </p>
     */
    ResourceTrace trace(String resourceName);

    /**
     * 跟踪并获取搜索资源的路径。
     * <p>
     * 即使资源无法找到，<code>trace</code>方法也会返回它所尝试过的搜索路径，而不是抛出异常。
     * </p>
     * <p>
     * 该方法主要用于调试和测试服务。
     * </p>
     */
    ResourceTrace trace(String resourceName, Set<ResourceLoadingOption> options);

    /**
     * 罗列出指定资源的子目录或文件名。目录名以<code>/</code>结尾。如果目录不存在，则返回<code>null</code>。
     */
    String[] list(String resourceName) throws ResourceNotFoundException;

    /**
     * 罗列出指定资源的子目录或文件名。目录名以<code>/</code>结尾。如果目录不存在，则返回<code>null</code>。
     */
    String[] list(String resourceName, Set<ResourceLoadingOption> options) throws ResourceNotFoundException;

    /**
     * 罗列出指定资源的子目录或文件资源。如果目录不存在，则返回<code>null</code>。
     */
    Resource[] listResources(String resourceName) throws ResourceNotFoundException;

    /**
     * 罗列出指定资源的子目录或文件资源。如果目录不存在，则返回<code>null</code>。
     */
    Resource[] listResources(String resourceName, Set<ResourceLoadingOption> options) throws ResourceNotFoundException;

    /**
     * 取得所有的patterns名称。
     */
    String[] getPatterns(boolean includeParent);
}
