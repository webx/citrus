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
package com.alibaba.citrus.test.runner;

import static org.junit.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import com.alibaba.citrus.test.TestUtil;

/**
 * 类似{@link Parameterized}，作了如下改进和变化：
 * <p>
 * <ul>
 * <li>通过原型对象来创建test case对象</li> *
 * <li>可通过@TestName注释指定的方法（非static方法）来设置测试组的名称，如“[0] xxx”，而不只是[0]、[1]、[2]。</li>
 * <li>支持多个@Prototypes方法。</li>
 * <li>支持<code>TestUtil.getTestName()</code>，以便在测试中取得当前测试的名称。</li>
 * </ul>
 * </p>
 */
public class Prototyped extends Suite {
    /**
     * 系统将通过标记此注释的方法，取得测试的名称。
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface TestName {
    }

    /**
     * 系统将通过标记此注释的方法，取得测试的数据。
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Prototypes {
    }

    /**
     * 用来简化Prototyped测试。
     */
    public static class TestData<T> extends LinkedList<T> {
        private static final long serialVersionUID = 2818372350747718688L;
        private final Class<T> prototypeClass;

        public static <T> TestData<T> getInstance(Class<T> prototypeClass) {
            return new TestData<T>(prototypeClass);
        }

        public TestData(Class<T> prototypeClass) {
            assertNotNull("prototypeClass not specified", prototypeClass);
            this.prototypeClass = prototypeClass;
        }

        public T newPrototype() {
            T prototype = null;

            try {
                prototype = prototypeClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            super.add(prototype);
            return prototype;
        }
    }

    private static class TestClassRunnerForPrototypes extends BlockJUnit4ClassRunner {
        private final Object fPrototype;
        private final int fPrototypeNumber;

        TestClassRunnerForPrototypes(Class<?> type, Object prototype, int i) throws InitializationError {
            super(type);
            fPrototype = prototype;
            fPrototypeNumber = i;
        }

        @Override
        public Object createTest() throws Exception {
            if (fPrototype instanceof Cloneable && getTestClass().getJavaClass().isInstance(fPrototype)) {
                Method cloneMethod = null;

                for (Class<?> clazz = fPrototype.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                    try {
                        cloneMethod = clazz.getDeclaredMethod("clone");
                        cloneMethod.setAccessible(true);
                        break;
                    } catch (NoSuchMethodException e) {
                    }
                }

                return cloneMethod.invoke(fPrototype);
            }

            fail(String.format("Class %s is not Cloneable", getTestClass().getJavaClass().getSimpleName()));

            return null;
        }

        @Override
        protected String getName() {
            List<FrameworkMethod> methods = new ArrayList<FrameworkMethod>(getTestClass().getAnnotatedMethods(
                    TestName.class));

            for (FrameworkMethod each : methods) {
                int modifiers = each.getMethod().getModifiers();

                if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)
                        && String.class.equals(each.getMethod().getReturnType())
                        && each.getMethod().getParameterTypes().length == 0) {
                    String name = null;

                    try {
                        name = (String) each.invokeExplosively(fPrototype);
                        assertNotNull(String.format("%s.%s() returned null", getTestClass().getName(), each.getName()),
                                name);
                        return String.format("[%s] %s", fPrototypeNumber, name);
                    } catch (Error e) {
                        throw e;
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new RuntimeException(String.format(
                            "%s.%s() should be public, non-static, accept no arguments, and return String",
                            getTestClass().getName(), each.getName()));
                }
            }

            return String.format("[%s]", fPrototypeNumber);
        }

        @Override
        protected String testName(final FrameworkMethod method) {
            return String.format("%s[%s]", method.getName(), fPrototypeNumber);
        }

        @Override
        protected void validateZeroArgConstructor(List<Throwable> errors) {
            // constructor can, nay, should have args.
        }

        @Override
        protected Statement classBlock(RunNotifier notifier) {
            return childrenInvoker(notifier);
        }

        @Override
        protected void runChild(FrameworkMethod method, RunNotifier notifier) {
            TestUtil.setTestName(method.getName());

            try {
                super.runChild(method, notifier);
            } finally {
                TestUtil.setTestName(null);
            }
        }
    }

    /**
     * Only called reflectively. Do not use programmatically.
     */
    public Prototyped(Class<?> klass) throws Throwable {
        super(klass, getRunners(klass));
    }

    private static List<Runner> getRunners(Class<?> klass) throws Throwable, InitializationError {
        List<Runner> runners = new ArrayList<Runner>();

        int i = 0;
        for (final Object each : getPrototypesList(klass)) {
            runners.add(new TestClassRunnerForPrototypes(klass, each, i++));
        }

        return runners;
    }

    private static Collection<Object> getPrototypesList(Class<?> klass) throws Throwable {
        Collection<Object> prototypeList = new ArrayList<Object>();

        for (FrameworkMethod method : getPrototypesMethods(klass)) {
            @SuppressWarnings("unchecked")
            Collection<Object> results = (Collection<Object>) method.invokeExplosively(null);

            for (final Object each : results) {
                if (!klass.isInstance(each)) {
                    throw new Exception(String.format("%s.%s() must return a Collection of test object.",
                            klass.getName(), method.getName()));
                }
            }

            prototypeList.addAll(results);
        }

        return prototypeList;
    }

    private static List<FrameworkMethod> getPrototypesMethods(Class<?> testClass) throws Exception {
        List<FrameworkMethod> methods = new TestClass(testClass).getAnnotatedMethods(Prototypes.class);

        if (methods.isEmpty()) {
            throw new Exception("No public static prototypes method on class " + testClass.getName());
        }

        for (FrameworkMethod each : methods) {
            int modifiers = each.getMethod().getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
                throw new Exception(String.format("%s.%s() should be public static method: ", testClass.getName(),
                        each.getName()));
            }
        }

        return methods;
    }
}
