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

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.logconfig.support.SecurityLogger;
import com.alibaba.citrus.service.requestcontext.basic.CookieHeaderValueInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.CookieInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.HeaderNameInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.HeaderValueInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.RedirectLocationInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.RequestContextLifecycleInterceptor;
import com.alibaba.citrus.service.requestcontext.basic.ResponseHeaderRejectedException;
import com.alibaba.citrus.service.requestcontext.basic.StatusMessageInterceptor;
import com.alibaba.citrus.service.requestcontext.util.CookieSupport;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.util.HumanReadableSize;
import com.alibaba.citrus.util.StringEscapeUtil;

/**
 * 过滤header中的crlf，将status message用HTML entities转义，限制cookie的总大小。
 * 
 * @author Michael Zhou
 */
public class ResponseHeaderSecurityFilter implements RequestContextLifecycleInterceptor, HeaderNameInterceptor,
        HeaderValueInterceptor, CookieInterceptor, CookieHeaderValueInterceptor, StatusMessageInterceptor,
        RedirectLocationInterceptor {
    public static final HumanReadableSize MAX_SET_COOKIE_SIZE_DEFAULT = new HumanReadableSize("7k");
    private static final String COOKIE_LENGTH_ATTR = "_COOKIE_LENGTH_";
    private static final Pattern crlf = Pattern.compile("\\r\\n|\\r|\\n");
    private final SecurityLogger log = new SecurityLogger();
    private final CookieLengthAccumulator cookieLengthAccumulator;
    private HumanReadableSize maxSetCookieSize;

    public ResponseHeaderSecurityFilter() {
        this(null);
    }

    public ResponseHeaderSecurityFilter(HttpServletRequest request) {
        // 注意，此request无法从@Autowired中注入，因为此时BeanPostProcessor还未初始化。但通过constructor注入是可行的。
        assertProxy(request);

        if (request == null) {
            cookieLengthAccumulator = new ThreadLocalBasedCookieLengthAccumulator();
        } else {
            cookieLengthAccumulator = new RequestBasedCookieLengthAccumulator(request);
        }
    }

    public void setLogName(String logName) {
        log.setLogName(logName);
    }

    public HumanReadableSize getMaxSetCookieSize() {
        return maxSetCookieSize == null || maxSetCookieSize.getValue() <= 0 ? MAX_SET_COOKIE_SIZE_DEFAULT
                : maxSetCookieSize;
    }

    public void setMaxSetCookieSize(HumanReadableSize maxSetCookieSize) {
        this.maxSetCookieSize = maxSetCookieSize;
    }

    public void prepare() {
    }

    public void commit() {
        cookieLengthAccumulator.reset();
    }

    public String checkHeaderName(String name) {
        if (containsCRLF(name)) {
            String msg = "Invalid response header: " + StringEscapeUtil.escapeJava(name);
            log.getLogger().error(msg);
            throw new ResponseHeaderRejectedException(msg);
        }

        return name;
    }

    public String checkHeaderValue(String name, String value) {
        return defaultIfNull(filterCRLF(value, "header " + name), value);
    }

    public Cookie checkCookie(Cookie cookie) {
        String name = cookie.getName();

        if (containsCRLF(name)) {
            log.getLogger().error("Invalid cookie name: " + StringEscapeUtil.escapeJava(name));
            return null;
        }

        String value = cookie.getValue();
        String filteredValue = filterCRLF(value, "cookie " + name);

        if (filteredValue == null) {
            return cookie;
        } else {
            CookieSupport newCookie = new CookieSupport(cookie);
            newCookie.setValue(filteredValue);
            return newCookie;
        }
    }

    public String checkCookieHeaderValue(String name, String value, boolean setHeader) {
        if (value != null) {
            int maxSetCookieSize = (int) getMaxSetCookieSize().getValue();
            int length = cookieLengthAccumulator.getLength();

            if (length + value.length() > maxSetCookieSize) {
                log.getLogger().error(
                        "Cookie size exceeds the max value: {} + {} > maxSize {}.  Cookie is ignored: {}",
                        new Object[] { length, value.length(), getMaxSetCookieSize(), value });

                return EMPTY_STRING;
            } else {
                if (setHeader) {
                    cookieLengthAccumulator.setCookie(value);
                } else {
                    cookieLengthAccumulator.addCookie(value);
                }
            }
        }

        return value;
    }

    public String checkStatusMessage(int sc, String msg) {
        return StringEscapeUtil.escapeHtml(msg);
    }

    public String checkRedirectLocation(String location) {
        return defaultIfNull(filterCRLF(location, "redirectLocation"), location);
    }

    private boolean containsCRLF(String str) {
        if (str != null) {
            for (int i = 0; i < str.length(); i++) {
                switch (str.charAt(i)) {
                    case '\r':
                    case '\n':
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * 如果不包含CRLF，则返回<code>null</code>，否则除去所有CRLF，替换成空格。
     */
    private String filterCRLF(String value, String logInfo) {
        if (containsCRLF(value)) {
            log.getLogger().warn("Found CRLF in {}: {}", logInfo, StringEscapeUtil.escapeJava(value));

            StringBuffer sb = new StringBuffer();
            Matcher m = crlf.matcher(value);

            while (m.find()) {
                m.appendReplacement(sb, " ");
            }

            m.appendTail(sb);

            return sb.toString();
        }

        return null;
    }

    private static abstract class CookieLengthAccumulator {
        public final void addCookie(String cookie) {
            setLength(getLength() + cookie.length());
        }

        public final void setCookie(String cookie) {
            setLength(cookie.length());
        }

        public abstract int getLength();

        protected abstract void setLength(int length);

        protected abstract void reset();
    }

    private final class ThreadLocalBasedCookieLengthAccumulator extends CookieLengthAccumulator {
        private final ThreadLocal<Integer> cookieLengthHolder = new ThreadLocal<Integer>();

        @Override
        public int getLength() {
            Object value = cookieLengthHolder.get();

            if (value instanceof Integer) {
                return (Integer) value;
            } else {
                return 0;
            }
        }

        @Override
        protected void setLength(int length) {
            cookieLengthHolder.set(length);
        }

        @Override
        protected void reset() {
            cookieLengthHolder.remove();
        }
    }

    private final class RequestBasedCookieLengthAccumulator extends CookieLengthAccumulator {
        private final HttpServletRequest request;

        private RequestBasedCookieLengthAccumulator(HttpServletRequest request) {
            this.request = request;
        }

        @Override
        public int getLength() {
            Object value = request.getAttribute(COOKIE_LENGTH_ATTR);

            if (value instanceof Integer) {
                return (Integer) value;
            } else {
                return 0;
            }
        }

        @Override
        protected void setLength(int length) {
            request.setAttribute(COOKIE_LENGTH_ATTR, length);
        }

        @Override
        protected void reset() {
            request.removeAttribute(COOKIE_LENGTH_ATTR);
        }
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<ResponseHeaderSecurityFilter> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "logName", "maxSetCookieSize");
            addConstructorArg(builder, true, HttpServletRequest.class); // 依赖request
        }
    }
}
