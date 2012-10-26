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

package com.alibaba.citrus.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;

/**
 * 测试<code>ClassUtil</code>。
 *
 * @author Michael Zhou
 */
public class ClassUtilTests {

    // ==========================================================================
    // 取得友好类名和package名的方法。
    // ==========================================================================

    @Test
    public void getFriendlyClassName() {
        // null
        assertNull(ClassUtil.getFriendlyClassNameForObject(null));

        assertNull(ClassUtil.getFriendlyClassName((Class<?>) null));

        assertNull(ClassUtil.getFriendlyClassName((String) null));

        assertEquals("  ", ClassUtil.getFriendlyClassName("  "));

        // 数组
        assertGetFriendlyClassName("int[]", new int[0]);
        assertGetFriendlyClassName("int[][]", new int[0][]);
        assertGetFriendlyClassName("long[]", new long[0]);
        assertGetFriendlyClassName("long[][]", new long[0][]);
        assertGetFriendlyClassName("short[]", new short[0]);
        assertGetFriendlyClassName("short[][]", new short[0][]);
        assertGetFriendlyClassName("byte[]", new byte[0]);
        assertGetFriendlyClassName("byte[][]", new byte[0][]);
        assertGetFriendlyClassName("char[]", new char[0]);
        assertGetFriendlyClassName("char[][]", new char[0][]);
        assertGetFriendlyClassName("boolean[]", new boolean[0]);
        assertGetFriendlyClassName("boolean[][]", new boolean[0][]);
        assertGetFriendlyClassName("float[]", new float[0]);
        assertGetFriendlyClassName("float[][]", new float[0][]);
        assertGetFriendlyClassName("double[]", new double[0]);
        assertGetFriendlyClassName("double[][]", new double[0][]);
        assertGetFriendlyClassName("java.util.List[]", new List[0]);
        assertGetFriendlyClassName("java.util.List[][]", new List[0][]);

        // 非数组
        assertGetFriendlyClassName("java.util.ArrayList", createArrayList());

        // 内联类/本地类/匿名类
        assertGetFriendlyClassName("com.alibaba.citrus.util.ClassUtilTests.Inner", new Inner());
        assertGetFriendlyClassName("com.alibaba.citrus.util.ClassUtilTests.Inner[]", new Inner[0]);

        class Local {
        }

        assertGetFriendlyClassName("com.alibaba.citrus.util.ClassUtilTests.1Local", new Local());
        assertGetFriendlyClassName("com.alibaba.citrus.util.ClassUtilTests.1Local[]", new Local[0]);

        Object anonymous = new Serializable() {
            private static final long serialVersionUID = 4375828012902534105L;
        };

        assertGetFriendlyClassName("com.alibaba.citrus.util.ClassUtilTests.1", anonymous);

        // 非法类名
        assertEquals("", ClassUtil.getFriendlyClassName(""));
        assertEquals("[", ClassUtil.getFriendlyClassName("["));
        assertEquals("[[", ClassUtil.getFriendlyClassName("[["));
        assertEquals("[[X", ClassUtil.getFriendlyClassName("[[X"));
        assertEquals("[[L", ClassUtil.getFriendlyClassName("[[L"));
        assertEquals("[[L;", ClassUtil.getFriendlyClassName("[[L;"));
        assertEquals("[[Lx", ClassUtil.getFriendlyClassName("[[Lx"));
    }

    private void assertGetFriendlyClassName(String result, Object object) {
        // object
        assertEquals(result, ClassUtil.getFriendlyClassNameForObject(object));

        // class
        assertEquals(result, ClassUtil.getFriendlyClassName(object.getClass()));

        // class name
        assertEquals(result, ClassUtil.getFriendlyClassName(object.getClass().getName()));
        assertEquals(result, ClassUtil.getFriendlyClassName("  " + object.getClass().getName() + "  ")); // trim
    }

    @Test
    public void getSimpleClassName() {
        // null
        assertNull(ClassUtil.getSimpleClassNameForObject(null));
        assertNull(ClassUtil.getSimpleClassName((Class<?>) null));
        assertNull(ClassUtil.getSimpleClassName((String) null));

        assertEquals("  ", ClassUtil.getSimpleClassName("  "));

        // 普通类
        assertGetSimpleClassName("ClassUtil", new ClassUtil(), true);
        assertGetSimpleClassName("String", "hello", true);

        // 内联类
        assertGetSimpleClassName("ClassUtilTests.Inner", new Inner(), true);
        assertGetSimpleClassName("ClassUtilTests$Inner", new Inner(), false);

        // 内联类/本地类/匿名类
        assertGetSimpleClassName("ClassUtilTests.Inner", new Inner(), true);
        assertGetSimpleClassName("ClassUtilTests$Inner[]", new Inner[0], false);

        class Local {
        }

        assertGetSimpleClassName("ClassUtilTests.2Local", new Local(), true);
        assertGetSimpleClassName("ClassUtilTests$2Local", new Local(), false);
        assertGetSimpleClassName("ClassUtilTests.2Local[]", new Local[0], true);
        assertGetSimpleClassName("ClassUtilTests$2Local[]", new Local[0], false);

        Object anonymous = new Serializable() {
            private static final long serialVersionUID = 4375828012902534105L;
        };

        assertGetSimpleClassName("ClassUtilTests.2", anonymous, true);
        assertGetSimpleClassName("ClassUtilTests$2", anonymous, false);

        // 数组
        assertGetSimpleClassName("int[]", new int[0], true);
        assertGetSimpleClassName("int[][]", new int[0][], true);
        assertGetSimpleClassName("long[]", new long[0], true);
        assertGetSimpleClassName("long[][]", new long[0][], true);
        assertGetSimpleClassName("short[]", new short[0], true);
        assertGetSimpleClassName("short[][]", new short[0][], true);
        assertGetSimpleClassName("byte[]", new byte[0], true);
        assertGetSimpleClassName("byte[][]", new byte[0][], true);
        assertGetSimpleClassName("char[]", new char[0], true);
        assertGetSimpleClassName("char[][]", new char[0][], true);
        assertGetSimpleClassName("boolean[]", new boolean[0], true);
        assertGetSimpleClassName("boolean[][]", new boolean[0][], true);
        assertGetSimpleClassName("float[]", new float[0], true);
        assertGetSimpleClassName("float[][]", new float[0][], true);
        assertGetSimpleClassName("double[]", new double[0], true);
        assertGetSimpleClassName("double[][]", new double[0][], true);
        assertGetSimpleClassName("String[]", new String[0], true);
        assertGetSimpleClassName("Map.Entry[]", new Map.Entry[0], true);
        assertGetSimpleClassName("Map$Entry[]", new Map.Entry[0], false);

        // 非法类名
        assertEquals("", ClassUtil.getSimpleClassName(""));
        assertEquals("[", ClassUtil.getSimpleClassName("["));
        assertEquals("[[", ClassUtil.getSimpleClassName("[["));
        assertEquals("[[X", ClassUtil.getSimpleClassName("[[X"));
        assertEquals("[[L", ClassUtil.getSimpleClassName("[[L"));
        assertEquals("[[L;", ClassUtil.getSimpleClassName("[[L;"));
        assertEquals("[[Lx", ClassUtil.getSimpleClassName("[[Lx"));
    }

    private void assertGetSimpleClassName(String result, Object object, boolean proccessInnerClass) {
        if (proccessInnerClass) {
            // object
            assertEquals(result, ClassUtil.getSimpleClassNameForObject(object));
            assertEquals(result, ClassUtil.getSimpleClassNameForObject(object, true));

            // class
            assertEquals(result, ClassUtil.getSimpleClassName(object.getClass()));
            assertEquals(result, ClassUtil.getSimpleClassName(object.getClass(), true));

            // class name
            assertEquals(result, ClassUtil.getSimpleClassName(object.getClass().getName()));
            assertEquals(result, ClassUtil.getSimpleClassName(object.getClass().getName(), true));
            assertEquals(result, ClassUtil.getSimpleClassName("  " + object.getClass().getName() + "  ")); // trim
            assertEquals(result, ClassUtil.getSimpleClassName("  " + object.getClass().getName() + "  ", true)); // trim
        } else {
            // object
            assertEquals(result, ClassUtil.getSimpleClassNameForObject(object, proccessInnerClass));

            // class
            assertEquals(result, ClassUtil.getSimpleClassName(object.getClass(), proccessInnerClass));

            // class name
            assertEquals(result, ClassUtil.getSimpleClassName(object.getClass().getName(), proccessInnerClass));
            assertEquals(result,
                         ClassUtil.getSimpleClassName("  " + object.getClass().getName() + "  ", proccessInnerClass)); // trim
        }
    }

    @SuppressWarnings("unused")
    static class MyClass {
        public final void func() {
        }

        private static int func(String s, long[] l) throws IOException, RuntimeException {
            return 0;
        }
    }

    @Test
    public void getSimpleMethodSignature() throws Exception {
        Method m1 = MyClass.class.getDeclaredMethod("func");
        Method m2 = MyClass.class.getDeclaredMethod("func", String.class, long[].class);

        // null
        assertMethod(null, null, false, false, false, false);

        // modifiers, returnType, className, exceptionTypes
        assertMethod("public final void ClassUtilTests.MyClass.func()", m1, true, true, true, true);
        assertMethod(
                "private static int ClassUtilTests.MyClass.func(String, long[]) throws IOException, RuntimeException",
                m2, true, true, true, true);

        // modifiers, returnType, className, exceptionTypes
        assertMethod("public final void ClassUtilTests.MyClass.func()", m1, true, true, true, true);
        assertMethod(
                "private static int ClassUtilTests.MyClass.func(String, long[]) throws IOException, RuntimeException",
                m2, true, true, true, true);

        // no modifiers, returnType, className, exceptionTypes
        assertMethod("void ClassUtilTests.MyClass.func()", m1, false, true, true, true);
        assertMethod("int ClassUtilTests.MyClass.func(String, long[]) throws IOException, RuntimeException", m2, false,
                     true, true, true);

        // no modifiers, no returnType, className, exceptionTypes
        assertMethod("ClassUtilTests.MyClass.func()", m1, false, false, true, true);
        assertMethod("ClassUtilTests.MyClass.func(String, long[]) throws IOException, RuntimeException", m2, false,
                     false, true, true);

        // no modifiers, no returnType, no className, exceptionTypes
        assertMethod("func()", m1, false, false, false, true);
        assertMethod("func(String, long[]) throws IOException, RuntimeException", m2, false, false, false, true);

        // no modifiers, no returnType, no className, no exceptionTypes
        assertMethod("func()", m1, false, false, false, false);
        assertMethod("func(String, long[])", m2, false, false, false, false);

        // simple version
        assertEquals(null, ClassUtil.getSimpleMethodSignature(null));
        assertEquals("func()", ClassUtil.getSimpleMethodSignature(m1));
        assertEquals("func(String, long[])", ClassUtil.getSimpleMethodSignature(m2));

        // simple version 2
        assertEquals(null, ClassUtil.getSimpleMethodSignature(null, true));
        assertEquals("ClassUtilTests.MyClass.func()", ClassUtil.getSimpleMethodSignature(m1, true));
        assertEquals("func()", ClassUtil.getSimpleMethodSignature(m1, false));
        assertEquals("ClassUtilTests.MyClass.func(String, long[])", ClassUtil.getSimpleMethodSignature(m2, true));
        assertEquals("func(String, long[])", ClassUtil.getSimpleMethodSignature(m2, false));
    }

    private void assertMethod(String str, Method method, boolean withModifiers, boolean withReturnType,
                              boolean withClassName, boolean withExceptionType) {
        assertEquals(str, ClassUtil.getSimpleMethodSignature(method, withModifiers, withReturnType, withClassName,
                                                             withExceptionType));
    }

    @Test
    public void getPackageName() {
        // null
        assertEquals("", ClassUtil.getPackageNameForObject(null));
        assertEquals("", ClassUtil.getPackageName((Class<?>) null));
        assertEquals("", ClassUtil.getPackageName((String) null));
        assertEquals("", ClassUtil.getPackageName("  "));

        // 数组
        assertGetPackageName("", new int[0]);
        assertGetPackageName("", new int[0][]);
        assertGetPackageName("", new long[0]);
        assertGetPackageName("", new long[0][]);
        assertGetPackageName("", new short[0]);
        assertGetPackageName("", new short[0][]);
        assertGetPackageName("", new byte[0]);
        assertGetPackageName("", new byte[0][]);
        assertGetPackageName("", new char[0]);
        assertGetPackageName("", new char[0][]);
        assertGetPackageName("", new boolean[0]);
        assertGetPackageName("", new boolean[0][]);
        assertGetPackageName("", new float[0]);
        assertGetPackageName("", new float[0][]);
        assertGetPackageName("", new double[0]);
        assertGetPackageName("", new double[0][]);
        assertGetPackageName("java.util", new List[0]);
        assertGetPackageName("java.util", new List[0][]);

        // 非数组
        assertGetPackageName("java.util", createArrayList());
        assertGetPackageName("com.alibaba.citrus.util", new ClassUtil());
        assertGetPackageName("com.alibaba.citrus.util", new Inner());
        assertGetPackageName("java.lang", new Integer[0]);

        // 内联类/本地类/匿名类
        assertGetPackageName("com.alibaba.citrus.util", new Inner());
        assertGetPackageName("com.alibaba.citrus.util", new Inner[0]);

        class Local {
        }

        assertGetPackageName("com.alibaba.citrus.util", new Local());
        assertGetPackageName("com.alibaba.citrus.util", new Local[0]);

        Object anonymous = new Serializable() {
            private static final long serialVersionUID = 4375828012902534105L;
        };

        assertGetPackageName("com.alibaba.citrus.util", anonymous);

        // 非法类名
        assertEquals("", ClassUtil.getPackageName(""));
        assertEquals("", ClassUtil.getPackageName("["));
        assertEquals("", ClassUtil.getPackageName("[["));
        assertEquals("", ClassUtil.getPackageName("[[X"));
        assertEquals("", ClassUtil.getPackageName("[[L"));
        assertEquals("", ClassUtil.getPackageName("[[L;"));
        assertEquals("", ClassUtil.getPackageName("[[Lx"));
    }

    private void assertGetPackageName(String result, Object object) {
        // object
        assertEquals(result, ClassUtil.getPackageNameForObject(object));

        // class
        assertEquals(result, ClassUtil.getPackageName(object.getClass()));

        // class name
        assertEquals(result, ClassUtil.getPackageName(object.getClass().getName()));
        assertEquals(result, ClassUtil.getPackageName("  " + object.getClass().getName() + "  ")); // trim
    }

    // ==========================================================================
    // 取得类名和package名的resource名的方法。
    //
    // 和类名、package名不同的是，resource名符合文件名命名规范，例如：
    // java/lang/String.class
    // com/alibaba/commons/lang
    // etc.
    // ==========================================================================

    @Test
    public void getResourceNameForObjectClass() {
        // null
        assertNull(ClassUtil.getResourceNameForObjectClass(null));

        // 非数组
        assertEquals("java/util/ArrayList.class", ClassUtil.getResourceNameForObjectClass(createArrayList()));

        // 内联类
        assertEquals("com/alibaba/citrus/util/ClassUtilTests$Inner.class",
                     ClassUtil.getResourceNameForObjectClass(new Inner()));
    }

    @Test
    public void getResourceNameForClass() {
        // null
        assertNull(ClassUtil.getResourceNameForClass((Class<?>) null));

        // 非数组
        assertEquals("java/util/ArrayList.class", ClassUtil.getResourceNameForClass(ArrayList.class));

        // 内联类
        assertEquals("com/alibaba/citrus/util/ClassUtilTests$Inner.class",
                     ClassUtil.getResourceNameForClass(Inner.class));
    }

    @Test
    public void getResourceNameForClassName() {
        // null
        assertNull(ClassUtil.getResourceNameForClass((String) null));

        // 非数组
        assertEquals("java/util/ArrayList.class", ClassUtil.getResourceNameForClass(ArrayList.class.getName()));

        // 内联类
        assertEquals("com/alibaba/citrus/util/ClassUtilTests$Inner.class",
                     ClassUtil.getResourceNameForClass(Inner.class.getName()));
    }

    @Test
    public void getResourceNameForObjectPackage() {
        assertNull(ClassUtil.getResourceNameForObjectPackage(null));
        assertEquals("com/alibaba/citrus/util", ClassUtil.getResourceNameForObjectPackage(new ClassUtil()));
        assertEquals("com/alibaba/citrus/util", ClassUtil.getResourceNameForObjectPackage(new Inner()));
        assertEquals("java/lang", ClassUtil.getResourceNameForObjectPackage(new Integer[0]));
        assertEquals("", ClassUtil.getResourceNameForObjectPackage(new int[0]));
    }

    @Test
    public void getResourceNameForPackage() {
        assertNull(ClassUtil.getResourceNameForPackage((Class<?>) null));
        assertEquals("java/lang", ClassUtil.getResourceNameForPackage(String.class));
        assertEquals("java/util", ClassUtil.getResourceNameForPackage(Map.Entry.class));
        assertEquals("java/lang", ClassUtil.getResourceNameForPackage(Integer[].class));
        assertEquals("", ClassUtil.getResourceNameForPackage(int[].class));
    }

    @Test
    public void getResourceNameForPackageName() {
        assertNull(ClassUtil.getResourceNameForPackage((String) null));
        assertEquals("com/alibaba/citrus/util", ClassUtil.getResourceNameForPackage(ClassUtil.class.getName()));
        assertEquals("java/util", ClassUtil.getResourceNameForPackage(Map.Entry.class.getName()));
        assertEquals("java/lang", ClassUtil.getResourceNameForPackage(Integer[].class.getName()));
        assertEquals("", ClassUtil.getResourceNameForPackage(int[].class.getName()));
    }

    // ==========================================================================
    // 取得数组类。
    // ==========================================================================

    @Test
    public void getArrayClass() {
        // dim < 0
        try {
            ClassUtil.getArrayClass(int.class, -1);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("wrong dimension: -1"));
        }

        // null
        assertNull(ClassUtil.getArrayClass(null, 1));

        // form 1
        assertEquals(int[].class, ClassUtil.getArrayClass(int.class));
        assertEquals(int[][].class, ClassUtil.getArrayClass(int[].class));

        assertEquals(Integer[].class, ClassUtil.getArrayClass(Integer.class));
        assertEquals(Integer[][].class, ClassUtil.getArrayClass(Integer[].class));

        // form 2
        assertEquals(int.class, ClassUtil.getArrayClass(int.class, 0));
        assertEquals(int[].class, ClassUtil.getArrayClass(int.class, 1));
        assertEquals(int[][].class, ClassUtil.getArrayClass(int.class, 2));

        assertEquals(int[].class, ClassUtil.getArrayClass(int[].class, 0));
        assertEquals(int[][].class, ClassUtil.getArrayClass(int[].class, 1));
        assertEquals(int[][][].class, ClassUtil.getArrayClass(int[].class, 2));

        assertEquals(String.class, ClassUtil.getArrayClass(String.class, 0));
        assertEquals(String[].class, ClassUtil.getArrayClass(String.class, 1));
        assertEquals(String[][].class, ClassUtil.getArrayClass(String.class, 2));

        assertEquals(ClassUtilTests[].class, ClassUtil.getArrayClass(ClassUtilTests[].class, 0));
        assertEquals(ClassUtilTests[][].class, ClassUtil.getArrayClass(ClassUtilTests[].class, 1));
        assertEquals(ClassUtilTests[][][].class, ClassUtil.getArrayClass(ClassUtilTests[].class, 2));
    }

    // ==========================================================================
    // 取得原子类型或者其wrapper类。
    // ==========================================================================

    @Test
    public void getPrimitiveType_byName() {
        assertEquals(int.class, ClassUtil.getPrimitiveType("int"));
        assertEquals(long.class, ClassUtil.getPrimitiveType("long"));
        assertEquals(short.class, ClassUtil.getPrimitiveType("short"));
        assertEquals(double.class, ClassUtil.getPrimitiveType("double"));
        assertEquals(float.class, ClassUtil.getPrimitiveType("float"));
        assertEquals(char.class, ClassUtil.getPrimitiveType("char"));
        assertEquals(byte.class, ClassUtil.getPrimitiveType("byte"));
        assertEquals(boolean.class, ClassUtil.getPrimitiveType("boolean"));
        assertEquals(void.class, ClassUtil.getPrimitiveType("void"));

        assertEquals(int.class, ClassUtil.getPrimitiveType("java.lang.Integer"));
        assertEquals(long.class, ClassUtil.getPrimitiveType("java.lang.Long"));
        assertEquals(short.class, ClassUtil.getPrimitiveType("java.lang.Short"));
        assertEquals(double.class, ClassUtil.getPrimitiveType("java.lang.Double"));
        assertEquals(float.class, ClassUtil.getPrimitiveType("java.lang.Float"));
        assertEquals(char.class, ClassUtil.getPrimitiveType("java.lang.Character"));
        assertEquals(byte.class, ClassUtil.getPrimitiveType("java.lang.Byte"));
        assertEquals(boolean.class, ClassUtil.getPrimitiveType("java.lang.Boolean"));
        assertEquals(void.class, ClassUtil.getPrimitiveType("java.lang.Void"));

        assertEquals(null, ClassUtil.getPrimitiveType("java.lang.String"));
    }

    @Test
    public void getPrimitiveType_byType() {
        assertEquals(int.class, ClassUtil.getPrimitiveType(Integer.class));
        assertEquals(long.class, ClassUtil.getPrimitiveType(Long.class));
        assertEquals(short.class, ClassUtil.getPrimitiveType(Short.class));
        assertEquals(double.class, ClassUtil.getPrimitiveType(Double.class));
        assertEquals(float.class, ClassUtil.getPrimitiveType(Float.class));
        assertEquals(char.class, ClassUtil.getPrimitiveType(Character.class));
        assertEquals(byte.class, ClassUtil.getPrimitiveType(Byte.class));
        assertEquals(boolean.class, ClassUtil.getPrimitiveType(Boolean.class));
        assertEquals(void.class, ClassUtil.getPrimitiveType(Void.class));

        assertEquals(null, ClassUtil.getPrimitiveType(String.class));
    }

    @Test
    public void getWrapperTypeIfPrimitive() {
        assertEquals(Integer.class, ClassUtil.getWrapperTypeIfPrimitive(int.class));
        assertEquals(Long.class, ClassUtil.getWrapperTypeIfPrimitive(long.class));
        assertEquals(Short.class, ClassUtil.getWrapperTypeIfPrimitive(short.class));
        assertEquals(Double.class, ClassUtil.getWrapperTypeIfPrimitive(double.class));
        assertEquals(Float.class, ClassUtil.getWrapperTypeIfPrimitive(float.class));
        assertEquals(Character.class, ClassUtil.getWrapperTypeIfPrimitive(char.class));
        assertEquals(Byte.class, ClassUtil.getWrapperTypeIfPrimitive(byte.class));
        assertEquals(Boolean.class, ClassUtil.getWrapperTypeIfPrimitive(boolean.class));
        assertEquals(Void.class, ClassUtil.getWrapperTypeIfPrimitive(void.class));

        assertEquals(int[][].class, ClassUtil.getWrapperTypeIfPrimitive(int[][].class));
        assertEquals(long[][].class, ClassUtil.getWrapperTypeIfPrimitive(long[][].class));
        assertEquals(short[][].class, ClassUtil.getWrapperTypeIfPrimitive(short[][].class));
        assertEquals(double[][].class, ClassUtil.getWrapperTypeIfPrimitive(double[][].class));
        assertEquals(float[][].class, ClassUtil.getWrapperTypeIfPrimitive(float[][].class));
        assertEquals(char[][].class, ClassUtil.getWrapperTypeIfPrimitive(char[][].class));
        assertEquals(byte[][].class, ClassUtil.getWrapperTypeIfPrimitive(byte[][].class));
        assertEquals(boolean[][].class, ClassUtil.getWrapperTypeIfPrimitive(boolean[][].class));

        assertEquals(String.class, ClassUtil.getWrapperTypeIfPrimitive(String.class));

        assertEquals(String[][].class, ClassUtil.getWrapperTypeIfPrimitive(String[][].class));
    }

    @Test
    public void getPrimitiveDefaultValue() {
        assertEquals(new Integer(0), ClassUtil.getPrimitiveDefaultValue(int.class));
        assertEquals(new Long(0), ClassUtil.getPrimitiveDefaultValue(long.class));
        assertEquals(new Short((short) 0), ClassUtil.getPrimitiveDefaultValue(short.class));
        assertEquals(new Double(0), ClassUtil.getPrimitiveDefaultValue(double.class));
        assertEquals(new Float(0), ClassUtil.getPrimitiveDefaultValue(float.class));
        assertEquals(new Character('\0'), ClassUtil.getPrimitiveDefaultValue(char.class));
        assertEquals(new Byte((byte) 0), ClassUtil.getPrimitiveDefaultValue(byte.class));
        assertEquals(Boolean.FALSE, ClassUtil.getPrimitiveDefaultValue(boolean.class));
        assertEquals(null, ClassUtil.getPrimitiveDefaultValue(void.class));

        assertEquals(new Integer(0), ClassUtil.getPrimitiveDefaultValue(Integer.class));
        assertEquals(new Long(0), ClassUtil.getPrimitiveDefaultValue(Long.class));
        assertEquals(new Short((short) 0), ClassUtil.getPrimitiveDefaultValue(Short.class));
        assertEquals(new Double(0), ClassUtil.getPrimitiveDefaultValue(Double.class));
        assertEquals(new Float(0), ClassUtil.getPrimitiveDefaultValue(Float.class));
        assertEquals(new Character('\0'), ClassUtil.getPrimitiveDefaultValue(Character.class));
        assertEquals(new Byte((byte) 0), ClassUtil.getPrimitiveDefaultValue(Byte.class));
        assertEquals(Boolean.FALSE, ClassUtil.getPrimitiveDefaultValue(Boolean.class));
        assertEquals(null, ClassUtil.getPrimitiveDefaultValue(Void.class));

        assertEquals(null, ClassUtil.getPrimitiveDefaultValue(String.class));
        assertEquals(null, (Object) ClassUtil.getPrimitiveDefaultValue(long[][].class));
    }

    // ==========================================================================
    // 类型匹配。
    // ==========================================================================

    @Test
    public void isAssignable_classArray() {
        Class<?>[] array2 = new Class<?>[] { Object.class, Object.class };
        Class<?>[] array1 = new Class<?>[] { Object.class };
        Class<?>[] array1s = new Class<?>[] { String.class };
        Class<?>[] array0 = new Class<?>[] { };

        assertFalse(ClassUtil.isAssignable(array2, array1));
        assertFalse(ClassUtil.isAssignable(array2, null));
        assertTrue(ClassUtil.isAssignable(array0, null));
        assertTrue(ClassUtil.isAssignable(array0, array0));
        assertTrue(ClassUtil.isAssignable(null, array0));
        assertTrue(ClassUtil.isAssignable((Class<?>[]) null, (Class<?>[]) null));

        assertFalse(ClassUtil.isAssignable(array1s, array1));
        assertTrue(ClassUtil.isAssignable(array1s, array1s));
        assertTrue(ClassUtil.isAssignable(array1, array1s));
    }

    @Test
    public void isAssignable_class() {
        // clazz=null, fromClass=*
        assertFalse(ClassUtil.isAssignable((Class<?>) null, null));
        assertFalse(ClassUtil.isAssignable((Class<?>) null, String.class));

        // clazz=reference type, fromClass=null
        assertTrue(ClassUtil.isAssignable(String.class, null));

        // clazz=primitive type, fromClass=null
        assertFalse(ClassUtil.isAssignable(int.class, null));

        // clazz.isAssignableFrom(fromClass)
        assertTrue(ClassUtil.isAssignable(Object.class, String.class));
        assertTrue(ClassUtil.isAssignable(String.class, String.class));
        assertFalse(ClassUtil.isAssignable(String.class, Object.class));
        assertFalse(ClassUtil.isAssignable(Integer.class, Integer.TYPE));
        assertFalse(ClassUtil.isAssignable(Long.class, Long.TYPE));
        assertFalse(ClassUtil.isAssignable(Short.class, Short.TYPE));
        assertFalse(ClassUtil.isAssignable(Byte.class, Byte.TYPE));
        assertFalse(ClassUtil.isAssignable(Double.class, Double.TYPE));
        assertFalse(ClassUtil.isAssignable(Float.class, Float.TYPE));
        assertFalse(ClassUtil.isAssignable(Character.class, Character.TYPE));
        assertFalse(ClassUtil.isAssignable(Boolean.class, Boolean.TYPE));
    }

    @Test
    public void isAssignable_widening() {
        // boolean可以接受：boolean
        assertTrue("boolean = boolean", ClassUtil.isAssignable(boolean.class, boolean.class));
        assertTrue("boolean = Boolean", ClassUtil.isAssignable(boolean.class, Boolean.class));
        assertFalse("boolean = char", ClassUtil.isAssignable(boolean.class, char.class));
        assertFalse("boolean = Character", ClassUtil.isAssignable(boolean.class, Character.class));
        assertFalse("boolean = int", ClassUtil.isAssignable(boolean.class, int.class));
        assertFalse("boolean = Integer", ClassUtil.isAssignable(boolean.class, Integer.class));
        assertFalse("boolean = long", ClassUtil.isAssignable(boolean.class, long.class));
        assertFalse("boolean = Long", ClassUtil.isAssignable(boolean.class, Long.class));
        assertFalse("boolean = short", ClassUtil.isAssignable(boolean.class, short.class));
        assertFalse("boolean = Short", ClassUtil.isAssignable(boolean.class, Short.class));
        assertFalse("boolean = byte", ClassUtil.isAssignable(boolean.class, byte.class));
        assertFalse("boolean = Byte", ClassUtil.isAssignable(boolean.class, Byte.class));
        assertFalse("boolean = double", ClassUtil.isAssignable(boolean.class, double.class));
        assertFalse("boolean = Double", ClassUtil.isAssignable(boolean.class, Double.class));
        assertFalse("boolean = float", ClassUtil.isAssignable(boolean.class, float.class));
        assertFalse("boolean = Float", ClassUtil.isAssignable(boolean.class, Float.class));

        // byte可以接受：byte
        assertFalse("byte = boolean", ClassUtil.isAssignable(byte.class, boolean.class));
        assertFalse("byte = Boolean", ClassUtil.isAssignable(byte.class, Boolean.class));
        assertFalse("byte = char", ClassUtil.isAssignable(byte.class, char.class));
        assertFalse("byte = Character", ClassUtil.isAssignable(byte.class, Character.class));
        assertFalse("byte = int", ClassUtil.isAssignable(byte.class, int.class));
        assertFalse("byte = Integer", ClassUtil.isAssignable(byte.class, Integer.class));
        assertFalse("byte = long", ClassUtil.isAssignable(byte.class, long.class));
        assertFalse("byte = Long", ClassUtil.isAssignable(byte.class, Long.class));
        assertFalse("byte = short", ClassUtil.isAssignable(byte.class, short.class));
        assertFalse("byte = Short", ClassUtil.isAssignable(byte.class, Short.class));
        assertTrue("byte = byte", ClassUtil.isAssignable(byte.class, byte.class));
        assertTrue("byte = Byte", ClassUtil.isAssignable(byte.class, Byte.class));
        assertFalse("byte = double", ClassUtil.isAssignable(byte.class, double.class));
        assertFalse("byte = Double", ClassUtil.isAssignable(byte.class, Double.class));
        assertFalse("byte = float", ClassUtil.isAssignable(byte.class, float.class));
        assertFalse("byte = Float", ClassUtil.isAssignable(byte.class, Float.class));

        // char可以接受：char
        assertFalse("char = boolean", ClassUtil.isAssignable(char.class, boolean.class));
        assertFalse("char = Boolean", ClassUtil.isAssignable(char.class, Boolean.class));
        assertTrue("char = char", ClassUtil.isAssignable(char.class, char.class));
        assertTrue("char = Character", ClassUtil.isAssignable(char.class, Character.class));
        assertFalse("char = int", ClassUtil.isAssignable(char.class, int.class));
        assertFalse("char = Integer", ClassUtil.isAssignable(char.class, Integer.class));
        assertFalse("char = long", ClassUtil.isAssignable(char.class, long.class));
        assertFalse("char = Long", ClassUtil.isAssignable(char.class, Long.class));
        assertFalse("char = short", ClassUtil.isAssignable(char.class, short.class));
        assertFalse("char = Short", ClassUtil.isAssignable(char.class, Short.class));
        assertFalse("char = byte", ClassUtil.isAssignable(char.class, byte.class));
        assertFalse("char = Byte", ClassUtil.isAssignable(char.class, Byte.class));
        assertFalse("char = double", ClassUtil.isAssignable(char.class, double.class));
        assertFalse("char = Double", ClassUtil.isAssignable(char.class, Double.class));
        assertFalse("char = float", ClassUtil.isAssignable(char.class, float.class));
        assertFalse("char = Float", ClassUtil.isAssignable(char.class, Float.class));

        // short可以接受：short, byte
        assertFalse("short = boolean", ClassUtil.isAssignable(short.class, boolean.class));
        assertFalse("short = Boolean", ClassUtil.isAssignable(short.class, Boolean.class));
        assertFalse("short = char", ClassUtil.isAssignable(short.class, char.class));
        assertFalse("short = Character", ClassUtil.isAssignable(short.class, Character.class));
        assertFalse("short = int", ClassUtil.isAssignable(short.class, int.class));
        assertFalse("short = Integer", ClassUtil.isAssignable(short.class, Integer.class));
        assertFalse("short = long", ClassUtil.isAssignable(short.class, long.class));
        assertFalse("short = Long", ClassUtil.isAssignable(short.class, Long.class));
        assertTrue("short = short", ClassUtil.isAssignable(short.class, short.class));
        assertTrue("short = Short", ClassUtil.isAssignable(short.class, Short.class));
        assertTrue("short = byte", ClassUtil.isAssignable(short.class, byte.class));
        assertTrue("short = Byte", ClassUtil.isAssignable(short.class, Byte.class));
        assertFalse("short = double", ClassUtil.isAssignable(short.class, double.class));
        assertFalse("short = Double", ClassUtil.isAssignable(short.class, Double.class));
        assertFalse("short = float", ClassUtil.isAssignable(short.class, float.class));
        assertFalse("short = Float", ClassUtil.isAssignable(short.class, Float.class));

        // int可以接受：int、byte、short、char
        assertFalse("int = boolean", ClassUtil.isAssignable(int.class, boolean.class));
        assertFalse("int = Boolean", ClassUtil.isAssignable(int.class, Boolean.class));
        assertTrue("int = char", ClassUtil.isAssignable(int.class, char.class));
        assertTrue("int = Character", ClassUtil.isAssignable(int.class, Character.class));
        assertTrue("int = int", ClassUtil.isAssignable(int.class, int.class));
        assertTrue("int = Integer", ClassUtil.isAssignable(int.class, Integer.class));
        assertFalse("int = long", ClassUtil.isAssignable(int.class, long.class));
        assertFalse("int = Long", ClassUtil.isAssignable(int.class, Long.class));
        assertTrue("int = short", ClassUtil.isAssignable(int.class, short.class));
        assertTrue("int = Short", ClassUtil.isAssignable(int.class, Short.class));
        assertTrue("int = byte", ClassUtil.isAssignable(int.class, byte.class));
        assertTrue("int = Byte", ClassUtil.isAssignable(int.class, Byte.class));
        assertFalse("int = double", ClassUtil.isAssignable(int.class, double.class));
        assertFalse("int = Double", ClassUtil.isAssignable(int.class, Double.class));
        assertFalse("int = float", ClassUtil.isAssignable(int.class, float.class));
        assertFalse("int = Float", ClassUtil.isAssignable(int.class, Float.class));

        // long可以接受：long、int、byte、short、char
        assertFalse("long = boolean", ClassUtil.isAssignable(long.class, boolean.class));
        assertFalse("long = Boolean", ClassUtil.isAssignable(long.class, Boolean.class));
        assertTrue("long = char", ClassUtil.isAssignable(long.class, char.class));
        assertTrue("long = Character", ClassUtil.isAssignable(long.class, Character.class));
        assertTrue("long = int", ClassUtil.isAssignable(long.class, int.class));
        assertTrue("long = Integer", ClassUtil.isAssignable(long.class, Integer.class));
        assertTrue("long = long", ClassUtil.isAssignable(long.class, long.class));
        assertTrue("long = Long", ClassUtil.isAssignable(long.class, Long.class));
        assertTrue("long = short", ClassUtil.isAssignable(long.class, short.class));
        assertTrue("long = Short", ClassUtil.isAssignable(long.class, Short.class));
        assertTrue("long = byte", ClassUtil.isAssignable(long.class, byte.class));
        assertTrue("long = Byte", ClassUtil.isAssignable(long.class, Byte.class));
        assertFalse("long = double", ClassUtil.isAssignable(long.class, double.class));
        assertFalse("long = Double", ClassUtil.isAssignable(long.class, Double.class));
        assertFalse("long = float", ClassUtil.isAssignable(long.class, float.class));
        assertFalse("long = Float", ClassUtil.isAssignable(long.class, Float.class));

        // float可以接受：float, long, int, byte, short, char
        assertFalse("float = boolean", ClassUtil.isAssignable(float.class, boolean.class));
        assertFalse("float = Boolean", ClassUtil.isAssignable(float.class, Boolean.class));
        assertTrue("float = char", ClassUtil.isAssignable(float.class, char.class));
        assertTrue("float = Character", ClassUtil.isAssignable(float.class, Character.class));
        assertTrue("float = int", ClassUtil.isAssignable(float.class, int.class));
        assertTrue("float = Integer", ClassUtil.isAssignable(float.class, Integer.class));
        assertTrue("float = long", ClassUtil.isAssignable(float.class, long.class));
        assertTrue("float = Long", ClassUtil.isAssignable(float.class, Long.class));
        assertTrue("float = short", ClassUtil.isAssignable(float.class, short.class));
        assertTrue("float = Short", ClassUtil.isAssignable(float.class, Short.class));
        assertTrue("float = byte", ClassUtil.isAssignable(float.class, byte.class));
        assertTrue("float = Byte", ClassUtil.isAssignable(float.class, Byte.class));
        assertFalse("float = double", ClassUtil.isAssignable(float.class, double.class));
        assertFalse("float = Double", ClassUtil.isAssignable(float.class, Double.class));
        assertTrue("float = float", ClassUtil.isAssignable(float.class, float.class));
        assertTrue("float = Float", ClassUtil.isAssignable(float.class, Float.class));

        // double可以接受：double, float, long, int, byte, short, char
        assertFalse("double = boolean", ClassUtil.isAssignable(double.class, boolean.class));
        assertFalse("double = Boolean", ClassUtil.isAssignable(double.class, Boolean.class));
        assertTrue("double = char", ClassUtil.isAssignable(double.class, char.class));
        assertTrue("double = Character", ClassUtil.isAssignable(double.class, Character.class));
        assertTrue("double = int", ClassUtil.isAssignable(double.class, int.class));
        assertTrue("double = Integer", ClassUtil.isAssignable(double.class, Integer.class));
        assertTrue("double = long", ClassUtil.isAssignable(double.class, long.class));
        assertTrue("double = Long", ClassUtil.isAssignable(double.class, Long.class));
        assertTrue("double = short", ClassUtil.isAssignable(double.class, short.class));
        assertTrue("double = Short", ClassUtil.isAssignable(double.class, Short.class));
        assertTrue("double = byte", ClassUtil.isAssignable(double.class, byte.class));
        assertTrue("double = Byte", ClassUtil.isAssignable(double.class, Byte.class));
        assertTrue("double = double", ClassUtil.isAssignable(double.class, double.class));
        assertTrue("double = Double", ClassUtil.isAssignable(double.class, Double.class));
        assertTrue("double = float", ClassUtil.isAssignable(double.class, float.class));
        assertTrue("double = Float", ClassUtil.isAssignable(double.class, Float.class));
    }

    @Test
    public void locateClass() {
        assertThat(ClassUtil.locateClass(Logger.class), containsRegex("slf4j-api-\\S+\\.jar$"));
        assertThat(ClassUtil.locateClass(ClassUtilTests.class.getName()),
                   containsAllRegex("^file:", "target/test-classes/$"));
    }

    private static class Inner {
    }
}
