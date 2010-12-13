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
package com.alibaba.citrus.service.requestcontext.util;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.requestcontext.util.tomcat.ServerCookie;

/**
 * 扩展原cookie，使之支持HttpOnly cookie。
 * 
 * @author Michael Zhou
 */
public class CookieSupport extends Cookie {
    private final static Logger log = LoggerFactory.getLogger(CookieSupport.class);
    private final static FastMethod getHttpOnlyMethod = getHttpOnlyMethod();
    private boolean httpOnly;

    /**
     * 创建一个cookie。
     */
    public CookieSupport(String name, String value) {
        super(assertNotNull(trimToNull(name), "cookieName"), value);
    }

    /**
     * 复制一个cookie。
     */
    public CookieSupport(Cookie cookie) {
        this(cookie, null);
    }

    /**
     * 复制一个cookie，修改cookie的名称。
     */
    public CookieSupport(Cookie cookie, String name) {
        super(assertNotNull(getCookieName(cookie, name), "cookieName"), cookie.getValue());

        setVersion(cookie.getVersion());
        setMaxAge(cookie.getMaxAge());
        setSecure(cookie.getSecure());

        String comment = cookie.getComment();

        if (!isEmpty(comment)) {
            setComment(comment);
        }

        String domain = cookie.getDomain();

        if (!isEmpty(domain)) {
            setDomain(domain);
        }

        String path = cookie.getPath();

        if (!isEmpty(path)) {
            setPath(path);
        }

        if (cookie instanceof CookieSupport) {
            setHttpOnly(((CookieSupport) cookie).getHttpOnly());
        } else if (getHttpOnlyMethod != null) {
            try {
                setHttpOnly((Boolean) getHttpOnlyMethod.invoke(cookie, EMPTY_OBJECT_ARRAY));
            } catch (InvocationTargetException e) {
                log.warn("Invocation of Cookie.isHttpOnly() failed", e.getTargetException());
            }
        }
    }

    /**
     * 对于servlet spec 3.0，已经支持<code>isHttpOnly</code>方法。
     */
    private static FastMethod getHttpOnlyMethod() {
        Method m = null;

        try {
            m = Cookie.class.getMethod("isHttpOnly"); // servlet 3.0 spec draft
        } catch (Exception e) {
            try {
                m = Cookie.class.getMethod("getHttpOnlyMethod"); // 另一种可能
            } catch (Exception ee) {
            }
        }

        if (m != null) {
            log.debug("Method Cookie.isHttpOnly() defined in current version of servlet api.  CookieSupport will make use of it.");
            return FastClass.create(Cookie.class).getMethod(m);
        }

        log.debug("No method Cookie.isHttpOnly() defined in current version of servlet api.");

        return null;
    }

    private static String getCookieName(Cookie cookie, String name) {
        name = trimToNull(name);

        if (name == null) {
            name = trimToNull(cookie.getName());
        }

        return name;
    }

    /**
     * 是否生成IE6支持的HttpOnly标记。
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * 是否生成IE6支持的HttpOnly标记。
     */
    public boolean getHttpOnly() {
        return httpOnly;
    }

    /**
     * 是否生成IE6支持的HttpOnly标记。
     */
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    @Override
    public void setDomain(String domain) {
        domain = trimToEmpty(domain);

        if (!isEmpty(domain) && !domain.startsWith(".")) {
            domain = "." + domain;
        }

        super.setDomain(domain); // 根据RFC2109，确保以“.”为前缀
    }

    /**
     * 将cookie添加到response中。
     */
    public void addCookie(HttpServletResponse response) {
        response.addHeader(getCookieHeaderName(), getCookieHeaderValue());
    }

    /**
     * 取得cookie header的名称。
     */
    public String getCookieHeaderName() {
        return ServerCookie.getCookieHeaderName(getVersion());
    }

    /**
     * 取得cookie header的值。
     * 
     * @throws IllegalArgumentException 假如cookie value中包含非法值
     */
    public String getCookieHeaderValue() throws IllegalArgumentException {
        return appendCookieHeaderValue(new StringBuilder()).toString();
    }

    private StringBuilder appendCookieHeaderValue(StringBuilder buf) throws IllegalArgumentException {
        int version = getVersion();
        String name = defaultIfNull(getName(), EMPTY_STRING);
        String value = getValue();
        String path = getPath();
        String domain = trimToNull(getDomain());
        String comment = trimToNull(getComment());
        int maxAge = isEmpty(getValue()) ? 0 : getMaxAge(); // empty value means remove cookie
        boolean secure = getSecure();
        boolean httpOnly = getHttpOnly();

        ServerCookie.appendCookieValue(buf, version, name, value, path, domain, comment, maxAge, secure, httpOnly);

        return buf;
    }

    /**
     * 生成set-cookie header的值，即使cookie value中包含非法值，也不会报错。
     * <p>
     * 请不要使用<code>toString()</code>方法来生成cookie header，而应该使用
     * <code>getCookieHeaderValue()</code>来取代。
     * </p>
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder().append(getCookieHeaderName()).append(": ");
        int length = buf.length();

        try {
            appendCookieHeaderValue(buf);
        } catch (IllegalArgumentException e) {
            buf.setLength(length);
            buf.append(e.getMessage());
        }

        return buf.toString();
    }
}
