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
package com.alibaba.citrus.turbine.form.impl.validation;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.form.support.AbstractValidator;
import com.alibaba.citrus.service.form.support.AbstractValidatorDefinitionParser;
import com.alibaba.citrus.turbine.util.CsrfToken;

/**
 * 在表单里加上该validator，可用来确保csrf token被提交。
 * 
 * @author Michael Zhou
 */
public class CsrfFormValidator extends AbstractValidator {
    private final HttpServletRequest request;

    public CsrfFormValidator(HttpServletRequest request) {
        this.request = assertProxy(assertNotNull(request, "no request proxy"));
    }

    public boolean validate(Context context) {
        return CsrfToken.check(request);
    }

    public static class DefinitionParser extends AbstractValidatorDefinitionParser<CsrfFormValidator> {
        @Override
        protected void doParseElement(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            addConstructorArg(builder, true, HttpServletRequest.class);
        }
    }
}
