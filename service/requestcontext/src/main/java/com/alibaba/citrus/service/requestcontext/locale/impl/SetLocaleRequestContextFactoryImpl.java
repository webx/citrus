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

import static com.alibaba.citrus.service.requestcontext.locale.SetLocaleRequestContext.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Locale;
import java.util.regex.Pattern;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.locale.SetLocaleRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextFactory;
import com.alibaba.citrus.util.i18n.LocaleUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 创建<code>SetLocaleRequestContext</code>的工厂。
 * 
 * @author Michael Zhou
 */
public class SetLocaleRequestContextFactoryImpl extends AbstractRequestContextFactory<SetLocaleRequestContext> {
    private String inputCharsetParam;
    private Pattern inputCharsetPattern;
    private String outputCharsetParam;
    private Pattern outputCharsetPattern;
    private String defaultLocaleName;
    private Locale defaultLocale;
    private String defaultCharset;
    private String sessionKey;
    private String paramKey;

    public void setInputCharsetParam(String inputCharsetParam) {
        this.inputCharsetParam = trimToNull(inputCharsetParam);
    }

    public void setOutputCharsetParam(String outputCharsetParam) {
        this.outputCharsetParam = trimToNull(outputCharsetParam);
    }

    public void setDefaultLocale(String defaultLocaleName) {
        this.defaultLocaleName = trimToNull(defaultLocaleName);
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = trimToNull(defaultCharset);
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = trimToNull(sessionKey);
    }

    public void setParamKey(String paramKey) {
        this.paramKey = trimToNull(paramKey);
    }

    @Override
    protected void init() {
        inputCharsetParam = defaultIfNull(inputCharsetParam, INPUT_CHARSET_PARAM_DEFAULT);
        inputCharsetPattern = Pattern.compile(inputCharsetParam + "=([\\w-]+)");

        outputCharsetParam = defaultIfNull(outputCharsetParam, OUTPUT_CHARSET_PARAM_DEFAULT);
        outputCharsetPattern = Pattern.compile(outputCharsetParam + "=([\\w-]+)");

        sessionKey = defaultIfNull(sessionKey, SESSION_KEY_DEFAULT);
        paramKey = defaultIfNull(paramKey, PARAMETER_KEY_DEFAULT);

        defaultLocaleName = defaultIfNull(defaultLocaleName, LOCALE_DEFAULT);
        defaultLocale = LocaleUtil.parseLocale(defaultLocaleName);

        assertTrue(LocaleUtil.isLocaleSupported(defaultLocale), "Locale %s is not supported", defaultLocale);

        defaultCharset = defaultIfNull(defaultCharset, CHARSET_DEFAULT);

        assertTrue(LocaleUtil.isCharsetSupported(defaultCharset), "Charset %s is not supported", defaultCharset);
    }

    /**
     * 包装一个request context。
     * 
     * @param wrappedContext 被包装的<code>RequestContext</code>对象
     * @return request context
     */
    public SetLocaleRequestContext getRequestContextWrapper(RequestContext wrappedContext) {
        SetLocaleRequestContextImpl requestContext = new SetLocaleRequestContextImpl(wrappedContext);

        requestContext.setInputCharsetPattern(inputCharsetPattern);
        requestContext.setOutputCharsetPattern(outputCharsetPattern);
        requestContext.setDefaultLocale(defaultLocale);
        requestContext.setDefaultCharset(defaultCharset);
        requestContext.setSessionKey(sessionKey);
        requestContext.setParamKey(paramKey);

        return requestContext;
    }

    /**
     * 本类提供了设置多语言环境的功能。
     */
    public String[] getFeatures() {
        return new String[] { "setLocaleAndCharset" };
    }

    /**
     * 假如网站支持resource bundle和多语言环境，那么本类提供的request context会把locale信息保存在session里。
     * 假设我们要使用基于cookie的session，那么本类必须放在提供session功能的request
     * context之后。不过，基于cookie的session机制并不是必须的。
     */
    public FeatureOrder[] featureOrders() {
        return new FeatureOrder[] { new AfterFeature("session") };
    }

    @Override
    protected Object dumpConfiguration() {
        MapBuilder mb = new MapBuilder();

        mb.append("Input Charset Pattern", inputCharsetParam);
        mb.append("Output Charset Pattern", outputCharsetParam);
        mb.append("Default Locale", defaultLocaleName);
        mb.append("Default Charset", defaultCharset);
        mb.append("Session Key", sessionKey);
        mb.append("Parameter Key", paramKey);

        return mb;
    }
}
