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
package com.alibaba.citrus.service.form.configuration;

import java.util.List;

import org.springframework.beans.PropertyEditorRegistrar;

import com.alibaba.citrus.service.form.FormService;

/**
 * 代表一个form的定义信息。
 * <p>
 * Form定义是不可更改的。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface FormConfig {
    /**
     * 取得创建此form的service。
     */
    FormService getFormService();

    /**
     * 类型转换出错时，是否不报错，而是返回默认值。
     */
    boolean isConverterQuiet();

    /**
     * Group是否默认必须从post请求中取得数据。
     */
    boolean isPostOnlyByDefault();

    /**
     * 取得message code的前缀。
     * <p>
     * Validator可以从spring <code>MessageSource</code>
     * 中取得message内容。用来引用message的code为：
     * <code>messageCodePrefix.groupName.fieldName.validatorId</code>。
     * </p>
     * <p>
     * 默认的前缀为：<code>form.</code>。
     * </p>
     */
    String getMessageCodePrefix();

    /**
     * 取得所有group config的列表。
     */
    List<GroupConfig> getGroupConfigList();

    /**
     * 取得指定名称的group config。名称大小写不敏感。 如果未找到，则返回<code>null</code>。
     */
    GroupConfig getGroupConfig(String groupName);

    /**
     * 取得和指定key相对应的group config。如果未找到，则返回<code>null</code>
     */
    GroupConfig getGroupConfigByKey(String groupKey);

    /**
     * 取得<code>PropertyEditor</code>注册器。
     * <p>
     * <code>PropertyEditor</code>负责将字符串值转换成bean property的类型，或反之。
     * </p>
     */
    PropertyEditorRegistrar getPropertyEditorRegistrar();
}
