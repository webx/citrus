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
package com.alibaba.citrus.service.requestcontext.support;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ClassUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.CollectionFactory;
import org.springframework.core.MethodParameter;

import com.alibaba.citrus.service.requestcontext.util.ValueList;
import com.alibaba.citrus.util.ArrayUtil;
import com.alibaba.citrus.util.ClassUtil;
import com.alibaba.citrus.util.ObjectUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 代表一个值的列表。
 * 
 * @author Michael Zhou
 */
public class ValueListSupport implements ValueList {
    private final TypeConverter converter;
    private final List<Object> values = createLinkedList();
    private final boolean quiet;

    public ValueListSupport(TypeConverter converter, boolean quiet) {
        this.converter = assertNotNull(converter, "converter");
        this.quiet = quiet;
    }

    // =============================================================
    //  取得参数的值 
    // =============================================================

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>false</code>。
     * 
     * @return 参数值
     */
    public boolean getBooleanValue() {
        return getBooleanValue(null);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    public boolean getBooleanValue(Boolean defaultValue) {
        return getValueOfType(Boolean.class, true, (MethodParameter) null, new Object[] { defaultValue });
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    public byte getByteValue() {
        return getByteValue(null);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    public byte getByteValue(Byte defaultValue) {
        return getValueOfType(Byte.class, true, (MethodParameter) null, new Object[] { defaultValue });
    }

    /**
     * 取得指定参数的字节。
     * 
     * @param charset 用来转换字符的编码
     * @return 参数值的字节数组
     * @throws UnsupportedEncodingException 如果指定了错误的编码字符集
     */
    public byte[] getBytes(String charset) throws UnsupportedEncodingException {
        String value = getStringValue();
        return value == null ? EMPTY_BYTE_ARRAY : value.getBytes(charset);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>'\0'</code>。
     * 
     * @return 参数值
     */
    public char getCharacterValue() {
        return getCharacterValue(null);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    public char getCharacterValue(Character defaultValue) {
        return getValueOfType(Character.class, true, (MethodParameter) null, new Object[] { defaultValue });
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    public double getDoubleValue() {
        return getDoubleValue(null);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    public double getDoubleValue(Double defaultValue) {
        return getValueOfType(Double.class, true, (MethodParameter) null, new Object[] { defaultValue });
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    public float getFloatValue() {
        return getFloatValue(null);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    public float getFloatValue(Float defaultValue) {
        return getValueOfType(Float.class, true, (MethodParameter) null, new Object[] { defaultValue });
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    public int getIntegerValue() {
        return getIntegerValue(null);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    public int getIntegerValue(Integer defaultValue) {
        return getValueOfType(Integer.class, true, (MethodParameter) null, new Object[] { defaultValue });
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值的数组
     */
    public int[] getIntegerValues() {
        return getIntegerValues(EMPTY_INT_ARRAY);
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    public int[] getIntegerValues(int[] defaultValue) {
        return getValueOfType(int[].class, null, toIntegerArray(defaultValue));
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    public long getLongValue() {
        return getLongValue(null);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    public long getLongValue(Long defaultValue) {
        return getValueOfType(Long.class, true, (MethodParameter) null, new Object[] { defaultValue });
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值的数组
     */
    public long[] getLongValues() {
        return getLongValues(EMPTY_LONG_ARRAY);
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    public long[] getLongValues(long[] defaultValue) {
        return getValueOfType(long[].class, null, toLongArray(defaultValue));
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @return 参数值
     */
    public short getShortValue() {
        return getShortValue(null);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    public short getShortValue(Short defaultValue) {
        return getValueOfType(Short.class, true, (MethodParameter) null, new Object[] { defaultValue });
    }

    /**
     * 取得日期。字符串将使用指定的<code>DateFormat</code>来解析。如果不存在，则返回<code>null</code>。
     * 
     * @param format <code>DateFormat</code>对象
     * @return <code>java.util.Date</code>对象
     */
    public Date getDateValue(DateFormat format) {
        return getDateValue(format, null);
    }

    /**
     * 取得日期。字符串将使用指定的<code>DateFormat</code>来解析。如果不存在，则返回默认值。
     * 
     * @param format <code>DateFormat</code>对象
     * @param defaultValue 默认值
     * @return <code>java.util.Date</code>对象
     */
    public Date getDateValue(DateFormat format, Date defaultValue) {
        String value = getStringValue();
        Date date = defaultValue;

        if (value != null) {
            try {
                format.setLenient(false);
                date = format.parse(value);
            } catch (ParseException e) {
                date = defaultValue;
            }
        }

        return date;
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值
     */
    public String getStringValue() {
        return getStringValue(null);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    public String getStringValue(String defaultValue) {
        String value = getValueOfType(String.class, null, new Object[] { defaultValue });

        if (value == null || "null".equals(value) || value.length() == 0) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值的数组
     */
    public String[] getStringValues() {
        return getStringValues(EMPTY_STRING_ARRAY);
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    public String[] getStringValues(String[] defaultValue) {
        String[] values = getValueOfType(String[].class, null, defaultValue);

        if (values == null) {
            values = defaultValue;
        } else {
            for (int i = 0; i < values.length; i++) {
                if (values[i] == null || "null".equals(values[i]) || values[i].length() == 0) {
                    values[i] = "";
                }
            }
        }

        return values;
    }

    /**
     * 取得<code>FileItem</code>对象，如果不存在，则返回<code>null</code>。
     * 
     * @return <code>FileItem</code>对象
     */
    public FileItem getFileItem() {
        Object value = getValue();

        return value instanceof FileItem ? (FileItem) value : null;
    }

    /**
     * 取得<code>FileItem</code>对象，如果不存在，则返回<code>null</code>。
     * 
     * @return <code>FileItem</code>对象的数组
     */
    public FileItem[] getFileItems() {
        try {
            return values.toArray(new FileItem[values.size()]);
        } catch (ArrayStoreException e) {
            return new FileItem[0];
        }
    }

    /**
     * 取得指定参数的值。如果参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值
     */
    public Object getValue() {
        return getValue(null);
    }

    /**
     * 取得指定参数的值。如果参数不存在，则返回默认值。
     * 
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Object getValue(Object defaultValue) {
        Object value = null;

        if (values.size() > 0) {
            value = values.get(0);
        }

        return ObjectUtil.defaultIfNull(value, defaultValue);
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @return 参数值的数组
     */
    public Object[] getValues() {
        return getValues(EMPTY_OBJECT_ARRAY);
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param defaultValues 默认值
     * @return 参数值的数组
     */
    public Object[] getValues(Object[] defaultValues) {
        Object[] values = this.values.toArray();
        return isEmptyArray(values) ? defaultValues : values;
    }

    /**
     * 取得指定类型的值。
     */
    public <T> T getValueOfType(Class<T> type, MethodParameter methodParameter, Object[] defaultValues) {
        return getValueOfType(type, false, methodParameter, defaultValues);
    }

    /**
     * 取得指定类型的值。
     */
    public <T> T getValueOfType(Class<T> type, boolean isPrimitive, MethodParameter methodParameter,
                                Object[] defaultValues) {
        // 处理默认值，如为空，转换为空数组。
        if (defaultValues == null || defaultValues.length == 1 && defaultValues[0] == null) {
            defaultValues = EMPTY_OBJECT_ARRAY;
        }

        // 对于primitive类型，转换为系统默认值。
        if (type.isPrimitive()) {
            isPrimitive = true;
            type = ClassUtil.getWrapperTypeIfPrimitive(type);
        }

        if (isPrimitive && isEmptyArray(defaultValues)) {
            Object defaultValue = getPrimitiveDefaultValue(type);

            if (defaultValue != null) {
                defaultValues = new Object[] { defaultValue };
            }
        }

        // 取得值，假如参数不存在，则取默认值。
        Object[] values = getValues(defaultValues);

        // 特殊情况，[""]也取默认值。
        if (values.length == 1 && isEmptyObject(values[0])) {
            values = defaultValues;
        }

        try {
            return convert(type, methodParameter, values, defaultValues.length > 0 ? defaultValues[0] : null);
        } catch (TypeMismatchException e) {
            if (quiet) {
                return convert(type, methodParameter, defaultValues, null);
            } else {
                throw e;
            }
        }
    }

    private <T> T convert(Class<T> type, MethodParameter methodParameter, Object[] values, Object defaultValue) {
        if (values == null) {
            values = EMPTY_STRING_ARRAY;
        }

        Object convertedValue = null;
        boolean requiresArray = type.isArray() || CollectionFactory.isApproximableCollectionType(type);

        if (values.length == 0) {
            if (!type.equals(String.class)) {
                try {
                    convertedValue = converter.convertIfNecessary(values, type, methodParameter);
                } catch (TypeMismatchException e) {
                    // ignored for empty value, just returns null
                }
            }
        } else {
            if (requiresArray) {
                convertedValue = converter.convertIfNecessary(values, type, methodParameter);
            } else {
                Object singleValue = values[0];

                if (isEmptyObject(singleValue)) {
                    singleValue = defaultValue;
                }

                convertedValue = converter.convertIfNecessary(singleValue, type, methodParameter);
            }
        }

        return type.cast(convertedValue);
    }

    private Integer[] toIntegerArray(int[] values) {
        if (isEmptyArray(values)) {
            return EMPTY_INTEGER_OBJECT_ARRAY;
        }

        Integer[] integerValues = new Integer[values.length];

        for (int i = 0; i < values.length; i++) {
            integerValues[i] = values[i];
        }

        return integerValues;
    }

    private Long[] toLongArray(long[] values) {
        if (isEmptyArray(values)) {
            return EMPTY_LONG_OBJECT_ARRAY;
        }

        Long[] longValues = new Long[values.length];

        for (int i = 0; i < values.length; i++) {
            longValues[i] = values[i];
        }

        return longValues;
    }

    // =============================================================
    //  添加和修改参数的方法
    // =============================================================

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    public void addValue(boolean value) {
        addValue(Boolean.toString(value));
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    public void addValue(byte value) {
        addValue(Byte.toString(value));
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    public void addValue(char value) {
        addValue(Character.toString(value));
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    public void addValue(double value) {
        addValue(Double.toString(value));
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    public void addValue(float value) {
        addValue(Float.toString(value));
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    public void addValue(int value) {
        addValue(Integer.toString(value));
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    public void addValue(long value) {
        addValue(Long.toString(value));
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    public void addValue(short value) {
        addValue(Short.toString(value));
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param value 参数值
     */
    public void addValue(Object value) {
        values.add(value);
    }

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param value 参数值
     */
    public void setValue(Object value) {
        clear();
        addValue(value);
    }

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param values 参数值
     */
    public void setValues(Object[] values) {
        clear();

        if (!ArrayUtil.isEmptyArray(values)) {
            for (Object value : values) {
                addValue(value);
            }
        }
    }

    // =============================================================
    //  辅助方法
    // =============================================================

    /**
     * 取得值的个数。
     * 
     * @return 值的个数
     */
    public int size() {
        return values.size();
    }

    /**
     * 清除所有值。
     */
    protected void clear() {
        values.clear();
    }

    /**
     * 取得字符串表示。
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        ToStringBuilder buf = new ToStringBuilder();

        if (values.size() == 1) {
            buf.append(values.get(0));
        } else {
            buf.appendCollection(values);
        }

        return buf.toString();
    }
}
