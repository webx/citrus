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

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.core.MethodParameter;

import com.alibaba.citrus.service.requestcontext.support.ValueListSupport;
import com.alibaba.citrus.service.requestcontext.util.ValueList;
import com.alibaba.citrus.service.upload.support.StringFileItemEditor;
import com.alibaba.citrus.util.ObjectUtil;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 代表一个解析器的基类，用来取得HTTP请求中的参数和cookies。
 * <p>
 * 注意：参数和cookie的名称可能被转换成全部大写或全部小写。 这是根据配置文件中的参数：<code>caseFolding</code> 来指定的。
 * </p>
 */
public abstract class AbstractValueParser implements ValueParser {
    protected final SimpleTypeConverter converter;
    protected final Map<String, Object> parameters = createLinkedHashMap();
    protected final Map<String, String> parameterKeys = createLinkedHashMap();
    protected final ParserRequestContext requestContext;

    public AbstractValueParser(ParserRequestContext requestContext) {
        this.requestContext = requestContext;
        this.converter = new SimpleTypeConverter();
        this.converter.registerCustomEditor(String.class, new StringFileItemEditor());

        if (requestContext.getPropertyEditorRegistrar() != null) {
            requestContext.getPropertyEditorRegistrar().registerCustomEditors(converter);
        }
    }

    protected abstract Logger getLogger();

    public TypeConverter getTypeConverter() {
        return converter;
    }

    // =============================================================
    //  查询参数的方法 
    // =============================================================

    /**
     * 取得值的数量。
     * 
     * @return 值的数量
     */
    public int size() {
        return parameters.size();
    }

    /**
     * 判断是否无值。
     * 
     * @return 如果无值，则返回<code>true</code>
     */
    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    /**
     * 检查是否包含指定名称的参数。
     * 
     * @param key 要查找的参数名
     * @return 如果存在，则返回<code>true</code>
     */
    public boolean containsKey(String key) {
        return parameters.containsKey(convert(key));
    }

    /*
     * 取得所有参数名的集合。
     * @return 所有参数名的集合
     */
    public Set<String> keySet() {
        return createLinkedHashSet(parameterKeys.values());
    }

    /*
     * 取得所有参数名的数组。
     * @return 所有参数名的数组
     */
    public String[] getKeys() {
        return parameterKeys.values().toArray(new String[parameterKeys.size()]);
    }

    // =============================================================
    //  取得参数的值
    // =============================================================

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>false</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    public boolean getBoolean(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? false : container.getBooleanValue();
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getBooleanValue(defaultValue);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    public byte getByte(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? 0 : container.getByteValue();
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public byte getByte(String key, byte defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getByteValue(defaultValue);
    }

    /**
     * 取得指定参数的字节。这个字节是根据<code>getCharacterEncoding()</code>所返回的字符集进行编码的。
     * 
     * @param key 参数名
     * @return 参数值的字节数组，如果参数不存在，则返回<code>null</code>
     * @throws UnsupportedEncodingException 如果指定了错误的编码字符集
     */
    public byte[] getBytes(String key) throws UnsupportedEncodingException {
        ValueList container = getValueList(key, false);
        return container == null ? EMPTY_BYTE_ARRAY : container.getBytes(getCharacterEncoding());
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>'\0'</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    public char getChar(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? '\0' : container.getCharacterValue();
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public char getChar(String key, char defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getCharacterValue(defaultValue);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    public double getDouble(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? 0 : container.getDoubleValue();
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public double getDouble(String key, double defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getDoubleValue(defaultValue);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    public float getFloat(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? 0 : container.getFloatValue();
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public float getFloat(String key, float defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getFloatValue(defaultValue);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    public int getInt(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? 0 : container.getIntegerValue();
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public int getInt(String key, int defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getIntegerValue(defaultValue);
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值的数组
     */
    public int[] getInts(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? EMPTY_INT_ARRAY : container.getIntegerValues();
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    public int[] getInts(String key, int[] defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getIntegerValues(defaultValue);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    public long getLong(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? 0 : container.getLongValue();
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public long getLong(String key, long defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getLongValue(defaultValue);
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值的数组
     */
    public long[] getLongs(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? EMPTY_LONG_ARRAY : container.getLongValues();
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    public long[] getLongs(String key, long[] defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getLongValues(defaultValue);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>0</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    public short getShort(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? 0 : container.getShortValue();
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public short getShort(String key, short defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getShortValue(defaultValue);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    public String getString(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? null : container.getStringValue();
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public String getString(String key, String defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getStringValue(defaultValue);
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值的数组
     */
    public String[] getStrings(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? EMPTY_STRING_ARRAY : container.getStringValues();
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    public String[] getStrings(String key, String[] defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getStringValues(defaultValue);
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>null</code>。 此方法和<code>getObject</code>
     * 一样，但在模板中便易于使用。
     * 
     * @param key 参数名
     * @return 参数值
     */
    public Object get(String key) {
        return getObject(key);
    }

    /**
     * 取得日期。字符串将使用指定的<code>DateFormat</code>来解析。如果不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @param format <code>DateFormat</code>对象
     * @return <code>java.util.Date</code>对象
     */
    public Date getDate(String key, DateFormat format) {
        ValueList container = getValueList(key, false);
        return container == null ? null : container.getDateValue(format);
    }

    /**
     * 取得日期。字符串将使用指定的<code>DateFormat</code>来解析。如果不存在，则返回默认值。
     * 
     * @param key 参数名
     * @param format <code>DateFormat</code>对象
     * @param defaultValue 默认值
     * @return <code>java.util.Date</code>对象
     */
    public Date getDate(String key, DateFormat format, Date defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getDateValue(format, defaultValue);
    }

    /**
     * 取得指定参数的值。如果参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值
     */
    public Object getObject(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? null : container.getValue();
    }

    /**
     * 取得指定参数的值。如果参数不存在，则返回默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Object getObject(String key, Object defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getValue(defaultValue);
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return 参数值的数组
     */
    public Object[] getObjects(String key) {
        ValueList container = getValueList(key, false);
        return container == null ? EMPTY_OBJECT_ARRAY : container.getValues();
    }

    /**
     * 取得指定参数的所有值。如果参数不存在，则返回指定默认值。
     * 
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值的数组
     */
    public Object[] getObjects(String key, Object[] defaultValue) {
        ValueList container = getValueList(key, false);
        return container == null ? defaultValue : container.getValues(defaultValue);
    }

    /**
     * 取得指定类型的对象。
     */
    public <T> T getObjectOfType(String key, Class<T> type) {
        return getObjectOfType(key, type, null, null);
    }

    /**
     * 取得指定类型的对象。
     */
    public <T> T getObjectOfType(String key, Class<T> type, MethodParameter methodParameter, Object[] defaultValues) {
        return getObjectOfType(key, type, false, methodParameter, defaultValues);
    }

    /**
     * 取得指定类型的对象。
     */
    <T> T getObjectOfType(String key, Class<T> type, boolean isPrimitive, MethodParameter methodParameter,
                          Object[] defaultValues) {
        ValueList container = getValueList(key, false);

        if (container == null) {
            container = new ValueListSupport(getTypeConverter(), requestContext.isConverterQuiet());

            if (!isEmptyArray(defaultValues)) {
                for (Object value : defaultValues) {
                    container.addValue(value);
                }
            }

            defaultValues = null;
        }

        return container.getValueOfType(type, isPrimitive, methodParameter, defaultValues);
    }

    public void setProperties(Object object) {
        if (object == null) {
            return;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Set HTTP request parameters to object " + ObjectUtil.identityToString(object));
        }

        BeanWrapper bean = new BeanWrapperImpl(object);
        requestContext.getPropertyEditorRegistrar().registerCustomEditors(bean);

        for (String key : keySet()) {
            String propertyName = StringUtil.toCamelCase(key);

            if (bean.isWritableProperty(propertyName)) {
                PropertyDescriptor pd = bean.getPropertyDescriptor(propertyName);
                MethodParameter mp = BeanUtils.getWriteMethodParameter(pd);
                Object value = getObjectOfType(key, pd.getPropertyType(), mp, null);

                bean.setPropertyValue(propertyName, value);
            } else {
                getLogger().debug("No writable property \"{}\" found in type {}", propertyName,
                        object.getClass().getName());
            }
        }
    }

    // =============================================================
    //  添加和修改参数的方法
    // =============================================================

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void add(String key, boolean value) {
        getValueList(key, true).addValue(value);
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void add(String key, byte value) {
        getValueList(key, true).addValue(value);
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void add(String key, char value) {
        getValueList(key, true).addValue(value);
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void add(String key, double value) {
        getValueList(key, true).addValue(value);
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void add(String key, float value) {
        getValueList(key, true).addValue(value);
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void add(String key, int value) {
        getValueList(key, true).addValue(value);
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void add(String key, long value) {
        getValueList(key, true).addValue(value);
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void add(String key, short value) {
        getValueList(key, true).addValue(value);
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void add(String key, Object value) {
        getValueList(key, true).addValue(value);
    }

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void setString(String key, String value) {
        setObject(key, value);
    }

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param key 参数名
     * @param values 参数值的数组
     */
    public void setStrings(String key, String[] values) {
        setObjects(key, values);
    }

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void setObject(String key, Object value) {
        getValueList(key, true).setValue(value);
    }

    /**
     * 设置参数值。和<code>add</code>方法不同，此方法将覆盖原有的值。
     * 
     * @param key 参数名
     * @param values 参数值
     */
    public void setObjects(String key, Object[] values) {
        getValueList(key, true).setValues(values);
    }

    // =============================================================
    //  清除参数的方法
    // =============================================================

    /**
     * 删除指定名称的参数。
     * 
     * @return 原先和指定名称对应的参数值，可能是<code>String[]</code>或<code>null</code>
     */
    public Object remove(String key) {
        key = convert(key);
        parameterKeys.remove(key);
        return parameters.remove(key);
    }

    /**
     * 清除所有值。
     */
    public void clear() {
        parameterKeys.clear();
        parameters.clear();
    }

    // =============================================================
    //  辅助方法
    // =============================================================

    /**
     * 首先将参数名进行<code>trim()</code>，然后再进行大小写转换。转换是根据配置文件中的
     * <code>url.case.folding</code>来设定的。
     * 
     * @param key 要转换的参数名
     * @return 被<code>trim()</code>和大小写转换后的参数名，如果是<code>null</code>，则转换成空字符串
     */
    protected String convert(String key) {
        if (requestContext == null) {
            return key;
        }

        return requestContext.convertCase(key);
    }

    /**
     * 取得指定参数的值的列表。
     * 
     * @param key 参数名
     * @param create 如果参数不存在，是否创建之
     * @return 参数值的列表，如果参数不存在，且<code>create==false</code>，则返回<code>null</code>
     */
    protected ValueList getValueList(String key, boolean create) {
        String originalKey = key;

        key = convert(key);

        ValueList container = (ValueList) parameters.get(key);

        if (create) {
            if (container == null) {
                container = new ValueListSupport(getTypeConverter(), requestContext.isConverterQuiet());
                parameterKeys.put(key, originalKey);
                parameters.put(key, container);
            }

            return container;
        } else {
            if (container == null || container.size() == 0) {
                return null;
            } else {
                return container;
            }
        }
    }

    /**
     * 取得用于解析参数的编码字符集。不同的实现取得编码字符集的方法也不同，例如，对于<code>ParameterParser</code>，
     * 此编码字符集是由<code>request.getCharacterEncoding()</code>决定的。
     * <p>
     * 默认总是返回<code>ISO-8859-1</code>。
     * </p>
     * 
     * @return 编码字符集
     */
    protected String getCharacterEncoding() {
        return ParserRequestContext.DEFAULT_CHARSET_ENCODING;
    }

    /**
     * 转换成字符串。
     * 
     * @return 字符串表现
     */
    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder().setSortKeys(true);

        for (String key : parameterKeys.values()) {
            mb.append(key, getValueList(key, false));
        }

        return mb.toString();
    }
}
