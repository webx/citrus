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
package com.alibaba.citrus.service.dataresolver.impl;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.dataresolver.DataResolver;
import com.alibaba.citrus.service.dataresolver.DataResolverContext;
import com.alibaba.citrus.service.dataresolver.DataResolverFactory;
import com.alibaba.citrus.service.dataresolver.DataResolverNotFoundException;
import com.alibaba.citrus.service.dataresolver.DataResolverService;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

public class DataResolverServiceImpl extends AbstractService<DataResolverService> implements DataResolverService {
    private DataResolverFactory[] factories;

    public void setFactories(DataResolverFactory[] factories) {
        this.factories = factories;
    }

    @Override
    protected void init() throws Exception {
        if (factories == null) {
            factories = new DataResolverFactory[0];
        }
    }

    public DataResolver getDataResolver(Type type, Annotation[] annotations, Object... extraInfo)
            throws DataResolverNotFoundException {
        DataResolverContext context = new DataResolverContext(type, annotations, extraInfo);
        DataResolver resolver = null;

        for (DataResolverFactory factory : factories) {
            resolver = factory.getDataResolver(context);

            if (resolver != null) {
                getLogger().debug("Found resolver: {}", resolver);
                break;
            }
        }

        if (resolver == null) {
            throw new DataResolverNotFoundException("Could not find data resolver for " + context);
        }

        return resolver;
    }

    public DataResolver[] getParameterResolvers(Method method, Object... extraInfo)
            throws DataResolverNotFoundException {
        assertNotNull(method, "method");

        Type[] paramTypes = method.getGenericParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        assertTrue(paramTypes.length == paramAnnotations.length, "invalid params");

        DataResolver[] resolvers = new DataResolver[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            Type type = assertNotNull(paramTypes[i], "paramTypes");
            Annotation[] annotations = assertNotNull(paramAnnotations[i], "paramAnnotations");

            resolvers[i] = getDataResolver(type, annotations, getExtraInfo(extraInfo, method, i));
        }

        return resolvers;
    }

    /**
     * 复制extraInfo，并追加methodParameter对象。
     */
    private Object[] getExtraInfo(Object[] extraInfo, Method method, int paramIndex) {
        Object[] result;

        if (isEmptyArray(extraInfo)) {
            result = new Object[1];
        } else {
            result = new Object[extraInfo.length + 1];
            System.arraycopy(extraInfo, 0, result, 0, extraInfo.length);
        }

        result[result.length - 1] = new MethodParameter(method, paramIndex) {
            @Override
            public String toString() {
                MapBuilder mb = new MapBuilder();

                mb.append("method", getMethod());
                mb.append("paramIndex", getParameterIndex());

                return new ToStringBuilder().append("MethodParameter").append(mb).toString();
            }
        };

        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append(getBeanDescription()).append(factories).toString();
    }
}
