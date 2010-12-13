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
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * ≤‚ ‘{@link TypeInfoTests}°£
 * 
 * @author Michael Zhou
 */
public class TypeInfoUtilTests {
    class StringIntegerListMap extends HashMap<String, List<Integer>> {
        private static final long serialVersionUID = -2430843847500533492L;
    }

    abstract class NumberList<E extends Number> implements List<E> {
    }

    abstract class IntegerList extends NumberList<Integer> {
    }

    abstract class IntegerIterator implements Iterator<Integer> {
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveParameter_outOfBound() {
        TypeInfoUtil.resolveParameter(NumberList.class, List.class, 1);
    }

    @Test
    public void resolveParameter() {
        assertEquals(factory.getType(Number.class), TypeInfoUtil.resolveParameter(NumberList.class, List.class, 0));
        assertEquals(factory.getType(Integer.class), TypeInfoUtil.resolveParameter(IntegerList.class, List.class, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveMapKey_wrong1() {
        TypeInfoUtil.resolveMapKey(Object.class);
    }

    @Test
    public void resolveMapKey() {
        assertEquals(factory.getType(String.class), TypeInfoUtil.resolveMapKey(StringIntegerListMap.class));
        assertEquals(factory.getType(String.class),
                TypeInfoUtil.resolveMapKey(factory.getType(StringIntegerListMap.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveMapValue_wrong1() {
        TypeInfoUtil.resolveMapValue(Object.class);
    }

    @Test
    public void resolveMapValue() {
        assertEquals(factory.getParameterizedType(List.class, Integer.class),
                TypeInfoUtil.resolveMapValue(StringIntegerListMap.class));
        assertEquals(factory.getParameterizedType(List.class, Integer.class),
                TypeInfoUtil.resolveMapValue(factory.getType(StringIntegerListMap.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveIterableElement_wrong1() {
        TypeInfoUtil.resolveIterableElement(Object.class);
    }

    @Test
    public void resolveIterableElement() {
        assertEquals(factory.getType(Number.class), TypeInfoUtil.resolveIterableElement(NumberList.class));
        assertEquals(factory.getType(Number.class),
                TypeInfoUtil.resolveIterableElement(factory.getType(NumberList.class)));

        assertEquals(factory.getType(Integer.class), TypeInfoUtil.resolveIterableElement(IntegerList.class));
        assertEquals(factory.getType(Integer.class),
                TypeInfoUtil.resolveIterableElement(factory.getType(IntegerList.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveIteratorElement_wrong1() {
        TypeInfoUtil.resolveIteratorElement(Object.class);
    }

    @Test
    public void resolveIteratorElement() {
        assertEquals(factory.getType(Integer.class), TypeInfoUtil.resolveIteratorElement(IntegerIterator.class));
        assertEquals(factory.getType(Integer.class),
                TypeInfoUtil.resolveIteratorElement(factory.getType(IntegerIterator.class)));
    }
}
