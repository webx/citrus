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

import com.alibaba.citrus.service.form.support.AbstractRegexpValidator;
import com.alibaba.citrus.service.form.support.AbstractValidatorDefinitionParser;

/**
 * 判断输入值是否为合法的e-mail格式。
 * 
 * @author Michael Zhou
 */
public class MailAddressValidator extends AbstractRegexpValidator {
    @Override
    protected void init() throws Exception {
        setPattern("^\\S+@[^\\.]\\S*$");
        super.init();
    }

    public static class DefinitionParser extends AbstractValidatorDefinitionParser<MailAddressValidator> {
    }
}
