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

import com.alibaba.citrus.service.form.support.AbstractValidator;
import com.alibaba.citrus.service.form.support.AbstractValidatorDefinitionParser;

/**
 * 判断多值field中，values的数量的validator。
 * 
 * @author Michael Zhou
 */
public class MultiValuesCountValidator extends AbstractValidator {
    private int minCount = 0;
    private int maxCount = -1;

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public boolean validate(Context context) {
        Object[] values = context.getField().getValues();

        if (minCount >= 0 && values.length < minCount) {
            return false;
        }

        if (maxCount >= 0 && values.length > maxCount) {
            return false;
        }

        return true;
    }

    public static class DefinitionParser extends AbstractValidatorDefinitionParser<MultiValuesCountValidator> {
    }
}
