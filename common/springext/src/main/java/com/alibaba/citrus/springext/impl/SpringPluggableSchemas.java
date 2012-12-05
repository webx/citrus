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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.citrus.springext.ConfigurationPointException;
import com.alibaba.citrus.springext.ResourceResolver;
import com.alibaba.citrus.springext.ResourceResolver.Resource;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.Schemas;
import com.alibaba.citrus.springext.support.ClasspathResourceResolver;
import com.alibaba.citrus.util.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.PluggableSchemaResolver;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.ClassUtils;

/**
 * 将Spring所支持的<code>META-INF/spring.schemas</code>中定义的schemas移到本地服务器。
 *
 * @author Michael Zhou
 */
public class SpringPluggableSchemas implements Schemas {
    private final static Logger  log                      = LoggerFactory.getLogger(SpringPluggableSchemas.class);
    private final static String  SCHEMA_MAPPINGS_LOCATION = PluggableSchemaResolver.DEFAULT_SCHEMA_MAPPINGS_LOCATION;
    private final static Pattern SCHEMA_VERSION_PATTERN   = Pattern.compile("-((\\d+)(.\\d+)*)\\.xsd$");
    private final ResourceResolver    resourceResolver;
    private final Map<String, Schema> nameToSchemaMappings;
    private final Map<String, String> uriToNameMappings;
    private       boolean             initialized;

    /** 通过默认的<code>ClassLoader</code>来查找spring schemas。 */
    public SpringPluggableSchemas() {
        this(null, null);
    }

    /**
     * 通过指定的<code>ClassLoader</code>来查找spring schemas。
     * 如果未指定<code>ClassLoader</code>，则使用默认的<code>ClassLoader</code>。
     */
    public SpringPluggableSchemas(ClassLoader classLoader) {
        this(classLoader, null);
    }

    /**
     * 通过指定的<code>ResourceResolver</code>来查找spring schemas。
     * 适合来实现IDE plugins。
     */
    public SpringPluggableSchemas(ResourceResolver resourceResolver) {
        this(null, assertNotNull(resourceResolver, "no resourceResolver was specified"));
    }

    private SpringPluggableSchemas(ClassLoader classLoader, ResourceResolver resourceResolver) {
        if (resourceResolver == null) {
            if (classLoader == null) {
                classLoader = ClassUtils.getDefaultClassLoader();
            }

            this.resourceResolver = new ClasspathResourceResolver(classLoader);
        } else {
            // IDE plugin mode
            this.resourceResolver = resourceResolver;
        }

        this.nameToSchemaMappings = createTreeMap();
        this.uriToNameMappings = createTreeMap();
    }

    public Map<String, Schema> getNamedMappings() {
        ensureInit();
        return nameToSchemaMappings;
    }

    public Map<String, String> getUriToNameMappings() {
        ensureInit();
        return uriToNameMappings;
    }

    private void ensureInit() {
        if (initialized) {
            return;
        }

        initialized = true;

        Map<String, String> uriToClasspathLocationMappings;

        log.trace("Trying to load Spring schema mappings at {}", SCHEMA_MAPPINGS_LOCATION);

        try {
            uriToClasspathLocationMappings = resourceResolver.loadAllProperties(SCHEMA_MAPPINGS_LOCATION);
        } catch (IOException e) {
            throw new ConfigurationPointException("Unable to load Spring schema mappings from " + SCHEMA_MAPPINGS_LOCATION, e);
        }

        String desc = "SpringSchema[" + SCHEMA_MAPPINGS_LOCATION + "]";

        for (Entry<String, String> entry : uriToClasspathLocationMappings.entrySet()) {
            String uri = trimToNull(entry.getKey());
            String classpathLocation = trimToNull(entry.getValue());
            String schemaName = getSchemaName(uri);
            Matcher matcher = SCHEMA_VERSION_PATTERN.matcher(schemaName);
            String version = null;

            if (matcher.find()) {
                version = matcher.group(1);
            }

            InputStreamSource source = getResource(classpathLocation, uri);

            if (source != null) {
                nameToSchemaMappings.put(schemaName, new SchemaImpl(schemaName, version, true, desc, source));
                uriToNameMappings.put(uri, schemaName);
            }
        }

        if (log.isDebugEnabled() && !uriToNameMappings.isEmpty()) {
            ToStringBuilder buf = new ToStringBuilder();

            buf.format("Loaded Spring schema mappings at %s, %d schemas found.", SCHEMA_MAPPINGS_LOCATION,
                       uriToNameMappings.size()).appendMap(uriToNameMappings);

            log.debug(buf.toString());
        }
    }

    private InputStreamSource getResource(String classpathLocation, String uri) {
        Resource resource = resourceResolver.getResource(classpathLocation);

        if (resource == null) {
            log.warn("Could not find schema {} for URI: {}", classpathLocation, uri);
        }

        return resource;
    }

    private String getSchemaName(String uri) {
        // 替换URI，将http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
        // 替换成：www.springframework.org/schema/beans/spring-beans-2.5.xsd
        return URI.create(uri).normalize().getSchemeSpecificPart().replaceAll("^/+|/+$", "");
    }

    @Override
    public String toString() {
        ToStringBuilder buf = new ToStringBuilder();

        buf.format("SpringPluggableSchemas[loaded from %s]", SCHEMA_MAPPINGS_LOCATION);
        buf.appendMap(uriToNameMappings);

        return buf.toString();
    }
}
