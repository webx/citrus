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
import static org.junit.Assert.*;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Store;
import javax.mail.internet.AddressException;

import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import com.alibaba.citrus.service.mail.MailException;
import com.alibaba.citrus.service.mail.MailService;
import com.alibaba.citrus.service.mail.mock.MyMockStore;
import com.alibaba.citrus.service.mail.support.DefaultMailStoreHandler;

public class MailStoreTests extends AbstractMailSessionTests<MailStore> {
    @Test
    public void config_stores() {
        // normal store
        MailStore store = mailService.getMailStore("mystore");

        assertSame(mailService, store.getMailService());
        assertFalse(store.isDebug());
        assertFalse(store.isDefault()); // default flag is not copied
        assertEquals("myhost", store.getHost());
        assertEquals(110, store.getPort());
        assertEquals("myname", store.getUser());
        assertEquals("mypass", store.getPassword());
        assertEquals("pop3", store.getProtocol());
        assertEquals("INBOX2", store.getFolder());
        assertEquals(1, store.getSessionProperties().size());
        assertEquals("30000", store.getSessionProperties().get("mail.pop3.connectiontimeout"));

        // default store
        store = mailService.getMailStore();
        assertDefaultStore(store);

        store = mailService.getMailStore("mystore_default");
        assertDefaultStore(store);
    }

    private void assertDefaultStore(MailStore store) {
        assertSame(mailService, store.getMailService());
        assertTrue(store.isDebug());
        assertFalse(store.isDefault()); // default flag is not copied
        assertEquals("alibaba.com", store.getHost());
        assertEquals(-1, store.getPort());
        assertEquals("aliren", store.getUser());
        assertEquals(null, store.getPassword());
        assertEquals("pop3", store.getProtocol());
        assertEquals("INBOX", store.getFolder());
        assertEquals(1, store.getSessionProperties().size());
        assertEquals("true", store.getSessionProperties().get("mail.debug"));
    }

    @Test
    public void setHandler() throws Exception {
        // add handler1
        MyHandler handler1 = new MyHandler();
        session.setHandler(handler1);
        assertSame(handler1, session.getHandler());

        // add null - no effect
        session.setHandler(null);
        assertSame(handler1, session.getHandler());

        // re-add handler1
        session.setHandler(handler1);
        assertSame(handler1, session.getHandler());

        // add handler2
        MyHandler handler2 = new MyHandler();
        session.setHandler(handler2);
        assertSame(handler2, session.getHandler());
    }

    @Test
    public void connectAndClose() {
        session.setProtocol("unknown");

        // unknown protocol
        try {
            session.connect();
            fail();
        } catch (MailException e) {
            assertThat(e, exception(NoSuchProviderException.class, "Could not find a provider of unknown protocol"));
        }

        // connect error
        MyMockStore.setError(true);
        session.setProtocol("pop3");

        try {
            session.connect();
            fail();
        } catch (MailException e) {
            assertThat(e, exception(MessagingException.class, "Could not connect to the store"));
        } finally {
            MyMockStore.setError(false);
        }

        // close error: ignored
        session.connect();
        assertTrue(session.isConnected());
        MyMockStore.setError(true);

        try {
            session.close();
        } finally {
            MyMockStore.setError(false);
        }
    }

    @Test
    public void receive() throws Exception {
        addMail("hello");

        session.setDebug(true);
        session.setUser(ALIREN);
        session.setHost(ALIBABA_COM);

        // no handler
        assertFalse(session.isConnected());
        session.receive();
        assertFalse(session.isConnected());

        assertEquals(1, Mailbox.get(ALIREN_ALIBABA_COM).size());

        // with handler - delete all
        MyHandler handler = new MyHandler();

        assertFalse(session.isConnected());
        session.receive(handler);
        assertFalse(session.isConnected());

        assertEquals(0, Mailbox.get(ALIREN_ALIBABA_COM).size());
        assertEquals(1, handler.messageCount);
        assertEquals(1, handler.msgs.size());
        assertEquals("hello", handler.msgs.get(0).getSubject());

        // with handler - delete partially
        handler = new MyHandler(2, true);
        addMail("111");
        addMail("222");
        addMail("333");
        addMail("444");

        assertFalse(session.isConnected());
        session.receive(handler);
        assertFalse(session.isConnected());

        assertEquals(2, Mailbox.get(ALIREN_ALIBABA_COM).size());
        assertEquals(4, handler.messageCount);
        assertEquals(2, handler.msgs.size());
        assertEquals("111", handler.msgs.get(0).getSubject());
        assertEquals("222", handler.msgs.get(1).getSubject());

        // with handler - read all, do not delete
        handler = new MyHandler(-1, false);
        assertFalse(session.isConnected());
        session.receive(handler);
        assertFalse(session.isConnected());

        assertEquals(2, Mailbox.get(ALIREN_ALIBABA_COM).size());
        assertEquals(2, handler.messageCount);
        assertEquals(2, handler.msgs.size());
        assertEquals("333", handler.msgs.get(0).getSubject());
        assertEquals("444", handler.msgs.get(1).getSubject());

        // connection failure
        setError(ALIREN_ALIBABA_COM, true);
        assertFalse(session.isConnected());

        try {
            session.receive();
            fail();
        } catch (MailException e) {
            assertThat(e, exception(MessagingException.class, "Could not connect to the store"));
        } finally {
            setError(ALIREN_ALIBABA_COM, false);
        }

        assertFalse(session.isConnected());

        // receive failure
        assertFalse(session.isConnected());
        handler = new MyHandler(-1, false) {
            @Override
            public void prepareConnection(Store store) throws MailException, MessagingException {
                MyMockStore.setError(true);
            }
        };

        try {
            session.receive(handler);
            fail();
        } catch (MailException e) {
            assertThat(e, exception(MessagingException.class, "Could not receive messages"));
        } finally {
            MyMockStore.setError(false);
        }

        assertFalse(session.isConnected());
    }

    @Test
    public void receive_config() throws Exception {
        session = mailService.getMailStore();
        addMail("hello");

        MyHandler handler = new MyHandler();

        assertFalse(session.isConnected());
        session.receive(handler);
        assertFalse(session.isConnected());

        assertEquals(0, Mailbox.get(ALIREN_ALIBABA_COM).size());
        assertEquals(1, handler.messageCount);
        assertEquals(1, handler.msgs.size());
        assertEquals("hello", handler.msgs.get(0).getSubject());
    }

    @Test
    public void receive_batch_config() throws Exception {
        session = mailService.getMailStore();
        addMail("111");
        addMail("222");
        addMail("333");

        MyHandler handler = new MyHandler(1, true); // 每次收一封信

        try {
            assertFalse(session.isConnected());
            session.connect();
            assertTrue(session.isConnected());

            // 111
            session.receive(handler);
            assertTrue(session.isConnected());

            assertEquals(2, Mailbox.get(ALIREN_ALIBABA_COM).size());
            assertEquals(3, handler.messageCount);
            assertEquals(1, handler.msgs.size());
            assertEquals("111", handler.msgs.remove(0).getSubject());

            // 222
            session.receive(handler);
            assertTrue(session.isConnected());

            assertEquals(1, Mailbox.get(ALIREN_ALIBABA_COM).size());
            assertEquals(2, handler.messageCount);
            assertEquals(1, handler.msgs.size());
            assertEquals("222", handler.msgs.remove(0).getSubject());

            // 333
            session.receive(handler);
            assertTrue(session.isConnected());

            assertEquals(0, Mailbox.get(ALIREN_ALIBABA_COM).size());
            assertEquals(1, handler.messageCount);
            assertEquals(1, handler.msgs.size());
            assertEquals("333", handler.msgs.remove(0).getSubject());

            // no mail
            session.receive(handler);
            assertTrue(session.isConnected());

            assertEquals(0, Mailbox.get(ALIREN_ALIBABA_COM).size());
            assertEquals(0, handler.messageCount);
            assertEquals(0, handler.msgs.size());
        } finally {
            session.close();
            assertFalse(session.isConnected());
        }
    }

    private void addMail(String text) throws AddressException {
        Mailbox.get(ALIREN_ALIBABA_COM).add(createMessage(text, ALIREN_ALIBABA_COM));
    }

    @Override
    protected void assertCopy(MailStore copy, Properties propsCopy) {
        assertEquals("protocol", copy.getProtocol());
        assertEquals("folder", copy.getFolder());
    }

    @Override
    protected void assertToString(String str) {
        assertThat(str, containsRegex("MailStore \\{"));
        assertThat(str, containsRegex("folder\\s+= folder"));
        assertThat(str, containsRegex("protocol\\s+= protocol"));
    }

    @Override
    protected void prepareForSubclass(MailStore session) {
        session.setProtocol("protocol");
        session.setFolder("folder");
    }

    @Override
    protected MailStore createMailSession(MailService service) {
        MailStore store = new MailStore();
        store.setMailService(service);
        return store;
    }

    @Override
    protected MailStore copyMailSession(MailStore session, Properties overrideProps) {
        return new MailStore(session, overrideProps);
    }

    public static class MyHandler extends DefaultMailStoreHandler {
        private final List<Message> msgs = createArrayList();
        private int messageCount;
        private final int receiveMax;
        private final boolean delete;

        private MyHandler() {
            this(-1, true);
        }

        private MyHandler(int receiveMax, boolean delete) {
            this.receiveMax = receiveMax;
            this.delete = delete;
        }

        @Override
        public int getMessageCount(int messageCount) throws MailException {
            this.messageCount = messageCount;

            if (receiveMax == -1) {
                return messageCount;
            } else {
                return receiveMax;
            }
        }

        @Override
        public boolean processMessage(Message message) throws MailException, MessagingException {
            msgs.add(message);
            return delete;
        }
    }
}
