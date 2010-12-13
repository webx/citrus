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
package com.alibaba.citrus.service.template.support;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 基于<code>Map</code>的<code>TemplateContext</code>实现。
 * 
 * @author Michael Zhou
 */
public class MappedTemplateContext implements TemplateContext {
    private final Map<String, Object> map;

    public MappedTemplateContext() {
        this.map = createHashMap();
    }

    public MappedTemplateContext(Map<String, Object> map) {
        this.map = assertNotNull(map, "map");
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append(getClass().getSimpleName()).appendMap(map, true).toString();
    }
}
