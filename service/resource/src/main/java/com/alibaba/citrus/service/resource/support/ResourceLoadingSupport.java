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
package com.alibaba.citrus.service.resource.support;

import static com.alibaba.citrus.service.resource.ResourceLoadingService.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.ResourceNotFoundException;
import com.alibaba.citrus.springext.ResourceLoadingExtender;

/**
 * 将<code>ResourceLoadingService</code>整合到Spring <code>ApplicationContext</code>
 * 中。
 * 
 * @author Michael Zhou
 */
public class ResourceLoadingSupport implements ResourceLoadingExtender, ApplicationListener {
    private final static Logger log = LoggerFactory.getLogger(ResourceLoadingSupport.class);
    private final ApplicationContext factory;
    private final String resourceLoadingServiceName;
    private final ResourcePatternResolver resolver;
    private ResourceLoadingService resourceLoadingService;
    private boolean contextRefreshed = false;
    private boolean complained = false;

    /**
     * 创建<code>ResourceLoadingSupport</code>，并指定
     * <code>ResourceLoadingService</code>所在的bean factory。
     */
    public ResourceLoadingSupport(ApplicationContext factory) {
        this(factory, null);
    }

    /**
     * 创建<code>ResourceLoadingSupport</code>，并指定
     * <code>ResourceLoadingService</code>所在的bean factory，以及
     * <code>ResourceLoadingService</code>的名称。
     */
    public ResourceLoadingSupport(ApplicationContext factory, String resourceLoadingServiceName) {
        this.factory = assertNotNull(factory, "beanFactory");
        this.resourceLoadingServiceName = defaultIfNull(trimToNull(resourceLoadingServiceName),
                "resourceLoadingService");
        this.resolver = new ResourceLoadingServicePatternResolver();
    }

    /**
     * 当applicatioon context被refresh后，调用此方法初始化。
     */
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            contextRefreshed = true;
            resourceLoadingService = getResourceLoadingServiceFromContext();
        }
    }

    /**
     * 取得<code>ResourceLoadingService</code>，如果还未初始化或不存在，则返回<code>null</code>。
     */
    public ResourceLoadingService getResourceLoadingService() {
        if (contextRefreshed) {
            return resourceLoadingService;
        }

        return getResourceLoadingServiceFromContext();
    }

    private ResourceLoadingService getResourceLoadingServiceFromContext() {
        try {
            return (ResourceLoadingService) factory.getBean(resourceLoadingServiceName);
        } catch (IllegalStateException e) {
            // beanFactory未准备好，试一下parent factory。如果均取不到ResourceLoadingService，返回null，不打日志
            ApplicationContext parent = factory.getParent();

            if (parent != null) {
                try {
                    return (ResourceLoadingService) parent.getBean(resourceLoadingServiceName);
                } catch (Exception ee) {
                }
            }
        } catch (NoSuchBeanDefinitionException e) {
            if (!complained) {
                complained = true;
                log.warn("ResourceLoadingService does not exists: beanName={}", resourceLoadingServiceName);
            }
        }

        return null;
    }

    /**
     * 取得指定路径名称所代表的资源对象。
     * <p>
     * 如果返回<code>null</code>表示使用原来的装载机制来取得资源。
     * </p>
     */
    public Resource getResourceByPath(String path) {
        ResourceLoadingService resourceLoadingService = getResourceLoadingService();

        if (resourceLoadingService == null) {
            // 如果resource loading service不存在，则返回null，调用原来的装载机制来取得资源。
            return null;
        }

        com.alibaba.citrus.service.resource.Resource resource;

        try {
            resource = resourceLoadingService.getResource(path, FOR_CREATE);
        } catch (IllegalStateException e) {
            // resourceLoadingService未准备好，有可能是在初始化resource loading service的过程中，
            // 某个loader或filter通过spring resource loader注入resource，从而产生递归调用。
            // 此时返回null，调用原来的装载机制来取得资源。
            return null;
        } catch (ResourceNotFoundException e) {
            return new NonExistResource(path, e);
        }

        return new ResourceAdapter(path, resource, this);
    }

    /**
     * 取得用来解析resource pattern的解析器。
     */
    public ResourcePatternResolver getResourcePatternResolver() {
        return resolver;
    }

    /**
     * 用<code>ResourceLoadingService</code>来解析resource pattern。
     */
    private class ResourceLoadingServicePatternResolver extends PathMatchingResourcePatternResolver {
        public ResourceLoadingServicePatternResolver() {
            super(factory);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Set<Resource> doFindPathMatchingFileResources(Resource rootDirResource, String subPattern)
                throws IOException {
            ResourceLoadingService resourceLoadingService = getResourceLoadingService();

            // 如果resource loading service不存在，或者resource不是从resource loading service取得的，
            // 则调用原来的装载机制来取得资源。
            if (resourceLoadingService == null || !(rootDirResource instanceof ResourceAdapter)) {
                return super.doFindPathMatchingFileResources(rootDirResource, subPattern);
            }

            ResourceAdapter rootResource = (ResourceAdapter) rootDirResource;
            String path = rootResource.getPathWithinContext();

            if (!path.endsWith("/")) {
                path += "/";
            }

            String fullPattern = path + subPattern;
            Set<Resource> result = createLinkedHashSet();

            findMatchingResources(resourceLoadingService, fullPattern, path, result);

            return result;
        }

        private void findMatchingResources(ResourceLoadingService resourceLoadingService, String fullPattern,
                                           String dir, Set<Resource> result) throws IOException {
            String[] candidates;

            try {
                candidates = resourceLoadingService.list(dir);
            } catch (ResourceNotFoundException e) {
                return;
            }

            boolean dirDepthNotFixed = fullPattern.indexOf("**") != -1;

            for (String name : candidates) {
                String currPath = dir + name;

                if (currPath.endsWith("/")
                        && (dirDepthNotFixed || StringUtils.countOccurrencesOf(currPath, "/") <= StringUtils
                                .countOccurrencesOf(fullPattern, "/"))) {
                    findMatchingResources(resourceLoadingService, fullPattern, currPath, result);
                }

                if (getPathMatcher().match(fullPattern, currPath)) {
                    try {
                        result.add(new ResourceAdapter(currPath, resourceLoadingService.getResource(currPath)));
                    } catch (ResourceNotFoundException e) {
                        // ignore
                    }
                }
            }
        }
    }

    /**
     * 一个特殊的resource：代表资源未找到。
     */
    private static class NonExistResource extends AbstractResource implements ContextResource {
        private final String location;
        private final IOException ioe;
        private final String description;

        public NonExistResource(String location, ResourceNotFoundException e) {
            this.location = location;
            this.ioe = new IOException("Resource Not Found [" + location + "]");
            this.ioe.initCause(e);
            this.description = "NonExistResource[" + location + "]";
        }

        public String getPathWithinContext() {
            return location;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public URL getURL() throws IOException {
            throw (IOException) ioe.fillInStackTrace();
        }

        @Override
        public File getFile() throws IOException {
            throw (IOException) ioe.fillInStackTrace();
        }

        public InputStream getInputStream() throws IOException {
            throw (IOException) ioe.fillInStackTrace();
        }

        @Override
        public Resource createRelative(String relativePath) throws IOException {
            throw (IOException) ioe.fillInStackTrace();
        }
    }
}
