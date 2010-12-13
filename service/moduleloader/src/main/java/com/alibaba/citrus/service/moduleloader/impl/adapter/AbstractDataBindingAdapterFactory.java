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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.reflect.FastMethod;

import com.alibaba.citrus.service.dataresolver.DataResolver;
import com.alibaba.citrus.service.dataresolver.DataResolverService;
import com.alibaba.citrus.service.moduleloader.ModuleAdapterFactory;
import com.alibaba.citrus.service.moduleloader.ModuleInfo;
import com.alibaba.citrus.service.moduleloader.UnadaptableModuleException;

public abstract class AbstractDataBindingAdapterFactory implements ModuleAdapterFactory {
    private DataResolverService dataResolverService;

    public void setDataResolverService(DataResolverService dataResolverService) {
        this.dataResolverService = dataResolverService;
    }

    /**
     * 取得方法，如果不存在或不符合要求，则返回null。
     */
    protected Method getMethod(Class<?> moduleClass, String methodName) {
        for (Method candidateMethod : moduleClass.getMethods()) {
            if (candidateMethod.getName().equals(methodName) && checkMethod(candidateMethod)) {
                return candidateMethod;
            }
        }

        return null;
    }

    /**
     * 检查方法：public或protected、非static、参数任意。
     */
    protected boolean checkMethod(Method method) {
        int modifiers = method.getModifiers();

        if (Modifier.isStatic(modifiers)) {
            return false;
        }

        if (!Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers)) {
            return false;
        }

        return true;
    }

    /**
     * 取得method调用器。其中，moduleInfo为dataResolverFactory提供额外的信息，用来生成适当的resolver。
     */
    protected MethodInvoker getMethodInvoker(FastMethod fastMethod, ModuleInfo moduleInfo, boolean skippable) {
        if (fastMethod == null) {
            return null;
        }

        DataResolver[] resolvers = null;

        if (dataResolverService != null) {
            // extra object: moduleInfo
            resolvers = dataResolverService.getParameterResolvers(fastMethod.getJavaMethod(), moduleInfo);
        } else {
            Method javaMethod = fastMethod.getJavaMethod();
            int paramsCount = javaMethod.getParameterTypes().length;

            if (paramsCount > 0) {
                throw new UnadaptableModuleException(String.format(
                        "Could not adapt object to module: type=%s, name=%s, class=%s: "
                                + "method %s has %d parameters, but no DataResolvers defined.", moduleInfo.getType(),
                        moduleInfo.getName(), javaMethod.getDeclaringClass().getName(), javaMethod.getName(),
                        paramsCount));
            }
        }

        return new MethodInvoker(fastMethod, resolvers, skippable);
    }
}
