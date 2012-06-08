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
import static com.alibaba.citrus.util.regex.ClassNameWildcardCompiler.*;

import java.util.regex.Pattern;

import com.alibaba.citrus.util.regex.ClassNameWildcardCompiler;

public class AuthActionPattern extends AuthPattern {
    public AuthActionPattern(String patternName) {
        super(patternName);
    }

    @Override
    protected String normalizePatternName(String patternName) {
        return assertNotNull(trimToNull(patternName), "patternName");
    }

    @Override
    protected Pattern compilePattern(String patternName) {
        return compileClassName(patternName, ClassNameWildcardCompiler.MATCH_PREFIX);
    }
}
