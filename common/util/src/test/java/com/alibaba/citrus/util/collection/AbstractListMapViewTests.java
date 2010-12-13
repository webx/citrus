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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

/**
 * 测试<code>ListMap.entryList()</code>, <code>ListMap.keyList()</code>,
 * <code>ListMap.valueList()</code> 返回的list view对象.
 * 
 * @author Michael Zhou
 */
public abstract class AbstractListMapViewTests extends AbstractTests {
    private ListMap<Object, Object> map1;
    private ListMap<Object, Object> map2;
    private ListMap<Object, Object> map3;
    private List<?> view1;
    private List<?> view2;
    private List<?> view3;

    @Before
    public void init() {
        // map1测试一般情况.
        map1 = createListMap();
        map1.put("aaa", "111");
        map1.put("bbb", "222");
        map1.put("ccc", "333");
        view1 = getView(map1);

        // map2测试key和value为null的情况.
        map2 = createListMap();
        map2.put(null, "111");
        map2.put("aaa", null);
        view2 = getView(map2);

        // map3为空.
        map3 = createListMap();
        view3 = getView(map3);
    }

    private Object newItem(Object key, Object value) {
        return createItem(key, value);
    }

    @Test
    public void size() {
        assertEquals(3, view1.size());
        assertEquals(2, view2.size());
        assertEquals(0, view3.size());
    }

    @Test
    public void isEmpty() {
        assertFalse(view1.isEmpty());
        assertFalse(view2.isEmpty());
        assertTrue(view3.isEmpty());
    }

    @Test
    public void clear() {
        view1.clear();
        assertEquals(0, map1.size());
        assertTrue(map1.isEmpty());
        assertTrue(view1.isEmpty());

        view2.clear();
        assertEquals(0, map2.size());
        assertTrue(map2.isEmpty());
        assertTrue(view2.isEmpty());

        view3.clear();
        assertEquals(0, map3.size());
        assertTrue(map3.isEmpty());
        assertTrue(view3.isEmpty());
    }

    @Test
    public void removeByIndex() {
        view1.remove(0);
        assertFalse(map1.containsKey("aaa"));
        assertFalse(view1.contains(newItem("aaa", "111")));
        assertEquals(2, map1.size());
        assertEquals(2, view1.size());

        view2.remove(1);
        assertFalse(map2.containsKey("aaa"));
        assertFalse(view2.contains(newItem("aaa", null)));
        view2.remove(0);
        assertFalse(map2.containsKey(null));
        assertFalse(view2.contains(newItem(null, "111")));
        assertEquals(0, map2.size());
        assertEquals(0, view2.size());

        try {
            view3.remove(0);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            assertFalse(map3.containsKey("not exists"));
            assertFalse(view3.contains(newItem("not exists", null)));
            assertEquals(0, map3.size());
            assertEquals(0, view3.size());
        }
    }

    @Test
    public void remove() {
        view1.remove(newItem("aaa", "111"));
        assertFalse(map1.containsKey("aaa"));
        assertFalse(view1.contains(newItem("aaa", "111")));
        assertEquals(2, map1.size());
        assertEquals(2, view1.size());

        view2.remove(newItem("aaa", null));
        assertFalse(map2.containsKey("aaa"));
        assertFalse(view2.contains(newItem("aaa", null)));
        view2.remove(newItem(null, "111"));
        assertFalse(map2.containsKey(null));
        assertFalse(view2.contains(newItem(null, "111")));
        assertEquals(0, map2.size());
        assertEquals(0, view2.size());

        view3.remove(newItem("not exists", null));
        assertFalse(map3.containsKey("not exists"));
        assertFalse(view3.contains(newItem("not exists", null)));
        assertEquals(0, map3.size());
        assertEquals(0, view3.size());
    }

    @Test
    public void indexOf() {
        assertEquals(0, view1.indexOf(newItem("aaa", "111")));
        assertEquals(1, view1.indexOf(newItem("bbb", "222")));
        assertEquals(2, view1.indexOf(newItem("ccc", "333")));
        assertEquals(-1, view1.indexOf(newItem(null, null)));

        assertEquals(0, view2.indexOf(newItem(null, "111")));
        assertEquals(1, view2.indexOf(newItem("aaa", null)));
        assertEquals(-1, view2.indexOf(newItem("bbb", "222")));

        assertEquals(-1, view3.indexOf(newItem("aaa", "111")));
    }

    @Test
    public void lastIndexOf() {
        assertEquals(0, view1.lastIndexOf(newItem("aaa", "111")));
        assertEquals(1, view1.lastIndexOf(newItem("bbb", "222")));
        assertEquals(2, view1.lastIndexOf(newItem("ccc", "333")));
        assertEquals(-1, view1.lastIndexOf(newItem(null, null)));

        assertEquals(0, view2.lastIndexOf(newItem(null, "111")));
        assertEquals(1, view2.lastIndexOf(newItem("aaa", null)));
        assertEquals(-1, view2.lastIndexOf(newItem("bbb", "222")));

        assertEquals(-1, view3.lastIndexOf(newItem("aaa", "111")));
    }

    @Test
    public void contains() {
        assertTrue(view1.contains(newItem("aaa", "111")));
        assertTrue(view1.contains(newItem("bbb", "222")));
        assertTrue(view1.contains(newItem("ccc", "333")));

        assertTrue(view2.contains(newItem("aaa", null)));
        assertTrue(view2.contains(newItem(null, "111")));
    }

    @Test
    public void containsAll() {
        List<?> items;

        items = createArrayList(newItem("aaa", "111"), newItem("bbb", "222"), newItem("ccc", "333"));
        assertTrue(view1.containsAll(items));

        items = createArrayList(newItem("aaa", null), newItem(null, "111"));
        assertTrue(view2.containsAll(items));

        items = createArrayList();
        assertTrue(view1.containsAll(items));
        assertTrue(view2.containsAll(items));
        assertTrue(view3.containsAll(items));
    }

    @Test
    public void get() {
        assertEquals(newItem("aaa", "111"), view1.get(0));
        assertEquals(newItem("bbb", "222"), view1.get(1));
        assertEquals(newItem("ccc", "333"), view1.get(2));

        assertEquals(newItem(null, "111"), view2.get(0));
        assertEquals(newItem("aaa", null), view2.get(1));

        try {
            view3.get(0);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void iterator() {
        Iterator<?> i = view1.iterator();

        assertTrue(i.hasNext());
        assertEquals(newItem("aaa", "111"), i.next());
        assertTrue(i.hasNext());
        assertEquals(newItem("bbb", "222"), i.next());
        assertTrue(i.hasNext());
        assertEquals(newItem("ccc", "333"), i.next());
        assertFalse(i.hasNext());

        try {
            i.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
        }

        i = view2.iterator();
        i.next();
        i.remove();
        assertEquals(1, map2.size());
        i.next();
        i.remove();
        assertEquals(0, map2.size());

        try {
            i.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listIterator() {
        ListIterator<Object> i = (ListIterator<Object>) view1.listIterator();

        assertTrue(i.hasNext());
        assertEquals(newItem("aaa", "111"), i.next());
        assertTrue(i.hasNext());
        assertEquals(newItem("bbb", "222"), i.next());
        assertTrue(i.hasNext());
        assertEquals(newItem("ccc", "333"), i.next());
        assertFalse(i.hasNext());

        try {
            i.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
        }

        assertTrue(i.hasPrevious());
        assertEquals(newItem("ccc", "333"), i.previous());
        assertTrue(i.hasPrevious());
        assertEquals(newItem("bbb", "222"), i.previous());
        assertTrue(i.hasPrevious());
        assertEquals(newItem("aaa", "111"), i.previous());
        assertFalse(i.hasPrevious());

        try {
            i.previous();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
        }

        i.remove();
        assertEquals(2, map1.size());
        i.next();
        i.remove();
        assertEquals(1, map1.size());
        i.next();
        i.remove();
        assertEquals(0, map1.size());

        try {
            i.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
        }

        i = (ListIterator<Object>) view2.listIterator();
        i.next();

        // 此操作只有valueList支持.
        try {
            i.set(newItem("hello", "world"));
            assertEquals(newItem("hello", "world"), map2.get(0));
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void failFast() {
        Iterator<Object> i;
        ListIterator<Object> j;

        // 修改map以后, 试图i.next()导致异常.
        i = (Iterator<Object>) view1.iterator();
        map1.put("aaa+aaa", "111+111");

        try {
            i.next();
            fail("should throw a ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
        }

        // 修改map以后, 试图i.remove()导致异常.
        j = (ListIterator<Object>) view2.listIterator();
        j.next();
        map2.put("aaa+aaa", "111+111");

        try {
            j.remove();
            fail("should throw a ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
        }
    }

    protected abstract ListMap<Object, Object> createListMap();

    /**
     * 从<code>ListMap</code>中取得view. 可能是<code>ListMap.entryList()</code>,
     * <code>ListMap.keyList()</code> 和<code>ListMap.valueList()</code>
     * 等方法返回的结果.
     * 
     * @param map 被测试的view所属的<code>ListMap</code>
     * @return view
     */
    protected abstract List<?> getView(ListMap<Object, Object> map);

    /**
     * 创建一个和View中存放的对象可比较的对象.
     * 
     * @param key key
     * @param value value
     * @return 对象
     */
    protected abstract Object createItem(Object key, Object value);
}
