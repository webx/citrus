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
package com.alibaba.citrus.service.dataresolver.data;

import java.util.Map;

import com.alibaba.citrus.service.dataresolver.DataResolver;
import com.alibaba.citrus.service.dataresolver.DataResolverContext;
import com.alibaba.citrus.service.dataresolver.DataResolverFactory;

public class MapResolverFactory implements DataResolverFactory {
    private Map<String, String> data;

    public MapResolverFactory(Map<String, String> data) {
        this.data = data;
    }

    public DataResolver getDataResolver(DataResolverContext context) {
        Class<?> rawType = context.getTypeInfo().getRawType();

        if (String.class.isAssignableFrom(rawType)) {
            Param param = context.getAnnotation(Param.class);

            if (param != null) {
                return new MapResolver(param.value(), context);
            }
        }

        return null;
    }

    private class MapResolver implements DataResolver, ContextAwareResolver {
        private final String key;
        private final DataResolverContext context;

        public MapResolver(String key, DataResolverContext context) {
            this.key = key;
            this.context = context;
        }

        public Object resolve() {
            return data.get(key);
        }

        public DataResolverContext getContext() {
            return context;
        }
    }
}
