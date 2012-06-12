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

package com.alibaba.citrus.turbine.auth;

import static org.junit.Assert.*;

import com.alibaba.citrus.turbine.auth.impl.AuthPattern;
import com.alibaba.citrus.turbine.auth.impl.AuthTargetPattern;
import org.junit.Test;

public class AuthTargetPatternTests {
    private AuthTargetPattern pattern;

    @Test(expected = IllegalArgumentException.class)
    public void create_noname1() {
        new AuthTargetPattern(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_noname2() {
        new AuthTargetPattern(" ");
    }

    @Test
    public void getPatternName() {
        pattern = new AuthTargetPattern("test");
        assertEquals("/test", pattern.getPatternName());

        pattern = new AuthTargetPattern("/test");
        assertEquals("/test", pattern.getPatternName());
    }

    @Test
    public void getPattern() {
        // relative path
        pattern = new AuthTargetPattern("test");

        assertMatches(false, "/a/b/test");
        assertMatches(false, "/a/test/");
        assertMatches(false, "/a/test/b");
        assertMatches(true, "/test");
        assertMatches(true, "/test/");
        assertMatches(true, "/test/b");

        assertMatches(false, "/atest");
        assertMatches(false, "/testb");
        assertMatches(false, "/atestb");
        assertMatches(false, "test");
        assertMatches(false, "test/");

        // abs path
        pattern = new AuthTargetPattern("/t/est");

        assertMatches(false, "/a/b/t/est");
        assertMatches(false, "/a/t/est/");
        assertMatches(false, "/a/t/est/b");
        assertMatches(true, "/t/est");
        assertMatches(true, "/t/est/");
        assertMatches(true, "/t/est/b");

        assertMatches(false, "/at/est");
        assertMatches(false, "/t/estb");
        assertMatches(false, "/at/estb");
        assertMatches(false, "t/est");
        assertMatches(false, "t/est/");

        // root path
        pattern = new AuthTargetPattern("/");

        assertMatches(true, null);
        assertMatches(true, " ");
        assertMatches(true, " /");
    }

    private void assertMatches(boolean matches, String s) {
        assertEquals(s, matches, pattern.matcher(s).find());

        if (s != null) {
            assertEquals(s, matches, pattern.getPattern().matcher(s).find());
        }
    }

    @Test
    public void equalsAndHashCode() {
        AuthPattern p1 = new AuthTargetPattern("test");
        AuthPattern p2 = new AuthTargetPattern("/test");
        AuthPattern p3 = new AuthTargetPattern("/*/test");
        AuthPattern p4 = new AuthTargetPattern("/");

        assertEquals(p1, p1);
        assertEquals(p1, p2);
        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("test"));

        assertEquals(p1.hashCode(), p2.hashCode());
        assertFalse(p1.hashCode() == p3.hashCode());
        assertFalse(p4.hashCode() == p3.hashCode());
    }

    @Test
    public void toString_() {
        assertEquals("/test", new AuthTargetPattern(" test ").toString());
    }
}
