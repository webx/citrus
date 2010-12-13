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
package com.alibaba.citrus.generictype;

import static com.alibaba.citrus.generictype.TypeInfo.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.Test;

/**
 * 综合测试。
 * 
 * @author Michael Zhou
 */
public class TypeInfoFactoryTests {
    @Test(expected = IllegalArgumentException.class)
    public void getClass_array() {
        factory.getClassType(int[].class);
    }

    @Test
    public void getClass_rawClass() {
        assertSame(factory.getType(int.class), factory.getClassType(int.class));
        assertSame(factory.getType(String.class), factory.getClassType(String.class));
        assertSame(factory.getType(List.class), factory.getClassType(List.class));

        assertSame(factory.getGenericDeclaration(int.class), factory.getClassType(int.class));
        assertSame(factory.getGenericDeclaration(String.class), factory.getClassType(String.class));
        assertSame(factory.getGenericDeclaration(List.class), factory.getClassType(List.class));
    }

    @Test
    public void getClass_parameterizedClass() {
        class StringList extends ArrayList<String> {
            private static final long serialVersionUID = -6675085611077625191L;
        }

        ParameterizedType type = (ParameterizedType) StringList.class.getGenericSuperclass();

        assertEquals(factory.getType(type), factory.getClassType(type));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getGenericDeclaration_arrayClass() {
        factory.getGenericDeclaration(Integer[][].class);
    }

    @Test
    public void getMethod() {
        for (Method method : TypeInfoFactoryTests.class.getMethods()) {
            assertEquals(factory.getGenericDeclaration(method), factory.getMethod(method));
        }
    }

    @Test
    public void getConstructorMethod() {
        for (Constructor<?> constructor : ArrayList.class.getConstructors()) {
            assertEquals(factory.getGenericDeclaration(constructor), factory.getConstructor(constructor));
        }
    }

    @Test
    public void getParameterizedType() {
        ParameterizedTypeInfo type1 = factory.getParameterizedType(List.class, String.class);
        ParameterizedTypeInfo type2 = factory.getParameterizedType(factory.getClassType(List.class), String.class);
        ParameterizedTypeInfo type3 = factory.getParameterizedType(factory.getClassType(List.class),
                factory.getClassType(String.class));

        assertEquals(type1, type2);
        assertEquals(type2, type3);
    }

    @Test
    public void getArrayType() {
        ArrayTypeInfo ati1 = factory.getArrayType(String.class, 2);
        ArrayTypeInfo ati2 = factory.getArrayType(factory.getType(String.class), 2);

        assertSame(ati1, ati2);

    }

    /**
     * 递归创建types的情形。
     */
    @Test
    public void recursiveType() {
        RawTypeInfo layout = (RawTypeInfo) factory.getType(Layout.class);
        ParameterizedTypeInfo pt_layout = (ParameterizedTypeInfo) layout.getTypeParameters().get(0).getBaseType();

        assertSame(layout.getRawType(), pt_layout.getRawType());

        RawTypeInfo layoutData = (RawTypeInfo) factory.getType(LayoutData.class);
        ParameterizedTypeInfo pt_layout2 = (ParameterizedTypeInfo) layoutData.getTypeParameters().get(0).getBaseType();

        assertSame(pt_layout.getRawType(), pt_layout2.getRawType());

        assertEquals("Layout<L>", layout.toString());
        assertEquals("Layout<L=L>", pt_layout.toString());
        assertEquals("LayoutData<L>", layoutData.toString());
        assertEquals("Layout<L=L>", pt_layout2.toString());
    }

    @Test
    public void recursiveMethod() throws Exception {
        MethodInfo method = (MethodInfo) factory.getGenericDeclaration(TypeInfoFactoryTests.class.getDeclaredMethod(
                "testMethod", List.class));

        // Type Vars
        TypeVariableInfo varA = method.getTypeParameters().get(0);
        TypeVariableInfo varB = method.getTypeParameters().get(1);

        // 返回值类型
        TypeVariableInfo returnType = (TypeVariableInfo) method.getReturnType();

        assertEquals(varB, returnType);
        assertSame(method, returnType.getGenericDeclaration());

        // 参数类型
        ParameterizedTypeInfo paramType = (ParameterizedTypeInfo) method.getParameterTypes().get(0);
        TypeVariableInfo paramArgType = (TypeVariableInfo) paramType.getActualTypeArguments().get(0);

        assertEquals(varA, paramArgType);
        assertSame(method, paramArgType.getGenericDeclaration());
    }

    @SuppressWarnings("unused")
    private <A, B extends Number> B testMethod(List<A> list) {
        return null;
    }

    @Test
    public void stressTest() throws Exception {
        File jarFile = new File(getJavaHome(), "jre/lib/rt.jar");

        if (!jarFile.exists()) {
            jarFile = new File(getJavaHome(), "../Classes/classes.jar"); // mac style

            if (!jarFile.exists()) {
                throw new IllegalArgumentException("could not find jar file: jre/lib/rt.jar or classes.jar");
            }
        }

        jarFile = jarFile.getCanonicalFile();
        List<Class<?>> classes = getClasses(jarFile);
        long start = System.currentTimeMillis();

        for (Class<?> c : classes) {
            TypeInfo type = factory.getType(c);

            System.out.println(type);
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("===========================");
        System.out.printf("Total %,d classes in %,d ms, avg. %,.3f ms.\n", classes.size(), duration, (double) duration
                / classes.size());
    }

    private List<Class<?>> getClasses(File jarFile) throws Exception {
        JarFile jar = new JarFile(jarFile);
        List<Class<?>> classes = new LinkedList<Class<?>>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (Enumeration<JarEntry> i = jar.entries(); i.hasMoreElements();) {
            JarEntry entry = i.nextElement();
            String name = entry.getName();

            if (name.endsWith(".class")) {
                name = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                classes.add(Class.forName(name, false, loader));
            }
        }

        jar.close();

        return classes;
    }

    @Test
    public void resolve_includeBaseType() throws Exception {
        ClassTypeInfo myClassType = factory.getClassType(MyClass.class);
        ClassTypeInfo listAType = (ClassTypeInfo) factory.getType(MyClass.class.getField("listA").getGenericType());

        // includeBaseType==false, Iterable<T=E> => Iterable<T=A>
        TypeInfo expectedType = factory.getParameterizedType(Iterable.class, MyClass.class.getTypeParameters()[0]);
        ClassTypeInfo iterableType = (ClassTypeInfo) factory.getType(List.class).getSupertype(Iterable.class);
        TypeInfo resolvedIterableType = iterableType.resolve(listAType, false);

        assertEquals(expectedType, resolvedIterableType);

        // resolvedIterableType应当还能再次resolve：Iterable<T=A> => Iterable<T=Integer>
        expectedType = factory.getParameterizedType(Iterable.class, Integer.class);
        ParameterizedTypeInfo context = factory.getParameterizedType(MyClass.class, Integer.class);

        assertEquals(expectedType, resolvedIterableType.resolve(context));
        assertEquals(expectedType, resolvedIterableType.resolve(context, true));
        assertEquals(expectedType, resolvedIterableType.resolve(context, false));

        // includeBaseType==true, Iterable<T=E> => Iterable<T=Number>
        expectedType = factory.getParameterizedType(Iterable.class, Number.class);

        assertEquals(expectedType, iterableType.resolve(listAType));
        assertEquals(expectedType, iterableType.resolve(listAType, true));

        // includeBaseType==false, T => A
        TypeInfo varA = myClassType.getTypeParameters().get(0);
        TypeInfo varT = iterableType.getTypeParameters().get(0);

        assertEquals(varA, varT.resolve(listAType, false));

        // includeBaseType==true, T => Number
        assertEquals(factory.getType(Number.class), varT.resolve(listAType));
        assertEquals(factory.getType(Number.class), varT.resolve(listAType, true));
    }

    @Test
    public void resolve_includeBaseType_2() throws Exception {
        ClassTypeInfo listAType = (ClassTypeInfo) factory.getType(MyClass2.class.getField("listA").getGenericType());

        // includeBaseType==false, Iterable<T=Collection.E> => Iterable<T=List.E>
        TypeInfo expectedType = factory.getParameterizedType(Iterable.class, List.class.getTypeParameters()[0]);
        ClassTypeInfo iterableType = (ClassTypeInfo) factory.getType(List.class).getSupertype(Iterable.class);
        TypeInfo resolvedIterableType = iterableType.resolve(listAType, false);

        assertEquals(expectedType, resolvedIterableType);

        // resolvedIterableType应当还能再次resolve：Iterable<T=List.E> => Iterable<T=Integer>
        expectedType = factory.getParameterizedType(Iterable.class, Integer.class);
        ParameterizedTypeInfo context = factory.getParameterizedType(List.class, Integer.class);

        assertEquals(expectedType, resolvedIterableType.resolve(context));
        assertEquals(expectedType, resolvedIterableType.resolve(context, true));
        assertEquals(expectedType, resolvedIterableType.resolve(context, false));

        // includeBaseType==true, Iterable<T=E> => Iterable<T=Object>
        expectedType = factory.getParameterizedType(Iterable.class, Object.class);

        assertEquals(expectedType, iterableType.resolve(listAType));
        assertEquals(expectedType, iterableType.resolve(listAType, true));

        // includeBaseType==false, T => List.E
        expectedType = factory.getType(List.class.getTypeParameters()[0]);
        TypeInfo varT = iterableType.getTypeParameters().get(0);

        assertEquals(expectedType, varT.resolve(listAType, false));

        // includeBaseType==true, T => Object
        assertEquals(TypeInfo.OBJECT, varT.resolve(listAType));
        assertEquals(TypeInfo.OBJECT, varT.resolve(listAType, true));
    }
}

class MyClass<A extends Number> {
    public List<A> listA;
}

class MyClass2<A extends Number> {
    @SuppressWarnings("rawtypes")
    public List listA;
}

class Layout<L extends Layout<L>> {
    public LayoutData<L> data;
}

class LayoutData<L extends Layout<L>> {
}
