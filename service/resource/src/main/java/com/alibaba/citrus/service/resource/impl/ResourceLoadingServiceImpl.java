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
package com.alibaba.citrus.service.resource.impl;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.resource.Resource;
import com.alibaba.citrus.service.resource.ResourceLoadingOption;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.ResourceNotFoundException;
import com.alibaba.citrus.service.resource.ResourceTrace;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 实现<code>ResourceLoadingService</code>。
 * 
 * @author Michael Zhou
 */
public class ResourceLoadingServiceImpl extends AbstractService<ResourceLoadingService> implements
        ResourceLoadingService, ApplicationContextAware {
    public final static String DEFAULT_NAME = "resourceLoadingService";
    private final static Resource[] EMPTY_RESOURCE_ARRAY = new Resource[0];

    private ApplicationContext factory;
    private ResourceLoadingService parent;
    private ResourceMapping[] resourceMappings;
    private ResourceFilterMapping[] filterMappings;
    private String configLocation;

    public void setApplicationContext(ApplicationContext factory) throws BeansException {
        this.factory = factory;
    }

    public ResourceLoadingService getParent() {
        return parent;
    }

    public void setParent(ResourceLoadingService parent) {
        this.parent = parent;
    }

    public void setResourceMappings(ResourceMapping[] mappings) {
        this.resourceMappings = mappings;
    }

    public void setFilterMappings(ResourceFilterMapping[] filterMappings) {
        this.filterMappings = filterMappings;
    }

    public void setConfigLocation(URL configLocation) {
        this.configLocation = configLocation.toExternalForm();
    }

    @Override
    protected void init() {
        assertNotNull(factory, "beanFactory");

        if (parent == null && factory.getParent() != null) {
            String parentBeanName = null;

            if (factory.getParent().containsBean(getBeanName())) {
                parentBeanName = getBeanName();
            } else if (factory.getParent().containsBean(DEFAULT_NAME)) {
                parentBeanName = DEFAULT_NAME;
            }

            if (parentBeanName != null) {
                parent = (ResourceLoadingService) factory.getParent().getBean(parentBeanName);
            }
        }

        if (resourceMappings == null) {
            resourceMappings = new ResourceMapping[0];
        }

        for (ResourceMapping mapping : resourceMappings) {
            mapping.init(this);
        }

        if (filterMappings == null) {
            filterMappings = new ResourceFilterMapping[0];
        }

        for (ResourceFilterMapping mapping : filterMappings) {
            mapping.init(this);
        }
    }

    /**
     * 查找指定名称的资源。
     */
    public URL getResourceAsURL(String resourceName) throws ResourceNotFoundException {
        return checkResource(getResource(resourceName).getURL(), resourceName, URL.class);
    }

    /**
     * 查找指定名称的资源。
     */
    public File getResourceAsFile(String resourceName) throws ResourceNotFoundException {
        return checkResource(getResource(resourceName).getFile(), resourceName, File.class);
    }

    /**
     * 查找指定名称的资源。
     */
    public File getResourceAsFile(String resourceName, Set<ResourceLoadingOption> options)
            throws ResourceNotFoundException {
        return checkResource(getResource(resourceName, options).getFile(), resourceName, File.class);
    }

    /**
     * 查找指定名称的资源。
     */
    public InputStream getResourceAsStream(String resourceName) throws ResourceNotFoundException, IOException {
        return checkResource(getResource(resourceName).getInputStream(), resourceName, InputStream.class);
    }

    /**
     * 判断非空。
     */
    private <T> T checkResource(T resource, String resourceName, Class<T> type) throws ResourceNotFoundException {
        if (resource == null) {
            throw new ResourceNotFoundException(String.format("Could not get %s of resource \"%s\"",
                    type.getSimpleName(), resourceName));
        }

        return resource;
    }

    /**
     * 查找指定名称的资源。
     */
    public Resource getResource(String resourceName) throws ResourceNotFoundException {
        return getResource(resourceName, null);
    }

    /**
     * 查找指定名称的资源。
     */
    public Resource getResource(String resourceName, Set<ResourceLoadingOption> options)
            throws ResourceNotFoundException {
        assertInitialized();
        return new ResourceLoaderContextImpl(resourceName, options, filterMappings, resourceMappings, getParent(),
                getLogger(), configLocation, getBeanName()).getResource();
    }

    /**
     * 判断指定名称的资源是否存在。如果存在，则返回<code>true</code>。
     */
    public boolean exists(String resourceName) {
        try {
            getResource(resourceName);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    /**
     * 跟踪并获取搜索资源的路径。
     */
    public ResourceTrace trace(String resourceName) {
        return trace(resourceName, FOR_CREATE);
    }

    /**
     * 跟踪并获取搜索资源的路径。
     */
    public ResourceTrace trace(String resourceName, Set<ResourceLoadingOption> options) {
        assertInitialized();
        return new ResourceLoaderContextImpl(resourceName, options, filterMappings, resourceMappings, getParent(),
                getLogger(), configLocation, getBeanName()).getTrace();
    }

    /**
     * 罗列出指定资源的子目录或文件名。如果不存在，则返回空数组。
     */
    public String[] list(String resourceName) throws ResourceNotFoundException {
        return list(resourceName, null);
    }

    /**
     * 罗列出指定资源的子目录或文件名。如果不存在，则返回空数组。
     */
    public String[] list(String resourceName, Set<ResourceLoadingOption> options) throws ResourceNotFoundException {
        assertInitialized();
        return new ResourceListerContextImpl(resourceName, options, resourceMappings, getParent(), getLogger()).list();
    }

    /**
     * 罗列出指定资源的子目录或文件资源。如果不存在，则返回空数组。
     */
    public Resource[] listResources(String resourceName) throws ResourceNotFoundException {
        return listResources(resourceName, null);
    }

    /**
     * 罗列出指定资源的子目录或文件资源。如果不存在，则返回空数组。
     */
    public Resource[] listResources(String resourceName, Set<ResourceLoadingOption> options)
            throws ResourceNotFoundException {
        String[] names = list(resourceName, options);

        if (isEmptyArray(names)) {
            return EMPTY_RESOURCE_ARRAY;
        }

        List<Resource> resources = createArrayList(names.length);

        for (String name : names) {
            String subResourceName = resourceName + "/" + name;

            try {
                resources.add(getResource(subResourceName));
            } catch (ResourceNotFoundException e) {
                // ignore
            }
        }

        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * 取得所有的patterns名称。
     */
    public String[] getPatterns(boolean includeParent) {
        Set<String> patterns = createLinkedHashSet();

        for (ResourceMapping mapping : resourceMappings) {
            if (!mapping.isInternal()) {
                patterns.add(mapping.getPatternName());
            }
        }

        if (includeParent && parent != null) {
            for (String pattern : parent.getPatterns(true)) {
                patterns.add(pattern);
            }
        }

        return patterns.toArray(new String[patterns.size()]);
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("configLocation", configLocation);
        mb.append("resourceMappings", resourceMappings);
        mb.append("filterMappings", filterMappings);

        if (parent != null) {
            mb.append("parent", parent);
        }

        return new ToStringBuilder().append(getBeanDescription()).append(mb).toString();
    }
}
