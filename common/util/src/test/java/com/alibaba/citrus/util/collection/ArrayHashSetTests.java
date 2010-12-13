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
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

/**
 * 测试<code>ArrayHashSet</code>.
 * 
 * @author Michael Zhou
 */
public class ArrayHashSetTests extends AbstractTests {
    private ArrayHashSet<String> set1;
    private ArrayHashSet<String> set2;
    private ArrayHashSet<String> set3;

    @Before
    public void init() {
        // set1测试一般情况.
        set1 = createArrayHashSet("aaa", "bbb", "ccc");

        // set2测试key和value为null的情况.
        set2 = createArrayHashSet(null, "aaa");

        // set3为空.
        set3 = createArrayHashSet();
    }

    @Test
    public void add() {
        set1.add("key");
        assertTrue(set1.contains("key"));
        assertEquals(4, set1.size());

        set2.add("key");
        assertTrue(set2.contains("key"));
        assertEquals(3, set2.size());

        set3.add("key");
        assertTrue(set3.contains("key"));
        assertEquals(1, set3.size());
    }

    @Test
    public void addAll() {
        Collection<String> items = createArrayList("key");

        set1.addAll(items);
        assertTrue(set1.contains("key"));
        assertEquals(4, set1.size());

        set2.addAll(items);
        assertTrue(set2.contains("key"));
        assertEquals(3, set2.size());

        set3.addAll(items);
        assertTrue(set3.contains("key"));
        assertEquals(1, set3.size());
    }

    @Test
    public void clear() {
        set1.clear();
        assertEquals(0, set1.size());
        assertTrue(set1.isEmpty());

        set2.clear();
        assertEquals(0, set2.size());
        assertTrue(set2.isEmpty());

        set3.clear();
        assertEquals(0, set3.size());
        assertTrue(set3.isEmpty());
    }

    @Test
    public void contains() {
        assertTrue(set1.contains("aaa"));
        assertTrue(set1.contains("bbb"));
        assertTrue(set1.contains("ccc"));

        assertTrue(set2.contains("aaa"));
        assertTrue(set2.contains(null));
    }

    @Test
    public void containsAll() {
        Collection<String> items;

        items = createArrayList("aaa", "bbb", "ccc");
        assertTrue(set1.containsAll(items));

        items = createArrayList("aaa", null);
        assertTrue(set2.containsAll(items));

        items = createArrayList();
        assertTrue(set1.containsAll(items));
        assertTrue(set2.containsAll(items));
        assertTrue(set3.containsAll(items));
    }

    @Test
    public void equals_() {
        Collection<String> items;

        items = createHashSet("aaa", "bbb", "ccc");
        assertTrue(set1.equals(items));

        items = createHashSet("aaa", null);
        assertTrue(set2.equals(items));

        items = createHashSet();
        assertFalse(set1.equals(items));
        assertFalse(set2.equals(items));
        assertTrue(set3.equals(items));
    }

    @Test
    public void hashCode_() {
        Collection<String> items;

        items = createHashSet("aaa", "bbb", "ccc");
        assertEquals(items.hashCode(), set1.hashCode());

        items = createHashSet("aaa", null);
        assertEquals(items.hashCode(), set2.hashCode());

        items = createHashSet();
        assertFalse(items.hashCode() == set1.hashCode());
        assertFalse(items.hashCode() == set2.hashCode());
        assertEquals(items.hashCode(), set3.hashCode());
    }

    @Test
    public void isEmpty() {
        assertFalse(set1.isEmpty());
        assertFalse(set2.isEmpty());
        assertTrue(set3.isEmpty());
    }

    @Test
    public void iterator() {
        Iterator<String> i = set1.iterator();

        assertEquals("aaa", i.next());
        assertEquals("bbb", i.next());
        assertEquals("ccc", i.next());

        try {
            i.next();
            fail("should throw a NoSuchElementException");
        } catch (NoSuchElementException e) {
        }

        i = set2.iterator();

        assertEquals(null, i.next());
        assertEquals("aaa", i.next());

        try {
            i.next();
            fail("should throw a NoSuchElementException");
        } catch (NoSuchElementException e) {
        }

        i = set3.iterator();

        try {
            i.next();
            fail("should throw a NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

    @Test
    public void iteratorRemove() {
        int count = set1.size() - 1;

        for (Iterator<String> i = set1.iterator(); i.hasNext(); count--) {
            i.next();
            i.remove();
            assertEquals(count, set1.size());
        }
    }

    @Test
    public void remove() {
        set1.remove("aaa");
        assertFalse(set1.contains("aaa"));
        assertEquals(2, set1.size());

        set2.remove("aaa");
        assertFalse(set2.contains("aaa"));
        set2.remove(null);
        assertFalse(set2.contains(null));
        assertEquals(0, set2.size());

        set3.remove("not exists");
        assertFalse(set3.contains("not exists"));
        assertEquals(0, set3.size());
    }

    @Test
    public void removeAll() {
        Collection<String> items;

        items = createArrayList("aaa", "bbb");
        assertTrue(set1.removeAll(items));
        assertTrue(set1.contains("ccc"));
        assertEquals(1, set1.size());

        items = createArrayList("aaa", null);
        assertTrue(set2.removeAll(items));
        assertEquals(0, set2.size());

        items = createArrayList();
        assertFalse(set3.removeAll(items));
        assertEquals(0, set3.size());
    }

    @Test
    public void retainAll() {
        Collection<String> items;

        items = createArrayList("aaa", "bbb");
        assertTrue(set1.retainAll(items));
        assertTrue(set1.contains("aaa"));
        assertTrue(set1.contains("bbb"));
        assertFalse(set1.contains("ccc"));
        assertEquals(2, set1.size());

        items = createArrayList("aaa");
        assertTrue(set2.retainAll(items));
        assertTrue(set2.contains("aaa"));
        assertFalse(set2.contains(null));
        assertEquals(1, set2.size());

        items = createArrayList();
        assertFalse(set3.retainAll(items));
        assertEquals(0, set3.size());
    }

    @Test
    public void size() {
        assertEquals(3, set1.size());
        assertEquals(2, set2.size());
        assertEquals(0, set3.size());
    }

    @Test
    public void toArray() {
        Object[] array;

        // 不带参数的toArray().
        array = set1.toArray();
        set1.removeAll(Arrays.asList(array));
        assertEquals(0, set1.size());

        // 带参数的toArray(Object[]).
        array = new Object[2];
        set2.toArray(array);
        set2.removeAll(Arrays.asList(array));
        assertEquals(0, set2.size());
    }

    @Test
    public void failFast() {
        Iterator<String> i;

        // 修改set以后, 试图i.next()导致异常.
        i = set1.iterator();
        set1.add("aaa+aaa");

        try {
            i.next();
            fail("should throw a ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
        }

        // 修改set以后, 试图i.remove()导致异常.
        i = set2.iterator();
        i.next();
        set2.add("aaa+aaa");

        try {
            i.remove();
            fail("should throw a ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
        }
    }
}
