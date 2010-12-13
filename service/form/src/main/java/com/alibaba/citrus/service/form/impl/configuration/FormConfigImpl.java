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

import static com.alibaba.citrus.service.form.FormConstant.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.Assert.ExceptionType.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.PropertyEditorRegistrar;

import com.alibaba.citrus.service.configuration.support.PropertyEditorRegistrarsSupport;
import com.alibaba.citrus.service.form.FormService;
import com.alibaba.citrus.service.form.configuration.FormConfig;
import com.alibaba.citrus.service.form.configuration.GroupConfig;
import com.alibaba.citrus.service.form.configuration.GroupConfig.Import;

/**
 * 实现<code>FormConfig</code>。
 * 
 * @author Michael Zhou
 */
public class FormConfigImpl extends AbstractConfig<FormConfig> implements FormConfig {
    private FormService formService;
    private Map<String, GroupConfigImpl> groups; // group name to groupConfig
    private Map<String, GroupConfigImpl> groupsByKey; // group key to groupConfig
    private List<GroupConfig> groupList; // unmodifiable group list
    private PropertyEditorRegistrarsSupport propertyEditorRegistrars = new PropertyEditorRegistrarsSupport();
    private Boolean converterQuiet;
    private Boolean postOnlyByDefault;
    private String messageCodePrefix;

    /**
     * 取得创建此form的service。
     */
    public FormService getFormService() {
        return formService;
    }

    /**
     * 设置创建此form的service。
     */
    public void setFormService(FormService formService) {
        this.formService = assertNotNull(formService, "formService");
    }

    /**
     * 类型转换出错时，是否不报错，而是返回默认值。
     */
    public boolean isConverterQuiet() {
        return converterQuiet == null ? true : converterQuiet.booleanValue();
    }

    /**
     * 设置类型转换出错时，是否不报错，而是返回默认值。
     */
    public void setConverterQuiet(boolean converterQuiet) {
        this.converterQuiet = converterQuiet;
    }

    /**
     * Group是否默认必须从post请求中取得数据。
     */
    public boolean isPostOnlyByDefault() {
        return postOnlyByDefault == null ? true : postOnlyByDefault.booleanValue();
    }

    /**
     * 设置group是否默认必须从post请求中取得数据。
     */
    public void setPostOnlyByDefault(boolean postOnlyByDefault) {
        this.postOnlyByDefault = postOnlyByDefault;
    }

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
    public String getMessageCodePrefix() {
        return messageCodePrefix == null ? FORM_MESSAGE_CODE_PREFIX : messageCodePrefix;
    }

    /**
     * 设置message code的前缀。
     */
    public void setMessageCodePrefix(String messageCodePrefix) {
        this.messageCodePrefix = normalizeMessageCodePrefix(messageCodePrefix);
    }

    private String normalizeMessageCodePrefix(String messageCodePrefix) {
        messageCodePrefix = trimToNull(messageCodePrefix);

        if (messageCodePrefix != null && !messageCodePrefix.endsWith(".")) {
            messageCodePrefix += ".";
        }

        return messageCodePrefix;
    }

    /**
     * 取得所有group config的列表。
     */
    public List<GroupConfig> getGroupConfigList() {
        if (groupList == null) {
            return emptyList();
        } else {
            return groupList;
        }
    }

    /**
     * 取得指定名称的group config。名称大小写不敏感。 如果未找到，则返回<code>null</code>。
     */
    public GroupConfig getGroupConfig(String groupName) {
        if (groups == null) {
            return null;
        } else {
            return groups.get(caseInsensitiveName(groupName));
        }
    }

    /**
     * 取得和指定key相对应的group config。如果未找到，则返回<code>null</code>
     */
    public GroupConfig getGroupConfigByKey(String groupKey) {
        return assertNotNull(groupsByKey, ILLEGAL_STATE, "groupsByKey not inited").get(groupKey);
    }

    /**
     * 设置group configs。
     */
    public void setGroupConfigImplList(List<GroupConfigImpl> groupConfigList) {
        if (groupConfigList != null) {
            groups = createLinkedHashMap();

            for (GroupConfigImpl groupConfig : groupConfigList) {
                String groupName = caseInsensitiveName(groupConfig.getName()); // 大小写不敏感！
                assertTrue(!groups.containsKey(groupName), "Duplicated group name: %s", groupConfig.getName());
                groups.put(groupName, groupConfig);
            }
        }
    }

    /**
     * 取得<code>PropertyEditor</code>注册器。
     * <p>
     * <code>PropertyEditor</code>负责将字符串值转换成bean property的类型，或反之。
     * </p>
     */
    public PropertyEditorRegistrar getPropertyEditorRegistrar() {
        return propertyEditorRegistrars;
    }

    /**
     * 设置一组<code>PropertyEditor</code>注册器。
     * <p>
     * <code>PropertyEditor</code>负责将字符串值转换成bean property的类型，或反之。
     * </p>
     */
    public void setPropertyEditorRegistrars(PropertyEditorRegistrar[] registrars) {
        propertyEditorRegistrars.setPropertyEditorRegistrars(registrars);
    }

    /**
     * 初始化form config。
     */
    @Override
    protected void init() throws Exception {
        // 初步初始化所有groups
        assertNotNull(groups, "no groups");

        groupsByKey = createHashMap();
        groupList = createArrayList(groups.size());

        for (Map.Entry<String, GroupConfigImpl> entry : groups.entrySet()) {
            String caseInsensitiveName = entry.getKey();
            GroupConfigImpl groupConfig = entry.getValue();

            // 设置不重复的key
            for (int i = 1; i <= caseInsensitiveName.length(); i++) {
                String key = caseInsensitiveName.substring(0, i);

                if (!groupsByKey.containsKey(key)) {
                    groupConfig.setKey(key);
                    groupsByKey.put(key, groupConfig);
                    break;
                }
            }

            // 设置group.form
            groupConfig.setFormConfig(this);

            // 设置groupList
            groupList.add(groupConfig);
        }

        groupList = unmodifiableList(groupList);

        // 处理group之间的继承关系，将parent group和imports中的内容展开到子group中。
        // 每个group.init2()将被调用。
        Set<GroupConfigImpl> processedGroups = createHashSet();
        GroupStack processingGroups = new GroupStack();

        for (GroupConfig groupConfig : getGroupConfigList()) {
            processGroup((GroupConfigImpl) groupConfig, processedGroups, processingGroups);
        }
    }

    /**
     * 处理group之间的继承关系，将parent group和imports中的内容展开到子group中。
     */
    private void processGroup(GroupConfigImpl groupConfig, Set<GroupConfigImpl> processedGroups,
                              GroupStack processingGroups) throws Exception {
        if (!processedGroups.contains(groupConfig)) {
            if (groupConfig.getParentGroup() != null || !groupConfig.getImports().isEmpty()) {
                // 防止循环继承或import
                if (processingGroups.contains(groupConfig)) {
                    StringBuilder buf = new StringBuilder();

                    for (GroupConfigImpl group : processingGroups) {
                        if (buf.length() == 0) {
                            buf.append("Cycle detected: ");
                        } else {
                            buf.append(" -> ");
                        }

                        buf.append(group.getName());
                    }

                    buf.append(" -> ").append(groupConfig.getName());

                    throw new IllegalArgumentException(buf.toString());
                }

                processingGroups.push(groupConfig);

                // 处理parentGroup
                if (groupConfig.getParentGroup() != null) {
                    copyFields(groupConfig, groupConfig.getParentGroup(), null, true, processedGroups, processingGroups);
                }

                // 处理imports
                for (Import impot : groupConfig.getImports()) {
                    copyFields(groupConfig, impot.getGroupName(), impot.getFieldName(), false, processedGroups,
                            processingGroups);
                }

                processingGroups.pop();
            }

            processedGroups.add(groupConfig); // 防止重复处理
            groupConfig.init2(); // 初始化group
        }
    }

    private void copyFields(GroupConfigImpl targetGroup, String srcGroupName, String srcFieldName, boolean isExtends,
                            Set<GroupConfigImpl> processedGroups, GroupStack processingGroups) throws Exception {
        GroupConfigImpl srcGroup = (GroupConfigImpl) assertNotNull(getGroupConfig(srcGroupName),
                "Parent or imported group name \"%s\" not found", srcGroupName);

        // 递归处理parentGroup和imported Groups。
        processGroup(srcGroup, processedGroups, processingGroups);

        // 将parentGroup或imported Groups中的所有内容复制到当前group中。
        if (isExtends) {
            targetGroup.extendsFrom(srcGroup);
        } else {
            targetGroup.importsFrom(srcGroup, srcFieldName);
        }
    }

    /**
     * 转换成易于阅读的字符串。
     */
    @Override
    public String toString() {
        return "FormConfig[groups: " + getGroupConfigList().size() + "]";
    }

    /**
     * 用来防止group递归继承。
     */
    private static class GroupStack implements Iterable<GroupConfigImpl> {
        private final LinkedList<GroupConfigImpl> groups = createLinkedList();

        public void push(GroupConfigImpl group) {
            groups.addLast(group);
        }

        public GroupConfigImpl pop() {
            return groups.removeLast();
        }

        public boolean contains(GroupConfigImpl group) {
            return groups.contains(group);
        }

        public Iterator<GroupConfigImpl> iterator() {
            return groups.iterator();
        }

        @Override
        public String toString() {
            return groups.toString();
        }
    }
}
