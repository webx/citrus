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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.internet.InternetAddress;

import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import com.alibaba.citrus.service.mail.MailException;
import com.alibaba.citrus.service.mail.MailNotFoundException;
import com.alibaba.citrus.service.mail.MailService;
import com.alibaba.citrus.service.mail.MailStoreNotFoundException;
import com.alibaba.citrus.service.mail.builder.MailBuilder;
import com.alibaba.citrus.service.mail.mock.MyMockStore;
import com.alibaba.citrus.service.mail.mock.MyMockTransport;
import com.alibaba.citrus.service.mail.support.DefaultMailTransportHandler;

public class MailTransportTests extends AbstractMailSessionTests<MailTransport> {
    @Test
    public void config_transports() {
        // normal transport
        MailTransport transport = mailService.getMailTransport("mytransport");

        assertSame(mailService, transport.getMailService());
        assertFalse(transport.isDebug());
        assertFalse(transport.isDefault()); // default flag is not copied
        assertEquals("myhost", transport.getHost());
        assertEquals(25, transport.getPort());
        assertEquals("myname", transport.getUser());
        assertEquals("mypass", transport.getPassword());
        assertEquals("smtp", transport.getProtocol());
        assertEquals(null, transport.getPopBeforeSmtp());
        assertEquals(2, transport.getSessionProperties().size());
        assertEquals("30000", transport.getSessionProperties().get("mail.smtp.connectiontimeout"));
        assertEquals("true", transport.getSessionProperties().get("mail.smtp.auth"));

        // default transport
        transport = mailService.getMailTransport();
        assertDefaultTransport(transport);

        transport = mailService.getMailTransport("mytransport_default");
        assertDefaultTransport(transport);
    }

    private void assertDefaultTransport(MailTransport transport) {
        assertSame(mailService, transport.getMailService());
        assertTrue(transport.isDebug());
        assertFalse(transport.isDefault()); // default flag is not copied
        assertEquals("alibaba.com", transport.getHost());
        assertEquals(-1, transport.getPort());
        assertEquals(null, transport.getUser());
        assertEquals(null, transport.getPassword());
        assertEquals("smtp", transport.getProtocol());
        assertEquals("mystore", transport.getPopBeforeSmtp());
        assertEquals(1, transport.getSessionProperties().size());
        assertEquals("true", transport.getSessionProperties().get("mail.debug"));
    }

    @Test
    public void setHandler() throws Exception {
        session.connect();
        Transport rawTransport = getFieldValue(session, "transport", Transport.class);
        assertNotNull(rawTransport);

        // add handler1
        MyHandler handler1 = new MyHandler(null);
        session.setHandler(handler1);
        assertSame(handler1, session.getHandler());

        List<?> transportListeners = getFieldValue(rawTransport, "transportListeners", List.class);
        assertArrayEquals(new Object[] { handler1 }, transportListeners.toArray());

        // add null - 设置transportListeners
        transportListeners.clear();
        session.setHandler(null);
        assertSame(handler1, session.getHandler());
        assertArrayEquals(new Object[] { handler1 }, transportListeners.toArray());

        // re-add handler1
        session.setHandler(handler1);
        assertSame(handler1, session.getHandler());
        assertArrayEquals(new Object[] { handler1 }, transportListeners.toArray());

        // add handler2
        MyHandler handler2 = new MyHandler(null);
        session.setHandler(handler2);
        assertSame(handler2, session.getHandler());
        assertArrayEquals(new Object[] { handler2 }, transportListeners.toArray());
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
        MyMockTransport.setError(true);
        session.setProtocol("smtp");

        try {
            session.connect();
            fail();
        } catch (MailException e) {
            assertThat(e, exception(MessagingException.class, "Could not connect to the transport"));
        } finally {
            MyMockTransport.setError(false);
        }

        // close error: ignored
        session.connect();
        assertTrue(session.isConnected());
        MyMockTransport.setError(true);

        try {
            session.close();
        } finally {
            MyMockTransport.setError(false);
        }
    }

    @Test
    public void send_builder_no_receipients() {
        try {
            session.send(createMailBuilder("", null));
            fail();
        } catch (MailException e) {
            assertThat(e, exception("No recipient was specified in mail"));
        }
    }

    @Test
    public void send_single_builder() throws Exception {
        MailBuilder builder = createMailBuilder("", ALIREN_ALIBABA_COM);
        builder.setSentDate(new Date(0L)); // 1970s

        session.setDebug(true);

        assertFalse(session.isConnected());
        session.send(builder);
        assertFalse(session.isConnected());

        Message msg = receiveMail(ALIREN_ALIBABA_COM);
        assertEquals(ALIREN_ALIBABA_COM, msg.getAllRecipients()[0].toString());

        // test sent date
        assertTrue(Math.abs(msg.getSentDate().getTime() - System.currentTimeMillis()) < 1000);

        // test failure
        setError(ALIREN_ALIBABA_COM, true);
        assertFalse(session.isConnected());

        try {
            session.send(builder);
            fail();
        } catch (MailException e) {
            assertThat(e, exception(MessagingException.class, "Could not send message"));
        }

        assertFalse(session.isConnected());
    }

    @Test
    public void send_single_builder_handler() throws Exception {
        MailBuilder builder = createMailBuilder("", ALIREN_ALIBABA_COM);
        builder.setSentDate(new Date(0L)); // 1970s

        session.setDebug(true);
        assertFalse(session.isConnected());

        MyHandler handler = new MyHandler(builder);

        session.send(builder, handler);

        assertFalse(session.isConnected());
        assertEquals("processMessage", handler.state);

        Message msg = receiveMail(ALIREN_ALIBABA_COM);
        assertEquals(ALIREN_ALIBABA_COM, msg.getAllRecipients()[0].toString());

        // test modified header
        assertEquals(new InternetAddress(MODIFIED_FROM_ALIBABA_COM), msg.getFrom()[0]);

        // test sent date
        assertThat(Math.abs(msg.getSentDate().getTime() - System.currentTimeMillis()), lessThan(1000L));

        // test failure
        setError(ALIREN_ALIBABA_COM, true);
        handler = new MyHandler(builder);

        try {
            session.send(builder, handler);
            fail();
        } catch (MailException e) {
            assertThat(e, exception(MessagingException.class, "Could not send message"));
        }
    }

    @Test
    public void send_single_builder_id_config() throws Exception {
        session = mailService.getMailTransport();

        assertFalse(session.isConnected());
        session.send("simple_cn");
        assertFalse(session.isConnected());

        assertEquals("我的标题 My Subject", receiveMail("to@alibaba.com").getSubject());
        assertEquals("我的标题 My Subject", receiveMail("to2@alibaba.com").getSubject());
        assertEquals("我的标题 My Subject", receiveMail("cc@alibaba.com").getSubject());
        assertEquals("我的标题 My Subject", receiveMail("cc2@alibaba.com").getSubject());
        assertEquals("我的标题 My Subject", receiveMail("bcc@alibaba.com").getSubject());

        // mail not found
        try {
            session.send("not_exist");
            fail();
        } catch (MailNotFoundException e) {
            assertThat(e, exception("Could not find mail builder: not_exist"));
        }

        // mailService is null
        session.setMailService(null);

        try {
            session.send("simple_cn");
            fail();
        } catch (MailNotFoundException e) {
            assertThat(e, exception("Could not find mail \"simple_cn\": mail service is not set"));
        }
    }

    @Test
    public void send_single_builder_id_handler_config() throws Exception {
        session = mailService.getMailTransport();
        assertFalse(session.isConnected());

        MyHandler handler = new MyHandler(null);

        session.send("simple_cn", handler);

        assertFalse(session.isConnected());
        assertEquals("processMessage", handler.state);

        assertEquals("我的标题 My Subject", receiveMail("to@alibaba.com").getSubject());
        assertEquals("我的标题 My Subject", receiveMail("to2@alibaba.com").getSubject());
        assertEquals("我的标题 My Subject", receiveMail("cc@alibaba.com").getSubject());
        assertEquals("我的标题 My Subject", receiveMail("cc2@alibaba.com").getSubject());
        assertEquals("我的标题 My Subject", receiveMail("bcc@alibaba.com").getSubject());

        Message msg = receiveMail("to@alibaba.com");

        // test modified header
        assertEquals(new InternetAddress(MODIFIED_FROM_ALIBABA_COM), msg.getFrom()[0]);

        // test sent date
        assertTrue(Math.abs(msg.getSentDate().getTime() - System.currentTimeMillis()) < 1000);
    }

    @Test
    public void send_raw_message() throws Exception {
        Message msg = createMessage("", ALIREN_ALIBABA_COM);
        assertEquals(new Date(0), msg.getSentDate());

        session.setDebug(true);

        assertFalse(session.isConnected());
        session.send(msg);
        assertFalse(session.isConnected());

        msg = receiveMail(ALIREN_ALIBABA_COM);
        assertEquals(ALIREN_ALIBABA_COM, msg.getAllRecipients()[0].toString());

        // test sent date
        assertTrue(Math.abs(msg.getSentDate().getTime() - System.currentTimeMillis()) < 1000);
    }

    @Test
    public void send_raw_message_handler() throws Exception {
        Message msg = createMessage("", ALIREN_ALIBABA_COM);
        assertEquals(new Date(0), msg.getSentDate());

        session.setDebug(true);
        assertFalse(session.isConnected());

        MyHandler handler = new MyHandler(null);
        handler.state = "prepareMessage";

        session.send(msg, handler);

        assertFalse(session.isConnected());
        assertEquals("processMessage", handler.state);

        msg = receiveMail(ALIREN_ALIBABA_COM);
        assertEquals(ALIREN_ALIBABA_COM, msg.getAllRecipients()[0].toString());

        // test modified header
        assertEquals(new InternetAddress(MODIFIED_FROM_ALIBABA_COM), msg.getFrom()[0]);

        // test sent date
        assertTrue(Math.abs(msg.getSentDate().getTime() - System.currentTimeMillis()) < 1000);
    }

    @Test
    public void send_batch() throws Exception {
        session.setDebug(true);
        assertFalse(session.isConnected());

        try {
            session.connect();
            assertTrue(session.isConnected());

            session.send(createMailBuilder("我爱北京天安门！", ALIREN_ALIBABA_COM));
            assertTrue(session.isConnected());

            session.send(createMessage("我爱中国！", ALIREN_ALIBABA_COM));
            assertTrue(session.isConnected());

            session.send("simple_cn");
            assertTrue(session.isConnected());
        } finally {
            session.close();
            assertFalse(session.isConnected());
        }

        Message[] msgs = receiveMails(ALIREN_ALIBABA_COM, 2);

        assertEquals("我爱中国！", msgs[0].getSubject());
        assertEquals("我爱北京天安门！", msgs[1].getSubject());

        assertEquals("我的标题 My Subject", receiveMail("to@alibaba.com").getSubject());
    }

    @Test
    public void send_pop_before_smtp() throws Exception {
        session.setDebug(true);
        assertFalse(session.isConnected());

        // no mail service
        session.setMailService(null);
        session.setPopBeforeSmtp("mystore");

        try {
            session.send(createMessage("我爱中国！", ALIREN_ALIBABA_COM));
            fail();
        } catch (MailStoreNotFoundException e) {
            assertThat(e, exception("Could not find mail store \"mystore\": mail service is not set"));
        }

        // store not found
        session.setMailService(mailService);
        session.setPopBeforeSmtp("notExistStore");

        try {
            session.send(createMessage("我爱中国！", ALIREN_ALIBABA_COM));
            fail();
        } catch (MailStoreNotFoundException e) {
            assertThat(e, exception("Could not find mail store: notExistStore"));
        }

        // check pop before smtp
        MyMockStore.stateHolder.set("check");
        session.setPopBeforeSmtp("mystore");

        try {
            session.send(createMessage("我爱中国！", ALIREN_ALIBABA_COM));
            assertEquals("closed", MyMockStore.stateHolder.get());
        } finally {
            MyMockStore.stateHolder.remove();
        }
    }

    private Message receiveMail(String targetAddress) throws Exception {
        return receiveMails(targetAddress, 1)[0];
    }

    private Message[] receiveMails(String targetAddress, int count) throws Exception {
        Mailbox mailbox = Mailbox.get(targetAddress);
        assertEquals(count, mailbox.size());

        Message[] msgs = mailbox.toArray(new Message[mailbox.size()]);

        Arrays.sort(msgs, new Comparator<Message>() {
            public int compare(Message o1, Message o2) {
                try {
                    return o1.getSubject().compareTo(o2.getSubject());
                } catch (MessagingException e) {
                    fail(e.toString());
                    return 0;
                }
            }
        });

        return msgs;
    }

    @Override
    protected void assertCopy(MailTransport copy, Properties propsCopy) {
        assertEquals("protocol", copy.getProtocol());
        assertEquals("popBeforeSmtp", copy.getPopBeforeSmtp());
        assertEquals("true", propsCopy.remove("mail.smtp.auth"));
    }

    @Override
    protected void assertToString(String str) {
        assertThat(str, containsRegex("MailTransport \\{"));
        assertThat(str, containsRegex("popBeforeSmtp\\s+= popBeforeSmtp"));
        assertThat(str, containsRegex("protocol\\s+= protocol"));
    }

    @Override
    protected void prepareForSubclass(MailTransport session) {
        session.setProtocol("protocol");
        session.setPopBeforeSmtp("popBeforeSmtp");
    }

    @Override
    protected MailTransport createMailSession(MailService service) {
        MailTransport transport = new MailTransport();
        transport.setMailService(service);
        return transport;
    }

    @Override
    protected MailTransport copyMailSession(MailTransport session, Properties overrideProps) {
        return new MailTransport(session, overrideProps);
    }

    private static final class MyHandler extends DefaultMailTransportHandler {
        private final MailBuilder builder;
        private String state;

        private MyHandler(MailBuilder builder) {
            this.builder = builder;
        }

        @Override
        public void prepareMessage(MailBuilder b) throws MailException {
            assertEquals(null, state);

            if (builder != null) {
                assertSame(builder, b);
            }

            state = "prepareMessage";
        }

        @Override
        public void prepareConnection(Transport transport) throws MailException, MessagingException {
            assertEquals("prepareMessage", state);
            state = "prepareConnection";
            assertNotNull(transport);
        }

        @Override
        public void processMessage(Message message) throws MailException, MessagingException {
            assertEquals("prepareConnection", state);
            state = "processMessage";
            assertNotNull(message);

            message.setFrom(new InternetAddress(MODIFIED_FROM_ALIBABA_COM));
        }

        @Override
        public void messageDelivered(TransportEvent transportEvent) {
            fail("unsupported by mock-javamail");
        }

        @Override
        public void messageNotDelivered(TransportEvent transportEvent) {
            fail("unsupported by mock-javamail");
        }

        @Override
        public void messagePartiallyDelivered(TransportEvent transportEvent) {
            fail("unsupported by mock-javamail");
        }
    }
}
