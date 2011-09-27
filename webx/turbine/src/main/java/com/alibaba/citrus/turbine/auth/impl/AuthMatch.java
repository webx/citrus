package com.alibaba.citrus.turbine.auth.impl;

import static com.alibaba.citrus.util.ObjectUtil.*;

import com.alibaba.citrus.util.ToStringBuilder;
import com.alibaba.citrus.util.ToStringBuilder.MapBuilder;

/**
 * 代表一组patterns和grants授权的组合。
 * 
 * @author Michael Zhou
 */
public class AuthMatch {
    private final static AuthGrant[] NO_GRANTS = new AuthGrant[0];
    private final AuthPattern pattern;
    private final AuthGrant[] grants;

    public AuthMatch(String pattern, AuthGrant[] grants) {
        this.pattern = new AuthTargetPattern(pattern);
        this.grants = defaultIfNull(grants, NO_GRANTS);
    }

    public AuthPattern getPattern() {
        return pattern;
    }

    public AuthGrant[] getGrants() {
        return grants;
    }

    @Override
    public String toString() {
        return toString(-1);
    }

    public String toString(int matchedGrantIndex) {
        MapBuilder mb = new MapBuilder();

        mb.append("pattern", pattern);

        if (matchedGrantIndex < 0 || matchedGrantIndex >= grants.length) {
            mb.append("grants", grants);
        } else {
            mb.append("grants[" + matchedGrantIndex + "]", grants[matchedGrantIndex]);
        }

        return new ToStringBuilder().append("Match").append(mb).toString();
    }
}
