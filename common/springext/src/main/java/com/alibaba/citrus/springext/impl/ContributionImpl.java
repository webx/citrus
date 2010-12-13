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

import static com.alibaba.citrus.springext.Schema.*;
import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static java.util.Collections.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.ConfigurationPointException;
import com.alibaba.citrus.springext.ConfigurationPoints;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionType;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.VersionableSchemas;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 实现<code>Contribution</code>接口。
 * 
 * @author Michael Zhou
 */
public class ContributionImpl implements Contribution {
    private final static Logger log = LoggerFactory.getLogger(Contribution.class);
    private final ConfigurationPoint configurationPoint;
    private final ConfigurationPointSettings settings;
    private final ContributionKey key;
    private final String implementationClassName;
    private VersionableSchemas schemas;

    ContributionImpl(ConfigurationPointImpl cp, ConfigurationPointSettings settings, ContributionType type,
                     String name, String contributionClassName) {
        this.configurationPoint = assertNotNull(cp, "configurationPoint");
        this.settings = settings;
        this.key = new ContributionKey(name, type);
        this.implementationClassName = contributionClassName; // 可能为空，但推迟到创建时再报错
    }

    public ConfigurationPoint getConfigurationPoint() {
        return configurationPoint;
    }

    public ContributionType getType() {
        return key.getType();
    }

    public String getName() {
        return key.getName();
    }

    ContributionKey getKey() {
        return key;
    }

    public String getImplementationClassName() {
        return implementationClassName;
    }

    public VersionableSchemas getSchemas() {
        if (schemas == null) {
            String mainName = configurationPoint.getName() + "/" + getName(); // configurationPointName/contributionName
            Schema mainSchema = loadMainSchema(mainName);
            Schema[] versionedSchemas = loadVersionedSchemas(mainName);

            schemas = new VersionableSchemasImpl(mainSchema, versionedSchemas);
        }

        return schemas;
    }

    private Schema loadMainSchema(String mainName) {
        String schemaName = mainName + "." + XML_SCHEMA_EXTENSION;
        URL resource;

        try {
            resource = settings.getResource(schemaName, log);
            assertNotNull(resource, schemaName);
            log.debug("Found schema file for contribution {}: {}", mainName, resource);
        } catch (IOException e) {
            log.warn("Failed to load schema: {}:  {}", schemaName, e);
            resource = null;
        }

        if (resource == null) {
            return null; // no schema found
        } else {
            return new SchemaImpl(schemaName, null, getDescription(), new ContributionSchemaSource(resource,
                    getConfigurationPoint().getConfigurationPoints(), getConfigurationPoint()));
        }
    }

    private Schema[] loadVersionedSchemas(String mainName) {
        String schemaNamePattern = mainName + "-*." + XML_SCHEMA_EXTENSION;
        Pattern pattern = Pattern.compile("^.*(" + mainName + "-(.+)\\." + XML_SCHEMA_EXTENSION + ")$");
        List<URL> resources;

        try {
            resources = settings.getResources(schemaNamePattern, log);
        } catch (IOException e) {
            log.warn("Failed to load schemas: {}:  {}", schemaNamePattern, e);
            resources = emptyList();
        }

        assertNotNull(resources, schemaNamePattern);

        List<Schema> schemas = createLinkedList();

        for (Iterator<URL> i = resources.iterator(); i.hasNext();) {
            URL url = i.next();
            String path = url.getPath();
            Matcher matcher = pattern.matcher(path);

            if (matcher.matches()) {
                String schemaName = matcher.group(1);
                String schemaVersion = matcher.group(2);

                if (checkVersion(schemaVersion)) {
                    schemas.add(new SchemaImpl(schemaName, schemaVersion, getDescription(),
                            new ContributionSchemaSource(url, getConfigurationPoint().getConfigurationPoints(),
                                    getConfigurationPoint())));
                } else {
                    i.remove();
                }
            } else {
                throw new ConfigurationPointException("Invalid schema name: " + url);
            }
        }

        if (!resources.isEmpty() && log.isDebugEnabled()) {
            ToStringBuilder buf = new ToStringBuilder();

            buf.format("Found %d versioned schema files for contribution %s:", resources.size(), mainName);
            buf.append(resources);

            log.debug(buf.toString());
        }

        return schemas.toArray(new Schema[schemas.size()]);
    }

    /**
     * 检查schemaName-version组合是否为另一个contribution的名字，如果是，则抛弃该组合。
     * <p>
     * 例如：有两个contribution：<code>break</code>和<code>break-if</code>。那么后者的schema：
     * <code>break-if.xsd</code>就会被误判成前者的某个version版本。该方法用来检查并排除这种情况。
     * </p>
     */
    private boolean checkVersion(String schemaVersion) {
        String name = getName();

        for (String v : schemaVersion.split("-")) {
            name += "-" + v;

            for (ContributionType type : ContributionType.values()) {
                if (getConfigurationPoint().getContribution(name, type) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 用于生成或取得contribution schema的内容。
     */
    private static class ContributionSchemaSource implements InputStreamSource {
        private final URL url;
        private final ConfigurationPoints cps;
        private final ConfigurationPoint thisCp;

        public ContributionSchemaSource(URL url, ConfigurationPoints cps, ConfigurationPoint thisCp) {
            this.url = assertNotNull(url, "no schema URL");
            this.cps = assertNotNull(cps, "no ConfigurationPoints");
            this.thisCp = assertNotNull(thisCp, "this ConfigurationPoint");
        }

        /**
         * 读取contribution schema，必要时处理和改进其内容。
         * <p>
         * 但假如schema读取失败，则只返回原始的schema流。
         * </p>
         */
        public InputStream getInputStream() throws IOException {
            try {
                return new ByteArrayInputStream(getContributionSchemaContent(getOriginalInputStream(),
                        url.toExternalForm(), true, cps, thisCp));
            } catch (DocumentException e) {
                return getOriginalInputStream();
            }
        }

        private InputStream getOriginalInputStream() throws IOException {
            URLConnection connection = this.url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        }

        @Override
        public String toString() {
            return url.toExternalForm();
        }
    }

    public String getDescription() {
        return String.format("Contribution[%s:%s]", getConfigurationPoint().getName(), getName());
    }

    @Override
    public String toString() {
        ToStringBuilder buf = new ToStringBuilder();

        buf.format("Contribution[toConfigurationPoint=%s, name=%s, type=%s, class=%s]",
                getConfigurationPoint().getName(), getName(), getType(), implementationClassName).start();

        buf.append(getSchemas());

        return buf.end().toString();
    }
}
