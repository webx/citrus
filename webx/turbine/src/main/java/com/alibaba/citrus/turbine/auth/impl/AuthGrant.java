package com.alibaba.citrus.turbine.auth.impl;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Set;

import com.alibaba.citrus.util.ToStringBuilder;
import com.alibaba.citrus.util.ToStringBuilder.CollectionBuilder;
import com.alibaba.citrus.util.ToStringBuilder.MapBuilder;

/**
 * 代表一个授权，可以对role和user进行授权。
 * 
 * @author Michael Zhou
 */
public class AuthGrant {
    private String user;
    private String role;
    private Set<AuthPattern> allowedActions = createLinkedHashSet();
    private Set<AuthPattern> deniedActions = createLinkedHashSet();

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = trimToNull(user);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = trimToNull(role);
    }

    public Set<AuthPattern> getAllowedActions() {
        return allowedActions;
    }

    public boolean isActionAllowed(String action) {
        return matches(allowedActions, action);
    }

    public void setAllow(String... allow) {
        setActions(allowedActions, allow);
    }

    public Set<AuthPattern> getDeniedActions() {
        return deniedActions;
    }

    public boolean isActionDenied(String action) {
        return matches(deniedActions, action);
    }

    public void setDeny(String... deny) {
        setActions(deniedActions, deny);
    }

    private void setActions(Set<AuthPattern> actionSet, String[] actions) {
        actionSet.clear();

        for (String action : defaultIfNull(actions, EMPTY_STRING_ARRAY)) {
            actionSet.add(new AuthActionPattern(action));
        }
    }

    private boolean matches(Set<AuthPattern> actionSet, String action) {
        for (AuthPattern pattern : actionSet) {
            if (pattern.matcher(action).find()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        if (user != null) {
            mb.append("user", user);
        }

        if (role != null) {
            mb.append("role", role);
        }

        if (!allowedActions.isEmpty()) {
            mb.append("allow", new CollectionBuilder().appendAll(allowedActions).setOneLine(true));
        }

        if (!deniedActions.isEmpty()) {
            mb.append("deny", new CollectionBuilder().appendAll(deniedActions).setOneLine(true));
        }

        return new ToStringBuilder().append("Grant").append(mb).toString();
    }
}
