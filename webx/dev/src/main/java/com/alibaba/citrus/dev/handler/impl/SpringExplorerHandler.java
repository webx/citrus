package com.alibaba.citrus.dev.handler.impl;

import static com.alibaba.citrus.dev.handler.util.DomUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ExceptionUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.AbstractApplicationContext;

import com.alibaba.citrus.dev.handler.component.DomComponent;
import com.alibaba.citrus.dev.handler.component.DomComponent.ControlBarCallback;
import com.alibaba.citrus.dev.handler.component.TabsComponent;
import com.alibaba.citrus.dev.handler.component.TabsComponent.TabItem;
import com.alibaba.citrus.dev.handler.util.BeanDefinitionReverseEngine;
import com.alibaba.citrus.dev.handler.util.ConfigurationFile;
import com.alibaba.citrus.dev.handler.util.ConfigurationFileReader;
import com.alibaba.citrus.dev.handler.util.Element;
import com.alibaba.citrus.util.ClassUtil;
import com.alibaba.citrus.util.FileUtil;
import com.alibaba.citrus.util.templatelite.Template;
import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.WebxComponents;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;
import com.alibaba.citrus.webx.handler.support.LayoutRequestProcessor;

public class SpringExplorerHandler extends LayoutRequestProcessor {
    private static final String FN_BEANS = "Beans";
    private static final String FN_RESOLVABLE_DEPENDENCIES = "ResolvableDependencies";
    private static final String FN_CONFIGURATIONS = "Configurations";
    private static final String FN_DEFAULT = FN_BEANS;
    private static final Set<String> AVAILABLE_FUNCTIONS = createHashSet(FN_BEANS, FN_RESOLVABLE_DEPENDENCIES,
            FN_CONFIGURATIONS);

    private final TabsComponent tabsComponent = new TabsComponent(this, "tabs");
    private final DomComponent domComponent = new DomComponent(this, "dom");

    @Autowired
    private WebxComponents components;

    private static String getFunctionName(String functionName) {
        functionName = trimToNull(functionName);

        if (!AVAILABLE_FUNCTIONS.contains(functionName)) {
            functionName = FN_DEFAULT;
        }

        return functionName;
    }

    @Override
    protected Object getBodyVisitor(RequestHandlerContext context) {
        return new SpringExplorerVisitor(context);
    }

    @Override
    protected String getTitle(Object bodyVisitor) {
        SpringExplorerVisitor visitor = (SpringExplorerVisitor) bodyVisitor;
        String contextName = visitor.currentContextName;

        if (contextName == null) {
            return visitor.currentFunctionName + " - Root Context - " + visitor.getConfigLocationString();
        } else {
            return visitor.currentFunctionName + " - " + contextName + " - " + visitor.getConfigLocationString();
        }
    }

    @Override
    protected String[] getStyleSheets() {
        return new String[] { "springExplorer.css" };
    }

    @Override
    protected String[] getJavaScripts() {
        return new String[] { "springExplorer.js" };
    }

    private static Field getAccessibleField(Class<?> targetType, String fieldName) throws Exception {
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

    private static Method getAccessibleMethod(Class<?> targetType, String methodName, Class<?>[] argTypes)
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

    @SuppressWarnings("unused")
    private class SpringExplorerVisitor extends AbstractVisitor {
        private final String currentContextName;
        private final WebxComponent currentComponent;
        private final String currentFunctionName;
        private final AbstractApplicationContext appcontext;
        private final String[] configLocations;
        private final DefaultListableBeanFactory factory;
        private final Map<Class<?>, Object> resolvableDependencies;
        private Class<?> type;
        private Object value;

        public SpringExplorerVisitor(RequestHandlerContext context) {
            super(context);

            // 取得当前的component信息
            String contextName = trimToNull(context.getRequest().getParameter("context"));
            WebxComponent component = components.getComponent(contextName);

            if (component == null) {
                currentContextName = null;
            } else {
                currentContextName = component.getName();
            }

            currentComponent = components.getComponent(currentContextName);

            // 取得当前的功能
            this.currentFunctionName = getFunctionName(context.getRequest().getParameter("fn"));

            // 取得context信息
            this.appcontext = (AbstractApplicationContext) currentComponent.getApplicationContext();
            this.factory = (DefaultListableBeanFactory) appcontext.getBeanFactory();

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

            // 取得resolvableDependencies
            this.resolvableDependencies = getResolvableDependencies();
        }

        @SuppressWarnings("unchecked")
        private Map<Class<?>, Object> getResolvableDependencies() {
            Map<Class<?>, Object> deps;

            try {
                deps = (Map<Class<?>, Object>) getAccessibleField(factory.getClass(), "resolvableDependencies").get(
                        factory);
            } catch (Exception e) {
                deps = createHashMap();
            }

            return deps;
        }

        private String getConfigLocationString() {
            return join(configLocations, ", ");
        }

        private String[] normalizeConfigLocations(String[] locations) {
            for (int i = 0; i < locations.length; i++) {
                locations[i] = FileUtil.normalizeAbsolutePath(locations[i]);
            }

            return locations;
        }

        public void visitTabs() {
            List<TabItem> tabs = createLinkedList();

            // list beans
            TabItem tab = new TabItem("Beans");

            tab.setHref(link(currentContextName, FN_BEANS));
            tab.setSelected(FN_BEANS.equals(currentFunctionName));
            tabs.add(tab);

            addSubTabs(tab, FN_BEANS);

            // list configurations
            tab = new TabItem("Configurations");

            tab.setHref(link(currentContextName, FN_CONFIGURATIONS));
            tab.setSelected(FN_CONFIGURATIONS.equals(currentFunctionName));
            tabs.add(tab);

            addSubTabs(tab, FN_CONFIGURATIONS);

            // list resolvable dependencies
            tab = new TabItem("Resolvable Dependencies");

            tab.setHref(link(currentContextName, FN_RESOLVABLE_DEPENDENCIES));
            tab.setSelected(FN_RESOLVABLE_DEPENDENCIES.equals(currentFunctionName));
            tabs.add(tab);

            addSubTabs(tab, FN_RESOLVABLE_DEPENDENCIES);

            tabsComponent.visitTemplate(context, tabs);
        }

        private void addSubTabs(TabItem tab, String functionName) {
            // root context
            TabItem subtab = new TabItem("Root Context");

            subtab.setHref(link(null, functionName));
            subtab.setSelected(currentContextName == null);
            tab.addSubTab(subtab);

            // all sub-contexts
            for (String contextName : components.getComponentNames()) {
                subtab = new TabItem(contextName);

                subtab.setHref(link(contextName, functionName));
                subtab.setSelected(contextName.equals(currentContextName));
                tab.addSubTab(subtab);
            }
        }

        private String link(String contextName, String functionName) {
            StringBuilder buf = new StringBuilder("?");

            if (contextName != null) {
                buf.append("context=").append(contextName);
            }

            functionName = getFunctionName(functionName);

            if (!FN_DEFAULT.equals(functionName)) {
                if (buf.length() > 1) {
                    buf.append("&");
                }

                buf.append("fn=").append(functionName);
            }

            return buf.toString();
        }

        public void visitContextName() {
            out().print(currentContextName == null ? "Root Context" : currentContextName);
        }

        public void visitConfigLocations() {
            out().print(getConfigLocationString());
        }

        public void visitExplorer(Template resolvableDependenciesTemplate, Template beansTemplate,
                                  Template configurationsTemplate) {
            if (FN_RESOLVABLE_DEPENDENCIES.equals(currentFunctionName)) {
                resolvableDependenciesTemplate.accept(this);
            }

            if (FN_BEANS.equals(currentFunctionName)) {
                beansTemplate.accept(this);
            }

            if (FN_CONFIGURATIONS.equals(currentFunctionName)) {
                configurationsTemplate.accept(this);
            }
        }

        public void visitResolvableDependencyCount() {
            out().print(resolvableDependencies.size());
        }

        public void visitResolvableDependency(Template resolvableDependencyTemplate) {
            Map<Class<?>, Object> sorted = createTreeMap(new Comparator<Class<?>>() {
                public int compare(Class<?> o1, Class<?> o2) {
                    int n1 = countMatches(o1.getName(), ".");
                    int n2 = countMatches(o2.getName(), ".");

                    if (n1 != n2) {
                        return n1 - n2;
                    }

                    if (!o1.getPackage().getName().equals(o2.getPackage().getName())) {
                        return o1.getPackage().getName().compareTo(o2.getPackage().getName());
                    }

                    return o1.getSimpleName().compareTo(o2.getSimpleName());
                }
            });

            sorted.putAll(resolvableDependencies);

            for (Map.Entry<Class<?>, Object> entry : sorted.entrySet()) {
                type = entry.getKey();
                value = entry.getValue();

                resolvableDependencyTemplate.accept(this);
            }
        }

        public void visitTypePackage() {
            if (type.getPackage() != null) {
                out().print(type.getPackage().getName() + ".");
            }
        }

        public void visitTypeName() {
            out().print(ClassUtil.getSimpleClassName(type));
        }

        public void visitVarName() {
            out().print(toCamelCase(type.getSimpleName()));
        }

        public void visitValueTypePackage() {
            if (value != null && value.getClass().getPackage() != null) {
                out().print(value.getClass().getPackage().getName() + ".");
            }
        }

        public void visitValueTypeName() {
            if (value != null) {
                out().print(ClassUtil.getSimpleClassName(value.getClass()));
            }
        }

        public void visitValue() {
            out().print(escapeHtml(String.valueOf(value)));
        }

        public void visitBeanCount() {
            out().print(factory.getBeanDefinitionCount());
        }

        public void visitBeans() {
            List<Element> elements = createLinkedList();

            for (String name : getSortedBeanNames()) {
                Element beanElement = null;

                try {
                    RootBeanDefinition bd = getBeanDefinition(name);
                    beanElement = new BeanDefinitionReverseEngine(bd, name, factory.getAliases(name)).toDom();
                } catch (Exception e) {
                    beanElement = new Element("bean").setText(getStackTrace(getRootCause(e)));
                }

                if (beanElement != null) {
                    elements.add(beanElement);
                }
            }

            domComponent.visitTemplate(context, elements);
        }

        public void visitConfigurations(Template configurationsTemplate) throws IOException {
            configurationsTemplate.accept(new ConfigurationFilesVisitor(context, new ConfigurationFileReader(
                    appcontext, configLocations).toConfigurationFiles()));
        }

        private RootBeanDefinition getBeanDefinition(String name) throws Exception {
            return (RootBeanDefinition) getAccessibleMethod(factory.getClass(), "getMergedLocalBeanDefinition",
                    new Class<?>[] { String.class }).invoke(factory, name);
        }

        /**
         * 将bean names排序。先按bean name的复杂度排序，再按字母顺序排序。
         */
        private String[] getSortedBeanNames() {
            String[] names = factory.getBeanDefinitionNames();
            BeanName[] beanNames = new BeanName[names.length];

            for (int i = 0; i < names.length; i++) {
                beanNames[i] = new BeanName();
                beanNames[i].beanName = names[i];
                beanNames[i].components = split(names[i], ".");
            }

            Arrays.sort(beanNames);

            names = new String[beanNames.length];

            for (int i = 0; i < beanNames.length; i++) {
                names[i] = beanNames[i].beanName;
            }

            return names;
        }
    }

    @SuppressWarnings("unused")
    private class ConfigurationFilesVisitor extends AbstractVisitor {
        private final ConfigurationFile[] configurationFiles;
        private ConfigurationFile configurationFile;

        public ConfigurationFilesVisitor(RequestHandlerContext context, ConfigurationFile[] configurationFiles) {
            super(context);
            this.configurationFiles = configurationFiles;
        }

        public void visitConfigurations(Template withImportsTemplate, Template noImportsTemplate) throws IOException {
            for (ConfigurationFile configurationFile : configurationFiles) {
                this.configurationFile = configurationFile;

                if (isEmptyArray(configurationFile.getImportedFiles())) {
                    noImportsTemplate.accept(this);
                } else {
                    withImportsTemplate.accept(this);
                }
            }
        }

        public void visitConfigurationName() {
            out().print(configurationFile.getName());
        }

        public void visitConfigurationNameForId() {
            out().print(toId(configurationFile.getName()));
        }

        public void visitConfigurationUrl() {
            out().print(configurationFile.getUrl().toExternalForm());
        }

        public void visitImports(Template configurationsTemplate) {
            configurationsTemplate.accept(new ConfigurationFilesVisitor(context, configurationFile.getImportedFiles()));
        }

        public void visitConfigurationContent(final Template controlBarTemplate) {
            domComponent.visitTemplate(context, singletonList(configurationFile.getRootElement()),
                    new ControlBarCallback() {
                        public void renderControlBar() {
                            controlBarTemplate.accept(ConfigurationFilesVisitor.this);
                        }
                    });
        }
    }

    private static class BeanName implements Comparable<BeanName> {
        private String beanName;
        private String[] components;

        public int compareTo(BeanName o) {
            if (components.length != o.components.length) {
                return components.length - o.components.length;
            }

            for (int i = 0; i < components.length; i++) {
                String comp1 = components[i];
                String comp2 = o.components[i];

                if (!comp1.equals(comp2)) {
                    return comp1.compareTo(comp2);
                }
            }

            return 0;
        }
    }
}
