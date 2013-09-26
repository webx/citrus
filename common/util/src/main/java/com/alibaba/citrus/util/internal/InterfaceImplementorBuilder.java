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
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/**
 * 实现指定的接口。你可以：
 * <ol>
 * <li>指定一个base object，将所有的调用转发给这个对象。Base object必须实现同样的接口。</li>
 * <li>指定一个overrider object，用其中的方法来覆盖base object中相同名称和参数的方法。Overrider object不必实现任何接口，甚至其方法的参数也不必完全相同，只需要兼容即可。</li>
 * <li>如果未指定base object，且某接口的方法未被overrider实现，那么调用这个方法将抛出<code>UnsupportedOperationException</code>。</li>
 * </ol>
 * <p/>
 * 用途示例：Servlet 3.0和Servlet 2.5有些许API的不同。例如：Servlet 3.0的方法：<code>Set&lt;String&gt; ServletContext.getResourcePaths(String path)</code>中使用了generic类型，而早期的API则没有使用generic。
 * 假如你需要扩展并覆盖这个<code>ServletContext</code>的方法，并希望这个代码能在不同的Servlet API版本下编译通过。如果不使用动态类生成技术，就无法兼容两个Servlet API的版本。
 *
 * @author Michael Zhou
 */
public class InterfaceImplementorBuilder extends DynamicClassBuilder {
    private final static Callback CALL_SUPER                             = new CallSuper();
    private final static String   OVERRIDER_SET_PROXY_OBJECT_METHOD_NAME = "setThisProxy";

    private Class<?>                   superclass;
    private List<Class<?>>             interfaces;
    private Class<?>                   baseClass;
    private Class<?>                   overriderClass;
    private String                     overriderSetProxyObjectMethodName;
    private Map<Signature, FastMethod> overridedMethods;
    private FastMethod                 overriderSetProxyObjectMethod;
    private Factory                    factory;

    public InterfaceImplementorBuilder() {
        this(null);
    }

    public InterfaceImplementorBuilder(ClassLoader cl) {
        super(cl);
        interfaces = createLinkedList();
    }

    public InterfaceImplementorBuilder setSuperclass(Class<?> superclass) {
        this.superclass = superclass;
        return this;
    }

    public InterfaceImplementorBuilder addInterface(Class<?>... interfaceClasses) {
        if (interfaceClasses != null) {
            for (Class<?> interfaceClass : interfaceClasses) {
                if (interfaceClass != null) {
                    this.interfaces.add(interfaceClass);
                }
            }
        }

        return this;
    }

    public InterfaceImplementorBuilder setBaseClass(Class<?> baseClass) {
        this.baseClass = baseClass;
        return this;
    }

    public InterfaceImplementorBuilder setOverriderClass(Class<?> overriderClass) {
        this.overriderClass = overriderClass;
        return this;
    }

    public InterfaceImplementorBuilder setOverriderSetProxyObjectMethodName(String methodName) {
        this.overriderSetProxyObjectMethodName = methodName;
        return this;
    }

    private Map<String, List<Method>> getMethodMap(Class<?> clazz) {
        Map<String, List<Method>> methods = createHashMap();

        for (Method method : clazz.getMethods()) {
            String methodName = method.getName();
            List<Method> methodList = methods.get(methodName);

            if (methodList == null) {
                methodList = createLinkedList();
                methods.put(methodName, methodList);
            }

            methodList.add(method);
        }

        return methods;
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

                if (compatible) {
                    return overriderMethod;
                }
            }
        }

        return null;
    }

    private Method getOverriderSetProxyObjectMethod(Map<String, List<Method>> overriderMethods) {
        String methodName = overriderSetProxyObjectMethodName;

        if (methodName == null) {
            methodName = OVERRIDER_SET_PROXY_OBJECT_METHOD_NAME;
        }

        List<Method> methods = overriderMethods.get(methodName);

        if (methods != null) {
            for (Method overriderMethod : methods) {
                if (overriderMethod.getParameterTypes().length != 1) {
                    continue;
                }

                boolean compatible = true;
                Class<?> paramType = overriderMethod.getParameterTypes()[0];

                for (Class<?> interfaceClass : interfaces) {
                    if (!paramType.isAssignableFrom(interfaceClass)) {
                        compatible = false;
                        break;
                    }
                }

                if (compatible) {
                    return overriderMethod;
                }
            }
        }

        return null;
    }

    public final InterfaceImplementorBuilder init() {
        init(null, null);
        return this;
    }

    private void init(Object overrider, Object baseObject) {
        if (factory != null) {
            return;
        }

        // check superclass
        if (superclass == null) {
            superclass = Object.class;
        }

        // check interfaces
        assertTrue(!interfaces.isEmpty(), "no interface specified");

        // 如果指定了baseObject，它必须实现所有接口。
        // 也可不指定baseObject，此时，所有未实现的接口方法将抛出UnsupportedOperationException。
        if (baseClass == null && baseObject != null) {
            baseClass = baseObject.getClass();
        }

        if (baseClass != null) {
            for (Class<?> interfaceClass : interfaces) {
                assertTrue(interfaceClass.isAssignableFrom(baseClass), "Base class %s must implement %s", baseClass.getName(), interfaceClass);
            }
        }

        // check overrider
        if (overriderClass == null && overrider != null) {
            overriderClass = overrider.getClass();
        }

        this.overriderClass = overriderClass == null ? Object.class : overriderClass;
        this.overridedMethods = createHashMap();

        Map<String, List<Method>> overriderMethods = getMethodMap(overriderClass);
        FastClass overriderFastClass = FastClass.create(getClassLoader(), overriderClass);

        for (Class<?> interfaceClass : interfaces) {
            for (Method interfaceMethod : interfaceClass.getMethods()) {
                Signature sig = getSignature(interfaceMethod, null);
                Method overriderMethod = getCompatibleOverrideMethod(overriderMethods, interfaceMethod);

                if (overriderMethod != null) {
                    log.trace("Overrided method: {}", getSimpleMethodSignature(interfaceMethod, true));
                    overridedMethods.put(sig, overriderFastClass.getMethod(overriderMethod));
                }
            }
        }

        // overriderSetProxyObjectMethodName
        Method overriderSetProxyObjectMethod = getOverriderSetProxyObjectMethod(overriderMethods);

        if (overriderSetProxyObjectMethod != null) {
            this.overriderSetProxyObjectMethod = overriderFastClass.getMethod(overriderSetProxyObjectMethod);
        }

        // generate class
        Enhancer generator = new Enhancer();

        generator.setClassLoader(getClassLoader());
        generator.setSuperclass(superclass);
        generator.setInterfaces(interfaces.toArray(new Class<?>[interfaces.size()]));

        generator.setCallbackTypes(new Class<?>[] {
                CallSuper.class,
                CallBaseObject.class,
                CallOverrider.class
        });

        generator.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
                if (isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(method)) {
                    return 0; // invoke super
                } else if (overridedMethods.containsKey(getSignature(method, null))) {
                    return 2; // invoke overrided method
                } else {
                    return 1; // invoke base object
                }
            }
        });

        generator.setCallbacks(new Callback[] { CALL_SUPER, new CallBaseObject(new Object()), new CallOverrider(new Object()) });

        factory = (Factory) generator.create();
    }

    public Object toObject() {
        return toObject(null, null);
    }

    public Object toObject(Object overrider) {
        return toObject(overrider, null);
    }

    public Object toObject(Object overrider, Object baseObject) {
        if (overrider == null) {
            overrider = new Object();
        }

        init(overrider, baseObject);

        Object proxy = factory.newInstance(new Callback[] {
                CALL_SUPER,
                new CallBaseObject(baseObject),
                new CallOverrider(overrider)
        });

        if (overriderSetProxyObjectMethod != null) {
            try {
                overriderSetProxyObjectMethod.invoke(overrider, new Object[] { proxy });
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Failed to call " + getSimpleMethodSignature(overriderSetProxyObjectMethod.getJavaMethod(), true), e.getTargetException());
            }
        }

        return proxy;
    }

    private static class CallSuper implements MethodInterceptor {
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            return proxy.invokeSuper(obj, args);
        }
    }

    private class CallBaseObject implements MethodInterceptor {
        private final Object baseObject;

        private CallBaseObject(Object baseObject) {
            this.baseObject = baseObject;
        }

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (baseObject == null || !method.getDeclaringClass().isAssignableFrom(baseObject.getClass())) {
                if (method.getDeclaringClass().isAssignableFrom(superclass)) {
                    return proxy.invokeSuper(obj, args);
                } else {
                    throw new UnsupportedOperationException(getSimpleMethodSignature(method, true));
                }
            }

            return proxy.invoke(baseObject, args);
        }
    }

    private class CallOverrider implements MethodInterceptor {
        private final Object overrider;

        private CallOverrider(Object overrider) {
            this.overrider = overrider;
        }

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            FastMethod overridedMethod = overridedMethods.get(getSignature(method, null));

            try {
                return overridedMethod.invoke(overrider, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
