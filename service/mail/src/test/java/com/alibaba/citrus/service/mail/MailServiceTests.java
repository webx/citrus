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
package com.alibaba.citrus.service.mail;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;

import com.alibaba.citrus.service.mail.builder.MailAddressType;
import com.alibaba.citrus.service.mail.builder.MailBuilder;
import com.alibaba.citrus.service.mail.builder.content.AbstractContent;
import com.alibaba.citrus.service.mail.builder.content.AlternativeMultipartContent;
import com.alibaba.citrus.service.mail.builder.content.AttachmentContent;
import com.alibaba.citrus.service.mail.builder.content.HTMLTemplateContent;
import com.alibaba.citrus.service.mail.builder.content.MixedMultipartContent;
import com.alibaba.citrus.service.mail.builder.content.TextContent;
import com.alibaba.citrus.service.mail.builder.content.TextTemplateContent;
import com.alibaba.citrus.service.mail.impl.MailServiceImpl;
import com.alibaba.citrus.service.mail.session.MailStore;
import com.alibaba.citrus.service.mail.session.MailTransport;
import com.alibaba.citrus.util.UnexpectedFailureException;

/**
 * 测试mail service及其配置。
 * 
 * @author Michael Zhou
 */
public class MailServiceTests extends AbstractMailBuilderTests {
    @Before
    public void init() {
        mailService = (MailServiceImpl) factory.getBean("mailService");
        assertSame(mailService, factory.getBean("mails"));
        assertNotNull(mailService);
    }

    @Test
    public void importedServices() throws Exception {
        mailService = new MailServiceImpl();

        List<?> importedServices = getFieldValue(mailService, "importedServices", List.class);

        assertNotNull(importedServices);
        assertTrue(importedServices.isEmpty());

        // set null
        mailService.setImportedServices(null);
        assertTrue(importedServices.isEmpty());

        // set arrays of null
        mailService.setImportedServices(new Object[] { 1, null, true });
        assertEquals(2, importedServices.size());
        assertEquals(1, importedServices.get(0));
        assertEquals(true, importedServices.get(1));

        // reset
        mailService.setImportedServices(new Object[] { 2, false });
        assertEquals(2, importedServices.size());
        assertEquals(2, importedServices.get(0));
        assertEquals(false, importedServices.get(1));

        // getService(null)
        try {
            mailService.getService(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("serviceType"));
        }

        // getService(type, null)
        assertEquals(2, mailService.getService(Integer.class, null).intValue());
        assertEquals(false, mailService.getService(Boolean.class, null).booleanValue());
        assertEquals(null, mailService.getService(Long.class, null)); // not found

        // getService(type, null) with factory
        BeanFactory factory = createMock(BeanFactory.class);
        expect(factory.getBean("myLong", Long.class)).andReturn(123L).anyTimes();
        expect(factory.getBean("myLong", Double.class)).andReturn(123L).anyTimes();
        expect(factory.getBean("notExist", Double.class)).andReturn(null).anyTimes();
        replay(factory);

        try {
            mailService.setBeanFactory(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("beanFactory"));
        }

        mailService.setBeanFactory(factory);

        assertEquals(2, mailService.getService(Integer.class, null).intValue());
        assertEquals(false, mailService.getService(Boolean.class, null).booleanValue());
        assertEquals(null, mailService.getService(Long.class, null)); // not found

        // getService(type, "xxx") with factory
        assertEquals(2, mailService.getService(Integer.class, "myLong").intValue());
        assertEquals(false, mailService.getService(Boolean.class, "myLong").booleanValue());
        assertEquals(123L, mailService.getService(Long.class, "myLong").longValue()); // default bean
        assertEquals(null, mailService.getService(Double.class, "notExist")); // not found

        try {
            mailService.getService(Double.class, "myLong"); // wrong type
            fail();
        } catch (ClassCastException e) {
        }
    }

    @Test
    public void importedServices_config() throws Exception {
        mailService = (MailServiceImpl) factory.getBean("importServices");
        assertNotSame(mailService, factory.getBean("mails"));
        assertNotNull(mailService);

        assertEquals(1234L, mailService.getService(Long.class, "myLong").longValue()); // imported value
        assertEquals(false, mailService.getService(Boolean.class, "myBoolean").booleanValue()); // imported value
        assertEquals(123.456D, mailService.getService(Double.class, "myDouble").doubleValue(), 0.01); // default value
        assertEquals(null, mailService.getService(Float.class, null)); // not found

        // import null
        try {
            initFactory("services_import_null.xml");
            fail();
        } catch (FatalBeanException e) {
            assertThat(e, exception(IllegalArgumentException.class, "miss serviceRef"));
        }
    }

    @Test
    public void setMails() {
        MailServiceImpl mailService = new MailServiceImpl();

        // miss mail id
        Map<String, MailBuilder> mails = createHashMap();
        mails.put(null, new MailBuilder());

        try {
            mailService.setMails(mails);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("mail id"));
        }

        // miss mail object
        mails = createHashMap();
        mails.put("id", null);

        try {
            mailService.setMails(mails);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("mail builder"));
        }

        // set null
        mailService = new MailServiceImpl();
        mailService.setMails(null);

        // reset mails
        mails = createHashMap();
        mails.put("mail1", new MailBuilder());
        mails.put("mail2", new MailBuilder());
        mailService.setMails(mails);

        assertNotNull(mailService.getMailBuilder("mail1"));
        assertNotNull(mailService.getMailBuilder("mail2"));

        mails = createHashMap();
        mails.put("mail3", new MailBuilder());
        mails.put("mail4", new MailBuilder());
        mailService.setMails(mails);

        try {
            mailService.getMailBuilder("mail1");
            fail();
        } catch (MailNotFoundException e) {
            assertThat(e, exception("Could not find mail builder: mail1"));
        }

        try {
            mailService.getMailBuilder("mail2");
            fail();
        } catch (MailNotFoundException e) {
            assertThat(e, exception("Could not find mail builder: mail2"));
        }

        assertNotNull(mailService.getMailBuilder("mail3"));
        assertNotNull(mailService.getMailBuilder("mail4"));
    }

    @Test
    public void setMailTransports() {
        MailServiceImpl mailService = new MailServiceImpl();

        // set null
        mailService.setMailTransports(null);

        // reset transports
        Map<String, MailTransport> transports = createHashMap();
        transports.put("transport1", new MailTransport());
        transports.put("transport2", new MailTransport());
        mailService.setMailTransports(transports);

        assertNotNull(mailService.getMailTransport("transport1"));
        assertNotNull(mailService.getMailTransport("transport2"));

        transports = createHashMap();
        transports.put("transport3", new MailTransport());
        transports.put("transport4", new MailTransport());
        mailService.setMailTransports(transports);

        try {
            mailService.getMailTransport("transport1");
            fail();
        } catch (MailTransportNotFoundException e) {
            assertThat(e, exception("Could not find mail transport: transport1"));
        }

        try {
            mailService.getMailTransport("transport2");
            fail();
        } catch (MailTransportNotFoundException e) {
            assertThat(e, exception("Could not find mail transport: transport2"));
        }

        assertNotNull(mailService.getMailTransport("transport3"));
        assertNotNull(mailService.getMailTransport("transport4"));
    }

    @Test
    public void setMailStores() {
        MailServiceImpl mailService = new MailServiceImpl();

        // set null
        mailService.setMailStores(null);

        // reset stores
        Map<String, MailStore> stores = createHashMap();
        stores.put("store1", new MailStore());
        stores.put("store2", new MailStore());
        mailService.setMailStores(stores);

        assertNotNull(mailService.getMailStore("store1"));
        assertNotNull(mailService.getMailStore("store2"));

        stores = createHashMap();
        stores.put("store3", new MailStore());
        stores.put("store4", new MailStore());
        mailService.setMailStores(stores);

        try {
            mailService.getMailStore("store1");
            fail();
        } catch (MailStoreNotFoundException e) {
            assertThat(e, exception("Could not find mail store: store1"));
        }

        try {
            mailService.getMailStore("store2");
            fail();
        } catch (MailStoreNotFoundException e) {
            assertThat(e, exception("Could not find mail store: store2"));
        }

        assertNotNull(mailService.getMailStore("store3"));
        assertNotNull(mailService.getMailStore("store4"));
    }

    @Test
    public void getTransport() {
        MailServiceImpl mailService = new MailServiceImpl();

        Map<String, MailTransport> transports = createHashMap();
        transports.put("transport1", new MailTransport());
        mailService.setMailTransports(transports);

        // id is null
        try {
            mailService.getMailTransport((String) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no mailTransport id"));
        }

        // clone
        MailTransport transport = mailService.getMailTransport("transport1");

        assertNotNull(transport);
        assertNotSame(transport, mailService.getMailTransport("transport1"));
    }

    @Test
    public void getDefaultTransport() {
        MailServiceImpl mailService = new MailServiceImpl();

        // 1 transport
        Map<String, MailTransport> transports = createHashMap();
        transports.put("transport1", new MailTransport());
        mailService.setMailTransports(transports);

        assertNotNull(mailService.getMailTransport()); // the only transport is the default transport

        // 2 transports
        transports.put("transport2", new MailTransport());
        mailService.setMailTransports(transports);

        try {
            mailService.getMailTransport();
            fail();
        } catch (MailTransportNotFoundException e) {
            assertThat(e, exception("Could not find mail transport: _DEFAULT_"));
        }

        // with default transport
        MailTransport transport3 = new MailTransport();
        transport3.setDefault(true);
        transports.put("transport3", transport3);
        mailService.setMailTransports(transports);

        assertNotNull(mailService.getMailTransport());
        assertNotNull(mailService.getMailTransport("transport3"));

        // 2 default transports
        MailTransport transport4 = new MailTransport();
        transport4.setDefault(true);
        transports.put("transport4", transport4);

        try {
            mailService.setMailTransports(transports);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("more than 1 default transports"));
        }
    }

    @Test
    public void getStore() {
        MailServiceImpl mailService = new MailServiceImpl();

        Map<String, MailStore> stores = createHashMap();
        stores.put("store1", new MailStore());
        mailService.setMailStores(stores);

        // id is null
        try {
            mailService.getMailStore((String) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no mailStore id"));
        }

        // clone
        MailStore store = mailService.getMailStore("store1");

        assertNotNull(store);
        assertNotSame(store, mailService.getMailStore("store1"));
    }

    @Test
    public void getDefaultStore() {
        MailServiceImpl mailService = new MailServiceImpl();

        // 1 store
        Map<String, MailStore> stores = createHashMap();
        stores.put("store1", new MailStore());
        mailService.setMailStores(stores);

        assertNotNull(mailService.getMailStore()); // the only store is the default store

        // 2 stores
        stores.put("store2", new MailStore());
        mailService.setMailStores(stores);

        try {
            mailService.getMailStore();
            fail();
        } catch (MailStoreNotFoundException e) {
            assertThat(e, exception("Could not find mail store: _DEFAULT_"));
        }

        // with default store
        MailStore store3 = new MailStore();
        store3.setDefault(true);
        stores.put("store3", store3);
        mailService.setMailStores(stores);

        assertNotNull(mailService.getMailStore());
        assertNotNull(mailService.getMailStore("store3"));

        // 2 default stores
        MailStore store4 = new MailStore();
        store4.setDefault(true);
        stores.put("store4", store4);

        try {
            mailService.setMailStores(stores);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("more than 1 default stores"));
        }
    }

    @Test
    public void toString_() {
        String str = mailService.toString();
        assertThat(str, containsAllRegex("MailService \\{", "mails\\s+=", "stores\\s+=", "transports\\s+="));
    }

    @Test
    public void mail_empty() throws Exception {
        getAndAssertMail("empty", "UTF-8", null); // default charset, no subject

        for (MailAddressType addrType : MailAddressType.values()) {
            assertEquals(0, builder.getAddresses(addrType).length);
        }

        assertNull(builder.getContent());

        // no content and javamail default charset
        assertThat(save(builder.getMessageAsString(rawSession)), containsAllRegex( //
                "Content-Type: text/plain; charset=us-ascii" + REGEX_EOL, //
                "Content-Transfer-Encoding: 7bit" + REGEX_EOL, //
                REGEX_EOL + REGEX_EOL + "$"));
    }

    @Test
    public void mail_simple_cn() throws Exception {
        getAndAssertMail("simple_cn", "EUC_CN", "我的标题 My Subject");

        for (MailAddressType addrType : MailAddressType.values()) {
            InternetAddress[] addrs = builder.getAddresses(addrType);

            switch (addrType) {
                case TO:
                    // 一行<to>中包括多个地址

                case CC:
                    // 两行<cc>
                    assertEquals(2, addrs.length);

                    assertEquals("我的地址", addrs[0].getPersonal());
                    assertEquals("我的地址", addrs[1].getPersonal());

                    assertEquals(addrType.getTagName() + "@alibaba.com", addrs[0].getAddress());
                    assertEquals(addrType.getTagName() + "2@alibaba.com", addrs[1].getAddress());

                    break;

                case FROM:
                    // addr中包含空白，被除去

                default:
                    assertEquals(1, addrs.length);
                    assertEquals("我的地址", addrs[0].getPersonal());
                    assertEquals(addrType.getTagName() + "@alibaba.com", addrs[0].getAddress());
                    break;
            }
        }

        MyContent content = (MyContent) builder.getContent();

        assertEquals("test", content.object);

        String eml = getMessageAsText();

        assertThat(eml, containsRegex(re("From: =?euc-cn?B?ztK1xLXY1rc=?= <from@alibaba.com>") + REGEX_EOL));
        assertThat(eml, containsRegex(re("Reply-To: =?euc-cn?B?ztK1xLXY1rc=?= <reply-to@alibaba.com>") + REGEX_EOL));
        assertThat(eml, containsRegex(re("To: =?euc-cn?B?ztK1xLXY1rc=?= <to@alibaba.com>,") + "\\s+"
                + re("=?euc-cn?B?ztK1xLXY1rc=?= <to2@alibaba.com>") + REGEX_EOL));
        assertThat(eml, containsRegex(re("Cc: =?euc-cn?B?ztK1xLXY1rc=?= <cc@alibaba.com>,") + "\\s+"
                + re("=?euc-cn?B?ztK1xLXY1rc=?= <cc2@alibaba.com>") + REGEX_EOL));
        assertThat(eml, containsRegex(re("Bcc: =?euc-cn?B?ztK1xLXY1rc=?= <bcc@alibaba.com>") + REGEX_EOL));
        assertThat(eml, containsRegex(re("Subject: =?euc-cn?Q?=CE=D2=B5=C4=B1=EA=CC=E2_My_Subject?=") + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: text/plain; charset=us-ascii" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 7bit" + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "test$"));
    }

    @Test
    public void mail_textContent() throws Exception {
        // no text
        getAndAssertMail("textContent_empty", "GBK", null);

        TextContent content = (TextContent) builder.getContent();
        assertEquals("text/plain", content.getContentType());
        assertEquals("", content.getText());

        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: text/plain; charset=GBK" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "$"));

        // with text
        getAndAssertMail("textContent_withText", "GBK", null);

        content = (TextContent) builder.getContent();
        assertEquals("text/html", content.getContentType());
        assertEquals("hello, 中国", content.getText());

        eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: text/html; charset=GBK" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "hello, 中国$"));
    }

    @Test
    public void mail_attachmentContent() throws Exception {
        // no resource
        getAndAssertMail("attachmentContent_empty", "GBK", null);
        assertThat(builder.getContent(), instanceOf(AttachmentContent.class));

        try {
            getMessageAsText();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("No attachment source was specified"));
        }

        // with resource
        getAndAssertMail("attachmentContent", "GBK", null);
        assertThat(builder.getContent(), instanceOf(AttachmentContent.class));

        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: text/plain")); // 文本附件的编码是取决于系统的，不一定是GBK
        assertThat(eml, containsRegex("Content-Transfer-Encoding: quoted-printable" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Disposition: attachment; filename=testfile.txt" + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "hello=B1=A6=B1=A6$"));

        // with fileName
        getAndAssertMail("attachmentContent_withFileName", "GBK", null);
        assertThat(builder.getContent(), instanceOf(AttachmentContent.class));

        eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: text/plain")); // 文本附件的编码是取决于系统的，不一定是GBK
        assertThat(eml, containsRegex("Content-Transfer-Encoding: quoted-printable" + REGEX_EOL));
        assertThat(eml, containsRegex(re("Content-Disposition: attachment; filename=\"=?GBK?B?ztK1xM7EvP4udHh0?=\"")
                + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "hello=B1=A6=B1=A6$"));
    }

    @Test
    public void mail_textTemplateContent() throws Exception {
        // no template name
        getAndAssertMail("textTemplateContent_empty", "GBK", null);
        assertThat(builder.getContent(), instanceOf(TextTemplateContent.class));

        try {
            getMessageAsText();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("templateName"));
        }

        // velocity template
        getAndAssertMail("textTemplateContent_vm", "GBK", null);
        assertThat(builder.getContent(), instanceOf(TextTemplateContent.class));

        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: text/plain; charset=GBK"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "velocity" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));
        assertThat(eml, containsRegex("Constant\\[MailConstant\\]"));
        assertThat(eml, containsRegex("\\$hello"));

        // velocity template, with context vars
        builder.setAttribute("hello", "中国");

        eml = getMessageAsText();

        assertThat(eml, not(containsRegex("\\$hello")));
        assertThat(eml, containsRegex("中国"));

        // freemarker template, contentType=text/html
        getAndAssertMail("textTemplateContent_ftl", "GBK", null);
        assertThat(builder.getContent(), instanceOf(TextTemplateContent.class));

        builder.setAttribute("hello", "中国");

        eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: text/html; charset=GBK"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "freemarker" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));
        assertThat(eml, containsRegex("Constant\\[MailConstant\\]"));
        assertThat(eml, containsRegex("中国"));
    }

    @Test
    public void mail_htmlTemplateContent() throws Exception {
        // no template name
        getAndAssertMail("htmlTemplateContent_empty", "GBK", null);
        assertThat(builder.getContent(), instanceOf(HTMLTemplateContent.class));

        try {
            getMessageAsText();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("templateName"));
        }

        // velocity - no resource
        getAndAssertMail("htmlTemplateContent_vm", "GBK", null);
        assertThat(builder.getContent(), instanceOf(HTMLTemplateContent.class));

        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, not(containsRegex("Content-Type: multipart/related;")));
        assertThat(eml, containsRegex("Content-Type: text/html; charset=GBK"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "velocity" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));
        assertThat(eml, containsString("image URL1: <img src=\"$image.getURI(\"java.gif\")\"/>"));
        assertThat(eml, containsString("image URL2: <img src=\"$image.getURI(\"java.gif\")\"/>"));
        assertThat(eml, containsString("image URL3: <img src=\"$image.getURI(\"subdir/bible.jpg\")\"/>"));

        // freemarker - with resource but not used
        getAndAssertMail("htmlTemplateContent_ftl", "GBK", null);
        assertThat(builder.getContent(), instanceOf(HTMLTemplateContent.class));

        eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, not(containsRegex("Content-Type: multipart/related;")));

        assertThat(eml, containsRegex("Content-Type: text/plain; charset=GBK"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "freemarker" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));
        assertThat(eml, containsRegex("Constant\\[MailConstant\\]"));
        assertThat(eml, containsString("$hello"));

        // velocity - with resource and used
        getAndAssertMail("htmlTemplateContent_vm_withResource", "GBK", null);
        assertThat(builder.getContent(), instanceOf(HTMLTemplateContent.class));

        eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: multipart/related;"));

        Pattern cidPattern = Pattern.compile("<img src=\"cid:([^\\\"]+)\"/>");
        Matcher cidMatcher = cidPattern.matcher(eml);

        assertTrue(cidMatcher.find());
        String id1 = cidMatcher.group(1);

        assertTrue(cidMatcher.find());
        assertEquals(id1, cidMatcher.group(1));

        assertTrue(cidMatcher.find());
        String id2 = cidMatcher.group(1);

        assertFalse(cidMatcher.find());

        // part 1
        assertThat(eml, containsRegex("Content-Type: text/html; charset=GBK"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "velocity" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));
        assertThat(eml, containsString("image URL1: <img src=\"cid:" + id1));
        assertThat(eml, containsString("image URL2: <img src=\"cid:" + id1));
        assertThat(eml, containsString("image URL3: <img src=\"cid:" + id2));

        // part 2
        assertThat(eml, containsRegex("Content-Type: image/gif"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: base64" + REGEX_EOL));
        assertThat(eml, containsString("Content-ID: <" + id1 + ">"));
        assertThat(eml, containsRegex("Content-Disposition: inline; filename=java.gif" + REGEX_EOL));

        // part 3
        assertThat(eml, containsRegex("Content-Type: image/jpeg"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: base64" + REGEX_EOL));
        assertThat(eml, containsString("Content-ID: <" + id2 + ">"));
        assertThat(eml, containsRegex("Content-Disposition: inline; filename=bible.jpg" + REGEX_EOL));
    }

    @Test
    public void mail_mixedContent() throws Exception {
        // no contents
        getAndAssertMail("mixedContent_empty", "GBK", null);
        assertThat(builder.getContent(), instanceOf(MixedMultipartContent.class));

        try {
            getMessageAsText();
            fail();
        } catch (UnexpectedFailureException e) {
            assertThat(e, exception(IOException.class, "Empty multipart"));
        }

        // with attachment
        getAndAssertMail("mixedContent", "GBK", null);
        assertThat(builder.getContent(), instanceOf(MixedMultipartContent.class));

        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: multipart/mixed;"));

        // part 1
        assertThat(eml, containsRegex("Content-Type: text/plain; charset=GBK" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "velocity" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));

        // part 2
        assertThat(eml, containsRegex("Content-Type: text/plain; charset=GBK"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: quoted-printable" + REGEX_EOL //
                + re("Content-Disposition: attachment; filename=\"=?GBK?B?ztK1xM7EvP4udHh0?=\"") + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "hello=B1=A6=B1=A6" + REGEX_EOL));

        // part 3
        assertThat(eml, containsRegex("Content-Type: image/gif"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: base64" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Disposition: attachment; filename=java.gif" + REGEX_EOL));
    }

    @Test
    public void mail_alternativeContent() throws Exception {
        // no contents
        getAndAssertMail("alternativeContent_empty", "GBK", null);
        assertThat(builder.getContent(), instanceOf(AlternativeMultipartContent.class));

        try {
            getMessageAsText();
            fail();
        } catch (UnexpectedFailureException e) {
            assertThat(e, exception(IOException.class, "Empty multipart"));
        }

        // with attachment
        getAndAssertMail("alternativeContent", "GBK", null);
        assertThat(builder.getContent(), instanceOf(AlternativeMultipartContent.class));

        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: multipart/alternative;"));

        // part 1
        assertThat(eml, containsRegex("Content-Type: text/plain; charset=GBK" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "velocity" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));

        // part 2
        assertThat(eml, containsRegex("Content-Type: multipart/related;"));

        // part 2.1
        assertThat(eml, containsRegex("Content-Type: text/html; charset=GBK" + REGEX_EOL));

        // part 2.2
        assertThat(eml, containsRegex("Content-Type: image/gif"));

        // part 2.3
        assertThat(eml, containsRegex("Content-Type: image/jpeg"));
    }

    @Test
    public void outlookStyle_withAttachments() throws Exception {
        getAndAssertMail("outlookStyle_withAttachments", "GBK", null);
        assertThat(builder.getContent(), instanceOf(MixedMultipartContent.class));

        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: multipart/mixed;"));

        // part 1: multipart/alternative
        assertThat(eml, containsRegex("Content-Type: multipart/alternative;"));

        // part 1.1: text content
        assertThat(eml, containsRegex("Content-Type: text/plain; charset=GBK" + REGEX_EOL));

        // part 1.2: html content
        assertThat(eml, containsRegex("Content-Type: multipart/related;"));

        // part 1.2.1: text/html
        assertThat(eml, containsRegex("Content-Type: text/html; charset=GBK" + REGEX_EOL));

        // part 1.2.2: gif
        assertThat(eml, containsRegex("Content-Disposition: inline; filename=java.gif" + REGEX_EOL));

        // part 1.2.3: jpg
        assertThat(eml, containsRegex("Content-Disposition: inline; filename=bible.jpg" + REGEX_EOL));

        // part 2: text attachment
        assertThat(eml, containsRegex("Content-Type: text/plain; charset=GBK"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: quoted-printable" + REGEX_EOL //
                + re("Content-Disposition: attachment; filename=\"=?GBK?B?ztK1xM7EvP4udHh0?=\"") + REGEX_EOL));
        assertThat(eml, containsRegex(REGEX_EOL + REGEX_EOL + "hello=B1=A6=B1=A6" + REGEX_EOL));

        // part 3: gif attachment
        assertThat(eml, containsRegex("Content-Type: image/gif"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: base64" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Disposition: attachment; filename=java.gif" + REGEX_EOL));
    }

    private void getAndAssertMail(String id, String charset, String subject) {
        builder = mailService.getMailBuilder(id);

        assertNotSame(builder, mailService.getMailBuilder(id)); // 两次取得不同的对象

        assertEquals(charset, builder.getCharacterEncoding());
        assertSame(mailService, builder.getMailService());
        assertTrue(builder.getAttributeKeys().isEmpty());
        assertEquals(id, builder.getId());
        assertEquals(subject, builder.getSubject());
    }

    public static class MyContent extends AbstractContent {
        Object object;

        public void setObject(Object obj) {
            this.object = obj;
        }

        public void render(Part mailPart) throws MessagingException {
            if (object == null) {
                throw new MessagingException("no object");
            } else {
                mailPart.setContent(object, "text/plain");
            }
        }

        @Override
        protected MyContent newInstance() {
            MyContent copy = new MyContent();
            copy.object = object;
            return copy;
        }
    }
}
