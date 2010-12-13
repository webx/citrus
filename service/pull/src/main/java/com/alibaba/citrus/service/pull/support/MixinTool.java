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
package com.alibaba.citrus.service.pull.support;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pull.ToolFactory;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.util.internal.StaticFunctionDelegatorBuilder;

/**
 * 或者创建一组utils类的mixin。
 * 
 * @author Michael Zhou
 */
public class MixinTool implements ToolFactory, ResourceLoaderAware {
    private ClassLoader classLoader;
    private Class<?>[] mixinClasses;
    private MethodInfo[] mixinMethods;

    public void setResourceLoader(ResourceLoader resourceLoader) {
        classLoader = resourceLoader.getClassLoader();
    }

    public void setMixinClasses(Class<?>[] mixinClasses) {
        this.mixinClasses = mixinClasses;
    }

    public void setMixinMethods(MethodInfo[] mixinMethods) {
        this.mixinMethods = mixinMethods;
    }

    public boolean isSingleton() {
        return true;
    }

    public Object createTool() throws Exception {
        StaticFunctionDelegatorBuilder builder = new StaticFunctionDelegatorBuilder();

        builder.setClassLoader(classLoader);

        // add classes
        if (mixinClasses != null) {
            for (Class<?> mixinClass : mixinClasses) {
                builder.addClass(mixinClass);
            }
        }

        // add methods
        if (mixinMethods != null) {
            for (MethodInfo methodInfo : mixinMethods) {
                for (Method method : methodInfo.containingClass.getMethods()) {
                    String methodName = method.getName();

                    if (methodName.equals(methodInfo.methodName)) {
                        builder.addMethod(method, methodInfo.rename);
                    }
                }
            }
        }

        return builder.toObject();
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<MixinTool> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            List<Object> classes = createManagedList(element, parserContext);
            List<Object> methods = createManagedList(element, parserContext);

            for (Element classElement : subElements(element, and(sameNs(element), name("class")))) {
                String className = assertNotNull(trimToNull(classElement.getAttribute("name")), "mixin class name");
                List<Element> methodElements = subElements(classElement, and(sameNs(classElement), name("method")));

                if (methodElements.isEmpty()) {
                    classes.add(className);
                } else {
                    for (Element methodElement : methodElements) {
                        BeanDefinitionBuilder methodBuilder = BeanDefinitionBuilder
                                .genericBeanDefinition(MethodInfo.class);

                        methodBuilder.addConstructorArgValue(className);
                        methodBuilder.addConstructorArgValue(assertNotNull(
                                trimToNull(methodElement.getAttribute("name")), "mixin method name"));
                        methodBuilder.addConstructorArgValue(trimToNull(methodElement.getAttribute("renameTo")));

                        methods.add(methodBuilder.getBeanDefinition());
                    }
                }
            }

            builder.addPropertyValue("mixinClasses", classes);
            builder.addPropertyValue("mixinMethods", methods);
        }
    }

    public static final class MethodInfo {
        private Class<?> containingClass;
        private String methodName;
        private String rename;

        public MethodInfo(Class<?> containingClass, String methodName, String rename) {
            this.containingClass = containingClass;
            this.methodName = methodName;
            this.rename = rename;
        }
    }
}
