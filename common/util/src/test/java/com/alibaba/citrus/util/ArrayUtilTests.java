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
package com.alibaba.citrus.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * 测试<code>ArrayUtil</code>类。
 * 
 * @author Michael Zhou
 */
public class ArrayUtilTests {
    // ==========================================================================
    // 取得数组长度。 
    // ==========================================================================

    @Test
    public void arrayLength() {
        assertEquals(0, ArrayUtil.arrayLength(null));

        assertEquals(0, ArrayUtil.arrayLength(new String[0]));
        assertEquals(10, ArrayUtil.arrayLength(new String[10]));

        assertEquals(0, ArrayUtil.arrayLength(new int[0]));
        assertEquals(10, ArrayUtil.arrayLength(new int[10]));

        assertEquals(0, ArrayUtil.arrayLength(new long[0]));
        assertEquals(10, ArrayUtil.arrayLength(new long[10]));

        assertEquals(0, ArrayUtil.arrayLength(new short[0]));
        assertEquals(10, ArrayUtil.arrayLength(new short[10]));

        assertEquals(0, ArrayUtil.arrayLength(new byte[0]));
        assertEquals(10, ArrayUtil.arrayLength(new byte[10]));

        assertEquals(0, ArrayUtil.arrayLength(new double[0]));
        assertEquals(10, ArrayUtil.arrayLength(new double[10]));

        assertEquals(0, ArrayUtil.arrayLength(new float[0]));
        assertEquals(10, ArrayUtil.arrayLength(new float[10]));

        assertEquals(0, ArrayUtil.arrayLength(new char[0]));
        assertEquals(10, ArrayUtil.arrayLength(new char[10]));

        assertEquals(0, ArrayUtil.arrayLength(new boolean[0]));
        assertEquals(10, ArrayUtil.arrayLength(new boolean[10]));

        assertEquals(0, ArrayUtil.arrayLength(new Object())); // not an array
    }

    // ==========================================================================
    // 判空函数。                                                                  
    //  
    // 判断一个数组是否为null或包含0个元素。                                       
    // ==========================================================================

    @Test
    public void isEmptyArray() {
        assertTrue(ArrayUtil.isEmptyArray(null));

        assertTrue(ArrayUtil.isEmptyArray(new String[0]));
        assertFalse(ArrayUtil.isEmptyArray(new String[10]));

        assertTrue(ArrayUtil.isEmptyArray(new int[0]));
        assertFalse(ArrayUtil.isEmptyArray(new int[10]));

        assertFalse(ArrayUtil.isEmptyArray(new Object())); // not an array
    }

    // ==========================================================================
    // 默认值函数。 
    //  
    // 当数组为空时，取得默认数组值。
    // 注：判断数组为null时，可用更通用的ObjectUtil.defaultIfNull。
    // ==========================================================================

    @Test
    public void defaultIfEmptyArray() {
        // Object array
        String[] empty = new String[0];
        String[] nonempty = new String[10];

        assertSame(empty, ArrayUtil.defaultIfEmptyArray(null, empty));
        assertSame(empty, ArrayUtil.defaultIfEmptyArray(new String[0], empty));
        assertSame(nonempty, ArrayUtil.defaultIfEmptyArray(nonempty, empty));

        // primitive array
        long[] emptylong = new long[0];
        long[] nonemptylong = new long[10];

        assertSame(emptylong, ArrayUtil.defaultIfEmptyArray(null, emptylong));
        assertSame(emptylong, ArrayUtil.defaultIfEmptyArray(new long[0], emptylong));
        assertSame(nonemptylong, ArrayUtil.defaultIfEmptyArray(nonemptylong, emptylong));

        // non-array
        Object anyObject = new Object();

        assertSame(anyObject, ArrayUtil.defaultIfEmptyArray(anyObject, empty));
    }

    // ==========================================================================
    // 将数组转换成集合类。                                                        
    // ==========================================================================

    @Test
    public void arrayAsIterable() {
        // null array
        assertIterable(ArrayUtil.arrayAsIterable(String.class, null));

        // object array
        assertIterable(ArrayUtil.arrayAsIterable(String.class, new String[] { "a", "b", "c" }), "a", "b", "c");

        // primitive array
        assertIterable(ArrayUtil.arrayAsIterable(Integer.class, new int[] { 1, 2, 3 }), 1, 2, 3);
        assertIterable(ArrayUtil.arrayAsIterable(Long.class, new long[] { 1, 2, 3 }), 1L, 2L, 3L);
        assertIterable(ArrayUtil.arrayAsIterable(Short.class, new short[] { 1, 2, 3 }), (short) 1, (short) 2, (short) 3);
        assertIterable(ArrayUtil.arrayAsIterable(Byte.class, new byte[] { 1, 2, 3 }), (byte) 1, (byte) 2, (byte) 3);
        assertIterable(ArrayUtil.arrayAsIterable(Double.class, new double[] { 1, 2, 3 }), 1D, 2D, 3D);
        assertIterable(ArrayUtil.arrayAsIterable(Float.class, new float[] { 1, 2, 3 }), 1F, 2F, 3F);
        assertIterable(ArrayUtil.arrayAsIterable(Boolean.class, new boolean[] { true, false, true }), true, false, true);
        assertIterable(ArrayUtil.arrayAsIterable(Character.class, "abc".toCharArray()), 'a', 'b', 'c');

        // not an array
        try {
            ArrayUtil.arrayAsIterable(String.class, "a");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("a is not an array"));
        }

        // no componentType
        try {
            ArrayUtil.arrayAsIterable(null, new String[] { "a", "b", "c" });
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("componentType"));
        }

        // componentType not match
        try {
            ArrayUtil.arrayAsIterable(String.class, new int[] { 1, 2, 3 }).iterator().next();
            fail();
        } catch (ClassCastException e) {
        }
    }

    private <T> void assertIterable(Iterable<T> iterable, T... expected) {
        assertNotNull(iterable.iterator());

        // 检查iterable内容
        assertArrayEquals(expected, createArrayList(iterable).toArray());

        // 异常情况
        Iterator<T> i = iterable.iterator();

        try {
            i.remove();
            fail();
        } catch (UnsupportedOperationException e) {
            assertThat(e, exception("remove"));
        }

        for (@SuppressWarnings("unused")
        T e : expected) {
            i.next();
        }

        assertFalse(i.hasNext());

        try {
            i.next();
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            assertThat(e, exception(expected.length + ""));
        }
    }

    @Test
    public void arrayToMap() {
        // keyType is null
        try {
            ArrayUtil.arrayToMap(null, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("keyType"));
        }

        // valueType is null
        try {
            ArrayUtil.arrayToMap(null, String.class, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("valueType"));
        }

        Map<String, Integer> map = createLinkedHashMap();

        // keyValueArray is null
        assertNull(ArrayUtil.arrayToMap(null, String.class, Integer.class)); // no map
        assertNull(ArrayUtil.arrayToMap(null, String.class, Integer.class, null)); // no map
        assertSame(map, ArrayUtil.arrayToMap(null, String.class, Integer.class, map)); // with map

        // keyValueArray is not null
        Object[][] colors = { { "RED", 0xFF0000 }, { "GREEN", 0x00FF00 }, { "BLUE", 0x0000FF } };

        Map<String, Integer> result = ArrayUtil.arrayToMap(colors, String.class, Integer.class); // no map

        assertTrue(result instanceof LinkedHashMap<?, ?>);
        assertNotSame(map, result);
        assertEquals("{RED=16711680, GREEN=65280, BLUE=255}", result.toString());

        result = ArrayUtil.arrayToMap(colors, String.class, Integer.class, null); // no map

        assertTrue(result instanceof LinkedHashMap<?, ?>);
        assertNotSame(map, result);
        assertEquals("{RED=16711680, GREEN=65280, BLUE=255}", result.toString());

        result = ArrayUtil.arrayToMap(colors, String.class, Integer.class, map); // with map
        assertSame(map, result);
        assertEquals("{RED=16711680, GREEN=65280, BLUE=255}", result.toString());

        // keyType/valueType doesn't match
        try {
            ArrayUtil.arrayToMap(colors, Long.class, Integer.class, null);
            fail();
        } catch (ClassCastException e) {
        }

        try {
            ArrayUtil.arrayToMap(colors, String.class, Long.class, null);
            fail();
        } catch (ClassCastException e) {
        }

        // keyValue is null
        try {
            ArrayUtil.arrayToMap(new Object[][] { { "RED", 0xFF0000, null }, null, { "BLUE", 0x0000FF } },
                    String.class, Integer.class, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Array element 1 is not an array of 2 elements"));
        }

        // keyValue is not object[2]
        try {
            ArrayUtil.arrayToMap(new Object[][] { { "RED", 0xFF0000, null }, { "GREEN", 0x00FF00 }, { "BLUE" } },
                    String.class, Integer.class, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Array element 2 is not an array of 2 elements"));
        }
    }

    // ==========================================================================
    // 比较数组的长度和类型。
    // ==========================================================================
    @Test
    public void isArraySameLength() {
        assertTrue(ArrayUtil.isArraySameLength(null, (String[]) null));
        assertTrue(ArrayUtil.isArraySameLength(null, (long[]) null));
        assertTrue(ArrayUtil.isArraySameLength(null, (int[]) null));
        assertTrue(ArrayUtil.isArraySameLength(null, (short[]) null));
        assertTrue(ArrayUtil.isArraySameLength(null, (byte[]) null));
        assertTrue(ArrayUtil.isArraySameLength(null, (double[]) null));
        assertTrue(ArrayUtil.isArraySameLength(null, (float[]) null));
        assertTrue(ArrayUtil.isArraySameLength(null, (boolean[]) null));
        assertTrue(ArrayUtil.isArraySameLength(null, (char[]) null));

        assertFalse(ArrayUtil.isArraySameLength(null, new String[3]));
        assertFalse(ArrayUtil.isArraySameLength(null, new long[3]));
        assertFalse(ArrayUtil.isArraySameLength(null, new int[3]));
        assertFalse(ArrayUtil.isArraySameLength(null, new short[3]));
        assertFalse(ArrayUtil.isArraySameLength(null, new byte[3]));
        assertFalse(ArrayUtil.isArraySameLength(null, new double[3]));
        assertFalse(ArrayUtil.isArraySameLength(null, new float[3]));
        assertFalse(ArrayUtil.isArraySameLength(null, new boolean[3]));
        assertFalse(ArrayUtil.isArraySameLength(null, new char[3]));

        assertTrue(ArrayUtil.isArraySameLength(null, new String[0]));
        assertTrue(ArrayUtil.isArraySameLength(null, new long[0]));
        assertTrue(ArrayUtil.isArraySameLength(null, new int[0]));
        assertTrue(ArrayUtil.isArraySameLength(null, new short[0]));
        assertTrue(ArrayUtil.isArraySameLength(null, new byte[0]));
        assertTrue(ArrayUtil.isArraySameLength(null, new double[0]));
        assertTrue(ArrayUtil.isArraySameLength(null, new float[0]));
        assertTrue(ArrayUtil.isArraySameLength(null, new boolean[0]));
        assertTrue(ArrayUtil.isArraySameLength(null, new char[0]));

        assertFalse(ArrayUtil.isArraySameLength(new String[3], null));
        assertFalse(ArrayUtil.isArraySameLength(new long[3], null));
        assertFalse(ArrayUtil.isArraySameLength(new int[3], null));
        assertFalse(ArrayUtil.isArraySameLength(new short[3], null));
        assertFalse(ArrayUtil.isArraySameLength(new byte[3], null));
        assertFalse(ArrayUtil.isArraySameLength(new double[3], null));
        assertFalse(ArrayUtil.isArraySameLength(new float[3], null));
        assertFalse(ArrayUtil.isArraySameLength(new boolean[3], null));
        assertFalse(ArrayUtil.isArraySameLength(new char[3], null));

        assertTrue(ArrayUtil.isArraySameLength(new String[3], new String[3]));
        assertTrue(ArrayUtil.isArraySameLength(new long[3], new long[3]));
        assertTrue(ArrayUtil.isArraySameLength(new int[3], new int[3]));
        assertTrue(ArrayUtil.isArraySameLength(new short[3], new short[3]));
        assertTrue(ArrayUtil.isArraySameLength(new byte[3], new byte[3]));
        assertTrue(ArrayUtil.isArraySameLength(new double[3], new double[3]));
        assertTrue(ArrayUtil.isArraySameLength(new float[3], new float[3]));
        assertTrue(ArrayUtil.isArraySameLength(new boolean[3], new boolean[3]));
        assertTrue(ArrayUtil.isArraySameLength(new char[3], new char[3]));

        assertFalse(ArrayUtil.isArraySameLength(new String[3], new String[4]));
        assertFalse(ArrayUtil.isArraySameLength(new long[3], new long[4]));
        assertFalse(ArrayUtil.isArraySameLength(new int[3], new int[4]));
        assertFalse(ArrayUtil.isArraySameLength(new short[3], new short[4]));
        assertFalse(ArrayUtil.isArraySameLength(new byte[3], new byte[4]));
        assertFalse(ArrayUtil.isArraySameLength(new double[3], new double[4]));
        assertFalse(ArrayUtil.isArraySameLength(new float[3], new float[4]));
        assertFalse(ArrayUtil.isArraySameLength(new boolean[3], new boolean[4]));
        assertFalse(ArrayUtil.isArraySameLength(new char[3], new char[4]));
    }

    // ==========================================================================
    // 反转数组的元素顺序。
    // ==========================================================================

    @Test
    public void arrayReverse() {
        ArrayUtil.arrayReverse((Object[]) null);
        ArrayUtil.arrayReverse((long[]) null);
        ArrayUtil.arrayReverse((short[]) null);
        ArrayUtil.arrayReverse((int[]) null);
        ArrayUtil.arrayReverse((double[]) null);
        ArrayUtil.arrayReverse((float[]) null);
        ArrayUtil.arrayReverse((boolean[]) null);
        ArrayUtil.arrayReverse((char[]) null);
        ArrayUtil.arrayReverse((byte[]) null);

        // object
        Object[] objectArray;

        objectArray = new Object[0];
        ArrayUtil.arrayReverse(objectArray);
        assertArrayReverse(new Object[0], objectArray);

        objectArray = new Object[] { "aaa", "bbb", "ccc" };
        ArrayUtil.arrayReverse(objectArray);
        assertArrayReverse(new Object[] { "ccc", "bbb", "aaa" }, objectArray);

        objectArray = new Object[] { "aaa", "bbb", "ccc", "ddd" };
        ArrayUtil.arrayReverse(objectArray);
        assertArrayReverse(new Object[] { "ddd", "ccc", "bbb", "aaa" }, objectArray);

        // long
        long[] longArray;

        longArray = new long[0];
        ArrayUtil.arrayReverse(longArray);
        assertArrayReverse(new long[0], longArray);

        longArray = new long[] { 111, 222, 333 };
        ArrayUtil.arrayReverse(longArray);
        assertArrayReverse(new long[] { 333, 222, 111 }, longArray);

        longArray = new long[] { 111, 222, 333, 444 };
        ArrayUtil.arrayReverse(longArray);
        assertArrayReverse(new long[] { 444, 333, 222, 111 }, longArray);

        // int
        int[] intArray;

        intArray = new int[0];
        ArrayUtil.arrayReverse(intArray);
        assertArrayReverse(new int[0], intArray);

        intArray = new int[] { 111, 222, 333 };
        ArrayUtil.arrayReverse(intArray);
        assertArrayReverse(new int[] { 333, 222, 111 }, intArray);

        intArray = new int[] { 111, 222, 333, 444 };
        ArrayUtil.arrayReverse(intArray);
        assertArrayReverse(new int[] { 444, 333, 222, 111 }, intArray);

        // short
        short[] shortArray;

        shortArray = new short[0];
        ArrayUtil.arrayReverse(shortArray);
        assertArrayReverse(new short[0], shortArray);

        shortArray = new short[] { 111, 222, 333 };
        ArrayUtil.arrayReverse(shortArray);
        assertArrayReverse(new short[] { 333, 222, 111 }, shortArray);

        shortArray = new short[] { 111, 222, 333, 444 };
        ArrayUtil.arrayReverse(shortArray);
        assertArrayReverse(new short[] { 444, 333, 222, 111 }, shortArray);

        // byte
        byte[] byteArray;

        byteArray = new byte[0];
        ArrayUtil.arrayReverse(byteArray);
        assertArrayReverse(new byte[0], byteArray);

        byteArray = new byte[] { 111, (byte) 222, (byte) 333 };
        ArrayUtil.arrayReverse(byteArray);
        assertArrayReverse(new byte[] { (byte) 333, (byte) 222, 111 }, byteArray);

        byteArray = new byte[] { 111, (byte) 222, (byte) 333, (byte) 444 };
        ArrayUtil.arrayReverse(byteArray);
        assertArrayReverse(new byte[] { (byte) 444, (byte) 333, (byte) 222, 111 }, byteArray);

        // double
        double[] doubleArray;

        doubleArray = new double[0];
        ArrayUtil.arrayReverse(doubleArray);
        assertArrayReverse(new double[0], doubleArray);

        doubleArray = new double[] { 111, 222, 333 };
        ArrayUtil.arrayReverse(doubleArray);
        assertArrayReverse(new double[] { 333, 222, 111 }, doubleArray);

        doubleArray = new double[] { 111, 222, 333, 444 };
        ArrayUtil.arrayReverse(doubleArray);
        assertArrayReverse(new double[] { 444, 333, 222, 111 }, doubleArray);

        // float
        float[] floatArray;

        floatArray = new float[0];
        ArrayUtil.arrayReverse(floatArray);
        assertArrayReverse(new float[0], floatArray);

        floatArray = new float[] { 111, 222, 333 };
        ArrayUtil.arrayReverse(floatArray);
        assertArrayReverse(new float[] { 333, 222, 111 }, floatArray);

        floatArray = new float[] { 111, 222, 333, 444 };
        ArrayUtil.arrayReverse(floatArray);
        assertArrayReverse(new float[] { 444, 333, 222, 111 }, floatArray);

        // boolean
        boolean[] booleanArray;

        booleanArray = new boolean[0];
        ArrayUtil.arrayReverse(booleanArray);
        assertArrayReverse(new boolean[0], booleanArray);

        booleanArray = new boolean[] { true, false, false };
        ArrayUtil.arrayReverse(booleanArray);
        assertArrayReverse(new boolean[] { false, false, true }, booleanArray);

        booleanArray = new boolean[] { true, false, false, false };
        ArrayUtil.arrayReverse(booleanArray);
        assertArrayReverse(new boolean[] { false, false, false, true }, booleanArray);

        // char
        char[] charArray;

        charArray = new char[0];
        ArrayUtil.arrayReverse(charArray);
        assertArrayReverse(new char[0], charArray);

        charArray = new char[] { 111, 222, 333 };
        ArrayUtil.arrayReverse(charArray);
        assertArrayReverse(new char[] { 333, 222, 111 }, charArray);

        charArray = new char[] { 111, 222, 333, 444 };
        ArrayUtil.arrayReverse(charArray);
        assertArrayReverse(new char[] { 444, 333, 222, 111 }, charArray);
    }

    private void assertArrayReverse(Object expected, Object reversed) {
        assertEquals(Array.getLength(expected), Array.getLength(reversed));

        for (int i = 0; i < Array.getLength(expected); i++) {
            assertEquals(Array.get(expected, i), Array.get(reversed, i));
        }
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。
    //
    // 类型：Object[]
    // ==========================================================================
    @Test
    public void indexOfObject() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf(null, "a"));
        assertEquals(1, ArrayUtil.arrayIndexOf(new String[] { "a", null, "c" }, (String) null));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new String[] { "a", "b", "c" }, (String) null));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new String[0], "a"));
        assertEquals(0, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "a"));
        assertEquals(2, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b"));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf(null, "a", 0));
        assertEquals(1, ArrayUtil.arrayIndexOf(new String[] { "a", null, "c" }, (String) null, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new String[0], "a", 0));
        assertEquals(2, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", 0));
        assertEquals(5, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", 3));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", 9));
        assertEquals(2, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", -1));
    }

    @Test
    public void indexOfObjectArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf(null, new String[] { "a" }));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new String[] { "a", "b", "c" }, null));
        assertEquals(0, ArrayUtil.arrayIndexOf(new String[0], new String[0]));
        assertEquals(0,
                ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, new String[] { "a" }));
        assertEquals(2,
                ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, new String[] { "b" }));
        assertEquals(1, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, new String[] {
                "a", "b" }));
        assertEquals(0, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, new String[0]));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf(null, new String[] { "a" }, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new String[] { "a", "b", "c" }, null, 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new String[0], new String[0], 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "a" }, 0));
        assertEquals(2, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "b" }, 0));
        assertEquals(1, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, new String[] {
                "a", "b" }, 0));
        assertEquals(5, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "b" }, 3));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "b" }, 9));
        assertEquals(2, ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "b" }, -1));
        assertEquals(2,
                ArrayUtil.arrayIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, new String[0], 2));
        assertEquals(3, ArrayUtil.arrayIndexOf(new String[] { "a", "b", "c" }, new String[0], 9));
    }

    @Test
    public void lastIndexOfObject() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(null, "a"));
        assertEquals(1, ArrayUtil.arrayLastIndexOf(new String[] { "a", null, "c" }, (String) null));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new String[] { "a", "b", "c" }, (String) null));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new String[0], "a"));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "a"));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b"));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(null, "a", 0));
        assertEquals(1, ArrayUtil.arrayLastIndexOf(new String[] { "a", null, "c" }, (String) null, 2));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new String[0], "a", 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", 8));
        assertEquals(2, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", 4));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", 9));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", -1));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "a", 0));
    }

    @Test
    public void lastIndexOfObjectArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(null, new String[] { "a" }));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new String[] { "a", "b", "c" }, null));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new String[0], new String[] { "a" }));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "a" }));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "b" }));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, new String[] { "b",
                        "a" }));

        assertEquals(8,
                ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, new String[] {}));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(null, new String[] { "a" }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new String[] { "a", "b", "c" }, null, 0));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "a" }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "b" }, 8));
        assertEquals(
                4,
                ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, new String[] { "a",
                        "b" }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "b" }, 9));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "b" }, -1));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "a" }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" },
                new String[] { "b" }, 0));

        assertEquals(0,
                ArrayUtil.arrayLastIndexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, new String[] {}, 0));
    }

    @Test
    public void containsObject() {
        assertFalse(ArrayUtil.arrayContains(null, "a"));
        assertTrue(ArrayUtil.arrayContains(new String[] { "a", null, "c" }, (String) null));
        assertFalse(ArrayUtil.arrayContains(new String[0], "a"));
        assertTrue(ArrayUtil.arrayContains(new String[] { "a", "b", "c" }, "a"));
        assertFalse(ArrayUtil.arrayContains(new String[] { "a", "b", "c" }, "z"));
    }

    @Test
    public void containsObjectArray() {
        assertFalse(ArrayUtil.arrayContains(null, new String[] { "a" }));
        assertFalse(ArrayUtil.arrayContains(new String[] { "a", "b", "c" }, null));
        assertTrue(ArrayUtil.arrayContains(new String[0], new String[0]));
        assertTrue(ArrayUtil.arrayContains(new String[] { "a", "b", "c" }, new String[0]));
        assertTrue(ArrayUtil.arrayContains(new String[] { "a", "b", "c" }, new String[] { "a" }));
        assertFalse(ArrayUtil.arrayContains(new String[] { "a", "b", "c" }, new String[] { "z" }));
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。
    //
    // 类型：long[]
    // ==========================================================================
    @Test
    public void indexOfLong() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((long[]) null, 1L));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new long[0], 1L));
        assertEquals(0, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 1L));
        assertEquals(2, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 2L));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((long[]) null, 1L, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new long[0], 1L, 0));
        assertEquals(2, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 2L, 0));
        assertEquals(5, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 2L, 3));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 2L, 9));
        assertEquals(2, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 2L, -1));
    }

    @Test
    public void indexOfLongArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((long[]) null, new long[] { 1L }));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new long[] { 1L, 2L, 3L }, null));
        assertEquals(0, ArrayUtil.arrayIndexOf(new long[0], new long[0]));
        assertEquals(0, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 1L }));
        assertEquals(2, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 2L }));
        assertEquals(1, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 1L, 2L }));
        assertEquals(0, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[0]));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((long[]) null, new long[] { 1L }, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new long[] { 1L, 2L, 3L }, null, 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new long[0], new long[0], 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 1L }, 0));
        assertEquals(2, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 2L }, 0));
        assertEquals(1, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 1L, 2L }, 0));
        assertEquals(5, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 2L }, 3));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 2L }, 9));
        assertEquals(2, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 2L }, -1));
        assertEquals(2, ArrayUtil.arrayIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[0], 2));
        assertEquals(3, ArrayUtil.arrayIndexOf(new long[] { 1L, 2L, 3L }, new long[0], 9));
    }

    @Test
    public void lastIndexOfLong() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((long[]) null, 1L));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new long[0], 1L));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 1L));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 2L));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((long[]) null, 1L, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new long[0], 1L, 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 2L, 8));
        assertEquals(2, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 2L, 4));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 2L, 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 2L, 9));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 2L, -1));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, 1L, 0));
    }

    @Test
    public void lastIndexOfLongArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((long[]) null, new long[] { 1L }));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 2L, 3L }, null));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new long[0], new long[] { 1L }));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 1L }));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 2L }));
        assertEquals(5,
                ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 2L, 1L }));

        assertEquals(8, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] {}));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((long[]) null, new long[] { 1L }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 2L, 3L }, null, 0));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 1L }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 2L }, 8));
        assertEquals(4,
                ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 1L, 2L }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 2L }, 9));
        assertEquals(-1,
                ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 2L }, -1));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 1L }, 0));
        assertEquals(-1,
                ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] { 2L }, 0));

        assertEquals(0, ArrayUtil.arrayLastIndexOf(new long[] { 1L, 1L, 2L, 1L, 1L, 2L, 1L, 1L }, new long[] {}, 0));
    }

    @Test
    public void containsLong() {
        assertFalse(ArrayUtil.arrayContains((long[]) null, 1L));
        assertFalse(ArrayUtil.arrayContains(new long[0], 1L));
        assertTrue(ArrayUtil.arrayContains(new long[] { 1L, 2L, 3L }, 1L));
        assertFalse(ArrayUtil.arrayContains(new long[] { 1L, 2L, 3L }, 26L));
    }

    @Test
    public void containsLongArray() {
        assertFalse(ArrayUtil.arrayContains((long[]) null, new long[] { 1L }));
        assertFalse(ArrayUtil.arrayContains(new long[] { 1L, 2L, 3L }, null));
        assertTrue(ArrayUtil.arrayContains(new long[0], new long[0]));
        assertTrue(ArrayUtil.arrayContains(new long[] { 1L, 2L, 3L }, new long[0]));
        assertTrue(ArrayUtil.arrayContains(new long[] { 1L, 2L, 3L }, new long[] { 1L }));
        assertFalse(ArrayUtil.arrayContains(new long[] { 1L, 2L, 3L }, new long[] { 26L }));
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。
    //
    // 类型：int[]
    // ==========================================================================
    @Test
    public void indexOfInt() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((int[]) null, 1));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new int[0], 1));
        assertEquals(0, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 1));
        assertEquals(2, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((int[]) null, 1, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new int[0], 1, 0));
        assertEquals(2, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 0));
        assertEquals(5, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 3));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 9));
        assertEquals(2, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, -1));
    }

    @Test
    public void indexOfIntArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((int[]) null, new int[] { 1 }));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new int[] { 1, 2, 3 }, null));
        assertEquals(0, ArrayUtil.arrayIndexOf(new int[0], new int[0]));
        assertEquals(0, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 1 }));
        assertEquals(2, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 2 }));
        assertEquals(1, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 1, 2 }));
        assertEquals(0, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[0]));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((int[]) null, new int[] { 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new int[] { 1, 2, 3 }, null, 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new int[0], new int[0], 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 1 }, 0));
        assertEquals(2, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 2 }, 0));
        assertEquals(1, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 1, 2 }, 0));
        assertEquals(5, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 2 }, 3));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 2 }, 9));
        assertEquals(2, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 2 }, -1));
        assertEquals(2, ArrayUtil.arrayIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[0], 2));
        assertEquals(3, ArrayUtil.arrayIndexOf(new int[] { 1, 2, 3 }, new int[0], 9));
    }

    @Test
    public void lastIndexOfInt() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((int[]) null, 1));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new int[0], 1));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 1));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((int[]) null, 1, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new int[0], 1, 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 8));
        assertEquals(2, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 4));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 9));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, -1));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 1, 0));
    }

    @Test
    public void lastIndexOfIntArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((int[]) null, new int[] { 1 }));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new int[] { 1, 2, 3 }, null));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new int[0], new int[] { 1 }));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 1 }));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 2 }));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 2, 1 }));

        assertEquals(8, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] {}));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((int[]) null, new int[] { 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new int[] { 1, 2, 3 }, null, 0));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 1 }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 2 }, 8));
        assertEquals(4, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 1, 2 }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 2 }, 9));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 2 }, -1));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] { 2 }, 0));

        assertEquals(0, ArrayUtil.arrayLastIndexOf(new int[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new int[] {}, 0));
    }

    @Test
    public void containsInt() {
        assertFalse(ArrayUtil.arrayContains((int[]) null, 1));
        assertFalse(ArrayUtil.arrayContains(new int[0], 1));
        assertTrue(ArrayUtil.arrayContains(new int[] { 1, 2, 3 }, 1));
        assertFalse(ArrayUtil.arrayContains(new int[] { 1, 2, 3 }, 26));
    }

    @Test
    public void containsIntArray() {
        assertFalse(ArrayUtil.arrayContains((int[]) null, new int[] { 1 }));
        assertFalse(ArrayUtil.arrayContains(new int[] { 1, 2, 3 }, null));
        assertTrue(ArrayUtil.arrayContains(new int[0], new int[0]));
        assertTrue(ArrayUtil.arrayContains(new int[] { 1, 2, 3 }, new int[0]));
        assertTrue(ArrayUtil.arrayContains(new int[] { 1, 2, 3 }, new int[] { 1 }));
        assertFalse(ArrayUtil.arrayContains(new int[] { 1, 2, 3 }, new int[] { 26 }));
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。
    //
    // 类型：short[]
    // ==========================================================================
    @Test
    public void indexOfShort() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((short[]) null, (short) 1));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new short[0], (short) 1));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, (short) 1));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, (short) 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((short[]) null, (short) 1, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new short[0], (short) 1, 0));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, (short) 2, 0));
        assertEquals(
                5,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, (short) 2, 3));
        assertEquals(
                -1,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, (short) 2, 9));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, (short) 2, -1));
    }

    @Test
    public void indexOfShortArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((short[]) null, new short[] { (short) 1 }));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 2, (short) 3 }, null));
        assertEquals(0, ArrayUtil.arrayIndexOf(new short[0], new short[0]));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, new short[] { (short) 1 }));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, new short[] { (short) 2 }));
        assertEquals(
                1,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, new short[] { (short) 1, (short) 2 }));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, new short[0]));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((short[]) null, new short[] { (short) 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 2, (short) 3 }, null, 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new short[0], new short[0], 0));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, new short[] { (short) 1 }, 0));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, new short[] { (short) 2 }, 0));
        assertEquals(
                1,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, new short[] { (short) 1, (short) 2 }, 0));
        assertEquals(
                5,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, new short[] { (short) 2 }, 3));
        assertEquals(
                -1,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, new short[] { (short) 2 }, 9));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, new short[] { (short) 2 }, -1));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1, (short) 2,
                        (short) 1, (short) 1 }, new short[0], 2));
        assertEquals(3, ArrayUtil.arrayIndexOf(new short[] { (short) 1, (short) 2, (short) 3 }, new short[0], 9));
    }

    @Test
    public void lastIndexOfShort() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((short[]) null, (short) 1));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new short[0], (short) 1));
        assertEquals(
                7,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, (short) 1));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, (short) 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((short[]) null, (short) 1, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new short[0], (short) 1, 0));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, (short) 2, 8));
        assertEquals(
                2,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, (short) 2, 4));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, (short) 2, 0));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, (short) 2, 9));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, (short) 2, -1));
        assertEquals(
                0,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, (short) 1, 0));
    }

    @Test
    public void lastIndexOfShortArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((short[]) null, new short[] { (short) 1 }));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 2, (short) 3 }, null));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new short[0], new short[] { (short) 1 }));
        assertEquals(
                7,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] { (short) 1 }));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] { (short) 2 }));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] { (short) 2, (short) 1 }));

        assertEquals(
                8,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] {}));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((short[]) null, new short[] { (short) 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 2, (short) 3 }, null, 0));
        assertEquals(
                7,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] { (short) 1 }, 8));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] { (short) 2 }, 8));
        assertEquals(
                4,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] { (short) 1, (short) 2 }, 8));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] { (short) 2 }, 9));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] { (short) 2 }, -1));
        assertEquals(
                0,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] { (short) 1 }, 0));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] { (short) 2 }, 0));

        assertEquals(
                0,
                ArrayUtil.arrayLastIndexOf(new short[] { (short) 1, (short) 1, (short) 2, (short) 1, (short) 1,
                        (short) 2, (short) 1, (short) 1 }, new short[] {}, 0));
    }

    @Test
    public void containsShort() {
        assertFalse(ArrayUtil.arrayContains((short[]) null, (short) 1));
        assertFalse(ArrayUtil.arrayContains(new short[0], (short) 1));
        assertTrue(ArrayUtil.arrayContains(new short[] { (short) 1, (short) 2, (short) 3 }, (short) 1));
        assertFalse(ArrayUtil.arrayContains(new short[] { (short) 1, (short) 2, (short) 3 }, (short) 26));
    }

    @Test
    public void containsShortArray() {
        assertFalse(ArrayUtil.arrayContains((short[]) null, new short[] { (short) 1 }));
        assertFalse(ArrayUtil.arrayContains(new short[] { (short) 1, (short) 2, (short) 3 }, null));
        assertTrue(ArrayUtil.arrayContains(new short[0], new short[0]));
        assertTrue(ArrayUtil.arrayContains(new short[] { (short) 1, (short) 2, (short) 3 }, new short[0]));
        assertTrue(ArrayUtil.arrayContains(new short[] { (short) 1, (short) 2, (short) 3 }, new short[] { (short) 1 }));
        assertFalse(ArrayUtil
                .arrayContains(new short[] { (short) 1, (short) 2, (short) 3 }, new short[] { (short) 26 }));
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。
    //
    // 类型：byte[]
    // ==========================================================================
    @Test
    public void indexOfByte() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((byte[]) null, (byte) 1));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new byte[0], (byte) 1));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 1));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((byte[]) null, (byte) 1, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new byte[0], (byte) 1, 0));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 2, 0));
        assertEquals(
                5,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 2, 3));
        assertEquals(
                -1,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 2, 9));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 2, -1));
    }

    @Test
    public void indexOfByteArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((byte[]) null, new byte[] { (byte) 1 }));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 2, (byte) 3 }, null));
        assertEquals(0, ArrayUtil.arrayIndexOf(new byte[0], new byte[0]));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 1 }));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 2 }));
        assertEquals(
                1,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 1, (byte) 2 }));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[0]));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((byte[]) null, new byte[] { (byte) 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 2, (byte) 3 }, null, 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new byte[0], new byte[0], 0));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 1 }, 0));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 2 }, 0));
        assertEquals(
                1,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 1, (byte) 2 }, 0));
        assertEquals(
                5,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 2 }, 3));
        assertEquals(
                -1,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 2 }, 9));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 2 }, -1));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[0], 2));
        assertEquals(3, ArrayUtil.arrayIndexOf(new byte[] { (byte) 1, (byte) 2, (byte) 3 }, new byte[0], 9));
    }

    @Test
    public void lastIndexOfByte() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((byte[]) null, (byte) 1));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new byte[0], (byte) 1));
        assertEquals(
                7,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 1));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((byte[]) null, (byte) 1, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new byte[0], (byte) 1, 0));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 2, 8));
        assertEquals(
                2,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 2, 4));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 2, 0));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 2, 9));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 2, -1));
        assertEquals(
                0,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, (byte) 1, 0));
    }

    @Test
    public void lastIndexOfByteArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((byte[]) null, new byte[] { (byte) 1 }));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 2, (byte) 3 }, null));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new byte[0], new byte[] { (byte) 1 }));
        assertEquals(
                7,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 1 }));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 2 }));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 2, (byte) 1 }));

        assertEquals(
                8,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] {}));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((byte[]) null, new byte[] { (byte) 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 2, (byte) 3 }, null, 0));
        assertEquals(
                7,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 1 }, 8));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 2 }, 8));
        assertEquals(
                4,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 1, (byte) 2 }, 8));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 2 }, 9));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 2 }, -1));
        assertEquals(
                0,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 1 }, 0));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] { (byte) 2 }, 0));

        assertEquals(
                0,
                ArrayUtil.arrayLastIndexOf(new byte[] { (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
                        (byte) 1, (byte) 1 }, new byte[] {}, 0));
    }

    @Test
    public void containsByte() {
        assertFalse(ArrayUtil.arrayContains((byte[]) null, (byte) 1));
        assertFalse(ArrayUtil.arrayContains(new byte[0], (byte) 1));
        assertTrue(ArrayUtil.arrayContains(new byte[] { (byte) 1, (byte) 2, (byte) 3 }, (byte) 1));
        assertFalse(ArrayUtil.arrayContains(new byte[] { (byte) 1, (byte) 2, (byte) 3 }, (byte) 26));
    }

    @Test
    public void containsByteArray() {
        assertFalse(ArrayUtil.arrayContains((byte[]) null, new byte[] { (byte) 1 }));
        assertFalse(ArrayUtil.arrayContains(new byte[] { (byte) 1, (byte) 2, (byte) 3 }, null));
        assertTrue(ArrayUtil.arrayContains(new byte[0], new byte[0]));
        assertTrue(ArrayUtil.arrayContains(new byte[] { (byte) 1, (byte) 2, (byte) 3 }, new byte[0]));
        assertTrue(ArrayUtil.arrayContains(new byte[] { (byte) 1, (byte) 2, (byte) 3 }, new byte[] { (byte) 1 }));
        assertFalse(ArrayUtil.arrayContains(new byte[] { (byte) 1, (byte) 2, (byte) 3 }, new byte[] { (byte) 26 }));
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。
    //
    // 类型：double[]
    // ==========================================================================
    @Test
    public void indexOfDouble() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((double[]) null, 1));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[0], 1));
        assertEquals(0, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 1));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((double[]) null, 1, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[0], 1, 0));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 0));
        assertEquals(5, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 3));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 9));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, -1));
    }

    @Test
    public void indexOfDoubleTolerance() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((double[]) null, 1, 0.2));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[0], 1, 0.2));
        assertEquals(0, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 1, 0.2));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 2, 0.2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((double[]) null, 1, 0, 0.2));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[0], 1, 0, 0.2));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 2, 0, 0.2));
        assertEquals(5, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 2, 3, 0.2));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 2, 9, 0.2));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 2, -1, 0.2));
    }

    @Test
    public void indexOfDoubleArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((double[]) null, new double[] { 1 }));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[] { 1, 2, 3 }, null));
        assertEquals(0, ArrayUtil.arrayIndexOf(new double[0], new double[0]));
        assertEquals(0, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 1 }));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 2 }));
        assertEquals(1, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 1, 2 }));
        assertEquals(0, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[0]));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((double[]) null, new double[] { 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[] { 1, 2, 3 }, null, 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new double[0], new double[0], 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 1 }, 0));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 2 }, 0));
        assertEquals(1, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 1, 2 }, 0));
        assertEquals(5, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 2 }, 3));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 2 }, 9));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 2 }, -1));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[0], 2));
        assertEquals(3, ArrayUtil.arrayIndexOf(new double[] { 1, 2, 3 }, new double[0], 9));
    }

    @Test
    public void indexOfDoubleArrayTolerance() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((double[]) null, new double[] { 1.1 }, 0.2));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[] { 1.1, 2.1, 3.1 }, null, 0.2));
        assertEquals(0, ArrayUtil.arrayIndexOf(new double[0], new double[0], 0.2));
        assertEquals(0, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 1 }, 0.2));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 2 }, 0.2));
        assertEquals(1, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, new double[] {
                1, 2 }, 0.2));
        assertEquals(0,
                ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, new double[0], 0.2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((double[]) null, new double[] { 1 }, 0, 0.2));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[] { 1, 2, 3 }, null, 0, 0.2));
        assertEquals(0, ArrayUtil.arrayIndexOf(new double[0], new double[0], 0, 0.2));
        assertEquals(0, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 1 }, 0, 0.2));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 2 }, 0, 0.2));
        assertEquals(1, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, new double[] {
                1, 2 }, 0, 0.2));
        assertEquals(5, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 2 }, 3, 0.2));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 2 }, 9, 0.2));
        assertEquals(2, ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 2 }, -1, 0.2));
        assertEquals(2,
                ArrayUtil.arrayIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, new double[0], 2, 0.2));
        assertEquals(3, ArrayUtil.arrayIndexOf(new double[] { 1.1, 2.1, 3.1 }, new double[0], 9, 0.2));
    }

    @Test
    public void lastIndexOfDouble() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((double[]) null, 1));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[0], 1));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 1));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((double[]) null, 1, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[0], 1, 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 8));
        assertEquals(2, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 4));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 9));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, -1));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 1, 0));
    }

    @Test
    public void lastIndexOfDoubleTolerance() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((double[]) null, 1, 0.2));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[0], 1, 0.2));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 1, 0.2));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 2, 0.2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((double[]) null, 1, 0, 0.2));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[0], 1, 0, 0.2));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 2, 8, 0.2));
        assertEquals(2, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 2, 4, 0.2));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 2, 0, 0.2));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 2, 9, 0.2));
        assertEquals(-1,
                ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 2, -1, 0.2));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 }, 1, 0, 0.2));
    }

    @Test
    public void lastIndexOfDoubleArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((double[]) null, new double[] { 1 }));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[] { 1, 2, 3 }, null));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[0], new double[] { 1 }));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 1 }));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 2 }));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 2, 1 }));

        assertEquals(8, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] {}));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((double[]) null, new double[] { 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[] { 1, 2, 3 }, null, 0));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 1 }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 2 }, 8));
        assertEquals(4, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 1, 2 }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 2 }, 9));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 2 }, -1));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] { 2 }, 0));

        assertEquals(0, ArrayUtil.arrayLastIndexOf(new double[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new double[] {}, 0));
    }

    @Test
    public void lastIndexOfDoubleArrayTolerance() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((double[]) null, new double[] { 1 }, 0.2));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 2.1, 3.1 }, null, 0.2));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[0], new double[] { 1 }, 0.2));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 1 }, 0.2));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 2 }, 0.2));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 2, 1 }, 0.2));

        assertEquals(8, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] {}, 0.2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((double[]) null, new double[] { 1 }, 0, 0.2));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 2.1, 3.1 }, null, 0, 0.2));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 1 }, 8, 0.2));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 2 }, 8, 0.2));
        assertEquals(4, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 1, 2 }, 8, 0.2));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 2 }, 9, 0.2));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 2 }, -1, 0.2));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 1 }, 0, 0.2));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] { 2 }, 0, 0.2));

        assertEquals(0, ArrayUtil.arrayLastIndexOf(new double[] { 1.1, 1.1, 2.1, 1.1, 1.1, 2.1, 1.1, 1.1 },
                new double[] {}, 0, 0.2));
    }

    @Test
    public void containsDouble() {
        assertFalse(ArrayUtil.arrayContains((double[]) null, 1));
        assertFalse(ArrayUtil.arrayContains(new double[0], 1));
        assertTrue(ArrayUtil.arrayContains(new double[] { 1, 2, 3 }, 1));
        assertFalse(ArrayUtil.arrayContains(new double[] { 1, 2, 3 }, 26));
    }

    @Test
    public void containsDoubleTolerance() {
        assertFalse(ArrayUtil.arrayContains((double[]) null, 1, 0.2));
        assertFalse(ArrayUtil.arrayContains(new double[0], 1, 0.2));
        assertTrue(ArrayUtil.arrayContains(new double[] { 1.1, 2.1, 3.1 }, 1, 0.2));
        assertFalse(ArrayUtil.arrayContains(new double[] { 1.1, 2.1, 3.1 }, 26, 0.2));
    }

    @Test
    public void containsDoubleArray() {
        assertFalse(ArrayUtil.arrayContains((double[]) null, new double[] { 1 }));
        assertFalse(ArrayUtil.arrayContains(new double[] { 1, 2, 3 }, null));
        assertTrue(ArrayUtil.arrayContains(new double[0], new double[0]));
        assertTrue(ArrayUtil.arrayContains(new double[] { 1, 2, 3 }, new double[0]));
        assertTrue(ArrayUtil.arrayContains(new double[] { 1, 2, 3 }, new double[] { 1 }));
        assertFalse(ArrayUtil.arrayContains(new double[] { 1, 2, 3 }, new double[] { 26 }));
    }

    @Test
    public void containsDoubleArrayTolerance() {
        assertFalse(ArrayUtil.arrayContains((double[]) null, new double[] { 1 }, 0.2));
        assertFalse(ArrayUtil.arrayContains(new double[] { 1.1, 2.1, 3.1 }, null, 0.2));
        assertTrue(ArrayUtil.arrayContains(new double[0], new double[0], 0.2));
        assertTrue(ArrayUtil.arrayContains(new double[] { 1.1, 2.1, 3.1 }, new double[0], 0.2));
        assertTrue(ArrayUtil.arrayContains(new double[] { 1.1, 2.1, 3.1 }, new double[] { 1 }, 0.2));
        assertFalse(ArrayUtil.arrayContains(new double[] { 1.1, 2.1, 3.1 }, new double[] { 26 }, 0.2));
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。
    //
    // 类型：float[]
    // ==========================================================================
    @Test
    public void indexOfFloat() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((float[]) null, 1));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new float[0], 1));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 1));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((float[]) null, 1, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new float[0], 1, 0));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 0));
        assertEquals(5, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 3));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 9));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, -1));
    }

    @Test
    public void indexOfFloatTolerance() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((float[]) null, 1, 0.2F));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new float[0], 1, 0.2F));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 1, 0.2F));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 2, 0.2F));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((float[]) null, 1, 0, 0.2F));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new float[0], 1, 0, 0.2F));
        assertEquals(2,
                ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 2, 0, 0.2F));
        assertEquals(5,
                ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 2, 3, 0.2F));
        assertEquals(-1,
                ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 2, 9, 0.2F));
        assertEquals(2,
                ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 2, -1, 0.2F));
    }

    @Test
    public void indexOfFloatArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((float[]) null, new float[] { 1 }));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new float[] { 1, 2, 3 }, null));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[0], new float[0]));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 1 }));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 2 }));
        assertEquals(1, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 1, 2 }));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[0]));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((float[]) null, new float[] { 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new float[] { 1, 2, 3 }, null, 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[0], new float[0], 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 1 }, 0));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 2 }, 0));
        assertEquals(1, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 1, 2 }, 0));
        assertEquals(5, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 2 }, 3));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 2 }, 9));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 2 }, -1));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[0], 2));
        assertEquals(3, ArrayUtil.arrayIndexOf(new float[] { 1, 2, 3 }, new float[0], 9));
    }

    @Test
    public void indexOfFloatArrayTolerance() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((float[]) null, new float[] { 1 }, 0.2F));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 2.1F, 3.1F }, null, 0.2F));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[0], new float[0], 0.2F));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 1 }, 0.2F));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 2 }, 0.2F));
        assertEquals(
                1,
                ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, new float[] { 1,
                        2 }, 0.2F));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[0], 0.2F));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((float[]) null, new float[] { 1 }, 0, 0.2F));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 2.1F, 3.1F }, null, 0, 0.2F));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[0], new float[0], 0, 0.2F));
        assertEquals(0, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 1 }, 0, 0.2F));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 2 }, 0, 0.2F));
        assertEquals(
                1,
                ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, new float[] { 1,
                        2 }, 0, 0.2F));
        assertEquals(5, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 2 }, 3, 0.2F));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 2 }, 9, 0.2F));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 2 }, -1, 0.2F));
        assertEquals(2, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[0], 2, 0.2F));
        assertEquals(3, ArrayUtil.arrayIndexOf(new float[] { 1.1F, 2.1F, 3.1F }, new float[0], 9, 0.2F));
    }

    @Test
    public void lastIndexOfFloat() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((float[]) null, 1));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[0], 1));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 1));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((float[]) null, 1, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[0], 1, 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 8));
        assertEquals(2, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 4));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, 9));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 2, -1));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, 1, 0));
    }

    @Test
    public void lastIndexOfFloatTolerance() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((float[]) null, 1, 0.2F));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[0], 1, 0.2F));
        assertEquals(7,
                ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 1, 0.2F));
        assertEquals(5,
                ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 2, 0.2F));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((float[]) null, 1, 0, 0.2F));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[0], 1, 0, 0.2F));
        assertEquals(5,
                ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 2, 8, 0.2F));
        assertEquals(2,
                ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 2, 4, 0.2F));
        assertEquals(-1,
                ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 2, 0, 0.2F));
        assertEquals(5,
                ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 2, 9, 0.2F));
        assertEquals(-1,
                ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 2, -1, 0.2F));
        assertEquals(0,
                ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, 1, 0, 0.2F));
    }

    @Test
    public void lastIndexOfFloatArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((float[]) null, new float[] { 1 }));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[] { 1, 2, 3 }, null));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[0], new float[] { 1 }));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 1 }));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 2 }));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 2, 1 }));

        assertEquals(8, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] {}));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((float[]) null, new float[] { 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[] { 1, 2, 3 }, null, 0));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 1 }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 2 }, 8));
        assertEquals(4, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 1, 2 }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 2 }, 9));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 2 }, -1));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] { 2 }, 0));

        assertEquals(0, ArrayUtil.arrayLastIndexOf(new float[] { 1, 1, 2, 1, 1, 2, 1, 1 }, new float[] {}, 0));
    }

    @Test
    public void lastIndexOfFloatArrayTolerance() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((float[]) null, new float[] { 1 }, 0.2F));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 2.1F, 3.1F }, null, 0.2F));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[0], new float[] { 1 }, 0.2F));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 1 }, 0.2F));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 2 }, 0.2F));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, new float[] {
                        2, 1 }, 0.2F));

        assertEquals(8, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] {}, 0.2F));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((float[]) null, new float[] { 1 }, 0, 0.2F));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 2.1F, 3.1F }, null, 0, 0.2F));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 1 }, 8, 0.2F));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 2 }, 8, 0.2F));
        assertEquals(
                4,
                ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F }, new float[] {
                        1, 2 }, 8, 0.2F));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 2 }, 9, 0.2F));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 2 }, -1, 0.2F));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 1 }, 0, 0.2F));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] { 2 }, 0, 0.2F));

        assertEquals(0, ArrayUtil.arrayLastIndexOf(new float[] { 1.1F, 1.1F, 2.1F, 1.1F, 1.1F, 2.1F, 1.1F, 1.1F },
                new float[] {}, 0, 0.2F));
    }

    @Test
    public void containsFloat() {
        assertFalse(ArrayUtil.arrayContains((float[]) null, 1));
        assertFalse(ArrayUtil.arrayContains(new float[0], 1));
        assertTrue(ArrayUtil.arrayContains(new float[] { 1, 2, 3 }, 1));
        assertFalse(ArrayUtil.arrayContains(new float[] { 1, 2, 3 }, 26));
    }

    @Test
    public void containsFloatTolerance() {
        assertFalse(ArrayUtil.arrayContains((float[]) null, 1, 0.2F));
        assertFalse(ArrayUtil.arrayContains(new float[0], 1, 0.2F));
        assertTrue(ArrayUtil.arrayContains(new float[] { 1.1F, 2.1F, 3.1F }, 1, 0.2F));
        assertFalse(ArrayUtil.arrayContains(new float[] { 1.1F, 2.1F, 3.1F }, 26, 0.2F));
    }

    @Test
    public void containsFloatArray() {
        assertFalse(ArrayUtil.arrayContains((float[]) null, new float[] { 1 }));
        assertFalse(ArrayUtil.arrayContains(new float[] { 1, 2, 3 }, null));
        assertTrue(ArrayUtil.arrayContains(new float[0], new float[0]));
        assertTrue(ArrayUtil.arrayContains(new float[] { 1, 2, 3 }, new float[0]));
        assertTrue(ArrayUtil.arrayContains(new float[] { 1, 2, 3 }, new float[] { 1 }));
        assertFalse(ArrayUtil.arrayContains(new float[] { 1, 2, 3 }, new float[] { 26 }));
    }

    @Test
    public void containsFloatArrayTolerance() {
        assertFalse(ArrayUtil.arrayContains((float[]) null, new float[] { 1 }, 0.2F));
        assertFalse(ArrayUtil.arrayContains(new float[] { 1.1F, 2.1F, 3.1F }, null, 0.2F));
        assertTrue(ArrayUtil.arrayContains(new float[0], new float[0], 0.2F));
        assertTrue(ArrayUtil.arrayContains(new float[] { 1.1F, 2.1F, 3.1F }, new float[0], 0.2F));
        assertTrue(ArrayUtil.arrayContains(new float[] { 1.1F, 2.1F, 3.1F }, new float[] { 1 }, 0.2F));
        assertFalse(ArrayUtil.arrayContains(new float[] { 1.1F, 2.1F, 3.1F }, new float[] { 26 }, 0.2F));
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。
    //
    // 类型：boolean[]
    // ==========================================================================
    @Test
    public void indexOfBoolean() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((boolean[]) null, true));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new boolean[0], true));
        assertEquals(0,
                ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true }, true));
        assertEquals(2,
                ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true }, false));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((boolean[]) null, true, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new boolean[0], true, 0));
        assertEquals(2,
                ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true }, false, 0));
        assertEquals(5,
                ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true }, false, 3));
        assertEquals(-1,
                ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true }, false, 9));
        assertEquals(2,
                ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true }, false, -1));
    }

    @Test
    public void indexOfBooleanArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((boolean[]) null, new boolean[] { true }));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new boolean[] { true, true, true }, null));
        assertEquals(0, ArrayUtil.arrayIndexOf(new boolean[0], new boolean[0]));
        assertEquals(0, ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { true }));
        assertEquals(2, ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { false }));
        assertEquals(1, ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { true, false }));
        assertEquals(0, ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[0]));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((boolean[]) null, new boolean[] { true }, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new boolean[] { true, true, true }, null, 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new boolean[0], new boolean[0], 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { true }, 0));
        assertEquals(2, ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { false }, 0));
        assertEquals(1, ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { true, false }, 0));
        assertEquals(5, ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { false }, 3));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { false }, 9));
        assertEquals(2, ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { false }, -1));
        assertEquals(2, ArrayUtil.arrayIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[0], 2));
        assertEquals(3, ArrayUtil.arrayIndexOf(new boolean[] { true, true, true }, new boolean[0], 9));
    }

    @Test
    public void lastIndexOfBoolean() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((boolean[]) null, true));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new boolean[0], true));
        assertEquals(7,
                ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true }, true));
        assertEquals(5,
                ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true }, false));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((boolean[]) null, true, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new boolean[0], true, 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                false, 8));
        assertEquals(2, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                false, 4));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                false, 0));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                false, 9));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                false, -1));
        assertEquals(0,
                ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true }, true, 0));
    }

    @Test
    public void lastIndexOfBooleanArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((boolean[]) null, new boolean[] { true }));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, true }, null));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new boolean[0], new boolean[] { true }));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { true }));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { false }));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { false, true }));

        assertEquals(8, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] {}));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((boolean[]) null, new boolean[] { true }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, true }, null, 0));
        assertEquals(7, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { true }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { false }, 8));
        assertEquals(4, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { true, false }, 8));
        assertEquals(5, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { false }, 9));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { false }, -1));
        assertEquals(0, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { true }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] { false }, 0));

        assertEquals(0, ArrayUtil.arrayLastIndexOf(new boolean[] { true, true, false, true, true, false, true, true },
                new boolean[] {}, 0));
    }

    @Test
    public void containsBoolean() {
        assertFalse(ArrayUtil.arrayContains((boolean[]) null, true));
        assertFalse(ArrayUtil.arrayContains(new boolean[0], true));
        assertTrue(ArrayUtil.arrayContains(new boolean[] { true, true, true }, true));
        assertFalse(ArrayUtil.arrayContains(new boolean[] { true, true, true }, false));
    }

    @Test
    public void containsBooleanArray() {
        assertFalse(ArrayUtil.arrayContains((boolean[]) null, new boolean[] { true }));
        assertFalse(ArrayUtil.arrayContains(new boolean[] { true, true, true }, null));
        assertTrue(ArrayUtil.arrayContains(new boolean[0], new boolean[0]));
        assertTrue(ArrayUtil.arrayContains(new boolean[] { true, true, true }, new boolean[0]));
        assertTrue(ArrayUtil.arrayContains(new boolean[] { true, true, true }, new boolean[] { true }));
        assertFalse(ArrayUtil.arrayContains(new boolean[] { true, true, true }, new boolean[] { false }));
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。
    //
    // 类型：char[]
    // ==========================================================================
    @Test
    public void indexOfChar() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((char[]) null, (char) 1));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new char[0], (char) 1));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 1));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((char[]) null, (char) 1, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new char[0], (char) 1, 0));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 2, 0));
        assertEquals(
                5,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 2, 3));
        assertEquals(
                -1,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 2, 9));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 2, -1));
    }

    @Test
    public void indexOfCharArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayIndexOf((char[]) null, new char[] { (char) 1 }));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 2, (char) 3 }, null));
        assertEquals(0, ArrayUtil.arrayIndexOf(new char[0], new char[0]));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 1 }));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 2 }));
        assertEquals(
                1,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 1, (char) 2 }));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[0]));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayIndexOf((char[]) null, new char[] { (char) 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 2, (char) 3 }, null, 0));
        assertEquals(0, ArrayUtil.arrayIndexOf(new char[0], new char[0], 0));
        assertEquals(
                0,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 1 }, 0));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 2 }, 0));
        assertEquals(
                1,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 1, (char) 2 }, 0));
        assertEquals(
                5,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 2 }, 3));
        assertEquals(
                -1,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 2 }, 9));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 2 }, -1));
        assertEquals(
                2,
                ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[0], 2));
        assertEquals(3, ArrayUtil.arrayIndexOf(new char[] { (char) 1, (char) 2, (char) 3 }, new char[0], 9));
    }

    @Test
    public void lastIndexOfChar() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((char[]) null, (char) 1));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new char[0], (char) 1));
        assertEquals(
                7,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 1));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 2));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((char[]) null, (char) 1, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new char[0], (char) 1, 0));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 2, 8));
        assertEquals(
                2,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 2, 4));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 2, 0));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 2, 9));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 2, -1));
        assertEquals(
                0,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, (char) 1, 0));
    }

    @Test
    public void lastIndexOfCharArray() {
        // 形式1
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((char[]) null, new char[] { (char) 1 }));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 2, (char) 3 }, null));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new char[0], new char[] { (char) 1 }));
        assertEquals(
                7,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 1 }));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 2 }));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 2, (char) 1 }));

        assertEquals(
                8,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] {}));

        // 形式2
        assertEquals(-1, ArrayUtil.arrayLastIndexOf((char[]) null, new char[] { (char) 1 }, 0));
        assertEquals(-1, ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 2, (char) 3 }, null, 0));
        assertEquals(
                7,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 1 }, 8));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 2 }, 8));
        assertEquals(
                4,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 1, (char) 2 }, 8));
        assertEquals(
                5,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 2 }, 9));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 2 }, -1));
        assertEquals(
                0,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 1 }, 0));
        assertEquals(
                -1,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] { (char) 2 }, 0));

        assertEquals(
                0,
                ArrayUtil.arrayLastIndexOf(new char[] { (char) 1, (char) 1, (char) 2, (char) 1, (char) 1, (char) 2,
                        (char) 1, (char) 1 }, new char[] {}, 0));
    }

    @Test
    public void containsChar() {
        assertFalse(ArrayUtil.arrayContains((char[]) null, (char) 1));
        assertFalse(ArrayUtil.arrayContains(new char[0], (char) 1));
        assertTrue(ArrayUtil.arrayContains(new char[] { (char) 1, (char) 2, (char) 3 }, (char) 1));
        assertFalse(ArrayUtil.arrayContains(new char[] { (char) 1, (char) 2, (char) 3 }, (char) 26));
    }

    @Test
    public void containsCharArray() {
        assertFalse(ArrayUtil.arrayContains((char[]) null, new char[] { (char) 1 }));
        assertFalse(ArrayUtil.arrayContains(new char[] { (char) 1, (char) 2, (char) 3 }, null));
        assertTrue(ArrayUtil.arrayContains(new char[0], new char[0]));
        assertTrue(ArrayUtil.arrayContains(new char[] { (char) 1, (char) 2, (char) 3 }, new char[0]));
        assertTrue(ArrayUtil.arrayContains(new char[] { (char) 1, (char) 2, (char) 3 }, new char[] { (char) 1 }));
        assertFalse(ArrayUtil.arrayContains(new char[] { (char) 1, (char) 2, (char) 3 }, new char[] { (char) 26 }));
    }
}
