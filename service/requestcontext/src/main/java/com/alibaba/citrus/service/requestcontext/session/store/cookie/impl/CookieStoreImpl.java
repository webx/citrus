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
package com.alibaba.citrus.service.requestcontext.session.store.cookie.impl;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.requestcontext.session.encoder.SessionEncoder;
import com.alibaba.citrus.service.requestcontext.session.encoder.impl.SerializationEncoder;
import com.alibaba.citrus.service.requestcontext.session.store.SessionStoreException;
import com.alibaba.citrus.service.requestcontext.session.store.cookie.AbstractCookieStore;
import com.alibaba.citrus.util.ObjectUtil;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 将Session状态保存在cookie中。
 * <ul>
 * <li>将session数据用<code>SessionEncoder</code>编码成字符串。</li>
 * <li>将字符串数据分段保存在cookie中：<code>cookieName0</code>，<code>cookieName1</code>……
 * <code>cookieNameN</code>。</li>
 * <li>可选生成checksum cookie：<code>cookieNamesum</code>。
 * </ul>
 * 
 * @author Michael Zhou
 */
public class CookieStoreImpl extends AbstractCookieStore {
    private static final String NAME_PATTERN_SUFFIX = "(\\d+)";
    private static final Integer MAX_LENGTH_DEFAULT = (int) (4096 - 200);
    private static final Integer MAX_COUNT_DEFAULT = 5;
    private static final Boolean CHECKSUM_DEFAULT = false;
    private static final String CHECKSUM_SEPARATOR = "|";
    private static final int CHECKSUM_LENGTH = 15;
    private Pattern namePattern;
    private Integer maxLength;
    private Integer maxCount;
    private Boolean checksum;
    private String checksumName;
    private SessionEncoder[] encoders;

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public void setChecksum(boolean checksum) {
        this.checksum = checksum;
    }

    public void setEncoders(SessionEncoder[] encoders) {
        this.encoders = encoders;
    }

    @Override
    protected void init() {
        // 根据cookie名称，取得cookie名称的正则表达式
        namePattern = Pattern.compile(getName() + NAME_PATTERN_SUFFIX);

        // 取得cookie长度和个数的限制
        maxLength = defaultIfNull(maxLength, MAX_LENGTH_DEFAULT);
        maxCount = defaultIfNull(maxCount, MAX_COUNT_DEFAULT);

        // 取得cookie checksum的设置
        checksum = defaultIfNull(checksum, CHECKSUM_DEFAULT);
        checksumName = getName() + "sum";

        // 取得cookie encoder
        if (isEmptyArray(encoders)) {
            encoders = new SessionEncoder[] { createDefaultSessionEncoder() };
        }
    }

    protected SessionEncoder createDefaultSessionEncoder() {
        SerializationEncoder encoder = new SerializationEncoder();

        try {
            encoder.afterPropertiesSet();
        } catch (Exception e) {
            throw new SessionStoreException("Could not create default session encoder", e);
        }

        return encoder;
    }

    public Iterable<String> getAttributeNames(String sessionID, StoreContext storeContext) {
        State state = getState(storeContext);
        return state.attributes.keySet();
    }

    public Object loadAttribute(String attrName, String sessionID, StoreContext storeContext) {
        State state = getState(storeContext);
        return state.attributes.get(attrName);
    }

    public void invaldiate(String sessionID, StoreContext storeContext) {
        State state = getState(storeContext);

        if (!isSurvivesInInvalidating()) {
            state.attributes.clear();
        }
    }

    public void commit(Map<String, Object> attrs, String sessionID, StoreContext storeContext) {
        State state = getState(storeContext);

        if (state.cookieCommitted) {
            return;
        }

        state.cookieCommitted = true;

        for (Map.Entry<String, Object> entry : attrs.entrySet()) {
            String attrName = entry.getKey();
            Object attrValue = entry.getValue();

            if (attrValue == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Remove from session: {}", attrName);
                }

                state.attributes.remove(attrName);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Set to session: {} = {}", attrName, attrValue);
                }

                state.attributes.put(attrName, attrValue);
            }
        }

        if (log.isDebugEnabled()) {
            for (String attrName : state.attributes.keySet()) {
                if (!attrs.containsKey(attrName)) {
                    Object attrValue = state.attributes.get(attrName);
                    log.debug("Left unchanged attribute: {} = {}", attrName, attrValue);
                }
            }
        }

        String cookieState = null;

        if (!state.attributes.isEmpty()) {
            try {
                cookieState = encoders[0].encode(state.attributes, storeContext);
            } catch (Exception e) {
                log.warn("Failed to encode session state", e);
            }
        }

        cookieState = trimToEmpty(cookieState);

        if (cookieState.length() > maxLength * maxCount) {
            log.warn("Cookie store full! length {} exceeds the max length: {} * {}.\n"
                    + " All session attributes will be LOST!",
                    new Object[] { cookieState.length(), maxLength, maxCount });
        } else {
            StringBuilder checksumBuf = null;

            if (checksum) {
                checksumBuf = new StringBuilder();
            }

            for (int beginOffset = 0, i = 0; beginOffset < cookieState.length(); beginOffset += maxLength, i++) {
                int endOffset = Math.min(beginOffset + maxLength, cookieState.length());
                String cookieNameWithIndex = getName() + i;
                String cookieValue = cookieState.substring(beginOffset, endOffset);

                writeCookie(storeContext.getSessionRequestContext().getResponse(), cookieNameWithIndex, cookieValue);
                state.requestCookies.remove(cookieNameWithIndex);

                // 创建cookie checksum
                if (checksumBuf != null) {
                    if (checksumBuf.length() > 0) {
                        checksumBuf.append(CHECKSUM_SEPARATOR);
                    }

                    checksumBuf.append(StringUtil.substring(cookieValue, 0, CHECKSUM_LENGTH));
                }
            }

            for (String cookieName : state.requestCookies.keySet()) {
                writeCookie(storeContext.getSessionRequestContext().getResponse(), cookieName, null);
            }

            // 如果request中包括cookie checksum，并且此次cookie为空，则清除checksum
            // 否则如果cookie checksum被打开，则生成checksum并写入cookie
            if (checksumBuf == null || checksumBuf.length() <= 0) {
                if (state.hasChecksum) {
                    writeCookie(storeContext.getSessionRequestContext().getResponse(), checksumName, null);
                }
            } else {
                writeCookie(storeContext.getSessionRequestContext().getResponse(), checksumName, checksumBuf.toString());
            }
        }
    }

    /**
     * 取得cookie store的状态。
     */
    private State getState(StoreContext storeContext) {
        State state = (State) storeContext.getState();

        if (state == null) {
            state = new State();
            storeContext.setState(state);
        }

        ensureCookieLoading(state, storeContext.getSessionRequestContext().getRequest(), storeContext);
        return state;
    }

    /**
     * 确保cookie被装载。
     */
    private void ensureCookieLoading(State state, HttpServletRequest request, StoreContext storeContext) {
        if (state.cookieLoaded) {
            return;
        }

        state.cookieLoaded = true;

        // 读取cookies
        CookiesInfo cookiesInfo = readCookies(request);

        state.requestCookies = cookiesInfo.requestCookies;
        state.hasChecksum = cookiesInfo.hasChecksum;

        // 验证cookies
        state.checksumValid = validateCookies(cookiesInfo.cookieList, cookiesInfo.checksumList);

        // 合并cookies、trimToNull
        state.mergedCookieValue = mergeCookies(cookiesInfo.cookieList, cookiesInfo.checksumList);

        // 依次使用所有encoders，试着对cookieState解码，如果失败，则返回空表
        state.attributes = decodeCookieValue(state.mergedCookieValue, storeContext);
    }

    /**
     * 读取cookies。
     */
    private CookiesInfo readCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            cookies = new Cookie[0];
        }

        CookiesInfo cookiesInfo = new CookiesInfo();

        cookiesInfo.requestCookies = createHashMap();
        cookiesInfo.cookieList = createArrayList(cookies.length);
        cookiesInfo.hasChecksum = false;
        cookiesInfo.checksumList = null;

        // 扫描cookie。
        for (Cookie cookie : cookies) {
            String cookieName = cookie.getName();

            if (isEquals(checksumName, cookieName)) {
                cookiesInfo.hasChecksum = true;
                cookiesInfo.checksumList = StringUtil.split(cookie.getValue(), CHECKSUM_SEPARATOR + " ");
            } else {
                Matcher matcher = namePattern.matcher(cookieName);

                if (matcher.matches()) {
                    int index = Integer.parseInt(matcher.group(1));
                    String cookieValue = trimToNull(cookie.getValue());
                    CookieInfo cookieInfo = new CookieInfo(index, cookieName, cookieValue);

                    cookiesInfo.cookieList.add(cookieInfo);
                    cookiesInfo.requestCookies.put(cookieName, cookieInfo);
                }
            }
        }

        // 排序cookie。
        Collections.sort(cookiesInfo.cookieList);

        if (log.isDebugEnabled()) {
            ToStringBuilder buf = new ToStringBuilder();

            buf.format("[%s] Loading cookies: %d cookies found", getStoreName(), cookiesInfo.cookieList.size());

            if (isEmptyArray(cookiesInfo.checksumList)) {
                if (checksum) {
                    buf.append("\n No checksum cookie");
                }
            } else {
                buf.format("\n %s[expected cookies=%d]=%s", checksumName, cookiesInfo.checksumList.length,
                        ObjectUtil.toString(cookiesInfo.checksumList));
            }

            for (CookieInfo cookieInfo : cookiesInfo.cookieList) {
                int length = 0;

                if (cookieInfo.value != null) {
                    length = cookieInfo.value.length();
                }

                buf.format("\n %s[length=%d]=%s", cookieInfo.name, length, cookieInfo.value);
            }

            log.debug(buf.toString());
        }

        return cookiesInfo;
    }

    /**
     * 检查cookies。
     */
    private boolean validateCookies(List<CookieInfo> cookieList, String[] checksumList) {
        int checksumListSize = 0;
        int index = 0;

        if (checksumList != null) {
            checksumListSize = checksumList.length;
        }

        for (CookieInfo cookieInfo : cookieList) {
            if (cookieInfo.index != index || cookieInfo.value == null) {
                break; // cookie中的序号被中断，则退出。但为了容错，仍然看作合法的cookies，只检验前面连续的部分。
            }

            if (index < checksumListSize) {
                if (!cookieInfo.value.startsWith(checksumList[index])) {
                    log.warn("{} does not match the checksum.  "
                            + "Expected prefix: {}[length={}], actually: {}[length={}]", new Object[] {
                            cookieInfo.name, checksumList[index], checksumList[index].length(), cookieInfo.value,
                            cookieInfo.value.length() });

                    return false;
                }
            }

            index++;
        }

        if (checksumList != null && index != checksumListSize) {
            log.warn("Number of cookies {}* does not match checksum.  Expected cookies: {}" + ", actually: {}",
                    new Object[] { getName(), checksumListSize, index });

            return false;
        }

        return true;
    }

    /**
     * 检查和合并cookies。
     */
    private String mergeCookies(List<CookieInfo> cookieList, String[] checksumList) {
        StringBuilder buf = new StringBuilder();
        int index = 0;

        for (CookieInfo cookieInfo : cookieList) {
            if (cookieInfo.index != index || cookieInfo.value == null) {
                break; // cookie中的序号被中断，则退出。但为了容错，仍然看作合法的cookies，只检验前面连续的部分。
            }

            buf.append(cookieInfo.value);
            index++;
        }

        return trimToNull(buf.toString());
    }

    private Map<String, Object> decodeCookieValue(String cookieValue, StoreContext storeContext) {
        Map<String, Object> attrs = null;

        if (cookieValue == null) {
            return createHashMap();
        }

        List<Exception> encoderExceptions = null;

        for (SessionEncoder encoder : encoders) {
            try {
                attrs = encoder.decode(cookieValue, storeContext);
                log.debug("Succeeded decoding cookieValues using {}", encoder);
                break;
            } catch (Exception e) {
                if (encoderExceptions == null) {
                    encoderExceptions = createLinkedList();
                }

                encoderExceptions.add(e);
                log.trace("Failure decoding cookieValues using {}: {}", encoder, e.getMessage());
            }
        }

        if (attrs == null) {
            attrs = createHashMap();
        }

        // 如果失败，记录日志
        if (attrs.isEmpty() && encoderExceptions != null) {
            if (log.isWarnEnabled()) {
                ToStringBuilder buf = new ToStringBuilder();

                buf.append("Failed to decode cookie value: ").append(cookieValue);

                int i = 0;
                for (Exception e : encoderExceptions) {
                    buf.format("\n  Encoder #%d - %s threw %s", (i + 1), encoders[i].getClass().getSimpleName(), e);
                }

                log.warn(buf.toString());
            }
        } else {
            if (log.isDebugEnabled()) {
                int attrCount = attrs.size();
                ToStringBuilder buf = new ToStringBuilder();

                buf.format("Found %d attributes:", attrCount);

                if (!attrs.isEmpty()) {
                    buf.append(new MapBuilder().setPrintCount(true).setSortKeys(true).appendAll(attrs));
                }

                log.debug(buf.toString());
            }
        }

        return attrs;
    }

    @Override
    protected void toString(MapBuilder mb) {
        super.toString(mb);

        mb.append("maxLength", maxLength);
        mb.append("maxCount", maxCount);
        mb.append("checksum", checksum);
        mb.append("encoders", encoders);
    }

    /**
     * 用来保存所有cookies的信息。
     */
    private static class CookiesInfo {
        private Map<String, CookieInfo> requestCookies;
        private List<CookieInfo> cookieList;
        private String[] checksumList;
        private boolean hasChecksum;
    }

    /**
     * 存放cookie的状态。
     */
    private class State {
        private boolean cookieLoaded;
        private boolean cookieCommitted;
        private boolean hasChecksum;
        private Map<String, CookieInfo> requestCookies;
        private String mergedCookieValue;
        private Map<String, Object> attributes;

        @SuppressWarnings("unused")
        private boolean checksumValid; // used by testcase
    }

    /**
     * 保存一个cookie的信息。
     */
    private static class CookieInfo implements Comparable<CookieInfo> {
        public final int index;
        public final String name;
        public final String value;

        public CookieInfo(int index, String name, String value) {
            this.index = index;
            this.name = name;
            this.value = value;
        }

        public int compareTo(CookieInfo o) {
            return index - o.index;
        }
    }
}
