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

import static com.alibaba.citrus.util.BasicConstant.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代表一个用来匹配的pattern。
 *
 * @author Michael Zhou
 */
public abstract class AuthPattern {
    private final String  patternName;
    private final Pattern pattern;

    public AuthPattern(String patternName) {
        this.patternName = normalizePatternName(patternName);
        this.pattern = compilePattern(this.patternName);
    }

    protected abstract String normalizePatternName(String patternName);

    protected abstract Pattern compilePattern(String patternName);

    public String getPatternName() {
        return patternName;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Matcher matcher(String s) {
        return pattern.matcher(s == null ? EMPTY_STRING : s);
    }

    @Override
    public int hashCode() {
        return 31 + patternName.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        return patternName.equals(((AuthPattern) other).patternName);
    }

    @Override
    public String toString() {
        return patternName;
    }
}
