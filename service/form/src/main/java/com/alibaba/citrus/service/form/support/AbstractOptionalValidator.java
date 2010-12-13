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

import static com.alibaba.citrus.util.StringUtil.*;

/**
 * 抽象的<code>Validator</code>实现，这个类的子类将忽略值为空的情形。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractOptionalValidator extends AbstractValidator {
    /**
     * 验证一个字段。
     */
    public boolean validate(Context context) {
        String value = context.getValueAsType(String.class);

        // 在trimming=false模式下，空白也算有值。
        if (isEmpty(value)) {
            return true;
        }

        return validate(context, value);
    }

    /**
     * 验证一个字段。
     */
    protected abstract boolean validate(Context context, String value);
}
