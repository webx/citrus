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
package com.alibaba.citrus.service.moduleloader.impl.factory;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.internal.regex.ClassNameWildcardCompiler.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AspectJTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;
import com.alibaba.citrus.util.internal.regex.ClassNameWildcardCompiler;

/**
 * 解析class-modules。
 * 
 * @author Michael Zhou
 */
public class ClassModuleFactoryDefinitionParser extends AbstractModuleFactoryDefinitionParser<ClassModuleFactory> {
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        Map<String, ParsingModuleInfo> classes = parseSpecificBeans(element, parserContext,
                builder.getRawBeanDefinition(), and(beansNs(), name("bean")));

        ElementSelector searchPackages = and(sameNs(element), name("search-packages"));
        ElementSelector searchClasses = and(sameNs(element), name("search-classes"));
        ModuleDefinitionScanner scanner = getScanner(parserContext);

        for (Element subElement : subElements(element)) {
            Pattern classNamePattern = null;
            String typeName = null;
            String moduleName = null;
            String classResourceName = null;

            if (searchPackages.accept(subElement)) {
                String packageName = assertNotNull(normalizeClassName(subElement.getAttribute("packages")),
                        "no package name provided for search-packages");

                classNamePattern = compileClassName(packageName, MATCH_PREFIX);
                typeName = assertNotNull(trimToNull(subElement.getAttribute("type")), "no type name provided");
                classResourceName = classNameToPathName(packageName) + "/**/*.class";

                log.trace("Searching in packages: {}, moduleType={}", packageName, typeName);
            } else if (searchClasses.accept(subElement)) {
                String className = assertNotNull(normalizeClassName(subElement.getAttribute("classes")),
                        "no class name provided for search-classes");

                classNamePattern = compileClassName(className, MATCH_PREFIX);
                typeName = assertNotNull(trimToNull(subElement.getAttribute("type")), "no type name provided");
                moduleName = assertNotNull(trimToNull(subElement.getAttribute("name")), "no module name provided");
                classResourceName = classNameToPathName(className);

                if (classResourceName.endsWith("**")) {
                    classResourceName += "/*.class";
                } else {
                    classResourceName += ".class";
                }

                log.trace("Searching for classes: {}, moduleType={}, moduleName={}", new Object[] { className,
                        typeName, moduleName });
            }

            boolean includeAbstractClasses = "true".equalsIgnoreCase(trimToNull(subElement
                    .getAttribute("includeAbstractClasses")));

            if (classResourceName != null) {
                // 处理所有的include/exclude filters
                ClassLoader classLoader = scanner.getResourceLoader().getClassLoader();
                List<TypeFilter> includes = createLinkedList();
                List<TypeFilter> excludes = createLinkedList();

                parseTypeFilters(subElement, classLoader, includes, excludes);

                ModuleTypeFilter filter = new ModuleTypeFilter(classes, classNamePattern, typeName, moduleName,
                        includes);

                // 事实上，只有一个顶级的include filter，其它的include filter被这一个moduleTypeFilter所调用。
                scanner.addIncludeFilter(filter);

                // Exclude filter比较简单，直接加到scanner中即可。
                for (TypeFilter exclude : excludes) {
                    scanner.addExcludeFilter(exclude);
                }

                scanner.setBeanNameGenerator(filter);
                scanner.setResourcePattern(classResourceName.replace('?', '*'));
                scanner.setIncludeAbstractClasses(includeAbstractClasses);
                scanner.setBeanDefinitionDefaults(getBeanDefinitionDefaults(subElement, parserContext));

                int found = scanner.scan("");

                log.debug("Found {} module classes with pattern: {}", found, classResourceName);
            }
        }

        postProcessItems(element, parserContext, builder, classes, "search-packages or search-classes");
    }

    private ModuleDefinitionScanner getScanner(ParserContext parserContext) {
        ModuleDefinitionScanner scanner = new ModuleDefinitionScanner(parserContext.getRegistry());
        scanner.setResourceLoader(parserContext.getReaderContext().getResourceLoader());
        return scanner;
    }

    @Override
    protected String parseItemName(ParserContext parserContext, Element element, BeanDefinition bd) {
        return assertNotNull(bd.getBeanClassName(), "no className provided for bean definition: %s", bd);
    }

    @Override
    protected String getDefaultName() {
        return "classModuleFactory";
    }

    /**
     * 解析<code>TypeFilter</code>s。
     */
    private void parseTypeFilters(Element element, ClassLoader classLoader, List<TypeFilter> includes,
                                  List<TypeFilter> excludes) {
        ElementSelector includeSelector = and(sameNs(element), name("include-filter"));
        ElementSelector excludeSelector = and(sameNs(element), name("exclude-filter"));

        for (Element subElement : subElements(element)) {
            if (includeSelector.accept(subElement)) {
                TypeFilter filter = createTypeFilter(subElement, classLoader);

                if (filter != null) {
                    includes.add(filter);
                }
            } else if (excludeSelector.accept(subElement)) {
                TypeFilter filter = createTypeFilter(subElement, classLoader);

                if (filter != null) {
                    excludes.add(filter);
                }
            }
        }
    }

    /**
     * 创建指定的TypeFilter。
     */
    private TypeFilter createTypeFilter(Element element, ClassLoader classLoader) {
        String filterType = defaultIfNull(trimToNull(element.getAttribute("type")), "wildcard");
        String expression = assertNotNull(trimToNull(element.getAttribute("expression")), "expression for %s"
                + filterType);

        try {
            if ("assignable".equals(filterType)) {
                return new AssignableTypeFilter(classLoader.loadClass(expression));
            } else if ("aspectj".equals(filterType)) {
                return new AspectJTypeFilter(expression, classLoader);
            } else if ("wildcard".equals(filterType)) {
                return new RegexPatternTypeFilter(ClassNameWildcardCompiler.compileClassName(expression,
                        ClassNameWildcardCompiler.MATCH_PREFIX));
            } else if ("custom".equals(filterType)) {
                Class<?> filterClass = classLoader.loadClass(expression);

                assertTrue(TypeFilter.class.isAssignableFrom(filterClass), "Class is not assignable to TypeFilter: %s",
                        expression);

                return (TypeFilter) BeanUtils.instantiateClass(filterClass);
            } else {
                unreachableCode("Unsupported filter type: %s", filterType);
                return null;
            }
        } catch (ClassNotFoundException e) {
            throw new FatalBeanException("Failed to create TypeFilter of type " + filterType + ": " + expression, e);
        }
    }

    private static class ModuleDefinitionScanner extends ClassPathBeanDefinitionScanner {
        private boolean includeAbstractClasses;

        public ModuleDefinitionScanner(BeanDefinitionRegistry registry) {
            super(registry, false);
        }

        public void setIncludeAbstractClasses(boolean includeAbstractClasses) {
            this.includeAbstractClasses = includeAbstractClasses;
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            // 排除内联类
            if (beanDefinition.getMetadata().getClassName().contains("$")) {
                return false;
            }

            // 对于concreteClass，直接返回true
            if (super.isCandidateComponent(beanDefinition)) {
                return true;
            }

            if (!includeAbstractClasses) {
                return false;
            }

            // 非concrete，但指定了includeAbstractClasses=true
            beanDefinition.setBeanClassName(NonInstantiatableClassFactoryBean.class.getName());
            beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0,
                    beanDefinition.getMetadata().getClassName(), Class.class.getName());

            return true;
        }
    }

    private static class ModuleTypeFilter extends ParsingModuleMatcher implements TypeFilter, BeanNameGenerator {
        public ModuleTypeFilter(Map<String, ParsingModuleInfo> classes, Pattern classNamePattern, String typeName,
                                String moduleName, List<TypeFilter> includeFilters) {
            super(classes, classNamePattern, typeName, moduleName, includeFilters);
        }

        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                throws IOException {
            return doMatch(metadataReader, metadataReaderFactory);
        }

        public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
            String className;

            // 从metadata中取得classname，因为对于非concrete类，当includeAbstractClasses=true时，
            // definition.getBeanClassName() == NonInstantiatableObject，而不是最终的类。
            if (definition instanceof AnnotatedBeanDefinition) {
                className = ((AnnotatedBeanDefinition) definition).getMetadata().getClassName();
            } else {
                className = definition.getBeanClassName();
            }

            return generateBeanName(className, registry);
        }
    }

    public static class NonInstantiatableClassFactoryBean implements FactoryBean {
        private final Class<?> clazz;

        public NonInstantiatableClassFactoryBean(Class<?> clazz) {
            this.clazz = assertNotNull(clazz, "class");
        }

        public Object getObject() throws Exception {
            return clazz;
        }

        public Class<?> getObjectType() {
            return Class.class;
        }

        public boolean isSingleton() {
            return true;
        }
    }
}
