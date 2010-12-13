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

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.alibaba.citrus.generictype.impl.TypeInfoFactory;

/**
 * 测试类的基类。
 * 
 * @author Michael Zhou
 */
public abstract class BaseTypeTests {
    protected static Type getReturnType(Class<?> ownerType, String methodName) {
        try {
            return ownerType.getDeclaredMethod(methodName).getGenericReturnType();
        } catch (Exception e) {
            fail(e.toString());
            return null;
        }
    }

    protected static Type getArgOfReturnType(Class<?> ownerType, String methodName) {
        return ((ParameterizedType) getReturnType(ownerType, methodName)).getActualTypeArguments()[0];
    }

    /**
     * 取得指定名称的method或指定参数个数的constructor。
     */
    protected static GenericDeclaration getMethodOrConstructor(Class<?> ownerType, String methodName,
                                                               Class<?>[] paramTypes) throws NoSuchMethodException {
        if (methodName != null) {
            try {
                return ownerType.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                return ownerType.getDeclaredMethod(methodName, paramTypes);
            }
        } else {
            return ownerType.getDeclaredConstructor(paramTypes);
        }
    }

    protected static void assertSupertypes(TypeInfo type, String... expectedSupertypes) {
        List<TypeInfo> supertypes = createArrayList(type.getSupertypes());
        List<TypeInfo> superclasses = createArrayList(type.getSuperclasses());
        List<TypeInfo> interfaces = createArrayList(type.getInterfaces());

        // 先判断顺序：本身、类、接口、Object
        assertThat(supertypes.size(), greaterThan(0));

        // 第一个是自己
        TypeInfo first = findNonBoundedType(type);

        if (first.getComponentType() instanceof BoundedTypeInfo) {
            assertEquals(findNonBoundedType(first.getComponentType()), supertypes.get(0).getComponentType());
        } else {
            assertEquals(first, supertypes.get(0));
        }

        boolean isInterface = type.getRawType().isInterface();

        if (!isInterface && !type.getRawType().isPrimitive()) {
            assertEquals(Object.class, supertypes.get(supertypes.size() - 1).getRawType()); // 最后一个是Object
        }

        // 排序
        Comparator<TypeInfo> comparator = new Comparator<TypeInfo>() {
            public int compare(TypeInfo o1, TypeInfo o2) {
                int c1 = o1.getRawType().isInterface() ? 1 : o1.getRawType().equals(Object.class) ? 2 : 0;
                int c2 = o2.getRawType().isInterface() ? 1 : o2.getRawType().equals(Object.class) ? 2 : 0;

                if (c1 != c2) {
                    return c1 - c2; // 按class, interface, Object排序
                } else if (c1 == 0) {
                    return 0; // class不排序
                } else {
                    return o1.toString().compareTo(o2.toString()); // interface按名称排序
                }
            }
        };

        Collections.sort(supertypes.subList(1, supertypes.size()), comparator);

        if (type.isInterface()) {
            Collections.sort(superclasses, comparator);
            Collections.sort(interfaces.subList(1, interfaces.size()), comparator);
        } else {
            Collections.sort(superclasses.subList(1, superclasses.size()), comparator);
            Collections.sort(interfaces, comparator);
        }

        if (isEmptyArray(expectedSupertypes)) {
            StringBuilder buf = new StringBuilder("\n");

            for (Iterator<TypeInfo> i = supertypes.iterator(); i.hasNext();) {
                buf.append("\"").append(i.next()).append("\"");

                if (i.hasNext()) {
                    buf.append(", ");
                }
            }

            fail(buf.toString());
        }

        Iterator<TypeInfo> i = supertypes.iterator();
        Iterator<TypeInfo> itfs = interfaces.iterator();
        Iterator<TypeInfo> classes = superclasses.iterator();

        for (String expectedSupertype : expectedSupertypes) {
            TypeInfo supertype = i.next();

            assertEquals(expectedSupertype, supertype.toString());

            if (supertype.getRawType().isInterface()) {
                assertEquals(itfs.next(), supertype);
            } else {
                assertEquals(classes.next(), supertype);
            }

            assertTrue(String.format("%s is not assignable from %s", supertype, type), supertype.getRawType()
                    .isAssignableFrom(type.getRawType()));

            // 测试getSupertype()
            assertSame(supertype, type.getSupertype(supertype.getRawType()));
        }

        assertFalse("getSupertypes still has elements", i.hasNext());
        assertFalse("getInterfaces still has elements", itfs.hasNext());
        assertFalse("getSuperclasses still has elements", classes.hasNext());
    }

    // 调用TypeInfoFactory.findNonBoundedType
    protected static TypeInfo findNonBoundedType(TypeInfo type) {
        Method m;

        try {
            m = TypeInfoFactory.class.getDeclaredMethod("findNonBoundedType", TypeInfo.class);
            m.setAccessible(true);

            return (TypeInfo) m.invoke(null, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
