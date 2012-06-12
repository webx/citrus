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
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Comparator;
import java.util.Map;

import com.alibaba.citrus.dev.handler.impl.ExplorerHandler.ExplorerVisitor;
import com.alibaba.citrus.util.ClassUtil;
import com.alibaba.citrus.util.templatelite.Template;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class ResolvableDepsVisitor extends AbstractFallbackVisitor<ExplorerVisitor> {
    private final Map<Class<?>, Object> resolvableDependencies;
    private       Class<?>              type;
    private       Object                value;
    private int id = 1000;

    public ResolvableDepsVisitor(RequestHandlerContext context, ExplorerVisitor v) {
        super(context, v);
        this.resolvableDependencies = getResolvableDependencies((DefaultListableBeanFactory) getFallbackVisitor()
                .getApplicationContext().getBeanFactory());
    }

    @SuppressWarnings("unchecked")
    private Map<Class<?>, Object> getResolvableDependencies(DefaultListableBeanFactory factory) {
        Map<Class<?>, Object> deps;

        try {
            deps = (Map<Class<?>, Object>) getAccessibleField(factory.getClass(), "resolvableDependencies")
                    .get(factory);
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
            id++;

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

    public void visitValueId() {
        out().print(id);
    }

    public void visitValue() {
        out().print(escapeHtml(String.valueOf(value)));
    }
}
