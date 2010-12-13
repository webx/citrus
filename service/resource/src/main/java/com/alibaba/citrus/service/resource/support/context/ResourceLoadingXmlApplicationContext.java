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
package com.alibaba.citrus.service.resource.support.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.service.resource.support.ResourceLoadingSupport;
import com.alibaba.citrus.springext.support.context.AbstractXmlApplicationContext;

/**
 * 从resource loading service中装载配置文件<code>ApplicationContext</code>实现。
 * 
 * @author Michael Zhou
 * @see AbstractXmlApplicationContext
 */
public class ResourceLoadingXmlApplicationContext extends AbstractXmlApplicationContext {
    private Resource configResource;

    /**
     * 从一个现成的<code>Resource</code>中创建spring容器，并初始化。
     */
    public ResourceLoadingXmlApplicationContext(Resource resource) throws BeansException {
        this(resource, null);
    }

    /**
     * 从一个现成的<code>Resource</code>中创建spring容器，并初始化。
     */
    public ResourceLoadingXmlApplicationContext(Resource resource, ApplicationContext parentContext)
            throws BeansException {
        super(parentContext);
        this.configResource = resource;
        setResourceLoadingExtender(new ResourceLoadingSupport(this));
        refresh();
    }

    /**
     * 从一组配置文件名中，创建spring容器，并初始化。
     * <p>
     * 假如<code>parentContext</code>中定义了<code>ResourceLoadingService</code>，那么
     * <code>configLocations</code>以及所有的imports将从中装载。
     * </p>
     */
    public ResourceLoadingXmlApplicationContext(String[] configLocations, ApplicationContext parentContext) {
        super(parentContext);
        setConfigLocations(configLocations);
        setResourceLoadingExtender(new ResourceLoadingSupport(this));
        refresh();
    }

    public void setConfigResource(Resource configResource) {
        this.configResource = configResource;
    }

    @Override
    protected Resource[] getConfigResources() {
        if (configResource == null) {
            return null;
        } else {
            return new Resource[] { configResource };
        }
    }
}
