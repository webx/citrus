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
package com.alibaba.citrus.turbine.support;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Set;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 抽象的<code>Context</code>实现，提供了可嵌套的context机制。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractContext implements Context {
    /** 父context，如果指定key在当前context不存在，则会在父context中查找。 */
    private final Context parentContext;

    /**
     * 创建一个context。
     */
    public AbstractContext() {
        this(null);
    }

    /**
     * 创建一个context，指定parent context。
     */
    public AbstractContext(Context parentContext) {
        this.parentContext = parentContext;
    }

    /**
     * 取得父context，如不存在则返回<code>null</code>。
     */
    public Context getParentContext() {
        return parentContext;
    }

    /**
     * 添加一个值。
     */
    public final void put(String key, Object value) {
        if (value == null) {
            remove(key);
        } else {
            internalPut(key, value);
        }
    }

    /**
     * 取得指定值。
     */
    public final Object get(String key) {
        Object value = internalGet(key);

        if (value == null && parentContext != null) {
            return parentContext.get(key);
        }

        return decodeValue(value);
    }

    /**
     * 删除一个值。
     */
    public final void remove(String key) {
        if (parentContext != null && parentContext.containsKey(key)) {
            internalPut(key, NULL_PLACEHOLDER);
        } else {
            internalRemove(key);
        }
    }

    /**
     * 判断是否包含指定的键。
     */
    public final boolean containsKey(String key) {
        boolean containsKey = internalContainsKey(key);

        if (!containsKey && parentContext != null) {
            return parentContext.containsKey(key);
        }

        return containsKey;
    }

    /**
     * 取得所有key的集合。
     */
    public final Set<String> keySet() {
        Set<String> internalKeySet = internalKeySet();
        Set<String> parentKeySet = parentContext == null ? null : parentContext.keySet();

        if (parentKeySet == null || parentKeySet.isEmpty()) {
            return internalKeySet;
        }

        Set<String> newSet = createHashSet();

        newSet.addAll(parentKeySet);
        newSet.addAll(internalKeySet);

        return newSet;
    }

    /**
     * 取得所有key的集合。
     */
    protected abstract Set<String> internalKeySet();

    /**
     * 取得指定值。
     */
    protected abstract Object internalGet(String key);

    /**
     * 删除一个值。
     */
    protected abstract void internalRemove(String key);

    /**
     * 判断是否包含指定的键。
     */
    protected abstract boolean internalContainsKey(String key);

    /**
     * 添加一个值。
     */
    protected abstract void internalPut(String key, Object value);

    /**
     * 解码context的值。如果为<code>NULL_PLACEHOLDER</code>，则返回<code>null</code>。
     */
    private Object decodeValue(Object value) {
        return value == NULL_PLACEHOLDER ? null : value;
    }

    @Override
    public String toString() {
        MapBuilder mb;

        if (parentContext == null) {
            mb = getMapBuilder();
        } else {
            mb = new MapBuilder();

            mb.append("parentContext", parentContext);
            mb.append("thisContext", getMapBuilder());
        }

        return new ToStringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }

    private MapBuilder getMapBuilder() {
        MapBuilder mb = new MapBuilder().setSortKeys(true);

        for (String key : internalKeySet()) {
            mb.append(key, get(key));
        }

        return mb;
    }
}
