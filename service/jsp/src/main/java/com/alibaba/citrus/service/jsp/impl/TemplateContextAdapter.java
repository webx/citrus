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
package com.alibaba.citrus.service.jsp.impl;

import static com.alibaba.citrus.util.Assert.*;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.alibaba.citrus.service.template.TemplateContext;

/**
 * 将<code>TemplateContext</code>适配到HTTP request的适配器。
 * 
 * @author Michael Zhou
 */
public class TemplateContextAdapter extends HttpServletRequestWrapper {
    private final TemplateContext context;

    public TemplateContextAdapter(HttpServletRequest request, TemplateContext context) {
        super(assertNotNull(request, "request"));
        this.context = assertNotNull(context, "templateContext");
    }

    /**
     * 取得被适配的<code>TemplateContext</code>对象。
     */
    public TemplateContext getTemplateContext() {
        return context;
    }

    /**
     * 取得request作用域的所有属性的keys。
     * <p>
     * 首先取得context中的所有keys，然后取得<code>request.getAttributeNames</code>
     * 所返回的keys。keys不会重复。
     * </p>
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        @SuppressWarnings("unchecked")
        Enumeration<String> attrNames = getRequest().getAttributeNames();
        return new AttributeNamesEnumeration(context.keySet(), attrNames);
    }

    /**
     * 取得request作用域的属性。
     * <p>
     * 如果context中存在指定名称的对象，则返回之，否则返回<code>request.getAttribute</code>的值。
     * </p>
     * <p>
     * 如果值不存在，则返回<code>null</code>。
     * </p>
     */
    @Override
    public Object getAttribute(String name) {
        Object value = context.get(name);

        if (value == null) {
            value = getRequest().getAttribute(name);
        }

        return value;
    }

    /**
     * 设置request作用域的属性。
     * <p>
     * 该值将被设置到<code>request.setAttribute</code>
     * 中，而context中的同名值将被删除，以便随后的代码可以访问新设置的值。
     * </p>
     */
    @Override
    public void setAttribute(String name, Object value) {
        context.remove(name);
        getRequest().setAttribute(name, value);
    }

    /**
     * 删除request作用域的属性。同时从<code>request.removeAttribute</code>
     * 和context中删除指定名称的属性。
     */
    @Override
    public void removeAttribute(String name) {
        context.remove(name);
        getRequest().removeAttribute(name);
    }

    @Override
    public String toString() {
        return "TemplateContextAdapter[" + context + "]";
    }

    /**
     * 将一个集合和一个<code>Enumeration</code>结合的<code>Enumeration</code>。
     */
    private static class AttributeNamesEnumeration implements Enumeration<String> {
        private final Set<String> set;
        private final Iterator<String> iterator;
        private final Enumeration<String> enumeration; // 可以为null
        private String next = null;

        public AttributeNamesEnumeration(Set<String> set, Enumeration<String> enumeration) {
            this.set = set;
            this.iterator = set.iterator();
            this.enumeration = enumeration; // 可以为null
        }

        public boolean hasMoreElements() {
            if (next == null) {
                if (iterator.hasNext()) {
                    next = iterator.next();
                } else if (enumeration != null) {
                    while (next == null && enumeration.hasMoreElements()) {
                        next = enumeration.nextElement();

                        if (set.contains(next)) {
                            next = null;
                        }
                    }
                }
            }

            return next != null;
        }

        public String nextElement() {
            if (hasMoreElements()) {
                String result = next;

                next = null;
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
