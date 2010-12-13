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

import static com.alibaba.citrus.util.StringUtil.*;

import com.alibaba.citrus.service.form.support.AbstractValidator;

public class MyValidator2 extends AbstractValidator {
    private int validatorIndex = -1;

    public void setIndex(int validatorIndex) {
        this.validatorIndex = validatorIndex;
    }

    public boolean validate(Context context) {
        String value = trimToNull(context.getValueAsType(String.class));

        if (value == null) {
            return true;
        }

        if (validatorIndex >= 0) {
            value = value.split("[,\\s]+")[validatorIndex];
        }

        return Boolean.parseBoolean(value);
    }
}
