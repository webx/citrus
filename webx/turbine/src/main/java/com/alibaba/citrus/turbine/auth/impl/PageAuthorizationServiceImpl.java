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

import static com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl.PageAuthorizationResult.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.lang.Boolean.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.turbine.auth.PageAuthorizationService;
import com.alibaba.citrus.util.ObjectUtil;

/**
 * 为页面授权的service。
 *
 * @author Michael Zhou
 */
public class PageAuthorizationServiceImpl extends AbstractService<PageAuthorizationService> implements
                                                                                            PageAuthorizationService {
    private final List<AuthMatch> matches        = createLinkedList();
    private       boolean         allowByDefault = false;

    public void setMatches(AuthMatch[] matches) {
        this.matches.clear();

        if (matches != null) {
            for (AuthMatch match : matches) {
                this.matches.add(match);
            }
        }
    }

    public boolean isAllowByDefault() {
        return allowByDefault;
    }

    public void setAllowByDefault(boolean allowByDefault) {
        this.allowByDefault = allowByDefault;
    }

    public boolean isAllow(String target, String userName, String[] roleNames, String... actions) {
        PageAuthorizationResult result = authorize(target, userName, roleNames, actions);

        switch (result) {
            case ALLOWED:
                return true;

            case DENIED:
                return false;

            default:
                return allowByDefault;
        }
    }

    public PageAuthorizationResult authorize(String target, String userName, String[] roleNames, String... actions) {
        userName = trimToNull(userName);

        if (actions == null) {
            actions = new String[] { EMPTY_STRING };
        }

        if (roleNames == null) {
            roleNames = EMPTY_STRING_ARRAY;
        }

        // 找出所有匹配的pattern，按匹配长度倒排序。
        MatchResult[] results = getMatchResults(target);
        PageAuthorizationResult result;

        if (isEmptyArray(results)) {
            result = TARGET_NOT_MATCH;
        } else {
            boolean grantNotMatch = false;

            for (int i = 0; i < actions.length; i++) {
                actions[i] = trimToEmpty(actions[i]);
                Boolean actionAllowed = isActionAllowed(results, target, userName, roleNames, actions[i]);

                if (actionAllowed == null) {
                    grantNotMatch = true;
                } else if (!actionAllowed) {
                    return DENIED;
                }
            }

            if (!grantNotMatch) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                            "Access Permitted: target=\"{}\", user=\"{}\", roles={}, action={}",
                            new Object[] { target, userName, ObjectUtil.toString(roleNames),
                                           ObjectUtil.toString(actions) });
                }

                return ALLOWED;
            } else {
                result = GRANT_NOT_MATCH;
            }
        }

        if (allowByDefault) {
            if (getLogger().isDebugEnabled()) {
                getLogger()
                        .debug("Access Permitted.  No matches found for request: target=\"{}\", user=\"{}\", roles={}, action={}",
                               new Object[] { target, userName, ObjectUtil.toString(roleNames),
                                              ObjectUtil.toString(actions) });
            }
        } else {
            if (getLogger().isWarnEnabled()) {
                getLogger()
                        .warn("Access Denied.  No matches found for request: target=\"{}\", user=\"{}\", roles={}, action={}",
                              new Object[] { target, userName, ObjectUtil.toString(roleNames),
                                             ObjectUtil.toString(actions) });
            }
        }

        return result;
    }

    private Boolean isActionAllowed(MatchResult[] results, String target, String userName, String[] roleNames,
                                    String action) {
        // 按顺序检查授权，直到role或user被allow或deny
        for (MatchResult result : results) {
            AuthMatch match = result.match;

            // 倒序检查grant，后面的覆盖前面的。
            for (int i = match.getGrants().length - 1; i >= 0; i--) {
                AuthGrant grant = match.getGrants()[i];

                // 判断user或role是否匹配
                boolean userMatch = grant.isUserMatched(userName);
                boolean roleMatch = grant.areRolesMatched(roleNames);

                if (userMatch || roleMatch) {
                    // 判断action是否匹配
                    boolean actionAllowed = grant.isActionAllowed(action);
                    boolean actionDenied = grant.isActionDenied(action);

                    if (actionAllowed || actionDenied) {
                        boolean allowed = !actionDenied;

                        if (allowed) {
                            if (getLogger().isTraceEnabled()) {
                                getLogger()
                                        .trace("Access Partially Permitted: target=\"{}\", user=\"{}\", roles={}, action=\"{}\"\n{}",
                                               new Object[] { target, userName, ObjectUtil.toString(roleNames),
                                                              action, match.toString(i) });
                            }

                            return TRUE;
                        } else {
                            if (getLogger().isWarnEnabled()) {
                                getLogger().warn(
                                        "Access Denied: target=\"{}\", user=\"{}\", roles={}, action=\"{}\"\n{}",
                                        new Object[] { target, userName, ObjectUtil.toString(roleNames), action,
                                                       match.toString(i) });
                            }

                            return FALSE;
                        }
                    }
                }
            }
        }

        return null;
    }

    private MatchResult[] getMatchResults(String target) {
        List<MatchResult> results = createArrayList(matches.size());

        // 匹配所有，注意，这里按倒序匹配，这样长度相同的匹配，以后面的为准。
        for (ListIterator<AuthMatch> i = matches.listIterator(matches.size()); i.hasPrevious(); ) {
            AuthMatch match = i.previous();
            Matcher matcher = match.getPattern().matcher(target);

            if (matcher.find()) {
                MatchResult result = new MatchResult();
                result.matchLength = matcher.end() - matcher.start();
                result.match = match;
                result.target = target;

                results.add(result);
            }
        }

        // 按匹配长度倒排序，注意，这是稳定排序，对于长度相同的匹配，顺序不变。
        sort(results);

        // 除去重复的匹配
        Map<AuthGrant[], MatchResult> grantsSet = createLinkedHashMap();

        for (MatchResult result : results) {
            AuthGrant[] grants = result.match.getGrants();

            if (!grantsSet.containsKey(grants)) {
                grantsSet.put(grants, result);
            }
        }

        return grantsSet.values().toArray(new MatchResult[grantsSet.size()]);
    }

    private static class MatchResult implements Comparable<MatchResult> {
        private int matchLength = -1;
        private AuthMatch match;
        private String    target;

        public int compareTo(MatchResult o) {
            return o.matchLength - matchLength;
        }

        @Override
        public String toString() {
            return "Match length=" + matchLength + ", target=" + target + ", " + match;
        }
    }

    public static enum PageAuthorizationResult {
        /** 代表页面被许可访问。 */
        ALLOWED,

        /** 代表页面被拒绝访问。 */
        DENIED,

        /** 代表当前的target未匹配。 */
        TARGET_NOT_MATCH,

        /** 代表当前的grant未匹配，也就是user/roles/actions未匹配。 */
        GRANT_NOT_MATCH
    }
}
