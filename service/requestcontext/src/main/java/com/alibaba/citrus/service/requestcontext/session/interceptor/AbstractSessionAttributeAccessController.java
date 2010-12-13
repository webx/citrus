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
package com.alibaba.citrus.service.requestcontext.session.interceptor;

import com.alibaba.citrus.service.requestcontext.session.SessionAttributeInterceptor;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig;

/**
 * 用来控制session attributes的使用。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractSessionAttributeAccessController implements SessionAttributeInterceptor {
    private String modelKey;

    public void init(SessionConfig sessionConfig) {
        this.modelKey = sessionConfig.getModelKey();
    }

    public final Object onRead(String name, Object value) {
        if (modelKey.equals(name) || allowForAttribute(name, value == null ? null : value.getClass())) {
            return value;
        }

        return readInvalidAttribute(name, value);
    }

    public final Object onWrite(String name, Object value) {
        if (modelKey.equals(name) || allowForAttribute(name, value == null ? null : value.getClass())) {
            return value;
        }

        return writeInvalidAttribute(name, value);
    }

    protected abstract boolean allowForAttribute(String name, Class<?> type);

    protected abstract Object readInvalidAttribute(String name, Object value);

    protected abstract Object writeInvalidAttribute(String name, Object value);
}
