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
package com.alibaba.citrus.service.mail.session;

import static com.alibaba.citrus.service.mail.MailConstant.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;
import java.util.Properties;

import javax.mail.Session;

import com.alibaba.citrus.service.mail.MailException;
import com.alibaba.citrus.service.mail.MailService;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 这个类包装了<code>java.mail.Session</code>，以更友好的方式来支持mail transport和store。
 * <p>
 * 注意，<code>java.mail.Session</code>是在多次调用和多个线程中共享的，而<code>MailSession</code>
 * 被设计成每次调用都创建新的。
 * </p>
 * 
 * @author Michael Zhou
 */
public abstract class MailSession {
    private final Properties props = new Properties();
    private MailService mailService;
    private Session session;
    private String host;
    private int port;
    private String user;
    private String password;
    private boolean debug;
    private boolean defaultSession;

    /**
     * 创建一个mail session。
     */
    public MailSession() {
    }

    /**
     * 复制一个mail session。
     */
    public MailSession(MailSession session, Properties overrideProps) {
        this.mailService = session.mailService;
        this.session = session.getSession(); // 注意，此方法是synchronized。
        this.host = session.host;
        this.port = session.port;
        this.user = session.user;
        this.password = session.password;
        this.debug = session.debug;
        this.defaultSession = false; // default value not copied

        this.props.putAll(session.props);

        if (overrideProps != null) {
            for (Object element : overrideProps.keySet()) {
                String key = (String) element;
                String value = overrideProps.getProperty(key);

                // 注意，执行此方法时，如果key/value和原值不同，session将被清空。
                setProperty(key, value, null);
            }
        }
    }

    /**
     * 取得创建该session的mail service。
     */
    public MailService getMailService() {
        return mailService;
    }

    /**
     * 设置mail service。
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * 取得mail server的服务器名或IP地址。
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置mail server的服务器名或IP地址。
     */
    public void setHost(String host) {
        this.host = trimToNull(host);
    }

    /**
     * 取得mail server的服务器端口。
     */
    public int getPort() {
        return port > 0 ? port : -1;
    }

    /**
     * 设置mail server的服务器端口。
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 判断是否需要验证。
     */
    public boolean useAuth() {
        return user != null;
    }

    /**
     * 取得mail server的验证用户。
     */
    public String getUser() {
        return user;
    }

    /**
     * 设置mail server的验证用户。
     */
    public void setUser(String user) {
        this.user = trimToNull(user);
    }

    /**
     * 取得mail server的验证密码。
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置mail server的验证密码。
     */
    public void setPassword(String password) {
        this.password = trimToNull(password);
    }

    /**
     * 是否是debug模式。在此模式下，javamail会打印出具体的信息。
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 设置debug模式。在此模式下，javamail会打印出具体的信息。
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * 是否为默认的transport或store。
     * <p>
     * 在一个<code>MailService</code>中只能有一个默认的transport和一个默认的store。
     * </p>
     */
    public boolean isDefault() {
        return defaultSession;
    }

    /**
     * 设置为默认的transport或store。
     * <p>
     * 在一个<code>MailService</code>中只能有一个默认的transport和一个默认的store。
     * </p>
     */
    public void setDefault(boolean defaultSession) {
        this.defaultSession = defaultSession;
    }

    /**
     * 批量设置属性。
     */
    public void setProperties(Map<String, String> props) {
        if (props != null) {
            this.props.clear();

            for (Map.Entry<String, String> entry : props.entrySet()) {
                String key = assertNotNull(trimToNull(entry.getKey()), "propertyName");
                String value = trimToNull(entry.getValue());

                setProperty(key, value);
            }
        }
    }

    /**
     * 设置session的属性，如果值被改变了，则清除session。
     */
    public void setProperty(String key, String value) {
        setProperty(key, value, null);
    }

    /**
     * 设置session的属性，如果值被改变了，则清除session。
     */
    protected final void setProperty(String key, String value, String defaultValue) {
        String currentValue = props.getProperty(key, defaultValue);

        if (!isEquals(currentValue, value)) {
            props.setProperty(key, value);
            session = null;
        }
    }

    /**
     * 取得session properties。
     */
    protected Properties getSessionProperties() {
        setProperty(MAIL_DEBUG, String.valueOf(isDebug()), "false");
        return props;
    }

    /**
     * 取得javamail session。
     * <p>
     * 此方法是线程安全的，尤其是在复制session的时候。
     * </p>
     */
    protected synchronized Session getSession() {
        // 注意，在执行此方法时，session有可能被清空。
        Properties props = getSessionProperties();

        if (session == null) {
            session = Session.getInstance(props);
        }

        return session;
    }

    /**
     * 判断是否已经连接上。
     */
    protected abstract boolean isConnected();

    /**
     * 连接mail服务器。
     */
    protected abstract void connect() throws MailException;

    /**
     * 关闭mail服务器的连接。
     */
    protected abstract void close() throws MailException;

    @Override
    public final String toString() {
        MapBuilder mb = new MapBuilder().setSortKeys(true).setPrintCount(true);

        mb.append("host", getHost());
        mb.append("port", getPort());
        mb.append("user", getUser());
        mb.append("password", getPassword());
        mb.append("debug", isDebug());
        mb.append("default", isDefault());
        mb.append("otherProperties", props);

        toString(mb);

        return new ToStringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }

    protected abstract void toString(MapBuilder mb);
}
