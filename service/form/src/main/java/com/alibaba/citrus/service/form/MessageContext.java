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
package com.alibaba.citrus.service.form;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import com.alibaba.citrus.expr.ExpressionContext;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 用于生成validator出错信息的context。
 * 
 * @author Michael Zhou
 */
public abstract class MessageContext implements ExpressionContext {
    protected final Map<Object, Object> thisContext = createHashMap();

    /**
     * 取得指定值。
     */
    public Object get(String key) {
        Object value = thisContext.get(key);

        if (value == null) {
            value = internalGet(key);
        }

        ExpressionContext parentContext = getParentContext();

        if (value == null && parentContext != null) {
            value = parentContext.get(key);
        }

        return decorate(value);
    }

    /**
     * 假如是数组，转换成更易操作的<code>List</code>。
     */
    protected Object decorate(Object value) {
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> list = createArrayList(length);

            for (int i = 0; i < length; i++) {
                list.add(Array.get(value, i));
            }

            value = list;
        }

        return value;
    }

    /**
     * 添加一个值。
     */
    public void put(String key, Object value) {
        if (value == null) {
            thisContext.remove(key);

            ExpressionContext parentContext = getParentContext();

            if (parentContext != null) {
                parentContext.put(key, null);
            }
        } else {
            thisContext.put(key, value);
        }
    }

    /**
     * 批量添加值。
     */
    public void putAll(Map<?, ?> values) {
        if (values != null) {
            thisContext.putAll(values);
        }
    }

    /**
     * 从另一个<code>MessageContext</code>中，复制所有本地context，但不覆盖。
     */
    public void copyLocalContext(MessageContext src) {
        for (Map.Entry<Object, Object> entry : src.thisContext.entrySet()) {
            Object key = entry.getKey();

            if (!thisContext.containsKey(key)) {
                thisContext.put(key, entry.getValue());
            }
        }
    }

    /**
     * 取得指定值。
     */
    protected abstract Object internalGet(String key);

    /**
     * 取得parent context。
     */
    public abstract ExpressionContext getParentContext();

    @Override
    public final String toString() {
        ToStringBuilder sb = new ToStringBuilder();
        MapBuilder mb = new MapBuilder();

        buildToString(mb);
        mb.append("context", new MapBuilder().setSortKeys(true).setPrintCount(true).appendAll(thisContext));

        buildToString(sb);
        sb.append(mb);

        ExpressionContext parentContext = getParentContext();

        if (parentContext != null) {
            sb.append(parentContext);
        }

        return sb.toString();
    }

    protected abstract void buildToString(ToStringBuilder sb);

    protected abstract void buildToString(MapBuilder mb);
}
