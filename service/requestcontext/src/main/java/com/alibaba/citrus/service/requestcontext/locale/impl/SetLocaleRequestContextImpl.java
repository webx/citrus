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
package com.alibaba.citrus.service.requestcontext.locale.impl;

import static com.alibaba.citrus.util.StringUtil.*;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.locale.SetLocaleRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextWrapper;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestWrapper;
import com.alibaba.citrus.service.requestcontext.support.AbstractResponseWrapper;
import com.alibaba.citrus.util.i18n.LocaleInfo;
import com.alibaba.citrus.util.i18n.LocaleUtil;

/**
 * <code>SetLocaleRequestContext</code>的实现。
 * 
 * @author Michael Zhou
 */
public class SetLocaleRequestContextImpl extends AbstractRequestContextWrapper implements SetLocaleRequestContext {
    private final static Logger log = LoggerFactory.getLogger(SetLocaleRequestContext.class);
    private Pattern inputCharsetPattern;
    private Pattern outputCharsetPattern;
    private Locale defaultLocale;
    private String defaultCharset;
    private String sessionKey;
    private String paramKey;
    private Locale locale;

    /**
     * 包装一个<code>RequestContext</code>对象。
     * 
     * @param wrappedContext 被包装的<code>RequestContext</code>
     */
    public SetLocaleRequestContextImpl(RequestContext wrappedContext) {
        super(wrappedContext);
        setRequest(new RequestWrapper(wrappedContext.getRequest()));
        setResponse(new ResponseWrapper(wrappedContext.getResponse()));
    }

    public void setInputCharsetPattern(Pattern inputCharsetPattern) {
        this.inputCharsetPattern = inputCharsetPattern;
    }

    public void setOutputCharsetPattern(Pattern outputCharsetPattern) {
        this.outputCharsetPattern = outputCharsetPattern;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    /**
     * 取得content type。
     * 
     * @return content type，包括charset的定义
     */
    public String getResponseContentType() {
        return ((ResponseWrapper) getResponse()).getContentType();
    }

    /**
     * 设置content type。 如果content type不包含charset，并且
     * <code>getCharacterEncoding</code>被设置，则加上charset标记。
     * <p>
     * 如果<code>appendCharset</code>为<code>false</code>，则content
     * type中将不包含charset标记。
     * </p>
     * 
     * @param contentType content type
     * @param appendCharset 输出字符集
     */
    public void setResponseContentType(String contentType, boolean appendCharset) {
        ((ResponseWrapper) getResponse()).setContentType(contentType, appendCharset);
    }

    /**
     * 设置response输出字符集。注意，此方法必须在第一次<code>getWriter</code>之前执行。
     * 
     * @param charset 输出字符集，如果charset为<code>null</code>
     *            ，则从contentType中删除charset标记
     */
    public void setResponseCharacterEncoding(String charset) {
        ((ResponseWrapper) getResponse()).setCharacterEncoding(charset);
    }

    /**
     * 设置locale。
     */
    @Override
    public void prepare() {
        // 首先从session中取得input charset，并设置到request中，以便进一步解析request parameters。
        LocaleInfo locale = getLocaleFromSession();

        try {
            // 试图从queryString中取得inputCharset
            String queryString = getRequest().getQueryString();
            String inputCharset = locale.getCharset().name();

            if (queryString != null) {
                Matcher matcher = inputCharsetPattern.matcher(queryString);

                if (matcher.find()) {
                    String charset = matcher.group(1);

                    if (LocaleUtil.isCharsetSupported(charset)) {
                        inputCharset = charset;
                    } else {
                        log.warn("Specified input charset is not supported: " + charset);
                    }
                }
            }

            getRequest().setCharacterEncoding(inputCharset);

            log.debug("Set INPUT charset to " + inputCharset);
        } catch (UnsupportedEncodingException e) {
            try {
                getRequest().setCharacterEncoding(CHARSET_DEFAULT);

                log.warn("Unknown charset " + locale.getCharset() + ".  Set INPUT charset to " + CHARSET_DEFAULT);
            } catch (UnsupportedEncodingException ee) {
                log.error("Failed to set INPUT charset to " + locale.getCharset());
            }
        }

        // 从parameter中取locale信息，如果存在，则设置到cookie中。
        if (PARAMETER_SET_TO_DEFAULT_VALUE.equalsIgnoreCase(getRequest().getParameter(paramKey))) {
            HttpSession session = getRequest().getSession(false); // 如果session不存在，也不用创建

            if (session != null) {
                session.removeAttribute(sessionKey);
            }

            locale = new LocaleInfo(defaultLocale, defaultCharset);

            log.debug("Reset OUTPUT locale:charset to " + locale);
        } else {
            // 试图从queryString中取得outputCharset
            String queryString = getRequest().getQueryString();
            String outputCharset = null;

            if (queryString != null) {
                Matcher matcher = outputCharsetPattern.matcher(queryString);

                if (matcher.find()) {
                    String charset = matcher.group(1);

                    if (LocaleUtil.isCharsetSupported(charset)) {
                        outputCharset = charset;
                    } else {
                        log.warn("Specified output charset is not supported: " + charset);
                    }
                }
            }

            // 如果parameter中指明了locale，则取得并保存之
            LocaleInfo paramLocale = getLocaleFromParameter();

            if (paramLocale != null) {
                getRequest().getSession().setAttribute(sessionKey, paramLocale.toString());

                // 用parameter中的locale信息覆盖cookie的信息。
                locale = paramLocale;
            }

            if (outputCharset != null) {
                locale = new LocaleInfo(locale.getLocale(), outputCharset);
            }
        }

        // 设置用于输出的locale信息。
        getResponse().setLocale(locale.getLocale());
        setResponseCharacterEncoding(locale.getCharset().name());
        log.debug("Set OUTPUT locale:charset to " + locale);

        // 设置thread context中的locale信息。
        LocaleUtil.setContext(locale.getLocale(), locale.getCharset().name());
        log.debug("Set THREAD CONTEXT locale:charset to " + locale);

        this.locale = locale.getLocale();
    }

    /**
     * 从当前请求的session中取得用户的locale设置。如果session未设置，则取默认值。
     * 
     * @return 当前session中的locale设置
     */
    private LocaleInfo getLocaleFromSession() {
        HttpSession session = getRequest().getSession(false); // 如果session不存在，也不用创建。
        String localeName = session == null ? null : (String) getRequest().getSession().getAttribute(sessionKey);
        LocaleInfo locale = null;

        if (isEmpty(localeName)) {
            locale = new LocaleInfo(defaultLocale, defaultCharset);
        } else {
            locale = LocaleInfo.parse(localeName);

            if (!LocaleUtil.isLocaleSupported(locale.getLocale())
                    || !LocaleUtil.isCharsetSupported(locale.getCharset().name())) {
                log.warn("Invalid locale " + locale + " from session");

                locale = new LocaleInfo(defaultLocale, defaultCharset);
            }
        }

        return locale;
    }

    /**
     * 从当前请求的参数中取得用户的locale设置。如果参数未设置，则返回<code>null</code>。
     * 
     * @return 当前request parameters中的locale设置
     */
    private LocaleInfo getLocaleFromParameter() {
        String localeName = getRequest().getParameter(paramKey);
        LocaleInfo locale = null;

        if (!isEmpty(localeName)) {
            locale = LocaleInfo.parse(localeName);

            if (!LocaleUtil.isLocaleSupported(locale.getLocale())
                    || !LocaleUtil.isCharsetSupported(locale.getCharset().name())) {
                log.warn("Invalid locale " + locale + " from request parameters");

                locale = new LocaleInfo(defaultLocale, defaultCharset);
            }
        }

        return locale;
    }

    /**
     * 包装request。
     */
    private class RequestWrapper extends AbstractRequestWrapper {
        public RequestWrapper(HttpServletRequest request) {
            super(SetLocaleRequestContextImpl.this, request);
        }

        @Override
        public Locale getLocale() {
            return locale == null ? super.getLocale() : locale;
        }
    }

    /**
     * 包装response。
     */
    private class ResponseWrapper extends AbstractResponseWrapper {
        private String contentType;
        private String charset;

        public ResponseWrapper(HttpServletResponse response) {
            super(SetLocaleRequestContextImpl.this, response);
        }

        /**
         * 取得content type。
         * 
         * @return content type，包括charset的定义
         */
        @Override
        public String getContentType() {
            return contentType;
        }

        /**
         * 设置content type。 如果content type不包含charset，并且
         * <code>getCharacterEncoding</code>被设置，则加上charset标记。
         * 
         * @param contentType content type
         */
        @Override
        public void setContentType(String contentType) {
            setContentType(contentType, true);
        }

        /**
         * 设置content type。 如果content type不包含charset，并且
         * <code>getCharacterEncoding</code>被设置，则加上charset标记。
         * <p>
         * 如果<code>appendCharset</code>为<code>false</code>，则content
         * type中将不包含charset标记。
         * </p>
         * 
         * @param contentType content type
         * @param appendCharset 输出字符集
         */
        public void setContentType(String contentType, boolean appendCharset) {
            // 取得指定contentType中的"; charset="部分。
            String charset = trimToNull(substringAfterLast(contentType, "charset="));

            // 如果未指定charset，则从this.charset中取，这是由setCharacterEncoding方法所设置的。
            if (charset == null) {
                charset = this.charset;
            }

            // 除去contentType中的charset部分。
            this.contentType = trimToNull(substringBefore(contentType, ";"));

            // 调用setCharacterEncoding方法加上charset。
            setCharacterEncoding(appendCharset ? charset : null);
        }

        /**
         * 取得response的输出字符集。
         */
        @Override
        public String getCharacterEncoding() {
            return super.getCharacterEncoding();
        }

        /**
         * 设置response输出字符集。注意，此方法必须在第一次<code>getWriter</code>之前执行。
         * 
         * @param charset 输出字符集，如果charset为<code>null</code>
         *            ，则从contentType中删除charset标记
         */
        @Override
        public void setCharacterEncoding(String charset) {
            this.charset = charset;

            if (contentType != null) {
                contentType = trimToNull(substringBefore(contentType, ";"));

                if (charset != null) {
                    contentType += "; charset=" + charset;
                }

                log.debug("Set content type to " + contentType);

                super.setContentType(contentType);
            } else {
                // 假如没有设置contentType，确保charset仍然被设置。
                // 适用于Servlet API 2.4及更新版。
                try {
                    super.setCharacterEncoding(charset);
                } catch (NoSuchMethodError e) {
                }
            }
        }
    }
}
