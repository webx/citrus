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

import java.util.Set;

import com.alibaba.citrus.turbine.auth.impl.AuthGrant;
import com.alibaba.citrus.turbine.auth.impl.AuthPattern;
import org.junit.Before;
import org.junit.Test;

public class AuthGrantTests {
    private AuthGrant grant;

    @Before
    public void init() {
        grant = new AuthGrant();
    }

    @Test
    public void setUsers() {
        assertNull(grant.getUsers());

        grant.setUsers(new String[] { " ", null });
        assertNull(grant.getUsers());

        grant.setUsers(new String[] { " user1 ", "user2", null });
        assertArrayEquals(new String[] { "user1", "user2" }, grant.getUsers());
    }

    @Test
    public void setRoles() {
        assertNull(grant.getRoles());

        grant.setRoles(new String[] { " ", null });
        assertNull(grant.getRoles());

        grant.setRoles(new String[] { " role1 ", "role2", null });
        assertArrayEquals(new String[] { "role1", "role2" }, grant.getRoles());
    }

    @Test
    public void setAllow() {
        assertTrue(grant.getAllowedActions().isEmpty());

        grant.setAllow((String[]) null);
        assertTrue(grant.getAllowedActions().isEmpty());

        grant.setAllow("aa", "bb", "cc");
        assertArrayEquals(new String[] { "aa", "bb", "cc" }, toArray(grant.getAllowedActions()));

        grant.setAllow(" bb", "*", "cc");
        assertArrayEquals(new String[] { "bb", "*", "cc" }, toArray(grant.getAllowedActions()));
    }

    @Test
    public void setDeny() {
        assertTrue(grant.getDeniedActions().isEmpty());

        grant.setDeny((String[]) null);
        assertTrue(grant.getDeniedActions().isEmpty());

        grant.setDeny("aa", "bb", "cc");
        assertArrayEquals(new String[] { "aa", "bb", "cc" }, toArray(grant.getDeniedActions()));

        grant.setDeny(" bb", "*", "cc");
        assertArrayEquals(new String[] { "bb", "*", "cc" }, toArray(grant.getDeniedActions()));
    }

    private String[] toArray(Set<AuthPattern> patterns) {
        String[] s = new String[patterns.size()];
        int i = 0;

        for (AuthPattern pattern : patterns) {
            s[i++] = pattern.getPatternName();
        }

        return s;
    }

    @Test
    public void isActionAllowed() {
        grant.setAllow("*");

        assertTrue(grant.isActionAllowed("a.b"));
        assertTrue(grant.isActionAllowed("a"));
        assertTrue(grant.isActionAllowed(""));

        grant.setAllow("a", "b");

        assertTrue(grant.isActionAllowed("a.b"));
        assertTrue(grant.isActionAllowed("b"));
        assertFalse(grant.isActionAllowed("c"));

        grant.setAllow("a.b*");

        assertTrue(grant.isActionAllowed("a.bc"));
        assertTrue(grant.isActionAllowed("a.b"));
        assertTrue(grant.isActionAllowed("a.b.c"));
        assertFalse(grant.isActionAllowed("c"));
    }

    @Test
    public void isActionDenied() {
        grant.setDeny("*");

        assertTrue(grant.isActionDenied("a.b"));
        assertTrue(grant.isActionDenied("a"));
        assertTrue(grant.isActionDenied(""));

        grant.setDeny("a", "b");

        assertTrue(grant.isActionDenied("a.b"));
        assertTrue(grant.isActionDenied("b"));
        assertFalse(grant.isActionDenied("c"));

        grant.setDeny("a.b*");

        assertTrue(grant.isActionDenied("a.bc"));
        assertTrue(grant.isActionDenied("a.b"));
        assertTrue(grant.isActionDenied("a.b.c"));
        assertFalse(grant.isActionDenied("c"));
    }

    @Test
    public void isUserMatched() {
        // default
        assertFalse(grant.isUserMatched(null));
        assertFalse(grant.isUserMatched("baobao"));

        // * except anonymous
        grant.setUsers(new String[] { new String("*") /* new instance */ });
        assertFalse(grant.isUserMatched(null));
        assertTrue(grant.isUserMatched("baobao"));
        assertTrue(grant.isUserMatched("anonymous")); // 注意：用户名允许使用anonymous，但不代表真正的匿名用户

        // * and anonymous
        grant.setUsers(new String[] { new String("*") /* new instance */, new String("anonymous") /*
                                                                                                   * new
                                                                                                   * instance
                                                                                                   */ });
        assertTrue(grant.isUserMatched(null));
        assertTrue(grant.isUserMatched("baobao"));
        assertTrue(grant.isUserMatched("anonymous")); // 注意：用户名允许使用anonymous，但不代表真正的匿名用户

        // specific name
        grant.setUsers(new String[] { "baobao" });
        assertFalse(grant.isUserMatched(null));
        assertTrue(grant.isUserMatched("baobao"));
        assertFalse(grant.isUserMatched("anonymous")); // 注意：用户名允许使用anonymous，但不代表真正的匿名用户
    }

    @Test
    public void areRolesMatched() {
        // default
        assertFalse(grant.areRolesMatched(null));
        assertFalse(grant.areRolesMatched(new String[] { "admin" }));

        // * except anonymous
        grant.setRoles(new String[] { new String("*") /* new instance */ });
        assertFalse(grant.areRolesMatched(new String[0]));
        assertFalse(grant.areRolesMatched(new String[] { null }));
        assertTrue(grant.areRolesMatched(new String[] { "admin" }));

        // specific name
        grant.setRoles(new String[] { "admin" });
        assertFalse(grant.areRolesMatched(new String[0]));
        assertTrue(grant.areRolesMatched(new String[] { "admin" }));
    }

    @Test
    public void toString_() {
        grant.setAllow("a", "b", "c");
        grant.setDeny("e", "f", "g");
        grant.setUsers(new String[] { "user" });
        grant.setRoles(new String[] { "role" });

        String s = "";
        s += "Grant {\n";
        s += "  users = [user]\n";
        s += "  roles = [role]\n";
        s += "  allow = [a, b, c]\n";
        s += "  deny  = [e, f, g]\n";
        s += "}";

        assertEquals(s, grant.toString());
    }
}
