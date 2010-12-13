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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * <p>
 * 一个Hash表的实现, 实现了<code>ListMap</code>和<code>Map</code>接口.
 * </p>
 * <p>
 * 这个hash表的实现具有以下特性:
 * </p>
 * <ul>
 * <li>在内部以数组的方式保存所有entry, 可以顺序访问</li>
 * <li>和<code>DefaultHashMap</code>一样, 没有进行任何<code>synchronized</code>操作</li>
 * </ul>
 * 
 * @author Michael Zhou
 * @see DefaultHashMap
 * @see ListMap
 */
public class ArrayHashMap<K, V> extends DefaultHashMap<K, V> implements ListMap<K, V> {
    private static final long serialVersionUID = 3258411729271927857L;

    // ==========================================================================
    // 成员变量 
    // ==========================================================================

    /** 记录entry的顺序的数组. */
    protected transient DefaultHashMap.Entry<K, V>[] order;

    /** Key的列表视图. */
    private transient List<K> keyList;

    /** Value的列表视图. */
    private transient List<V> valueList;

    /** Entry的列表视图. */
    private transient List<Map.Entry<K, V>> entryList;

    // ==========================================================================
    // 构造函数 
    // ==========================================================================

    /**
     * 创建一个空的hash表. 使用指定的默认的初始容量(16)和默认的负载系数(0.75).
     */
    public ArrayHashMap() {
        super();
    }

    /**
     * 创建一个空的hash表. 使用指定的初始阈值和默认的负载系数(0.75).
     * 
     * @param initialCapacity 初始容量.
     */
    public ArrayHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 创建一个空的hash表. 使用指定的初始容量和负载系数.
     * 
     * @param initialCapacity 初始容量
     * @param loadFactor 负载系数.
     */
    public ArrayHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * 复制指定<code>Map</code>内容相同的<code>HashMap</code>. 使用默认的负载系数(0.75).
     * 
     * @param map 要复制的<code>Map</code>
     */
    public ArrayHashMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    // ==========================================================================
    // 实现Map和ListMap接口的方法 
    // ==========================================================================

    /**
     * 如果hash表中包含一个或多个key对应指定的value, 则返回true.
     * 
     * @param value 指定value, 检查它的存在与否.
     * @return 如果hash表中包含一个或多个key对应指定的value, 则返回<code>true</code>.
     */
    @Override
    public boolean containsValue(Object value) {
        // 覆盖此方法是出于性能的考虑.  利用数组查找更有效.
        for (int i = 0; i < size; i++) {
            Entry entry = (Entry) order[i];

            if (eq(value, entry.getValue())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 清除hash表中的所有entry.
     */
    @Override
    public void clear() {
        super.clear();
        Arrays.fill(order, null);
    }

    /**
     * 返回指定index处的value. 如果index超出范围, 则掷出<code>IndexOutOfBoundsException</code>.
     * 
     * @param index 要返回的value的索引值
     * @return 指定index处的value对象
     */
    public V get(int index) {
        checkRange(index);
        return order[index].getValue();
    }

    /**
     * 返回指定index处的key. 如果index超出范围, 则掷出<code>IndexOutOfBoundsException</code>.
     * 
     * @param index 要返回的key的索引值
     * @return 指定index处的key对象
     */
    public K getKey(int index) {
        checkRange(index);
        return order[index].getKey();
    }

    /**
     * 删除指定index处的项. 如果index超出范围, 则掷出<code>IndexOutOfBoundsException</code>.
     * 
     * @param index 要删除的项的索引值
     * @return 被删除的<code>Map.Entry</code>项
     */
    public Map.Entry<K, V> removeEntry(int index) {
        checkRange(index);
        return removeEntryForKey(order[index].getKey());
    }

    /**
     * 返回所有key的<code>List</code>.
     * 
     * @return 所有key的<code>List</code>.
     */
    public List<K> keyList() {
        return keyList != null ? keyList : (keyList = new KeyList());
    }

    /**
     * 返回所有value的<code>List</code>.
     * 
     * @return 所有value的<code>List</code>.
     */
    public List<V> valueList() {
        return valueList != null ? valueList : (valueList = new ValueList());
    }

    /**
     * 返回所有entry的<code>List</code>.
     * 
     * @return 所有entry的<code>List</code>.
     */
    public List<Map.Entry<K, V>> entryList() {
        return entryList != null ? entryList : (entryList = new EntryList());
    }

    // ==========================================================================
    // 内部类 
    // ==========================================================================

    /**
     * <code>Map.Entry</code>的实现.
     */
    protected class Entry extends DefaultHashMap.Entry<K, V> {
        /** Entry在列表中的索引值. */
        protected int index;

        /**
         * 创建一个新的entry.
         * 
         * @param h key的hash值
         * @param k entry的key
         * @param v entry的value
         * @param n 链表中的下一个entry
         */
        protected Entry(int h, K k, V v, DefaultHashMap.Entry<K, V> n) {
            super(h, k, v, n);
        }

        /**
         * 当entry将被删除时, 更新后续的entry的索引值.
         */
        @Override
        protected void onRemove() {
            int numMoved = size - index;

            if (numMoved > 0) {
                System.arraycopy(order, index + 1, order, index, numMoved);
            }

            order[size] = null;

            for (int i = index; i < size; i++) {
                ((Entry) order[i]).index--;
            }
        }
    }

    /**
     * 遍历器.
     */
    private abstract class ArrayHashIterator<E> implements ListIterator<E> {
        /** 最近返回的entry. */
        private Entry lastReturned;

        /** 当前位置. */
        private int cursor;

        /** 创建iterator时的修改计数. */
        private int expectedModCount;

        /**
         * 创建一个list iterator.
         * 
         * @param index 起始点
         */
        protected ArrayHashIterator(int index) {
            if (index < 0 || index > size()) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }

            cursor = index;
            expectedModCount = modCount;
        }

        /**
         * 将指定对象插入到列表中. (不支持此操作)
         * 
         * @param o 要插入的对象
         */
        public void add(E o) {
            throw new UnsupportedOperationException();
        }

        /**
         * 将指定对象替换到列表中. (除了<code>valueList</code>以外, 不支持此操作)
         * 
         * @param o 要替换的对象
         */
        public void set(E o) {
            throw new UnsupportedOperationException();
        }

        /**
         * 返回遍历器中是否还有下一个entry.
         * 
         * @return 如果遍历器中还有下一个entry, 返回<code>true</code>
         */
        public boolean hasNext() {
            return cursor < size;
        }

        /**
         * 返回遍历器中是否还有前一个entry.
         * 
         * @return 如果遍历器中还有前一个entry, 返回<code>true</code>
         */
        public boolean hasPrevious() {
            return cursor > 0;
        }

        /**
         * 取得下一个index. 如果是最后一项, 则返回<code>size</code>.
         * 
         * @return 后一项的index
         */
        public int nextIndex() {
            return cursor;
        }

        /**
         * 取得前一个index. 如果是第一项, 则返回<code>-1</code>.
         * 
         * @return 前一项的index
         */
        public int previousIndex() {
            return cursor - 1;
        }

        /**
         * 删除一个当前entry. 执行前必须先执行<code>next()</code>或<code>previous()</code>方法.
         */
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }

            checkForComodification();

            removeEntryForKey(lastReturned.getKey());

            if (lastReturned.index < cursor) {
                cursor--;
            }

            lastReturned = null;
            expectedModCount = modCount;
        }

        /**
         * 取得下一个entry.
         * 
         * @return 下一个entry
         */
        protected Entry nextEntry() {
            checkForComodification();

            if (cursor >= size) {
                throw new NoSuchElementException();
            }

            lastReturned = (Entry) order[cursor++];

            return lastReturned;
        }

        /**
         * 取得前一个entry.
         * 
         * @return 前一个entry
         */
        protected Entry previousEntry() {
            checkForComodification();

            if (cursor <= 0) {
                throw new NoSuchElementException();
            }

            lastReturned = (Entry) order[--cursor];

            return lastReturned;
        }

        /**
         * 设置当前entry的值.
         * 
         * @param o 要设置的值
         */
        protected void setValue(V o) {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }

            checkForComodification();

            lastReturned.setValue(o);
        }

        /**
         * 检查是否同时被修改.
         */
        private void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * 取得hash表的key的遍历器.
     */
    private class KeyIterator extends ArrayHashIterator<K> {
        /**
         * 创建一个list iterator.
         * 
         * @param index 起始点
         */
        protected KeyIterator(int index) {
            super(index);
        }

        /**
         * 取得下一个key.
         * 
         * @return 下一个key
         */
        public K next() {
            return nextEntry().getKey();
        }

        /**
         * 取得前一个key.
         * 
         * @return 前一个key
         */
        public K previous() {
            return previousEntry().getKey();
        }
    }

    /**
     * 取得hash表的value的遍历器.
     */
    private class ValueIterator extends ArrayHashIterator<V> {
        /**
         * 创建一个list iterator.
         * 
         * @param index 起始点
         */
        protected ValueIterator(int index) {
            super(index);
        }

        /**
         * 将指定对象替换到列表中.
         * 
         * @param o 要替换的对象(value)
         */
        @Override
        public void set(V o) {
            setValue(o);
        }

        /**
         * 取得下一个value.
         * 
         * @return 下一个value
         */
        public V next() {
            return nextEntry().getValue();
        }

        /**
         * 取得前一个value.
         * 
         * @return 前一个value
         */
        public V previous() {
            return previousEntry().getValue();
        }
    }

    /**
     * 取得hash表的entry的遍历器.
     */
    private class EntryIterator extends ArrayHashIterator<Map.Entry<K, V>> {
        /**
         * 创建一个list iterator.
         * 
         * @param index 起始点
         */
        protected EntryIterator(int index) {
            super(index);
        }

        /**
         * 取得下一个entry.
         * 
         * @return 下一个entry
         */
        public Map.Entry<K, V> next() {
            return nextEntry();
        }

        /**
         * 取得前一个entry.
         * 
         * @return 前一个entry
         */
        public Map.Entry<K, V> previous() {
            return previousEntry();
        }
    }

    /**
     * 列表视图.
     */
    private abstract class ArrayHashList<E> extends AbstractList<E> {
        /**
         * 返回hash表中entry的个数.
         * 
         * @return hash表中的entry数.
         */
        @Override
        public int size() {
            return size;
        }

        /**
         * 判断是否为空的hash表.
         * 
         * @return 如果为空(<code>size() == 0</code>), 则返回<code>true</code>.
         */
        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         * 清除所有entry.
         */
        @Override
        public void clear() {
            ArrayHashMap.this.clear();
        }

        /**
         * 取得指定entry的索引. 同<code>indexOf</code>方法.
         * 
         * @param o 要查找的entry
         * @return 指定entry的索引
         */
        @Override
        public int lastIndexOf(Object o) {
            return indexOf(o);
        }
    }

    /**
     * entry的列表视图.
     */
    private class EntryList extends ArrayHashList<Map.Entry<K, V>> {
        /**
         * 判断entry列表中是否包含指定对象.
         * 
         * @param o 要查找的对象
         * @return 如果entry列表中是否包含指定对象, 则返回<code>true</code>
         */
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry<?, ?>)) {
                return false;
            }

            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            Entry candidate = (Entry) getEntry(entry.getKey());

            return eq(candidate, entry);
        }

        /**
         * 取得entry的遍历器.
         * 
         * @return entry的遍历器
         */
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return newEntryIterator();
        }

        /**
         * 删除指定的entry.
         * 
         * @param o 要删除的entry
         * @return 如果删除成功, 返回<code>true</code>
         */
        @Override
        public boolean remove(Object o) {
            return removeEntry(o) != null;
        }

        /**
         * 删除指定index处的项. 如果index超出范围, 则掷出<code>IndexOutOfBoundsException</code>.
         * 
         * @param index 要删除的项的索引值
         * @return 被删除的<code>Map.Entry</code>项
         */
        @Override
        public Map.Entry<K, V> remove(int index) {
            checkRange(index);
            return removeEntryForKey(order[index].getKey());
        }

        /**
         * 返回指定index处的entry. 如果index超出范围, 则掷出
         * <code>IndexOutOfBoundsException</code>.
         * 
         * @param index 要返回的entry的索引值
         * @return 指定index处的entry对象
         */
        @Override
        public Map.Entry<K, V> get(int index) {
            checkRange(index);
            return order[index];
        }

        /**
         * 取得指定entry的索引.
         * 
         * @param o 要查找的entry
         * @return 指定entry的索引
         */
        @Override
        public int indexOf(Object o) {
            if (o != null && o instanceof Map.Entry<?, ?>) {
                Entry entry = (Entry) getEntry(((Map.Entry<?, ?>) o).getKey());

                if (entry != null && entry.equals(o)) {
                    return entry.index;
                }
            }

            return -1;
        }

        /**
         * 取得list iterator, 并设置当前位置.
         * 
         * @param index 当前位置
         * @return list iterator
         */
        @Override
        public ListIterator<Map.Entry<K, V>> listIterator(int index) {
            return new EntryIterator(index);
        }
    }

    /**
     * key的列表视图.
     */
    private class KeyList extends ArrayHashList<K> {
        /**
         * 判断key列表中是否包含指定对象.
         * 
         * @param o 要查找的对象
         * @return 如果key列表中是否包含指定对象, 则返回<code>true</code>
         */
        @Override
        public boolean contains(Object o) {
            return ArrayHashMap.this.containsKey(o);
        }

        /**
         * 取得key的遍历器.
         * 
         * @return key的遍历器
         */
        @Override
        public Iterator<K> iterator() {
            return newKeyIterator();
        }

        /**
         * 删除指定的key.
         * 
         * @param o 要删除的key
         * @return 如果删除成功, 返回<code>true</code>
         */
        @Override
        public boolean remove(Object o) {
            Entry entry = (Entry) getEntry(o);

            if (entry == null) {
                return false;
            } else {
                removeEntry(entry);
                return true;
            }
        }

        /**
         * 删除指定index处的项. 如果index超出范围, 则掷出<code>IndexOutOfBoundsException</code>.
         * 
         * @param index 要删除的项的索引值
         * @return 被删除的<code>Map.Entry</code>项
         */
        @Override
        public K remove(int index) {
            checkRange(index);
            return removeEntryForKey(order[index].getKey()).getKey();
        }

        /**
         * 返回指定index处的key. 如果index超出范围, 则掷出
         * <code>IndexOutOfBoundsException</code>.
         * 
         * @param index 要返回的key的索引值
         * @return 指定index处的key对象
         */
        @Override
        public K get(int index) {
            checkRange(index);
            return order[index].getKey();
        }

        /**
         * 取得指定key的索引.
         * 
         * @param o 要查找的key
         * @return 指定key的索引
         */
        @Override
        public int indexOf(Object o) {
            Entry entry = (Entry) getEntry(o);

            if (entry != null) {
                return entry.index;
            }

            return -1;
        }

        /**
         * 取得list iterator, 并设置当前位置.
         * 
         * @param index 当前位置
         * @return list iterator
         */
        @Override
        public ListIterator<K> listIterator(int index) {
            return new KeyIterator(index);
        }
    }

    /**
     * value的列表视图.
     */
    private class ValueList extends ArrayHashList<V> {
        /**
         * 判断value列表中是否包含指定对象.
         * 
         * @param o 要查找的对象
         * @return 如果value列表中是否包含指定对象, 则返回<code>true</code>
         */
        @Override
        public boolean contains(Object o) {
            return ArrayHashMap.this.containsValue(o);
        }

        /**
         * 取得value的遍历器.
         * 
         * @return value的遍历器
         */
        @Override
        public Iterator<V> iterator() {
            return newValueIterator();
        }

        /**
         * 删除指定的value.
         * 
         * @param o 要删除的value
         * @return 如果删除成功, 返回<code>true</code>
         */
        @Override
        public boolean remove(Object o) {
            int index = indexOf(o);

            if (index != -1) {
                ArrayHashMap.this.removeEntry(index);
                return true;
            }

            return false;
        }

        /**
         * 删除指定index处的项. 如果index超出范围, 则掷出<code>IndexOutOfBoundsException</code>.
         * 
         * @param index 要删除的项的索引值
         * @return 被删除的<code>Map.Entry</code>项
         */
        @Override
        public V remove(int index) {
            checkRange(index);
            return removeEntryForKey(order[index].getKey()).getValue();
        }

        /**
         * 返回指定index处的value. 如果index超出范围, 则掷出
         * <code>IndexOutOfBoundsException</code>.
         * 
         * @param index 要返回的value的索引值
         * @return 指定index处的value对象
         */
        @Override
        public V get(int index) {
            checkRange(index);
            return order[index].getValue();
        }

        /**
         * 取得指定value的索引.
         * 
         * @param o 要查找的value
         * @return 指定value的索引
         */
        @Override
        public int indexOf(Object o) {
            for (int i = 0; i < size; i++) {
                if (eq(o, order[i].getValue())) {
                    return i;
                }
            }

            return -1;
        }

        /**
         * 取得list iterator, 并设置当前位置.
         * 
         * @param index 当前位置
         * @return list iterator
         */
        @Override
        public ListIterator<V> listIterator(int index) {
            return new ValueIterator(index);
        }
    }

    // ==========================================================================
    // 内部方法 
    // ==========================================================================

    /**
     * 初始化时hash表.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void onInit() {
        order = new DefaultHashMap.Entry[threshold];
    }

    /**
     * 此方法覆盖了父类的方法. 向表中增加一个entry, 同时将entry记录到order列表中.
     * 
     * @param key hash表的key
     * @param value hash表的value
     */
    @Override
    protected void addEntry(K key, V value) {
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        Entry entry = new Entry(hash, key, value, table[i]);

        table[i] = entry;
        entry.index = size;
        order[size++] = entry;
    }

    /**
     * 覆盖父类的方法, 用来创建key的遍历器.
     * 
     * @return hash表的key的遍历器
     */
    @Override
    protected Iterator<K> newKeyIterator() {
        return new KeyIterator(0);
    }

    /**
     * 覆盖父类的方法, 用来创建value的遍历器.
     * 
     * @return hash表的key的遍历器
     */
    @Override
    protected Iterator<V> newValueIterator() {
        return new ValueIterator(0);
    }

    /**
     * 覆盖父类的方法, 用来创建entry的遍历器.
     * 
     * @return hash表的key的遍历器
     */
    @Override
    protected Iterator<Map.Entry<K, V>> newEntryIterator() {
        return new EntryIterator(0);
    }

    /**
     * 对map进行扩容. 此方法在entry数超过阈值时被调用.
     * 
     * @param newCapacity 新的容量
     */
    @Override
    protected void resize(int newCapacity) {
        super.resize(newCapacity);

        if (threshold > order.length) {
            @SuppressWarnings("unchecked")
            DefaultHashMap.Entry<K, V>[] newOrder = new DefaultHashMap.Entry[threshold];

            System.arraycopy(order, 0, newOrder, 0, order.length);

            order = newOrder;
        }
    }

    /**
     * 基类在<code>resize</code>时会调用此方法把所有的项复制到新的数组中. 覆盖此方法是出于性能的考虑,
     * 因为利用数组遍历hash表比原来的实现方法更有效.
     * 
     * @param newTable 新表
     */
    @Override
    protected void transfer(DefaultHashMap.Entry<K, V>[] newTable) {
        int newCapacity = newTable.length;

        for (int i = 0; i < size; i++) {
            Entry entry = (Entry) order[i];
            int index = indexFor(entry.hash, newCapacity);

            entry.next = newTable[index];
            newTable[index] = entry;
        }
    }

    /**
     * 检查指定的索引值是否越界. 如果是, 则掷出运行时异常.
     * 
     * @param index 要检查的异常
     */
    private void checkRange(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }
}
