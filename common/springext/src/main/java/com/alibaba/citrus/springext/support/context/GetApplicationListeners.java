/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.springext.support.context;

import static com.alibaba.citrus.util.Assert.*;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.context.ApplicationListener;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * 由于spring3和spring2的api不完全兼容，导致在spring2上编译的context.getApplicationListeners()
 * 命令在spring3中报NoSuchMethodError。此类用反射的方法来解决兼容性问题。将来完全迁移到spring3以后，可以删除这个实现。
 *
 * @author Michael Zhou
 */
class GetApplicationListeners {
    private final AbstractApplicationContext context;
    private final Method                     getApplicationListenersMethod;

    public GetApplicationListeners(AbstractApplicationContext context) {
        this.context = context;

        Method method = null;

        for (Class<?> clazz = context.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod("getApplicationListeners");
                break;
            } catch (Exception ignored) {
            }
        }

        getApplicationListenersMethod = assertNotNull(method,
                                                      "Could not call method: context.getApplicationListeners()");
    }

    public Collection<ApplicationListener> invoke() {
        try {
            @SuppressWarnings("unchecked")
            Collection<ApplicationListener> listeners = (Collection<ApplicationListener>) getApplicationListenersMethod
                    .invoke(context);

            return listeners;
        } catch (Exception e) {
            throw new RuntimeException("Could not call method: context.getApplicationListeners()", e);
        }
    }
}
