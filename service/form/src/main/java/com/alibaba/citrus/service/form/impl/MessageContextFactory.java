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

import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

import com.alibaba.citrus.expr.ExpressionContext;
import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.form.Form;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.service.form.MessageContext;
import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.util.Utils;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

public class MessageContextFactory {
    /**
     * 设置form级别的错误信息context，包含下列内容：
     * <ol>
     * <li>所有系统属性：<code>System.getProperties()</code>。</li>
     * <li>常用小工具如：<code>stringUtil</code>和<code>stringEscapeUtil</code>等。</li>
     * </ol>
     */
    public static MessageContext newInstance(final Form form) {
        MessageContext formContext = new MessageContext() {
            private static final long serialVersionUID = 3833185835016140853L;

            @Override
            protected Object internalGet(String key) {
                return null;
            }

            @Override
            public ExpressionContext getParentContext() {
                return null;
            }

            @Override
            protected void buildToString(ToStringBuilder sb) {
                sb.append("FormMessageContext");
            }

            @Override
            protected void buildToString(MapBuilder mb) {
                mb.append("form", form);
            }
        };

        Map<String, Object> utils = Utils.getUtils();

        formContext.putAll(System.getProperties());
        formContext.putAll(utils);

        return formContext;
    }

    /**
     * 设置group级别的错误信息context，包含下列内容：
     * <ol>
     * <li>Form级别的context的所有内容。</li>
     * <li><code>form</code>指向当前表单对象</li>
     * <li>Group中的所有field。</li>
     * </ol>
     */
    public static MessageContext newInstance(final Group group) {
        MessageContext groupContext = new MessageContext() {
            private static final long serialVersionUID = 3258407326913149238L;

            @Override
            protected Object internalGet(String key) {
                Object value = null;

                // 查找fields
                value = group.getField(key);

                if (value == null) {
                    if ("form".equals(key)) {
                        value = group.getForm();
                    }
                }

                return value;
            }

            @Override
            public ExpressionContext getParentContext() {
                return ((FormImpl) group.getForm()).getMessageContext();
            }

            @Override
            protected void buildToString(ToStringBuilder sb) {
                sb.append("GroupMessageContext");
            }

            @Override
            protected void buildToString(MapBuilder mb) {
                mb.append("group", group);
            }
        };

        return groupContext;
    }

    /**
     * 设置field级别的错误信息context，包含下列内容：
     * <ol>
     * <li>Group级别的context的所有内容。</li>
     * <li><code>group</code>指向当前组对象</li>
     * <li>Field对象的属性，如：<code>displayName</code>、<code>value</code>和
     * <code>values</code>、<code>defaultValue</code>和<code>defaultValues</code>。
     * </li>
     * </ol>
     */
    public static MessageContext newInstance(final Field field) {
        MessageContext fieldContext = new MessageContext() {
            private static final long serialVersionUID = 3258130258607026229L;
            private BeanWrapper fieldWrapper;

            @Override
            protected Object internalGet(String key) {
                if (fieldWrapper == null) {
                    fieldWrapper = new BeanWrapperImpl(field);
                    field.getGroup().getForm().getFormConfig().getPropertyEditorRegistrar()
                            .registerCustomEditors(fieldWrapper);
                }

                // 在field实例中查找property
                try {
                    return fieldWrapper.getPropertyValue(key);
                } catch (BeansException e) {
                    return null;
                }
            }

            @Override
            public ExpressionContext getParentContext() {
                return ((GroupImpl) field.getGroup()).getMessageContext();
            }

            @Override
            protected void buildToString(ToStringBuilder sb) {
                sb.append("FieldMessageContext");
            }

            @Override
            protected void buildToString(MapBuilder mb) {
                mb.append("field", field);
            }
        };

        return fieldContext;
    }

    /**
     * 设置validator级别的错误信息context，包含下列内容：
     * <ol>
     * <li>Field级别的context的所有内容。</li>
     * <li>Validator对象的所有属性。</li>
     * </ol>
     */
    public static MessageContext newInstance(final Field field, final Validator validator) {
        MessageContext validatorContext = new MessageContext() {
            private static final long serialVersionUID = 3616450081390475317L;
            private BeanWrapper validatorWrapper;

            @Override
            protected Object internalGet(String key) {
                if (validatorWrapper == null) {
                    validatorWrapper = new BeanWrapperImpl(validator);
                    field.getGroup().getForm().getFormConfig().getPropertyEditorRegistrar()
                            .registerCustomEditors(validatorWrapper);
                }

                // 在validator object中查找property
                try {
                    return validatorWrapper.getPropertyValue(key);
                } catch (BeansException e) {
                    return null;
                }
            }

            @Override
            public ExpressionContext getParentContext() {
                return ((FieldImpl) field).getMessageContext();
            }

            @Override
            protected void buildToString(ToStringBuilder sb) {
                sb.append("ValidatorMessageContext");
            }

            @Override
            protected void buildToString(MapBuilder mb) {
                mb.append("validator", validator);
            }
        };

        return validatorContext;
    }
}
