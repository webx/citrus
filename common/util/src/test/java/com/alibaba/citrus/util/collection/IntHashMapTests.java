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
package com.alibaba.citrus.util.collection;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Before;
import org.junit.Test;

/**
 * 测试<code>IntHashMap</code>类。
 * 
 * @author Michael Zhou
 */
public class IntHashMapTests {
    private IntHashMap<String> map1;
    private IntHashMap<String> map2;
    private IntHashMap<Integer> map3;

    @Before
    public void init() {
        // map1测试一般情况.
        map1 = new IntHashMap<String>();

        map1.put(111, "aaa");
        map1.put(222, "bbb");
        map1.put(333, "ccc");

        // map2测试value为null的情况.
        map2 = new IntHashMap<String>();
        map2.put(111, null);

        // map3为空.
        map3 = new IntHashMap<Integer>();
    }

    @Test
    public void size() {
        assertEquals(3, map1.size());
        assertEquals(1, map2.size());
        assertEquals(0, map3.size());
    }

    @Test
    public void isEmpty() {
        assertFalse(map1.isEmpty());
        assertFalse(map2.isEmpty());
        assertTrue(map3.isEmpty());
    }

    @Test
    public void clear() {
        map1.clear();
        assertEquals(0, map1.size());
        assertTrue(map1.isEmpty());

        map2.clear();
        assertEquals(0, map2.size());
        assertTrue(map2.isEmpty());

        map3.clear();
        assertEquals(0, map3.size());
        assertTrue(map3.isEmpty());
    }

    @Test
    public void containsKey() {
        assertTrue(map1.containsKey(111));
        assertTrue(map1.containsKey(222));
        assertTrue(map1.containsKey(333));
        assertFalse(map1.containsKey(444));

        assertTrue(map2.containsKey(111));
        assertFalse(map2.containsKey(222));

        assertFalse(map3.containsKey(111));
    }

    @Test
    public void containsValue() {
        assertTrue(map1.containsValue("aaa"));
        assertTrue(map1.containsValue("bbb"));
        assertTrue(map1.containsValue("ccc"));
        assertFalse(map1.containsValue("ddd"));

        assertTrue(map2.containsValue(null));
        assertFalse(map2.containsValue("aaa"));

        assertFalse(map3.containsValue(null));
        assertFalse(map3.containsValue("aaa"));
    }

    @Test
    public void get() {
        assertEquals("aaa", map1.get(111));
        assertEquals("bbb", map1.get(222));
        assertEquals("ccc", map1.get(333));
        assertNull(map1.get(444));

        assertNull(map2.get(111));

        assertNull(map3.get(111));
    }

    @Test
    public void put() {
        assertEquals("aaa", map1.put(111, "111+111")); // 替换111
        assertEquals(null, map1.put(444, "222+222")); // 新增444
        assertEquals("111+111", map1.get(111));
        assertEquals("222+222", map1.get(444));
        assertEquals(4, map1.size());

        assertEquals(null, map2.put(111, "222+222")); // 替换111
        assertEquals(null, map2.put(333, "333+333")); // 新增333
        assertEquals("222+222", map2.get(111));
        assertEquals("333+333", map2.get(333));
        assertEquals(2, map2.size());

        assertEquals(null, map3.put(111, 1111)); // 新增111
        assertEquals(1111, map3.get(111).intValue());
        assertEquals(1, map3.size());
    }

    @Test
    public void remove() {
        map1.remove(111);
        assertTrue(!map1.containsKey(111));
        assertEquals(2, map1.size());

        map2.remove(111);
        assertTrue(!map2.containsKey(111));
        assertEquals(0, map2.size());

        map3.remove(111);
        assertTrue(!map3.containsKey(111));
        assertEquals(0, map3.size());
    }

    @Test
    public void keys() {
        int[] keys = map1.keys();

        Arrays.sort(keys);
        assertEquals(3, keys.length);
        assertEquals(111, keys[0]);
        assertEquals(222, keys[1]);
        assertEquals(333, keys[2]);

        keys = map2.keys();

        Arrays.sort(keys);
        assertEquals(1, keys.length);
        assertEquals(111, keys[0]);

        keys = map3.keys();

        Arrays.sort(keys);
        assertEquals(0, keys.length);

        for (int i = 0; i < 1000; i++) {
            map3.put(i, new Integer(i));
        }

        keys = map3.keys();

        Arrays.sort(keys);
        assertEquals(1000, keys.length);

        for (int i = 0; i < 1000; i++) {
            assertEquals(i, keys[i]);
        }
    }

    /**
     * 测试当hash表中的项数超过阈值时的表现.
     */
    @Test
    public void resize() {
        // 取得初始容量和阈值.
        int capacity = map3.getCapacity();
        int threshold = map3.getThreshold();

        // 预计扩容三次.
        int max = threshold * 4 + 1;

        /**
         * 放入足够多的项到hash表中, 确保项数超过阈值.
         */
        for (int i = 0; i < max; i++) {
            map3.put(i, new Integer(i));

            if (map3.size() > threshold) { // 扩容!
                threshold *= 2;
                capacity *= 2;
            }

            assertEquals(capacity, map3.getCapacity());
            assertEquals(threshold, map3.getThreshold());
        }

        /** 读出所有项, 并排序测试正确性. */
        List<String> list;

        assertNotNull(list = parseToString(map3));
        assertEquals(max, list.size());

        List<String> expectedList = createArrayList(max);

        for (int i = 0; i < max; i++) {
            expectedList.add(i + "=" + i);
        }

        Collections.sort(expectedList);

        assertEquals(expectedList, list);
    }

    /**
     * 将map.toString()的结果重新排序后输出.
     */
    private List<String> parseToString(IntHashMap<?> map) {
        List<String> list = createArrayList();
        String str = map.toString();

        try {
            str = str.substring(1, str.length() - 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(str, ", ");

        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }

        Collections.sort(list);
        return list;
    }
}
