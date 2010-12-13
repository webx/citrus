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

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.turbine.Context;

/**
 * 基于<code>Map</code>的<code>Context</code>实现。
 * 
 * @author Michael Zhou
 */
public class MappedContext extends AbstractContext {
    private final Map<String, Object> map;

    public MappedContext() {
        this(null, null);
    }

    public MappedContext(Context parentContext) {
        this(null, parentContext);
    }

    public MappedContext(Map<String, Object> map) {
        this(map, null);
    }

    public MappedContext(Map<String, Object> map, Context parentContext) {
        super(parentContext);

        if (map == null) {
            map = createHashMap();
        }

        this.map = map;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    protected boolean internalContainsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    protected Object internalGet(String key) {
        return map.get(key);
    }

    @Override
    protected void internalPut(String key, Object value) {
        map.put(key, value);
    }

    @Override
    protected Set<String> internalKeySet() {
        return map.keySet();
    }

    @Override
    protected void internalRemove(String key) {
        map.remove(key);
    }
}
