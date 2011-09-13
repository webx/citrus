package com.alibaba.citrus.turbine.auth.impl;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;

import com.alibaba.citrus.logconfig.support.SecurityLogger;
import com.alibaba.citrus.turbine.auth.PageAuthorizationService;
import com.alibaba.citrus.util.ObjectUtil;

/**
 * 为页面授权的service。
 * 
 * @author Michael Zhou
 */
public class PageAuthorizationServiceImpl implements PageAuthorizationService {
    private final SecurityLogger securityLogger = new SecurityLogger();

    private final List<AuthMatch> matches = createLinkedList();

    public void setMatches(AuthMatch[] matches) {
        this.matches.clear();

        if (matches != null) {
            for (AuthMatch match : matches) {
                this.matches.add(match);
            }
        }
    }

    public String getLogName() {
        return this.securityLogger.getLogger().getName();
    }

    public void setLogName(String logName) {
        this.securityLogger.setLogName(logName);
    }

    public boolean isAllow(String target, String userName, String[] roleNames, String... actions) {
        userName = trimToNull(userName);

        if (actions == null) {
            actions = new String[] { null };
        }

        for (int i = 0; i < actions.length; i++) {
            actions[i] = trimToEmpty(actions[i]);
        }

        String roleNameStr = ObjectUtil.toString(roleNames);
        String actionStr = ObjectUtil.toString(actions);

        // 找出所有匹配的pattern，按匹配长度倒排序。
        MatchResult[] results = getMatchResults(target);

        if (isEmptyArray(results)) {
            log(true, "Access Denied: no patterns matched", target, userName, roleNameStr, actionStr, null);
            return false;
        }

        for (String action : actions) {
            if (!isActionAllowed(results, target, userName, roleNames, roleNameStr, action)) {
                return false;
            }
        }

        return true;
    }

    private boolean isActionAllowed(MatchResult[] results, String target, String userName, String[] roleNames,
                                    String roleNameStr, String action) {
        // 按顺序检查授权，直到role或user被allow或deny
        for (MatchResult result : results) {
            AuthMatch match = result.match;

            for (AuthGrant grant : match.getGrants()) {
                // 判断user或role是否匹配
                boolean userMatch = grant.isUserMatched(userName);
                boolean roleMatch = grant.areRolesMatched(roleNames);

                if (!userMatch && !roleMatch) {
                    continue;
                }

                // 判断action是否匹配
                boolean actionAllowed = grant.isActionAllowed(action);
                boolean actionDenied = grant.isActionDenied(action);

                if (!actionAllowed && !actionDenied) {
                    continue;
                }

                boolean allowed = !actionDenied;

                if (allowed) {
                    log(false, "Access Permitted: ", target, userName, roleNameStr, action, match);
                } else {
                    log(true, "Access Denied: ", target, userName, roleNameStr, action, match);
                }

                return allowed;
            }
        }

        // 默认为拒绝
        log(true, "Access Denied: user or role has not be authorized", target, userName, roleNameStr, action, null);

        return false;
    }

    private MatchResult[] getMatchResults(String target) {
        List<MatchResult> results = createArrayList(matches.size());

        // 匹配所有，注意，这里按倒序匹配，这样长度相同的匹配，以后面的为准。
        for (ListIterator<AuthMatch> i = matches.listIterator(matches.size()); i.hasPrevious();) {
            AuthMatch match = i.previous();
            Matcher matcher = match.getPattern().matcher(target);

            if (matcher.find()) {
                MatchResult result = new MatchResult();
                result.matchLength = matcher.end() - matcher.start();
                result.match = match;

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

    private void log(boolean warn, String message, String target, String userName, String roleNameStr,
                     String actionStr, AuthMatch match) {
        String format = match == null ? "{}: target=\"{}\", user=\"{}\", roles=\"{}\", action=\"{}\""
                : "{}: target=\"{}\", user=\"{}\", roles=\"{}\", action=\"{}\"\n{}";

        if (warn) {
            securityLogger.getLogger().warn(format,
                    new Object[] { message, target, userName, roleNameStr, actionStr, match });
        } else {
            securityLogger.getLogger().debug(format,
                    new Object[] { message, target, userName, roleNameStr, actionStr, match });
        }
    }

    private static class MatchResult implements Comparable<MatchResult> {
        private int matchLength = -1;
        private AuthMatch match;

        public int compareTo(MatchResult o) {
            return o.matchLength - matchLength;
        }
    }
}
