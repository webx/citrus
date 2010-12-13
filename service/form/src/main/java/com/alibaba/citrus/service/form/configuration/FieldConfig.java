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

import com.alibaba.citrus.service.form.Validator;

/**
 * 代表一个form field的定义信息。
 * <p>
 * Form field定义是不可更改的。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface FieldConfig {
    /**
     * 取得field所属的group config。
     */
    GroupConfig getGroupConfig();

    /**
     * 取得field name。
     */
    String getName();

    /**
     * 取得field key。
     */
    String getKey();

    /**
     * 取得用来显示field的名称。
     */
    String getDisplayName();

    /**
     * 取得trimming选项。
     */
    boolean isTrimming();

    /**
     * 取得bean property名称。
     */
    String getPropertyName();

    /**
     * 取得单个默认值。
     */
    String getDefaultValue();

    /**
     * 取得一组默认值。
     */
    String[] getDefaultValues();

    /**
     * 取得validator列表。
     */
    List<Validator> getValidators();
}
