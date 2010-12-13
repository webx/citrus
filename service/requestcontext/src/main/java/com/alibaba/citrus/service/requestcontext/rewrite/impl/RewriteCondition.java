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
package com.alibaba.citrus.service.requestcontext.rewrite.impl;

import static com.alibaba.citrus.service.requestcontext.rewrite.impl.RewriteUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.citrus.util.StringEscapeUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;
import com.alibaba.citrus.util.internal.regex.MatchResultSubstitution;

public class RewriteCondition implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(RewriteCondition.class);
    private String testString;
    private String patternString;
    private Pattern pattern;
    private boolean negative;
    private ConditionFlags flags;

    public void setTest(String testString) {
        this.testString = trimToNull(testString);
    }

    public void setPattern(String patternString) throws PatternSyntaxException {
        this.patternString = trimToNull(patternString);
    }

    public void setFlags(String[] flags) {
        this.flags = new ConditionFlags(flags);
    }

    public ConditionFlags getFlags() {
        return flags;
    }

    public void afterPropertiesSet() throws Exception {
        // test
        assertNotNull(testString, "missing test attribute for condition");

        // pattern
        if (patternString == null || "!".equals(patternString)) {
            throw new PatternSyntaxException("empty pattern", patternString, -1);
        }

        String realPattern;

        if (patternString.startsWith("!")) {
            this.negative = true;
            realPattern = patternString.substring(1);
        } else {
            realPattern = patternString;
        }

        this.pattern = Pattern.compile(realPattern);

        // flags
        if (flags == null) {
            flags = new ConditionFlags();
        }
    }

    public MatchResult match(MatchResult ruleMatchResult, MatchResult conditionMatchResult, HttpServletRequest request) {
        if (log.isTraceEnabled()) {
            log.trace("Testing condition: testString=\"{}\", pattern=\"{}\"", StringEscapeUtil.escapeJava(testString),
                    StringEscapeUtil.escapeJava(patternString));
        }

        String subsTestString = getSubstitutedTestString(testString, ruleMatchResult, conditionMatchResult, request);

        if (log.isTraceEnabled()) {
            log.trace("Expanded testString: original=\"{}\", expanded=\"{}\"", StringEscapeUtil.escapeJava(testString),
                    StringEscapeUtil.escapeJava(subsTestString));
        }

        Matcher matcher = pattern.matcher(subsTestString);
        boolean matched = matcher.find();

        if (!negative && matched) {
            if (log.isDebugEnabled()) {
                log.debug("Testing \"{}\" with condition pattern: \"{}\", MATCHED",
                        StringEscapeUtil.escapeJava(subsTestString), StringEscapeUtil.escapeJava(patternString));
            }

            return matcher.toMatchResult();
        }

        if (negative && !matched) {
            if (log.isDebugEnabled()) {
                log.debug("Testing \"{}\" with condition pattern: \"{}\", MATCHED",
                        StringEscapeUtil.escapeJava(subsTestString), StringEscapeUtil.escapeJava(patternString));
            }

            return MatchResultSubstitution.EMPTY_MATCH_RESULT;
        }

        if (log.isTraceEnabled()) {
            log.trace("Testing \"{}\" with condition pattern: \"{}\", MISMATCHED",
                    StringEscapeUtil.escapeJava(subsTestString), StringEscapeUtil.escapeJava(patternString));
        }

        return null;
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("test", testString);
        mb.append("pattern", patternString);

        if (!flags.isEmpty()) {
            mb.append("flags", flags);
        }

        return new ToStringBuilder().append("Condition").append(mb).toString();
    }

    /**
     * 代表condition的标志位。
     */
    public static class ConditionFlags extends Flags {
        public ConditionFlags() {
            super();
        }

        public ConditionFlags(String... flags) {
            super(flags);
        }

        /**
         * 标志位：和下一个condition呈“或”的关系。
         */
        public boolean hasOR() {
            return hasFlags("OR", "ornext");
        }
    }
}
