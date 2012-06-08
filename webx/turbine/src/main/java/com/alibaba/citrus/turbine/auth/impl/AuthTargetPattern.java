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

package com.alibaba.citrus.turbine.auth.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.regex.PathNameWildcardCompiler.*;

import java.util.regex.Pattern;

public class AuthTargetPattern extends AuthPattern {
    public AuthTargetPattern(String patternName) {
        super(patternName);
    }

    @Override
    protected String normalizePatternName(String patternName) {
        patternName = assertNotNull(trimToNull(patternName), "patternName");

        // 对于相对路径，自动在前面加上/，变成绝对路径。
        if (!patternName.startsWith("/")) {
            patternName = "/" + patternName;
        }

        return patternName;
    }

    @Override
    protected Pattern compilePattern(String patternName) {
        return compilePathName(patternName, FORCE_MATCH_PREFIX | FORCE_ABSOLUTE_PATH);
    }
}
