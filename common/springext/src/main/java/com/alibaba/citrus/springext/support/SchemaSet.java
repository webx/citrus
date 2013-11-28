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

package com.alibaba.citrus.springext.support;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.alibaba.citrus.springext.Namespaces;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.Schema.Element;
import com.alibaba.citrus.springext.Schema.Transformer;
import com.alibaba.citrus.springext.Schemas;
import com.alibaba.citrus.util.ToStringBuilder;
import com.alibaba.citrus.util.internal.LazyLoader;
import com.alibaba.citrus.util.internal.LazyLoader.Loader;

/**
 * 将一组<code>Schemas</code>整合在一起的集合。
 *
 * @author Michael Zhou
 */
public class SchemaSet implements Schemas, Namespaces, Iterable<Schemas> {
    private final List<Schemas> allSchemas;
    private final Map<String, Schema> nameToSchemas             = createHashMap();
    private final Map<String, Schema> nameToSchemasUnmodifiable = unmodifiableMap(nameToSchemas);
    private final SortedSet<String> names;
    private final SortedSet<String> namespaces;
    private final Set<String>       namespacesUnmodifiable;

    // 延迟加载namespace mappings，仅当有需要时再做。
    // 将所有相同namespace的schema放在一起，并按名称倒排序，即按：beans.xsd、beans-2.5.xsd、beans-2.0.xsd 顺序。
    private final LazyLoader<Map<String, Set<Schema>>, Object> nsToSchemas = LazyLoader.getDefault(new Loader<Map<String, Set<Schema>>, Object>() {
        public Map<String, Set<Schema>> load(Object context) {
            Map<String, Set<Schema>> nsToSchemasMappings = createTreeMap(); // 使用排序的map，使测试结果恒定。

            for (Schema schema : nameToSchemas.values()) {
                String namespace = schema.getTargetNamespace();

                if (namespace != null) {
                    Set<Schema> nsSchemas = nsToSchemasMappings.get(namespace);

                    if (nsSchemas == null) {
                        nsSchemas = createTreeSet(new Comparator<Schema>() {
                            public int compare(Schema o1, Schema o2) {
                                return o2.getName().compareTo(o1.getName());
                            }
                        });

                        nsToSchemasMappings.put(namespace, nsSchemas);
                        namespaces.add(namespace);
                    }

                    nsSchemas.add(schema);
                }
            }

            // getAvailableNamespaces()中可能包含一些namespace，它们没有对应的schemas。需要把它们特别地找出来。
            for (Schemas schemas : allSchemas) {
                if (schemas instanceof Namespaces) {
                    for (String namespace : ((Namespaces) schemas).getAvailableNamespaces()) {
                        if (!nsToSchemasMappings.containsKey(namespace)) {
                            nsToSchemasMappings.put(namespace, Collections.<Schema>emptySet());
                            namespaces.add(namespace);
                        }
                    }
                }
            }

            return unmodifiableMap(nsToSchemasMappings);
        }
    });

    public static SchemaSet getInstance(Schemas... schemasList) {
        if (schemasList != null && schemasList.length == 1 && schemasList[0] instanceof SchemaSet) {
            return (SchemaSet) schemasList[0];
        } else {
            return new SchemaSet(schemasList);
        }
    }

    public SchemaSet(Schemas... schemasList) {
        assertTrue(!isEmptyArray(schemasList), "schemasList");

        allSchemas = createArrayList(schemasList);

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

        this.namespaces = createTreeSet();
        this.namespacesUnmodifiable = unmodifiableSet(namespaces);

        // 检查所有schema，将重复的include提到最上层
        processIncludes();
    }

    @Override
    public Iterator<Schemas> iterator() {
        return allSchemas.iterator();
    }

    @Override
    public Set<String> getAvailableNamespaces() {
        nsToSchemas.getInstance(); // ensure initialized
        return namespacesUnmodifiable;
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
        class SchemaIncludes {
            Map<String, Schema> allIncludes;
            boolean             removeAllIncludes;
        }

        Map<String, SchemaIncludes> nameToSchemaIncludes = createHashMap();

        // 所有包含了include的，并且被其它schema所包含的schema，其引用需要被移到最上层的schema中。
        for (Schema schema : createArrayList(nameToSchemas.values())) {
            Map<String, Schema> allIncludes = getAllIncludes(schema); // 直接或间接的所有includes，按依赖顺序排列
            Map<String, Element> allElements = getAllElements(schema, allIncludes.values());
            boolean withIndirectIncludes = false;

            foundIncludes(schema, allIncludes.values()); // 给子类一个机会处理includes

            for (Schema includedSchema : allIncludes.values()) {
                if (includedSchema.getIncludes().length > 0) {
                    withIndirectIncludes = true;

                    SchemaIncludes si = nameToSchemaIncludes.get(includedSchema.getName());

                    if (si == null) {
                        si = new SchemaIncludes();
                        nameToSchemaIncludes.put(includedSchema.getName(), si);
                    }

                    si.removeAllIncludes = true;
                }
            }

            if (withIndirectIncludes) {
                SchemaIncludes si = nameToSchemaIncludes.get(schema.getName());

                if (si == null) {
                    si = new SchemaIncludes();
                    nameToSchemaIncludes.put(schema.getName(), si);
                }

                si.allIncludes = allIncludes;
            }

            // 收集当前schema的所有elements
            schema.setElements(allElements.values());
        }

        for (Map.Entry<String, SchemaIncludes> entry : nameToSchemaIncludes.entrySet()) {
            Schema schema = nameToSchemas.get(entry.getKey());
            SchemaIncludes si = entry.getValue();

            // 立即执行以下所有的transformer，以防在多线程环境下出错。
            if (si.removeAllIncludes) {
                schema.transform(getTransformerWhoRemovesIncludes(), true);
            } else if (si.allIncludes != null) {
                schema.transform(getTransformerWhoAddsIndirectIncludes(si.allIncludes), true);
            }
        }

        finishProcessIncludes();
    }

    protected void foundIncludes(Schema schema, Collection<Schema> allIncludes) {
    }

    protected void finishProcessIncludes() {
    }

    private Map<String, Element> getAllElements(Schema schema, Collection<Schema> includes) {
        Map<String, Element> all = createHashMap();

        for (Element element : schema.getElements()) {
            all.put(element.getName(), element);
        }

        for (Schema include : includes) {
            for (Element element : include.getElements()) {
                all.put(element.getName(), element);
            }
        }

        return all;
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

    /** 查找include schema，如未找到，抛异常。 */
    private Schema findIncludedSchema(String include, String fromSchema) {
        Schema found = findSchema(include);

        if (found == null) {
            String resolvedInclude = URI.create(fromSchema + "/../" + include).normalize().toString();

            if (!isEquals(include, resolvedInclude)) {
                found = findSchema(resolvedInclude);
            }
        }

        return assertNotNull(found, "Could not include schema \"%s\" in %s", include, fromSchema);
    }

    /** 添加一个schema。 */
    public void addSchema(Schema schema) {
        nameToSchemas.put(schema.getName(), schema);
        names.add(schema.getName());
    }

    /** 取得名称和schema的映射表。 */
    public Map<String, Schema> getNamedMappings() {
        return nameToSchemasUnmodifiable;
    }

    /** 取得namespace和schema的映射表。 */
    public Map<String, Set<Schema>> getNamespaceMappings() {
        return nsToSchemas.getInstance();
    }

    /** 查找systemId对应的schema，如未找到，则返回<code>null</code>。 */
    public Schema findSchema(String systemId) {
        systemId = assertNotNull(trimToNull(systemId), "systemId").replaceAll("\\\\", "/");

        try {
            systemId = URI.create(systemId).normalize().getSchemeSpecificPart();
        } catch (Exception e) {
            // ignore
        }

        for (String schemaName : names) {
            if (systemId.equals(schemaName)) {
                return nameToSchemas.get(schemaName);
            }

            if (systemId.endsWith(schemaName)) {
                if (schemaName.startsWith("/") || systemId.endsWith("/" + schemaName)) {
                    return nameToSchemas.get(schemaName);
                }
            }
        }

        return null;
    }

    /** 对所有的schema应用转换器。 */
    public void transformAll(Transformer transformer) {
        if (transformer == null) {
            transformer = getNoopTransformer();
        }

        for (Schema schema : nameToSchemas.values()) {
            schema.transform(transformer, true);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append("SchemaSet").append(names).toString();
    }
}
