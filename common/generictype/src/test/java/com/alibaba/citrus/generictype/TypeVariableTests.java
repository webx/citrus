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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alibaba.citrus.test.runner.Prototyped;
import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;
import com.alibaba.citrus.test.runner.Prototyped.TestName;

/**
 * 测试{@link TypeVariableInfo}。
 * 
 * @author Michael Zhou
 */
@RunWith(Prototyped.class)
public class TypeVariableTests extends BaseTypeTests implements Cloneable {
    private transient TypeVariableInfo typeInfo;
    private transient GenericDeclarationInfo decl; // generic declaration
    private Class<?> ownerType; // 变量所在的类
    private String methodName; // 变量的方法名
    private String baseType; //baseType.toString()
    private String upperBounds; // upper bounds toString
    private Class<?> rawType; // rawClass
    private String name; // 名称
    private boolean isInterface; // 是否为接口
    private TypeInfo primitiveWrapper; // 原子包装类
    private TypeInfo componentType; // 数组成员类型
    private TypeInfo directComponentType; // 数组直接成员类型
    private String toString; // toString结果
    private String[] supertypes; // 父类、接口
    private GenericDeclarationInfo context; // resolve context
    private TypeInfo resolvedType; // resolve result;
    private TypeInfo resolvedTypeDefault; // 当context为null时，resolve result

    @Before
    public void init() {
        this.typeInfo = (TypeVariableInfo) factory.getType(getReturnType(ownerType, methodName));
        this.decl = factory.getGenericDeclaration(ownerType);
        this.componentType = componentType == null ? typeInfo.getBaseType() : componentType;
        this.directComponentType = directComponentType == null ? typeInfo.getBaseType() : directComponentType;
    }

    @TestName
    public String testName() {
        return "<" + getReturnType(ownerType, methodName) + ">";
    }

    interface TestClass<A, B extends Number & Serializable, C extends List<String>, D extends B, E, Z> {
        A a();

        B b();

        C c();

        D d();

        E e();

        Z zzz();
    }

    interface TestClassDerived<F> extends TestClass<String, Integer, ArrayList<String>, Integer, F, Exception> {
        F f();
    }

    interface ParameterizedTestClass extends TestClassDerived<String> {
        TestClassDerived<? extends Number> g();
    }

    @Prototypes
    public static TestData<TypeVariableTests> data() {
        TestData<TypeVariableTests> data = TestData.getInstance(TypeVariableTests.class);
        TypeVariableTests prototype;

        /*
         * 注： 对于var而言，upper bounds不能为数组，也不支持lower bounds
         */
        GenericDeclarationInfo context = (GenericDeclarationInfo) factory.getType(ParameterizedTestClass.class);

        // =========================
        // 普通var： <A>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "a";
        prototype.baseType = "Object";
        prototype.upperBounds = "[Object]";
        prototype.rawType = Object.class;
        prototype.name = "A";
        prototype.isInterface = false;
        prototype.primitiveWrapper = factory.getType(Object.class);
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "A";
        prototype.supertypes = new String[] { "Object" };
        prototype.context = context;
        prototype.resolvedType = factory.getType(String.class);
        prototype.resolvedTypeDefault = factory.getType(Object.class);

        // =========================
        // 包含多个upper bounds的var： <B extends Number & Serializable>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "b";
        prototype.baseType = "Number";
        prototype.upperBounds = "[Number, Serializable]";
        prototype.rawType = Number.class;
        prototype.name = "B";
        prototype.isInterface = false;
        prototype.primitiveWrapper = factory.getType(Number.class);
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "B";
        prototype.supertypes = new String[] { "Number", "Serializable", "Object" };
        prototype.context = context;
        prototype.resolvedType = factory.getType(Integer.class);
        prototype.resolvedTypeDefault = factory.getType(Number.class);

        // =========================
        // upper bounds为generic类型： <C extends List<String>>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "c";
        prototype.baseType = "List<E=String>";
        prototype.upperBounds = "[List<E=String>]";
        prototype.rawType = List.class;
        prototype.name = "C";
        prototype.isInterface = true;
        prototype.primitiveWrapper = factory.getParameterizedType(List.class, String.class);
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "C";
        prototype.supertypes = new String[] { "List<E=String>", "Collection<E=E>", "Iterable<T=E>", "Object" };
        prototype.context = context;
        prototype.resolvedType = factory.getParameterizedType(ArrayList.class, String.class);
        prototype.resolvedTypeDefault = factory.getParameterizedType(List.class, String.class);

        // =========================
        // upper bounds为var： <D extends B>
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "d";
        prototype.baseType = "B";
        prototype.upperBounds = "[B]";
        prototype.rawType = Number.class;
        prototype.name = "D";
        prototype.isInterface = false;
        prototype.primitiveWrapper = factory.getType(Number.class);
        prototype.componentType = factory.getType(Number.class);
        prototype.directComponentType = factory.getType(Number.class);
        prototype.toString = "D";
        prototype.supertypes = new String[] { "Number", "Serializable", "Object" };
        prototype.context = context;
        prototype.resolvedType = factory.getType(Integer.class);
        prototype.resolvedTypeDefault = factory.getType(Number.class);

        // =========================
        // 间接引用： E = F = String
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "e";
        prototype.baseType = "Object";
        prototype.upperBounds = "[Object]";
        prototype.rawType = Object.class;
        prototype.name = "E";
        prototype.isInterface = false;
        prototype.primitiveWrapper = factory.getType(Object.class);
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "E";
        prototype.supertypes = new String[] { "Object" };
        prototype.context = context;
        prototype.resolvedType = factory.getType(String.class);
        prototype.resolvedTypeDefault = factory.getType(Object.class);

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClassDerived.class;
        prototype.methodName = "f";
        prototype.baseType = "Object";
        prototype.upperBounds = "[Object]";
        prototype.rawType = Object.class;
        prototype.name = "F";
        prototype.isInterface = false;
        prototype.primitiveWrapper = factory.getType(Object.class);
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "F";
        prototype.supertypes = new String[] { "Object" };
        prototype.context = context;
        prototype.resolvedType = factory.getType(String.class);
        prototype.resolvedTypeDefault = factory.getType(Object.class);

        // =========================
        // 引用wildcard： F = ? extends Number
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClassDerived.class;
        prototype.methodName = "f";
        prototype.baseType = "Object";
        prototype.upperBounds = "[Object]";
        prototype.rawType = Object.class;
        prototype.name = "F";
        prototype.isInterface = false;
        prototype.primitiveWrapper = factory.getType(Object.class);
        prototype.componentType = null;
        prototype.directComponentType = null;
        prototype.toString = "F";
        prototype.supertypes = new String[] { "Object" };
        prototype.context = (ClassTypeInfo) factory.getType(getReturnType(ParameterizedTestClass.class, "g"));
        prototype.resolvedType = factory.getType(Number.class);
        prototype.resolvedTypeDefault = factory.getType(Object.class);

        return data;
    }

    /**
     * {@link TypeVariableInfo}功能。
     */
    @Test
    public void bounds() {
        assertEquals(baseType, typeInfo.getBaseType().toString());
        assertEquals(upperBounds, typeInfo.getUpperBounds().toString());
        assertEquals("[]", typeInfo.getLowerBounds().toString());
    }

    /**
     * {@link TypeVariableInfo}功能。
     */
    @Test
    public void getGenericDeclaration() {
        assertEquals(decl, typeInfo.getGenericDeclaration());
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
        assertFalse(typeInfo.isArray());
    }

    @Test
    public void isInterface() {
        assertEquals(isInterface, typeInfo.isInterface());
    }

    @Test
    public void primitiveWrapper() {
        assertFalse(typeInfo.isPrimitive());
        assertEquals(primitiveWrapper, typeInfo.getPrimitiveWrapperType());
    }

    @Test
    public void getDimension() {
        assertEquals(0, typeInfo.getDimension());
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
        assertEquals(resolvedTypeDefault, typeInfo.resolve(null));
        assertEquals(resolvedTypeDefault, typeInfo.resolve(null, true));

        assertNotSame(typeInfo, typeInfo.resolve(context));
    }

    @Test
    public void toString_() {
        assertEquals(toString, typeInfo.toString());
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
}
