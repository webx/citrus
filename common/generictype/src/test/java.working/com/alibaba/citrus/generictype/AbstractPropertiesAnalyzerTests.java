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

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import com.alibaba.citrus.generictype.introspect.Introspector;
import com.alibaba.citrus.generictype.introspect.PropertyInfo;
import com.alibaba.citrus.util.internal.ArrayUtil;

/**
 * <code>PropertiesAnalyzerTests</code>µÄ»ùÀà¡£
 * 
 * @author Michael Zhou
 */
public abstract class AbstractPropertiesAnalyzerTests {
    protected final TypeIntrospectionInfo getClassInfo(Class<?> clazz) {
        return new Introspector(clazz, getAnalyzers()).analyze();
    }

    protected abstract ClassAnalyzer[] getAnalyzers();

    protected void assertPropertyInfo(PropertyInfo pi, Class<?> clazz, String name, Class<?> type, boolean isGeneric,
                                      String readMethod, String writeMethod) {
        assertPropertyInfo(pi, clazz, name, type, isGeneric, new String[] { readMethod }, new String[] { writeMethod });
    }

    protected void assertPropertyInfo(PropertyInfo pi, Class<?> clazz, String name, Class<?> type, boolean isGeneric,
                                      String[] readMethods, String[] writeMethods) {
        assertSame(clazz, pi.getDeclaringClass());
        assertEquals(name, pi.getName());
        assertSame(type, pi.getType());

        if (isGeneric) {
            java.lang.reflect.Type gt = pi.getGenericType();

            assertNotSame(type, gt);

            if (gt instanceof ParameterizedType) {
                assertSame(type, ((ParameterizedType) gt).getRawType());
            }
        } else {
            assertSame(type, pi.getGenericType());
        }

        if (ArrayUtil.isEmpty(readMethods) || readMethods.length == 1 && readMethods[0] == null) {
            assertNull(pi.getReadMethod());
        } else {
            String result = getMethodDesc(pi.getReadMethod());
            boolean matched = false;

            for (String readMethod : readMethods) {
                if (result.equals(readMethod)) {
                    matched = true;
                    break;
                }
            }

            assertTrue(matched);
        }

        if (ArrayUtil.isEmpty(writeMethods) || writeMethods.length == 1 && writeMethods[0] == null) {
            assertNull(pi.getWriteMethod());
        } else {
            String result = getMethodDesc(pi.getWriteMethod());
            boolean matched = false;

            for (String writeMethod : writeMethods) {
                if (result.equals(writeMethod)) {
                    matched = true;
                    break;
                }
            }

            assertTrue(matched);
        }
    }

    protected String getMethodDesc(Method method) {
        return method.getName() + Type.getMethodDescriptor(method);
    }
}
