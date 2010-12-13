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

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ExceptionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.session.ExactMatchesOnlySessionStore;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig.CookieConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig.IdConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig.StoreMappingsConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig.StoresConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig.UrlEncodeConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionIDGenerator;
import com.alibaba.citrus.service.requestcontext.session.SessionInterceptor;
import com.alibaba.citrus.service.requestcontext.session.SessionModelEncoder;
import com.alibaba.citrus.service.requestcontext.session.SessionRequestContext;
import com.alibaba.citrus.service.requestcontext.session.SessionStore;
import com.alibaba.citrus.service.requestcontext.session.idgen.uuid.impl.UUIDGenerator;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextFactory;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 用来创建和初始化<code>SessionRequestContext</code>的工厂。
 */
public class SessionRequestContextFactoryImpl extends AbstractRequestContextFactory<SessionRequestContext> {
    private final static Logger log = LoggerFactory.getLogger(SessionRequestContext.class);
    private final ConfigImpl config = new ConfigImpl();

    public SessionConfig getConfig() {
        return config;
    }

    /**
     * 初始化factory。
     */
    @Override
    protected void init() throws Exception {
        config.init();

        String storeName = config.getStoreMappings().getStoreNameForAttribute(config.getModelKey());

        if (storeName == null) {
            throw new IllegalArgumentException("No storage configured for session model: key=" + config.getModelKey());
        }
    }

    /**
     * 包装一个request context。
     * 
     * @param wrappedContext 被包装的<code>RequestContext</code>对象
     * @return request context
     */
    public SessionRequestContext getRequestContextWrapper(RequestContext wrappedContext) {
        return new SessionRequestContextImpl(wrappedContext, config);
    }

    /**
     * 本类提供了可扩展的session机制。
     */
    public String[] getFeatures() {
        return new String[] { "session" };
    }

    /**
     * 基于cookie的session机制，会在commit时修改cookie和headers，因而依赖于lazyCommit。
     * Session框架由于要读写cookie，因此在parser之后。
     */
    public FeatureOrder[] featureOrders() {
        return new FeatureOrder[] { new AfterFeature("parseRequest"), new RequiresFeature("lazyCommitHeaders") };
    }

    @Override
    protected Object dumpConfiguration() {
        return config;
    }

    // 实现SessionConfig。
    @SuppressWarnings("unused")
    private static class ConfigImpl implements SessionConfig {
        private final IdConfigImpl id = new IdConfigImpl();
        private final StoresConfigImpl stores = new StoresConfigImpl();
        private final StoreMappingsConfigImpl storeMappings = new StoreMappingsConfigImpl();
        private Integer maxInactiveInterval;
        private Long forceExpirationPeriod;
        private String modelKey;
        private Boolean keepInTouch;
        private SessionModelEncoder[] sessionModelEncoders;
        private SessionInterceptor[] sessionInterceptors;

        public int getMaxInactiveInterval() {
            return maxInactiveInterval;
        }

        public void setMaxInactiveInterval(int maxInactiveInterval) {
            this.maxInactiveInterval = maxInactiveInterval;
        }

        public long getForceExpirationPeriod() {
            return forceExpirationPeriod;
        }

        public void setForceExpirationPeriod(long forceExpirationPeriod) {
            this.forceExpirationPeriod = forceExpirationPeriod;
        }

        public String getModelKey() {
            return modelKey;
        }

        public void setModelKey(String modelKey) {
            this.modelKey = modelKey;
        }

        public boolean isKeepInTouch() {
            return keepInTouch;
        }

        public void setKeepInTouch(boolean keepInTouch) {
            this.keepInTouch = keepInTouch;
        }

        public IdConfig getId() {
            return id;
        }

        public StoresConfig getStores() {
            return stores;
        }

        public StoreMappingsConfig getStoreMappings() {
            return storeMappings;
        }

        public SessionModelEncoder[] getSessionModelEncoders() {
            return sessionModelEncoders;
        }

        public void setSessionModelEncoders(SessionModelEncoder[] sessionModelEncoders) {
            this.sessionModelEncoders = sessionModelEncoders;
        }

        public SessionInterceptor[] getSessionInterceptors() {
            return sessionInterceptors;
        }

        public void setSessionInterceptors(SessionInterceptor[] sessionInterceptors) {
            this.sessionInterceptors = sessionInterceptors;
        }

        private void init() throws Exception {
            maxInactiveInterval = defaultIfNull(maxInactiveInterval, MAX_INACTIVE_INTERVAL_DEFAULT);
            forceExpirationPeriod = defaultIfNull(forceExpirationPeriod, FORCE_EXPIRATION_PERIOD_DEFAULT);
            modelKey = defaultIfEmpty(modelKey, MODEL_KEY_DEFAULT);
            keepInTouch = defaultIfNull(keepInTouch, KEEP_IN_TOUCH_DEFAULT);

            id.init();
            stores.init(this);
            storeMappings.init(stores);

            // 对所有的ExactMatchesOnlySessionStore，设置attribute names。
            for (String storeName : stores.getStoreNames()) {
                SessionStore store = stores.getStore(storeName);

                if (store instanceof ExactMatchesOnlySessionStore) {
                    String[] exactMatchedAttrNames = storeMappings.getExactMatchedAttributeNames(storeName);

                    if (exactMatchedAttrNames == null) {
                        throw new IllegalArgumentException("Session store " + storeName
                                + " only support exact matches to attribute names");
                    }

                    ((ExactMatchesOnlySessionStore) store).initAttributeNames(exactMatchedAttrNames);
                }
            }

            if (isEmptyArray(sessionModelEncoders)) {
                sessionModelEncoders = new SessionModelEncoder[] { new SessionModelEncoderImpl() };
            }

            if (isEmptyArray(sessionInterceptors)) {
                sessionInterceptors = new SessionInterceptor[0];
            }

            for (SessionInterceptor l : sessionInterceptors) {
                l.init(this);
            }
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder();

            mb.append("maxInactiveInterval", String.format("%,d seconds (%,3.2f hours)", maxInactiveInterval,
                    (double) maxInactiveInterval / 3600));
            mb.append("forceExpirationPeriod", String.format("%,d seconds (%,3.2f hours)", forceExpirationPeriod,
                    (double) forceExpirationPeriod / 3600));
            mb.append("modelKey", modelKey);
            mb.append("keepInTouch", keepInTouch);
            mb.append("idConfig", id);
            mb.append("stores", stores);
            mb.append("storeMappings", storeMappings);
            mb.append("sessionModelEncoders", sessionModelEncoders);

            return new ToStringBuilder().append("SessionConfig").append(mb).toString();
        }
    }

    @SuppressWarnings("unused")
    private static class IdConfigImpl implements IdConfig {
        private final CookieConfigImpl cookie = new CookieConfigImpl();
        private final UrlEncodeConfigImpl urlEncode = new UrlEncodeConfigImpl();
        private Boolean cookieEnabled;
        private Boolean urlEncodeEnabled;
        private SessionIDGenerator generator;

        public boolean isCookieEnabled() {
            return cookieEnabled;
        }

        public void setCookieEnabled(boolean cookieEnabled) {
            this.cookieEnabled = cookieEnabled;
        }

        public boolean isUrlEncodeEnabled() {
            return urlEncodeEnabled;
        }

        public void setUrlEncodeEnabled(boolean urlEncodeEnabled) {
            this.urlEncodeEnabled = urlEncodeEnabled;
        }

        public CookieConfig getCookie() {
            return cookie;
        }

        public UrlEncodeConfig getUrlEncode() {
            return urlEncode;
        }

        public SessionIDGenerator getGenerator() {
            return generator;
        }

        public void setGenerator(SessionIDGenerator generator) {
            this.generator = generator;
        }

        private void init() {
            cookieEnabled = defaultIfNull(cookieEnabled, COOKIE_ENABLED_DEFAULT);
            urlEncodeEnabled = defaultIfNull(urlEncodeEnabled, URL_ENCODE_ENABLED_DEFAULT);

            if (generator == null) {
                generator = new UUIDGenerator();

                if (generator instanceof InitializingBean) {
                    try {
                        ((InitializingBean) generator).afterPropertiesSet();
                    } catch (Exception e) {
                        throw toRuntimeException(e);
                    }
                }
            }

            cookie.init();
            urlEncode.init();
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder();

            mb.append("cookieEnabled", cookieEnabled);
            mb.append("urlEncodeEnabled", urlEncodeEnabled);
            mb.append("cookieConfig", cookie);
            mb.append("urlEncodeConfig", urlEncode);
            mb.append("generator", generator);

            return new ToStringBuilder().append("IdConfig").append(mb).toString();
        }
    }

    @SuppressWarnings("unused")
    private static class CookieConfigImpl implements CookieConfig {
        private String name;
        private String domain;
        private String path;
        private Integer maxAge;
        private Boolean httpOnly;
        private Boolean secure;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            // normalize domain
            domain = trimToNull(domain);

            if (domain != null && !domain.startsWith(".")) {
                domain = "." + domain;
            }

            this.domain = domain;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(int maxAge) {
            this.maxAge = maxAge;
        }

        public boolean isHttpOnly() {
            return httpOnly;
        }

        public void setHttpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
        }

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }

        private void init() {
            name = defaultIfEmpty(name, COOKIE_NAME_DEFAULT);
            domain = defaultIfEmpty(domain, COOKIE_DOMAIN_DEFAULT);
            path = defaultIfEmpty(path, COOKIE_PATH_DEFAULT);
            maxAge = defaultIfNull(maxAge, COOKIE_MAX_AGE_DEFAULT);
            httpOnly = defaultIfNull(httpOnly, COOKIE_HTTP_ONLY_DEFAULT);
            secure = defaultIfNull(secure, COOKIE_SECURE_DEFAULT);
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder();

            mb.append("name", name);
            mb.append("domain", domain);
            mb.append("path", path);
            mb.append("maxAge", String.format("%,d seconds", maxAge));
            mb.append("httpOnly", httpOnly);
            mb.append("secure", secure);

            return new ToStringBuilder().append("CookieConfig").append(mb).toString();
        }
    }

    @SuppressWarnings("unused")
    private static class UrlEncodeConfigImpl implements UrlEncodeConfig {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private void init() {
            name = defaultIfEmpty(name, URL_ENCODE_NAME_DEFAULT);
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder();

            mb.append("name", name);

            return new ToStringBuilder().append("UrlEncodeConfig").append(mb).toString();
        }
    }

    @SuppressWarnings("unused")
    private static class StoresConfigImpl implements StoresConfig {
        private Map<String, SessionStore> stores;

        public void setStores(LinkedHashMap<String, SessionStore> stores) {
            this.stores = stores;
        }

        public SessionStore getStore(String storeName) {
            return stores.get(storeName);
        }

        public String[] getStoreNames() {
            return stores.keySet().toArray(new String[stores.size()]);
        }

        private void init(SessionConfig sessionConfig) throws Exception {
            if (stores == null) {
                stores = createLinkedHashMap();
            }

            // 初始化所有stores
            for (Map.Entry<String, SessionStore> entry : stores.entrySet()) {
                entry.getValue().init(entry.getKey(), sessionConfig);
            }
        }

        @Override
        public String toString() {
            return new ToStringBuilder().append("Stores").append(stores).toString();
        }
    }

    @SuppressWarnings("unused")
    private static class StoreMappingsConfigImpl implements StoreMappingsConfig {
        private AttributePattern[] patterns;
        private String defaultStore;
        private Map<String, String> attributeMatchCache;

        public void setPatterns(AttributePattern[] patterns) {
            this.patterns = patterns;
        }

        private void init(StoresConfig stores) {
            this.attributeMatchCache = createConcurrentHashMap();

            if (patterns == null) {
                patterns = new AttributePattern[0];
            }

            for (AttributePattern pattern : patterns) {
                if (pattern.isDefaultPattern()) {
                    if (defaultStore != null) {
                        throw new IllegalArgumentException("More than one stores mapped to *: " + defaultStore
                                + " and " + pattern.getStoreName());
                    }

                    defaultStore = pattern.getStoreName();
                }

                if (stores.getStore(pattern.getStoreName()) == null) {
                    throw new IllegalArgumentException("Undefined Session Store: " + pattern);
                }
            }
        }

        public String getStoreNameForAttribute(String attrName) {
            attrName = assertNotNull(trimToNull(attrName), "attrName");
            String matchedStoreName = attributeMatchCache.get(attrName);

            if (matchedStoreName != null) {
                return matchedStoreName;
            } else {
                List<AttributeMatch> matches = createArrayList(patterns.length);

                for (AttributePattern pattern : patterns) {
                    if (pattern.isDefaultPattern()) {
                        matches.add(new AttributeMatch(pattern, 0));
                    } else if (pattern.isRegexPattern()) {
                        Matcher matcher = pattern.getPattern().matcher(attrName);

                        if (matcher.find()) {
                            matches.add(new AttributeMatch(pattern, matcher.end() - matcher.start()));
                        }
                    } else {
                        if (pattern.patternName.equals(attrName)) {
                            matches.add(new AttributeMatch(pattern, pattern.patternName.length()));
                        }
                    }
                }

                // 最长匹配优先
                Collections.sort(matches);

                if (log.isTraceEnabled()) {
                    ToStringBuilder buf = new ToStringBuilder();

                    buf.format("Attribute \"%s\" ", attrName);

                    if (matches.isEmpty()) {
                        buf.append("does not match any pattern");
                    } else {
                        buf.append("matches the following CANDIDATED patterns:").append(matches);
                    }

                    log.trace(buf.toString());
                }

                if (!matches.isEmpty()) {
                    matchedStoreName = matches.get(0).pattern.getStoreName();
                }

                if (matchedStoreName != null) {
                    attributeMatchCache.put(attrName, matchedStoreName);
                }
            }

            if (log.isDebugEnabled() && matchedStoreName != null) {
                log.debug("Session attribute {} is handled by session store: {}", attrName, matchedStoreName);
            }

            return matchedStoreName;
        }

        public String[] getExactMatchedAttributeNames(String storeName) {
            storeName = assertNotNull(trimToNull(storeName), "no storeName");

            Set<String> attrNames = createLinkedHashSet();

            for (AttributePattern pattern : patterns) {
                if (pattern.getStoreName().equals(storeName)) {
                    // 如果是非精确匹配，则返回null。
                    if (pattern.isDefaultPattern() || pattern.isRegexPattern()) {
                        return null;
                    }

                    attrNames.add(pattern.patternName);
                }
            }

            return attrNames.toArray(new String[attrNames.size()]);
        }

        @Override
        public String toString() {
            return new ToStringBuilder().append("StoreMappings").append(patterns).toString();
        }
    }

    /**
     * 代表一个attribute的匹配。
     */
    private static class AttributeMatch implements Comparable<AttributeMatch> {
        private final AttributePattern pattern;
        private final int matchLength;

        public AttributeMatch(AttributePattern pattern, int matchLength) {
            this.pattern = pattern;
            this.matchLength = matchLength;
        }

        /**
         * 先比较匹配长度，较长的优先。其次比较匹配的类型，精确匹配比正则表达式匹配优先。
         */
        public int compareTo(AttributeMatch o) {
            int result = o.matchLength - matchLength;

            if (result == 0) {
                int r1 = pattern.isRegexPattern() ? 0 : 1;
                int r2 = o.pattern.isRegexPattern() ? 0 : 1;

                return r2 - r1;
            }

            return result;
        }

        @Override
        public String toString() {
            return new ToStringBuilder().append(pattern).append(", matchLength=").append(matchLength).toString();
        }
    }

    /**
     * 代表一个pattern的信息。
     */
    static class AttributePattern {
        public final String patternName;
        public final String storeName;
        public final Pattern pattern;

        /**
         * 创建默认匹配，匹配所有attribute names。
         */
        public static AttributePattern getDefaultPattern(String storeName) {
            return new AttributePattern(storeName, null, null);
        }

        /**
         * 创建精确匹配，匹配名称完全相同的attribute names。
         */
        public static AttributePattern getExactPattern(String storeName, String attrName) {
            return new AttributePattern(storeName, attrName, null);
        }

        /**
         * 创建正则表达式匹配。
         */
        public static AttributePattern getRegexPattern(String storeName, String regexName) {
            try {
                return new AttributePattern(storeName, regexName, Pattern.compile(regexName));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("Invalid regex pattern %s for store %s", regexName,
                        storeName));
            }
        }

        private AttributePattern(String storeName, String patternName, Pattern pattern) {
            this.storeName = assertNotNull(trimToNull(storeName), "storeName");
            this.patternName = patternName;
            this.pattern = pattern;
        }

        public boolean isDefaultPattern() {
            return patternName == null;
        }

        public boolean isRegexPattern() {
            return pattern != null;
        }

        public String getPatternName() {
            return patternName;
        }

        public String getStoreName() {
            return storeName;
        }

        public Pattern getPattern() {
            return pattern;
        }

        @Override
        public String toString() {
            ToStringBuilder buf = new ToStringBuilder();

            if (isDefaultPattern()) {
                buf.format("match=\"*\", store=\"%s\"", storeName);
            } else if (isRegexPattern()) {
                buf.format("match=~/%s/, store=\"%s\"", patternName, storeName);
            } else {
                buf.format("match=\"%s\", store=\"%s\"", patternName, storeName);
            }

            return buf.toString();
        }
    }
}
