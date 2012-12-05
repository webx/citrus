/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.springext.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.io.IOException;
import java.util.List;

import com.alibaba.citrus.springext.ResourceResolver;
import com.alibaba.citrus.springext.ResourceResolver.Resource;
import com.alibaba.citrus.springext.support.ClasspathResourceResolver;
import org.slf4j.Logger;
import org.springframework.util.ClassUtils;

final class ConfigurationPointSettings {
    public final ResourceResolver resourceResolver;
    public final String           baseLocation;
    public final ClassLoader      classLoader;

    /**
     * 从指定<code>ResourceResolver</code>中装载configuration points和contributions。
     * 所有contribution classes都<em>不会</em>被创建和注册。
     * 对于IDE plugin，此构造函数将被调用。
     */
    ConfigurationPointSettings(ResourceResolver resourceResolver, String configurationPointsLocation) {
        this(null, resourceResolver, configurationPointsLocation);
    }

    /**
     * 从class loader中装载configuration points和contributions。
     * 所有contribution classes也会被创建和注册。
     */
    ConfigurationPointSettings(ClassLoader classLoader, String configurationPointsLocation) {
        this(classLoader, null, configurationPointsLocation);
    }

    private ConfigurationPointSettings(ClassLoader classLoader, ResourceResolver resourceResolver, String configurationPointsLocation) {
        if (resourceResolver == null) {
            if (classLoader == null) {
                classLoader = ClassUtils.getDefaultClassLoader();
            }

            this.classLoader = classLoader;
            this.resourceResolver = new ClasspathResourceResolver(classLoader);
        } else {
            // 对于IDE plugin运行环境，不需要创建和注册contribution class类，因此也不需要classLoader。
            this.classLoader = null;
            this.resourceResolver = resourceResolver;
        }

        assertNotNull(configurationPointsLocation, "configurationPointsLocation");
        this.baseLocation = configurationPointsLocation.substring(0, configurationPointsLocation.lastIndexOf("/") + 1);
    }

    Resource getResourceFromRelativeLocation(String relativeLocation, Logger log) {
        String location = toAbsoluteLocation(relativeLocation);

        if (log != null) {
            log.trace("Trying to find resource at {}", location);
        }

        return resourceResolver.getResource(location);
    }

    List<Resource> getResourcesFromRelativeLocationPattern(String relativeLocationPattern, Logger log) {
        String locationPattern = toAbsoluteLocation(relativeLocationPattern);

        if (log != null) {
            log.trace("Trying to find resources at {}", locationPattern);
        }

        try {
            return createArrayList(resourceResolver.getResources(locationPattern));
        } catch (IOException e) {
            log.warn("Failed to load resources: {}: {}", relativeLocationPattern, e);
            return emptyList();
        }
    }

    private String toAbsoluteLocation(String relativeLocationPattern) {
        return baseLocation + assertNotNull(trimToNull(relativeLocationPattern), "locationPattern");
    }
}
