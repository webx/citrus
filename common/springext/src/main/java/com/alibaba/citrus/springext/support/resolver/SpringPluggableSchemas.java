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
package com.alibaba.citrus.springext.support.resolver;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.springframework.util.ResourceUtils.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.PluggableSchemaResolver;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.alibaba.citrus.springext.ConfigurationPointException;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.Schemas;
import com.alibaba.citrus.springext.impl.SchemaImpl;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 将Spring所支持的<code>META-INF/spring.schemas</code>中定义的schemas移到本地服务器。
 * 
 * @author Michael Zhou
 */
public class SpringPluggableSchemas implements Schemas {
    private final static Logger log = LoggerFactory.getLogger(SpringPluggableSchemas.class);
    private final static String SCHEMA_MAPPINGS_LOCATION = PluggableSchemaResolver.DEFAULT_SCHEMA_MAPPINGS_LOCATION;
    private final static Pattern SCHEMA_VERSION_PATTERN = Pattern.compile("-((\\d+)(.\\d+)*)\\.xsd$");
    private final ResourceLoader resourceLoader;
    private final Map<String, Schema> nameToSchemaMappings;
    private final Map<String, String> uriToNameMappings;
    private boolean initialized;

    public SpringPluggableSchemas() {
        this(null);
    }

    public SpringPluggableSchemas(ResourceLoader resourceLoader) {
        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader();
        }

        this.resourceLoader = resourceLoader;
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

        Properties uriToClasspathLocationMappings;

        log.trace("Trying to load Spring schema mappings at {}", SCHEMA_MAPPINGS_LOCATION);

        try {
            uriToClasspathLocationMappings = PropertiesLoaderUtils.loadAllProperties(SCHEMA_MAPPINGS_LOCATION,
                    resourceLoader.getClassLoader());
        } catch (IOException e) {
            throw new ConfigurationPointException("Unable to load Spring schema mappings from "
                    + SCHEMA_MAPPINGS_LOCATION, e);
        }

        String desc = "SpringSchema[" + SCHEMA_MAPPINGS_LOCATION + "]";

        for (Entry<Object, Object> entry : uriToClasspathLocationMappings.entrySet()) {
            String uri = trimToNull((String) entry.getKey());
            String classpathLocation = trimToNull((String) entry.getValue());
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
        if (!classpathLocation.startsWith(CLASSPATH_URL_PREFIX)) {
            classpathLocation = CLASSPATH_URL_PREFIX + classpathLocation;
        }

        URL resource;

        try {
            resource = resourceLoader.getResource(classpathLocation).getURL();
        } catch (Exception e) {
            log.warn("Could not find schema {} for URI: {},\n  {}",
                    new String[] { classpathLocation, uri, e.getMessage() });

            return null;
        }

        return new UrlResource(resource) {
            @Override
            public String getDescription() {
                try {
                    return getURL().toExternalForm();
                } catch (IOException e) {
                    unexpectedException(e);
                    return null;
                }
            }
        };
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
