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

import static com.alibaba.citrus.asm.Type.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import com.alibaba.citrus.asm.Type;
import com.alibaba.citrus.util.StringUtil;

/**
 * 补充<code>Type</code>中的功能。
 * 
 * @author Michael Zhou
 */
public class TypeUtil {
    private static final Map<String, String> PRIMITIVE_DESCRIPTORS = createHashMap();

    static {
        PRIMITIVE_DESCRIPTORS.put("boolean", "Z");
        PRIMITIVE_DESCRIPTORS.put("short", "S");
        PRIMITIVE_DESCRIPTORS.put("int", "I");
        PRIMITIVE_DESCRIPTORS.put("long", "J");
        PRIMITIVE_DESCRIPTORS.put("float", "F");
        PRIMITIVE_DESCRIPTORS.put("double", "D");
        PRIMITIVE_DESCRIPTORS.put("char", "C");
        PRIMITIVE_DESCRIPTORS.put("byte", "B");
        PRIMITIVE_DESCRIPTORS.put("void", "V");
    }

    // ===========================================================
    // [region] Class.getName() -> 各种形式

    /**
     * 取得类名的内部表示。只有普通类和接口可以转换成这种形式，原子类型和数组不支持该方法。
     */
    public static String getInternalNameFromClassName(String className) {
        assertTrue(!StringUtil.isEmpty(className), "className is empty");
        assertTrue(!className.startsWith("["), "%s is an array", className);
        assertTrue(!PRIMITIVE_DESCRIPTORS.containsKey(className), "%s is a primitive type", className);

        return className.replace('.', '/');
    }

    /**
     * 取得类型名称的descriptor。这里的类型名称为<code>Class.getName()</code>的返回值。 例如：
     * 
     * <pre>
     *  TypeUtil.getDescriptorFromClassName(int.class.getName())         = &quot;I&quot;  // int
     *  TypeUtil.getDescriptorFromClassName(int[].class.getName())       = &quot;[I&quot;  // int[]
     *  TypeUtil.getDescriptorFromClassName(Integer.class.getName())     = &quot;Ljava/lang/Integer;&quot; // Integer
     *  TypeUtil.getDescriptorFromClassName(Integer[][].class.getName()) = &quot;[[Ljava/lang/Integer;&quot; // Integer[][]
     * </pre>
     */
    public static String getDescriptorFromClassName(String className) {
        assertTrue(!StringUtil.isEmpty(className), "className is empty");

        // 是数组：[I、[Ljava.lang.Object;
        if (className.startsWith("[")) {
            return className.replace('.', '/');
        }

        // 是primitive类型
        String descriptor = PRIMITIVE_DESCRIPTORS.get(className);

        if (descriptor != null) {
            return descriptor;
        }

        // 是普通类型
        return "L" + className.replace('.', '/') + ";";
    }

    // ===========================================================
    // [region] 各种形式 -> Type

    /**
     * 将内部类名转换成<code>Type</code>。
     */
    public static Type getTypeFromInternalName(String internalName) {
        return getObjectType(internalName);
    }

    /**
     * 将类名转换成<code>Type</code>。
     */
    public static Type getTypeFromClassName(String className) {
        return getType(getDescriptorFromClassName(className));
    }

    /**
     * 将类型转换成<code>Type</code>。
     */
    public static Type getTypeFromClass(Class<?> type) {
        assertNotNull(type, "class");
        return getType(getDescriptorFromClassName(type.getName()));
    }

    // ===========================================================
    // [region] Type或名称的数组

    /**
     * 将一组class转换成<code>Type[]</code>。
     */
    public static Type[] getTypes(Class<?>[] classes) {
        if (classes == null) {
            return new Type[0];
        }

        Type[] types = new Type[classes.length];

        for (int i = classes.length - 1; i >= 0; --i) {
            types[i] = getTypeFromClass(classes[i]);
        }

        return types;
    }

    /**
     * 将<code>Type[]</code>转换成内部名称。
     */
    public static String[] getInternalNames(Type[] types) {
        if (types == null) {
            return new String[0];
        }

        String[] names = new String[types.length];

        for (int i = 0; i < types.length; i++) {
            Type type = types[i];

            assertTrue(type.getSort() == Type.OBJECT, "Type %s does not has internal name", type);

            names[i] = type.getInternalName();
        }

        return names;
    }

    // ===========================================================
    // [region] 各种形式 -> MethodDescriptor

    /**
     * 取得<code>Method</code>对应的签名。
     */
    public static MethodSignature getMethodSignature(Method method) {
        return new MethodSignature(method);
    }

    /**
     * 取得<code>Method</code>对应的签名。
     */
    public static MethodSignature getMethodSignature(String name, Class<?> returnType, Class<?>[] parameterTypes) {
        return new MethodSignature(name, returnType, parameterTypes);
    }

    /**
     * 取得<code>Constructor</code>对应的签名。
     */
    public static MethodSignature getConstructorSignature(Constructor<?> constructor) {
        return new MethodSignature(constructor);
    }

    /**
     * 取得<code>Constructor</code>对应的签名。
     */
    public static MethodSignature getConstructorSignature(Class<?>[] parameterTypes) {
        return new MethodSignature(CodegenConstant.CONSTRUCTOR_NAME, void.class, parameterTypes);
    }

    // ===========================================================
    // [region] 其它功能

    /**
     * 检测标志位是否被设置。
     */
    public static boolean testBits(int flags, int bits) {
        return (flags & bits) != 0;
    }
}
