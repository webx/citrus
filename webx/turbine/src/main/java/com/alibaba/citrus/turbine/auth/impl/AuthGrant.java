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

    /** 特例用户名：匿名用户 */
    public final static String ANONYMOUS_USER = "anonymous";

    private String[] users;
    private String[] roles;
    private Set<AuthPattern> allowedActions = createLinkedHashSet();
    private Set<AuthPattern> deniedActions  = createLinkedHashSet();

    public String[] getUsers() {
        return users;
    }

    public void setUsers(String[] users) {
        this.users = trim(users, MATCH_EVERYTHING, ANONYMOUS_USER);
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = trim(roles, MATCH_EVERYTHING);
    }

    private String[] trim(String[] array, String... canonicals) {
        List<String> list = createLinkedList();

        if (!isEmptyArray(array)) {
            for (String item : array) {
                item = trimToNull(item);

                if (item != null) {
                    // 优化性能，避免字符串的比较，只需要用==比较即可。
                    if (canonicals != null) {
                        int i = arrayIndexOf(canonicals, item);

                        if (i >= 0) {
                            item = canonicals[i];
                        }
                    }

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
            for (String grantUser : users) {
                if (grantUser == ANONYMOUS_USER) {
                    if (userName == null) {
                        return true;
                    }
                } else if (grantUser == MATCH_EVERYTHING) {
                    if (userName != null) {
                        return true;
                    }
                } else {
                    if (grantUser.equals(userName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean areRolesMatched(String[] roleNames) {
        if (!isEmptyArray(roles)) {
            for (String grantRole : roles) {
                if (grantRole == MATCH_EVERYTHING) {
                    boolean emptyRoleNames = true;

                    if (!isEmptyArray(roleNames)) {
                        for (String roleName : roleNames) {
                            if (roleName != null) {
                                emptyRoleNames = false;
                                break;
                            }
                        }
                    }

                    if (!emptyRoleNames) {
                        return true;
                    }
                } else {
                    if (arrayContains(roleNames, grantRole)) {
                        return true;
                    }
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
