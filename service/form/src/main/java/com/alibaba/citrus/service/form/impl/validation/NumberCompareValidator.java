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
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.form.configuration.FieldConfig;
import com.alibaba.citrus.service.form.support.AbstractNumberValidator;
import com.alibaba.citrus.service.form.support.AbstractValidatorDefinitionParser;
import com.alibaba.citrus.service.form.support.CompareOperator;
import com.alibaba.citrus.service.form.support.NumberSupport;

/**
 * 和另一个field比较数字的validator。
 * 
 * @author Michael Zhou
 */
public class NumberCompareValidator extends AbstractNumberValidator {
    private String fieldName;
    private CompareOperator op;

    /**
     * 取得要比较的field名称。
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * 取得比较操作的类型。
     */
    public CompareOperator getOp() {
        return op;
    }

    /**
     * 设置等于操作。
     */
    public void setEqualTo(String fieldName) {
        setFieldName(equalTo, fieldName);
    }

    /**
     * 设置不等于操作。
     */
    public void setNotEqualTo(String fieldName) {
        setFieldName(notEqualTo, fieldName);
    }

    /**
     * 设置小于操作。
     */
    public void setLessThan(String fieldName) {
        setFieldName(lessThan, fieldName);
    }

    /**
     * 设置大于操作。
     */
    public void setGreaterThan(String fieldName) {
        setFieldName(greaterThan, fieldName);
    }

    /**
     * 设置小于等于操作。
     */
    public void setLessThanOrEqualTo(String fieldName) {
        setFieldName(lessThanOrEqualTo, fieldName);
    }

    /**
     * 设置大于等于操作。
     */
    public void setGreaterThanOrEqualTo(String fieldName) {
        setFieldName(greaterThanOrEqualTo, fieldName);
    }

    private void setFieldName(CompareOperator op, String fieldName) {
        this.op = op;
        this.fieldName = trimToNull(fieldName);
    }

    /**
     * 验证参数并初始化。
     * <p>
     * 此初始化方法可以取到同组中的其它fieldConfig。
     * </p>
     */
    @Override
    public void init(FieldConfig fieldConfig) throws Exception {
        super.init(fieldConfig);

        if (fieldName == null || op == null) {
            throw new IllegalArgumentException("One of the following attributes should be set: "
                    + asList(CompareOperator.values()));
        }

        assertNotNull(fieldConfig.getGroupConfig().getFieldConfig(fieldName), "Field %s not exists", fieldName);
    }

    /**
     * 验证一个字段。
     */
    @Override
    protected boolean validate(Context context, String value) {
        Field fieldToCompare = assertNotNull(context.getField(fieldName), "field not found");
        NumberSupport thisValue = new NumberSupport(getNumberType(), value);
        NumberSupport otherValue = new NumberSupport(getNumberType(), fieldToCompare.getStringValue());

        try {
            return getOp().accept(thisValue.compareTo(otherValue));
        } catch (IllegalArgumentException e) {
            return false; // 包括NumberFormatException。如果getValue失败，验证失败。
        }
    }

    public static class DefinitionParser extends AbstractValidatorDefinitionParser<NumberCompareValidator> {
    }
}
