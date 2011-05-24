package com.alibaba.citrus.dev.handler.impl;

import static com.alibaba.citrus.dev.handler.util.DomUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ExceptionUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import com.alibaba.citrus.dev.handler.component.DomComponent.ControlBarCallback;
import com.alibaba.citrus.dev.handler.util.BeanDefinitionReverseEngine;
import com.alibaba.citrus.dev.handler.util.ConfigurationFile;
import com.alibaba.citrus.dev.handler.util.ConfigurationFileReader;
import com.alibaba.citrus.dev.handler.util.Element;
import com.alibaba.citrus.util.ClassUtil;
import com.alibaba.citrus.util.templatelite.Template;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;

public class SpringExplorerHandler extends AbstractExplorerHandler {
    private static final String FN_BEANS = "Beans";
    private static final String FN_RESOLVABLE_DEPENDENCIES = "ResolvableDependencies";
    private static final String FN_CONFIGURATIONS = "Configurations";
    private static final Set<String> AVAILABLE_FUNCTIONS = createLinkedHashSet(FN_BEANS, FN_CONFIGURATIONS,
            FN_RESOLVABLE_DEPENDENCIES);

    @Override
    protected Set<String> getAvailableFunctions() {
        return AVAILABLE_FUNCTIONS;
    }

    @Override
    protected String getDefaultFunction() {
        return FN_BEANS;
    }

    @Override
    protected SpringExplorerVisitor getBodyVisitor(RequestHandlerContext context) {
        return new SpringExplorerVisitor(context);
    }

    @Override
    protected String[] getStyleSheets() {
        return new String[] { "springExplorer.css" };
    }

    @Override
    protected String[] getJavaScripts() {
        return new String[] { "springExplorer.js" };
    }

    @SuppressWarnings("unused")
    private class SpringExplorerVisitor extends AbstractExplorerVisitor {
        public SpringExplorerVisitor(RequestHandlerContext context) {
            super(context);
        }

        public Object visitBeans(Template beansTemplate) {
            return new BeansVisitor(context, this);
        }

        public Object visitConfigurations(Template configurationsTemplate) throws IOException {
            return new ConfigurationsVisitor(context, this,
                    new ConfigurationFileReader(appcontext, configLocations).toConfigurationFiles());
        }

        public Object visitResolvableDependencies(Template resolvableDepsTemplate) {
            return new ResolvableDepsVisitor(context, this);
        }
    }

    @SuppressWarnings("unused")
    private class BeansVisitor extends AbstractFallbackVisitor {
        private final DefaultListableBeanFactory factory;

        public BeansVisitor(RequestHandlerContext context, SpringExplorerVisitor v) {
            super(context, v);
            this.factory = (DefaultListableBeanFactory) v.appcontext.getBeanFactory();
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

        private RootBeanDefinition getBeanDefinition(String name) throws Exception {
            return (RootBeanDefinition) getAccessibleMethod(factory.getClass(), "getMergedLocalBeanDefinition",
                    new Class<?>[] { String.class }).invoke(factory, name);
        }

        /**
         * Ω´bean names≈≈–Ú°£œ»∞¥bean nameµƒ∏¥‘”∂»≈≈–Ú£¨‘Ÿ∞¥◊÷ƒ∏À≥–Ú≈≈–Ú°£
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
    private class ConfigurationsVisitor extends AbstractFallbackVisitor {
        private final ConfigurationFile[] configurationFiles;
        private ConfigurationFile configurationFile;

        public ConfigurationsVisitor(RequestHandlerContext context, SpringExplorerVisitor v,
                                     ConfigurationFile[] configurationFiles) {
            super(context, v);
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

        public void visitImports(Template withImportsTemplate, Template noImportsTemplate) throws IOException {
            new ConfigurationsVisitor(context, (SpringExplorerVisitor) ftv.getVisitor(),
                    configurationFile.getImportedFiles()).visitConfigurations(withImportsTemplate, noImportsTemplate);
        }

        public void visitConfigurationContent(final Template controlBarTemplate) {
            domComponent.visitTemplate(context, singletonList(configurationFile.getRootElement()),
                    new ControlBarCallback() {
                        public void renderControlBar() {
                            controlBarTemplate.accept(ConfigurationsVisitor.this);
                        }
                    });
        }
    }

    @SuppressWarnings("unused")
    private class ResolvableDepsVisitor extends AbstractFallbackVisitor {
        private final Map<Class<?>, Object> resolvableDependencies;
        private Class<?> type;
        private Object value;

        public ResolvableDepsVisitor(RequestHandlerContext context, SpringExplorerVisitor v) {
            super(context, v);
            this.resolvableDependencies = getResolvableDependencies((DefaultListableBeanFactory) v.appcontext
                    .getBeanFactory());
        }

        @SuppressWarnings("unchecked")
        private Map<Class<?>, Object> getResolvableDependencies(DefaultListableBeanFactory factory) {
            Map<Class<?>, Object> deps;

            try {
                deps = (Map<Class<?>, Object>) getAccessibleField(factory.getClass(), "resolvableDependencies").get(
                        factory);
            } catch (Exception e) {
                deps = createHashMap();
            }

            return deps;
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
                out().print(ClassUtil.getSimpleClassName(value.getClass(), false));
            }
        }

        public void visitValue() {
            out().print(escapeHtml(String.valueOf(value)));
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
