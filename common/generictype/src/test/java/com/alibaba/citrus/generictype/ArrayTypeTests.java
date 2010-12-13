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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alibaba.citrus.test.runner.Prototyped;
import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;
import com.alibaba.citrus.test.runner.Prototyped.TestName;

/**
 * 测试{@link ArrayTypeInfo}。
 * 
 * @author Michael Zhou
 */
@RunWith(Prototyped.class)
public class ArrayTypeTests extends BaseTypeTests implements Cloneable {
    private transient ArrayTypeInfo typeInfo;
    private Class<?> ownerType; // 变量所在的类
    private String methodName; // 变量的方法名
    private String name; // 名称
    private String simpleName; // 简称
    private Class<?> clazz; // rawType
    private int dimension; // 数组维度
    private TypeInfo componentType; // 数组元素类型
    private TypeInfo directComponentType; // 数组直接元素类型
    private String toString; // toString结果
    private String[] supertypes; // 父类、接口
    private GenericDeclarationInfo context; // resolve context
    private TypeInfo resolvedType; // resolve result;
    private boolean resolveChanged; // 如果为false，代表resolve返回this

    @Before
    public void init() {
        this.typeInfo = (ArrayTypeInfo) factory.getType(getReturnType(ownerType, methodName));
    }

    @TestName
    public String testName() {
        return getReturnType(ownerType, methodName).toString();
    }

    interface TestClass<A extends Number> {
        String[] a();

        String[][] a_1();

        int[] b();

        int[][] b_1();

        List<String>[] c();

        List<String> c_component();

        Map<String, Integer>[][] d();

        Map<String, Integer> d_component();

        Map<String, Integer>[] d_direct_component();

        ArrayList<String>[][][] e();

        ArrayList<String> e_component();

        ArrayList<String>[][] e_direct_component();

        A[] f();

        A[][] f_1();

        List<A>[] g();

        List<A>[][] g_1();

        List<A> g_component();
    }

    @Prototypes
    public static TestData<ArrayTypeTests> data() {
        TestData<ArrayTypeTests> data = TestData.getInstance(ArrayTypeTests.class);
        ArrayTypeTests prototype;

        // =========================
        // rawClass数组
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "a";
        prototype.name = "[Ljava.lang.String;";
        prototype.simpleName = "String[]";
        prototype.clazz = String[].class;
        prototype.dimension = 1;
        prototype.componentType = factory.getType(String.class);
        prototype.directComponentType = factory.getType(String.class);
        prototype.toString = "String[]";
        prototype.supertypes = new String[] { "String[]", "Serializable[]", "Comparable<T=String>[]", "CharSequence[]",
                "Object[]", "Cloneable", "Serializable", "Object" };
        prototype.context = null;
        prototype.resolvedType = factory.getType(String[].class);

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "a_1";
        prototype.name = "[[Ljava.lang.String;";
        prototype.simpleName = "String[][]";
        prototype.clazz = String[][].class;
        prototype.dimension = 2;
        prototype.componentType = factory.getType(String.class);
        prototype.directComponentType = factory.getType(String[].class);
        prototype.toString = "String[][]";
        prototype.supertypes = new String[] { "String[][]", "Serializable[][]", "Comparable<T=String>[][]",
                "CharSequence[][]", "Object[][]", "Cloneable[]", "Serializable[]", "Object[]", "Cloneable",
                "Serializable", "Object" };
        prototype.context = null;
        prototype.resolvedType = factory.getType(String[][].class);

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "b";
        prototype.name = "[I";
        prototype.simpleName = "int[]";
        prototype.clazz = int[].class;
        prototype.dimension = 1;
        prototype.componentType = factory.getType(int.class);
        prototype.directComponentType = factory.getType(int.class);
        prototype.toString = "int[]";
        prototype.supertypes = new String[] { "int[]", "Cloneable", "Serializable", "Object" };
        prototype.context = null;
        prototype.resolvedType = factory.getType(int[].class);

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "b_1";
        prototype.name = "[[I";
        prototype.simpleName = "int[][]";
        prototype.clazz = int[][].class;
        prototype.dimension = 2;
        prototype.componentType = factory.getType(int.class);
        prototype.directComponentType = factory.getType(int[].class);
        prototype.toString = "int[][]";
        prototype.supertypes = new String[] { "int[][]", "Cloneable[]", "Serializable[]", "Object[]", "Cloneable",
                "Serializable", "Object" };
        prototype.context = null;
        prototype.resolvedType = factory.getType(int[][].class);

        // =========================
        // parameterized type数组
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "c";
        prototype.name = "[Ljava.util.List;";
        prototype.simpleName = "List[]";
        prototype.clazz = List[].class;
        prototype.dimension = 1;
        prototype.componentType = factory.getType(getReturnType(TestClass.class, "c_component"));
        prototype.directComponentType = factory.getType(getReturnType(TestClass.class, "c_component"));
        prototype.toString = "List<E=String>[]";
        prototype.supertypes = new String[] { "List<E=String>[]", "Collection<E=E>[]", "Iterable<T=E>[]", "Object[]",
                "Cloneable", "Serializable", "Object" };
        prototype.context = null;
        prototype.resolvedType = factory.getType(getReturnType(TestClass.class, "c"));

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "d";
        prototype.name = "[[Ljava.util.Map;";
        prototype.simpleName = "Map[][]";
        prototype.clazz = Map[][].class;
        prototype.dimension = 2;
        prototype.componentType = factory.getType(getReturnType(TestClass.class, "d_component"));
        prototype.directComponentType = factory.getType(getReturnType(TestClass.class, "d_direct_component"));
        prototype.toString = "Map<K=String, V=Integer>[][]";
        prototype.supertypes = new String[] { "Map<K=String, V=Integer>[][]", "Object[][]", "Cloneable[]",
                "Serializable[]", "Object[]", "Cloneable", "Serializable", "Object" };
        prototype.context = null;
        prototype.resolvedType = factory.getType(getReturnType(TestClass.class, "d"));

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "e";
        prototype.name = "[[[Ljava.util.ArrayList;";
        prototype.simpleName = "ArrayList[][][]";
        prototype.clazz = ArrayList[][][].class;
        prototype.dimension = 3;
        prototype.componentType = factory.getType(getReturnType(TestClass.class, "e_component"));
        prototype.directComponentType = factory.getType(getReturnType(TestClass.class, "e_direct_component"));
        prototype.toString = "ArrayList<E=String>[][][]";
        prototype.supertypes = new String[] { "ArrayList<E=String>[][][]", "AbstractList<E=E>[][][]",
                "AbstractCollection<E=E>[][][]", "List<E=E>[][][]", "RandomAccess[][][]", "Cloneable[][][]",
                "Serializable[][][]", "Collection<E=E>[][][]", "Iterable<T=E>[][][]", "Object[][][]", "Cloneable[][]",
                "Serializable[][]", "Object[][]", "Cloneable[]", "Serializable[]", "Object[]", "Cloneable",
                "Serializable", "Object" };
        prototype.context = null;
        prototype.resolvedType = factory.getType(getReturnType(TestClass.class, "e"));

        // =========================
        // var数组
        // -----------------

        // -----------------
        // context = TestClass
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "f";
        prototype.name = "[Ljava.lang.Number;";
        prototype.simpleName = "Number[]";
        prototype.clazz = Number[].class;
        prototype.dimension = 1;
        prototype.componentType = factory.getType(TestClass.class.getTypeParameters()[0]);
        prototype.directComponentType = factory.getType(TestClass.class.getTypeParameters()[0]);
        prototype.toString = "A[]";
        prototype.supertypes = new String[] { "Number[]", "Serializable[]", "Object[]", "Cloneable", "Serializable",
                "Object" };
        prototype.context = factory.getClassType(TestClass.class);
        prototype.resolvedType = factory.getType(Number[].class);
        prototype.resolveChanged = true;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "f_1";
        prototype.name = "[[Ljava.lang.Number;";
        prototype.simpleName = "Number[][]";
        prototype.clazz = Number[][].class;
        prototype.dimension = 2;
        prototype.componentType = factory.getType(TestClass.class.getTypeParameters()[0]);
        prototype.directComponentType = factory.getType(getReturnType(TestClass.class, "f"));
        prototype.toString = "A[][]";
        prototype.supertypes = new String[] { "Number[][]", "Serializable[][]", "Object[][]", "Cloneable[]",
                "Serializable[]", "Object[]", "Cloneable", "Serializable", "Object" };
        prototype.context = factory.getClassType(TestClass.class);
        prototype.resolvedType = factory.getType(Number[][].class);
        prototype.resolveChanged = true;

        // -----------------
        // context = TestClass<Integer>
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "f";
        prototype.name = "[Ljava.lang.Number;";
        prototype.simpleName = "Number[]";
        prototype.clazz = Number[].class;
        prototype.dimension = 1;
        prototype.componentType = factory.getType(TestClass.class.getTypeParameters()[0]);
        prototype.directComponentType = factory.getType(TestClass.class.getTypeParameters()[0]);
        prototype.toString = "A[]";
        prototype.supertypes = new String[] { "Number[]", "Serializable[]", "Object[]", "Cloneable", "Serializable",
                "Object" };
        prototype.context = factory.getParameterizedType(TestClass.class, Integer.class);
        prototype.resolvedType = factory.getType(Integer[].class);
        prototype.resolveChanged = true;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "f_1";
        prototype.name = "[[Ljava.lang.Number;";
        prototype.simpleName = "Number[][]";
        prototype.clazz = Number[][].class;
        prototype.dimension = 2;
        prototype.componentType = factory.getType(TestClass.class.getTypeParameters()[0]);
        prototype.directComponentType = factory.getType(getReturnType(TestClass.class, "f"));
        prototype.toString = "A[][]";
        prototype.supertypes = new String[] { "Number[][]", "Serializable[][]", "Object[][]", "Cloneable[]",
                "Serializable[]", "Object[]", "Cloneable", "Serializable", "Object" };
        prototype.context = factory.getParameterizedType(TestClass.class, Integer.class);
        prototype.resolvedType = factory.getType(Integer[][].class);
        prototype.resolveChanged = true;

        // -----------------
        // context = TestClass<Integer>
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "g";
        prototype.name = "[Ljava.util.List;";
        prototype.simpleName = "List[]";
        prototype.clazz = List[].class;
        prototype.dimension = 1;
        prototype.componentType = factory.getType(getReturnType(TestClass.class, "g_component"));
        prototype.directComponentType = factory.getType(getReturnType(TestClass.class, "g_component"));
        prototype.toString = "List<E=A>[]";
        prototype.supertypes = new String[] { "List<E=A>[]", "Collection<E=E>[]", "Iterable<T=E>[]", "Object[]",
                "Cloneable", "Serializable", "Object" };
        prototype.context = factory.getParameterizedType(TestClass.class, Integer.class);
        prototype.resolvedType = factory.getArrayType(factory.getParameterizedType(List.class, Integer.class), 1);
        prototype.resolveChanged = true;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "g_1";
        prototype.name = "[[Ljava.util.List;";
        prototype.simpleName = "List[][]";
        prototype.clazz = List[][].class;
        prototype.dimension = 2;
        prototype.componentType = factory.getType(getReturnType(TestClass.class, "g_component"));
        prototype.directComponentType = factory.getType(getReturnType(TestClass.class, "g"));
        prototype.toString = "List<E=A>[][]";
        prototype.supertypes = new String[] { "List<E=A>[][]", "Collection<E=E>[][]", "Iterable<T=E>[][]",
                "Object[][]", "Cloneable[]", "Serializable[]", "Object[]", "Cloneable", "Serializable", "Object" };
        prototype.context = factory.getParameterizedType(TestClass.class, Integer.class);
        prototype.resolvedType = factory.getArrayType(factory.getParameterizedType(List.class, Integer.class), 2);
        prototype.resolveChanged = true;

        return data;
    }

    @Test
    public void getName() {
        assertEquals(name, typeInfo.getName());
    }

    @Test
    public void getSimpleName() {
        assertEquals(simpleName, typeInfo.getSimpleName());
    }

    @Test
    public void getRawType() {
        assertEquals(clazz, typeInfo.getRawType());
    }

    @Test
    public void isArray() {
        assertTrue(typeInfo.isArray());
    }

    @Test
    public void isInterface() {
        assertFalse(typeInfo.isInterface());
    }

    @Test
    public void primitiveWrapper() {
        assertFalse(typeInfo.isPrimitive());
        assertSame(typeInfo, typeInfo.getPrimitiveWrapperType());
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
        assertEquals(resolvedType, typeInfo.resolve(context));
        assertEquals(resolvedType, typeInfo.resolve(context, true));
        assertEquals(resolveChanged, typeInfo != typeInfo.resolve(context));
    }

    @Test
    public void toString_() {
        assertEquals(toString, typeInfo.toString());
    }

    @Test
    public void equalsHashCode() {
        TypeInfo newType = factory.getType(getReturnType(ownerType, methodName));

        if (typeInfo.getComponentType() instanceof RawTypeInfo) {
            assertSame(newType, typeInfo);
        } else {
            assertEquals(newType, typeInfo);
            assertNotSame(newType, typeInfo);
            assertEquals(newType.hashCode(), typeInfo.hashCode());
        }

        newType = factory.getType(Exception[].class);

        assertThat(typeInfo, not(equalTo(newType)));
        assertNotSame(newType, typeInfo);
        assertThat(typeInfo.hashCode(), not(equalTo(newType.hashCode())));
    }
}
