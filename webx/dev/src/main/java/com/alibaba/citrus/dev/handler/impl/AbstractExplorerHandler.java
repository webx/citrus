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

package com.alibaba.citrus.dev.handler.impl;

import static com.alibaba.citrus.dev.handler.util.ReflectionUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.join;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import java.util.Map;

import com.alibaba.citrus.dev.handler.component.AccessControlComponent;
import com.alibaba.citrus.dev.handler.component.DomComponent;
import com.alibaba.citrus.dev.handler.component.TabsComponent;
import com.alibaba.citrus.dev.handler.component.TabsComponent.TabItem;
import com.alibaba.citrus.util.FileUtil;
import com.alibaba.citrus.util.templatelite.Template;
import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.WebxComponents;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;
import com.alibaba.citrus.webx.handler.support.LayoutRequestProcessor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;

public abstract class AbstractExplorerHandler extends LayoutRequestProcessor {
    protected final TabsComponent          tabsComponent = new TabsComponent(this, "tabs");
    protected final DomComponent           domComponent  = new DomComponent(this, "dom");
    protected final AccessControlComponent aclComponent  = new AccessControlComponent(this, "acl");

    @Autowired
    private WebxComponents components;

    public WebxComponents getWebxComponents() {
        return components;
    }

    @Override
    protected abstract AbstractExplorerVisitor getBodyVisitor(RequestHandlerContext context);

    @Override
    protected String getTitle(Object bodyVisitor) {
        AbstractExplorerVisitor visitor = (AbstractExplorerVisitor) bodyVisitor;
        String contextName = visitor.currentContextName;

        if (contextName == null) {
            return visitor.currentFunctionName + " - Root Context - " + visitor.getConfigLocationString();
        } else {
            return visitor.currentFunctionName + " - " + contextName + " - " + visitor.getConfigLocationString();
        }
    }

    protected String getFunctionName(String functionName) {
        functionName = trimToNull(functionName);

        if (!getAvailableFunctions().containsKey(functionName)) {
            functionName = getDefaultFunction();
        }

        return functionName;
    }

    protected String link(String contextName, String functionName) {
        StringBuilder buf = new StringBuilder("?");

        if (contextName != null) {
            buf.append("context=").append(contextName);
        }

        functionName = getFunctionName(functionName);

        if (!getDefaultFunction().equals(functionName)) {
            if (buf.length() > 1) {
                buf.append("&");
            }

            buf.append("fn=").append(functionName);
        }

        return buf.toString();
    }

    protected abstract Map<String, String> getAvailableFunctions();

    protected abstract String getDefaultFunction();

    protected abstract class AbstractExplorerVisitor extends AbstractVisitor {
        protected final String                     currentFunctionName;
        protected final String                     currentContextName;
        protected final WebxComponent              currentComponent;
        protected final AbstractApplicationContext appcontext;
        protected final String[]                   configLocations;

        public AbstractExplorerVisitor(RequestHandlerContext context) {
            super(context);

            // 取得当前的component信息
            String contextName = trimToNull(context.getRequest().getParameter("context"));
            WebxComponent component = getWebxComponents().getComponent(contextName);

            if (component == null) {
                currentContextName = null;
            } else {
                currentContextName = component.getName();
            }

            currentComponent = getWebxComponents().getComponent(currentContextName);

            // 取得当前的功能
            this.currentFunctionName = getFunctionName(context.getRequest().getParameter("fn"));

            // 取得context信息
            this.appcontext = (AbstractApplicationContext) currentComponent.getApplicationContext();

            // 取得config locations
            String[] locations;

            try {
                locations = normalizeConfigLocations(String[].class.cast(getAccessibleMethod(
                        this.appcontext.getClass(), "getConfigLocations", EMPTY_CLASS_ARRAY).invoke(this.appcontext,
                                                                                                    EMPTY_OBJECT_ARRAY)));
            } catch (Exception e) {
                locations = EMPTY_STRING_ARRAY;
            }

            this.configLocations = locations;
        }

        public AbstractApplicationContext getApplicationContext() {
            return appcontext;
        }

        public TabsComponent getTabsComponent() {
            return tabsComponent;
        }

        public DomComponent getDomComponent() {
            return domComponent;
        }

        protected String getConfigLocationString() {
            return join(configLocations, ", ");
        }

        public void visitTabs() {
            List<TabItem> tabs = createLinkedList();

            for (String fn : getAvailableFunctions().keySet()) {
                TabItem tab = new TabItem(getAvailableFunctions().get(fn));

                tab.setHref(link(currentContextName, fn));
                tab.setSelected(fn.equals(currentFunctionName));
                tabs.add(tab);

                // root context
                TabItem subtab = new TabItem("Root Context");

                subtab.setHref(link(null, fn));
                subtab.setSelected(currentContextName == null);
                tab.addSubTab(subtab);

                // all sub-contexts
                for (String contextName : getWebxComponents().getComponentNames()) {
                    subtab = new TabItem(contextName);

                    subtab.setHref(link(contextName, fn));
                    subtab.setSelected(contextName.equals(currentContextName));
                    tab.addSubTab(subtab);
                }
            }

            tabsComponent.visitTemplate(context, tabs);
        }

        public void visitContextName() {
            out().print(currentContextName == null ? "Root Context" : currentContextName);
        }

        public void visitContextNameForParam() {
            out().print(currentContextName == null ? "" : currentContextName);
        }

        public void visitFunctionName() {
            out().print(currentFunctionName);
        }

        public void visitConfigLocations() {
            out().print(getConfigLocationString());
        }

        public final void visitExplorer(Template[] functionTemplates) {
            if (!aclComponent.accessAllowed(context)) {
                return;
            }

            String[] functions = getAvailableFunctions().keySet().toArray(new String[getAvailableFunctions().size()]);

            for (int i = 0; i < functions.length && i < functionTemplates.length; i++) {
                String function = functions[i];

                if (function.equals(currentFunctionName)) {
                    functionTemplates[i].accept(this);
                }
            }
        }

        protected final <S> S getService(String name, Class<S> type) {
            try {
                return type.cast(appcontext.getBean(name));
            } catch (NoSuchBeanDefinitionException e) {
                return null;
            }
        }

        private String[] normalizeConfigLocations(String[] locations) {
            for (int i = 0; i < locations.length; i++) {
                String location = locations[i];
                int index = location.indexOf(":");

                if (index >= 0) {
                    location = location.substring(0, index + 1)
                               + FileUtil.normalizeAbsolutePath(location.substring(index + 1));
                } else {
                    location = FileUtil.normalizeAbsolutePath(location);
                }

                locations[i] = location;
            }

            return locations;
        }
    }
}
