/*
 * Copyright 2010 Alibaba Group Holding Limited.
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
 *
 */
package com.alibaba.citrus.service.requestcontext.session.impl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig.CookieConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextWrapper;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestWrapper;
import com.alibaba.citrus.service.requestcontext.support.AbstractResponseWrapper;
import com.alibaba.citrus.service.requestcontext.util.CookieSupport;
import com.alibaba.citrus.util.StringUtil;

/**
 * 支持session的<code>HttpRequestContext</code>实现。
 */
public class SessionRequestContextImpl extends AbstractRequestContextWrapper implements SessionRequestContext {
    private final static Logger log = LoggerFactory.getLogger(SessionRequestContext.class);
    private SessionConfig sessionConfig;
    private boolean requestedSessionIDParsed;
    private String requestedSessionID;
    private boolean requestedSessionIDFromCookie;
    private boolean requestedSessionIDFromURL;
    private SessionImpl session;
    private boolean sessionReturned;

    /**
     * 构造函数。
     */
    public SessionRequestContextImpl(RequestContext wrappedContext, SessionConfig sessionConfig) {
        super(wrappedContext);
        this.sessionConfig = sessionConfig;
        setRequest(new SessionRequestWrapper(wrappedContext.getRequest()));
        setResponse(new SessionResponseWrapper(wrappedContext.getResponse()));
    }

    /**
     * 取得<code>SessionConfig</code>实例。
     * 
     * @return <code>SessionConfig</code>实例
     */
    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }

    /**
     * 判断session是否已经作废。
     * 
     * @return 如已作废，则返回<code>true</code>
     */
    public boolean isSessionInvalidated() {
        return session == null ? false : session.isInvalidated();
    }

    /**
     * 清除session。类似<code>invalidate()</code>方法，但支持后续操作，而不会抛出
     * <code>IllegalStateException</code>。
     */
    public void clear() {
        if (session != null) {
            session.clear();
        }
    }

    /**
     * 取得当前的session ID。
     * 
     * @return session ID
     */
    public String getRequestedSessionID() {
        ensureRequestedSessionID();
        return requestedSessionID;
    }

    /**
     * 当前的session ID是从cookie中取得的吗？
     * 
     * @return 如果是，则返回<code>true</code>
     */
    public boolean isRequestedSessionIDFromCookie() {
        ensureRequestedSessionID();
        return requestedSessionIDFromCookie;
    }

    /**
     * 当前的session ID是从URL中取得的吗？
     * 
     * @return 如果是，则返回<code>true</code>
     */
    public boolean isRequestedSessionIDFromURL() {
        ensureRequestedSessionID();
        return requestedSessionIDFromURL;
    }

    /**
     * 判断当前的session ID是否仍然合法。
     * 
     * @return 如果是，则返回<code>true</code>
     */
    public boolean isRequestedSessionIDValid() {
        HttpSession session = getSession(false);

        return session != null && session.getId().equals(requestedSessionID);
    }

    /**
     * 确保session ID已经从request中被解析出来了。
     */
    private void ensureRequestedSessionID() {
        if (!requestedSessionIDParsed) {
            if (sessionConfig.getId().isCookieEnabled()) {
                requestedSessionID = decodeSessionIDFromCookie();
                requestedSessionIDFromCookie = requestedSessionID != null;
            }

            if (requestedSessionID == null && sessionConfig.getId().isUrlEncodeEnabled()) {
                requestedSessionID = decodeSessionIDFromURL();
                requestedSessionIDFromURL = requestedSessionID != null;
            }
        }
    }

    /**
     * 将session ID编码到Cookie中去。
     */
    public void encodeSessionIDIntoCookie() {
        writeSessionIDCookie(session.getId());
    }

    /**
     * 将session ID从Cookie中删除。
     */
    public void clearSessionIDFromCookie() {
        writeSessionIDCookie("");
    }

    /**
     * 写cookie。
     */
    private void writeSessionIDCookie(String cookieValue) {
        CookieConfig cookieConfig = sessionConfig.getId().getCookie();
        CookieSupport cookie = new CookieSupport(cookieConfig.getName(), cookieValue);
        String cookieDomain = cookieConfig.getDomain();

        if (!StringUtil.isEmpty(cookieDomain)) {
            cookie.setDomain(cookieDomain);
        }

        String cookiePath = cookieConfig.getPath();

        if (!StringUtil.isEmpty(cookiePath)) {
            cookie.setPath(cookiePath);
        }

        int cookieMaxAge = cookieConfig.getMaxAge();

        if (cookieMaxAge > 0) {
            cookie.setMaxAge(cookieMaxAge);
        }

        cookie.setHttpOnly(cookieConfig.isHttpOnly());
        cookie.setSecure(cookieConfig.isSecure());

        log.debug("Set-cookie: {}", cookie);

        cookie.addCookie(getResponse());
    }

    /**
     * 从cookie中取得session ID。
     * 
     * @return 如果存在，则返回session ID，否则返回<code>null</code>
     */
    public String decodeSessionIDFromCookie() {
        Cookie[] cookies = getRequest().getCookies();

        if (cookies != null) {
            String sessionCookieName = sessionConfig.getId().getCookie().getName();

            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(sessionCookieName)) {
                    String sessionID = StringUtil.trimToNull(cookie.getValue());

                    if (sessionID != null) {
                        return sessionID;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 将session ID编码到URL中去。
     * 
     * @return 包含session ID的URL
     */
    public String encodeSessionIDIntoURL(String url) {
        HttpSession session = getRequest().getSession(false);

        if (session != null && (session.isNew() || isRequestedSessionIDFromURL() && !isRequestedSessionIDFromCookie())) {
            String sessionID = session.getId();
            String keyName = getSessionConfig().getId().getUrlEncode().getName();
            int keyNameLength = keyName.length();
            int urlLength = url.length();
            int urlQueryIndex = url.indexOf('?');

            if (urlQueryIndex >= 0) {
                urlLength = urlQueryIndex;
            }

            boolean found = false;

            for (int keyBeginIndex = url.indexOf(';'); keyBeginIndex >= 0 && keyBeginIndex < urlLength; keyBeginIndex = url
                    .indexOf(';', keyBeginIndex + 1)) {
                keyBeginIndex++;

                if (urlLength - keyBeginIndex <= keyNameLength
                        || !url.regionMatches(keyBeginIndex, keyName, 0, keyNameLength)
                        || url.charAt(keyBeginIndex + keyNameLength) != '=') {
                    continue;
                }

                int valueBeginIndex = keyBeginIndex + keyNameLength + 1;
                int valueEndIndex = url.indexOf(';', valueBeginIndex);

                if (valueEndIndex < 0) {
                    valueEndIndex = urlLength;
                }

                if (!url.regionMatches(valueBeginIndex, sessionID, 0, sessionID.length())) {
                    url = url.substring(0, valueBeginIndex) + sessionID + url.substring(valueEndIndex);
                }

                found = true;
                break;
            }

            if (!found) {
                url = url.substring(0, urlLength) + ';' + keyName + '=' + sessionID + url.substring(urlLength);
            }
        }

        return url;
    }

    /**
     * 从URL中取得session ID。
     * 
     * @return 如果存在，则返回session ID，否则返回<code>null</code>
     */
    public String decodeSessionIDFromURL() {
        String uri = getRequest().getRequestURI();
        String keyName = sessionConfig.getId().getUrlEncode().getName();
        int uriLength = uri.length();
        int keyNameLength = keyName.length();

        for (int keyBeginIndex = uri.indexOf(';'); keyBeginIndex >= 0; keyBeginIndex = uri.indexOf(';',
                keyBeginIndex + 1)) {
            keyBeginIndex++;

            if (uriLength - keyBeginIndex <= keyNameLength
                    || !uri.regionMatches(keyBeginIndex, keyName, 0, keyNameLength)
                    || uri.charAt(keyBeginIndex + keyNameLength) != '=') {
                continue;
            }

            int valueBeginIndex = keyBeginIndex + keyNameLength + 1;
            int valueEndIndex = uri.indexOf(';', valueBeginIndex);

            if (valueEndIndex < 0) {
                valueEndIndex = uriLength;
            }

            return uri.substring(valueBeginIndex, valueEndIndex);
        }

        return null;
    }

    /**
     * 取得当前的session，如果不存在，且<code>create</code>为<code>true</code>，则创建一个新的。
     * 
     * @param create 必要时是否创建新的session
     * @return 当前的session或新的session，如果不存在，且<code>create</code>为
     *         <code>false</code> ，则返回<code>null</code>
     */
    public HttpSession getSession(boolean create) {
        // 如果getSession方法已经被执行过了，那么直接返回
        if (session != null && sessionReturned) {
            return session;
        }

        // 创建session，尽管有可能创建却不返回
        if (session == null) {
            // 从request中取得session ID
            ensureRequestedSessionID();

            String sessionID = requestedSessionID;
            boolean isNew = false;

            // 如果sessionID为空，则创建一个新的ID
            if (sessionID == null) {
                if (!create) {
                    return null; // 除了create=false，直接返回null
                }

                sessionID = sessionConfig.getId().getGenerator().generateSessionID();
                isNew = true;
            }

            // 不管怎样，先创建一个session对象再说，但这个session有可能不存在或是过期的
            session = new SessionImpl(sessionID, this, isNew, create);
        }

        // Session为new，有可能是sessionID为空，或是sessionID对应的session不存在，或是session已过期
        // 如果同时create为false，返回null就可以了。
        if (session.isNew() && !create) {
            return null;
        }

        // 如果原来sessionID已存在于request中，不论session是否是新建与否，重用该sessionID。
        // 因此，在这种情况下，就不需要再设置cookie了。
        if (sessionConfig.getId().isCookieEnabled() && !session.getId().equals(requestedSessionID)) {
            if (getResponse().isCommitted()) {
                throw new IllegalStateException(
                        "Failed to create a new session because the responseWrapper was already committed");
            }

            encodeSessionIDIntoCookie();
        }

        sessionReturned = true;
        return session;
    }

    /**
     * 开始一个请求。
     */
    @Override
    public void prepare() {
    }

    /**
     * 结束一个请求。
     */
    @Override
    public void commit() {
        if (!sessionReturned) {
            return;
        }

        if (session.isInvalidated()) {
            // 清除cookie中的session ID
            clearSessionIDFromCookie();
        }

        session.commit();
    }

    /**
     * 支持session的<code>HttpServletRequestWrapper</code>。
     */
    private class SessionRequestWrapper extends AbstractRequestWrapper {
        /**
         * 构造函数。
         * 
         * @param request 被包装的<code>HttpServletRequest</code>实例
         */
        public SessionRequestWrapper(HttpServletRequest request) {
            super(SessionRequestContextImpl.this, request);
        }

        /**
         * 取得当前request中的session ID。
         * 
         * @return session ID
         */
        @Override
        public String getRequestedSessionId() {
            return SessionRequestContextImpl.this.getRequestedSessionID();
        }

        /**
         * 当前的session ID是从cookie中取得的吗？
         * 
         * @return 如果是，则返回<code>true</code>
         */
        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return SessionRequestContextImpl.this.isRequestedSessionIDFromCookie();
        }

        /**
         * 当前的session ID是从URL中取得的吗？
         * 
         * @return 如果是，则返回<code>true</code>
         */
        @Override
        public boolean isRequestedSessionIdFromURL() {
            return SessionRequestContextImpl.this.isRequestedSessionIDFromURL();
        }

        /**
         * 判断当前的session ID是否仍然合法。
         * 
         * @return 如果是，则返回<code>true</code>
         */
        @Override
        public boolean isRequestedSessionIdValid() {
            return SessionRequestContextImpl.this.isRequestedSessionIDValid();
        }

        /**
         * 取得当前的session，如果不存在，则创建一个新的。
         * 
         * @return 当前的session或新的session
         */
        @Override
        public HttpSession getSession() {
            return getSession(true);
        }

        /**
         * 取得当前的session，如果不存在，且<code>create</code>为<code>true</code>，则创建一个新的。
         * 
         * @param create 必要时是否创建新的session
         * @return 当前的session或新的session，如果不存在，且<code>create</code>为
         *         <code>false</code>，则返回<code>null</code>
         */
        @Override
        public HttpSession getSession(boolean create) {
            return SessionRequestContextImpl.this.getSession(create);
        }

        /**
         * @deprecated use isRequestedSessionIdFromURL instead
         */
        @Override
        @Deprecated
        public boolean isRequestedSessionIdFromUrl() {
            return isRequestedSessionIdFromURL();
        }
    }

    /**
     * 支持session的<code>HttpServletResponseWrapper</code>。
     */
    private class SessionResponseWrapper extends AbstractResponseWrapper {
        /**
         * 构造函数。
         * 
         * @param response 被包装的<code>HttpServletResponse</code>实例
         */
        public SessionResponseWrapper(HttpServletResponse response) {
            super(SessionRequestContextImpl.this, response);
        }

        /**
         * 将session ID编码到URL中。
         * 
         * @param url 要编码的URL
         * @return 包含session ID的URL
         */
        @Override
        public String encodeURL(String url) {
            if (sessionConfig.getId().isUrlEncodeEnabled()) {
                url = SessionRequestContextImpl.this.encodeSessionIDIntoURL(url);
            }

            return url;
        }

        /**
         * 将session ID编码到URL中。
         * 
         * @param url 要编码的URL
         * @return 包含session ID的URL
         */
        @Override
        public String encodeRedirectURL(String url) {
            if (sessionConfig.getId().isUrlEncodeEnabled()) {
                url = SessionRequestContextImpl.this.encodeSessionIDIntoURL(url);
            }

            return url;
        }

        /**
         * @deprecated use encodeURL instead
         */
        @Override
        @Deprecated
        public String encodeUrl(String url) {
            return encodeURL(url);
        }

        /**
         * @deprecated use encodeRedirectURL instead
         */
        @Override
        @Deprecated
        public String encodeRedirectUrl(String url) {
            return encodeRedirectURL(url);
        }
    }
}
