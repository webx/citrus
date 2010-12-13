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

import com.alibaba.citrus.service.form.support.AbstractOptionalValidator;
import com.alibaba.citrus.service.form.support.AbstractValidatorDefinitionParser;

/**
 * 检查输入值的字符串长度。
 * 
 * @author Michael Zhou
 */
public class StringLengthValidator extends AbstractOptionalValidator {
    private int minLength = 0;
    private int maxLength = -1;

    /**
     * 取得最短长度。
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * 设置最短长度。
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * 取得最大长度。
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * 设置最大长度。
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    protected boolean validate(Context context, String value) {
        int length = getLength(value);

        if (minLength >= 0 && length < minLength) {
            return false;
        }

        if (maxLength >= 0 && length > maxLength) {
            return false;
        }

        return true;
    }

    protected int getLength(String value) {
        return value.length();
    }

    public static class DefinitionParser extends AbstractValidatorDefinitionParser<StringLengthValidator> {
    }
}
