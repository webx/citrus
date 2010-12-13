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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通过正则表达式来验证字段的validator。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractRegexpValidator extends AbstractOptionalValidator {
    private String patternString;
    private Pattern pattern;

    /**
     * 取得regexp。
     */
    public String getPattern() {
        return patternString;
    }

    /**
     * 设置regexp。
     */
    protected void setPattern(String pattern) {
        this.patternString = trimToNull(pattern);
    }

    /**
     * 取得匹配或不匹配。
     */
    public boolean getNot() {
        return patternString == null ? false : patternString.startsWith("!");
    }

    /**
     * 检查配置参数的合法性。
     */
    @Override
    protected void init() throws Exception {
        super.init();

        String regexp = patternString;

        if (regexp != null && regexp.startsWith("!")) {
            regexp = trimToNull(regexp.substring(1));
        }

        assertNotNull(regexp, "missing regexp pattern");
        pattern = Pattern.compile(regexp);
    }

    /**
     * 验证一个字段。
     */
    @Override
    protected boolean validate(Context context, String value) {
        Matcher matcher = pattern.matcher(value);
        boolean matched = matcher.find();

        if (getNot()) {
            return !matched;
        } else {
            return matched;
        }
    }
}
