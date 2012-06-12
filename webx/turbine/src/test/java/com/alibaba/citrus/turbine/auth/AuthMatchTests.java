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

import com.alibaba.citrus.turbine.auth.impl.AuthGrant;
import com.alibaba.citrus.turbine.auth.impl.AuthMatch;
import org.junit.Test;

public class AuthMatchTests {
    private AuthMatch match;

    @Test(expected = IllegalArgumentException.class)
    public void create_noPatterns() {
        new AuthMatch(null, new AuthGrant[0]);
    }

    @Test
    public void getPattern() {
        match = new AuthMatch("test", new AuthGrant[0]);
        assertEquals("/test", match.getPattern().getPatternName());
    }

    @Test
    public void getGrants() {
        match = new AuthMatch("test", new AuthGrant[] { new AuthGrant() });
        assertEquals(1, match.getGrants().length);
    }

    @Test
    public void toString_() {
        match = new AuthMatch("test", new AuthGrant[] { new AuthGrant() });

        String s = "";

        s += "Match {\n";
        s += "  pattern = /test\n";
        s += "  grants  = [\n";
        s += "              [1/1] Grant{}\n";
        s += "            ]\n";
        s += "}";

        assertEquals(s, match.toString());
    }
}
