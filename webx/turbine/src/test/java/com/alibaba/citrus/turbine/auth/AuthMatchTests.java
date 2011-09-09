package com.alibaba.citrus.turbine.auth;

import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.turbine.auth.impl.AuthGrant;
import com.alibaba.citrus.turbine.auth.impl.AuthMatch;

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
