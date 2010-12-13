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
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.mail.builder.InvalidAddressException;
import com.alibaba.citrus.service.mail.builder.MailAddressType;
import com.alibaba.citrus.service.mail.builder.MailBuilder;
import com.alibaba.citrus.service.mail.builder.MailBuilderException;
import com.alibaba.citrus.service.mail.builder.content.AbstractContent;
import com.alibaba.citrus.service.mail.builder.content.MultipartContent;
import com.alibaba.citrus.service.mail.util.MailUtil;
import com.alibaba.citrus.util.io.ByteArrayOutputStream;

/**
 * 测试手工装配mail builder。
 * 
 * @author Michael Zhou
 */
public class MailBuilderTests extends AbstractMailBuilderTests {
    private final static String DATE_PATTERN = "EEE, d MMM yyyy HH:mm:ss";
    private MailService mailService;

    @Before
    public void init() {
        mailService = createMock(MailService.class);
        replay(mailService);
    }

    @Test
    public void mailService() {
        assertNull(builder.getMailService());

        builder.setMailService(mailService);
        assertSame(mailService, builder.getMailService());
    }

    @Test
    public void session() {
        // 只有在build message的过程中才可取得session。
        try {
            builder.getSession();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Not in build time"));
        }

        // 在build过程中取得session
        final Session[] holder = new Session[1];

        builder.setContent(new AbstractContent() {
            @Override
            protected AbstractContent newInstance() {
                return null;
            }

            public void render(Part mailPart) throws MessagingException {
                holder[0] = getMailBuilder().getSession();
            }
        });

        builder.getMessage(rawSession);

        assertNotNull(rawSession);
        assertSame(rawSession, holder[0]);
    }

    @Test
    public void charsets() {
        // default
        assertEquals("UTF-8", builder.getCharacterEncoding());

        // set
        builder.setCharacterEncoding("GBK");
        assertEquals("GBK", builder.getCharacterEncoding());

        // address charset
        for (MailAddressType addrType : MailAddressType.values()) {
            builder.setAddress(addrType, 中国_CHINA_EARTH_COM);
        }

        for (MailAddressType addrType : MailAddressType.values()) {
            assertAddresses(builder, addrType, "=?GBK?B?1tC5+g==?= <china@earth.com>");
        }

        // change charset
        builder.setCharacterEncoding("EUC_CN");
        assertEquals("EUC_CN", builder.getCharacterEncoding());

        for (MailAddressType addrType : MailAddressType.values()) {
            assertAddresses(builder, addrType, "=?euc-cn?B?1tC5+g==?= <china@earth.com>");
        }

        // change charset to default
        builder.setCharacterEncoding(null);
        assertEquals("UTF-8", builder.getCharacterEncoding());

        for (MailAddressType addrType : MailAddressType.values()) {
            assertAddresses(builder, addrType, "=?UTF-8?B?5Lit5Zu9?= <china@earth.com>");
        }

        // invalid charset
        try {
            builder.setCharacterEncoding("invalid_charset");
            fail();
        } catch (MailBuilderException e) {
            assertThat(e, exception(UnsupportedEncodingException.class, "Invalid charset", "invalid_charset"));
        }

        // invalid charset, with mail id
        builder.setId("myid");
        builder.setCharacterEncoding("invalid_charset");

        try {
            builder.addAddress(MailAddressType.FROM, 中国_CHINA_EARTH_COM);
            fail();
        } catch (MailBuilderException e) {
            assertThat(
                    e,
                    exception(UnsupportedEncodingException.class, "Invalid charset", "invalid_charset",
                            "specified at mail (id=\"myid\")"));
        }
    }

    @Test
    public void subject() throws Exception {
        builder.setSubject(我爱北京敏感词_I_LOVE_THE_PRESERVED_KEYWORDS);
        assertEquals(我爱北京敏感词_I_LOVE_THE_PRESERVED_KEYWORDS, builder.getSubject());

        // render as UTF-8
        assertThat(save(builder.getMessageAsString(rawSession)), containsAll( // 
                "=?UTF-8?Q?=E6=88=91=E7=88=B1=E5=8C=97=E4=BA=AC?=", //
                "=?UTF-8?Q?=E6=95=8F=E6=84=9F=E8=AF=8D_I?=", //
                "=?UTF-8?Q?_love_the_?=", //
                "=?UTF-8?Q?preserved_keywords.?="));

        // render as GBK
        builder.setCharacterEncoding("GBK");
        assertThat(save(builder.getMessageAsString(rawSession)), containsAll( // 
                "=?GBK?Q?=CE=D2=B0=AE=B1=B1=BE=A9=C3=F4=B8=D0=B4=CA_I_love_the_?=", //
                "=?GBK?Q?preserved_keywords.?="));
    }

    @Test
    public void getSentDate() throws Exception {
        Date sentDate = builder.getSentDate(); // lazy set current date

        assertNotNull(sentDate);
        assertTrue(sentDate.getTime() - System.currentTimeMillis() < 1000);

        assertThat(save(builder.getMessageAsString(rawSession)), containsAll( // 
                newDateFormat().format(sentDate)));
    }

    @Test
    public void setSentDate() throws Exception {
        builder.setSentDate(new Date(0)); // 1970s
        assertEquals(0, builder.getSentDate().getTime());

        assertThat(save(builder.getMessageAsString(rawSession)), containsAll( //
                newDateFormat().format(new Date(0))));
    }

    @Test
    public void attrs() {
        // empty attrs
        assertEquals(0, builder.getAttributeKeys().size());

        // put values
        builder.setAttribute("aaa", 111);
        builder.setAttribute("bbb", 222);
        builder.setAttribute("ccc", 333);

        assertEquals(111, builder.getAttribute("aaa"));
        assertEquals(222, builder.getAttribute("bbb"));
        assertEquals(333, builder.getAttribute("ccc"));

        Object[] keys = builder.getAttributeKeys().toArray();
        Arrays.sort(keys);

        assertArrayEquals(new Object[] { "aaa", "bbb", "ccc" }, keys);

        // remove value
        builder.setAttribute("ccc", null);
        assertEquals(null, builder.getAttribute("ccc"));

        keys = builder.getAttributeKeys().toArray();
        Arrays.sort(keys);

        assertArrayEquals(new Object[] { "aaa", "bbb" }, keys);
    }

    @Test
    public void attrs_setAttributes() {
        Map<String, Object> map = createHashMap();

        map.put("aaa", 111);
        map.put("bbb", 222);
        map.put("ccc", 333);

        builder.setAttributes(map);

        assertEquals(111, builder.getAttribute("aaa"));
        assertEquals(222, builder.getAttribute("bbb"));
        assertEquals(333, builder.getAttribute("ccc"));

        Object[] keys = builder.getAttributeKeys().toArray();
        Arrays.sort(keys);

        assertArrayEquals(new Object[] { "aaa", "bbb", "ccc" }, keys);
    }

    @Test
    public void getAddresses() throws Exception {
        // no addresses
        for (MailAddressType addrType : MailAddressType.values()) {
            assertAddresses(builder, addrType);
        }

        Set<?>[] addresses = getFieldValue(builder, "addresses", Set[].class);

        assertEquals(MailAddressType.values().length, addresses.length);

        for (Set<?> addrSet : addresses) {
            assertNull(addrSet);
        }
    }

    @Test
    public void setAddress() {
        for (MailAddressType addrType : MailAddressType.values()) {
            builder.setAddress(addrType, 美国_CHINA_EARTH_COM);
            builder.setAddress(addrType, 中国_CHINA_EARTH_COM);
        }

        for (MailAddressType addrType : MailAddressType.values()) {
            assertAddresses(builder, addrType, "=?UTF-8?B?5Lit5Zu9?= <china@earth.com>");
        }
    }

    @Test
    public void addAddress() {
        for (MailAddressType addrType : MailAddressType.values()) {
            builder.addAddress(addrType, null); // null
            builder.addAddress(addrType, ""); // empty list
            builder.addAddress(addrType, "  "); // empty list
            builder.addAddress(addrType, 美国_CHINA_EARTH_COM);
            builder.addAddress(addrType, 美国_CHINA_EARTH_COM); // dup address
            builder.addAddress(addrType, 中国_CHINA_EARTH_COM);
        }

        for (MailAddressType addrType : MailAddressType.values()) {
            assertAddresses(builder, addrType, //
                    "=?UTF-8?B?576O5Zu9?= <us@earth.com>", // 
                    "=?UTF-8?B?5Lit5Zu9?= <china@earth.com>");
        }
    }

    @Test
    public void addAddress_invalid() {
        try {
            builder.addAddress(MailAddressType.FROM, "<>");
            fail();
        } catch (InvalidAddressException e) {
            assertThat(e, exception(AddressException.class, "Invalid mail address: <>", "Empty address"));
        }

        builder.setCharacterEncoding("unknown_charset");

        try {
            builder.addAddress(MailAddressType.FROM, 中国_CHINA_EARTH_COM);
            fail();
        } catch (MailBuilderException e) {
            assertThat(e, exception(UnsupportedEncodingException.class, "Invalid charset", "unknown_charset"));
        }
    }

    @Test
    public void clone_builder_empty() {
        MailBuilder copy = (MailBuilder) builder.clone();

        assertEquals(null, copy.getMailService());
        assertEquals(null, copy.getId());
        assertEquals("UTF-8", copy.getCharacterEncoding());

        for (MailAddressType addrType : MailAddressType.values()) {
            assertAddresses(copy, addrType);
        }

        assertEquals(0, copy.getAttributeKeys().size());

        assertEquals(null, copy.getSubject());

        Date sentDate = copy.getSentDate(); // lazy set current date

        assertNotNull(sentDate);
        assertTrue(sentDate.getTime() - System.currentTimeMillis() < 1000);

        assertSame(null, copy.getContent());
    }

    @Test
    public void clone_builder() {
        // prepare
        Object obj = new Object();
        Date sentDate = new Date(0);

        initBuilder(builder, obj, sentDate);

        // clone
        MailBuilder copy = (MailBuilder) builder.clone();

        assertSame(mailService, copy.getMailService());
        assertEquals("myid", copy.getId());
        assertEquals("EUC_JP", copy.getCharacterEncoding());

        for (MailAddressType addrType : MailAddressType.values()) {
            assertAddresses(copy, addrType, "=?euc-jp?B?w+a58Q==?= <china@earth.com>");
        }

        assertEquals(1, copy.getAttributeKeys().size());
        assertSame(obj, copy.getAttribute("aaa"));

        assertEquals("my SUBJECT", copy.getSubject());

        assertSame(sentDate, copy.getSentDate());

        assertNotSame(builder.getContent(), copy.getContent());
        assertSame(obj, ((MyContent) copy.getContent()).object);
    }

    private void assertAddresses(MailBuilder builder, MailAddressType addrType, String... addrToStrings) {
        InternetAddress[] addrs = builder.getAddresses(addrType);

        assertEquals(addrToStrings.length, addrs.length);

        for (int i = 0; i < addrs.length; i++) {
            assertEquals(addrToStrings[i], addrs[i].toString());
        }
    }

    private void initBuilder(MailBuilder builder, Object obj, Date sentDate) {
        builder.setMailService(mailService);
        builder.setId("myid");
        builder.setCharacterEncoding("EUC_JP");

        for (MailAddressType addrType : MailAddressType.values()) {
            builder.addAddress(addrType, 中国_CHINA_EARTH_COM);
        }

        builder.setAttribute("aaa", obj);

        builder.setSubject("my SUBJECT");

        builder.setSentDate(sentDate);

        MyContent content = new MyContent();
        content.object = obj;
        builder.setContent(content);
    }

    @Test
    public void getMessage() throws Exception {
        MimeMessage message = builder.getMessage(rawSession);

        assertNotNull(message);

        String eml = save(MailUtil.toString(message));

        assertThat(eml, containsAllRegex( //
                "Subject:\\s*" + REGEX_EOL, //
                "Content-Type: text/plain; charset=us-ascii" + REGEX_EOL, //
                REGEX_EOL + REGEX_EOL + "$"));
    }

    @Test
    public void getMessageAsString() throws Exception {
        String eml = save(builder.getMessageAsString(rawSession));

        assertThat(eml, containsAllRegex( //
                "Subject:\\s*" + REGEX_EOL, //
                "Content-Type: text/plain; charset=us-ascii" + REGEX_EOL, //
                REGEX_EOL + REGEX_EOL + "$"));
    }

    @Test
    public void getMessageAsString_withContent() throws Exception {
        MyContent content = new MyContent();
        builder.setContent(content);

        try {
            builder.getMessageAsString(rawSession);
            fail();
        } catch (MailBuilderException e) {
            assertThat(e, exception(MessagingException.class, "Failed to render content", "no object"));
        }

        content.object = "hello, world";

        String eml = save(builder.getMessageAsString(rawSession));

        assertThat(eml, containsAllRegex( //
                "Subject:\\s*" + REGEX_EOL, //
                "Content-Type: text/plain; charset=us-ascii" + REGEX_EOL, //
                "hello, world"));
    }

    @Test
    public void writeTo() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        builder.writeTo(baos, rawSession);

        String eml = save(new String(baos.toByteArray().toByteArray()));

        assertThat(eml, containsAllRegex( //
                "Subject:\\s*" + REGEX_EOL, //
                "Content-Type: text/plain; charset=us-ascii" + REGEX_EOL, //
                REGEX_EOL + REGEX_EOL + "$"));
    }

    @Test
    public void setContent() {
        MyContent content1 = new MyContent();
        MyContent content2 = new MyContent();

        assertNoMailBuilder(content1);
        assertNoMailBuilder(content2);

        builder.setContent(content1);
        assertSame(builder, content1.getMailBuilder());
        assertNoMailBuilder(content2);

        builder.setContent(content2);
        assertNoMailBuilder(content1);
        assertSame(builder, content2.getMailBuilder());
    }

    @Test
    public void getContentById() {
        MultipartContent multi1 = new MyMultipart();
        multi1.setId("multi1");

        MultipartContent multi2 = new MyMultipart();
        multi2.setId("multi2");

        MyContent content1 = new MyContent();
        content1.setId("content1");

        MyContent content2 = new MyContent();

        // simple content with id
        builder.setContent(content1);
        assertSame(content1, builder.getContent("content1"));

        // simple content without id
        builder.setContent(content2);
        assertSame(content2, builder.getContent());
        assertNull(builder.getContent("content2"));

        // complex content
        builder.setContent(multi1);
        assertSame(multi1, builder.getContent("multi1"));
        assertNull(builder.getContent("multi2"));
        assertNull(builder.getContent("content1"));
        assertNull(builder.getContent("content2"));

        // content changed
        multi1.addContent(multi2);
        multi1.addContent(content2);
        multi2.addContent(content1);

        assertSame(multi1, builder.getContent("multi1"));
        assertSame(multi2, builder.getContent("multi2"));
        assertSame(content1, builder.getContent("content1"));
        assertNull(builder.getContent("content2"));
    }

    @Test
    public void toString_empty() {
        String result = "";

        result += "MailBuilder {\n";
        result += "  subject    = <null>\n";
        result += "  charset    = UTF-8\n";
        result += "  sentDate   = <null>\n";
        result += "  FROM       = \n";
        result += "  TO         = \n";
        result += "  CC         = \n";
        result += "  BCC        = \n";
        result += "  REPLY_TO   = \n";
        result += "  attributes = {}\n";
        result += "  content    = <null>\n";
        result += "}";

        assertEquals(result, builder.toString());
    }

    @Test
    public void toString_notEmpty() {
        Object obj = new Object();
        Date sentDate = new Date(0);

        initBuilder(builder, obj, sentDate);

        builder.addAddress(MailAddressType.TO, 美国_CHINA_EARTH_COM);

        String result = "";

        result += "MailBuilder {\n";
        result += "  id         = myid\n";
        result += "  subject    = my SUBJECT\n";
        result += "  charset    = EUC_JP\n";
        result += "  sentDate   = " + sentDate + "\n";
        result += "  FROM       = =?euc-jp?B?w+a58Q==?= <china@earth.com>\n";
        result += "  TO         = [\n";
        result += "                 [1/2] =?euc-jp?B?w+a58Q==?= <china@earth.com>\n";
        result += "                 [2/2] =?euc-jp?B?yP658Q==?= <us@earth.com>\n";
        result += "               ]\n";
        result += "  CC         = =?euc-jp?B?w+a58Q==?= <china@earth.com>\n";
        result += "  BCC        = =?euc-jp?B?w+a58Q==?= <china@earth.com>\n";
        result += "  REPLY_TO   = =?euc-jp?B?w+a58Q==?= <china@earth.com>\n";
        result += "  attributes = {\n";
        result += "                 [1/1] aaa = " + obj + "\n";
        result += "               }\n";
        result += "  content    = " + builder.getContent() + "\n";
        result += "}";

        assertEquals(result, builder.toString());
    }

    private SimpleDateFormat newDateFormat() {
        return new SimpleDateFormat(DATE_PATTERN, Locale.US);
    }

    public static class MyContent extends AbstractContent {
        Object object;

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

    public static class MyMultipart extends MultipartContent {
        @Override
        protected Multipart getMultipart() {
            return new MimeMultipart();
        }

        @Override
        protected MyMultipart newInstance() {
            return new MyMultipart();
        }
    }
}
