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

import static com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl.PageAuthorizationResult.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import com.alibaba.citrus.turbine.auth.impl.AuthGrant;
import com.alibaba.citrus.turbine.auth.impl.AuthMatch;
import com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl;
import com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl.PageAuthorizationResult;
import org.junit.Before;
import org.junit.Test;

public class PageAuthorizationServiceTests {
    protected static final String[] ADMIN_ROLE = new String[] { "admin" };

    protected PageAuthorizationServiceImpl auth;

    @Before
    public void init() throws Exception {
        auth = new PageAuthorizationServiceImpl();

        auth.setMatches(new AuthMatch[] {
                // matches
                match("/user", grant(null, "*", null, "*")), //
                match("/user", grant("baobao", null, "read,write", null)), //
                match("/admin", grant("baobao", null, "read,write", null)), //
                match("/user/profile", grant(null, "admin", "*", null)), //
                match("/user/public", //
                      // grants
                      grant(null, "*", "action", null), //
                      grant("*", null, "read", null), //
                      grant("anonymous", null, null, "write"), // 这句将被下面一行覆盖
                      grant("anonymous", null, "write", null)), //
                match("/**/*.vm", grant(null, "*", "*", null)) //
        });
    }

    private AuthMatch match(String target, AuthGrant... grants) {
        return new AuthMatch(target, grants);
    }

    private AuthGrant grant(String user, String role, String allow, String deny) {
        AuthGrant grant = new AuthGrant();

        grant.setUsers(new String[] { user });
        grant.setRoles(new String[] { role });
        grant.setAllow(split(allow, ", "));
        grant.setDeny(split(deny, ", "));

        return grant;
    }

    @Test
    public void noTarget() {
        assertAuth(TARGET_NOT_MATCH, null, null, ADMIN_ROLE, (String[]) null);
    }

    @Test
    public void noAction() {
        // allow=*, actions=null
        assertAuth(ALLOWED, "/test.vm", null, ADMIN_ROLE, (String[]) null);

        // deny=*, actions=null
        assertAuth(DENIED, "/user", null, ADMIN_ROLE, (String[]) null);
    }

    @Test
    public void multiActions() {
        // allow=read,write, actions=read,write
        assertAuth(ALLOWED, "/user", "baobao", null, "read", "write");

        // allow=read,write, action=read,write,other
        assertAuth(GRANT_NOT_MATCH, "/user", "baobao", null, "read", "write", "other");
    }

    /** target不匹配。 */
    @Test
    public void targetNotMatch() {
        assertAuth(TARGET_NOT_MATCH, "/", "baobao", null, (String[]) null);
        assertAuth(TARGET_NOT_MATCH, "/notMatch", "baobao", null, (String[]) null);
    }

    /** 最长的匹配优先授权，相同的匹配以后面的为准。 */
    @Test
    public void priority() {
        // allow=read,write, actions=read
        assertAuth(ALLOWED, "/user", "baobao", null, "read");

        // allow=read,write, actions=write
        assertAuth(ALLOWED, "/user", "baobao", null, "write");

        // deny=*, actions=write
        assertAuth(DENIED, "/user", null, ADMIN_ROLE, "write");
    }

    /** target匹配，但用户未匹配。 */
    @Test
    public void userNotMatch() {
        assertAuth(GRANT_NOT_MATCH, "/user", "other", null, "read");
        assertAuth(GRANT_NOT_MATCH, "/user", "other", null, "write");
    }

    /** target匹配、用户匹配，但action不匹配。 */
    @Test
    public void actionNotMatch() {
        // allow=read,write, action=otherAction
        assertAuth(GRANT_NOT_MATCH, "/user", "baobao", null, "otherAction");
    }

    /** 匹配role。 */
    @Test
    public void role() {
        // allow=*, action=read
        assertAuth(ALLOWED, "/user/profile", "other", ADMIN_ROLE, "read");

        // allow=*, action=write
        assertAuth(ALLOWED, "/user/profile/abc", "other", ADMIN_ROLE, "write");

        // role=admin不匹配null
        assertAuth(GRANT_NOT_MATCH, "/user/profile/abc", "other", null, "write");
    }

    /** 相对路径。 */
    @Test
    public void relativeTarget() {
        // allow=*
        assertAuth(ALLOWED, "/user/hello.vm", "other", ADMIN_ROLE, "read");

        // role=admin不匹配null
        assertAuth(GRANT_NOT_MATCH, "/user/world.vm", "other", null, "write");
    }

    /** 匿名访问。 */
    @Test
    public void anonymous() {
        // role=*不包括空role
        assertAuth(GRANT_NOT_MATCH, "/user/public/hello", null, null, "action");

        // user=* 不包括anonymous
        assertAuth(GRANT_NOT_MATCH, "/user/public/hello", null, null, "read");

        // user=anonymous
        assertAuth(ALLOWED, "/user/public/hello", null, null, "write");
    }

    private void assertAuth(PageAuthorizationResult result, String target, String userName, String[] roleNames,
                            String... actions) {
        assertSame(result, auth.authorize(target, userName, roleNames, actions));

        if (result == ALLOWED) {
            assertTrue(auth.isAllow(target, userName, roleNames, actions));
        } else {
            assertFalse(auth.isAllow(target, userName, roleNames, actions));
        }
    }
}
