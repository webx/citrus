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
package com.alibaba.citrus.service.resource.loader;

import static com.alibaba.citrus.util.BasicConstant.*;

import java.net.URL;
import java.util.Set;

import org.springframework.context.ResourceLoaderAware;

import com.alibaba.citrus.service.resource.Resource;
import com.alibaba.citrus.service.resource.ResourceLoader;
import com.alibaba.citrus.service.resource.ResourceLoaderContext;
import com.alibaba.citrus.service.resource.ResourceLoadingOption;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.support.URLResource;

/**
 * 从classloader中装载资源。
 * 
 * @author Michael Zhou
 */
public class ClasspathResourceLoader implements ResourceLoader, ResourceLoaderAware {
    private ClassLoader cl;

    public ClassLoader getClassLoader() {
        return cl;
    }

    public void setClassLoader(ClassLoader cl) {
        this.cl = cl;
    }

    public void setResourceLoader(org.springframework.core.io.ResourceLoader springLoader) {
        this.cl = springLoader.getClassLoader();
    }

    /**
     * 初始化loader，并设定loader所在的<code>ResourceLoadingService</code>的实例。
     */
    public void init(ResourceLoadingService resourceLoadingService) {
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
    }

    public Resource getResource(ResourceLoaderContext context, Set<ResourceLoadingOption> options) {
        String resourceName = context.substitute(EMPTY_STRING);

        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }

        URL resourceURL = cl.getResource(resourceName);

        if (resourceURL != null) {
            return new URLResource(resourceURL);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + cl + "]";
    }
}
