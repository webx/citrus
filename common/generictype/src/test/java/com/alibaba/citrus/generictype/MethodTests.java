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
import static java.lang.reflect.Modifier.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alibaba.citrus.test.runner.Prototyped;
import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;
import com.alibaba.citrus.test.runner.Prototyped.TestName;
import com.alibaba.citrus.util.ClassUtil;

/**
 * 测试{@link MethodInfo}。
 * 
 * @author Michael Zhou
 */
@RunWith(Prototyped.class)
public class MethodTests extends BaseTypeTests implements Cloneable {
    private transient MethodInfo method;
    private transient boolean isConstructor; // 是否为构造函数？
    private transient String name; // 方法名，如果是构造函数，则为<init>
    private Class<?> ownerType; // 方法所在类的子类
    private Class<?> declaringType; // 方法所在类
    private String methodName; // 方法名，如果是构造函数，则为null
    private Class<?>[] paramTypes; // 参数类型表
    private boolean isGeneric; // 是否包含类型参数？
    private String typeParams; // 类型参数toString
    private String signature; // 方法签名
    private int modifiers; // 限定符
    private String returnType; // 返回类型
    private String exceptionTypes; // 异常类型
    private String effectiveExceptionTypes; // 有效异常类型
    private String toString; // toString结果
    private String resolved; // resolve的结果
    private boolean resolveChanged; // 如果为false，代表resolve返回this
    private String getMethodFromClassTypeInfo; // ClassTypeInfo.getMethod的结果

    @Before
    public void init() throws Exception {
        this.declaringType = declaringType == null ? ownerType : declaringType;
        this.paramTypes = paramTypes == null ? new Class<?>[0] : paramTypes;
        this.method = (MethodInfo) factory.getGenericDeclaration(getMethodOrConstructor(ownerType, methodName,
                paramTypes));
        this.isConstructor = methodName == null;
        this.name = isConstructor ? "<init>" : methodName;
        this.effectiveExceptionTypes = effectiveExceptionTypes == null ? exceptionTypes : effectiveExceptionTypes;
        this.resolved = resolved == null ? toString : resolved;
        this.getMethodFromClassTypeInfo = getMethodFromClassTypeInfo == null ? resolved : getMethodFromClassTypeInfo;
    }

    @TestName
    public String testName() throws Exception {
        GenericDeclaration decl = getMethodOrConstructor(ownerType, methodName, paramTypes);

        if (decl instanceof Method) {
            return String.format("%s.%s", ClassUtil.getSimpleClassName(ownerType), ((Method) decl).getName());
        } else {
            return String.format("%s[%d params]", ClassUtil.getSimpleClassName(ownerType),
                    ((Constructor<?>) decl).getParameterTypes().length);
        }
    }

    @SuppressWarnings("unused")
    private static class TestClass<X extends Number> {
        // constructors
        public TestClass(int i) {
        }

        protected TestClass(int i, int j) throws FileNotFoundException, IOException, RuntimeException {
        }

        TestClass(int i, int j, int k) throws IOException, FileNotFoundException, RuntimeException {
        }

        private TestClass(int i, int j, int k, int l) throws Exception {
        }

        public <A, B extends Number> TestClass(A i, List<B> j, int k, int l, int m) {
        }

        // methods
        public void a(int i) {
        }

        protected String b(String i, int j) throws FileNotFoundException, IOException, RuntimeException {
            return null;
        }

        List<Integer> c(String i, int j, int k) throws IOException, FileNotFoundException, RuntimeException {
            return null;
        }

        private int d(String i, int j, int k, int l) throws Exception {
            return 0;
        }

        public final static <A, B extends Number> Map<A, ? extends B> e(A a, List<B> b) {
            return null;
        }

        public List<X> f() {
            return null;
        }
    }

    @SuppressWarnings("unused")
    private static class TestClass2<Y extends Number> extends TestClass<Y> {
        public <A, B extends Number> TestClass2(A i, List<B> j, int k, int l, int m) {
            super(i, j, k, l, m);
        }

        public void g(List<Y> list) {
        }
    }

    @Prototypes
    public static TestData<MethodTests> data() {
        TestData<MethodTests> data = TestData.getInstance(MethodTests.class);
        MethodTests prototype;

        // =========================
        // constructor
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = null;
        prototype.paramTypes = new Class<?>[] { int.class };
        prototype.isGeneric = false;
        prototype.typeParams = "[]";
        prototype.signature = "<init>(I)V";
        prototype.modifiers = PUBLIC;
        prototype.returnType = "void";
        prototype.exceptionTypes = "[]";
        prototype.effectiveExceptionTypes = "[]";
        prototype.toString = "public MethodTests$TestClass(int)";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = null;
        prototype.paramTypes = new Class<?>[] { int.class, int.class };
        prototype.isGeneric = false;
        prototype.typeParams = "[]";
        prototype.signature = "<init>(II)V";
        prototype.modifiers = PROTECTED;
        prototype.returnType = "void";
        prototype.exceptionTypes = "[FileNotFoundException, IOException, RuntimeException]";
        prototype.effectiveExceptionTypes = "[IOException]";
        prototype.toString = "protected MethodTests$TestClass(int, int) throws IOException";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = null;
        prototype.paramTypes = new Class<?>[] { int.class, int.class, int.class };
        prototype.isGeneric = false;
        prototype.typeParams = "[]";
        prototype.signature = "<init>(III)V";
        prototype.modifiers = 0;
        prototype.returnType = "void";
        prototype.exceptionTypes = "[IOException, FileNotFoundException, RuntimeException]";
        prototype.effectiveExceptionTypes = "[IOException]";
        prototype.toString = "MethodTests$TestClass(int, int, int) throws IOException";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = null;
        prototype.paramTypes = new Class<?>[] { int.class, int.class, int.class, int.class };
        prototype.isGeneric = false;
        prototype.typeParams = "[]";
        prototype.signature = "<init>(IIII)V";
        prototype.modifiers = PRIVATE;
        prototype.returnType = "void";
        prototype.exceptionTypes = "[Exception]";
        prototype.effectiveExceptionTypes = "[Exception]";
        prototype.toString = "private MethodTests$TestClass(int, int, int, int) throws Exception";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = null;
        prototype.paramTypes = new Class<?>[] { Object.class, List.class, int.class, int.class, int.class };
        prototype.isGeneric = true;
        prototype.typeParams = "[A, B]";
        prototype.signature = "<init>(Ljava/lang/Object;Ljava/util/List;III)V";
        prototype.modifiers = PUBLIC;
        prototype.returnType = "void";
        prototype.exceptionTypes = "[]";
        prototype.effectiveExceptionTypes = "[]";
        prototype.toString = "public <A, B> MethodTests$TestClass(A, List<E=B>, int, int, int)";
        prototype.resolved = "public <A, B> MethodTests$TestClass(Object, List<E=Number>, int, int, int)";
        prototype.resolveChanged = true;
        prototype.getMethodFromClassTypeInfo = "public <A, B> MethodTests$TestClass(A, List<E=B>, int, int, int)";

        // =========================
        // method
        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "a";
        prototype.paramTypes = new Class<?>[] { int.class };
        prototype.isGeneric = false;
        prototype.typeParams = "[]";
        prototype.signature = "a(I)V";
        prototype.modifiers = PUBLIC;
        prototype.returnType = "void";
        prototype.exceptionTypes = "[]";
        prototype.effectiveExceptionTypes = "[]";
        prototype.toString = "public void MethodTests$TestClass.a(int)";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "b";
        prototype.paramTypes = new Class<?>[] { String.class, int.class };
        prototype.isGeneric = false;
        prototype.typeParams = "[]";
        prototype.signature = "b(Ljava/lang/String;I)Ljava/lang/String;";
        prototype.modifiers = PROTECTED;
        prototype.returnType = "String";
        prototype.exceptionTypes = "[FileNotFoundException, IOException, RuntimeException]";
        prototype.effectiveExceptionTypes = "[IOException]";
        prototype.toString = "protected String MethodTests$TestClass.b(String, int) throws IOException";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "c";
        prototype.paramTypes = new Class<?>[] { String.class, int.class, int.class };
        prototype.isGeneric = false;
        prototype.typeParams = "[]";
        prototype.signature = "c(Ljava/lang/String;II)Ljava/util/List;";
        prototype.modifiers = 0;
        prototype.returnType = "List<E=Integer>";
        prototype.exceptionTypes = "[IOException, FileNotFoundException, RuntimeException]";
        prototype.effectiveExceptionTypes = "[IOException]";
        prototype.toString = "List<E=Integer> MethodTests$TestClass.c(String, int, int) throws IOException";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "d";
        prototype.paramTypes = new Class<?>[] { String.class, int.class, int.class, int.class };
        prototype.isGeneric = false;
        prototype.typeParams = "[]";
        prototype.signature = "d(Ljava/lang/String;III)I";
        prototype.modifiers = PRIVATE;
        prototype.returnType = "int";
        prototype.exceptionTypes = "[Exception]";
        prototype.effectiveExceptionTypes = "[Exception]";
        prototype.toString = "private int MethodTests$TestClass.d(String, int, int, int) throws Exception";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass.class;
        prototype.methodName = "e";
        prototype.paramTypes = new Class<?>[] { Object.class, List.class };
        prototype.isGeneric = true;
        prototype.typeParams = "[A, B]";
        prototype.signature = "e(Ljava/lang/Object;Ljava/util/List;)Ljava/util/Map;";
        prototype.modifiers = PUBLIC | STATIC;
        prototype.returnType = "Map<K=A, V=? extends B>";
        prototype.exceptionTypes = "[]";
        prototype.effectiveExceptionTypes = "[]";
        prototype.toString = "public static <A, B> Map<K=A, V=? extends B> MethodTests$TestClass.e(A, List<E=B>)";
        prototype.resolved = "public static <A, B> Map<K=Object, V=Number> MethodTests$TestClass.e(Object, List<E=Number>)";
        prototype.resolveChanged = true;
        prototype.getMethodFromClassTypeInfo = "public static <A, B> Map<K=A, V=B> MethodTests$TestClass.e(A, List<E=B>)";

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass2.class;
        prototype.declaringType = TestClass.class;
        prototype.methodName = "f";
        prototype.paramTypes = new Class<?>[] {};
        prototype.isGeneric = false;
        prototype.typeParams = "[]";
        prototype.signature = "f()Ljava/util/List;";
        prototype.modifiers = PUBLIC;
        prototype.returnType = "List<E=X>";
        prototype.exceptionTypes = "[]";
        prototype.effectiveExceptionTypes = "[]";
        prototype.toString = "public List<E=X> MethodTests$TestClass.f()";
        prototype.resolved = "public List<E=Number> MethodTests$TestClass.f()";
        prototype.resolveChanged = true;
        prototype.getMethodFromClassTypeInfo = "public List<E=Y> MethodTests$TestClass.f()";

        // -----------------
        prototype = data.newPrototype();
        prototype.ownerType = TestClass2.class;
        prototype.declaringType = TestClass2.class;
        prototype.methodName = "g";
        prototype.paramTypes = new Class<?>[] { List.class };
        prototype.isGeneric = false;
        prototype.typeParams = "[]";
        prototype.signature = "g(Ljava/util/List;)V";
        prototype.modifiers = PUBLIC;
        prototype.returnType = "void";
        prototype.exceptionTypes = "[]";
        prototype.effectiveExceptionTypes = "[]";
        prototype.toString = "public void MethodTests$TestClass2.g(List<E=Y>)";
        prototype.resolved = "public void MethodTests$TestClass2.g(List<E=Number>)";
        prototype.resolveChanged = true;
        prototype.getMethodFromClassTypeInfo = "public void MethodTests$TestClass2.g(List<E=Y>)";

        return data;
    }

    @Test
    public void isGeneric() {
        assertEquals(isGeneric, method.isGeneric());
    }

    @Test
    public void getTypeParameters() {
        assertEquals(typeParams, method.getTypeParameters().toString());
    }

    @Test
    public void isConstructor() {
        assertEquals(isConstructor, method.isConstructor());
    }

    @Test
    public void getConstructorOrMethod() {
        if (isConstructor) {
            assertNotNull(method.getConstructor());
            assertNull(method.getMethod());
        } else {
            assertNull(method.getConstructor());
            assertNotNull(method.getMethod());
        }
    }

    @Test
    public void getDeclaringType() {
        assertSame(factory.getType(declaringType), method.getDeclaringType());
    }

    @Test
    public void getSignature() {
        assertEquals(signature, method.getSignature().toString());
    }

    @Test
    public void getModifiers() {
        assertEquals(modifiers, method.getModifiers());
    }

    @Test
    public void getReturnType() {
        assertEquals(returnType, method.getReturnType().toString());
    }

    @Test
    public void getName() {
        assertEquals(name, method.getName());
    }

    @Test
    public void getParamTypes() {
        List<TypeInfo> types = method.getParameterTypes();

        assertEquals(paramTypes.length, types.size());

        int i = 0;
        for (TypeInfo type : types) {
            assertEquals(paramTypes[i++], type.getRawType());
        }
    }

    @Test
    public void getExceptionTypes() {
        assertEquals(exceptionTypes, method.getExceptionTypes().toString());
    }

    @Test
    public void getEffectiveExceptionTypes() {
        assertEquals(effectiveExceptionTypes, method.getEffectiveExceptionTypes().toString());
    }

    @Test
    public void getActualTypeParameters() {
        List<TypeVariableInfo> vars = method.getTypeParameters();
        List<TypeInfo> actualArgs = method.getActualTypeArguments();

        for (int i = 0; i < vars.size(); i++) {
            assertEquals(vars.get(i).getBaseType(), actualArgs.get(i));
            assertEquals(vars.get(i).getBaseType(), vars.get(i).resolve(method));
            assertEquals(vars.get(i).getBaseType(), vars.get(i).resolve(factory.getGenericDeclaration(List.class)));
        }
    }

    @Test
    public void resolve() {
        assertEquals(resolved, method.resolve(null).toString());
        assertEquals(resolved, method.resolve(null, true).toString());
        assertEquals(resolveChanged, method != method.resolve(null));
    }

    @Test
    public void getMethod_from_ClassTypeInfo() {
        ClassTypeInfo ownerType = TypeInfo.factory.getClassType(this.ownerType);

        if (isConstructor) {
            assertEquals(getMethodFromClassTypeInfo, ownerType.getConstructor(paramTypes).toString());
        } else {
            assertEquals(getMethodFromClassTypeInfo, ownerType.getMethod(methodName, paramTypes).toString());
        }
    }

    @Test
    public void equalsHashCode() throws Exception {
        MethodInfo newMethod = (MethodInfo) factory.getGenericDeclaration(getMethodOrConstructor(ownerType, methodName,
                paramTypes));

        assertEquals(newMethod, method);
        assertNotSame(newMethod, method);
        assertEquals(newMethod.hashCode(), method.hashCode());

        newMethod = (MethodInfo) factory.getGenericDeclaration(getMethodOrConstructor(String.class, null,
                new Class<?>[] { String.class }));

        assertThat(method, not(equalTo(newMethod)));
        assertNotSame(newMethod, method);
        assertThat(method.hashCode(), not(equalTo(newMethod.hashCode())));
    }

    @Test
    public void toString_() {
        assertEquals(toString, method.toString());
    }
}
