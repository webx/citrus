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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.ConfigurationPoints;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ResourceResolver;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.Schemas;
import com.alibaba.citrus.springext.impl.ConfigurationPointImpl;
import com.alibaba.citrus.springext.impl.ConfigurationPointsImpl;
import com.alibaba.citrus.springext.impl.SpringPluggableSchemas;
import com.alibaba.citrus.springext.support.SchemaUtil.AnyElementVisitor;
import com.alibaba.citrus.util.ToStringBuilder;

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
    private NamespaceItem[] allItems;
    private NamespaceItem[] treeItems;
    private NamespaceItem[] treeItemsWithAllContributions;

    /** 通过默认的<code>ClassLoader</code>来装载schemas。 */
    public SpringExtSchemaSet() {
        this(new ConfigurationPointsImpl(), new SpringPluggableSchemas());
    }

    /** 通过指定的<code>ClassLoader</code>来装载schemas。 */
    public SpringExtSchemaSet(ClassLoader classLoader) {
        this(new ConfigurationPointsImpl(classLoader), new SpringPluggableSchemas(classLoader));
    }

    /** 通过指定的<code>ResourceResolver</code>来装载schemas（IDE plugins mode）。 */
    public SpringExtSchemaSet(ResourceResolver resourceResolver) {
        this(new ConfigurationPointsImpl(resourceResolver), new SpringPluggableSchemas(resourceResolver));
    }

    /** for test only */
    SpringExtSchemaSet(String location) {
        this(new ConfigurationPointsImpl((ClassLoader) null, location), new SpringPluggableSchemas());
    }

    private SpringExtSchemaSet(ConfigurationPointsImpl configurationPoints, SpringPluggableSchemas springPluggableSchemas) {
        super(configurationPoints, springPluggableSchemas);
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

    /*
     * 处理以下情形：
     * <p/>
     * 一个contribution schema引用了一个spring pluggable schema。如果spring pluggable schema中包含一个anyElement，引用了另一个configuration point。
     * 则需要将anyElement展开，并添加configuration point对于contribution的依赖。
     */
    private static class IncludedSchemaInfo implements Iterable<Contribution> {
        private final Map<String, Contribution> includingContributions = createHashMap();
        private final Schema includedSchema;

        private IncludedSchemaInfo(Schema includedSchema) {
            this.includedSchema = includedSchema;
        }

        public Schema getIncludedSchema() {
            return includedSchema;
        }

        public Iterator<Contribution> iterator() {
            return includingContributions.values().iterator();
        }
    }

    private Map<String, IncludedSchemaInfo> includedSchemaInfoMap;

    @Override
    protected void foundIncludes(Schema schema, Collection<Schema> allIncludes) {
        if (schema instanceof ContributionSchemaSourceInfo) {
            Contribution contribution = (Contribution) ((ContributionSchemaSourceInfo) schema).getParent();

            if (contribution != null) {
                for (Schema includedSchema : allIncludes) {
                    if (includedSchema instanceof SpringPluggableSchemaSourceInfo) {
                        if (includedSchemaInfoMap == null) {
                            includedSchemaInfoMap = createHashMap();
                        }

                        String includedSchemaName = includedSchema.getName();
                        IncludedSchemaInfo includedSchemaInfo = includedSchemaInfoMap.get(includedSchemaName);

                        if (includedSchemaInfo == null) {
                            includedSchemaInfo = new IncludedSchemaInfo(includedSchema);
                            includedSchemaInfoMap.put(includedSchemaName, includedSchemaInfo);
                        }

                        includedSchemaInfo.includingContributions.put(schema.getName(), contribution);
                    }
                }
            }
        }
    }

    @Override
    protected void finishProcessIncludes() {
        if (includedSchemaInfoMap != null) {
            for (final IncludedSchemaInfo includedSchemaInfo : includedSchemaInfoMap.values()) {
                Schema includedSchema = includedSchemaInfo.getIncludedSchema();

                includedSchema.transform(SchemaUtil.getAnyElementTransformer(getConfigurationPoints(), new AnyElementVisitor() {
                    public void visitAnyElement(ConfigurationPoint cp) {
                        for (Contribution contribution : includedSchemaInfo) {
                            if (cp instanceof ConfigurationPointImpl) {
                                ((ConfigurationPointImpl) cp).addDependingContribution(contribution);
                            }
                        }
                    }
                }), true);
            }
        }

        includedSchemaInfoMap = null;
    }

    /** 取得所有的schemas。 */
    public NamespaceItem[] getAllItems() {
        ensureTreeBuilt(false);
        return allItems;
    }

    /** 取得所有独立的schemas。 */
    public NamespaceItem[] getIndependentItems() {
        return getIndependentItems(false);
    }

    /** 取得所有独立的schemas。 */
    public NamespaceItem[] getIndependentItems(boolean includingAllContributions) {
        ensureTreeBuilt(includingAllContributions);
        return includingAllContributions ? treeItemsWithAllContributions : treeItems;
    }

    private synchronized void ensureTreeBuilt(boolean includingAllContributions) {
        if (allItems == null || (includingAllContributions ? treeItemsWithAllContributions : treeItems) == null) {
            TreeBuilder builder = new TreeBuilder().build(includingAllContributions);

            if (allItems == null) {
                allItems = builder.getAllNamespaceItems();
            }

            if (includingAllContributions) {
                if (treeItemsWithAllContributions == null) {
                    this.treeItemsWithAllContributions = builder.getIndependentNamespaceItems();
                }
            } else {
                if (treeItems == null) {
                    this.treeItems = builder.getIndependentNamespaceItems();
                }
            }
        }
    }

    public static interface TreeItem {
        boolean hasChildren();

        TreeItem[] getChildren();
    }

    public static interface NamespaceItem extends TreeItem {
        String getNamespace();

        Set<Schema> getSchemas();
    }

    public static interface ParentOf<C extends TreeItem> {
        Map<String, C> getChildrenMap();
    }

    public static class ConfigurationPointItem extends AbstractNamespaceItem<ContributionItem> {
        private final ConfigurationPoint configurationPoint;

        public ConfigurationPointItem(String namespace, Set<Schema> schemas, ConfigurationPoint configurationPoint) {
            super(ContributionItem.class, namespace, schemas);
            this.configurationPoint = configurationPoint;
        }

        public ConfigurationPoint getConfigurationPoint() {
            return configurationPoint;
        }
    }

    public static class ContributionItem extends AbstractTreeItem<ConfigurationPointItem> {
        private final Contribution contribution;

        public ContributionItem(Contribution contribution) {
            super(ConfigurationPointItem.class);
            this.contribution = contribution;
        }

        public Contribution getContribution() {
            return contribution;
        }

        @Override
        public String toString() {
            return contribution.getName();
        }
    }

    public static class SpringPluggableItem extends AbstractNamespaceItem<TreeItem> {
        public SpringPluggableItem(String namespace, Set<Schema> schemas) {
            super(TreeItem.class, namespace, schemas);
        }
    }

    private static abstract class AbstractTreeItem<C extends TreeItem> implements TreeItem, ParentOf<C> {
        protected final Map<String, C> children = createTreeMap();
        private final Class<C> childType;

        protected AbstractTreeItem(Class<C> childType) {
            this.childType = assertNotNull(childType, "child item type");
        }

        @Override
        public boolean hasChildren() {
            return !children.isEmpty();
        }

        public C[] getChildren() {
            C[] copy = (C[]) Array.newInstance(childType, children.size());
            return children.values().toArray(copy);
        }

        @Override
        public Map<String, C> getChildrenMap() {
            return children;
        }

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

        protected AbstractNamespaceItem(Class<C> childType, String namespace, Set<Schema> schemas) {
            super(childType);
            this.namespace = namespace;
            this.schemas = schemas;
        }

        public String getNamespace() {
            return namespace;
        }

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
        private final Map<String, NamespaceItem> items              = createTreeMap();
        private final Map<String, NamespaceItem> independentItems   = createTreeMap();
        private final LinkedList<String>         buildingNamespaces = createLinkedList();
        private boolean includingAllContributions;

        public TreeBuilder build(boolean includingAllContributions) {
            this.includingAllContributions = includingAllContributions;

            for (String namespace : namespaceMappings.keySet()) {
                buildNamespaceItemRecursively(namespace);
            }

            return this;
        }

        public NamespaceItem[] getAllNamespaceItems() {
            return items.values().toArray(new NamespaceItem[items.size()]);
        }

        public NamespaceItem[] getIndependentNamespaceItems() {
            return independentItems.values().toArray(new NamespaceItem[independentItems.size()]);
        }

        private boolean buildNamespaceItemRecursively(String namespace) {
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

        private void buildNamespaceItem(String namespace) {
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

        private void buildSpringPluggableItem(String namespace, Set<Schema> schemas) {
            NamespaceItem item = createSpringPluggableItem(namespace, schemas);
            items.put(namespace, item);
            independentItems.put(namespace, item);
        }

        private void buildConfigurationPointItem(String namespace, Set<Schema> schemas, ConfigurationPointSchemaSourceInfo schema) {
            ConfigurationPoint configurationPoint = (ConfigurationPoint) schema.getParent();
            ConfigurationPointItem item = createConfigurationPointItem(namespace, schemas, configurationPoint);
            items.put(namespace, item);

            // build contributions
            if (includingAllContributions) {
                for (Contribution contribution : configurationPoint.getContributions()) {
                    ContributionItem contributionItem = createContributionItem(contribution);
                    addChildItem(item, contribution.getName(), contributionItem);
                }
            }

            // build depending contributions
            int count = 0;

            for (Contribution contribution : configurationPoint.getDependingContributions()) {
                String dependingNamespace = contribution.getConfigurationPoint().getNamespaceUri();

                if (buildNamespaceItemRecursively(dependingNamespace)) {
                    count++;
                    NamespaceItem parentItem = assertNotNull(items.get(dependingNamespace), "no item for namespace %s", namespace);

                    if (parentItem instanceof ConfigurationPointItem) {
                        buildContributionItem(namespace, item, contribution, (ConfigurationPointItem) parentItem);
                    }
                }
            }

            if (count == 0) {
                independentItems.put(namespace, item);
            }
        }

        private void buildContributionItem(String namespace, ConfigurationPointItem item, Contribution contribution, ConfigurationPointItem parentItem) {
            String contributionName = contribution.getName();
            ContributionItem parentContributionItem = parentItem.getChildrenMap().get(contributionName);

            if (parentContributionItem == null) {
                parentContributionItem = createContributionItem(contribution);
                addChildItem(parentItem, contributionName, parentContributionItem);
            }

            if (!parentContributionItem.getChildrenMap().containsKey(namespace)) {
                addChildItem(parentContributionItem, namespace, item);
            }
        }
    }

    /** Template method */
    protected ConfigurationPointItem createConfigurationPointItem(String namespace, Set<Schema> schemas, ConfigurationPoint configurationPoint) {
        return new ConfigurationPointItem(namespace, schemas, configurationPoint);
    }

    /** Template method */
    protected ContributionItem createContributionItem(Contribution contribution) {
        return new ContributionItem(contribution);
    }

    /** Template method */
    protected SpringPluggableItem createSpringPluggableItem(String namespace, Set<Schema> schemas) {
        return new SpringPluggableItem(namespace, schemas);
    }

    /** Template method */
    protected <C extends TreeItem> void addChildItem(ParentOf<C> parent, String key, C childItem) {
        parent.getChildrenMap().put(key, childItem);
    }
}
