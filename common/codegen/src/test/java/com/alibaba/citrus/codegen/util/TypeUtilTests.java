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
package com.alibaba.citrus.codegen.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.Test;

import com.alibaba.citrus.asm.Type;

/**
 * 测试<code>TypeUtil</code>类。
 * 
 * @author Michael Zhou
 */
public class TypeUtilTests {
    @Test
    public void internalNames() {
        assertEquals("java/lang/String", TypeUtil.getInternalNameFromClassName(String.class.getName()));

        try {
            TypeUtil.getInternalNameFromClassName(int.class.getName());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("primitive"));
        }

        try {
            TypeUtil.getInternalNameFromClassName(int[].class.getName());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("array"));
        }

        try {
            TypeUtil.getInternalNameFromClassName(String[].class.getName());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("array"));
        }
    }

    @Test
    public void descriptors() {
        testDescriptor("I", int.class);
        testDescriptor("[I", int[].class);
        testDescriptor("Ljava/lang/Integer;", Integer.class);
        testDescriptor("[[Ljava/lang/Integer;", Integer[][].class);
    }

    private void testDescriptor(String descriptor, Class<?> type) {
        // className -> descriptor
        assertEquals(descriptor, TypeUtil.getDescriptorFromClassName(type.getName()));

        // className -> Type
        assertEquals(descriptor, TypeUtil.getTypeFromClassName(type.getName()).getDescriptor());

        // class -> Type
        assertEquals(descriptor, TypeUtil.getTypeFromClass(type).getDescriptor());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDescriptorFromClassName() {
        TypeUtil.getDescriptorFromClassName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDescriptorFromClassName_2() {
        TypeUtil.getDescriptorFromClassName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTypeFromClassName() {
        TypeUtil.getTypeFromClassName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTypeFromClassName_2() {
        TypeUtil.getTypeFromClassName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTypeFromClass() {
        TypeUtil.getTypeFromClass(null);
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new Type[0], TypeUtil.getTypes(null));
        assertArrayEquals(new Type[0], TypeUtil.getTypes(new Class<?>[0]));

        assertArrayEquals(new Type[] { Type.getType("I"), Type.getType("[Ljava/lang/Integer;") },
                TypeUtil.getTypes(new Class<?>[] { int.class, Integer[].class }));
    }

    @Test
    public void getInternalNames() {
        assertArrayEquals(new String[0], TypeUtil.getInternalNames(null));
        assertArrayEquals(new String[0], TypeUtil.getInternalNames(new Type[0]));

        assertArrayEquals(new String[] { "java/lang/String", "java/lang/Integer" },
                TypeUtil.getInternalNames(TypeUtil.getTypes(new Class<?>[] { String.class, Integer.class })));

        try {
            TypeUtil.getInternalNames(TypeUtil.getTypes(new Class<?>[] { int.class, Integer[].class }));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("not", "internal name"));
        }
    }

    @Test
    public void getConstructorSignature() {
        for (Constructor<?> constructor : String.class.getDeclaredConstructors()) {
            com.alibaba.citrus.asm.commons.Method asmMethod = new com.alibaba.citrus.asm.commons.Method("<init>",
                    Type.getConstructorDescriptor(constructor));

            // 方式一
            MethodSignature signature = TypeUtil.getConstructorSignature(constructor);

            assertEquals(asmMethod.hashCode(), signature.hashCode());
            assertEquals(asmMethod, signature);

            // 方式二
            signature = TypeUtil.getConstructorSignature(constructor.getParameterTypes());

            assertEquals(asmMethod.hashCode(), signature.hashCode());
            assertEquals(asmMethod, signature);
        }
    }

    @Test
    public void getMethodSignature() {
        for (Method method : String.class.getDeclaredMethods()) {
            com.alibaba.citrus.asm.commons.Method asmMethod = new com.alibaba.citrus.asm.commons.Method(
                    method.getName(), Type.getMethodDescriptor(method));

            // 方式一
            MethodSignature signature = TypeUtil.getMethodSignature(method);

            assertEquals(asmMethod.hashCode(), signature.hashCode());
            assertEquals(asmMethod, signature);

            // 方式二
            signature = TypeUtil.getMethodSignature(method.getName(), method.getReturnType(),
                    method.getParameterTypes());

            assertEquals(asmMethod.hashCode(), signature.hashCode());
            assertEquals(asmMethod, signature);
        }
    }

    @Test
    public void testBits() {
        assertTrue(TypeUtil.testBits(6, 2));
        assertFalse(TypeUtil.testBits(6, 1));
    }
}
