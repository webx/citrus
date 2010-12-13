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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * <p>
 * 一个集合的实现, 实现了<code>Set</code>接口.
 * </p>
 * <p>
 * 这个集合在内部使用<code>ArrayHashMap</code>保存集合中的元素, 因而具有以下特性:
 * </p>
 * <ul>
 * <li>不同于<code>HashMap</code>, 集合中元素的顺序是确定的</li>
 * <li>和<code>ArrayHashMap</code>一样, 没有进行任何<code>synchronized</code>操作</li>
 * </ul>
 * 
 * @author Michael Zhou
 * @see ArrayHashMap
 */
public class ArrayHashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, Serializable {
    // ==========================================================================
    // 常量                                                                      
    // ==========================================================================

    /** 表示内部hash表的值. */
    private static final Object PRESENT = new Object();

    // ==========================================================================
    // 成员变量                                                                  
    // ==========================================================================

    /** 内部的hash表. */
    protected transient ArrayHashMap<E, Object> map;

    // ==========================================================================
    // 构造函数                                                                   
    // ==========================================================================

    /**
     * 创建一个空的集合. 使用指定的默认的初始容量(16)和默认的负载系数(0.75).
     */
    public ArrayHashSet() {
        map = new ArrayHashMap<E, Object>();
    }

    /**
     * 创建一个空的集合. 使用指定的初始阈值和默认的负载系数(0.75).
     * 
     * @param initialCapacity 初始容量.
     */
    public ArrayHashSet(int initialCapacity) {
        map = new ArrayHashMap<E, Object>(initialCapacity);
    }

    /**
     * 创建一个空的集合. 使用指定的初始容量和负载系数.
     * 
     * @param initialCapacity 初始容量
     * @param loadFactor 负载系数.
     */
    public ArrayHashSet(int initialCapacity, float loadFactor) {
        map = new ArrayHashMap<E, Object>(initialCapacity, loadFactor);
    }

    /**
     * 创建一个空的集合, 并复制指定的<code>Collection</code>的所有项到这个集合中. 使用默认的负载系数(0.75).
     * 
     * @param collection 要复制的<code>Collection</code>
     */
    public ArrayHashSet(Collection<? extends E> collection) {
        map = new ArrayHashMap<E, Object>(Math.max((int) (collection.size() / .75f) + 1, 16));
        addAll(collection);
    }

    // ==========================================================================
    // 实现Set接口的方法                                                          
    // ==========================================================================

    /**
     * 返回集合中entry的个数.
     * 
     * @return 集合中的entry数.
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * 判断是否为空的集合.
     * 
     * @return 如果为空(<code>size() == 0</code>), 则返回<code>true</code>.
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * 如果集合中包含指定值, 则返回<code>true</code>.
     * 
     * @param object 测试指定值是否存在.
     * @return 如果指定值存在, 则返回<code>true</code>.
     */
    @Override
    public boolean contains(Object object) {
        return map.containsKey(object);
    }

    /**
     * 将指定的值加入到集合中.
     * 
     * @param object 要加入的值
     * @return 如果集合中已经存在此值, 则返回<code>false</code>. 否则返回<code>true</code>.
     */
    @Override
    public boolean add(E object) {
        return map.put(object, PRESENT) == null;
    }

    /**
     * 将指定值从集合中删除(如果该值存在的话).
     * 
     * @param object 要被删除的值
     * @return 如果被删除的值原来不存在, 则返回<code>false</code>, 否则返回<code>true</code>
     */
    @Override
    public boolean remove(Object object) {
        return map.remove(object) == PRESENT;
    }

    /**
     * 清除集合中的所有对象.
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * 取得集合中所有项的遍历器.
     * 
     * @return 集合中所有项的遍历器
     */
    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    // ==========================================================================
    // 复制方法(Clonable接口)                                                     
    // ==========================================================================

    /**
     * &quot;浅&quot;拷贝集合, 集合中的对象本身并不被复制.
     * 
     * @return 被复制的集合.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            ArrayHashSet<E> newSet = (ArrayHashSet<E>) super.clone();

            newSet.map = (ArrayHashMap<E, Object>) map.clone();

            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(); // 不支持clone(不可能).
        }
    }

    // ==========================================================================
    // 序列化                                                                     
    // ==========================================================================

    /** 序列化版本号. */
    private static final long serialVersionUID = -5024744406713321676L;

    /**
     * 从输入流中重建集合(也就是反序列化).
     * 
     * @param is 输入流
     * @exception IOException 输入流异常
     * @exception ClassNotFoundException 类未找到
     */
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();

        int capacity = is.readInt();
        float loadFactor = is.readFloat();

        map = new ArrayHashMap<E, Object>(capacity, loadFactor);

        int size = is.readInt();

        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked")
            E e = (E) is.readObject();

            map.put(e, PRESENT);
        }
    }

    /**
     * 将集合的状态保存到输出流中(也就是序列化).
     * 
     * @param os 输出流
     * @exception IOException 输出流异常
     */
    private void writeObject(ObjectOutputStream os) throws IOException {
        os.defaultWriteObject();

        os.writeInt(map.getCapacity());
        os.writeFloat(map.getLoadFactor());

        os.writeInt(map.size());

        for (E e : map.keySet()) {
            os.writeObject(e);
        }
    }
}
