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
package com.alibaba.citrus.service.pull.impl;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

import com.alibaba.citrus.service.pull.RuntimeToolSetFactory;
import com.alibaba.citrus.service.pull.ToolFactory;
import com.alibaba.citrus.service.pull.ToolSetFactory;

public class PullServiceBasicTests extends AbstractPullServiceTests {
    private final static int SINGLETON = 0x1;
    private final static int TOOL_FACTORY = 0x2;
    private final static int TOOL_SET_FACTORY = 0x4;
    private final static int RUNTIME_TOOL_SET_FACTORY = 0x8;

    private ToolFactory singleton_toolFactory = newToolFactory(true, null);
    private ToolSetFactory singleton_toolSetFactory = newToolSetFactory(true, false, null);
    private ToolSetFactory singleton_toolSetFactory2 = newToolSetFactory(true, true, null);
    private RuntimeToolSetFactory singleton_runtimeToolSetFactory = newRuntimeToolSetFactory(true, false, null);
    private RuntimeToolSetFactory singleton_runtimeToolSetFactory2 = newRuntimeToolSetFactory(true, true, null);

    private ToolFactory nonSingleton_toolFactory = newToolFactory(false, null);
    private ToolSetFactory nonSingleton_toolSetFactory = newToolSetFactory(false, false, null);
    private ToolSetFactory nonSingleton_toolSetFactory2 = newToolSetFactory(false, true, null);
    private RuntimeToolSetFactory nonSingleton_runtimeToolSetFactory = newRuntimeToolSetFactory(false, false, null);
    private RuntimeToolSetFactory nonSingleton_runtimeToolSetFactory2 = newRuntimeToolSetFactory(false, true, null);

    @Test
    public void getFactoryType() {
        assertEquals(SINGLETON + TOOL_FACTORY, getFactoryType(singleton_toolFactory));

        assertEquals(SINGLETON + TOOL_SET_FACTORY, getFactoryType(singleton_toolSetFactory));
        assertEquals(SINGLETON + TOOL_FACTORY + TOOL_SET_FACTORY, getFactoryType(singleton_toolSetFactory2));

        assertEquals(RUNTIME_TOOL_SET_FACTORY, getFactoryType(singleton_runtimeToolSetFactory));
        assertEquals(TOOL_FACTORY + RUNTIME_TOOL_SET_FACTORY, getFactoryType(singleton_runtimeToolSetFactory2));

        assertEquals(TOOL_FACTORY, getFactoryType(nonSingleton_toolFactory));

        assertEquals(TOOL_SET_FACTORY, getFactoryType(nonSingleton_toolSetFactory));
        assertEquals(TOOL_FACTORY + TOOL_SET_FACTORY, getFactoryType(nonSingleton_toolSetFactory2));

        assertEquals(RUNTIME_TOOL_SET_FACTORY, getFactoryType(nonSingleton_runtimeToolSetFactory));
        assertEquals(TOOL_FACTORY + RUNTIME_TOOL_SET_FACTORY, getFactoryType(nonSingleton_runtimeToolSetFactory2));

        try {
            getFactoryType(null);
            fail();
        } catch (Exception e) {
            assertThat(e, exception(IllegalArgumentException.class, "unknown pull tool factory type: null"));
        }

        try {
            getFactoryType("wrong type");
            fail();
        } catch (Exception e) {
            assertThat(e, exception(IllegalArgumentException.class, "unknown pull tool factory type: java.lang.String"));
        }
    }

    private int getFactoryType(Object factory) {
        Method method = getAccessibleMethod(PullServiceImpl.class, "getFactoryType", new Class<?>[] { Object.class });

        try {
            return (Integer) method.invoke(null, factory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testBit() {
        assertTrue(testBit(singleton_toolFactory, SINGLETON));
        assertTrue(testBit(singleton_toolFactory, TOOL_FACTORY));

        assertFalse(testBit(singleton_toolFactory, TOOL_SET_FACTORY));
        assertFalse(testBit(singleton_toolFactory, RUNTIME_TOOL_SET_FACTORY));

        assertTrue(testBit(singleton_runtimeToolSetFactory2, RUNTIME_TOOL_SET_FACTORY));
    }

    private boolean testBit(Object factory, int mask) {
        Method method = getAccessibleMethod(PullServiceImpl.class, "testBit", new Class<?>[] { int.class, int.class });

        try {
            return (Boolean) method.invoke(null, getFactoryType(factory), mask);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void encode() {
        assertSame(NULL_PLACEHOLDER, encode(null));
        assertSame(NULL_PLACEHOLDER, encode(NULL_PLACEHOLDER));
        assertEquals("hello", encode("hello"));
    }

    private Object encode(Object object) {
        Method method = getAccessibleMethod(PullServiceImpl.class, "encode", new Class<?>[] { Object.class });

        try {
            return method.invoke(null, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void decode() {
        assertSame(null, decode(null));
        assertSame(null, decode(NULL_PLACEHOLDER));
        assertEquals("hello", decode("hello"));
    }

    private Object decode(Object object) {
        Method method = getAccessibleMethod(PullServiceImpl.class, "decode", new Class<?>[] { Object.class });

        try {
            return method.invoke(null, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
