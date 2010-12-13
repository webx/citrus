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

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.CollectionFactory;
import org.springframework.core.MethodParameter;

import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.form.Form;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.service.form.InvalidGroupStateException;
import com.alibaba.citrus.service.form.MessageContext;
import com.alibaba.citrus.service.form.configuration.FieldConfig;
import com.alibaba.citrus.service.form.configuration.GroupConfig;
import com.alibaba.citrus.util.ObjectUtil;
import com.alibaba.citrus.util.StringUtil;

/**
 * 代表用户所提交表单中的一组字段。
 * <p>
 * 注意：group对象不是线程安全的，不能被多线程共享。
 * </p>
 * 
 * @author Michael Zhou
 */
public class GroupImpl implements Group {
    protected static final Logger log = LoggerFactory.getLogger(Group.class);
    private final GroupConfig groupConfig;
    private final Form form;
    private final String groupKey;
    private final String instanceKey;
    private final Map<String, Field> fields = createLinkedHashMap();
    private final Collection<Field> fieldList = Collections.unmodifiableCollection(fields.values());
    private final MessageContext messageContext;
    private boolean validated;
    private boolean valid;

    /**
     * 创建一个新group。
     */
    public GroupImpl(GroupConfig groupConfig, Form form, String instanceKey) {
        this.groupConfig = groupConfig;
        this.form = form;
        this.instanceKey = instanceKey;
        this.groupKey = form.getKey() + FIELD_KEY_SEPARATOR + groupConfig.getKey() + FIELD_KEY_SEPARATOR + instanceKey;
        this.messageContext = MessageContextFactory.newInstance(this);
    }

    /**
     * 取得group的配置信息。
     */
    public GroupConfig getGroupConfig() {
        return groupConfig;
    }

    /**
     * 取得包含此group的form。
     */
    public Form getForm() {
        return form;
    }

    /**
     * 取得group name，相当于<code>getGroupConfig().getName()</code>
     */
    public String getName() {
        return getGroupConfig().getName();
    }

    /**
     * 取得代表group的key。
     * <p>
     * 由固定前缀<code>"_fm"</code>，加上group名的缩写，再加上group instance key构成。例如：
     * <code>_fm.m._0</code>。
     * </p>
     */
    public String getKey() {
        return groupKey;
    }

    /**
     * 取得标识当前group的instance key。
     */
    public String getInstanceKey() {
        return instanceKey;
    }

    /**
     * 判定group是否通过验证。
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * 设置group的是否通过验证。
     * <p>
     * 注意：该值将被叠加到当前的状态中：<code>this.valid &= valid</code>
     * </p>
     */
    protected void setValid(boolean valid) {
        this.valid &= valid;
        ((FormImpl) getForm()).setValid(this.valid);
    }

    /**
     * 判定该group是否被置值，并验证。在两种情况下，<code>isValidated()</code>为<code>true</code>。
     * <ol>
     * <li>用户提交包含当前group字段的表单。此时相应的group被初始化并验证。</li>
     * <li>程序调用<code>validate()</code>方法。这种方式下，group中的字段值可以由程序来设置，效果如同用户提交表单一样。</li>
     * </ol>
     */
    public boolean isValidated() {
        return validated;
    }

    /**
     * 初始化group。
     */
    public void init() {
        init(null);
    }

    /**
     * 初始化group。 其中， <code>request</code>可以是<code>null</code>，如果
     * <code>request</code>不为<code>null</code>，则同时验证表单。
     */
    public void init(HttpServletRequest request) {
        fields.clear();
        valid = true;
        validated = request != null;

        for (FieldConfig fieldConfig : getGroupConfig().getFieldConfigList()) {
            Field field = new FieldImpl(fieldConfig, this);

            fields.put(StringUtil.toLowerCase(fieldConfig.getName()), field);
            field.init(request);
        }

        // 集中验证表单字段（有些validator需要读取多个字段的值，这样做是为了避免这些validator读不到在其后定义的field的值）
        if (request != null) {
            for (Field field : fields.values()) {
                ((FieldImpl) field).validate();
            }
        }
    }

    /**
     * 验证（或重新验证）当前的字段值。
     * <p>
     * 注意，此方法将设置<code>isValidated()</code>为<code>true</code>。
     * </p>
     */
    public void validate() {
        valid = true;
        validated = true;

        for (Field field : getFields()) {
            ((FieldImpl) field).validate();
        }
    }

    /**
     * 取得所有fields的列表。
     */
    public Collection<Field> getFields() {
        return fieldList;
    }

    /**
     * 取得指定名称的field。field名称（大小写不敏感）
     */
    public Field getField(String fieldName) {
        return fields.get(StringUtil.toLowerCase(fieldName));
    }

    /**
     * 取得group级别的错误信息表达式的context。
     */
    protected MessageContext getMessageContext() {
        return messageContext;
    }

    /**
     * 将对象中的属性值置入到fields中。
     * <p>
     * 对于<code>isValidated()</code>为<code>true</code>的group，该方法无效。
     * </p>
     */
    public void mapTo(Object object) {
        if (isValidated() || object == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Mapping properties to fields: group=\"{}\", object={}", getName(),
                    ObjectUtil.identityToString(object));
        }

        BeanWrapper bean = new BeanWrapperImpl(object);
        getForm().getFormConfig().getPropertyEditorRegistrar().registerCustomEditors(bean);

        for (Field field : getFields()) {
            String propertyName = field.getFieldConfig().getPropertyName();

            if (bean.isReadableProperty(propertyName)) {
                Object propertyValue = bean.getPropertyValue(propertyName);
                Class<?> propertyType = bean.getPropertyType(propertyName);
                PropertyEditor editor = bean.findCustomEditor(propertyType, propertyName);

                if (editor == null) {
                    editor = BeanUtils.findEditorByConvention(propertyType);
                }

                if (editor == null) {
                    if (propertyType.isArray() || CollectionFactory.isApproximableCollectionType(propertyType)) {
                        field.setValues((String[]) bean.convertIfNecessary(propertyValue, String[].class));
                    } else {
                        field.setValue(bean.convertIfNecessary(propertyValue, String.class));
                    }
                } else {
                    editor.setValue(propertyValue);
                    field.setValue(editor.getAsText());
                }
            } else {
                log.debug("No readable property \"{}\" found in type {}", propertyName, object.getClass().getName());
            }
        }
    }

    /**
     * 将group中的值置入指定对象。
     * <p>
     * 对于<code>isValidated()</code>为<code>false</code>的group，该方法无效。
     * </p>
     */
    public void setProperties(Object object) {
        if (!isValidated() || object == null) {
            return;
        }

        if (isValid()) {
            if (log.isDebugEnabled()) {
                log.debug("Set validated properties of group \"" + getName() + "\" to object "
                        + ObjectUtil.identityToString(object));
            }

            BeanWrapper bean = new BeanWrapperImpl(object);
            getForm().getFormConfig().getPropertyEditorRegistrar().registerCustomEditors(bean);

            for (Field field : getFields()) {
                String propertyName = field.getFieldConfig().getPropertyName();

                if (bean.isWritableProperty(propertyName)) {
                    PropertyDescriptor pd = bean.getPropertyDescriptor(propertyName);
                    MethodParameter mp = BeanUtils.getWriteMethodParameter(pd);
                    Object value = field.getValueOfType(pd.getPropertyType(), mp, null);

                    bean.setPropertyValue(propertyName, value);
                } else {
                    log.debug("No writable property \"{}\" found in type {}", propertyName, object.getClass().getName());
                }
            }
        } else {
            throw new InvalidGroupStateException("Attempted to call setProperties from an invalid input");
        }
    }

    /**
     * 转换成易于阅读的字符串。
     */
    @Override
    public String toString() {
        return "Group[name: " + getName() + "." + getInstanceKey() + ", fields: "
                + getGroupConfig().getFieldConfigList().size() + ", validated: " + isValidated() + ", valid: "
                + isValid() + "]";
    }
}
