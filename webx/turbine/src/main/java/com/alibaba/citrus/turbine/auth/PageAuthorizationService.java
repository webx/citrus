package com.alibaba.citrus.turbine.auth;

/**
 * 为页面授权的service。
 * 
 * @author Michael Zhou
 */
public interface PageAuthorizationService {
    boolean isAllow(String target, String userName, String[] roleNames, String... actions);
}
