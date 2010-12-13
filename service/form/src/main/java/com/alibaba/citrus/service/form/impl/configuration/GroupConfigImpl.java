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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.Assert.ExceptionType.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.Map;

import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.configuration.FieldConfig;
import com.alibaba.citrus.service.form.configuration.FormConfig;
import com.alibaba.citrus.service.form.configuration.GroupConfig;

/**
 * 代表一个form group的定义信息。
 * 
 * @author Michael Zhou
 */
public class GroupConfigImpl extends AbstractConfig<GroupConfig> implements GroupConfig {
    private FormConfig formConfig;
    private String parentGroup;
    private String name;
    private String key;
    private Map<String, FieldConfigImpl> fields; // field name to fieldConfig
    private Map<String, FieldConfigImpl> fieldsByKey; // field key to fieldConfig
    private List<Import> imports; // imported groups
    private List<Import> importList; // unmodifiable imported groups
    private List<FieldConfig> fieldList; // unmodifiable field list
    private Boolean trimmingByDefault;
    private Boolean postOnly;

    /**
     * 取得group所属的form config。
     */
    public FormConfig getFormConfig() {
        return formConfig;
    }

    /**
     * 设置group所属的form config。
     */
    public void setFormConfig(FormConfig formConfig) {
        this.formConfig = formConfig;
    }

    /**
     * 取得group name。
     */
    public String getName() {
        return name;
    }

    /**
     * 设置group name。
     */
    public void setName(String name) {
        this.name = trimToNull(name);
    }

    /**
     * 取得group key。
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置group key。
     */
    public void setKey(String key) {
        this.key = trimToNull(key);
    }

    /**
     * 取得parent group。
     */
    public String getParentGroup() {
        return parentGroup;
    }

    /**
     * 设置parent group，所有parent group中的内容都会被加入到当前group中。
     */
    public void setParentGroup(String parentGroup) {
        this.parentGroup = trimToNull(parentGroup);
    }

    /**
     * 取得默认的trimming选项。
     */
    public boolean isTrimmingByDefault() {
        return trimmingByDefault == null ? true : trimmingByDefault.booleanValue();
    }

    /**
     * 设置默认的trimming选项。
     */
    public void setTrimmingByDefault(boolean trimmingByDefault) {
        this.trimmingByDefault = trimmingByDefault;
    }

    /**
     * Group是否必须从post请求中取得数据。
     */
    public boolean isPostOnly() {
        if (postOnly == null) {
            return formConfig == null ? true : formConfig.isPostOnlyByDefault();
        } else {
            return postOnly.booleanValue();
        }
    }

    /**
     * 设置group是否必须从post请求中取得数据。
     */
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    /**
     * 取得所有field config的列表。
     */
    public List<FieldConfig> getFieldConfigList() {
        if (fieldList == null) {
            return emptyList();
        } else {
            return fieldList;
        }
    }

    /**
     * 取得指定名称的field config。名称大小写不敏感。 如果未找到，则返回<code>null</code>。
     */
    public FieldConfig getFieldConfig(String fieldName) {
        if (fields == null) {
            return null;
        } else {
            return fields.get(caseInsensitiveName(fieldName));
        }
    }

    /**
     * 取得指定key对应的field config。如果未找到，则返回<code>null</code>。
     */
    public FieldConfig getFieldConfigByKey(String fieldKey) {
        return assertNotNull(fieldsByKey, ILLEGAL_STATE, "fieldsByKey not inited").get(fieldKey);
    }

    /**
     * 设置一组field configs。
     */
    public void setFieldConfigImplList(List<FieldConfigImpl> fieldConfigList) {
        if (fieldConfigList != null) {
            fields = createLinkedHashMap();

            for (FieldConfigImpl fieldConfig : fieldConfigList) {
                addFieldConfig(fieldConfig, true); // 大小写不敏感！
            }
        }
    }

    /**
     * 添加一个field config。
     */
    private void addFieldConfig(FieldConfigImpl fieldConfig, boolean checkDuplicate) {
        if (fields == null) {
            fields = createLinkedHashMap();
        }

        String fieldName = caseInsensitiveName(fieldConfig.getName()); // 大小写不敏感！

        if (checkDuplicate) {
            assertTrue(!fields.containsKey(fieldName), "Duplicated field name: \"%s.%s\"", getName(),
                    fieldConfig.getName());
        }

        fields.put(fieldName, fieldConfig);
    }

    /**
     * 取得所有的imports。
     */
    public List<Import> getImports() {
        if (importList == null) {
            return emptyList();
        } else {
            return importList;
        }
    }

    /**
     * 引进其它group的字段。如果fieldName为null，则引进整个group（同extends）。
     */
    public void setImports(List<Import> imports) {
        if (imports != null) {
            this.imports = createArrayList(imports);
            this.importList = unmodifiableList(this.imports);
        }
    }

    /**
     * 扩展当前group，将指定group中的内容复制到当前group中。
     */
    void extendsFrom(GroupConfigImpl parentGroupConfig) {
        if (trimmingByDefault == null && parentGroupConfig.trimmingByDefault != null) {
            trimmingByDefault = parentGroupConfig.trimmingByDefault;
        }

        if (postOnly == null && parentGroupConfig.postOnly != null) {
            postOnly = parentGroupConfig.postOnly;
        }

        extendsOrImports(parentGroupConfig, null, false);
    }

    /**
     * 将指定group中的内容复制到当前group中。
     */
    void importsFrom(GroupConfigImpl srcGroupConfig, String fieldName) {
        extendsOrImports(srcGroupConfig, fieldName, true);
    }

    /**
     * 扩展或引入fields。
     */
    private void extendsOrImports(GroupConfigImpl srcGroupConfig, String fieldName, boolean checkDuplicate) {
        if (fieldName == null) {
            // merge/import all
            for (FieldConfig srcFieldConfig : srcGroupConfig.getFieldConfigList()) {
                mergeField((FieldConfigImpl) srcFieldConfig, checkDuplicate);
            }
        } else {
            // merge/import single field
            FieldConfig srcFieldConfig = srcGroupConfig.getFieldConfig(fieldName);
            assertNotNull(srcFieldConfig, "Field \"%s.%s\" not found", srcGroupConfig.getName(), fieldName);
            mergeField((FieldConfigImpl) srcFieldConfig, checkDuplicate);
        }
    }

    /**
     * 合并field。
     */
    private void mergeField(FieldConfigImpl srcFieldConfig, boolean checkDuplicate) {
        FieldConfigImpl copy = (FieldConfigImpl) getFieldConfig(srcFieldConfig.getName());

        if (copy == null) {
            // 如果当前group中未定义同名的field，则创建之
            copy = new FieldConfigImpl();
            copy.setGroupConfig(this);
        }

        copy.mergeWith(srcFieldConfig);

        // 如果当前group中已经定义了同名的field，那么，
        // 当checkDuplicate==false时，合并field（extends group的情形），
        // 当checkDuplicate==true时，报错（imports field的情形）
        addFieldConfig(copy, checkDuplicate);
    }

    /**
     * 初始化group config。
     * <p>
     * 不同于<code>init()</code>方法，此方法是被<code>formConfig.init()</code>调用。
     * </p>
     */
    void init2() throws Exception {
        assertNotNull(fields, "no fields");

        fieldsByKey = createHashMap();
        fieldList = createArrayList(fields.size());

        for (Map.Entry<String, FieldConfigImpl> entry : fields.entrySet()) {
            String caseInsensitiveName = entry.getKey();
            FieldConfigImpl fieldConfig = entry.getValue();

            // 设置不重复的key
            for (int i = 1; i <= caseInsensitiveName.length(); i++) {
                String key = caseInsensitiveName.substring(0, i);

                if (!fieldsByKey.containsKey(key)) {
                    fieldConfig.setKey(key);
                    fieldsByKey.put(key, fieldConfig);
                    break;
                }
            }

            // 设置field.group
            fieldConfig.setGroupConfig(this);

            // 设置fieldList
            fieldList.add(fieldConfig);
        }

        fieldList = unmodifiableList(fieldList);

        // 初始化所有validators
        for (FieldConfig fieldConfig : fieldList) {
            for (Validator validator : fieldConfig.getValidators()) {
                validator.init(fieldConfig);
            }
        }
    }

    /**
     * 转换成易于阅读的字符串。
     */
    @Override
    public String toString() {
        return "GroupConfig[name: " + getName() + ", fields: " + getFieldConfigList().size() + "]";
    }

    /**
     * 代表import其它group中的field的信息。
     */
    public static final class ImportImpl implements Import {
        private final String groupName;
        private final String fieldName;

        public ImportImpl(String groupName, String fieldName) {
            this.groupName = trimToNull(groupName);
            this.fieldName = trimToNull(fieldName);
        }

        public String getGroupName() {
            return groupName;
        }

        public String getFieldName() {
            return fieldName;
        }

        @Override
        public String toString() {
            return fieldName == null ? groupName : groupName + "." + fieldName;
        }
    }
}
