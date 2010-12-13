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

import org.springframework.beans.TypeConverter;

import com.alibaba.citrus.service.form.configuration.FormConfig;

/**
 * 代表一个用户提交的form信息。
 * <p>
 * 注意：form对象不是线程安全的，不能被多线程共享。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface Form {
    /**
     * 取得form的配置信息。
     */
    FormConfig getFormConfig();

    /**
     * 取得用于转换类型的converter。
     */
    TypeConverter getTypeConverter();

    /**
     * 是否强制为只接受post表单。
     */
    boolean isForcePostOnly();

    /**
     * 判定form是否通过验证。
     */
    boolean isValid();

    /**
     * 初始化form，将form恢复成“未验证”状态。随后，调用者可以重新设置值并手工验证表单。
     */
    void init();

    /**
     * 用request初始化form。假如request为<code>null</code>，则将form设置成“未验证”状态，否则，验证表单。
     */
    void init(HttpServletRequest request);

    /**
     * 验证（或重新验证）当前的所有group instance。
     */
    void validate();

    /**
     * 取得代表form的key。
     */
    String getKey();

    /**
     * 取得所有group的列表。
     */
    Collection<Group> getGroups();

    /**
     * 取得所有指定名称的group的列表。group名称大小写不敏感。
     */
    Collection<Group> getGroups(String groupName);

    /**
     * 取得默认的group instance。如果该group instance不存在，则创建之。Group名称大小写不敏感。
     */
    Group getGroup(String groupName);

    /**
     * 取得group instance。如果该group instance不存在，则创建之。Group名称大小写不敏感。
     */
    Group getGroup(String groupName, String instanceKey);

    /**
     * 取得group instance。如果该group instance不存在，并且<code>create == true</code>
     * ，则创建之。Group名称大小写不敏感。
     */
    Group getGroup(String groupName, String instanceKey, boolean create);
}
