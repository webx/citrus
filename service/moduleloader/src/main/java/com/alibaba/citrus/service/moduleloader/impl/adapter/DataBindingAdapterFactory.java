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

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import com.alibaba.citrus.service.dataresolver.DataResolverService;
import com.alibaba.citrus.service.moduleloader.Module;
import com.alibaba.citrus.service.moduleloader.ModuleInfo;

/**
 * 调用<code>execute()</code>方法，不要求实现<code>Module</code>接口。
 * <p>
 * 支持绑定参数，参见{@link DataResolverService}。
 * </p>
 * 
 * @author Michael Zhou
 */
public class DataBindingAdapterFactory extends AbstractDataBindingAdapterFactory {
    public Module adapt(String type, String name, Object moduleObject) {
        ModuleInfo moduleInfo = new ModuleInfo(type, name);
        Class<?> moduleClass = moduleObject.getClass();
        Method executeMethod = getMethod(moduleClass, "execute");

        if (executeMethod != null) {
            FastClass fc = FastClass.create(moduleClass);
            FastMethod fm = fc.getMethod(executeMethod);

            // 对于action，可被“跳过”执行。
            boolean skippable = "action".equalsIgnoreCase(type);

            return new DataBindingAdapter(moduleObject, getMethodInvoker(fm, moduleInfo, skippable));
        }

        return null;
    }
}
