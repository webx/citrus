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

import static com.alibaba.citrus.util.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.Map;
import java.util.TreeMap;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.VersionableSchemas;
import com.alibaba.citrus.util.internal.ToStringBuilder;

public class VersionableSchemasImpl implements VersionableSchemas {
    private final Schema mainSchema;
    private final Map<String, Schema> versionedSchemas;
    private final Map<String, Schema> nameToSchemas;

    public VersionableSchemasImpl(Schema mainSchema, Schema[] versionedSchemas) {
        this.mainSchema = mainSchema;
        this.versionedSchemas = createTreeMap();

        for (Schema schema : versionedSchemas) {
            this.versionedSchemas.put(schema.getVersion(), schema);
        }

        // √˚≥∆ -> schema”≥…‰
        TreeMap<String, Schema> mappings = createTreeMap();

        if (mainSchema != null) {
            mappings.put(mainSchema.getName(), mainSchema);
        }

        for (Schema versionedSchema : versionedSchemas) {
            mappings.put(versionedSchema.getName(), versionedSchema);
        }

        this.nameToSchemas = unmodifiableMap(mappings);
    }

    public Schema getMainSchema() {
        return mainSchema;
    }

    public Schema getVersionedSchema(String version) {
        return version == null ? mainSchema : versionedSchemas.get(version);
    }

    public String[] getVersions() {
        return versionedSchemas.keySet().toArray(new String[versionedSchemas.size()]);
    }

    public Map<String, Schema> getNamedMappings() {
        return nameToSchemas;
    }

    @Override
    public String toString() {
        ToStringBuilder buf = new ToStringBuilder();

        buf.format("Schemas[");

        if (mainSchema == null) {
            buf.format("missing main schema");
        } else if (mainSchema.getTargetNamespace() == null) {
            buf.format("%s", mainSchema.getName());
        } else {
            buf.format("%s, targetNamespace=%s", mainSchema.getName(), mainSchema.getTargetNamespace());
        }

        buf.format(", %d versioned schemas]", getVersions().length).start();

        for (String version : getVersions()) {
            buf.format("version %s: %s%n", version, getVersionedSchema(version));
        }

        return buf.end().toString();
    }
}
