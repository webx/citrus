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
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/**
 * Servlet 3.0 Support － 即使在非servlet 3.0的环境中，也不会出错。
 * 此类会引用如下几个Servlet 3.0的接口。在Servlet 2.5的环境中，接口由<code>citrus-common-servlet</code>项目提供。
 * <ul>
 * <li><code>AsyncContext</code></li>
 * <li><code>AsyncEvent</code></li>
 * <li><code>AsyncListener</code></li>
 * <li><code>WriteListener</code></li>
 * </ul>
 *
 * @author Michael Zhou
 */
public class Servlet3Util {
    public static final Enum<?> DISPATCHER_TYPE_FORWARD = getEnum("javax.servlet.DispatcherType", "FORWARD");
    public static final Enum<?> DISPATCHER_TYPE_INCLUDE = getEnum("javax.servlet.DispatcherType", "INCLUDE");
    public static final Enum<?> DISPATCHER_TYPE_REQUEST = getEnum("javax.servlet.DispatcherType", "REQUEST");
    public static final Enum<?> DISPATCHER_TYPE_ASYNC   = getEnum("javax.servlet.DispatcherType", "ASYNC");
    public static final Enum<?> DISPATCHER_TYPE_ERROR   = getEnum("javax.servlet.DispatcherType", "ERROR");

    private static final MethodInfo[] methods;
    private static final int          request_isAsyncStarted;
    private static final int          request_getAsyncContext;
    private static final int          request_getDispatcherType;
    private static final int          servletOutputStream_isReady;
    private static final int          servletOutputStream_setWriteListener;

    private static boolean disableServlet3Features = false;
    private static final boolean servlet3;

    static {
        List<MethodInfo> methodList = createLinkedList();
        int count = 0;

        methodList.add(new MethodInfo(Boolean.class, false, HttpServletRequest.class, "isAsyncStarted"));
        request_isAsyncStarted = count++;

        methodList.add(new MethodInfo(Object.class, null, HttpServletRequest.class, "getAsyncContext"));
        request_getAsyncContext = count++;

        methodList.add(new MethodInfo(Enum.class, null, HttpServletRequest.class, "getDispatcherType"));
        request_getDispatcherType = count++;

        methodList.add(new MethodInfo(Boolean.class, true, ServletOutputStream.class, "isReady"));
        servletOutputStream_isReady = count++;

        methodList.add(new MethodInfo(null, null, ServletOutputStream.class, "setWriteListener", WriteListener.class));
        servletOutputStream_setWriteListener = count++;

        methods = methodList.toArray(new MethodInfo[methodList.size()]);

        servlet3 = !methods[request_getAsyncContext].isDisabled();
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

    public static boolean request_isAsyncStarted(HttpServletRequest request) {
        return (Boolean) invoke(request_isAsyncStarted, request);
    }

    public static AsyncContext request_getAsyncContext(HttpServletRequest request) {
        int index = request_getAsyncContext;

        if (methods[index].isDisabled()) {
            throw new IllegalStateException("request.getAsyncContext");
        }

        return (AsyncContext) invoke(index, request);
    }

    public static boolean request_isDispatcherType(HttpServletRequest request, Enum<?> type) {
        Enum<?> dispatcherType = (Enum<?>) invoke(request_getDispatcherType, request);

        if (dispatcherType == null || type == null) {
            return false; // unsupported
        } else {
            return dispatcherType == type;
        }
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

    private static Enum<?> getEnum(String className, String name) {
        Class<?> enumClass = null;

        try {
            enumClass = Servlet3Util.class.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }

        assertTrue(Enum.class.isAssignableFrom(enumClass), "%s is not a enum class", enumClass.getName());

        try {
            return (Enum<?>) enumClass.getField(name).get(null);
        } catch (Exception e) {
            unexpectedException(e);
            return null;
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
            FastMethod method = null;

            if (declaringClass != null) {
                try {
                    javaMethod = declaringClass.getMethod(methodName, parameterTypes);
                    method = FastClass.create(getClass().getClassLoader(), declaringClass).getMethod(javaMethod);
                } catch (NoSuchMethodException e) {
                }
            }

            this.method = method;
        }

        public boolean isDisabled() {
            return disableServlet3Features || method == null;
        }
    }

    /** 一个可同时在servlet 3.0和servlet 2.5环境下使用的基类。 */
    public static abstract class Servlet3OutputStream extends ServletOutputStream {
        protected final ServletOutputStream originalStream;

        public Servlet3OutputStream(ServletOutputStream originalStream) {
            this.originalStream = originalStream;
        }

        // @Override
        public boolean isReady() {
            if (originalStream != null) {
                return (Boolean) invoke(servletOutputStream_isReady, originalStream);
            }

            return true;
        }

        // @Override
        public void setWriteListener(WriteListener writeListener) {
            if (originalStream != null) {
                invoke(servletOutputStream_setWriteListener, originalStream, writeListener);
            }
        }
    }
}
