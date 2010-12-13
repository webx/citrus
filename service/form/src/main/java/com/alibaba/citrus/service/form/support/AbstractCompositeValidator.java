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
import static com.alibaba.citrus.util.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.ListIterator;

import com.alibaba.citrus.service.form.MessageContext;
import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.configuration.FieldConfig;
import com.alibaba.citrus.service.form.impl.MessageContextFactory;
import com.alibaba.citrus.service.form.impl.ValidatorContextImpl;

/**
 * 组合式的validator基类。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractCompositeValidator extends AbstractValidator {
    private final List<Validator> validators = createLinkedList();
    private final List<Validator> validatorList = unmodifiableList(validators);

    /**
     * 取得子validators。
     */
    public List<Validator> getValidators() {
        return validatorList;
    }

    /**
     * 设置子validators。
     */
    public void setValidators(List<Validator> validators) {
        this.validators.clear();

        if (validators != null) {
            for (Validator validator : validators) {
                this.validators.add(assertNotNull(validator, "validator"));
            }
        }
    }

    /**
     * 当GroupConfig被初始化完成以后被调用，此时可取得同组中其它的fields。
     * <p>
     * 所有validator的同名方法将被调用。
     * </p>
     */
    @Override
    public void init(FieldConfig fieldConfig) throws Exception {
        super.init(fieldConfig);

        for (Validator validator : validators) {
            validator.init(fieldConfig);
        }
    }

    /**
     * 深度复制validators。
     */
    @Override
    public Validator clone() {
        AbstractCompositeValidator copy = (AbstractCompositeValidator) super.clone();

        for (ListIterator<Validator> i = copy.validators.listIterator(); i.hasNext();) {
            i.set(i.next().clone());
        }

        return copy;
    }

    /**
     * 创建一个和指定validator相关的context。
     */
    protected final Context newContext(Context context, Validator validator) {
        return newContext(context, validator, context.getValue());
    }

    /**
     * 创建一个和指定validator相关的context，使用指定的value。
     */
    protected final Context newContext(Context context, Validator validator, Object value) {
        MessageContext expressionContext = MessageContextFactory.newInstance(context.getField(), validator);
        expressionContext.copyLocalContext(context.getMessageContext());
        return new ValidatorContextImpl(expressionContext, context.getField(), value);
    }
}
