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

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/**
 * Servlet 3.0 Support － 即使在非servlet 3.0的环境中，也不会出错。
 *
 * @author Michael Zhou
 */
public class Servlet3Util {
    private static final boolean      servlet3;
    private static final MethodInfo[] methods;
    private static final int          request_isAsyncStarted;
    private static final int          request_getAsyncContext;

    private static boolean disableServlet3Features = false;

    static {
        boolean isServlet3;

        try {
            Servlet3Util.class.getClassLoader().loadClass("javax.servlet.AsyncContext");
            isServlet3 = true;
        } catch (ClassNotFoundException e) {
            isServlet3 = false;
        }

        servlet3 = isServlet3;

        List<MethodInfo> methodList = createLinkedList();
        int count = 0;

        methodList.add(new MethodInfo(Boolean.class, false, HttpServletRequest.class, "isAsyncStarted"));
        request_isAsyncStarted = count++;

        methodList.add(new MethodInfo(Object.class, null, HttpServletRequest.class, "getAsyncContext"));
        request_getAsyncContext = count++;

        methods = methodList.toArray(new MethodInfo[methodList.size()]);
    }

    public static boolean isServlet3() {
        return servlet3;
    }

    /**
     * 设置强制禁用servlet 3.0特性。
     * 有一种情况：当用httpunit测试时，虽然存在servlet 3.0的API包，但是由于httpunit未实现servlet 3.0而报错。
     * 在这种情况下，可强制禁用servlet 3.0，让测试通过。
     */
    public static boolean setDisableServlet3Features(boolean disabled) {
        boolean originalValue = disableServlet3Features;
        disableServlet3Features = disabled;
        return originalValue;
    }

    public static boolean isAsyncStarted(HttpServletRequest request) {
        return (Boolean) invoke(request_isAsyncStarted, request);
    }

    public static Object /* AsyncContext */ getAsyncContext(HttpServletRequest request) {
        int index = request_getAsyncContext;

        if (methods[index].isDisabled()) {
            throw new IllegalStateException("request.getAsyncContext");
        }

        return invoke(index, request);
    }

    private static Object invoke(int methodIndex, Object target, Object... args) {
        MethodInfo method = methods[methodIndex];

        if (method.isDisabled()) {
            return method.defaultReturnValue;
        }

        try {
            if (method.returnValueType == null) {
                method.method.invoke(target, args);
                return null;
            } else {
                return method.returnValueType.cast(method.method.invoke(target, args));
            }
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    private static class MethodInfo {
        private final FastMethod method;
        private final Object     defaultReturnValue;
        private final Class<?>   returnValueType;

        public <T> MethodInfo(Class<T> returnValueType, T defaultReturnValue, Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
            this.returnValueType = returnValueType;
            this.defaultReturnValue = defaultReturnValue;

            Method javaMethod;
            FastMethod method;

            try {
                javaMethod = declaringClass.getMethod(methodName, parameterTypes);
                method = FastClass.create(declaringClass).getMethod(javaMethod);
            } catch (NoSuchMethodException e) {
                method = null;
            }

            this.method = method;
        }

        public boolean isDisabled() {
            return disableServlet3Features || method == null;
        }
    }
}
