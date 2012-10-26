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

package com.alibaba.citrus.util.internal;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ClassUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/**
 * 实现指定的接口，覆盖指定的方法，转发余下的接口方法。
 * <p/>
 * 用途：Servlet 3.0和Servlet 2.5有些许API的变化。例如：Servlet 3.0的方法：<code>Set&lt;String&gt; ServletContext.getResourcePaths(String path)</code>中使用了generic类型，而早期的API则没有。
 * 有时需要扩展并覆盖某个<code>ServletContext</code>的方法，并希望这个代码能在不同的Servlet API版本下编译通过。如果不使用动态类生成技术，就无法兼容两个Servlet API的版本。
 *
 * @author Michael Zhou
 */
public class OverridedMethodBuilder extends DynamicClassBuilder {
    private Class<?>[]                 interfaces;
    private Object                     delegatedObject;
    private Object                     overrider;
    private Map<Signature, FastMethod> overridedMethods;

    public OverridedMethodBuilder(Class<?>[] interfaces, Object delegatedObject, Object overrider) {
        this(null, interfaces, delegatedObject, overrider);
    }

    public OverridedMethodBuilder(ClassLoader cl, Class<?>[] interfaces, Object delegatedObject, Object overrider) {
        super(cl);

        this.interfaces = assertNotNull(interfaces, "interfaces");
        this.delegatedObject = delegatedObject; // 当delegatedObject为null时，overrider必须实现所有接口的方法。
        this.overrider = assertNotNull(overrider, "overrider");
        this.overridedMethods = createHashMap();

        Map<String, List<Method>> overriderMethods = createHashMap();

        for (Method method : overrider.getClass().getMethods()) {
            String methodName = method.getName();
            List<Method> methods = overriderMethods.get(methodName);

            if (methods == null) {
                methods = createLinkedList();
                overriderMethods.put(methodName, methods);
            }

            methods.add(method);
        }

        FastClass fc = FastClass.create(cl, overrider.getClass());

        for (Class<?> interfaceClass : interfaces) {
            assertTrue(delegatedObject == null || interfaceClass.isInstance(delegatedObject), "%s is not of %s", delegatedObject, interfaceClass);

            for (Method interfaceMethod : interfaceClass.getMethods()) {
                Signature sig = getSignature(interfaceMethod, null);
                Method overriderMethod = getCompatibleOverrideMethod(overriderMethods, interfaceMethod);

                if (overriderMethod != null) {
                    log.trace("Overrided method: {}", getSimpleMethodSignature(interfaceMethod, false, true, true, false));
                    overridedMethods.put(sig, fc.getMethod(overriderMethod));
                }
            }
        }
    }

    private Method getCompatibleOverrideMethod(Map<String, List<Method>> overriderMethods, Method interfaceMethod) {
        List<Method> methods = overriderMethods.get(interfaceMethod.getName());

        if (methods != null) {
            for (Method overriderMethod : methods) {
                if (overriderMethod.getParameterTypes().length != interfaceMethod.getParameterTypes().length) {
                    continue;
                }

                boolean compatible = true;

                for (int i = 0; i < overriderMethod.getParameterTypes().length; i++) {
                    if (!overriderMethod.getParameterTypes()[i].isAssignableFrom(interfaceMethod.getParameterTypes()[i])) {
                        compatible = false;
                        break;
                    }
                }

                return overriderMethod;
            }
        }

        return null;
    }

    public Object toObject() {
        Enhancer generator = new Enhancer();

        generator.setClassLoader(getClassLoader());
        generator.setSuperclass(Object.class);
        generator.setInterfaces(interfaces);

        generator.setCallbacks(new Callback[] {
                // callback 0: invoke super
                new MethodInterceptor() {
                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                            throws Throwable {
                        return proxy.invokeSuper(obj, args);
                    }
                },

                // callback 1: invoke delegated object
                new MethodInterceptor() {
                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                            throws Throwable {
                        if (delegatedObject == null) {
                            throw new UnsupportedOperationException(getSimpleMethodSignature(method));
                        }

                        return proxy.invoke(delegatedObject, args);
                    }
                },

                // callback 2: invoke overrided method
                new MethodInterceptor() {
                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                            throws Throwable {
                        FastMethod overridedMethod = overridedMethods.get(getSignature(method, null));

                        try {
                            return overridedMethod.invoke(overrider, args);
                        } catch (InvocationTargetException e) {
                            throw e.getTargetException();
                        }
                    }
                } });

        generator.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
                if (isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(method)) {
                    return 0; // invoke super
                } else if (overridedMethods.containsKey(getSignature(method, null))) {
                    return 2; // invoke overrided method
                } else {
                    return 1; // invoke delegated object
                }
            }
        });

        return generator.create();
    }
}
