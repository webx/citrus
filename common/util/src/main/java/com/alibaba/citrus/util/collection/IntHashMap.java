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

import static com.alibaba.citrus.util.BasicConstant.*;

/**
 * 使用整数作为key的hash表。
 * 
 * @author Michael Zhou
 */
public class IntHashMap<T> {
    /** 默认的初始容量 - <code>2的整数次幂</code>. */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /** 最大容量 - <code>2的整数次幂</code>. */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /** 默认的负载系数 */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    // 私有变量
    private Entry<T>[] table;
    private int count;
    private int threshold;
    private float loadFactor;

    /**
     * 创建一个hash表，使用默认的初始容量<code>16</code>和默认的负载系数<code>0.75</code>。
     */
    public IntHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 创建一个hash表，使用指定的初始容量和默认的负载系数<code>0.75</code>。
     * 
     * @param initialCapacity hash表的初始容量
     * @throws IllegalArgumentException 如果初始容量小于或等于<code>0</code>
     */
    public IntHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 创建一个hash表，使用默认的指定的初始容量和指定的负载系数。
     * 
     * @param initialCapacity hash表的初始容量
     * @param loadFactor 负载系数
     * @throws IllegalArgumentException 如果初始容量小于或等于<code>0</code>，或负载系数不是正数
     */
    @SuppressWarnings("unchecked")
    public IntHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }

        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }

        if (loadFactor <= 0) {
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        }

        // 确保初始容量为2的整数次幂.
        int capacity = 1;

        while (capacity < initialCapacity) {
            capacity <<= 1;
        }

        this.loadFactor = loadFactor;
        table = new Entry[capacity];
        threshold = (int) (capacity * loadFactor);
    }

    /**
     * 取得当前hash表中元素的个数。
     * 
     * @return 元素个数
     */
    public int size() {
        return count;
    }

    /**
     * 测试hash表是否为空。
     * 
     * @return 如果为空，则返回<code>true</code>
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * 查看hash表中是否存在指定的key。
     * 
     * @param key 要搜索的key
     * @return 如果找到，则返回<code>true</code>
     */
    public boolean containsKey(int key) {
        Entry<T>[] tab = table;
        int hash = key;
        int index = (hash & 0x7FFFFFFF) % tab.length;

        for (Entry<T> e = tab[index]; e != null; e = e.next) {
            if (e.hash == hash) {
                return true;
            }
        }

        return false;
    }

    /**
     * 查看hash表中是否存在指定的值。
     * 
     * @param value 要搜索的值
     * @return 如果找到，则返回<code>true</code>
     */
    public boolean containsValue(Object value) {
        Entry<T>[] tab = table;

        boolean valueIsNull = value == null;

        for (int i = tab.length; i-- > 0;) {
            for (Entry<T> e = tab[i]; e != null; e = e.next) {
                if (valueIsNull ? e.value == null : value.equals(e.value)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 从hash表中取得和指定key对应的值。
     * 
     * @param key 要查找的key
     * @return key所对应的值，如果没找到，则返回<code>null</code>
     */
    public T get(int key) {
        Entry<T>[] tab = table;
        int hash = key;
        int index = (hash & 0x7FFFFFFF) % tab.length;

        for (Entry<T> e = tab[index]; e != null; e = e.next) {
            if (e.hash == hash) {
                return e.value;
            }
        }

        return null;
    }

    /**
     * 将key和指定对象相关联，并保存在hash表中。
     * 
     * @param key 对象的key
     * @param value 对象（值）
     * @return 如果指定key已经存在，则返回key所对应的原先的值
     */
    public T put(int key, T value) {
        // Makes sure the key is not already in the hashtable.
        Entry<T>[] tab = table;
        int hash = key;
        int index = (hash & 0x7FFFFFFF) % tab.length;

        for (Entry<T> e = tab[index]; e != null; e = e.next) {
            if (e.hash == hash) {
                T old = e.value;

                e.value = value;
                return old;
            }
        }

        if (count >= threshold) {
            rehash();

            tab = table;
            index = (hash & 0x7FFFFFFF) % tab.length;
        }

        // Creates the new entry.
        Entry<T> e = new Entry<T>(hash, key, value, tab[index]);

        tab[index] = e;
        count++;
        return null;
    }

    /**
     * 从hash表中删除一个值。
     * 
     * @param key 要删除的值所对应的key
     * @return 如果指定key已经存在，则返回key所对应的原先的值
     */
    public T remove(int key) {
        Entry<T>[] tab = table;
        int hash = key;
        int index = (hash & 0x7FFFFFFF) % tab.length;

        for (Entry<T> e = tab[index], prev = null; e != null; prev = e, e = e.next) {
            if (e.hash == hash) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }

                count--;

                T oldValue = e.value;

                e.value = null;
                return oldValue;
            }
        }

        return null;
    }

    /**
     * 清除hash表。
     */
    public void clear() {
        Entry<T>[] tab = table;

        for (int index = tab.length; --index >= 0;) {
            tab[index] = null;
        }

        count = 0;
    }

    public int[] keys() {
        if (count == 0) {
            return EMPTY_INT_ARRAY;
        }

        int[] keys = new int[count];
        int index = 0;

        for (Entry<T> element : table) {
            Entry<T> entry = element;

            while (entry != null) {
                keys[index++] = entry.key;
                entry = entry.next;
            }
        }

        return keys;
    }

    /**
     * 取得字符串表示。
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append('{');

        int[] keys = keys();

        for (int i = 0; i < keys.length; i++) {
            int key = keys[i];
            T value = get(key);

            if (i > 0) {
                buffer.append(", ");
            }

            buffer.append(key).append('=').append(value == this ? "(this Map)" : value);
        }

        buffer.append('}');

        return buffer.toString();
    }

    /**
     * 重构hash表，倍增其容量。
     */
    protected void rehash() {
        int oldCapacity = table.length;
        Entry<T>[] oldMap = table;

        int newCapacity = oldCapacity * 2;

        @SuppressWarnings("unchecked")
        Entry<T>[] newMap = new Entry[newCapacity];

        threshold = (int) (newCapacity * loadFactor);
        table = newMap;

        for (int i = oldCapacity; i-- > 0;) {
            for (Entry<T> old = oldMap[i]; old != null;) {
                Entry<T> e = old;

                old = old.next;

                int index = (e.hash & 0x7FFFFFFF) % newCapacity;

                e.next = newMap[index];
                newMap[index] = e;
            }
        }
    }

    /**
     * 取得hash表的容量。
     * 
     * @return hash表的容量
     */
    protected int getCapacity() {
        return table.length;
    }

    /**
     * 取得hash表的阈值。
     * 
     * @return hash表的阈值
     */
    protected int getThreshold() {
        return threshold;
    }

    /**
     * 代表hash表中的一个元素的类。
     */
    protected static class Entry<T> {
        protected int hash;
        protected int key;
        protected T value;
        protected Entry<T> next;

        protected Entry(int hash, int key, T value, Entry<T> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
}
