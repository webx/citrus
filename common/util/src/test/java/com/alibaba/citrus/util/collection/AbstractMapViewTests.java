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
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * 测试<code>Map.entrySet()</code>, <code>Map.keySet()</code>,
 * <code>Map.values()</code> 返回的collection view对象.
 * 
 * @author Michael Zhou
 */
public abstract class AbstractMapViewTests extends AbstractTests {
    private Map<Object, Object> map1;
    private Map<Object, Object> map2;
    private Map<Object, Object> map3;
    private Collection<?> view1;
    private Collection<?> view2;
    private Collection<?> view3;

    @Before
    public void init() {
        // map1测试一般情况.
        map1 = createMap();
        map1.put("aaa", "111");
        map1.put("bbb", "222");
        map1.put("ccc", "333");

        view1 = getView(map1);

        // map2测试key和value为null的情况.
        map2 = createMap();
        map2.put(null, "111");
        map2.put("aaa", null);

        view2 = getView(map2);

        // map3为空.
        map3 = createMap();
        view3 = getView(map3);
    }

    private Collection<?> newCollection() {
        return createHashSet();
    }

    private Object newItem(Object key, Object value) {
        return createItem(key, value);
    }

    @Test
    public void add() {
        assertAdd(view1);
        assertAdd(view2);
        assertAdd(view3);
    }

    @SuppressWarnings("unchecked")
    private void assertAdd(Collection<?> collection) {
        try {
            ((Collection<Object>) collection).add(newItem("key", "value"));
            fail("should throw an UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void addAll() {
        assertAddAll(view1);
        assertAddAll(view2);
        assertAddAll(view3);
    }

    @SuppressWarnings("unchecked")
    private void assertAddAll(Collection<?> collection) {
        Collection<Object> items = (Collection<Object>) newCollection();

        ((Collection<Object>) collection).addAll(items); // 加入空collection

        try {
            items.add(newItem("key", "value"));
            ((Collection<Object>) collection).addAll(items); // 加入非空collection
            fail("should throw an UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
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
    public void contains() {
        assertTrue(view1.contains(newItem("aaa", "111")));
        assertTrue(view1.contains(newItem("bbb", "222")));
        assertTrue(view1.contains(newItem("ccc", "333")));

        assertTrue(view2.contains(newItem("aaa", null)));
        assertTrue(view2.contains(newItem(null, "111")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void containsAll() {
        Collection<Object> items;

        items = (Collection<Object>) newCollection();
        items.add(newItem("aaa", "111"));
        items.add(newItem("bbb", "222"));
        items.add(newItem("ccc", "333"));
        assertTrue(view1.containsAll(items));

        items = (Collection<Object>) newCollection();
        items.add(newItem("aaa", null));
        items.add(newItem(null, "111"));
        assertTrue(view2.containsAll(items));

        items = (Collection<Object>) newCollection();
        assertTrue(view1.containsAll(items));
        assertTrue(view2.containsAll(items));
        assertTrue(view3.containsAll(items));
    }

    /**
     * 测试Object.equals()方法. 但并非所有的collection对象都定义了此方法.
     * 如果当前测试的collection对象不支持比较操作, 则 createCollectionToCompareWith()应该返回null.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void equals_() {
        Collection<Object> items;

        // 这个方法调用返回一个collection对象, 可以直接和当前的collection相比较.
        // 如果返回null, 表示当前collection不支持比较操作, 所以跳过此项测试.
        if (createCollectionToCompareWith() == null) {
            return;
        }

        items = (Collection<Object>) createCollectionToCompareWith();
        items.add(newItem("aaa", "111"));
        items.add(newItem("bbb", "222"));
        items.add(newItem("ccc", "333"));
        assertTrue(view1.equals(items));

        items = (Collection<Object>) createCollectionToCompareWith();
        items.add(newItem("aaa", null));
        items.add(newItem(null, "111"));
        assertTrue(view2.equals(items));

        items = (Collection<Object>) createCollectionToCompareWith();
        assertFalse(view1.equals(items));
        assertFalse(view2.equals(items));
        assertTrue(view3.equals(items));
    }

    /**
     * 测试Object.hashCode()方法. 该方法总是和Object.equals()相关的(相等的对象有 相同的hashCode),
     * 因此,类似equals_(), hashCode_通过判断
     * createCollectionToCompareWith()是否返回null来决定是否 做这项测试.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void hashCode_() {
        Collection<Object> items;

        // 这个方法调用返回一个collection对象, 可以直接和当前的collection相比较.
        // 如果返回null, 表示当前collection不支持比较操作, 所以不测试此项.
        if (createCollectionToCompareWith() == null) {
            return;
        }

        items = (Collection<Object>) createCollectionToCompareWith();
        items.add(newItem("aaa", "111"));
        items.add(newItem("bbb", "222"));
        items.add(newItem("ccc", "333"));
        assertEquals(items.hashCode(), view1.hashCode());

        items = (Collection<Object>) createCollectionToCompareWith();
        items.add(newItem("aaa", null));
        items.add(newItem(null, "111"));
        assertEquals(items.hashCode(), view2.hashCode());

        items = (Collection<Object>) createCollectionToCompareWith();
        assertFalse(items.hashCode() == view1.hashCode());
        assertFalse(items.hashCode() == view2.hashCode());
        assertEquals(items.hashCode(), view3.hashCode());
    }

    @Test
    public void isEmpty() {
        assertFalse(view1.isEmpty());
        assertFalse(view2.isEmpty());
        assertTrue(view3.isEmpty());
    }

    @Test
    public void iterator() {
        int count;

        count = 0;

        for (Iterator<?> i = view1.iterator(); i.hasNext(); count++) {
            Object entry = i.next();

            assertTrue(isEqual(newItem("aaa", "111"), entry) || isEqual(newItem("bbb", "222"), entry)
                    || isEqual(newItem("ccc", "333"), entry));
        }

        assertEquals(3, count);

        count = 0;

        for (Iterator<?> i = view2.iterator(); i.hasNext(); count++) {
            Object entry = i.next();

            assertTrue(isEqual(newItem("aaa", null), entry) || isEqual(newItem(null, "111"), entry));
        }

        assertEquals(2, count);

        for (Object name : view3) {
            fail("should not go here: " + name);
        }
    }

    @Test
    public void iteratorRemove() {
        int count = view1.size() - 1;

        for (Iterator<?> i = view1.iterator(); i.hasNext(); count--) {
            i.next();
            i.remove();
            assertEquals(count, view1.size());
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
    @SuppressWarnings("unchecked")
    public void removeAll() {
        Collection<Object> items;

        items = (Collection<Object>) newCollection();
        items.add(newItem("aaa", "111"));
        items.add(newItem("bbb", "222"));
        assertTrue(view1.removeAll(items));
        assertTrue(view1.contains(newItem("ccc", "333")));
        assertEquals(1, view1.size());

        items = (Collection<Object>) newCollection();
        items.add(newItem("aaa", null));
        items.add(newItem(null, "111"));
        assertTrue(view2.removeAll(items));
        assertEquals(0, view2.size());

        items = (Collection<Object>) newCollection();
        assertFalse(view3.removeAll(items));
        assertEquals(0, view3.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void retainAll() {
        Collection<Object> items;

        items = (Collection<Object>) newCollection();
        items.add(newItem("aaa", "111"));
        items.add(newItem("bbb", "222"));
        assertTrue(view1.retainAll(items));
        assertTrue(view1.contains(newItem("aaa", "111")));
        assertTrue(view1.contains(newItem("bbb", "222")));
        assertFalse(view1.contains(newItem("ccc", "333")));
        assertEquals(2, view1.size());

        items = (Collection<Object>) newCollection();
        items.add(newItem("aaa", null));
        assertTrue(view2.retainAll(items));
        assertTrue(view2.contains(newItem("aaa", null)));
        assertFalse(view2.contains(newItem(null, "111")));
        assertEquals(1, view2.size());

        items = (Collection<Object>) newCollection();
        assertFalse(view3.retainAll(items));
        assertEquals(0, view3.size());
    }

    @Test
    public void size() {
        assertEquals(3, view1.size());
        assertEquals(2, view2.size());
        assertEquals(0, view3.size());
    }

    @Test
    public void toArray() {
        Object[] array;

        // 不带参数的toArray().
        array = view1.toArray();
        view1.removeAll(Arrays.asList(array));
        assertEquals(0, view1.size());

        // 带参数的toArray(Object[]).
        array = new Object[2];
        view2.toArray(array);
        view2.removeAll(Arrays.asList(array));
        assertEquals(0, view2.size());
    }

    @Test
    public void failFast() {
        Iterator<?> i;

        // 修改map以后, 试图i.next()导致异常.
        i = view1.iterator();
        map1.put("aaa+aaa", "111+111");

        try {
            i.next();
            fail("should throw a ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
        }

        // 修改map以后, 试图i.remove()导致异常.
        i = view2.iterator();
        i.next();
        map2.put("aaa+aaa", "111+111");

        try {
            i.remove();
            fail("should throw a ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
        }
    }

    protected abstract Map<Object, Object> createMap();

    /**
     * 从<code>Map</code>中取得view. 可能是<code>Map.entrySet()</code>,
     * <code>Map.keySet()</code> 和<code>Map.values()</code>等方法返回的结果.
     * 
     * @param map 被测试的view所属的<code>Map</code>
     * @return view
     */
    protected abstract Collection<?> getView(Map<Object, Object> map);

    /**
     * 创建一个可以和当前测试的view相比较的对象. 对于<code>Map.values()</code>返回的
     * <code>Collection</code>对象, 没有定义<code>equals</code> 和
     * <code>hashCode</code>方法, 所以不可比较. 这种情况返回<code>null</code>即可.
     * 
     * @return <code>Collection</code>对象
     */
    protected abstract Collection<?> createCollectionToCompareWith();

    /**
     * 创建一个和View中存放的对象可比较的对象.
     * 
     * @param key key
     * @param value value
     * @return 对象
     */
    protected abstract Object createItem(Object key, Object value);
}
