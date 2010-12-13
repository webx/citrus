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

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alibaba.citrus.test.runner.Prototyped;
import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;
import com.alibaba.citrus.test.runner.Prototyped.TestName;

/**
 * 测试{@link FieldInfo}。
 * 
 * @author Michael Zhou
 */
@RunWith(Prototyped.class)
public class FieldTests extends BaseTypeTests implements Cloneable {
    private transient FieldInfo field;
    private Class<?> topType; // 字段所在类的子类
    private Class<?> declaringType; // 字段所在类
    private String fieldName; // 字段名
    private int modifiers; // 限定符
    private String type; // 字段类型
    private String toString; // toString结果
    private String resolved; // resolve的结果
    private boolean resolveChanged; // 如果为false，代表resolve返回this
    private String getFieldFromClassTypeInfo; // ClassTypeInfo.getField的结果

    @Before
    public void init() throws Exception {
        this.topType = topType == null ? declaringType : topType;
        this.field = factory.getField(declaringType.getDeclaredField(fieldName));
        this.resolved = resolved == null ? toString : resolved;
        this.getFieldFromClassTypeInfo = getFieldFromClassTypeInfo == null ? resolved : getFieldFromClassTypeInfo;
    }

    @TestName
    public String testName() throws Exception {
        return declaringType.getDeclaredField(fieldName).toGenericString();
    }

    @SuppressWarnings("unused")
    private static class TestClassBase<T extends Number> {
        public final Number a = 1;
        protected int b;
        Object c;
        private String d;
        private List<T> e;
    }

    @SuppressWarnings("unused")
    private static class TestClass<S extends Number> extends TestClassBase<S> {
        public String a;
        private static int d;
    }

    @Prototypes
    public static TestData<FieldTests> data() {
        TestData<FieldTests> data = TestData.getInstance(FieldTests.class);
        FieldTests prototype;

        // -----------------
        prototype = data.newPrototype();
        prototype.topType = TestClass.class;
        prototype.declaringType = TestClassBase.class;
        prototype.fieldName = "a";
        prototype.modifiers = PUBLIC;
        prototype.type = "Number";
        prototype.toString = "public Number FieldTests$TestClassBase.a";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.topType = TestClass.class;
        prototype.declaringType = TestClassBase.class;
        prototype.fieldName = "b";
        prototype.modifiers = PROTECTED;
        prototype.type = "int";
        prototype.toString = "protected int FieldTests$TestClassBase.b";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.topType = TestClass.class;
        prototype.declaringType = TestClassBase.class;
        prototype.fieldName = "c";
        prototype.modifiers = 0;
        prototype.type = "Object";
        prototype.toString = "Object FieldTests$TestClassBase.c";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.topType = TestClass.class;
        prototype.declaringType = TestClassBase.class;
        prototype.fieldName = "d";
        prototype.modifiers = PRIVATE;
        prototype.type = "String";
        prototype.toString = "private String FieldTests$TestClassBase.d";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.topType = TestClass.class;
        prototype.declaringType = TestClassBase.class;
        prototype.fieldName = "e";
        prototype.modifiers = PRIVATE;
        prototype.type = "List<E=T>";
        prototype.toString = "private List<E=T> FieldTests$TestClassBase.e";
        prototype.resolved = "private List<E=Number> FieldTests$TestClassBase.e";
        prototype.resolveChanged = true;
        prototype.getFieldFromClassTypeInfo = "private List<E=S> FieldTests$TestClassBase.e";

        // -----------------
        prototype = data.newPrototype();
        prototype.topType = TestClassBase.class;
        prototype.declaringType = TestClassBase.class;
        prototype.fieldName = "e";
        prototype.modifiers = PRIVATE;
        prototype.type = "List<E=T>";
        prototype.toString = "private List<E=T> FieldTests$TestClassBase.e";
        prototype.resolved = "private List<E=Number> FieldTests$TestClassBase.e";
        prototype.resolveChanged = true;
        prototype.getFieldFromClassTypeInfo = "private List<E=T> FieldTests$TestClassBase.e";

        // -----------------
        prototype = data.newPrototype();
        prototype.topType = TestClass.class;
        prototype.declaringType = TestClass.class;
        prototype.fieldName = "a";
        prototype.modifiers = PUBLIC;
        prototype.type = "String";
        prototype.toString = "public String FieldTests$TestClass.a";
        prototype.resolved = null;

        // -----------------
        prototype = data.newPrototype();
        prototype.topType = TestClass.class;
        prototype.declaringType = TestClass.class;
        prototype.fieldName = "d";
        prototype.modifiers = PRIVATE | STATIC;
        prototype.type = "int";
        prototype.toString = "private static int FieldTests$TestClass.d";
        prototype.resolved = null;

        return data;
    }

    @Test
    public void getField() {
        assertNotNull(field.getField());
    }

    @Test
    public void getDeclaringType() {
        assertSame(factory.getType(declaringType), field.getDeclaringType());
    }

    @Test
    public void getModifiers() {
        assertEquals(modifiers, field.getModifiers());
    }

    @Test
    public void getType() {
        assertEquals(type, field.getType().toString());
    }

    @Test
    public void getName() {
        assertEquals(fieldName, field.getName());
    }

    @Test
    public void resolve() {
        assertEquals(resolved, field.resolve(null).toString());
        assertEquals(resolved, field.resolve(null, true).toString());
        assertEquals(resolveChanged, field != field.resolve(null));
    }

    @Test
    public void getField_from_ClassTypeInfo() {
        ClassTypeInfo topType = TypeInfo.factory.getClassType(this.topType);
        ClassTypeInfo declaringType = TypeInfo.factory.getClassType(this.declaringType);

        assertEquals(getFieldFromClassTypeInfo, topType.getField(declaringType, fieldName).toString());

        if (topType.equals(declaringType)) {
            assertEquals(getFieldFromClassTypeInfo, topType.getField(fieldName).toString());
        }
    }

    @Test
    public void equalsHashCode() throws Exception {
        FieldInfo newField = factory.getField(declaringType.getDeclaredField(fieldName));

        assertEquals(newField, field);
        assertNotSame(newField, field);
        assertEquals(newField.hashCode(), field.hashCode());

        newField = factory.getField(getClass().getDeclaredField("field"));

        assertThat(field, not(equalTo(newField)));
        assertNotSame(newField, field);
        assertThat(field.hashCode(), not(equalTo(newField.hashCode())));
    }

    @Test
    public void toString_() {
        assertEquals(toString, field.toString());
    }
}
