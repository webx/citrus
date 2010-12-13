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
package com.alibaba.citrus.turbine.util;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.ecs.html.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pull.ToolFactory;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.util.ClassLoaderUtil;
import com.alibaba.citrus.util.StringUtil;

/**
 * 用于生成一个唯一的ID，来防止CSRF攻击（Cross Site Request Forgery）。 此外，还可以用来防止重复提交同一张表单。
 * <p>
 * 该类可作为pull tool，由于采用了singleton request proxy，所以该类可被注册成global作用域的pull tool。
 * </p>
 * <p>
 * CSRF token的key是按照以下逻辑取得的：
 * </p>
 * <ol>
 * <li>如果Thread上下文<code>setContextTokenKey()</code>被明确设置，则使用它；</li>
 * <li>否则，使用默认值“<code>_csrf_token</code>”。</li>
 * </ol>
 * 
 * @author Michael Zhou
 */
public class CsrfToken {
    public static final String DEFAULT_TOKEN_KEY = "_csrf_token";
    public static final int DEFAULT_MAX_TOKENS = 8;
    public static final String CSRF_TOKEN_SEPARATOR = "/";
    private static final AtomicInteger counter = new AtomicInteger();;
    private static final ThreadLocal<Configuration> contextTokenConfigurationHolder = new ThreadLocal<Configuration>();
    private final HttpServletRequest request;

    public CsrfToken(HttpServletRequest request) {
        this.request = assertNotNull(request, "request");
    }

    public static String getKey() {
        String key = null;
        Configuration conf = contextTokenConfigurationHolder.get();

        if (conf != null) {
            key = conf.getTokenKey();
        }

        if (key == null) {
            key = DEFAULT_TOKEN_KEY;
        }

        return key;
    }

    public static int getMaxTokens() {
        int maxTokens = -1;
        Configuration conf = contextTokenConfigurationHolder.get();

        if (conf != null) {
            maxTokens = conf.getMaxTokens();
        }

        if (maxTokens <= 0) {
            maxTokens = DEFAULT_MAX_TOKENS;
        }

        return maxTokens;
    }

    public static void setContextTokenConfiguration(String tokenKey, int maxTokens) {
        contextTokenConfigurationHolder.set(new Configuration(tokenKey, maxTokens));
    }

    public static void resetContextTokenConfiguration() {
        contextTokenConfigurationHolder.remove();
    }

    /**
     * 创建包含csrf token的hidden field。 所生成的token会保持有效，直到session过期。
     */
    public Input getHiddenField() {
        return getLongLiveHiddenField();
    }

    /**
     * 创建包含csrf token的hidden field。
     * 
     * @param longLiveToken 如果为<code>true</code>，则token会保持有效，直到session过期。
     * @deprecated use getUniqueHiddenField() or getLongLiveHiddenField()
     *             instead
     */
    @Deprecated
    public Input getHiddenField(boolean longLiveToken) {
        return longLiveToken ? getLongLiveHiddenField() : getUniqueHiddenField();
    }

    public Input getUniqueHiddenField() {
        return new Input("hidden", getKey(), getUniqueToken());
    }

    public Input getLongLiveHiddenField() {
        return new Input("hidden", getKey(), getLongLiveToken());
    }

    /**
     * 创建csrf token，所生成的token只能被使用一次。
     */
    public String getUniqueToken() {
        HttpSession session = request.getSession();
        String key = getKey();
        String tokenOfRequest = (String) request.getAttribute(key);
        int maxTokens = getMaxTokens();

        if (tokenOfRequest == null) {
            // 创建新的token。
            // 如果当前session中已经有token了，
            // 并且token数没有超过最大数，则将token追加到session中；
            // 如果token超过最大数，则覆盖最早的token。
            LinkedList<String> tokens = getTokensInSession(session, key);

            tokenOfRequest = getGenerator().generateUniqueToken();
            request.setAttribute(key, tokenOfRequest);

            tokens.addLast(tokenOfRequest);

            while (tokens.size() > maxTokens) {
                tokens.removeFirst();
            }

            setTokensInSession(session, key, tokens);
        }

        return tokenOfRequest;
    }

    /**
     * 取得长效token。和<code>uniqueToken</code> 不同，长效token的寿命和session相同。
     */
    public String getLongLiveToken() {
        return getLongLiveTokenInSession(request.getSession());
    }

    public static LinkedList<String> getTokensInSession(HttpSession session, String tokenKey) {
        return createLinkedList(StringUtil.split((String) session.getAttribute(tokenKey), CSRF_TOKEN_SEPARATOR));
    }

    public static void setTokensInSession(HttpSession session, String tokenKey, List<String> tokens) {
        if (tokens.isEmpty()) {
            session.removeAttribute(tokenKey);
        } else {
            session.setAttribute(tokenKey, StringUtil.join(tokens, CSRF_TOKEN_SEPARATOR));
        }
    }

    public static String getLongLiveTokenInSession(HttpSession session) {
        return getGenerator().generateLongLiveToken(session);
    }

    @Override
    public String toString() {
        try {
            return getUniqueToken();
        } catch (IllegalStateException e) {
            return "<No thread-bound request>";
        }
    }

    /**
     * 检查token，如果token存在，则返回<code>true</code>。
     */
    public static boolean check(HttpServletRequest request) {
        String key = getKey();
        String fromRequest = trimToNull(request.getParameter(key));

        return fromRequest != null;
    }

    private static class Configuration {
        private final String tokenKey;
        private final int maxTokens;

        public Configuration(String tokenKey, int maxTokens) {
            this.tokenKey = trimToNull(tokenKey);
            this.maxTokens = maxTokens;
        }

        public String getTokenKey() {
            return tokenKey;
        }

        public int getMaxTokens() {
            return maxTokens;
        }
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<Factory> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "tokenKey");
        }
    }

    /**
     * pull tool factory。
     */
    public static class Factory implements ToolFactory {
        private HttpServletRequest request;

        @Autowired
        public void setRequest(HttpServletRequest request) {
            this.request = request;
        }

        public boolean isSingleton() {
            return true;
        }

        public Object createTool() throws Exception {
            return new CsrfToken(request);
        }
    }

    private static Logger log = LoggerFactory.getLogger(CsrfToken.class);
    private static final Generator generator = new DefaultGenerator();
    private static final Generator generatorOverride = getGeneratorOverride();

    private static Generator getGeneratorOverride() {
        try {
            return Generator.class.cast(ClassLoaderUtil.newServiceInstance("csrfTokenGeneratorOverride",
                    CsrfToken.class));
        } catch (Exception e) {
            log.warn("Failure in CsrfToken.getGeneratorOverride()", e);
        }

        return null;
    }

    private static Generator getGenerator() {
        return generatorOverride != null ? generatorOverride : generator;
    }

    /**
     * 允许其它模块override生成token的算法。
     */
    public interface Generator {
        String generateUniqueToken();

        String generateLongLiveToken(HttpSession session);
    }

    private static class DefaultGenerator implements Generator {
        private final long seed = System.currentTimeMillis();

        public String generateUniqueToken() {
            return longToString(counter.getAndIncrement()) + longToString(seed + System.currentTimeMillis());
        }

        public String generateLongLiveToken(HttpSession session) {
            String sessionId = assertNotNull(session, "session").getId();
            byte[] digest = DigestUtils.md5(session.getCreationTime() + seed + sessionId);

            return StringUtil.bytesToString(digest);
        }
    }
}
