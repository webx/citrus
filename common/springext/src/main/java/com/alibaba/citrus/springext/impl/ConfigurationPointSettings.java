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
package com.alibaba.citrus.springext.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;
import static org.springframework.util.ResourceUtils.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

final class ConfigurationPointSettings {
    public final ClassLoader classLoader;
    public final String baseLocation;
    private final ResourcePatternResolver resolver;

    ConfigurationPointSettings(ClassLoader classLoader, String configurationPointsLocation) {
        if (classLoader == null) {
            classLoader = ClassUtils.getDefaultClassLoader();
        }

        assertNotNull(configurationPointsLocation, "configurationPointsLocation");

        this.classLoader = classLoader;
        this.baseLocation = configurationPointsLocation.substring(0, configurationPointsLocation.lastIndexOf("/") + 1);
        this.resolver = new PathMatchingResourcePatternResolver(classLoader);
    }

    URL getResource(String relativeLocation, Logger log) throws IOException {
        String location = toAbsoluteLocation(relativeLocation);

        if (log != null) {
            log.trace("Trying to find resource at {}", location);
        }

        Resource resource = resolver.getResource(location);

        if (resource != null) {
            return resource.getURL();
        } else {
            return null;
        }
    }

    List<URL> getResources(String relativeLocationPattern, Logger log) throws IOException {
        String locationPattern = toAbsoluteLocation(relativeLocationPattern);

        if (log != null) {
            log.trace("Trying to find resources at {}", locationPattern);
        }

        Resource[] resources = resolver.getResources(locationPattern);
        List<URL> urls;

        if (resources == null) {
            urls = emptyList();
        } else {
            urls = createLinkedList();

            for (Resource resource : resources) {
                urls.add(resource.getURL());
            }
        }

        return urls;
    }

    private String toAbsoluteLocation(String relativeLocationPattern) {
        relativeLocationPattern = assertNotNull(trimToNull(relativeLocationPattern), "locationPattern");

        if (relativeLocationPattern.startsWith(CLASSPATH_URL_PREFIX)) {
            relativeLocationPattern = relativeLocationPattern.substring(CLASSPATH_URL_PREFIX.length());
        }

        return CLASSPATH_URL_PREFIX + baseLocation + relativeLocationPattern;
    }
}
