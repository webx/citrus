package com.alibaba.citrus.dev.handler.impl;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;

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

public abstract class AbstractExplorerHandler extends LayoutRequestProcessor {
    private final TabsComponent tabsComponent = new TabsComponent(this, "tabs");
    protected final DomComponent domComponent = new DomComponent(this, "dom");

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

        if (!getAvailableFunctions().contains(functionName)) {
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

    protected abstract Set<String> getAvailableFunctions();

    protected abstract String getDefaultFunction();

    protected abstract class AbstractExplorerVisitor extends AbstractVisitor {
        protected final String currentFunctionName;
        protected final String currentContextName;
        protected final WebxComponent currentComponent;
        protected final AbstractApplicationContext appcontext;
        protected final String[] configLocations;

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

        protected String getConfigLocationString() {
            return join(configLocations, ", ");
        }

        public void visitTabs() {
            List<TabItem> tabs = createLinkedList();

            for (String fn : getAvailableFunctions()) {
                TabItem tab = new TabItem(getTabDescription(fn));

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

        public void visitFunctionName() {
            out().print(currentFunctionName);
        }

        public void visitConfigLocations() {
            out().print(getConfigLocationString());
        }

        public final void visitExplorer(Template[] functionTemplates) {
            String[] functions = getAvailableFunctions().toArray(new String[getAvailableFunctions().size()]);

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
                locations[i] = FileUtil.normalizeAbsolutePath(locations[i]);
            }

            return locations;
        }

        private String getTabDescription(String fn) {
            String[] ss = split(toLowerCaseWithUnderscores(fn), "_");
            StringBuilder buf = new StringBuilder();

            for (int i = 0; i < ss.length; i++) {
                if (i > 0) {
                    buf.append(' ');
                }

                buf.append(toPascalCase(ss[i]));
            }

            return buf.toString();
        }
    }

    protected static Field getAccessibleField(Class<?> targetType, String fieldName) throws Exception {
        Field field = null;

        for (Class<?> c = targetType; c != null && field == null; c = c.getSuperclass()) {
            try {
                field = c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            }
        }

        field.setAccessible(true);

        return field;
    }

    protected static Method getAccessibleMethod(Class<?> targetType, String methodName, Class<?>[] argTypes)
            throws Exception {
        Method method = null;

        for (Class<?> c = targetType; c != null && method == null; c = c.getSuperclass()) {
            try {
                method = c.getDeclaredMethod(methodName, argTypes);
            } catch (NoSuchMethodException e) {
            }
        }

        method.setAccessible(true);

        return method;
    }
}
