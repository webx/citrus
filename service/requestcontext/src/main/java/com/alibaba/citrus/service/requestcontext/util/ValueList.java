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
package com.alibaba.citrus.service.requestcontext.util;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.fileupload.FileItem;
import org.springframework.core.MethodParameter;

/**
 * 代表一个值的列表。
 * 
 * @author Michael Zhou
 */
public interface ValueList {
    // =============================================================
    //  取得参数的值 
    // =============================================================

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>false</code>。
     * 
     * @return 参数值
     */
    boolean getBooleanValue();

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    boolean getBooleanValue(Boolean defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    byte getByteValue();

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    byte getByteValue(Byte defaultValue);

    /**
     * 取得指定参数的字节。
     * 
     * @param charset 用来转换字符的编码
     * @return 参数值的字节数组
     * @throws UnsupportedEncodingException 如果指定了错误的编码字符集
     */
    byte[] getBytes(String charset) throws UnsupportedEncodingException;

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>'\0'</code>。
     * 
     * @return 参数值
     */
    char getCharacterValue();

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    char getCharacterValue(Character defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    double getDoubleValue();

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    double getDoubleValue(Double defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    float getFloatValue();

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    float getFloatValue(Float defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    int getIntegerValue();

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    int getIntegerValue(Integer defaultValue);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值的数组
     */
    int[] getIntegerValues();

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    int[] getIntegerValues(int[] defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    long getLongValue();

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    long getLongValue(Long defaultValue);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值的数组
     */
    long[] getLongValues();

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    long[] getLongValues(long[] defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    short getShortValue();

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    short getShortValue(Short defaultValue);

    /**
     * 取得日期。字符串将使用指定的<code>DateFormat</code>来解析。如果不存在，则返回<code>null</code>。
     * 
     * @param format <code>DateFormat</code>对象
     * @return <code>java.util.Date</code>对象
     */
    Date getDateValue(DateFormat format);

    /**
     * 取得日期。字符串将使用指定的<code>DateFormat</code>来解析。如果不存在，则返回默认值。
     * 
     * @param format <code>DateFormat</code>对象
     * @param defaultValue 默认值
     * @return <code>java.util.Date</code>对象
     */
    Date getDateValue(DateFormat format, Date defaultValue);

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值
     */
    String getStringValue();

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    String getStringValue(String defaultValue);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值的数组
     */
    String[] getStringValues();

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    String[] getStringValues(String[] defaultValue);

    /**
     * 取得<code>FileItem</code>对象，如果不存在，则返回<code>null</code>。
     * 
     * @return <code>FileItem</code>对象
     */
    FileItem getFileItem();

    /**
     * 取得<code>FileItem</code>对象，如果不存在，则返回<code>null</code>。
     * 
     * @return <code>FileItem</code>对象的数组
     */
    FileItem[] getFileItems();

    /**
     * 取得指定参数的值。如果参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值
     */
    Object getValue();

    /**
     * 取得指定参数的值。如果参数不存在，则返回默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    Object getValue(Object defaultValue);

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值的数组
     */
    Object[] getValues();

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param defaultValues 默认值
     * @return 参数值的数组
     */
    Object[] getValues(Object[] defaultValues);

    /**
     * 取得指定类型的值。
     */
    <T> T getValueOfType(Class<T> type, MethodParameter methodParameter, Object[] defaultValues);

    /**
     * 取得指定类型的值。
     */
    <T> T getValueOfType(Class<T> type, boolean isPrimitive, MethodParameter methodParameter, Object[] defaultValues);

    // =============================================================
    //  添加和修改参数的方法
    // =============================================================

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    void addValue(boolean value);

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    void addValue(byte value);

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    void addValue(char value);

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    void addValue(double value);

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    void addValue(float value);

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    void addValue(int value);

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    void addValue(long value);

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    void addValue(short value);

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    void addValue(Object value);

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param value 参数值
     */
    void setValue(Object value);

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param values 参数值
     */
    void setValues(Object[] values);

    // =============================================================
    //  辅助方法
    // =============================================================

    /**
     * 取得值的个数。
     * 
     * @return 值的个数
     */
    int size();
}
