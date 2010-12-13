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
package com.alibaba.citrus.service.template.impl;

import com.alibaba.citrus.service.template.TemplateEngine;

/**
 * 用来查找和匹配模板名。
 * 
 * @author Michael Zhou
 */
public abstract class TemplateMatcher implements TemplateMatchResult {
    private final String originalTemplateName;
    private final String originalTemplateNameWithoutExtension;
    private final String originalExtension;
    private String templateNameWithoutExtension;
    private String extension;
    private TemplateEngine engine;

    public TemplateMatcher(TemplateKey key) {
        this.originalTemplateName = key.getTemplateName();
        this.originalTemplateNameWithoutExtension = key.getTemplateNameWithoutExtension();
        this.originalExtension = key.getExtension();

        this.templateNameWithoutExtension = key.getTemplateNameWithoutExtension();
        this.extension = key.getExtension();
    }

    public String getOriginalTemplateName() {
        return originalTemplateName;
    }

    public String getOriginalTemplateNameWithoutExtension() {
        return originalTemplateNameWithoutExtension;
    }

    public String getOriginalExtension() {
        return originalExtension;
    }

    public String getTemplateName() {
        return TemplateKey.getTemplateName(templateNameWithoutExtension, extension);
    }

    public String getTemplateNameWithoutExtension() {
        return templateNameWithoutExtension;
    }

    public void setTemplateNameWithoutExtension(String templateNameWithoutExtension) {
        this.templateNameWithoutExtension = templateNameWithoutExtension;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public TemplateEngine getEngine() {
        return engine;
    }

    public void setEngine(TemplateEngine engine) {
        this.engine = engine;
    }

    public abstract boolean findTemplate();

    @Override
    public String toString() {
        return "TemplateMatcher[" + originalTemplateName + "]";
    }
}
