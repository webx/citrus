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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.ConfigurationPoints;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ResourceResolver;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.Schemas;
import com.alibaba.citrus.springext.impl.ConfigurationPointsImpl;
import com.alibaba.citrus.springext.impl.SpringPluggableSchemas;
import com.alibaba.citrus.util.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * 创建一个schema set，其中包含以下所有的schemas：
 * <ul>
 * <li>所有的SpringExt扩展点、捐献的schemas。</li>
 * <li>所有的Spring原有的schemas。</li>
 * </ul>
 *
 * @author Michael Zhou
 */
public class SpringExtSchemaSet extends SchemaSet {
    private NamespaceItem[] treeItems;

    /** 通过默认的<code>ClassLoader</code>来装载schemas。 */
    public SpringExtSchemaSet() {
        super(new ConfigurationPointsImpl(), new SpringPluggableSchemas());
    }

    /** 通过指定的<code>ClassLoader</code>来装载schemas。 */
    public SpringExtSchemaSet(ClassLoader classLoader) {
        super(new ConfigurationPointsImpl(classLoader), new SpringPluggableSchemas(classLoader));
    }

    /** 通过指定的<code>ResourceResolver</code>来装载schemas（IDE plugins mode）。 */
    public SpringExtSchemaSet(ResourceResolver resourceResolver) {
        super(new ConfigurationPointsImpl(resourceResolver), new SpringPluggableSchemas(resourceResolver));
    }

    /** for test only */
    SpringExtSchemaSet(String location) {
        super(new ConfigurationPointsImpl((ClassLoader) null, location), new SpringPluggableSchemas());
    }

    public ConfigurationPoints getConfigurationPoints() {
        for (Schemas schemas : this) {
            if (schemas instanceof ConfigurationPoints) {
                return (ConfigurationPoints) schemas;
            }
        }

        unreachableCode("no ConfigurationPoints found");
        return null;
    }

    /** 取得所有独立的schemas。 */
    public synchronized NamespaceItem[] getIndependentItems() {
        if (treeItems == null) {
            treeItems = new TreeBuilder().build();
        }

        return treeItems;
    }

    public static interface TreeItem {
        @NotNull
        TreeItem[] getChildren();
    }

    public static interface NamespaceItem extends TreeItem {
        @NotNull
        String getNamespace();

        @NotNull
        Set<Schema> getSchemas();
    }

    public static interface ParentOf<C extends TreeItem> {
    }

    public static class ConfigurationPointItem extends AbstractNamespaceItem<ContributionItem> {
        private final ConfigurationPoint configurationPoint;

        public ConfigurationPointItem(@NotNull String namespace, @NotNull Set<Schema> schemas, @NotNull ConfigurationPoint configurationPoint) {
            super(namespace, schemas);
            this.configurationPoint = configurationPoint;
        }

        @NotNull
        public ConfigurationPoint getConfigurationPoint() {
            return configurationPoint;
        }
    }

    public static class ContributionItem extends AbstractTreeItem<ConfigurationPointItem> {
        private final Contribution contribution;

        public ContributionItem(@NotNull Contribution contribution) {
            this.contribution = contribution;
        }

        @NotNull
        public Contribution getContribution() {
            return contribution;
        }

        @Override
        public String toString() {
            return contribution.getName();
        }
    }

    public static class SpringPluggableItem extends AbstractNamespaceItem<TreeItem> {
        public SpringPluggableItem(@NotNull String namespace, @NotNull Set<Schema> schemas) {
            super(namespace, schemas);
        }
    }

    private static abstract class AbstractTreeItem<C extends TreeItem> implements TreeItem, ParentOf<C> {
        protected final Map<String, C> children = createTreeMap();

        @NotNull
        public TreeItem[] getChildren() {
            return children.values().toArray(new TreeItem[children.size()]);
        }

        @NotNull
        public String dump() {
            ToStringBuilder buf = new ToStringBuilder();
            dump(buf);
            return buf.toString();
        }

        void dump(ToStringBuilder buf) {
            buf.append(this).start("{", "}");

            int i = 0;
            for (TreeItem child : getChildren()) {
                if (i++ > 0) {
                    buf.append("\n");
                }

                ((AbstractTreeItem) child).dump(buf);
            }

            buf.end();
        }
    }

    private static abstract class AbstractNamespaceItem<C extends TreeItem> extends AbstractTreeItem<C>
            implements NamespaceItem {
        private final String      namespace;
        private final Set<Schema> schemas;

        protected AbstractNamespaceItem(@NotNull String namespace, @NotNull Set<Schema> schemas) {
            this.namespace = namespace;
            this.schemas = schemas;
        }

        @NotNull
        public String getNamespace() {
            return namespace;
        }

        @NotNull
        public Set<Schema> getSchemas() {
            return schemas;
        }

        @Override
        public String toString() {
            return namespace;
        }
    }

    private class TreeBuilder {
        private final Map<String, Set<Schema>>   namespaceMappings  = getNamespaceMappings();
        private final Map<String, NamespaceItem> items              = createHashMap();
        private final Map<String, NamespaceItem> independentItems   = createTreeMap();
        private final LinkedList<String>         buildingNamespaces = createLinkedList();

        public NamespaceItem[] build() {
            for (String namespace : namespaceMappings.keySet()) {
                buildNamespaceItemRecursively(namespace);
            }

            return independentItems.values().toArray(new NamespaceItem[independentItems.size()]);
        }

        private boolean buildNamespaceItemRecursively(@NotNull String namespace) {
            if (buildingNamespaces.contains(namespace)) {
                return false; // 防止递归失控
            }

            try {
                buildingNamespaces.push(namespace);
                buildNamespaceItem(namespace);
            } finally {
                buildingNamespaces.pop();
            }

            return true;
        }

        private void buildNamespaceItem(@NotNull String namespace) {
            if (items.containsKey(namespace)) {
                return; // 防止重复build
            }

            Set<Schema> schemas = namespaceMappings.get(namespace);

            assertTrue(schemas != null, "%s not exist", namespace); // namespace必须存在

            Schema schema = null;

            if (!schemas.isEmpty()) {
                schema = schemas.iterator().next(); // 取得第一个schema作为main schema。
            }

            // spring.schemas
            if (schema == null || schema instanceof SpringPluggableSchemaSourceInfo) {
                buildSpringPluggableItem(namespace, schemas);
            }

            // configuration point
            else if (schema instanceof ConfigurationPointSchemaSourceInfo) {
                buildConfigurationPointItem(namespace, schemas, (ConfigurationPointSchemaSourceInfo) schema);
            }

            // 不会发生
            else {
                unreachableCode();
            }
        }

        private void buildSpringPluggableItem(@NotNull String namespace, @NotNull Set<Schema> schemas) {
            NamespaceItem item = new SpringPluggableItem(namespace, schemas);
            items.put(namespace, item);
            independentItems.put(namespace, item);
        }

        private void buildConfigurationPointItem(String namespace, Set<Schema> schemas, ConfigurationPointSchemaSourceInfo schema) {
            ConfigurationPoint configurationPoint = (ConfigurationPoint) schema.getParent();
            ConfigurationPointItem item = new ConfigurationPointItem(namespace, schemas, configurationPoint);
            items.put(namespace, item);

            Collection<Contribution> dependingContributions = configurationPoint.getDependingContributions();
            int count = 0;

            for (Contribution contribution : dependingContributions) {
                String dependingNamespace = contribution.getConfigurationPoint().getNamespaceUri();

                if (buildNamespaceItemRecursively(dependingNamespace)) {
                    count++;
                    NamespaceItem parentItem = assertNotNull(items.get(dependingNamespace), "no item for namespace %s", namespace);

                    if (parentItem instanceof ConfigurationPointItem) {
                        item = buildContributionItem(namespace, item, contribution, (ConfigurationPointItem) parentItem);
                    }
                }
            }

            if (count == 0) {
                independentItems.put(namespace, item);
            }
        }

        private ConfigurationPointItem buildContributionItem(String namespace, ConfigurationPointItem item, Contribution contribution, ConfigurationPointItem parentItem) {
            String contributionName = contribution.getName();
            ContributionItem parentContributionItem = parentItem.children.get(contributionName);

            if (parentContributionItem == null) {
                parentContributionItem = new ContributionItem(contribution);
                parentItem.children.put(contributionName, parentContributionItem);
            }

            if (!parentContributionItem.children.containsKey(namespace)) {
                parentContributionItem.children.put(namespace, item);
            }

            return item;
        }
    }
}
