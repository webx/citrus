package com.alibaba.citrus.dev.handler.impl;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.AbstractApplicationContext;

import com.alibaba.citrus.dev.handler.impl.BeanDefinitionReverseEngine.Attribute;
import com.alibaba.citrus.dev.handler.impl.BeanDefinitionReverseEngine.Element;
import com.alibaba.citrus.util.internal.templatelite.Template;
import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.WebxComponents;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.component.TabsComponent;
import com.alibaba.citrus.webx.handler.component.TabsComponent.TabItem;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;
import com.alibaba.citrus.webx.handler.support.LayoutRequestProcessor;

public class SpringExplorerHandler extends LayoutRequestProcessor {
    private static final String FN_BEANS = "Beans";
    private static final String FN_RESOLVABLE_DEPENDENCIES = "ResolvableDependencies";
    private final TabsComponent tabsComponent = new TabsComponent(this, "tabs");

    @Autowired
    private WebxComponents components;

    @Override
    protected Object getBodyVisitor(RequestHandlerContext context) {
        return new SpringExplorerVisitor(context);
    }

    @Override
    protected String getTitle(Object bodyVisitor) {
        SpringExplorerVisitor visitor = (SpringExplorerVisitor) bodyVisitor;
        String contextName = visitor.currentContextName;

        if (contextName == null) {
            return visitor.currentFunctionName + " - Root Context";
        } else {
            return visitor.currentFunctionName + " - " + contextName;
        }
    }

    @Override
    protected String[] getStyleSheets() {
        return new String[] { "springExplorer.css" };
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
            String functionName = trimToNull(context.getRequest().getParameter("fn"));

            if (FN_RESOLVABLE_DEPENDENCIES.equals(functionName)) {
                currentFunctionName = functionName;
            } else {
                currentFunctionName = FN_BEANS;
            }

            // 取得context信息
            this.appcontext = (AbstractApplicationContext) currentComponent.getApplicationContext();
            this.factory = (DefaultListableBeanFactory) appcontext.getBeanFactory();

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

        public void visitTabs() {
            List<TabItem> tabs = createLinkedList();

            // list beans
            TabItem tab = new TabItem("Beans");

            tab.setHref(link(currentContextName, FN_BEANS));
            tab.setSelected(FN_BEANS.equals(currentFunctionName));
            tabs.add(tab);

            addSubTabs(tab, FN_BEANS);

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

            if (!FN_RESOLVABLE_DEPENDENCIES.equals(functionName)) {
                functionName = FN_BEANS; // default function         
            }

            if (!FN_BEANS.equals(functionName)) {
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

        public void visitResolvableDependencies(Template resolvableDependenciesTemplate) {
            if (FN_RESOLVABLE_DEPENDENCIES.equals(currentFunctionName)) {
                resolvableDependenciesTemplate.accept(this);
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
            out().print(type.getSimpleName());
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
                out().print(value.getClass().getSimpleName());
            }
        }

        public void visitValue() {
            out().print(escapeHtml(String.valueOf(value)));
        }

        public void visitBeans(Template beansTemplate) {
            if (FN_BEANS.equals(currentFunctionName)) {
                beansTemplate.accept(this);
            }
        }

        public void visitBeanCount() {
            out().print(factory.getBeanDefinitionCount());
        }

        public void visitBean(Template beanTemplate) {
            for (String name : getSortedBeanNames()) {
                RootBeanDefinition bd;

                try {
                    bd = getBeanDefinition(name);
                    beanTemplate.accept(new BeanVisitor(context, bd, name, factory.getAliases(name)));
                } catch (Exception e) {
                    out().print(escapeHtml(e.toString()));
                }
            }
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

        private class BeanName implements Comparable<BeanName> {
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

    @SuppressWarnings("unused")
    private class BeanVisitor extends AbstractVisitor {
        private final Element beanElement;
        private Template elementStartTemplate;

        public BeanVisitor(RequestHandlerContext context, RootBeanDefinition bd, String name, String[] aliases) {
            super(context);
            this.beanElement = new BeanDefinitionReverseEngine(bd, name, aliases).toXml();
        }

        public void visitElementStart(Template elementStartTemplate) {
            this.elementStartTemplate = elementStartTemplate;
        }

        public void visitElement(Template elementTemplate, Template selfClosedElementTemplate,
                                 Template textElementTemplate) {
            new ElementVisitor(context, beanElement, elementStartTemplate, elementTemplate, selfClosedElementTemplate,
                    textElementTemplate).visitElement();
        }
    }

    @SuppressWarnings("unused")
    private class ElementVisitor extends AbstractVisitor {
        private final Element element;
        private final Template elementStartTemplate;
        private final Template elementTemplate;
        private final Template selfClosedElementTemplate;
        private final Template textElementTemplate;
        private Attribute attr;

        public ElementVisitor(RequestHandlerContext context, Element element, Template elementStartTemplate,
                              Template elementTemplate, Template selfClosedElementTemplate, Template textElementTemplate) {
            super(context);
            this.element = element;
            this.elementStartTemplate = elementStartTemplate;
            this.elementTemplate = elementTemplate;
            this.selfClosedElementTemplate = selfClosedElementTemplate;
            this.textElementTemplate = textElementTemplate;
        }

        public void visitElementStart() {
            elementStartTemplate.accept(this);
        }

        public void visitElementName() {
            out().print(escapeHtml(element.getName()));
        }

        public void visitAttribute(Template attributeTemplate) {
            for (Attribute attr : element.attributes()) {
                this.attr = attr;
                attributeTemplate.accept(this);
            }
        }

        public void visitAttributeKey() {
            out().print(escapeHtml(attr.getKey()));
        }

        public void visitAttributeValue() {
            out().print(escapeHtml(attr.getValue()));
        }

        public void visitSubElements() {
            for (Element subElement : element.subElements()) {
                new ElementVisitor(context, subElement, elementStartTemplate, elementTemplate,
                        selfClosedElementTemplate, textElementTemplate).visitElement();
            }
        }

        public void visitElementText() {
            out().print(escapeHtml(element.getText()));
        }

        private void visitElement() {
            if (element.hasSubElements()) {
                elementTemplate.accept(this);
            } else if (!isEmpty(element.getText())) {
                textElementTemplate.accept(this);
            } else {
                selfClosedElementTemplate.accept(this);
            }
        }
    }
}
