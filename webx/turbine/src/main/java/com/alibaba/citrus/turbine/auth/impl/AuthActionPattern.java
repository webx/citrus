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
