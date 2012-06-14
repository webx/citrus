/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.service.requestcontext.locale.impl;

import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.regex.PathNameWildcardCompiler.*;

import java.util.regex.Pattern;

/**
 * 根据request uri来设置输入、输出charset。
 *
 * @author Michael Zhou
 */
public class SetLocaleOverrider {
    private Pattern requestUriPattern;
    private String  requestUriPatternName;
    private String  inputCharset;
    private String  outputCharset;

    public Pattern getRequestUriPattern() {
        return requestUriPattern;
    }

    public void setUri(String requestUriPatternName) {
        this.requestUriPattern = compilePathName(requestUriPatternName);
        this.requestUriPatternName = requestUriPatternName;
    }

    public String getInputCharset() {
        return inputCharset;
    }

    public void setInputCharset(String inputCharset) {
        this.inputCharset = trimToNull(inputCharset);
    }

    public String getOutputCharset() {
        return outputCharset;
    }

    public void setOutputCharset(String outputCharset) {
        this.outputCharset = trimToNull(outputCharset);
    }

    @Override
    public String toString() {
        return String.format("Override[uri=%s, inputCharset=%s, outputCharset=%s]", requestUriPatternName, inputCharset, outputCharset);
    }
}
