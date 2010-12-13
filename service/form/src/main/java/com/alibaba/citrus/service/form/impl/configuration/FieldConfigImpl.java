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
package com.alibaba.citrus.service.form.impl.configuration;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.util.List;

import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.configuration.FieldConfig;
import com.alibaba.citrus.service.form.configuration.GroupConfig;

/**
 * 代表一个form field的定义信息。
 * 
 * @author Michael Zhou
 */
public class FieldConfigImpl extends AbstractConfig<FieldConfig> implements FieldConfig {
    private GroupConfig groupConfig;
    private String name;
    private String key;
    private String displayName;
    private String[] defaultValues;
    private Boolean trimming;
    private String propertyName;
    private List<Validator> validators;
    private List<Validator> validatorList;

    /**
     * 取得field所属的group config。
     */
    public GroupConfig getGroupConfig() {
        return groupConfig;
    }

    /**
     * 设置field所属的group config。
     */
    public void setGroupConfig(GroupConfig groupConfig) {
        this.groupConfig = groupConfig;
    }

    /**
     * 取得field name。
     */
    public String getName() {
        return name;
    }

    /**
     * 设置field name。
     */
    public void setName(String name) {
        this.name = trimToNull(name);
    }

    /**
     * 取得field key。
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置field key。
     */
    public void setKey(String key) {
        this.key = trimToNull(key);
    }

    /**
     * 取得用来显示field的名称。
     */
    public String getDisplayName() {
        return displayName == null ? getName() : displayName;
    }

    /**
     * 设置用来显示field的名称。
     */
    public void setDisplayName(String displayName) {
        this.displayName = trimToNull(displayName);
    }

    /**
     * 取得trimming选项。
     */
    public boolean isTrimming() {
        if (trimming == null) {
            return groupConfig == null ? true : getGroupConfig().isTrimmingByDefault();
        } else {
            return trimming.booleanValue();
        }
    }

    /**
     * 设置trimming选项。
     */
    public void setTrimming(boolean trimming) {
        this.trimming = trimming;
    }

    /**
     * 取得bean property名称。
     */
    public String getPropertyName() {
        return propertyName == null ? getName() : propertyName;
    }

    /**
     * 设置bean property名称。
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = trimToNull(propertyName);
    }

    /**
     * 取得单个默认值。
     */
    public String getDefaultValue() {
        if (!isEmptyArray(defaultValues)) {
            return defaultValues[0];
        } else {
            return null;
        }
    }

    /**
     * 取得一组默认值。
     */
    public String[] getDefaultValues() {
        if (!isEmptyArray(defaultValues)) {
            return defaultValues.clone();
        } else {
            return EMPTY_STRING_ARRAY;
        }
    }

    /**
     * 设置默认值。
     */
    public void setDefaultValues(String[] defaultValues) {
        if (!isEmptyArray(defaultValues)) {
            this.defaultValues = defaultValues.clone();
        }
    }

    /**
     * 取得validator列表。
     */
    public List<Validator> getValidators() {
        if (validatorList == null) {
            return emptyList();
        } else {
            return validatorList;
        }
    }

    /**
     * 设置一组validator。
     */
    public void setValidators(List<Validator> validators) {
        if (validators != null) {
            initValidatorList();
            this.validators.addAll(validators);
        }
    }

    private void initValidatorList() {
        validators = createArrayList();
        validatorList = unmodifiableList(validators);
    }

    /**
     * 将指定field中的内容复制到当前field中。
     */
    void mergeWith(FieldConfigImpl src) {
        if (name == null) {
            setName(src.name);
        }

        if (displayName == null) {
            setDisplayName(src.displayName);
        }

        if (isEmptyArray(defaultValues)) {
            setDefaultValues(src.defaultValues);
        }

        if (trimming == null) {
            trimming = src.trimming;
        }

        if (propertyName == null) {
            setPropertyName(src.propertyName);
        }

        if (validators == null) {
            initValidatorList();
        }

        for (Validator validator : src.getValidators()) {
            validators.add(validator.clone());
        }
    }

    /**
     * 转换成易于阅读的字符串。
     */
    @Override
    public String toString() {
        String groupName = groupConfig == null ? null : groupConfig.getName();
        return "FieldConfig[group: " + groupName + ", name: " + getName() + ", validators: " + getValidators().size()
                + "]";
    }
}
