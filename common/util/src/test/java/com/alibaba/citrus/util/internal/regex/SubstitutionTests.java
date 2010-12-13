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
package com.alibaba.citrus.util.internal.regex;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

public class SubstitutionTests {
    private MatchResult r1;
    private MatchResult r2;

    @Before
    public void init() {
        r1 = getMatchResult("(aa)bb", "aabb");
        r2 = getMatchResult("(cc)dd", "ccdd");
    }

    private MatchResult getMatchResult(String pattern, String input) {
        Matcher matcher = Pattern.compile(pattern).matcher(input);

        assertTrue(matcher.find());

        return matcher.toMatchResult();
    }

    @Test
    public void emptyResult() {
        assertEquals(0, MatchResultSubstitution.EMPTY_MATCH_RESULT.groupCount());
        assertEquals("", MatchResultSubstitution.EMPTY_MATCH_RESULT.group(0));
    }

    @Test
    public void illegal_replacementPrefixes() {
        try {
            new MatchResultSubstitution("");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("replacementPrefixes"));
        }
    }

    @Test
    public void illegal_results() {
        try {
            new MatchResultSubstitution("$");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("results"));
        }
    }

    @Test
    public void illegal_results_count_not_match() {
        try {
            new MatchResultSubstitution("$%", r1);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("expected 2 MatchResults"));
        }
    }

    @Test
    public void subst_null() {
        Substitution subs = new MatchResultSubstitution(r1);
        assertEquals(null, subs.substitute(null));
    }

    @Test
    public void setMatchResult() {
        MatchResultSubstitution subs = new MatchResultSubstitution();
        subs.setMatchResult(r1);

        assertEquals("xxaayy$2zz", subs.substitute("xx$1yy$2zz"));

        try {
            subs.setMatchResults(r1, r2);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("expected 1 MatchResults"));
        }
    }

    @Test
    public void matchNormal() {
        Substitution subs = new MatchResultSubstitution(r1);
        assertEquals("xxaayy$2zz", subs.substitute("xx$1yy$2zz"));
    }

    @Test
    public void matchMulti() {
        Substitution subs = new MatchResultSubstitution("$%", r1, r2);
        assertEquals("xxaayycczz$2%2", subs.substitute("xx$1yy%1zz$2%2"));

        r1 = getMatchResult("\\.(\\w+)\\.com/(.*)", "www.taobao.com/test.htm");
        r2 = getMatchResult("a=(\\d+)&b=(\\d+)", "a=1&b=2&c=3");

        assertEquals("$1, $x, .taobao.com/test.htm, taobao, test.htm, $3, %1, %x, a=1&b=2, 1, 2, %3",
                new MatchResultSubstitution("$%", r1, r2)
                        .substitute("\\$1, $x, $0, $1, $2, $3, \\%1, %x, %0, %1, %2, %3"));
    }

    @Test
    public void _toString() {
        String str = new MatchResultSubstitution(r1).toString();

        assertThat(str, containsAll("$n", "aabb"));

        str = new MatchResultSubstitution("$%", r1, r2).toString();

        assertThat(str, containsAll("$n", "aabb"));
        assertThat(str, containsAll("%n", "ccdd"));
    }
}
