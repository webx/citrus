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

import com.alibaba.citrus.turbine.auth.impl.AuthActionPattern;
import com.alibaba.citrus.turbine.auth.impl.AuthPattern;
import org.junit.Test;

public class AuthActionPatternTests {
    private AuthActionPattern pattern;

    @Test(expected = IllegalArgumentException.class)
    public void create_noname1() {
        new AuthActionPattern(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_noname2() {
        new AuthActionPattern(" ");
    }

    @Test
    public void getPatternName() {
        pattern = new AuthActionPattern("test");
        assertEquals("test", pattern.getPatternName());
    }

    @Test
    public void getPattern() {
        // relative path
        pattern = new AuthActionPattern("test");

        assertMatches(false, "a.b.test");
        assertMatches(false, "a.test.");
        assertMatches(false, "a.test.b");
        assertMatches(true, "test");
        assertMatches(true, "test.");
        assertMatches(true, "test.b");

        assertMatches(false, "atest");
        assertMatches(false, "testb");
        assertMatches(false, "atestb");

        // abs path
        pattern = new AuthActionPattern("t.est");

        assertMatches(false, "a.b.t.est");
        assertMatches(false, "a.t.est.");
        assertMatches(false, "a.t.est.b");
        assertMatches(true, "t.est");
        assertMatches(true, "t.est.");
        assertMatches(true, "t.est.b");

        assertMatches(false, "at.est");
        assertMatches(false, "t.estb");
        assertMatches(false, "at.estb");

        // root path
        pattern = new AuthActionPattern(".");

        assertMatches(true, null);
        assertMatches(true, " ");
        assertMatches(true, " .");
    }

    private void assertMatches(boolean matches, String s) {
        assertEquals(s, matches, pattern.matcher(s).find());

        if (s != null) {
            assertEquals(s, matches, pattern.getPattern().matcher(s).find());
        }
    }

    @Test
    public void equalsAndHashCode() {
        AuthPattern p1 = new AuthActionPattern("test");
        AuthPattern p2 = new AuthActionPattern("test");
        AuthPattern p3 = new AuthActionPattern("*.test");

        assertEquals(p1, p1);
        assertEquals(p1, p2);
        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("test"));

        assertEquals(p1.hashCode(), p2.hashCode());
        assertFalse(p1.hashCode() == p3.hashCode());
    }

    @Test
    public void toString_() {
        assertEquals("test", new AuthActionPattern(" test ").toString());
    }
}
