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
package com.alibaba.citrus.service.form.impl;

import static com.alibaba.citrus.service.form.FormConstant.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.lang.System.*;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.form.Form;
import com.alibaba.citrus.service.form.FormService;
import com.alibaba.citrus.service.form.configuration.FormConfig;
import com.alibaba.citrus.service.form.impl.configuration.FormConfigImpl;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 用来处理用户提交表单的service。
 * 
 * @author Michael Zhou
 */
public class FormServiceImpl extends AbstractService<FormService> implements FormService {
    private final HttpServletRequest request;
    private FormConfigImpl formConfig;
    private String requestKey;

    public FormServiceImpl(HttpServletRequest request) {
        this.request = assertProxy(assertNotNull(request, "request"));
    }

    /**
     * 取得form config。
     */
    public FormConfig getFormConfig() {
        return formConfig;
    }

    public void setFormConfigImpl(FormConfigImpl formConfig) {
        this.formConfig = formConfig;
    }

    /**
     * 初始化service。
     */
    @Override
    protected void init() {
        // 确保不同的formService取得不同的form实例。
        requestKey = "_FormService_" + defaultIfEmpty(getBeanName(), getClass().getName()) + "_"
                + identityHashCode(this);

        assertNotNull(formConfig, "formConfig");
        formConfig.setFormService(this);
    }

    /**
     * 从request中取得当前请求的form或创建新的form。
     */
    public Form getForm() {
        return getForm(false);
    }

    /**
     * 从request中取得当前请求的form或创建新的form。
     */
    public Form getForm(boolean forcePostOnly) {
        Object form = assertNotNull(request, "Could not getForm: request is null").getAttribute(requestKey);

        if (form == null) {
            form = new FormImpl(formConfig, FORM_KEY_PREFIX, forcePostOnly);
            ((Form) form).init(request);

            request.setAttribute(requestKey, form);
        }

        return (Form) form;
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append(getBeanDescription()).start().append(formConfig).end().toString();
    }
}
