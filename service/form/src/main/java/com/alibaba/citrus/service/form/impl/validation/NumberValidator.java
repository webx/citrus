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
package com.alibaba.citrus.service.form.impl.validation;

import static com.alibaba.citrus.service.form.support.CompareOperator.*;
import static com.alibaba.citrus.util.StringUtil.*;

import com.alibaba.citrus.service.form.support.AbstractNumberValidator;
import com.alibaba.citrus.service.form.support.AbstractValidatorDefinitionParser;
import com.alibaba.citrus.service.form.support.CompareOperator;
import com.alibaba.citrus.service.form.support.NumberSupport;

/**
 * 验证用户输入的是正确的数字格式，并且数字符合限定的范围。
 * 
 * @author Michael Zhou
 */
public class NumberValidator extends AbstractNumberValidator {
    private NumberSupport[] operands = new NumberSupport[CompareOperator.values().length];

    /**
     * 取得限定值：等于。
     */
    public String getEqualTo() {
        return getOperandString(equalTo);
    }

    /**
     * 设置限定值：等于。
     */
    public void setEqualTo(String value) {
        setOperand(equalTo, value);
    }

    /**
     * 取得限定值：不等于。
     */
    public String getNotEqualTo() {
        return getOperandString(notEqualTo);
    }

    /**
     * 设置限定值：不等于。
     */
    public void setNotEqualTo(String value) {
        setOperand(notEqualTo, value);
    }

    /**
     * 取得限定值：小于。
     */
    public String getLessThan() {
        return getOperandString(lessThan);
    }

    /**
     * 设置限定值：小于。
     */
    public void setLessThan(String value) {
        setOperand(lessThan, value);
    }

    /**
     * 取得限定值：大于。
     */
    public String getGreaterThan() {
        return getOperandString(greaterThan);
    }

    /**
     * 设置限定值：大于。
     */
    public void setGreaterThan(String value) {
        setOperand(greaterThan, value);
    }

    /**
     * 取得限定值：小于等于。
     */
    public String getLessThanOrEqualTo() {
        return getOperandString(lessThanOrEqualTo);
    }

    /**
     * 设置限定值：小于等于。
     */
    public void setLessThanOrEqualTo(String value) {
        setOperand(lessThanOrEqualTo, value);
    }

    /**
     * 取得限定值：大于等于。
     */
    public String getGreaterThanOrEqualTo() {
        return getOperandString(greaterThanOrEqualTo);
    }

    /**
     * 设置限定值：大于等于。
     */
    public void setGreaterThanOrEqualTo(String value) {
        setOperand(greaterThanOrEqualTo, value);
    }

    private String getOperandString(CompareOperator op) {
        NumberSupport n = getOperand(op);
        return n == null ? null : n.getStringValue();
    }

    public final NumberSupport getOperand(CompareOperator op) {
        return operands[op.ordinal()];
    }

    protected final void setOperand(CompareOperator op, String value) {
        operands[op.ordinal()] = new NumberSupport(null, trimToNull(value));
    }

    @Override
    protected void init() throws Exception {
        super.init();

        // parse operands, throws NumberFormatException
        for (NumberSupport operand : operands) {
            if (operand != null) {
                operand.setNumberType(getNumberType());
                operand.getValue();
            }
        }
    }

    /**
     * 验证一个字段。
     */
    @Override
    protected boolean validate(Context context, String value) {
        NumberSupport numberValue = new NumberSupport(getNumberType(), value);
        boolean valid = true;

        try {
            numberValue.getValue();
        } catch (NumberFormatException e) {
            valid = false;
        }

        for (int i = 0; i < operands.length; i++) {
            if (operands[i] != null) {
                valid &= CompareOperator.values()[i].accept(numberValue.compareTo(operands[i]));
            }
        }

        return valid;
    }

    public static class DefinitionParser extends AbstractValidatorDefinitionParser<NumberValidator> {
    }
}
