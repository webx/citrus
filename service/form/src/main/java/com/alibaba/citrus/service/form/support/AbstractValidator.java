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
package com.alibaba.citrus.service.form.support;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;

import com.alibaba.citrus.expr.Expression;
import com.alibaba.citrus.expr.ExpressionContext;
import com.alibaba.citrus.expr.ExpressionFactory;
import com.alibaba.citrus.expr.ExpressionParseException;
import com.alibaba.citrus.expr.composite.CompositeExpressionFactory;
import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.configuration.FieldConfig;
import com.alibaba.citrus.service.form.configuration.FormConfig;
import com.alibaba.citrus.service.form.configuration.GroupConfig;
import com.alibaba.citrus.springext.support.BeanSupport;
import com.alibaba.citrus.util.ObjectUtil;
import com.alibaba.citrus.util.StringEscapeUtil;
import com.alibaba.citrus.util.i18n.LocaleUtil;

/**
 * 抽象的<code>Validator</code>实现。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractValidator extends BeanSupport implements Validator, MessageSourceAware {
    protected static final ExpressionFactory EXPRESSION_FACTORY = new CompositeExpressionFactory();
    private String id;
    private String messageCode;
    private Message message;
    private MessageSource messageSource;

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 是否需要检查message值？
     * <p>
     * 如果是，则message未指定时将报错。
     * </p>
     */
    protected boolean requiresMessage() {
        return true;
    }

    /**
     * 当GroupConfig被初始化完成以后被调用，此时可取得同组中其它的fields。
     */
    public void init(FieldConfig fieldConfig) throws Exception {
        if (requiresMessage()) {
            boolean hasMessage = false;

            // 1. 从messageSource中查找
            if (id != null && messageSource != null) {
                GroupConfig groupConfig = fieldConfig.getGroupConfig();
                FormConfig formConfig = groupConfig.getFormConfig();

                // form.groupName.fieldName.validatorId
                messageCode = formConfig.getMessageCodePrefix() + groupConfig.getName() + "." + fieldConfig.getName()
                        + "." + id;

                hasMessage = getMessageFromMessageSource() != null;
            }

            // 2. 如果messageSource中找不到，则validator必须设置message
            if (!hasMessage) {
                assertNotNull(message, "no message");
                message.compile();
            }
        }
    }

    private String getMessageFromMessageSource() {
        try {
            return messageSource.getMessage(messageCode, null, LocaleUtil.getContext().getLocale());
        } catch (NoSuchMessageException e) {
            return null;
        }
    }

    /**
     * 取得validator的ID，通过该ID可以找到指定的validator。
     */
    public String getId() {
        return id == null ? getBeanName() : id;
    }

    /**
     * 设置validator的ID，通过该ID可以找到指定的validator。
     */
    public void setId(String id) {
        this.id = trimToNull(id);
    }

    /**
     * 取得出错信息。
     */
    public final String getMessage(Context context) {
        // 首先，假如message已经被设置，则直接返回。
        // 例如all-of-validator就会设置这个message。
        String result = trimToNull(context.getMessage());

        if (result == null) {
            // 然后，试着查找message source（仅当id存在）
            Message message = this.message;

            if (messageCode != null && messageSource != null) {
                String messageFromMessageSource = getMessageFromMessageSource();

                if (messageFromMessageSource != null) {
                    message = new Message(messageFromMessageSource);
                    message.compile();
                }
            }

            // 渲染message
            if (message != null) {
                result = message.getMessageString(context.getMessageContext());
            }
        }

        return result;
    }

    /**
     * 设置出错信息。
     */
    public void setMessage(String message) {
        this.message = new Message(message);
    }

    /**
     * 生成副本。
     */
    @Override
    public Validator clone() {
        try {
            return (Validator) super.clone();
        } catch (CloneNotSupportedException e) {
            return null; // 不可能发生！
        }
    }

    /**
     * 代表一个message表达式。
     */
    protected static class Message implements Cloneable {
        private String message;
        private Expression messageExpression;

        public Message(String message) {
            this.message = trimToNull(message);
        }

        /**
         * 编译表达式。
         */
        public void compile() {
            assertNotNull(message, "message");

            try {
                messageExpression = EXPRESSION_FACTORY.createExpression(message);
            } catch (ExpressionParseException e) {
                throw new IllegalArgumentException("Invalid message for validator " + getClass().getSimpleName()
                        + ": \"" + StringEscapeUtil.escapeJava(message) + "\"");
            }
        }

        /**
         * 取得出错信息。
         */
        public String getMessageString(ExpressionContext context) {
            return ObjectUtil.toString(messageExpression.evaluate(context), "");
        }

        @Override
        public String toString() {
            return "ValidatorMessage[" + message + "]";
        }
    }
}
