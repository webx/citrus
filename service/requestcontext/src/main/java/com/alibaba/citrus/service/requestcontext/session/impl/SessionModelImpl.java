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

import static com.alibaba.citrus.util.Assert.*;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.alibaba.citrus.service.requestcontext.session.SessionConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionModel;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 代表一个session本身的信息。该对象是可序列化的。
 * 
 * @author Michael Zhou
 */
public class SessionModelImpl implements SessionModel {
    private static final long serialVersionUID = 9158363263146288193L;
    private transient SessionConfig sessionConfig;
    private String sessionID;
    private long creationTime;
    private long lastAccessedTime;
    private int maxInactiveInterval;

    public SessionModelImpl(SessionImpl session) {
        setSession(session);
        reset();
    }

    public SessionModelImpl(String sessionID, long creationTime, long lastAccessedTime, int maxInactiveInterval) {
        this.sessionID = sessionID;
        this.creationTime = creationTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveInterval = maxInactiveInterval;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            unexpectedException(e);
            return null;
        }
    }

    private SessionConfig getSessionConfig() {
        return assertNotNull(sessionConfig, "sessionConfig");
    }

    public void reset() {
        getSessionConfig();
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = creationTime;
        this.maxInactiveInterval = sessionConfig.getMaxInactiveInterval();
    }

    /**
     * 设置model所在的session。
     */
    public void setSession(SessionImpl session) {
        this.sessionConfig = session.getSessionRequestContext().getSessionConfig();
        this.sessionID = session.getId();
    }

    /**
     * 取得session ID。
     * 
     * @return session ID
     */
    public String getSessionID() {
        return sessionID;
    }

    /**
     * 取得session的创建时间。
     * 
     * @return 创建时间戮
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * 取得最近访问时间。
     * 
     * @return 最近访问时间戮
     */
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    /**
     * 取得session的最大不活动期限，超过此时间，session就会失效。
     * 
     * @return 不活动期限的秒数
     */
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    /**
     * 设置session的最大不活动期限，超过此时间，session就会失效。
     * 
     * @param maxInactiveInterval 不活动期限的秒数
     */
    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    /**
     * 判断session有没有过期。
     * 
     * @return 如果过期了，则返回<code>true</code>
     */
    public boolean isExpired() {
        int maxInactiveInterval = getMaxInactiveInterval();
        long forceExpirationPeriod = getSessionConfig().getForceExpirationPeriod();
        long current = System.currentTimeMillis();

        // 如果从创建之时算起，已经超过了forceExpirationPeriod，则强制作废。
        if (forceExpirationPeriod > 0) {
            long expires = getCreationTime() + forceExpirationPeriod * 1000;

            if (expires < current) {
                return true;
            }
        }

        // 如果从上次访问时间算起，已经超过maxInactiveInterval没动静了，则作废。
        if (maxInactiveInterval > 0) {
            long expires = getLastAccessedTime() + maxInactiveInterval * 1000;

            if (expires < current) {
                return true;
            }
        }

        return false;
    }

    /**
     * 更新session的访问时间。
     */
    public void touch() {
        lastAccessedTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();
        DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.US);

        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        mb.append("sessionID", sessionID);
        mb.append("creationTime", creationTime <= 0 ? "n/a" : fmt.format(new Date(creationTime)));
        mb.append("lastAccessedTime", lastAccessedTime <= 0 ? "n/a" : fmt.format(new Date(lastAccessedTime)));
        mb.append("maxInactiveInterval", maxInactiveInterval);

        return new ToStringBuilder().append("SessionModel").append(mb).toString();
    }
}
