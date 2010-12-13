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

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Arrays;

import com.alibaba.citrus.util.FileUtil.FileNameAndExtension;
import com.alibaba.citrus.util.ObjectUtil;

public final class TemplateKey {
    private final String templateNameWithoutExtension;
    private final String extension;
    private final Object[] strategyKeys;

    public TemplateKey(String templateName, TemplateSearchingStrategy[] strategies) {
        templateName = assertNotNull(trimToNull(normalizeAbsolutePath(templateName)), "illegal templateName: %s",
                templateName);

        FileNameAndExtension names = getFileNameAndExtension(templateName, true);

        this.templateNameWithoutExtension = names.getFileName();
        this.extension = names.getExtension();

        if (isEmptyArray(strategies)) {
            this.strategyKeys = EMPTY_OBJECT_ARRAY;
        } else {
            this.strategyKeys = new Object[strategies.length];

            for (int i = 0; i < strategies.length; i++) {
                strategyKeys[i] = strategies[i].getKey(getTemplateName());
            }
        }
    }

    public String getTemplateName() {
        return getTemplateName(templateNameWithoutExtension, extension);
    }

    public String getTemplateNameWithoutExtension() {
        return templateNameWithoutExtension;
    }

    public String getExtension() {
        return extension;
    }

    public Object[] getStrategyKeys() {
        return strategyKeys.clone();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (!(other instanceof TemplateKey)) {
            return false;
        }

        TemplateKey otherKey = (TemplateKey) other;

        if (!ObjectUtil.isEquals(templateNameWithoutExtension, otherKey.templateNameWithoutExtension)) {
            return false;
        }

        if (!ObjectUtil.isEquals(extension, otherKey.extension)) {
            return false;
        }

        if (!Arrays.equals(strategyKeys, otherKey.strategyKeys)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + (extension == null ? 0 : extension.hashCode());
        result = prime * result + Arrays.hashCode(strategyKeys);
        result = prime * result + (templateNameWithoutExtension == null ? 0 : templateNameWithoutExtension.hashCode());

        return result;
    }

    @Override
    public String toString() {
        return getTemplateName() + Arrays.toString(strategyKeys);
    }

    public static String getTemplateName(String templateNameWithoutExtension, String extension) {
        String templateName = templateNameWithoutExtension;

        if (!isEmpty(extension)) {
            templateName = templateNameWithoutExtension + "." + extension;
        }

        return templateName;
    }
}
