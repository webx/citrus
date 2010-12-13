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
package com.alibaba.citrus.service.form;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.form.configuration.GroupConfig;

/**
 * 代表用户所提交表单中的一组字段。
 * <p>
 * 注意：group对象不是线程安全的，不能被多线程共享。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface Group {
    /**
     * 取得group的配置信息。
     */
    GroupConfig getGroupConfig();

    /**
     * 取得包含此group的form。
     */
    Form getForm();

    /**
     * 取得group name，相当于<code>getGroupConfig().getName()</code>
     */
    String getName();

    /**
     * 取得代表group的key。
     * <p>
     * 由固定前缀<code>"_fm"</code>，加上group名的缩写，再加上group instance key构成。例如：
     * <code>_fm.m._0</code>。
     * </p>
     */
    String getKey();

    /**
     * 取得标识当前group的instance key。
     */
    String getInstanceKey();

    /**
     * 判定group是否通过验证。
     */
    boolean isValid();

    /**
     * 判定该group是否被置值，并验证。在两种情况下，<code>isValidated()</code>为<code>true</code>。
     * <ol>
     * <li>用户提交包含当前group字段的表单。此时相应的group被初始化并验证。</li>
     * <li>程序调用<code>validate()</code>方法。这种方式下，group中的字段值可以由程序来设置，效果如同用户提交表单一样。</li>
     * </ol>
     */
    boolean isValidated();

    /**
     * 初始化group。
     */
    void init();

    /**
     * 初始化group。 其中， <code>request</code>可以是<code>null</code>，如果
     * <code>request</code>不为<code>null</code>，则同时验证表单。
     */
    void init(HttpServletRequest request);

    /**
     * 验证（或重新验证）当前的字段值。
     * <p>
     * 注意，此方法将设置<code>isValidated()</code>为<code>true</code>。
     * </p>
     */
    void validate();

    /**
     * 取得所有fields的列表。
     */
    Collection<Field> getFields();

    /**
     * 取得指定名称的field。field名称（大小写不敏感）
     */
    Field getField(String fieldName);

    /**
     * 将对象中的属性值置入到fields中。
     * <p>
     * 对于<code>isValidated()</code>为<code>true</code>的group，该方法无效。
     * </p>
     */
    void mapTo(Object object);

    /**
     * 将group中的值置入指定对象。
     * <p>
     * 对于<code>isValidated()</code>为<code>false</code>的group，该方法无效。
     * </p>
     */
    void setProperties(Object object);
}
