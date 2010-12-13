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
package com.alibaba.citrus.service.mappingrule.support;

import static com.alibaba.citrus.util.StringUtil.*;

import com.alibaba.citrus.service.template.TemplateService;

/**
 * 映射到模板名的<code>MappingRule</code>。
 * <p>
 * 该<code>MappingRule</code>有可能调用<code>TemplateService</code>以确定模板是否存在。
 * </p>
 * 
 * @author Michael Zhou
 */
public abstract class AbstractTemplateMappingRule extends AbstractMappingRule {
    public static final String TEMPLATE_NAME_SEPARATOR = "/";

    private TemplateService templateService;
    private String templatePrefix;

    public TemplateService getTemplateService() {
        return templateService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public String getTemplatePrefix() {
        return templatePrefix;
    }

    public void setTemplatePrefix(String templatePrefix) {
        this.templatePrefix = trimToNull(templatePrefix);
    }
}
