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

import java.util.Map;

import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.form.support.AbstractOptionalValidator;

public class MultiFieldsValidator extends AbstractOptionalValidator {
    private Map<String, String> fieldValues;

    public Map<String, String> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(Map<String, String> fieldValues) {
        this.fieldValues = fieldValues;
    }

    @Override
    protected boolean validate(Context context, String value) {
        if (fieldValues != null) {
            for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
                Field f = context.getField(entry.getKey());

                if (!f.getStringValue().equals(entry.getValue())) {
                    return false;
                }
            }
        }

        return true;
    }
}
