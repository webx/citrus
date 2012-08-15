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

package com.alibaba.citrus.service.moduleloader.impl.adapter;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.lang.reflect.Method;
import java.util.Map;

import com.alibaba.citrus.service.moduleloader.Module;
import com.alibaba.citrus.service.moduleloader.ModuleInfo;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderException;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 以事件的方式调用module的方法。
 * <p>
 * 支持绑定参数，参见{@link com.alibaba.citrus.service.dataresolver.DataResolverService}。
 * </p>
 */
public abstract class AbstractModuleEventAdapterFactory<A extends AbstractModuleEventAdapter>
        extends AbstractDataBindingAdapterFactory implements ApplicationContextAware {
    private static final String DEFAULT_EVENT = "perform";
    private static final String PRE_HANDLER   = "beforeExecution";
    private static final String POST_HANDLER  = "afterExecution";
    private ApplicationContext context;

    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    public Module adapt(String type, String name, Object moduleObject) {
        if (!isAdaptableType(type)) {
            return null;
        }

        Class<?> moduleClass = moduleObject.getClass();
        Map<String, Method> handlers = getEventHandlers(moduleClass);

        if (handlers == null) {
            return null;
        }

        ModuleInfo moduleInfo = new ModuleInfo(type, name);
        FastClass fc = FastClass.create(moduleClass);
        Map<String, MethodInvoker> fastHandlers = createHashMap(handlers.size());

        for (Map.Entry<String, Method> entry : handlers.entrySet()) {
            FastMethod fm = fc.getMethod(entry.getValue());
            fastHandlers.put(entry.getKey(), getMethodInvoker(fm, moduleInfo, isEventHandlerSkippable()));
        }

        FastMethod preHandler = getFastMethod(fc, PRE_HANDLER);
        FastMethod postHanlder = getFastMethod(fc, POST_HANDLER);

        AbstractModuleEventAdapter adapter = createAdapter(moduleObject,
                                                           fastHandlers,
                                                           getMethodInvoker(preHandler, moduleInfo, false),
                                                           getMethodInvoker(postHanlder, moduleInfo, false));

        try {
            // 注入并初始化adapter（不是注入moduleObject，后者取决于factory的设置）
            autowireAndInitialize(adapter, context, AbstractBeanDefinition.AUTOWIRE_AUTODETECT, type + "." + name);
        } catch (Exception e) {
            throw new ModuleLoaderException("Failed to configure module adapter", e);
        }

        return adapter;
    }

    /** 取得事件处理方法。 */
    private Map<String, Method> getEventHandlers(Class<?> moduleClass) {
        Map<String, Method> handlers = null;

        for (Method method : moduleClass.getMethods()) {
            if (checkMethod(method)) {
                String methodName = method.getName();

                // doXyz()
                if (methodName.length() > 2 && methodName.startsWith("do")
                    && Character.isUpperCase(methodName.charAt(2))) {
                    String eventName = toCamelCase(methodName.substring(2));

                    // default handler: doPerform()
                    if (DEFAULT_EVENT.equals(eventName)) {
                        eventName = null;
                    }

                    if (handlers == null) {
                        handlers = createHashMap();
                    }

                    handlers.put(eventName, method);
                }
            }
        }

        return handlers;
    }

    /** 取得方法，如果不存在或不符合要求，则返回null。 */
    private FastMethod getFastMethod(FastClass fc, String methodName) {
        Method method = getMethod(fc.getJavaClass(), methodName);

        if (method == null) {
            return null;
        } else {
            return fc.getMethod(method);
        }
    }

    /** 判断module type是否可被适配。 */
    protected abstract boolean isAdaptableType(String type);

    /** 判断方法是否可被跳过。 */
    protected abstract boolean isEventHandlerSkippable();

    /** 工厂方法，用来创建一个adapter实例。 */
    protected abstract A createAdapter(Object moduleObject, Map<String, MethodInvoker> handlers, MethodInvoker preHandler, MethodInvoker postHanlder);
}
