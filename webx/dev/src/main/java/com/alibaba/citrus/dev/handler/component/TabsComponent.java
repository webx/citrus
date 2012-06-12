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

package com.alibaba.citrus.dev.handler.component;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;

import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.util.templatelite.Template;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;

/**
 * 用来显示两级tabs的组件。
 *
 * @author Michael Zhou
 */
public class TabsComponent extends PageComponent {
    public TabsComponent(PageComponentRegistry registry, String componentPath) {
        super(registry, componentPath);
    }

    public void visitTemplate(RequestHandlerContext context, List<TabItem> tabs) {
        getTemplate().accept(new TabsVisitor(context, tabs));
    }

    private class TabsVisitor extends AbstractVisitor {
        private final List<TabItem> tabs;

        public TabsVisitor(RequestHandlerContext context, List<TabItem> tabs) {
            super(context, TabsComponent.this);
            this.tabs = assertNotNull(tabs, "tabs");
        }

        public void visitTab(Template tabTemplate) {
            for (TabItem tab : tabs) {
                tabTemplate.accept(new TabVisitor(context, tab));
            }
        }
    }

    @SuppressWarnings("unused")
    private class TabVisitor extends AbstractVisitor {
        private final TabItem tab;

        public TabVisitor(RequestHandlerContext context, TabItem tab) {
            super(context, TabsComponent.this);
            this.tab = assertNotNull(tab, "tab");
        }

        public void visitSelectedAttr(Template selectedTemplate) {
            if (tab.isSelected()) {
                selectedTemplate.accept(this);
            }
        }

        public void visitLinkAttrs(Template hrefTemplate, Template onclickTemplate) {
            if (tab.getHref() != null) {
                hrefTemplate.accept(this);
            }

            if (tab.getOnclick() != null) {
                onclickTemplate.accept(this);
            }
        }

        public void visitHrefLink() {
            out().print(escapeHtml(tab.getHref()));
        }

        public void visitOnclickScript() {
            out().print(escapeHtml(tab.getOnclick()));
        }

        public void visitDesc() {
            if (tab.getEscapedDesc() != null) {
                out().print(tab.getEscapedDesc());
            }
        }

        public void visitSubnav(Template subnavTemplate) {
            if (!tab.getSubTabs().isEmpty()) {
                subnavTemplate.accept(this);
            }
        }

        public void visitSubtabs(Template tabTemplate) {
            new TabsVisitor(context, tab.getSubTabs()).visitTab(tabTemplate);
        }
    }

    public static class TabItem {
        private final String  escapedDesc;
        private       String  href;
        private       String  onclick;
        private       boolean selected;
        private final List<TabItem> subtabs = createLinkedList();

        public TabItem(String desc) {
            this(desc, false);
        }

        public TabItem(String desc, boolean escaped) {
            if (escaped) {
                this.escapedDesc = trimToNull(desc);
            } else {
                this.escapedDesc = escapeHtml(trimToNull(desc));
            }
        }

        public String getEscapedDesc() {
            return escapedDesc;
        }

        public String getHref() {
            return trimToNull(href);
        }

        public void setHref(String href) {
            this.href = trimToNull(href);
        }

        public String getOnclick() {
            return onclick;
        }

        public void setOnclick(String onclick) {
            this.onclick = trimToNull(onclick);
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public List<TabItem> getSubTabs() {
            return subtabs;
        }

        public void addSubTab(TabItem tab) {
            subtabs.add(tab);
        }
    }
}
