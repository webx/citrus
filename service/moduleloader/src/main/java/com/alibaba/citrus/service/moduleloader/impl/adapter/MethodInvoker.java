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
package com.alibaba.citrus.service.moduleloader.impl.adapter;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ClassUtil.*;

import java.lang.reflect.InvocationTargetException;

import net.sf.cglib.reflect.FastMethod;

import org.slf4j.Logger;

import com.alibaba.citrus.service.dataresolver.DataResolver;
import com.alibaba.citrus.service.moduleloader.SkipModuleExecutionException;

class MethodInvoker {
    private final FastMethod fastMethod;
    private final DataResolver[] resolvers;
    private final boolean skippable;

    public MethodInvoker(FastMethod fastMethod, DataResolver[] resolvers, boolean skippable) {
        this.fastMethod = assertNotNull(fastMethod, "fastMethod");
        this.resolvers = resolvers == null ? new DataResolver[0] : resolvers;
        this.skippable = skippable;
    }

    public void invoke(Object moduleObject, Logger log) throws Exception {
        Object[] args = new Object[resolvers.length];

        for (int i = 0; i < args.length; i++) {
            Object value;

            try {
                value = resolvers[i].resolve();
            } catch (SkipModuleExecutionException e) {
                if (skippable) {
                    log.debug("Module execution has been skipped. Method: {}, {}", fastMethod, e.getMessage());
                    return;
                }

                value = e.getValueForNonSkippable();
            }

            // 特别处理：防止对primitive类型设置null
            Class<?> paramType = fastMethod.getJavaMethod().getParameterTypes()[i];

            if (value == null && paramType.isPrimitive()) {
                value = getPrimitiveDefaultValue(paramType);
            }

            args[i] = value;
        }

        try {
            fastMethod.invoke(moduleObject, args);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    @Override
    public String toString() {
        return fastMethod.toString();
    }
}
