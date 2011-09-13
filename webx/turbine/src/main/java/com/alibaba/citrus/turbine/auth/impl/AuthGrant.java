package com.alibaba.citrus.turbine.auth.impl;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
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
    /** MATCH_EVERYTHING代表所有用户和role，但不包含匿名用户 */
    public final static String MATCH_EVERYTHING = "*";

    /** 特列用户名：匿名用户 */
    public final static String ANONYMOUS_USER = "anonymous";

    private String[] users;
    private String[] roles;
    private Set<AuthPattern> allowedActions = createLinkedHashSet();
    private Set<AuthPattern> deniedActions = createLinkedHashSet();

    public String[] getUsers() {
        return users;
    }

    public void setUsers(String[] users) {
        this.users = trim(users);
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = trim(roles);
    }

    private String[] trim(String[] array) {
        List<String> list = createLinkedList();

        if (!isEmptyArray(array)) {
            for (String item : array) {
                item = trimToNull(item);

                if (item != null) {
                    list.add(item);
                }
            }
        }

        if (!list.isEmpty()) {
            return list.toArray(new String[list.size()]);
        } else {
            return null;
        }
    }

    public boolean isUserMatched(String userName) {
        if (!isEmptyArray(users)) {
            userName = defaultIfNull(userName, ANONYMOUS_USER);

            for (String grantUser : users) {
                if (grantUser.equals(MATCH_EVERYTHING) && !ANONYMOUS_USER.equals(userName)
                        || grantUser.equals(userName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean areRolesMatched(String[] roleNames) {
        if (!isEmptyArray(roles)) {
            boolean emptyRoleNames = true;

            if (!isEmptyArray(roleNames)) {
                for (String roleName : roleNames) {
                    if (roleName != null) {
                        emptyRoleNames = false;
                        break;
                    }
                }
            }

            for (String grantRole : roles) {
                if (grantRole.equals(MATCH_EVERYTHING) && !emptyRoleNames || arrayContains(roleNames, grantRole)) {
                    return true;
                }
            }
        }

        return false;
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

        if (!isEmptyArray(users)) {
            mb.append("users", new CollectionBuilder().appendAll(users).setOneLine(true));
        }

        if (!isEmptyArray(roles)) {
            mb.append("roles", new CollectionBuilder().appendAll(roles).setOneLine(true));
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
