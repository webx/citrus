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
    private final String patternName;
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
