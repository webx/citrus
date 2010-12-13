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

/**
 * 代表一个form group的定义信息。
 * <p>
 * Form group定义是不可更改的。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface GroupConfig {
    /**
     * 取得group所属的form config。
     */
    FormConfig getFormConfig();

    /**
     * 取得group name。
     */
    String getName();

    /**
     * 取得parent group。
     */
    String getParentGroup();

    /**
     * 取得group key。
     */
    String getKey();

    /**
     * 取得默认的trimming选项。
     */
    boolean isTrimmingByDefault();

    /**
     * Group是否必须从post请求中取得数据。
     */
    boolean isPostOnly();

    /**
     * 取得所有field config的列表。
     */
    List<FieldConfig> getFieldConfigList();

    /**
     * 取得指定名称的field config。名称大小写不敏感。 如果未找到，则返回<code>null</code>。
     */
    FieldConfig getFieldConfig(String fieldName);

    /**
     * 取得指定key对应的field config。如果未找到，则返回<code>null</code>。
     */
    FieldConfig getFieldConfigByKey(String fieldKey);

    /**
     * 取得所有的imports。
     */
    List<Import> getImports();

    /**
     * 代表import其它group中的field的信息。
     */
    interface Import {
        String getGroupName();

        String getFieldName();
    }
}
