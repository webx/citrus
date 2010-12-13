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
package com.alibaba.citrus.service.velocity.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 让velocity读取预装载的资源。
 * 
 * @author Michael Zhou
 */
public class PreloadedResourceLoader extends AbstractResourceLoader {
    public static final String PRELOADED_RESOURCES_KEY = "resources";
    private Map<String, Resource> preloadedResources;

    /**
     * 初始化resource loader.
     */
    @Override
    public void init(ExtendedProperties configuration) {
        rsvc.getLog().info(getLogID() + " : initialization starting.");

        preloadedResources = createTreeMap();

        @SuppressWarnings("unchecked")
        Map<String, Resource> resources = assertNotNull(
                (Map<String, Resource>) configuration.getProperty(PRELOADED_RESOURCES_KEY), PRELOADED_RESOURCES_KEY);

        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            String templateName = normalizeTemplateName(entry.getKey());
            Resource resource = entry.getValue();

            preloadedResources.put(templateName, resource);
        }

        rsvc.getLog().info(getLogID() + " : preloaded resources: " + new MapBuilder().appendAll(preloadedResources));
        rsvc.getLog().info(getLogID() + " : initialization complete.");
    }

    /**
     * 取得用于日志记录的ID。
     */
    @Override
    protected String getLogID() {
        return getClass().getSimpleName();
    }

    /**
     * 取得资源。
     */
    @Override
    protected Resource getResource(String templateName) {
        return preloadedResources.get(normalizeTemplateName(templateName));
    }

    @Override
    protected String getDesc() {
        return preloadedResources.size() + " preloaded resources";
    }
}
