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

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.form.MessageContext;
import com.alibaba.citrus.service.form.Validator;

/**
 * 携带着validator验证所必须的上下文信息。
 * 
 * @author Michael Zhou
 */
public class ValidatorContextImpl implements Validator.Context {
    private final MessageContext expressionContext;
    private final Field field;
    private String message;
    private Object value;

    public ValidatorContextImpl(MessageContext expressionContext, Field field) {
        this(expressionContext, field, null);
    }

    public ValidatorContextImpl(MessageContext expressionContext, Field field, Object value) {
        this.expressionContext = assertNotNull(expressionContext, "expressionContext");
        this.field = assertNotNull(field, "field");
        this.value = value == null ? field.getValue() : value;
    }

    public MessageContext getMessageContext() {
        return expressionContext;
    }

    public Field getField() {
        return field;
    }

    public Field getField(String fieldName) {
        return field.getGroup().getField(fieldName);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getValue() {
        return value;
    }

    public <T> T getValueAsType(Class<T> type) {
        return type.cast(field.getGroup().getForm().getTypeConverter().convertIfNecessary(value, type));
    }

    @Override
    public String toString() {
        return "ValidatorContext[" + field + "]";
    }
}
