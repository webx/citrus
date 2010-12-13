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

import com.alibaba.citrus.service.pull.PullContext;
import com.alibaba.citrus.turbine.Context;

public class PullableMappedContext extends MappedContext {
    private final PullContext pullContext;

    public PullableMappedContext(PullContext pullContext) {
        this(pullContext, null, null);
    }

    public PullableMappedContext(PullContext pullContext, Context parentContext) {
        this(pullContext, null, parentContext);
    }

    public PullableMappedContext(PullContext pullContext, Map<String, Object> map) {
        this(pullContext, map, null);
    }

    public PullableMappedContext(PullContext pullContext, Map<String, Object> map, Context parentContext) {
        super(map, parentContext);
        this.pullContext = pullContext;
    }

    @Override
    protected boolean internalContainsKey(String key) {
        return super.internalContainsKey(key) || pull(key) != null;
    }

    @Override
    protected Object internalGet(String key) {
        Object object = super.internalGet(key);

        if (object == null) {
            return pull(key);
        } else {
            return object;
        }
    }

    @Override
    protected Set<String> internalKeySet() {
        if (pullContext == null) {
            return super.internalKeySet();
        } else {
            Set<String> keys = createHashSet(super.internalKeySet());
            keys.addAll(pullContext.getToolNames());
            return keys;
        }
    }

    private Object pull(String key) {
        if (pullContext != null) {
            return pullContext.pull(key);
        } else {
            return null;
        }
    }
}
