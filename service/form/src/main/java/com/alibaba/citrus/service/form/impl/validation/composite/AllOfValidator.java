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
package com.alibaba.citrus.service.form.impl.validation.composite;

import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.support.AbstractCompositeValidator;
import com.alibaba.citrus.service.form.support.AbstractCompositeValidatorDefinitionParser;

/**
 * 只有当下属的所有validator均通过验证时，此validator才通过验证。
 * <p>
 * 当验证不通过时，message为第一个未通过验证的子validator的message。
 * </p>
 * 
 * @author Michael Zhou
 */
public class AllOfValidator extends AbstractCompositeValidator {
    /**
     * 不需要配置message，因为它的message是由子validator提供的。
     */
    @Override
    protected boolean requiresMessage() {
        return false;
    }

    public boolean validate(Context context) {
        for (Validator validator : getValidators()) {
            Context newContext = newContext(context, validator);

            if (!validator.validate(newContext)) {
                context.setMessage(validator.getMessage(newContext));
                return false;
            }
        }

        return true;
    }

    public static class DefinitionParser extends AbstractCompositeValidatorDefinitionParser<AllOfValidator> {
    }
}
