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
package com.alibaba.citrus.service.mail.util;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.ParseException;

import org.junit.Before;
import org.junit.Test;

public class MailUtilTests {
    private Session session;

    @Before
    public void init() throws Exception {
        session = Session.getDefaultInstance(System.getProperties());
    }

    @Test
    public void getJavaCharset() {
        // returns default charset
        assertEquals("UTF-8", MailUtil.getJavaCharset(null));
        assertEquals("UTF-8", MailUtil.getJavaCharset("    "));

        // keep same
        assertEquals("GBK", MailUtil.getJavaCharset(" GBK   "));
        assertEquals("ISO-8859-1", MailUtil.getJavaCharset("ISO-8859-1"));
    }

    @Test
    public void messageToString() throws Exception {
        // specified charset
        String mailContent = MailUtil.toString(readMessage("welcome.eml"), "GBK");
        assertThat(mailContent, containsAll("zyh@alibaba-inc.com", "你好，欢迎！"));

        // default UTF-8
        mailContent = MailUtil.toString(readMessage("welcome_utf8.eml"));
        assertThat(mailContent, containsAll("zyh@alibaba-inc.com", "你好，欢迎！"));
    }

    @Test
    public void encodeHeader() throws Exception {
        // empty header
        assertEquals("", MailUtil.encodeHeader(null, "GBK"));
        assertEquals("", MailUtil.encodeHeader("", "GBK"));

        // javaCharset == mimeCharset
        assertEquals("=?GBK?Q?=D6=D0=B9=FA?=", MailUtil.encodeHeader("中国", "GBK", "Q"));

        // javaCharset != mimeCharset
        assertEquals("=?ISO-8859-1?Q?=D6=D0=B9=FA?=",
                MailUtil.encodeHeader(new String("中国".getBytes("GBK"), "8859_1"), "8859_1", "Q"));
        assertEquals("=?euc-cn?Q?=D6=D0=B9=FA?=", MailUtil.encodeHeader("中国", "EUC_CN", "Q")); // EUC_CN即GB2312_80
        assertEquals("=?euc-cn?Q?=D6=D0=B9=FA?=", MailUtil.encodeHeader("中国", "euc-cn", "Q")); // EUC_CN即GB2312_80

        // default java charset: UTF-8
        assertEquals("=?UTF-8?Q?=E4=B8=AD=E5=9B=BD?=", MailUtil.encodeHeader("中国", null, "Q"));

        // auto encoding: use the shorter one
        assertEquals("=?GBK?B?1tC5+g==?=", MailUtil.encodeHeader("中国", "GBK"));
        assertEquals("=?GBK?B?1tC5+g==?=", MailUtil.encodeHeader("中国", "GBK", null));
        assertEquals("=?GBK?Q?abcdefg=D6=D0=B9=FA?=", MailUtil.encodeHeader("abcdefg中国", "GBK"));
        assertEquals("=?GBK?Q?abcdefg=D6=D0=B9=FA?=", MailUtil.encodeHeader("abcdefg中国", "GBK", null));
    }

    @Test
    public void parseAddresses() throws Exception {
        // empty list
        assertEquals(0, MailUtil.parse(null, "UTF-8").length);
        assertEquals(0, MailUtil.parse("", "UTF-8").length);
        assertEquals(0, MailUtil.parse("  ", "UTF-8").length);
        assertEquals(0, MailUtil.parse(" , ", "UTF-8").length);

        // Java charset is UTF-8
        InternetAddress[] addrs = MailUtil.parse("=?GBK?B?1tDW0A==?= <zhong_zhong@msn.com>  , "
                + "\"国国\" <guo_guo@hotmail.com>, <aa@bb.com> , cc@dd.com", "UTF-8");

        int i = 0;
        assertEquals(4, addrs.length);
        assertAddress(addrs[i++], "中中", "=?UTF-8?B?5Lit5Lit?= <zhong_zhong@msn.com>");
        assertAddress(addrs[i++], "国国", "=?UTF-8?B?5Zu95Zu9?= <guo_guo@hotmail.com>");
        assertAddress(addrs[i++], null, "aa@bb.com");
        assertAddress(addrs[i++], null, "cc@dd.com");

        // UTF-8 as default
        addrs = MailUtil.parse("中中 <zhong_zhong@msn.com>", null);

        i = 0;
        assertEquals(1, addrs.length);
        assertAddress(addrs[i++], "中中", "=?UTF-8?B?5Lit5Lit?= <zhong_zhong@msn.com>");

        // Convert to mime charset
        addrs = MailUtil.parse("中中 <zhong_zhong@msn.com>", "EUC_CN");

        i = 0;
        assertEquals(1, addrs.length);
        assertAddress(addrs[i++], "中中", "=?euc-cn?B?1tDW0A==?= <zhong_zhong@msn.com>");

        // strict mode
        try {
            MailUtil.parse("aa@bb.com   cc@dd.com\n ee@ff.com", "UTF-8", true);
            fail();
        } catch (AddressException e) {
        }

        // loose mode by default
        addrs = MailUtil.parse("aa@bb.com   cc@dd.com\n ee@ff.com", "UTF-8");

        i = 0;
        assertEquals(3, addrs.length);
        assertAddress(addrs[i++], null, "aa@bb.com");
        assertAddress(addrs[i++], null, "cc@dd.com");
        assertAddress(addrs[i++], null, "ee@ff.com");

        // loose mode
        addrs = MailUtil.parse("aa@bb.com   cc@dd.com\n ee@ff.com", "UTF-8", false);

        i = 0;
        assertEquals(3, addrs.length);
        assertAddress(addrs[i++], null, "aa@bb.com");
        assertAddress(addrs[i++], null, "cc@dd.com");
        assertAddress(addrs[i++], null, "ee@ff.com");
    }

    @Test
    public void getContentType() throws Exception {
        // contentType is null
        try {
            MailUtil.getContentType(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("contentType"));
        }

        // parse error
        try {
            MailUtil.getContentType("", null);
            fail();
        } catch (ParseException e) {
        }

        try {
            MailUtil.getContentType("illegal", null);
            fail();
        } catch (ParseException e) {
        }

        // charset is empty
        assertEquals("text/plain", MailUtil.getContentType("text/plain", null).toString());
        assertEquals("text/plain", MailUtil.getContentType("text/plain", "").toString());
        assertEquals("text/plain", MailUtil.getContentType("text/plain", "  ").toString());

        // charset is not empty
        assertEquals("text/plain; charset=GBK", MailUtil.getContentType("text/plain", "  GBK  ").toString());
        assertEquals("text/plain; charset=euc-cn", MailUtil.getContentType("text/plain", "EUC_CN").toString());
    }

    private MimeMessage readMessage(String fileName) throws FileNotFoundException, MessagingException, IOException {
        InputStream istream = null;

        try {
            istream = new FileInputStream(new File(srcdir, fileName));
            return new MimeMessage(session, istream);
        } finally {
            if (istream != null) {
                istream.close();
            }
        }
    }

    private void assertAddress(InternetAddress addr, String personal, String toString) {
        assertEquals(personal, addr.getPersonal());
        assertEquals(toString, addr.toString());
    }
}
