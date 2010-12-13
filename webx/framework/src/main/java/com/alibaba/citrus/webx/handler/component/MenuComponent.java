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
package com.alibaba.citrus.webx.handler.component;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.util.internal.templatelite.Template;
import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.RequestHandlerMapping;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;

/**
 * 用来显示可用handler菜单的组件。
 * 
 * @author Michael Zhou
 */
public class MenuComponent extends PageComponent {
    @Autowired
    private RequestHandlerMapping internalHandlers;

    public MenuComponent(PageComponentRegistry registry, String componentPath) {
        super(registry, componentPath);
    }

    public void visitTemplate(RequestHandlerContext context, String selection) {
        getTemplate().accept(new MenuVisitor(context, createRootEntry(), selection));
    }

    private Entry createRootEntry() {
        Entry root = new Entry();

        for (String name : internalHandlers.getRequestHandlerNames()) {
            Entry entry = root;
            int index = name.indexOf('/');

            for (; index >= 0; index = name.indexOf('/', index + 1)) {
                String catName = name.substring(0, index + 1); // 例如：cat1/cat2/
                entry = entry.getOrCreateSubEntry(catName);
            }

            entry.getOrCreateSubEntry(name); // 例如：cat1/cat2/item1
        }

        return root;
    }

    @SuppressWarnings("unused")
    private class MenuVisitor extends AbstractVisitor {
        private final String selection;
        private final Entry rootEntry;

        public MenuVisitor(RequestHandlerContext context, Entry rootEntry, String selection) {
            super(context, MenuComponent.this);
            this.selection = selection;
            this.rootEntry = rootEntry;
        }

        public void visitEntry(Template homeTemplate, Template catTemplate, Template itemTemplate) {
            homeTemplate.accept(this);

            for (Entry subEntry : rootEntry.getSubEntries()) {
                if (subEntry.isCategory()) {
                    catTemplate.accept(new CatEntryVisitor(context, subEntry, catTemplate, itemTemplate));
                } else {
                    itemTemplate.accept(new ItemEntryVisitor(context, subEntry, catTemplate, itemTemplate));
                }
            }
        }

        public void visitInternalHomePage() {
            out().print(context.getInternalResourceURL("/"));
        }

        public void visitOriginalHomePage() {
            out().print(context.getInternalResourceURL("../") + "?home");
        }
    }

    private abstract class AbstractEntryVisitor extends AbstractVisitor {
        protected final Template catTemplate;
        protected final Template itemTemplate;
        protected final Entry entry;

        public AbstractEntryVisitor(RequestHandlerContext context, Entry entry, Template catTemplate,
                                    Template itemTemplate) {
            super(context, MenuComponent.this);
            this.catTemplate = catTemplate;
            this.itemTemplate = itemTemplate;
            this.entry = entry;
        }
    }

    @SuppressWarnings("unused")
    private class CatEntryVisitor extends AbstractEntryVisitor {
        public CatEntryVisitor(RequestHandlerContext context, Entry entry, Template catTemplate, Template itemTemplate) {
            super(context, entry, catTemplate, itemTemplate);
        }

        public void visitCatName() {
            out().print(entry.getName());
        }

        public void visitSubEntries(Template noSubEntriesTemplate, Template withSubEntriesTemplate) {
            if (entry.getSubEntries().isEmpty()) {
                noSubEntriesTemplate.accept(this);
            } else {
                withSubEntriesTemplate.accept(this);
            }
        }

        public void visitSubEntriesRecursive() {
            for (Entry subEntry : entry.getSubEntries()) {
                if (subEntry.isCategory()) {
                    catTemplate.accept(new CatEntryVisitor(context, subEntry, catTemplate, itemTemplate));
                } else {
                    itemTemplate.accept(new ItemEntryVisitor(context, subEntry, catTemplate, itemTemplate));
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private class ItemEntryVisitor extends AbstractEntryVisitor {
        public ItemEntryVisitor(RequestHandlerContext context, Entry entry, Template catTemplate, Template itemTemplate) {
            super(context, entry, catTemplate, itemTemplate);
        }

        public void visitItemUrl() {
            out().print(context.getInternalResourceURL(entry.getPath()));
        }

        public void visitItemName() {
            out().print(entry.getName());
        }
    }

    @SuppressWarnings("unused")
    private static class Entry {
        private final String path;
        private final String name;
        private final boolean category;
        private final boolean root;
        private Map<String, Entry> subEntries;

        /**
         * 创建特殊的root entry。
         */
        private Entry() {
            this.path = "";
            this.name = "";
            this.category = true;
            this.root = true;
            this.subEntries = createTreeMap();
        }

        public Entry(String path) {
            this.path = path;
            this.category = path.endsWith("/");
            this.name = getName(path);
            this.root = false;
            this.subEntries = createTreeMap();
        }

        private String getName(String path) {
            int fromIndex = this.category ? path.length() - 2 : path.length();

            try {
                return unescapeURL(
                        path.substring(path.lastIndexOf("/", fromIndex) + 1, path.length() - (this.category ? 1 : 0)),
                        "UTF-8");
            } catch (UnsupportedEncodingException e) {
                unreachableCode();
                return null;
            }
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public boolean isCategory() {
            return category;
        }

        public boolean isRoot() {
            return root;
        }

        public Entry getOrCreateSubEntry(String path) {
            Entry subEntry = subEntries.get(path);

            if (subEntry == null) {
                subEntry = new Entry(path);
                subEntries.put(path, subEntry);
            }

            return subEntry;
        }

        public Collection<Entry> getSubEntries() {
            return subEntries.values();
        }

        @Override
        public String toString() {
            return root ? "/" : path;
        }
    }
}
