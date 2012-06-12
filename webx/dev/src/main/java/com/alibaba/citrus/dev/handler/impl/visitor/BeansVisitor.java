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

package com.alibaba.citrus.dev.handler.impl.visitor;

import static com.alibaba.citrus.dev.handler.util.ReflectionUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ExceptionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Arrays;
import java.util.List;

import com.alibaba.citrus.dev.handler.impl.ExplorerHandler.ExplorerVisitor;
import com.alibaba.citrus.dev.handler.util.BeanDefinitionReverseEngine;
import com.alibaba.citrus.dev.handler.util.Element;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

public class BeansVisitor extends AbstractFallbackVisitor<ExplorerVisitor> {
    private final DefaultListableBeanFactory factory;

    public BeansVisitor(RequestHandlerContext context, ExplorerVisitor v) {
        super(context, v);
        this.factory = (DefaultListableBeanFactory) v.getApplicationContext().getBeanFactory();
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

        getFallbackVisitor().getDomComponent().visitTemplate(context, elements);
    }

    private RootBeanDefinition getBeanDefinition(String name) throws Exception {
        return (RootBeanDefinition) getAccessibleMethod(factory.getClass(), "getMergedLocalBeanDefinition",
                                                        new Class<?>[] { String.class }).invoke(factory, name);
    }

    /** 将bean names排序。先按bean name的复杂度排序，再按字母顺序排序。 */
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

    static class BeanName implements Comparable<BeanName> {
        String   beanName;
        String[] components;

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
