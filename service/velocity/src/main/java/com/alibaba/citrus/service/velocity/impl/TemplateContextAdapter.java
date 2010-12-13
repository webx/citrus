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
package com.alibaba.citrus.service.velocity.impl;

import org.apache.velocity.context.AbstractContext;

import com.alibaba.citrus.service.template.TemplateContext;

/**
 * 将<code>TemplateContext</code>适配到velocity context的适配器。
 * 
 * @author Michael Zhou
 */
public class TemplateContextAdapter extends AbstractContext {
    private final TemplateContext context;

    /**
     * 创建一个适配器。
     */
    public TemplateContextAdapter(TemplateContext context) {
        this.context = context;
    }

    /**
     * 取得被适配的<code>TemplateContext</code>对象。
     */
    public TemplateContext getTemplateContext() {
        return context;
    }

    /**
     * 取得指定值。
     */
    @Override
    public Object internalGet(String key) {
        return context.get(key);
    }

    /**
     * 添加一个值，如果不存在，则返回<code>null</code>。
     */
    @Override
    public Object internalPut(String key, Object value) {
        Object oldValue = context.get(key);
        context.put(key, value);
        return oldValue;
    }

    /**
     * 判断是否包含指定的键。
     */
    @Override
    public boolean internalContainsKey(Object key) {
        if (key instanceof String) {
            return context.containsKey((String) key);
        } else {
            return false;
        }
    }

    /**
     * 取得所有key的集合。
     */
    @Override
    public Object[] internalGetKeys() {
        return context.keySet().toArray();
    }

    /**
     * 删除一个值，返回原值。
     */
    @Override
    public Object internalRemove(Object key) {
        if (key instanceof String) {
            Object oldValue = context.get((String) key);
            context.remove((String) key);
            return oldValue;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "TemplateContextAdapter[" + context + "]";
    }
}
