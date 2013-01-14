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

import static com.alibaba.citrus.springext.Schema.*;
import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.ConfigurationPointException;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionType;
import com.alibaba.citrus.springext.ResourceResolver.Resource;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.SourceInfo;
import com.alibaba.citrus.springext.VersionableSchemas;
import com.alibaba.citrus.springext.support.ConfigurationPointSourceInfo;
import com.alibaba.citrus.springext.support.ContributionSourceInfo;
import com.alibaba.citrus.springext.support.SourceInfoSupport;
import com.alibaba.citrus.util.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现<code>Contribution</code>接口。
 *
 * @author Michael Zhou
 */
public class ContributionImpl implements Contribution, ContributionSourceInfo {
    private final static Logger log = LoggerFactory.getLogger(Contribution.class);
    private final ConfigurationPoint                       configurationPoint;
    private final ConfigurationPointSettings               settings;
    private final ContributionKey                          key;
    private final String                                   implementationClassName;
    private final SourceInfo<ConfigurationPointSourceInfo> sourceInfo;
    private       VersionableSchemas                       schemas;

    ContributionImpl(ConfigurationPointImpl cp, ConfigurationPointSettings settings, ContributionType type,
                     String name, String contributionClassName, SourceInfo<ConfigurationPointSourceInfo> sourceInfo) {
        this.configurationPoint = assertNotNull(cp, "configurationPoint");
        this.settings = settings;
        this.key = new ContributionKey(name, type);
        this.implementationClassName = contributionClassName; // 可能为空，但推迟到创建时再报错
        this.sourceInfo = assertNotNull(sourceInfo, "sourceInfo");
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

    public String getAnnotation() {
        Schema schema = getSchemas().getMainSchema();

        if (schema != null) {
            Element element = schema.getElement(getName());

            if (element != null) {
                return element.getAnnotation();
            }
        }

        return null;
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
        Resource resource = settings.getResourceFromRelativeLocation(schemaName, log);

        if (resource == null) {
            return null; // no schema found
        } else {
            log.debug("Found schema file for contribution {}: {}", mainName, resource);
            return SchemaImpl.createForContribution(
                    schemaName, null, getDescription(), resource,
                    new SourceInfoSupport<ContributionSourceInfo>(this).setSource(resource),
                    // 此方法有一个副作用，将会把当前contribution添加到它所依赖的configuration point的depending contributions列表中。
                    getContributionSchemaTransformer(getConfigurationPoint().getConfigurationPoints(), this));
        }
    }

    private Schema[] loadVersionedSchemas(String mainName) {
        String schemaNamePattern = mainName + "-*." + XML_SCHEMA_EXTENSION;
        Pattern pattern = Pattern.compile("^.*(" + mainName + "-(.+)\\." + XML_SCHEMA_EXTENSION + ")$");
        List<Resource> resources = settings.getResourcesFromRelativeLocationPattern(schemaNamePattern, log);

        assertNotNull(resources, schemaNamePattern);

        List<Schema> schemas = createLinkedList();

        for (Iterator<Resource> i = resources.iterator(); i.hasNext(); ) {
            Resource resource = i.next();
            String path = resource.getName();
            Matcher matcher = pattern.matcher(path);

            if (matcher.matches()) {
                String schemaName = matcher.group(1);
                String schemaVersion = matcher.group(2);

                if (checkVersion(schemaVersion)) {
                    schemas.add(SchemaImpl.createForContribution(
                            schemaName, schemaVersion, getDescription(), resource,
                            new SourceInfoSupport<ContributionSourceInfo>(this).setSource(resource),
                            // 此方法有一个副作用，将会把当前contribution添加到它所依赖的configuration point的depending contributions列表中。
                            getContributionSchemaTransformer(getConfigurationPoint().getConfigurationPoints(), this)));
                } else {
                    i.remove();
                }
            } else {
                throw new ConfigurationPointException("Invalid schema name: " + resource);
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

    public String getDescription() {
        return String.format("Contribution[%s:%s]", getConfigurationPoint().getName(), getName());
    }

    public ConfigurationPointSourceInfo getParent() {
        return sourceInfo.getParent();
    }

    public Resource getSource() {
        return sourceInfo.getSource();
    }

    public int getLineNumber() {
        return sourceInfo.getLineNumber();
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
