package com.alibaba.citrus.turbine.auth;

import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.test.TestEnvStatic;
import com.alibaba.citrus.turbine.auth.impl.AuthGrant;
import com.alibaba.citrus.turbine.auth.impl.AuthMatch;
import com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl;

public class PageAuthorizationServiceTests {
    protected PageAuthorizationServiceImpl auth;

    static {
        System.setProperty("loggingLevel", "debug");
        TestEnvStatic.init();
    }

    @Before
    public void init() throws Exception {
        auth = new PageAuthorizationServiceImpl();

        auth.setMatches(new AuthMatch[] {
                // matches
                match("/user", grant(null, "*", null, "*")), //
                match("/user", grant("baobao", null, "read,write", null)), //
                match("/admin", grant("baobao", null, "read,write", null)), //
                match("/user/profile", grant(null, "administrator", "*", null)), //
                match("/user/public", //
                        // grants
                        grant(null, "*", "action", null), // 
                        grant("*", null, "read", null), // 
                        grant("anonymous", null, "write", null)), //
                match("/**/*.vm", grant(null, "*", "*", null)) //
        });
    }

    private AuthMatch match(String target, AuthGrant... grants) {
        return new AuthMatch(target, grants);
    }

    private AuthGrant grant(String user, String role, String allow, String deny) {
        AuthGrant grant = new AuthGrant();

        grant.setUser(user);
        grant.setRole(role);
        grant.setAllow(split(allow, ", "));
        grant.setDeny(split(deny, ", "));

        return grant;
    }

    @Test
    public void noAction() {
        assertTrue(auth.isAllow("/test.vm", "baobao", null, (String[]) null));
        assertFalse(auth.isAllow("/user", "baobao", null, (String[]) null));
    }

    @Test
    public void multiActions() {
        assertTrue(auth.isAllow("/user", "baobao", null, "read", "write"));
        assertFalse(auth.isAllow("/user", "baobao", null, "read", "write", "other"));
    }

    /**
     * target不匹配。
     */
    @Test
    public void targetNotMatch() {
        assertFalse(auth.isAllow("/", "baobao", null, (String[]) null));
        assertFalse(auth.isAllow("/notMatch", "baobao", null, (String[]) null));
    }

    /**
     * 最长的匹配优先授权，相同的匹配以后面的为准。
     */
    @Test
    public void priority() {
        assertTrue(auth.isAllow("/user", "baobao", null, "read"));
        assertTrue(auth.isAllow("/user", "baobao", null, "write"));
    }

    /**
     * target匹配，但用户未匹配。
     */
    @Test
    public void nonMatchedUser() {
        assertFalse(auth.isAllow("/user", "other", null, "read"));
        assertFalse(auth.isAllow("/user", "other", null, "write"));
    }

    /**
     * target匹配、用户匹配，但action不匹配。
     */
    @Test
    public void nonMatchedAction() {
        assertFalse(auth.isAllow("/user", "baobao", null, "otherAction"));
    }

    /**
     * 匹配role。
     */
    @Test
    public void role() {
        assertTrue(auth.isAllow("/user/profile", "other", new String[] { "administrator" }, "read"));
        assertTrue(auth.isAllow("/user/profile/abc", "other", new String[] { "administrator" }, "write"));
    }

    /**
     * 相对路径。
     */
    @Test
    public void relativeTarget() {
        assertTrue(auth.isAllow("/user/hello.vm", "other", null, "read"));
        assertTrue(auth.isAllow("/user/world.vm", "other", null, "write"));
    }

    /**
     * 匿名访问。
     */
    @Test
    public void anonymous() {
        // * 不包括anonymous
        assertFalse(auth.isAllow("/user/public/hello", null, null, "action"));
        assertFalse(auth.isAllow("/user/public/hello", null, null, "read"));
        assertTrue(auth.isAllow("/user/public/hello", null, null, "write"));
    }
}
