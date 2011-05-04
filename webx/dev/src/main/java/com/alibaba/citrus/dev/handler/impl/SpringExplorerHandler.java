package com.alibaba.citrus.dev.handler.impl;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
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
import com.alibaba.citrus.webx.handler.component.KeyValuesComponent;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;
import com.alibaba.citrus.webx.handler.support.LayoutRequestProcessor;

public class SpringExplorerHandler extends LayoutRequestProcessor {
    private final KeyValuesComponent keyValuesComponent = new KeyValuesComponent(this, "keyValues");

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
            return "Spring Explorer - Root Context";
        } else {
            return "Spring Explorer - Sub-context: " + contextName;
        }
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
        private final AbstractApplicationContext appcontext;
        private final DefaultListableBeanFactory factory;
        private final Map<Class<?>, Object> resolvableDependencies;
        private String contextName;

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

        public void visitContexts(Template contextTemplate) {
            for (String name : components.getComponentNames()) {
                contextName = name;
                contextTemplate.accept(this);
            }
        }

        public void visitContextName() {
            out().print(escapeHtml(contextName));
        }

        public void visitResolvableDependencyCount() {
            out().print(resolvableDependencies.size());
        }

        public void visitResolvableDependency(Template resolvableDependencyTemplate) {
            Map<String, Object> keyValues = createTreeMap();

            for (Map.Entry<Class<?>, Object> entry : resolvableDependencies.entrySet()) {
                keyValues.put(entry.getKey().getName(), entry.getValue());
            }

            keyValuesComponent.visitTemplate(context, keyValues);
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

        public BeanVisitor(RequestHandlerContext context, RootBeanDefinition bd, String name, String[] aliases) {
            super(context);
            this.beanElement = new BeanDefinitionReverseEngine(bd, name, aliases).toXml();
        }

        public void visitElement(Template elementTemplate, Template selfClosedElementTemplate,
                                 Template textElementTemplate) {
            new ElementVisitor(context, beanElement, elementTemplate, selfClosedElementTemplate, textElementTemplate)
                    .visitElement();
        }
    }

    @SuppressWarnings("unused")
    private class ElementVisitor extends AbstractVisitor {
        private final Element element;
        private final Template elementTemplate;
        private final Template selfClosedElementTemplate;
        private final Template textElementTemplate;
        private Attribute attr;

        public ElementVisitor(RequestHandlerContext context, Element element, Template elementTemplate,
                              Template selfClosedElementTemplate, Template textElementTemplate) {
            super(context);
            this.element = element;
            this.elementTemplate = elementTemplate;
            this.selfClosedElementTemplate = selfClosedElementTemplate;
            this.textElementTemplate = textElementTemplate;
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
                new ElementVisitor(context, subElement, elementTemplate, selfClosedElementTemplate, textElementTemplate)
                        .visitElement();
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
