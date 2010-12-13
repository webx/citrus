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

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.List;

import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.support.AbstractCompositeValidator;
import com.alibaba.citrus.service.form.support.AbstractCompositeValidatorDefinitionParser;

/**
 * 当下属的任何一个validator通过验证时，此validator就通过验证。
 * <p>
 * 当验证不通过时，message配置中指定的message，但可使用变量<code>${allMessages}</code>
 * 引用所有其下属的message列表。
 * </p>
 * 
 * @author Michael Zhou
 */
public class AnyOfValidator extends AbstractCompositeValidator {
    public boolean validate(Context context) {
        List<String> messages = createArrayList(getValidators().size());

        context.getMessageContext().put("allMessages", messages);

        for (Validator validator : getValidators()) {
            Context newContext = newContext(context, validator);

            if (validator.validate(newContext)) {
                return true;
            } else {
                messages.add(validator.getMessage(newContext));
            }
        }

        return false;
    }

    public static class DefinitionParser extends AbstractCompositeValidatorDefinitionParser<AnyOfValidator> {
    }
}
