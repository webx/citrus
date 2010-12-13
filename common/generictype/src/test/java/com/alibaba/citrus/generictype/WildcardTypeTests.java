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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alibaba.citrus.test.runner.Prototyped;
import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;
import com.alibaba.citrus.test.runner.Prototyped.TestName;

/**
 * 测试{@link WildcardTypeInfo}。
 * 
 * @author Michael Zhou
 */
@RunWith(Prototyped.class)
public class WildcardTypeTests extends BaseTypeTests implements Cloneable {
    private transient WildcardTypeInfo typeInfo;
    private Class<?> ownerType; // ?所在的类
    private String methodName; // ?所在的方法名
    private String baseType; // baseType.toString()
    private String upperBounds; // upper bounds toString
    private String lowerBounds; // lower bounds toString
    private Class<?> rawType; // rawClass
    private String name; // 名称
    private boolean array; // 是不是array？
    private int dimension; // 数组维度
    private TypeInfo componentType; // 数组元素类型
    private TypeInfo directComponentType; // 数组直接成员类型
    private String toString; // toString结果
    private String[] supertypes; // 父类、接口
    private String resolved; // resolve结果
    private ClassTypeInfo context; // resolve context

    @Before
    public void init() {
        this.typeInfo = (WildcardTypeInfo) factory.getType(getArgOfReturnType(ownerType, methodName));
        this.componentType = componentType == null ? findNonBoundedType(typeInfo) : componentType;
        this.directComponentType = directComponentType == null ? findNonBoundedType(typeInfo) : directComponentType;
        this.resolved = resolved == null ? findNonBoundedType(typeInfo).toString() : resolved;
    }

    @TestName
    public String testName() {
        return "<" + getArgOfReturnType(ownerType, methodName) + ">";
    }

    interface TestClass<T extends Number> {
        List<?> case1();

        TestClass<?> case1_2();

        List<? extends Number> case2();

        List<? super Number> case3();

        List<? extends List<String>> case4();

        List<? super List<String>> case5();

        List<? extends int[]> case6();

        List<? extends int[][]> case6_1();

        List<? extends List<String>[]> case7();

        List<? extends List<String>[][]> case7_1();

        List<String> case7_component();

        List<String>[] case7_1_component();

        List<? super String[][]> case8();

        List<? super List<String>[][]> case9();

        List<? extends T> case10();

        List<? extends T[][]> case10_1();

        List<? super T> case11();

        List<? extends TestClass<?>> zzz();
    }

    @Prototypes
    public static TestData<WildcardTypeTests> data() {
        TestData<WildcardTypeTests> data = TestData.getInstance(WildcardTypeTests.class);
        WildcardTypeTests prototype;

        // =========================
        // 普通wildcard：<?>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case1";
        prototype.baseType = "Object";
        prototype.upperBounds = "[Object]";
        prototype.lowerBounds = "[]";
        prototype.rawType = Object.class;
        prototype.name = "?";
        prototype.array = false;
        prototype.dimension = 0;
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "?";
        prototype.supertypes = new String[] { "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case1_2";
        prototype.baseType = "Object";
        prototype.upperBounds = "[Object]";
        prototype.lowerBounds = "[]";
        prototype.rawType = Object.class;
        prototype.name = "?";
        prototype.array = false;
        prototype.dimension = 0;
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "?";
        prototype.supertypes = new String[] { "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // =========================
        // rawClass作为upper bounds：<? extends Number>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case2";
        prototype.baseType = "Number";
        prototype.upperBounds = "[Number]";
        prototype.lowerBounds = "[]";
        prototype.rawType = Number.class;
        prototype.name = "?";
        prototype.array = false;
        prototype.dimension = 0;
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "? extends Number";
        prototype.supertypes = new String[] { "Number", "Serializable", "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // =========================
        // rawClass作为lower bounds：<? super Number>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case3";
        prototype.baseType = "Object";
        prototype.upperBounds = "[Object]";
        prototype.lowerBounds = "[Number]";
        prototype.rawType = Object.class;
        prototype.name = "?";
        prototype.array = false;
        prototype.dimension = 0;
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "? super Number";
        prototype.supertypes = new String[] { "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // =========================
        // parameterized type作为upper bounds：<? extends List<String>>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case4";
        prototype.baseType = "List<E=String>";
        prototype.upperBounds = "[List<E=String>]";
        prototype.lowerBounds = "[]";
        prototype.rawType = List.class;
        prototype.name = "?";
        prototype.array = false;
        prototype.dimension = 0;
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "? extends List<E=String>";
        prototype.supertypes = new String[] { "List<E=String>", "Collection<E=E>", "Iterable<T=E>", "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // =========================
        // parameterized type作为lower bounds：<? super List<String>>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case5";
        prototype.baseType = "Object";
        prototype.upperBounds = "[Object]";
        prototype.lowerBounds = "[List<E=String>]";
        prototype.rawType = Object.class;
        prototype.name = "?";
        prototype.array = false;
        prototype.dimension = 0;
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "? super List<E=String>";
        prototype.supertypes = new String[] { "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // =========================
        // 数组作为upper bounds: <? extends int[]>, <? extends int[][]>,
        // <? extends List<String>[]>, <? extends List<String>[][]>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case6";
        prototype.baseType = "int[]";
        prototype.upperBounds = "[int[]]";
        prototype.lowerBounds = "[]";
        prototype.rawType = int[].class;
        prototype.name = "?";
        prototype.array = true;
        prototype.dimension = 1;
        prototype.componentType = factory.getType(int.class);
        prototype.directComponentType = factory.getType(int.class);
        prototype.toString = "? extends int[]";
        prototype.supertypes = new String[] { "int[]", "Cloneable", "Serializable", "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case6_1";
        prototype.baseType = "int[][]";
        prototype.upperBounds = "[int[][]]";
        prototype.lowerBounds = "[]";
        prototype.rawType = int[][].class;
        prototype.name = "?";
        prototype.array = true;
        prototype.dimension = 2;
        prototype.componentType = factory.getType(int.class);
        prototype.directComponentType = factory.getType(int[].class);
        prototype.toString = "? extends int[][]";
        prototype.supertypes = new String[] { "int[][]", "Cloneable[]", "Serializable[]", "Object[]", "Cloneable",
                "Serializable", "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case7";
        prototype.baseType = "List<E=String>[]";
        prototype.upperBounds = "[List<E=String>[]]";
        prototype.lowerBounds = "[]";
        prototype.rawType = List[].class;
        prototype.name = "?";
        prototype.array = true;
        prototype.dimension = 1;
        prototype.componentType = factory.getType(getReturnType(TestClass.class, "case7_component"));
        prototype.directComponentType = factory.getType(getReturnType(TestClass.class, "case7_component"));
        prototype.toString = "? extends List<E=String>[]";
        prototype.supertypes = new String[] { "List<E=String>[]", "Collection<E=E>[]", "Iterable<T=E>[]", "Object[]",
                "Cloneable", "Serializable", "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case7_1";
        prototype.baseType = "List<E=String>[][]";
        prototype.upperBounds = "[List<E=String>[][]]";
        prototype.lowerBounds = "[]";
        prototype.rawType = List[][].class;
        prototype.name = "?";
        prototype.array = true;
        prototype.dimension = 2;
        prototype.componentType = factory.getType(getReturnType(TestClass.class, "case7_component"));
        prototype.directComponentType = factory.getType(getReturnType(TestClass.class, "case7_1_component"));
        prototype.toString = "? extends List<E=String>[][]";
        prototype.supertypes = new String[] { "List<E=String>[][]", "Collection<E=E>[][]", "Iterable<T=E>[][]",
                "Object[][]", "Cloneable[]", "Serializable[]", "Object[]", "Cloneable", "Serializable", "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // =========================
        // 数组作为lower bounds: <? super String[][]>, <? super List<String>[]>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case8";
        prototype.baseType = "Object";
        prototype.upperBounds = "[Object]";
        prototype.lowerBounds = "[String[][]]";
        prototype.rawType = Object.class;
        prototype.name = "?";
        prototype.array = false;
        prototype.dimension = 0;
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "? super String[][]";
        prototype.supertypes = new String[] { "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case9";
        prototype.baseType = "Object";
        prototype.upperBounds = "[Object]";
        prototype.lowerBounds = "[List<E=String>[][]]";
        prototype.rawType = Object.class;
        prototype.name = "?";
        prototype.array = false;
        prototype.dimension = 0;
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "? super List<E=String>[][]";
        prototype.supertypes = new String[] { "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // =========================
        // Type var作为upper bounds: <? extends T>, <? extends T[][]>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case10";
        prototype.baseType = "T";
        prototype.upperBounds = "[T]";
        prototype.lowerBounds = "[]";
        prototype.rawType = Number.class;
        prototype.name = "?";
        prototype.array = false;
        prototype.dimension = 0;
        prototype.componentType = factory.getType(Number.class);
        prototype.directComponentType = factory.getType(Number.class);
        prototype.toString = "? extends T";
        prototype.supertypes = new String[] { "Number", "Serializable", "Object" };
        prototype.resolved = null;
        prototype.context = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case10";
        prototype.baseType = "T";
        prototype.upperBounds = "[T]";
        prototype.lowerBounds = "[]";
        prototype.rawType = Number.class;
        prototype.name = "?";
        prototype.array = false;
        prototype.dimension = 0;
        prototype.componentType = factory.getType(Number.class);
        prototype.directComponentType = factory.getType(Number.class);
        prototype.toString = "? extends T";
        prototype.supertypes = new String[] { "Number", "Serializable", "Object" };
        prototype.resolved = "Integer";
        prototype.context = factory.getParameterizedType(TestClass.class, Integer.class);

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case10_1";
        prototype.baseType = "T[][]";
        prototype.upperBounds = "[T[][]]";
        prototype.lowerBounds = "[]";
        prototype.rawType = Number[][].class;
        prototype.name = "?";
        prototype.array = true;
        prototype.dimension = 2;
        prototype.componentType = factory.getType(prototype.ownerType.getTypeParameters()[0]);
        prototype.directComponentType = factory.getArrayType(
                factory.getType(prototype.ownerType.getTypeParameters()[0]), 1);
        prototype.toString = "? extends T[][]";
        prototype.supertypes = new String[] { "Number[][]", "Serializable[][]", "Object[][]", "Cloneable[]",
                "Serializable[]", "Object[]", "Cloneable", "Serializable", "Object" };
        prototype.resolved = "Integer[][]";
        prototype.context = factory.getParameterizedType(TestClass.class, Integer.class);

        // =========================
        // Type var作为lower bounds:
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "case11";
        prototype.baseType = "Object";
        prototype.upperBounds = "[Object]";
        prototype.lowerBounds = "[T]";
        prototype.rawType = Object.class;
        prototype.name = "?";
        prototype.array = false;
        prototype.dimension = 0;
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "? super T";
        prototype.supertypes = new String[] { "Object" };
        prototype.resolved = null;
        prototype.context = null;

        return data;
    }

    /**
     * {@link TypeVariableInfo}功能。
     */
    @Test
    public void bounds() {
        assertEquals(baseType, typeInfo.getBaseType().toString());
        assertEquals(upperBounds, typeInfo.getUpperBounds().toString());
        assertEquals(lowerBounds, typeInfo.getLowerBounds().toString());
    }

    @Test
    public void getName() {
        assertEquals(name, typeInfo.getName());
    }

    @Test
    public void getSimpleName() {
        assertEquals(name, typeInfo.getSimpleName());
    }

    @Test
    public void getRawType() {
        assertEquals(rawType, typeInfo.getRawType());
    }

    @Test
    public void isArray() {
        assertEquals(array, typeInfo.isArray());
    }

    @Test
    public void isInterface() {
        assertEquals(typeInfo.getBaseType().isInterface(), typeInfo.isInterface());
    }

    @Test
    public void primitiveWrapper() {
        assertFalse(typeInfo.isPrimitive());
        assertSame(findNonBoundedType(typeInfo), typeInfo.getPrimitiveWrapperType());
    }

    @Test
    public void getDimension() {
        assertEquals(dimension, typeInfo.getDimension());
    }

    @Test
    public void getComponentType() {
        assertEquals(componentType, typeInfo.getComponentType());
    }

    @Test
    public void getDirectComponentType() {
        assertEquals(directComponentType, typeInfo.getDirectComponentType());
    }

    @Test
    public void getSupertypes() {
        assertSupertypes(typeInfo, supertypes);
    }

    @Test
    public void resolve() {
        assertEquals(typeInfo.resolve(context), typeInfo.resolve(context, true));
        assertNotSame(typeInfo, typeInfo.resolve(context));
        assertEquals(resolved, typeInfo.resolve(context).toString());
    }

    @Test
    public void toString_() {
        assertEquals(toString, typeInfo.toString());
    }

    @Test
    public void equalsHashCode() {
        TypeInfo newType = factory.getType(getArgOfReturnType(ownerType, methodName));

        assertEquals(newType, typeInfo);
        assertNotSame(newType, typeInfo);
        assertEquals(newType.hashCode(), typeInfo.hashCode());

        newType = factory.getType(getArgOfReturnType(TestClass.class, "zzz"));

        assertThat(typeInfo, not(equalTo(newType)));
        assertNotSame(newType, typeInfo);
        assertThat(typeInfo.hashCode(), not(equalTo(newType.hashCode())));
    }
}
