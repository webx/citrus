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

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import com.alibaba.citrus.service.mail.AbstractMailBuilderTests;
import com.alibaba.citrus.service.mail.MailService;
import com.alibaba.citrus.service.mail.builder.MailAddressType;
import com.alibaba.citrus.service.mail.builder.MailBuilder;
import com.alibaba.citrus.service.mail.builder.content.TextContent;
import com.alibaba.citrus.service.mail.impl.MailServiceImpl;
import com.alibaba.citrus.service.mail.mock.MyMockStore;
import com.alibaba.citrus.service.mail.mock.MyMockTransport;

public abstract class AbstractMailSessionTests<T extends MailSession> extends AbstractMailBuilderTests {
    protected static final String ALIREN = "aliren";
    protected static final String ALIBABA_COM = "alibaba.com";
    protected static final String ALIREN_ALIBABA_COM = "aliren@alibaba.com";
    protected static final String MODIFIED_FROM_ALIBABA_COM = "modified_from@alibaba.com";
    protected T session;

    @BeforeClass
    public static void checkMockJavaMail() throws Exception {
        Session rawSession = Session.getInstance(System.getProperties());
        Transport rawTransport = rawSession.getTransport("smtp");
        Store rawStore = rawSession.getStore("pop3");

        assertThat(rawTransport, instanceOf(MyMockTransport.class));
        assertThat(rawStore, instanceOf(MyMockStore.class));
    }

    @Before
    public void initSuper() {
        mailService = (MailServiceImpl) factory.getBean("mailService");
        assertSame(mailService, factory.getBean("mails"));
        assertNotNull(mailService);

        createMailSession();

        // clear mock mailbox
        Mailbox.clearAll();
    }

    @Test
    public void setHost() {
        session.setHost(null);
        assertEquals(null, session.getHost());

        session.setHost("");
        assertEquals(null, session.getHost());

        session.setHost(" ");
        assertEquals(null, session.getHost());

        session.setHost(" localhost ");
        assertEquals("localhost", session.getHost());
    }

    @Test
    public void setPort() {
        session.setPort(0);
        assertEquals(-1, session.getPort());

        session.setPort(-99);
        assertEquals(-1, session.getPort());

        session.setPort(99);
        assertEquals(99, session.getPort());
    }

    @Test
    public void setUser() {
        session.setUser(null);
        assertEquals(null, session.getUser());
        assertFalse(session.useAuth());

        session.setUser("");
        assertEquals(null, session.getUser());
        assertFalse(session.useAuth());

        session.setUser("  ");
        assertEquals(null, session.getUser());
        assertFalse(session.useAuth());

        session.setUser("  user ");
        assertEquals("user", session.getUser());
        assertTrue(session.useAuth());
    }

    @Test
    public void setPassword() {
        session.setPassword(null);
        assertEquals(null, session.getPassword());

        session.setPassword("");
        assertEquals(null, session.getPassword());

        session.setPassword("  ");
        assertEquals(null, session.getPassword());

        session.setPassword(" pass ");
        assertEquals("pass", session.getPassword());
    }

    @Test
    public void setDebug() {
        session.setDebug(true);
        assertEquals(true, session.isDebug());
        assertEquals("true", session.getSessionProperties().getProperty("mail.debug"));

        session.setDebug(false);
        assertEquals(false, session.isDebug());
        assertEquals("false", session.getSessionProperties().getProperty("mail.debug"));
    }

    @Test
    public void setDefault() {
        session.setDefault(true);
        assertTrue(session.isDefault());

        session.setDefault(false);
        assertFalse(session.isDefault());
    }

    @Test
    public void setProperty() {
        Properties props = session.getSessionProperties();
        assertTrue(props.isEmpty());

        // 取得session
        Session rawSession = session.getSession();
        assertNotNull(rawSession);

        // 第二次取得session：同一个session
        assertSame(rawSession, session.getSession());

        // 添加property，从而改变session
        session.setProperty("aaa", "111");
        assertEquals(1, props.size());
        assertEquals("111", props.getProperty("aaa"));
        assertNotSame(rawSession, session.getSession());

        rawSession = session.getSession();

        // 设置property，但值相同，session不变
        session.setProperty("aaa", "111");
        assertEquals(1, props.size());
        assertEquals("111", props.getProperty("aaa"));
        assertSame(rawSession, session.getSession());

        // 改变property，session改变
        session.setProperty("aaa", "222");
        assertEquals(1, props.size());
        assertEquals("222", props.getProperty("aaa"));
        assertNotSame(rawSession, session.getSession());

        rawSession = session.getSession();

        // 设置值，和默认值相同，因此不设置
        session.setProperty("bbb", "false", "false");
        assertEquals(1, props.size());
        assertEquals("222", props.getProperty("aaa"));
        assertSame(rawSession, session.getSession());

        // 修改当前值，仅管和默认值相同，但也设置
        session.setProperty("aaa", "333", "333");
        assertEquals(1, props.size());
        assertEquals("333", props.getProperty("aaa"));
        assertNotSame(rawSession, session.getSession());

        rawSession = session.getSession();

        // 批量设置 - null，无影响
        session.setProperties(null);
        assertEquals(1, props.size());
        assertEquals("333", props.getProperty("aaa"));
        assertSame(rawSession, session.getSession());

        // 批量设置 - 覆盖原值
        Map<String, String> values = createHashMap();
        values.put("ccc", "  333  ");
        values.put("ddd", "444");

        session.setProperties(values);
        assertEquals(2, props.size());
        assertEquals("333", props.getProperty("ccc"));
        assertEquals("444", props.getProperty("ddd"));
        assertNotSame(rawSession, session.getSession());

        // 批量设置 - 非法值
        values.put(null, "555");

        try {
            session.setProperties(values);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("propertyName"));
        }
    }

    @Test
    public void copy() {
        prepareSession(session);

        // no override props
        Properties overrProperties = null;
        T copy = copyMailSession(session, overrProperties);
        Properties propsCopy = copy.getSessionProperties();

        assertSame(mailService, copy.getMailService());
        assertSame(session.getSession(), copy.getSession());
        assertEquals("host", copy.getHost());
        assertEquals(123, copy.getPort());
        assertEquals("user", copy.getUser());
        assertEquals("pass", copy.getPassword());
        assertEquals(true, copy.isDebug());
        assertEquals(false, copy.isDefault()); // default value is not copied
        assertEquals("111", propsCopy.remove("aaa"));
        assertEquals("222", propsCopy.remove("bbb"));
        assertEquals("true", propsCopy.remove("mail.debug"));
        assertCopy(copy, propsCopy);
        assertTrue(propsCopy.isEmpty());

        // with override props
        overrProperties = new Properties();
        overrProperties.setProperty("bbb", "2222");
        overrProperties.setProperty("ccc", "3333");

        copy = copyMailSession(session, overrProperties);
        propsCopy = copy.getSessionProperties();

        assertSame(mailService, copy.getMailService());
        assertNotSame(session.getSession(), copy.getSession());
        assertEquals("host", copy.getHost());
        assertEquals(123, copy.getPort());
        assertEquals("user", copy.getUser());
        assertEquals("pass", copy.getPassword());
        assertEquals(true, copy.isDebug());
        assertEquals(false, copy.isDefault()); // default value is not copied
        assertEquals("111", propsCopy.remove("aaa"));
        assertEquals("2222", propsCopy.remove("bbb"));
        assertEquals("3333", propsCopy.remove("ccc"));
        assertEquals("true", propsCopy.remove("mail.debug"));
        assertCopy(copy, propsCopy);
        assertTrue(propsCopy.isEmpty());
    }

    @Test
    public void toString_() {
        prepareSession(session);

        String str = session.toString();

        assertToString(str);

        assertThat(str, containsRegex("debug\\s+= true"));
        assertThat(str, containsRegex("default\\s+= true"));
        assertThat(str, containsRegex("host\\s+= host"));
        assertThat(str, containsRegex("otherProperties\\s+= \\{"));
        assertThat(str, containsRegex("password\\s+= pass"));
        assertThat(str, containsRegex("port\\s+= 123"));
        assertThat(str, containsRegex("user\\s+= user"));

        assertThat(str, containsRegex("aaa\\s+= 111"));
        assertThat(str, containsRegex("bbb\\s+= 222"));
    }

    protected abstract void assertToString(String str);

    protected final void prepareSession(T session) {
        session.setHost("host");
        session.setPort(123);
        session.setUser("user");
        session.setPassword("pass");
        session.setDebug(true);
        session.setDefault(true);
        session.setProperty("aaa", "111");
        session.setProperty("bbb", "222");
        prepareForSubclass(session);
    }

    protected abstract void prepareForSubclass(T session);

    protected abstract void assertCopy(T copy, Properties propsCopy);

    protected final void createMailSession() {
        session = createMailSession(mailService);
        assertSame(mailService, session.getMailService());
    }

    protected abstract T createMailSession(MailService service);

    protected abstract T copyMailSession(T session, Properties overrideProps);

    protected Message createMessage(String subject, String addr) {
        MailBuilder builder = createMailBuilder(subject, addr);
        builder.setSentDate(new Date(0));
        return builder.getMessage(rawSession);
    }

    protected MailBuilder createMailBuilder(String subject, String addr) {
        MailBuilder builder = new MailBuilder();

        builder.setSubject(subject);
        builder.setContent(new TextContent());
        builder.setAddress(MailAddressType.TO, addr);

        return builder;
    }

    protected void setError(String targetAddress, boolean err) throws Exception {
        Mailbox inbox = Mailbox.get(targetAddress);
        inbox.setError(err);
    }
}
