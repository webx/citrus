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
package com.alibaba.citrus.test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matcher;
import org.slf4j.MDC;

/**
 * 方便测试的工具类。
 * 
 * @author Michael Zhou
 */
public class TestUtil {
    private final static ThreadLocal<String> TEST_NAME_HOLDER = new ThreadLocal<String>();

    public static String getTestName() {
        return TEST_NAME_HOLDER.get();
    }

    public static void setTestName(String name) {
        if (name == null) {
            TEST_NAME_HOLDER.remove();
            MDC.remove("testName");
        } else {
            TEST_NAME_HOLDER.set(name);
            MDC.put("testName", name);
        }
    }

    public static <T extends Throwable> Matcher<T> exception(Class<? extends Throwable> cause, String... snippets) {
        return new ExceptionMatcher<T>(cause, snippets);
    }

    public static <T extends Throwable> Matcher<T> exception(String... snippets) {
        return new ExceptionMatcher<T>(snippets);
    }

    public static Matcher<String> containsRegex(String regex) {
        return new RegexMatcher(regex);
    }

    public static Matcher<String> containsAll(String... strs) {
        List<Matcher<? extends String>> list = new ArrayList<Matcher<? extends String>>();

        for (String str : strs) {
            list.add(containsString(str));
        }

        return allOf(list);
    }

    public static Matcher<String> containsAllRegex(String... regexes) {
        List<Matcher<? extends String>> list = new ArrayList<Matcher<? extends String>>();

        for (String regex : regexes) {
            list.add(containsRegex(regex));
        }

        return allOf(list);
    }

    public static File getJavaHome() {
        File javaHome = new File(System.getProperty("java.home"));

        if ("jre".equals(javaHome.getName())) {
            javaHome = javaHome.getParentFile();
        }

        return javaHome;
    }

    public static File getClassesDir(Class<?> classWithinDir) {
        return getClassesDir(classWithinDir == null ? null : classWithinDir.getName());
    }

    public static File getClassesDir(String classWithinDir) {
        String clazzResourceName = getResourceNameOfClass(classWithinDir) + ".class";
        File classFile;

        try {
            classFile = new File(Thread.currentThread().getContextClassLoader().getResource(clazzResourceName).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not find classes dir of " + classWithinDir, e);
        }

        File classesdir = classFile.getParentFile();

        for (; classesdir != null && classesdir.isDirectory(); classesdir = classesdir.getParentFile()) {
            if (new File(classesdir, clazzResourceName).equals(classFile)) {
                break;
            }
        }

        if (!classFile.equals(new File(classesdir, clazzResourceName))) {
            throw new RuntimeException("Could not find classes dir of " + classWithinDir);
        }

        return classesdir;
    }

    public static File[] getClassDirs(Class<?>... classes) {
        Set<File> dirs = new LinkedHashSet<File>();

        for (Class<?> clazz : classes) {
            dirs.add(getClassesDir(clazz));
        }

        return dirs.toArray(new File[dirs.size()]);
    }

    private static String getResourceNameOfClass(String className) {
        if (className == null) {
            return null;
        }

        return className.trim().replace('.', '/');
    }

    /**
     * 取得field，并设置为可访问。
     */
    public static Field getAccessibleField(Class<?> targetType, String fieldName) {
        assertNotNull("missing targetType", targetType);

        Field field = null;

        for (Class<?> c = targetType; c != null && field == null; c = c.getSuperclass()) {
            try {
                field = c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            } catch (Exception e) {
                fail(e.toString());
                return null;
            }
        }

        assertNotNull("field " + fieldName + " not found in " + targetType, field);

        field.setAccessible(true);

        return field;
    }

    /**
     * 取得method，并设置为可访问。
     */
    public static Method getAccessibleMethod(Class<?> targetType, String methodName, Class<?>[] argTypes) {
        assertNotNull("missing targetType", targetType);

        Method method = null;

        for (Class<?> c = targetType; c != null && method == null; c = c.getSuperclass()) {
            try {
                method = c.getDeclaredMethod(methodName, argTypes);
            } catch (NoSuchMethodException e) {
            } catch (Exception e) {
                fail(e.toString());
                return null;
            }
        }

        assertNotNull("method " + methodName + " not found in " + targetType, method);

        method.setAccessible(true);

        return method;
    }

    /**
     * 取得field值，即使private也可以。
     */
    public static <T> T getFieldValue(Object target, String fieldName, Class<T> fieldType) {
        return getFieldValue(target, null, fieldName, fieldType);
    }

    /**
     * 取得field值，即使private也可以。
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object target, Class<?> targetType, String fieldName, Class<T> fieldType) {
        if (targetType == null && target != null) {
            targetType = target.getClass();
        }

        Field field = getAccessibleField(targetType, fieldName);
        Object value = null;

        try {
            value = field.get(target);
        } catch (Exception e) {
            fail(e.toString());
            return null;
        }

        if (fieldType != null) {
            return fieldType.cast(value);
        } else {
            return (T) value;
        }
    }

    /**
     * 执行方法，即使private也没关系。
     */
    public static <T> T invokeMethod(Object target, String methodName, Class<?>[] argTypes, Object[] args,
                                     Class<T> returnType) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        return invokeMethod(target, null, methodName, argTypes, args, returnType);
    }

    /**
     * 执行方法，即使private也没关系。
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object target, Class<?> targetType, String methodName, Class<?>[] argTypes,
                                     Object[] args, Class<T> returnType) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        if (targetType == null && target != null) {
            targetType = target.getClass();
        }

        Method method = getAccessibleMethod(targetType, methodName, argTypes);
        Object value = method.invoke(target, args);

        if (returnType != null) {
            return returnType.cast(value);
        } else {
            return (T) value;
        }
    }
}
