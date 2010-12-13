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
package com.alibaba.citrus.service.requestcontext.parser;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import org.springframework.beans.TypeConverter;
import org.springframework.core.MethodParameter;

/**
 * 代表一个解析器，用来取得HTTP请求中的参数和cookies。
 * <p>
 * 注意：参数和cookie的名称可能被转换成全部大写或全部小写。 这是根据配置文件中的参数：<code>url.case.folding</code>
 * 来指定的。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface ValueParser {
    /**
     * 取得类型转换器。
     */
    TypeConverter getTypeConverter();

    // =============================================================
    //  查询参数的方法
    // =============================================================

    /**
     * 取得值的数量。
     * 
     * @return 值的数量
     */
    int size();

    /**
     * 判断是否无值。
     * 
     * @return 如果无值，则返回<code>true</code>
     */
    boolean isEmpty();

    /**
     * 检查是否包含指定名称的参数。
     * 
     * @param key 要查找的参数名
     * @return 如果存在，则返回<code>true</code>
     */
    boolean containsKey(String key);

    /*
     * 取得所有参数名的集合。
     * @return 所有参数名的集合
     */
    Set<String> keySet();

    /*
     * 取得所有参数名的数组。
     * @return 所有参数名的数组
     */
    String[] getKeys();

    // =============================================================
    //  取得参数的值
    // =============================================================

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>false</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    boolean getBoolean(String key);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    byte getByte(String key);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    byte getByte(String key, byte defaultValue);

    /**
     * 取得指定参数的字节。这个字节是根据<code>getCharacterEncoding()</code>所返回的字符集进行编码的。
     * 
     * @param key 参数名
     * @return 参数值的字节数组
     * @throws UnsupportedEncodingException 如果指定了错误的编码字符集
     */
    byte[] getBytes(String key) throws UnsupportedEncodingException;

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>'\0'</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    char getChar(String key);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    char getChar(String key, char defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    double getDouble(String key);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    double getDouble(String key, double defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    float getFloat(String key);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    float getFloat(String key, float defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    int getInt(String key);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    int getInt(String key, int defaultValue);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值的数组
     */
    int[] getInts(String key);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    int[] getInts(String key, int[] defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    long getLong(String key);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    long getLong(String key, long defaultValue);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值的数组
     */
    long[] getLongs(String key);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    long[] getLongs(String key, long[] defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    short getShort(String key);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    short getShort(String key, short defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    String getString(String key);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    String getString(String key, String defaultValue);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值的数组
     */
    String[] getStrings(String key);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    String[] getStrings(String key, String[] defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>null</code>。 此方法和<code>getString</code>
     * 一样，但在模板中便易于使用。
     * 
     * @param key 参数名
     * @return 参数值
     */
    Object get(String key);

    /**
     * 取得指定参数的值。如果参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    Object getObject(String key);

    /**
     * 取得指定参数的值。如果参数不存在，则返回默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    Object getObject(String key, Object defaultValue);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值的数组
     */
    Object[] getObjects(String key);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    Object[] getObjects(String key, Object[] defaultValue);

    /**
     * 取得日期。字符串将使用指定的<code>DateFormat</code>来解析。如果不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @param format <code>DateFormat</code>对象
     * @return <code>java.util.Date</code>对象
     */
    Date getDate(String key, DateFormat format);

    /**
     * 取得日期。字符串将使用指定的<code>DateFormat</code>来解析。如果不存在，则返回默认值。
     * 
     * @param key 参数名
     * @param format <code>DateFormat</code>对象
     * @param defaultValue 默认值
     * @return <code>java.util.Date</code>对象
     */
    Date getDate(String key, DateFormat format, Date defaultValue);

    /**
     * 取得指定类型的对象。
     */
    <T> T getObjectOfType(String key, Class<T> type);

    /**
     * 取得指定类型的对象。
     */
    <T> T getObjectOfType(String key, Class<T> type, MethodParameter methodParameter, Object[] defaultValues);

    /**
     * 将数据保存到object properties中。
     */
    void setProperties(Object object);

    // =============================================================
    //  添加和修改参数的方法
    // =============================================================

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void add(String key, boolean value);

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void add(String key, byte value);

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void add(String key, char value);

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void add(String key, double value);

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void add(String key, float value);

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void add(String key, int value);

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void add(String key, long value);

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void add(String key, short value);

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void add(String key, Object value);

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void setString(String key, String value);

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param key 参数名
     * @param values 参数值的数组
     */
    void setStrings(String key, String[] values);

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void setObject(String key, Object value);

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    void setObjects(String key, Object[] value);

    // =============================================================
    //  清除参数的方法 
    // =============================================================

    /**
     * 删除指定名称的参数。
     * 
     * @return 原先和指定名称对应的参数值，可能是<code>String[]</code>或<code>null</code>
     */
    Object remove(String key);

    /**
     * 清除所有值。
     */
    void clear();
}
