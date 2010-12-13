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
 * 测试{@link ParameterizedTypeInfo}。
 * 
 * @author Michael Zhou
 */
@RunWith(Prototyped.class)
public class ParameterizedTypeTests extends BaseTypeTests implements Cloneable {
    private transient ParameterizedTypeInfo typeInfo;
    private Class<?> ownerType; // 变量所在的类
    private String methodName; // 变量的方法名
    private Class<?> clazz; // rawClass
    private String name; // 名称
    private String simpleName; // 简称
    private boolean isInterface; // 是否为接口
    private String toString; // toString结果
    private String[] supertypes; // 父类、接口
    private GenericDeclarationInfo context; // resolve参数
    private String params; // 参数表toString
    private String[] args; // actual args
    private String[] resolvedArgs; // resolve结果
    private boolean resolveChanged; // 如果为false，表示resolve方法返回this

    @Before
    public void init() {
        this.typeInfo = (ParameterizedTypeInfo) factory.getType(getReturnType(ownerType, methodName));
    }

    @TestName
    public String testName() {
        return getReturnType(ownerType, methodName).toString();
    }

    interface NumberType<N extends Number> {
    }

    interface Enum<E extends Enum<?>> {
    }

    interface TestClass<A> {
        List<String> a();

        ArrayList<A> b();

        List<? extends A> c();

        List<? extends String> d();

        List<? extends Class<?>> e();

        NumberType<?> e_1(); // 将被修正成? extends Number

        NumberType<? extends Integer> e_2();

        Enum<? extends Enum<?>> e_3(); // 将被修正成? extends Enum<?>

        List<Map<String, Integer>> f();

        List<Map<A, List<? extends A>>> ff();

        List<Map<String, Integer>[][]> g();

        List<String[]> h();

        List<A[][]> i();

        List<Exception> zzz();
    }

    abstract class StringClass implements TestClass<String> {
    }

    @Prototypes
    public static TestData<ParameterizedTypeTests> data() {
        TestData<ParameterizedTypeTests> data = TestData.getInstance(ParameterizedTypeTests.class);
        ParameterizedTypeTests prototype;

        // =========================
        // 以rawclass作为actual args
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "a";
        prototype.clazz = List.class;
        prototype.name = "java.util.List";
        prototype.simpleName = "List";
        prototype.isInterface = true;
        prototype.toString = "List<E=String>";
        prototype.supertypes = new String[] { "List<E=String>", "Collection<E=E>", "Iterable<T=E>", "Object" };
        prototype.context = null;
        prototype.params = "[E]";
        prototype.args = new String[] { "String" };
        prototype.resolvedArgs = new String[] { "String" };

        // =========================
        // 以type var作为actual args
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "b";
        prototype.clazz = ArrayList.class;
        prototype.name = "java.util.ArrayList";
        prototype.simpleName = "ArrayList";
        prototype.isInterface = false;
        prototype.toString = "ArrayList<E=A>";
        prototype.supertypes = new String[] { "ArrayList<E=A>", "AbstractList<E=E>", "AbstractCollection<E=E>",
                "Cloneable", "Collection<E=E>", "Iterable<T=E>", "List<E=E>", "RandomAccess", "Serializable", "Object" };
        prototype.context = null;
        prototype.params = "[E]";
        prototype.args = new String[] { "A" };
        prototype.resolvedArgs = new String[] { "Object" };
        prototype.resolveChanged = true;

        // =========================
        // 以wildcard作为actual args
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "c";
        prototype.clazz = List.class;
        prototype.name = "java.util.List";
        prototype.simpleName = "List";
        prototype.isInterface = true;
        prototype.toString = "List<E=? extends A>";
        prototype.supertypes = new String[] { "List<E=? extends A>", "Collection<E=E>", "Iterable<T=E>", "Object" };
        prototype.context = null;
        prototype.params = "[E]";
        prototype.args = new String[] { "? extends A" };
        prototype.resolvedArgs = new String[] { "Object" };
        prototype.resolveChanged = true;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "d";
        prototype.clazz = List.class;
        prototype.name = "java.util.List";
        prototype.simpleName = "List";
        prototype.isInterface = true;
        prototype.toString = "List<E=? extends String>";
        prototype.supertypes = new String[] { "List<E=? extends String>", "Collection<E=E>", "Iterable<T=E>", "Object" };
        prototype.context = null;
        prototype.params = "[E]";
        prototype.args = new String[] { "? extends String" };
        prototype.resolvedArgs = new String[] { "String" };
        prototype.resolveChanged = true;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "e";
        prototype.clazz = List.class;
        prototype.name = "java.util.List";
        prototype.simpleName = "List";
        prototype.isInterface = true;
        prototype.toString = "List<E=? extends Class<T=?>>";
        prototype.supertypes = new String[] { "List<E=? extends Class<T=?>>", "Collection<E=E>", "Iterable<T=E>",
                "Object" };
        prototype.context = null;
        prototype.params = "[E]";
        prototype.args = new String[] { "? extends Class<T=?>" };
        prototype.resolvedArgs = new String[] { "Class<T=Object>" };
        prototype.resolveChanged = true;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "e_1";
        prototype.clazz = NumberType.class;
        prototype.name = NumberType.class.getName();
        prototype.simpleName = "ParameterizedTypeTests$NumberType";
        prototype.isInterface = true;
        prototype.toString = "ParameterizedTypeTests$NumberType<N=? extends Number>";
        prototype.supertypes = new String[] { "ParameterizedTypeTests$NumberType<N=? extends Number>", "Object" };
        prototype.context = null;
        prototype.params = "[N]";
        prototype.args = new String[] { "? extends Number" };
        prototype.resolvedArgs = new String[] { "Number" };
        prototype.resolveChanged = true;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "e_2";
        prototype.clazz = NumberType.class;
        prototype.name = NumberType.class.getName();
        prototype.simpleName = "ParameterizedTypeTests$NumberType";
        prototype.isInterface = true;
        prototype.toString = "ParameterizedTypeTests$NumberType<N=? extends Integer>";
        prototype.supertypes = new String[] { "ParameterizedTypeTests$NumberType<N=? extends Integer>", "Object" };
        prototype.context = null;
        prototype.params = "[N]";
        prototype.args = new String[] { "? extends Integer" };
        prototype.resolvedArgs = new String[] { "Integer" };
        prototype.resolveChanged = true;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "e_3";
        prototype.clazz = Enum.class;
        prototype.name = Enum.class.getName();
        prototype.simpleName = "ParameterizedTypeTests$Enum";
        prototype.isInterface = true;
        prototype.toString = "ParameterizedTypeTests$Enum<E=? extends ParameterizedTypeTests$Enum<?>";
        prototype.supertypes = new String[] { "ParameterizedTypeTests$NumberType<N=? extends Number>", "Object" };
        prototype.context = null;
        prototype.params = "[N]";
        prototype.args = new String[] { "? extends Number" };
        prototype.resolvedArgs = new String[] { "Number" };
        prototype.resolveChanged = true;

        // =========================
        // 以parameterized type作为actual args
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "f";
        prototype.clazz = List.class;
        prototype.name = "java.util.List";
        prototype.simpleName = "List";
        prototype.isInterface = true;
        prototype.toString = "List<E=Map<K=String, V=Integer>>";
        prototype.supertypes = new String[] { "List<E=Map<K=String, V=Integer>>", "Collection<E=E>", "Iterable<T=E>",
                "Object" };
        prototype.context = null;
        prototype.params = "[E]";
        prototype.args = new String[] { "Map<K=String, V=Integer>" };
        prototype.resolvedArgs = new String[] { "Map<K=String, V=Integer>" };

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "ff";
        prototype.clazz = List.class;
        prototype.name = "java.util.List";
        prototype.simpleName = "List";
        prototype.isInterface = true;
        prototype.toString = "List<E=Map<K=A, V=List<E=? extends A>>>";
        prototype.supertypes = new String[] { "List<E=Map<K=A, V=List<E=? extends A>>>", "Collection<E=E>",
                "Iterable<T=E>", "Object" };
        prototype.context = factory.getClassType(StringClass.class);
        prototype.params = "[E]";
        prototype.args = new String[] { "Map<K=A, V=List<E=? extends A>>" };
        prototype.resolvedArgs = new String[] { "Map<K=String, V=List<E=String>>" };
        prototype.resolveChanged = true;

        // =========================
        // 以array type作为actual args
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "g";
        prototype.clazz = List.class;
        prototype.name = "java.util.List";
        prototype.simpleName = "List";
        prototype.isInterface = true;
        prototype.toString = "List<E=Map<K=String, V=Integer>[][]>";
        prototype.supertypes = new String[] { "List<E=Map<K=String, V=Integer>[][]>", "Collection<E=E>",
                "Iterable<T=E>", "Object" };
        prototype.context = null;
        prototype.params = "[E]";
        prototype.args = new String[] { "Map<K=String, V=Integer>[][]" };
        prototype.resolvedArgs = new String[] { "Map<K=String, V=Integer>[][]" };

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "h";
        prototype.clazz = List.class;
        prototype.name = "java.util.List";
        prototype.simpleName = "List";
        prototype.isInterface = true;
        prototype.toString = "List<E=String[]>";
        prototype.supertypes = new String[] { "List<E=String[]>", "Collection<E=E>", "Iterable<T=E>", "Object" };
        prototype.context = null;
        prototype.params = "[E]";
        prototype.args = new String[] { "String[]" };
        prototype.resolvedArgs = new String[] { "String[]" };

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "i";
        prototype.clazz = List.class;
        prototype.name = "java.util.List";
        prototype.simpleName = "List";
        prototype.isInterface = true;
        prototype.toString = "List<E=A[][]>";
        prototype.supertypes = new String[] { "List<E=A[][]>", "Collection<E=E>", "Iterable<T=E>", "Object" };
        prototype.context = null;
        prototype.params = "[E]";
        prototype.args = new String[] { "A[][]" };
        prototype.resolvedArgs = new String[] { "Object[][]" };
        prototype.resolveChanged = true;

        return data;
    }

    /**
     * from {@link GenericDeclarationInfo}。
     */
    @Test
    public void isGeneric() {
        assertTrue(typeInfo.isGeneric());
    }

    /**
     * from {@link GenericDeclarationInfo}。
     */
    @Test
    public void getTypeParameters() {
        assertEquals(params, typeInfo.getTypeParameters().toString());
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
        assertFalse(typeInfo.isArray());
    }

    @Test
    public void isInterface() {
        assertEquals(isInterface, typeInfo.isInterface());
    }

    @Test
    public void primitiveWrapper() {
        assertFalse(typeInfo.isPrimitive());
        assertSame(typeInfo, typeInfo.getPrimitiveWrapperType());
    }

    @Test
    public void getDimension() {
        assertEquals(0, typeInfo.getDimension());
    }

    @Test
    public void getComponentType() {
        assertSame(typeInfo, typeInfo.getComponentType());
    }

    @Test
    public void getDirectComponentType() {
        assertSame(typeInfo, typeInfo.getDirectComponentType());
    }

    @Test
    public void toString_() {
        assertEquals(toString, typeInfo.toString());
    }

    @Test
    public void getActualTypeArguments() {
        List<TypeVariableInfo> vars = typeInfo.getTypeParameters();

        assertEquals(vars.size(), args.length);
        assertEquals(args.length, typeInfo.getActualTypeArguments().size());

        for (int i = 0; i < args.length; i++) {
            assertEquals(args[i], typeInfo.getActualTypeArguments().get(i).toString());
            assertEquals(args[i], typeInfo.getActualTypeArgument(vars.get(i).getName()).toString());
        }
    }

    @Test
    public void getSupertypes() {
        assertSupertypes(typeInfo, supertypes);
    }

    @Test
    public void resolve() {
        ClassTypeInfo resolvedType = typeInfo.resolve(context);

        assertEquals(resolvedType, typeInfo.resolve(context, true));
        assertEquals(resolveChanged, resolvedType != typeInfo);

        List<TypeVariableInfo> vars = resolvedType.getTypeParameters();
        List<TypeInfo> actualArgs = resolvedType.getActualTypeArguments();

        assertEquals(vars.size(), actualArgs.size());
        assertEquals(vars.size(), resolvedArgs.length);

        for (int i = 0; i < vars.size(); i++) {
            assertEquals(resolvedArgs[i], actualArgs.get(i).toString());
            assertEquals(actualArgs.get(i), resolvedType.getActualTypeArgument(vars.get(i).getName()));
        }
    }

    @Test
    public void equalsHashCode() {
        TypeInfo newType = factory.getType(getReturnType(ownerType, methodName));

        assertEquals(newType, typeInfo);
        assertNotSame(newType, typeInfo);
        assertEquals(newType.hashCode(), typeInfo.hashCode());

        newType = factory.getType(getReturnType(TestClass.class, "zzz"));

        assertThat(typeInfo, not(equalTo(newType)));
        assertNotSame(newType, typeInfo);
        assertThat(typeInfo.hashCode(), not(equalTo(newType.hashCode())));
    }

    @Test
    public void manuallyCreation() {
        ParameterizedTypeInfo pt = factory
                .getParameterizedType(factory.getType(Map.class), String.class, Integer.class);

        assertEquals("Map<K=String, V=Integer>", pt.toString());
        assertEquals(Map.class, pt.getRawType());
    }
}
