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
package com.alibaba.citrus.service.form.impl;

import static com.alibaba.citrus.service.form.FormConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;

import com.alibaba.citrus.service.form.Form;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.service.form.MessageContext;
import com.alibaba.citrus.service.form.configuration.FormConfig;
import com.alibaba.citrus.service.form.configuration.GroupConfig;
import com.alibaba.citrus.util.StringUtil;

/**
 * 代表一个用户提交的form信息。
 * <p>
 * 注意：form对象不是线程安全的，不能被多线程共享。
 * </p>
 * 
 * @author Michael Zhou
 */
public class FormImpl implements Form {
    protected static final Logger log = LoggerFactory.getLogger(Form.class);
    private final FormConfig formConfig;
    private final String formKey;
    private final boolean forcePostOnly;
    private final Map<String, Group> groups = createLinkedHashMap();
    private final Collection<Group> groupList = Collections.unmodifiableCollection(groups.values());
    private final MessageContext messageContext;
    private boolean valid;
    private SimpleTypeConverter typeConverter;

    /**
     * 创建一个新form。
     */
    public FormImpl(FormConfig formConfig, String formKey, boolean forcePostOnly) {
        this.formConfig = formConfig;
        this.formKey = formKey;
        this.messageContext = MessageContextFactory.newInstance(this);
        this.forcePostOnly = forcePostOnly;
    }

    /**
     * 取得form的配置信息。
     */
    public FormConfig getFormConfig() {
        return formConfig;
    }

    /**
     * 取得用于转换类型的converter。
     */
    public TypeConverter getTypeConverter() {
        if (typeConverter == null) {
            typeConverter = new SimpleTypeConverter();
            getFormConfig().getPropertyEditorRegistrar().registerCustomEditors(typeConverter);
        }

        return typeConverter;
    }

    /**
     * 是否强制为只接受post表单。
     */
    public boolean isForcePostOnly() {
        return forcePostOnly;
    }

    /**
     * 判定form是否通过验证。
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * 设置form的合法性。该值将被叠加到当前的状态中：<code>this.valid &= valid</code>
     */
    protected void setValid(boolean valid) {
        this.valid &= valid;
    }

    /**
     * 初始化form，将form恢复成“未验证”状态。随后，调用者可以重新设置值并手工验证表单。
     */
    public void init() {
        init(null);
    }

    /**
     * 用request初始化form。假如request为<code>null</code>，则将form设置成“未验证”状态，否则，验证表单。
     */
    public void init(HttpServletRequest request) {
        valid = true;

        // 清除所有group
        groups.clear();

        if (request != null) {
            Set<String> ignoredGroups = createHashSet();
            boolean logStarted = false;

            // 扫描用户submit过来的所有form参数，找到符合格式的key：formKey.groupKey.instanceKey.fieldKey
            @SuppressWarnings("unchecked")
            Enumeration<String> e = request.getParameterNames();

            while (e.hasMoreElements()) {
                String key = e.nextElement();
                String[] keyInfo = parseParameterKey(key);

                // keyInfo为null表示该参数不是从form service生成的，忽略之
                if (keyInfo != null && isEquals(keyInfo[0], formKey)) {
                    if (!logStarted) {
                        logStarted = true;
                        log.debug("Initializing user-submitted form for validating");
                    }

                    String groupKey = keyInfo[1];
                    String instanceKey = keyInfo[2];
                    String groupInstanceKey = getGroupInstanceKey(groupKey, instanceKey);

                    // 下面从request中初始化所有group instance，
                    // 并确保不会重复初始化同一个group instance。
                    if (!groups.containsKey(groupInstanceKey) && !ignoredGroups.contains(groupInstanceKey)) {
                        GroupConfig groupConfig = getFormConfig().getGroupConfigByKey(groupKey);

                        if (groupConfig == null) {
                            log.debug("No group associated with parameter: {}", key);
                            continue;
                        } else if ((forcePostOnly || groupConfig.isPostOnly())
                                && !"post".equalsIgnoreCase(request.getMethod())) {
                            log.warn("Group {} can only read from POST request: {}", groupConfig.getName(), key);
                            ignoredGroups.add(groupInstanceKey);
                            setValid(false);
                            continue;
                        } else {
                            if (log.isDebugEnabled()) {
                                if (DEFAULT_GROUP_INSTANCE_KEY.equals(instanceKey)) {
                                    log.debug("Initializing form group: {}", groupConfig.getName());
                                } else {
                                    log.debug("Initializing form group: {}[{}]", groupConfig.getName(), instanceKey);
                                }
                            }

                            Group group = new GroupImpl(groupConfig, this, instanceKey);

                            groups.put(groupInstanceKey, group);
                            group.init(request);
                        }
                    }
                }
            }
        }
    }

    /**
     * 解析从URL中传过来的key，如果解析成功，则返回相应的groupKey，instanceKey和fieldKey，否则返回
     * <code>null</code>。
     */
    private String[] parseParameterKey(String paramKey) {
        if (!paramKey.startsWith(FORM_KEY_PREFIX)) {
            return null;
        }

        String[] parts = StringUtil.split(paramKey, FIELD_KEY_SEPARATOR);

        if (parts.length < 4) {
            return null;
        }

        return parts;
    }

    /**
     * 取得group instance的key，用来索引所有group instance。
     */
    private String getGroupInstanceKey(String groupKey, String instanceKey) {
        return groupKey + '.' + instanceKey;
    }

    /**
     * 验证（或重新验证）当前的所有group instance。
     */
    public void validate() {
        valid = true;

        for (Group group : getGroups()) {
            group.validate();
        }
    }

    /**
     * 取得代表form的key。
     */
    public String getKey() {
        return formKey;
    }

    /**
     * 取得所有group的列表。
     */
    public Collection<Group> getGroups() {
        return groupList;
    }

    /**
     * 取得所有指定名称的group的列表。group名称大小写不敏感。
     */
    public Collection<Group> getGroups(String groupName) {
        List<Group> resultGroups = createArrayList(groups.size());

        for (Group group : groups.values()) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                resultGroups.add(group);
            }
        }

        return resultGroups;
    }

    /**
     * 取得默认的group instance。如果该group instance不存在，则创建之。Group名称大小写不敏感。
     */
    public Group getGroup(String groupName) {
        return getGroup(groupName, null, true);
    }

    /**
     * 取得group instance。如果该group instance不存在，则创建之。Group名称大小写不敏感。
     */
    public Group getGroup(String groupName, String instanceKey) {
        return getGroup(groupName, instanceKey, true);
    }

    /**
     * 取得group instance。如果该group instance不存在，并且<code>create == true</code>
     * ，则创建之。Group名称大小写不敏感。
     */
    public Group getGroup(String groupName, String instanceKey, boolean create) {
        GroupConfig groupConfig = getFormConfig().getGroupConfig(groupName);

        if (groupConfig == null) {
            return null;
        }

        instanceKey = defaultIfNull(trimToNull(instanceKey), DEFAULT_GROUP_INSTANCE_KEY);

        String groupInstanceKey = getGroupInstanceKey(groupConfig.getKey(), instanceKey);
        Group group = groups.get(groupInstanceKey);

        if (group == null && create) {
            group = new GroupImpl(groupConfig, this, instanceKey);
            groups.put(groupInstanceKey, group);
            group.init();
        }

        return group;
    }

    /**
     * 取得form级别的错误信息表达式的context，包含常用小工具和所有系统属性。
     */
    protected MessageContext getMessageContext() {
        return messageContext;
    }

    /**
     * 转换成易于阅读的字符串。
     */
    @Override
    public String toString() {
        return "Form[groups: " + getFormConfig().getGroupConfigList().size() + ", group instances: "
                + getGroups().size() + ", valid: " + isValid() + "]";
    }
}
