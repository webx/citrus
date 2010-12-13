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

import static com.alibaba.citrus.util.Assert.*;

import java.util.Set;

import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.turbine.Context;

/**
 * Ω´turbine context  ≈‰µΩtemplate context°£
 * 
 * @author Michael Zhou
 */
public class ContextAdapter implements TemplateContext {
    private final Context context;

    public ContextAdapter(Context context) {
        this.context = assertNotNull(context, "no context");
    }

    public boolean containsKey(String key) {
        return context.containsKey(key);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public Set<String> keySet() {
        return context.keySet();
    }

    public void put(String key, Object value) {
        context.put(key, value);
    }

    public void remove(String key) {
        context.remove(key);
    }

    @Override
    public String toString() {
        return context.toString();
    }
}
