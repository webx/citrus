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
package com.alibaba.test2.dataresolver;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.dataresolver.DataResolver;
import com.alibaba.citrus.service.dataresolver.DataResolverContext;
import com.alibaba.citrus.service.dataresolver.DataResolverFactory;

public class RequestResolverFactory implements DataResolverFactory {
    @Autowired
    private HttpServletRequest request;

    public DataResolver getDataResolver(DataResolverContext context) {
        Class<?> rawType = context.getTypeInfo().getRawType();

        if (ServletRequest.class.isAssignableFrom(rawType)) {
            return new RequestResolver(request);
        }

        return null;
    }

    private class RequestResolver implements DataResolver {
        private final HttpServletRequest request;

        public RequestResolver(HttpServletRequest request) {
            this.request = request;
        }

        public Object resolve() {
            return request;
        }
    }
}
