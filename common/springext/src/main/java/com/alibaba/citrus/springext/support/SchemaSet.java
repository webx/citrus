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
package com.alibaba.citrus.springext.support;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.springframework.core.io.InputStreamSource;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.Schemas;
import com.alibaba.citrus.springext.impl.SchemaImpl;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 将一组<code>Schemas</code>整合在一起的集合。
 * 
 * @author Michael Zhou
 */
public class SchemaSet implements Schemas {
    private final Map<String, Schema> nameToSchemas = createHashMap();
    private final Map<String, Schema> nameToSchemasUnmodifiable = unmodifiableMap(nameToSchemas);
    private final SortedSet<String> names;

    public SchemaSet(Schemas... schemasList) {
        assertTrue(!isEmptyArray(schemasList), "schemasList");

        for (Schemas schemas : schemasList) {
            this.nameToSchemas.putAll(schemas.getNamedMappings());
        }

        // sort by string length (descending) and name
        this.names = createTreeSet(new Comparator<String>() {
            public int compare(String o1, String o2) {
                int lengthCompare = o2.length() - o1.length();

                if (lengthCompare == 0) {
                    return o1.compareTo(o2);
                }

                return lengthCompare;
            }
        }, this.nameToSchemas.keySet());

        // 检查所有schema，将重复的include提到最上层
        processIncludes();
    }

    /**
     * 检查所有schema，将所有includes提到最上层。
     * <p>
     * 例如：
     * </p>
     * <ul>
     * <li>all.xsd 包含 x.xsd 和 y.xsd</li>
     * <li>x.xsd 包含 z.xsd</li>
     * <li>y.xsd 包含 z.xsd</li>
     * </ul>
     * <p>
     * 在上面的例子中，z.xsd被包含了两遍，导致解析错误。 该方法做如下处理，从而避免了上述问题：
     * </p>
     * <ul>
     * <li>all.xsd 包含 x.xsd，y.xsd and z.xsd</li>
     * <li>x.xsd 不包含 z.xsd</li>
     * <li>y.xsd 不包含 z.xsd</li>
     * </ul>
     */
    private void processIncludes() {
        // 所有包含了include的，并且被其它schema所包含的schema，其引用需要被移到最上层的schema中。
        for (Schema schema : createArrayList(nameToSchemas.values())) {
            Map<String, Schema> allIncludes = getAllIncludes(schema); // 直接或间接的所有includes，按依赖顺序排列
            String[] allElements = getAllElements(schema, allIncludes.values());
            boolean withIndirectIncludes = false;

            for (Schema includedSchema : allIncludes.values()) {
                if (includedSchema.getIncludes().length > 0) {
                    withIndirectIncludes = true;
                    overrideSchemaForInclude(includedSchema);
                }
            }

            if (withIndirectIncludes) {
                schema = setSchemaWithIncludes(schema, allIncludes);
            }

            // 收集当前schema的所有elements
            // 由于表中的schema对象可能被替换，所以在这里确保取得最新的schema对象。
            Schema newSchema = nameToSchemas.get(schema.getName());

            if (newSchema instanceof SchemaInternal) {
                ((SchemaInternal) newSchema).setElements(allElements);
            }
        }
    }

    private String[] getAllElements(Schema schema, Collection<Schema> includes) {
        Set<String> all = createTreeSet();

        all.addAll(Arrays.asList(schema.getElements()));

        for (Schema include : includes) {
            all.addAll(Arrays.asList(include.getElements()));
        }

        return all.toArray(new String[all.size()]);
    }

    private void overrideSchemaForInclude(final Schema schema) {
        InputStreamSource sourceWithoutIncludes = new InputStreamSource() {
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(SchemaUtil.getSchemaContentWithoutIncludes(schema));
            }
        };

        addSchema(new SchemaImpl(schema.getName(), schema.getVersion(), schema.getTargetNamespace(),
                schema.getPreferredNsPrefix(), schema.getSourceDescription(), sourceWithoutIncludes));
    }

    private Schema setSchemaWithIncludes(final Schema schema, final Map<String, Schema> allIncludes) {
        InputStreamSource sourceWithModifiedIncludes = new InputStreamSource() {
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(SchemaUtil.getSchemaContentWithIndirectIncludes(schema, allIncludes));
            }
        };

        Schema newSchema = new SchemaImpl(schema.getName(), schema.getVersion(), schema.getTargetNamespace(),
                schema.getPreferredNsPrefix(), schema.getSourceDescription(), sourceWithModifiedIncludes);

        addSchema(newSchema);

        return newSchema;
    }

    /**
     * 取得所有的直接或间接的includes。
     * <p>
     * 使用深度优先的算法，被include的总是列在较前面。
     * </p>
     */
    private Map<String, Schema> getAllIncludes(Schema schema) {
        Map<String, Schema> includes = createLinkedHashMap();
        getAllIncludesDepthFirst(schema, includes);
        includes.remove(schema.getName()); // 不包含自身
        return includes;
    }

    private void getAllIncludesDepthFirst(Schema schema, Map<String, Schema> includes) {
        for (String include : schema.getIncludes()) {
            Schema includedSchema = findIncludedSchema(include, schema.getName());
            getAllIncludesDepthFirst(includedSchema, includes);
        }

        includes.put(schema.getName(), schema);
    }

    /**
     * 查找include schema，如未找到，抛异常。
     */
    private Schema findIncludedSchema(String include, String fromSchema) {
        return assertNotNull(findSchema(include), "Could not include schema \"%s\" in %s", include, fromSchema);
    }

    /**
     * 添加一个schema。
     */
    public void addSchema(Schema schema) {
        nameToSchemas.put(schema.getName(), schema);
        names.add(schema.getName());
    }

    /**
     * 取得名称和schema的映射表。
     */
    public Map<String, Schema> getNamedMappings() {
        return nameToSchemasUnmodifiable;
    }

    /**
     * 查找systemId对应的schema，如未找到，则返回<code>null</code>。
     */
    public Schema findSchema(String systemId) {
        systemId = assertNotNull(trimToNull(systemId), "systemId").replaceAll("\\\\", "/");

        try {
            systemId = URI.create(systemId).normalize().toString();
        } catch (Exception e) {
            // ignore
        }

        for (String schemaName : names) {
            if (systemId.endsWith(schemaName)) {
                return nameToSchemas.get(schemaName);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append("SchemaSet").append(names).toString();
    }
}
