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
import com.alibaba.citrus.service.form.support.AbstractOptionalValidator;
import com.alibaba.citrus.service.form.support.AbstractValidatorDefinitionParser;
import com.alibaba.citrus.service.form.support.CompareOperator;
import com.alibaba.citrus.util.StringUtil;

/**
 * 和另一个field比较字符串值的validator。
 * 
 * @author Michael Zhou
 */
public class StringCompareValidator extends AbstractOptionalValidator {
    private String fieldName;
    private CompareOperator op;
    private boolean ignoreCase;

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

    private void setFieldName(CompareOperator op, String fieldName) {
        this.op = op;
        this.fieldName = trimToNull(fieldName);
    }

    /**
     * 设置忽略大小写。
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
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
                    + asList(equalTo, notEqualTo));
        }

        assertNotNull(fieldConfig.getGroupConfig().getFieldConfig(fieldName), "Field %s not exists", fieldName);
    }

    /**
     * 验证一个字段。
     */
    @Override
    protected boolean validate(Context context, String value) {
        Field fieldToCompare = assertNotNull(context.getField(fieldName), "field not found");
        String thisValue = value;
        String otherValue = fieldToCompare.getStringValue();

        if (ignoreCase) {
            thisValue = StringUtil.toLowerCase(thisValue);
            otherValue = StringUtil.toLowerCase(otherValue);
        }

        return getOp().accept(thisValue.compareTo(otherValue));
    }

    public static class DefinitionParser extends AbstractValidatorDefinitionParser<StringCompareValidator> {
    }
}
