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
package com.alibaba.citrus.service.requestcontext.basic.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.net.URI;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.basic.CookieHeaderValueInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.CookieInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.CookieRejectedException;
import com.alibaba.citrus.service.requestcontext.basic.HeaderNameInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.HeaderValueInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.RedirectLocationInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.RedirectLocationRejectedException;
import com.alibaba.citrus.service.requestcontext.basic.RequestContextLifecycleInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.ResponseHeaderRejectedException;
import com.alibaba.citrus.service.requestcontext.basic.StatusMessageInterceptor;
import com.alibaba.citrus.service.requestcontext.support.AbstractResponseWrapper;
import com.alibaba.citrus.service.requestcontext.util.CookieSupport;
import com.alibaba.citrus.util.StringEscapeUtil;

/**
 * 包裹<code>HttpServletResponse</code>，使之具备：
 * <ul>
 * <li>Header的安全性：过滤CRLF。</li>
 * <li>Cookie的安全性：限制cookie的大小。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class BasicResponseImpl extends AbstractResponseWrapper {
    private static final String LOCATION_HEADER = "Location";
    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    private final Object[] interceptors;

    public BasicResponseImpl(RequestContext context, HttpServletResponse response, Object[] interceptors) {
        super(context, response);

        if (interceptors == null) {
            this.interceptors = new Object[0];
        } else {
            this.interceptors = interceptors;
        }
    }

    @Override
    public void addDateHeader(String name, long date) {
        super.addDateHeader(checkHeaderName(name), date);
    }

    @Override
    public void setDateHeader(String name, long date) {
        super.setDateHeader(checkHeaderName(name), date);
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(checkHeaderName(name), value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(checkHeaderName(name), value);
    }

    @Override
    public void addHeader(String name, String value) {
        name = trimToNull(name);

        if (LOCATION_HEADER.equalsIgnoreCase(name)) {
            value = checkRedirectLocation(value, false);

            if (value != null) {
                super.setHeader(LOCATION_HEADER, value); // force SET header
            }
        } else if (SET_COOKIE_HEADER.equalsIgnoreCase(name)) {
            value = checkCookieHeaderValue(name, value, false);

            if (value != null) {
                super.addHeader(SET_COOKIE_HEADER, value);
            }
        } else {
            name = checkHeaderName(name);
            value = checkHeaderValue(name, value);

            if (value != null) {
                super.addHeader(name, value);
            }
        }
    }

    @Override
    public void setHeader(String name, String value) {
        name = trimToNull(name);

        if (LOCATION_HEADER.equalsIgnoreCase(name)) {
            value = checkRedirectLocation(value, false);

            if (value != null) {
                super.setHeader(LOCATION_HEADER, value);
            }
        } else if (SET_COOKIE_HEADER.equalsIgnoreCase(name)) {
            value = checkCookieHeaderValue(name, value, true);

            if (value != null) {
                super.setHeader(SET_COOKIE_HEADER, value);
            }
        } else {
            name = checkHeaderName(name);
            value = checkHeaderValue(name, value);

            if (value != null) {
                super.setHeader(name, value);
            }
        }
    }

    private String checkHeaderName(String name) throws ResponseHeaderRejectedException {
        String newName = assertNotNull(name, "header name is null"); // name==null报错

        for (Object interceptor : interceptors) {
            if (interceptor instanceof HeaderNameInterceptor) {
                newName = ((HeaderNameInterceptor) interceptor).checkHeaderName(newName);

                if (newName == null) {
                    break;
                }
            }
        }

        if (newName == null) {
            throw new ResponseHeaderRejectedException("HTTP header rejected: " + StringEscapeUtil.escapeJava(name));
        }

        return newName;
    }

    private String checkHeaderValue(String name, String value) throws ResponseHeaderRejectedException {
        if (value == null) {
            return null; // value==null返回
        }

        String newValue = value;

        for (Object interceptor : interceptors) {
            if (interceptor instanceof HeaderValueInterceptor) {
                newValue = ((HeaderValueInterceptor) interceptor).checkHeaderValue(name, newValue);

                if (newValue == null) {
                    break;
                }
            }
        }

        if (newValue == null) {
            throw new ResponseHeaderRejectedException("HTTP header rejected: " + StringEscapeUtil.escapeJava(name)
                    + "=" + StringEscapeUtil.escapeJava(value));
        }

        return newValue;
    }

    @Override
    public void addCookie(Cookie cookie) {
        Cookie newCookie = checkCookie(cookie);
        CookieSupport newCookieSupport;

        if (newCookie instanceof CookieSupport) {
            newCookieSupport = (CookieSupport) newCookie;
        } else {
            newCookieSupport = new CookieSupport(newCookie); // 将cookie强制转化成cookie support
        }

        newCookieSupport.addCookie(this); // 通过set-cookie header来添加cookie，以便统一监管
    }

    private Cookie checkCookie(Cookie cookie) throws CookieRejectedException {
        assertNotNull(cookie, "no cookie");

        Cookie newCookie = cookie;

        for (Object interceptor : interceptors) {
            if (interceptor instanceof CookieInterceptor) {
                newCookie = ((CookieInterceptor) interceptor).checkCookie(newCookie);

                if (newCookie == null) {
                    break;
                }
            }
        }

        if (newCookie == null) {
            throw new CookieRejectedException("Cookie rejected: " + StringEscapeUtil.escapeJava(cookie.getName()) + "="
                    + StringEscapeUtil.escapeJava(cookie.getValue()));
        }

        return newCookie;
    }

    private String checkCookieHeaderValue(String name, String value, boolean setHeader) throws CookieRejectedException {
        if (value == null) {
            return null; // value==null返回
        }

        String newValue = value;

        for (Object interceptor : interceptors) {
            if (interceptor instanceof CookieHeaderValueInterceptor) {
                newValue = ((CookieHeaderValueInterceptor) interceptor).checkCookieHeaderValue(name, newValue,
                        setHeader);

                if (newValue == null) {
                    break;
                }
            }
        }

        if (newValue == null) {
            throw new CookieRejectedException("Set-Cookie rejected: " + StringEscapeUtil.escapeJava(value));
        }

        return newValue;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        msg = checkStatusMessage(sc, msg);

        if (msg == null) {
            super.sendError(sc);
        } else {
            super.sendError(sc, msg);
        }
    }

    @Override
    @Deprecated
    public void setStatus(int sc, String msg) {
        msg = checkStatusMessage(sc, msg);

        if (msg == null) {
            super.setStatus(sc);
        } else {
            super.setStatus(sc, msg);
        }
    }

    private String checkStatusMessage(int sc, String msg) {
        if (msg != null) {
            for (Object interceptor : interceptors) {
                if (interceptor instanceof StatusMessageInterceptor) {
                    msg = ((StatusMessageInterceptor) interceptor).checkStatusMessage(sc, msg);

                    if (msg == null) {
                        break;
                    }
                }
            }
        }

        return msg;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        super.sendRedirect(checkRedirectLocation(location, true));
    }

    private String checkRedirectLocation(String location, boolean notNull) throws RedirectLocationRejectedException {
        String newLocation = trimToNull(location);

        if (newLocation == null && !notNull) {
            return null;
        }

        newLocation = normalizeLocation(newLocation, getRequestContext().getRequest());

        for (Object interceptor : interceptors) {
            if (interceptor instanceof RedirectLocationInterceptor) {
                newLocation = ((RedirectLocationInterceptor) interceptor).checkRedirectLocation(newLocation);

                if (newLocation == null) {
                    break;
                }
            }
        }

        if (newLocation == null) {
            throw new RedirectLocationRejectedException("Redirect location rejected: "
                    + StringEscapeUtil.escapeJava(location));
        }

        return newLocation;
    }

    static String normalizeLocation(String location, HttpServletRequest request) {
        location = assertNotNull(trimToNull(location), "no redirect location");

        URI locationURI = URI.create(location);

        if (!locationURI.isAbsolute()) {
            URI baseuri = URI.create(request.getRequestURL().toString());
            locationURI = baseuri.resolve(locationURI);
        }

        return locationURI.normalize().toString();
    }

    void prepareResponse() {
        for (Object interceptor : interceptors) {
            if (interceptor instanceof RequestContextLifecycleInterceptor) {
                ((RequestContextLifecycleInterceptor) interceptor).prepare();
            }
        }
    }

    void commitResponse() {
        for (int i = interceptors.length - 1; i >= 0; i--) {
            Object interceptor = interceptors[i];

            if (interceptor instanceof RequestContextLifecycleInterceptor) {
                ((RequestContextLifecycleInterceptor) interceptor).commit();
            }
        }
    }
}
