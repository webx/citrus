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
package com.alibaba.citrus.util.internal;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ClassUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.lang.reflect.Modifier.*;

import java.lang.reflect.Method;
import java.util.Map;

import net.sf.cglib.asm.Type;
import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InterfaceMaker;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.util.ClassLoaderUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 将一组静态方法组合成一个对象。
 * 
 * @author Michael Zhou
 */
public class StaticFunctionDelegatorBuilder {
    private final static int PUBLIC_STATIC_MODIFIERS = PUBLIC | STATIC;
    private final static Logger log = LoggerFactory.getLogger(StaticFunctionDelegatorBuilder.class);
    private final Map<Signature, Method> methods = createHashMap();
    private ClassLoader classLoader;
    private Class<?> mixinInterface;

    public ClassLoader getClassLoader() {
        return classLoader == null ? ClassLoaderUtil.getContextClassLoader() : classLoader;
    }

    public StaticFunctionDelegatorBuilder setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public StaticFunctionDelegatorBuilder addClass(Class<?> utilClass) {
        for (Method method : utilClass.getMethods()) {
            if (isPublicStatic(method)) {
                addMethod(method);
            }
        }

        return this;
    }

    public StaticFunctionDelegatorBuilder addMethod(Method method) {
        return addMethod(method, null);
    }

    public StaticFunctionDelegatorBuilder addMethod(Method method, String rename) {
        assertNotNull(method, "Method is null");
        assertTrue(isPublicStatic(method), "Method is not public static: %s", method);

        Signature sig = getSignature(method, rename);

        if (methods.containsKey(sig)) {
            throw new IllegalArgumentException("Duplicated method signature: " + sig + "\n  method: "
                    + methods.get(sig));
        }

        methods.put(sig, method);

        return this;
    }

    private Signature getSignature(Method method, String rename) {
        String name = defaultIfNull(trimToNull(rename), method.getName());
        Type returnType = Type.getType(method.getReturnType());
        Type[] paramTypes = Type.getArgumentTypes(method);

        return new Signature(name, returnType, paramTypes);
    }

    private boolean isPublicStatic(Method method) {
        return (method.getModifiers() & PUBLIC_STATIC_MODIFIERS) == PUBLIC_STATIC_MODIFIERS;
    }

    private boolean isEqualsMethod(Method method) {
        if (!"equals".equals(method.getName())) {
            return false;
        }

        Class<?>[] paramTypes = method.getParameterTypes();

        return paramTypes.length == 1 && paramTypes[0] == Object.class;
    }

    private boolean isHashCodeMethod(Method method) {
        return "hashCode".equals(method.getName()) && method.getParameterTypes().length == 0;
    }

    private boolean isToStringMethod(Method method) {
        return "toString".equals(method.getName()) && method.getParameterTypes().length == 0;
    }

    public Class<?> getMixinInterface() {
        if (mixinInterface == null) {
            InterfaceMaker im = new InterfaceMaker();

            for (Map.Entry<Signature, Method> entry : methods.entrySet()) {
                Signature sig = entry.getKey();
                Method method = entry.getValue();

                Type[] exceptionTypes = new Type[method.getExceptionTypes().length];

                for (int i = 0; i < exceptionTypes.length; i++) {
                    exceptionTypes[i] = Type.getType(method.getExceptionTypes()[i]);
                }

                im.add(sig, exceptionTypes);
            }

            im.setClassLoader(getClassLoader());
            im.setNamingPolicy(new DefaultNamingPolicy() {
                @Override
                public String getClassName(String prefix, String source, Object key, Predicate names) {
                    return super.getClassName(EMPTY_STRING, getSimpleClassName(StaticFunctionDelegatorBuilder.class),
                            key, names);
                }
            });

            mixinInterface = im.create();
        }

        return mixinInterface;
    }

    public Object toObject() {
        final Class<?> intfs = getMixinInterface();
        final Map<Method, FastMethod> methodMappings = getMethodMappings(intfs);

        Enhancer generator = new Enhancer();

        generator.setClassLoader(getClassLoader());
        generator.setSuperclass(Object.class);
        generator.setInterfaces(new Class<?>[] { intfs });

        generator.setCallbacks(new Callback[] {
                // default callback
                new MethodInterceptor() {
                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                            throws Throwable {
                        return proxy.invokeSuper(obj, args);
                    }
                },

                // toString callback
                new MethodInterceptor() {
                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                            throws Throwable {
                        MapBuilder mb = new MapBuilder().setPrintCount(true).setSortKeys(true);

                        for (Map.Entry<Method, FastMethod> entry : methodMappings.entrySet()) {
                            mb.append(entry.getKey().getName(), entry.getValue().getJavaMethod());
                        }

                        return new ToStringBuilder().append(intfs.getName()).append(mb).toString();
                    }
                },

                // proxied callback
                new MethodInterceptor() {
                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                            throws Throwable {
                        FastMethod realMethod = assertNotNull(methodMappings.get(method), "unknown method: %s", method);
                        return realMethod.invoke(null, args);
                    }
                } });

        generator.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
                if (isEqualsMethod(method) || isHashCodeMethod(method)) {
                    return 0; // invoke super
                } else if (isToStringMethod(method)) {
                    return 1; // invoke toString
                } else {
                    return 2; // invoke proxied object
                }
            }
        });

        Object obj = generator.create();

        log.debug("Generated mixin function delegator: {}", obj);

        return obj;
    }

    private Map<Method, FastMethod> getMethodMappings(Class<?> intfs) {
        // 查找interface中的方法和被代理方法之间的对应关系
        Map<Method, FastMethod> methodMappings = createHashMap();

        for (Method method : intfs.getMethods()) {
            Signature sig = getSignature(method, null);
            Method realMethod = assertNotNull(methods.get(sig), "unknown method signature: %s", sig);
            FastClass fc = FastClass.create(getClassLoader(), realMethod.getDeclaringClass());
            FastMethod fm = fc.getMethod(realMethod);

            methodMappings.put(method, fm);
        }

        return methodMappings;
    }
}
