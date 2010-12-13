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

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Message;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.mail.builder.MailBuilder;
import com.alibaba.citrus.service.mail.builder.MailBuilderException;
import com.alibaba.citrus.service.mail.builder.content.AttachmentContent;
import com.alibaba.citrus.service.mail.builder.content.TextContent;
import com.alibaba.citrus.service.mail.support.ResourceDataSource;

/**
 * 测试附件。
 * 
 * @author Michael Zhou
 */
public class AttachmentContentTests extends AbstractMailBuilderTests {
    private AttachmentContent content;

    @Before
    public void init() {
        content = new AttachmentContent();
    }

    @Test
    public void setSource() throws Exception {
        File f = new File(srcdir, "testfile.txt");
        URL u = f.toURI().toURL();

        content.setURL(u);

        try {
            content.setURL(u);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Attachment source already set: URL[", "testfile.txt]"));
        }

        try {
            content.setFile(f);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Attachment source already set: URL[", "testfile.txt]"));
        }

        try {
            content.setDataSource(new URLDataSource(u));
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Attachment source already set: URL[", "testfile.txt]"));
        }

        try {
            content.setResource("testfile.txt");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Attachment source already set: URL[", "testfile.txt]"));
        }

        try {
            content.setMail(new MailBuilder());
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Attachment source already set: URL[", "testfile.txt]"));
        }

        try {
            content.setMail(new MailBuilder().getMessage(rawSession));
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Attachment source already set: URL[", "testfile.txt]"));
        }

        try {
            content.setMail("attachedMail");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Attachment source already set: URL[", "testfile.txt]"));
        }
    }

    @Test
    public void url() throws Exception {
        // null url
        try {
            new AttachmentContent((URL) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("url"));
        }

        try {
            new AttachmentContent((URL) null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("url"));
        }

        try {
            content.setURL(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("url"));
        }

        // URL: text, default name
        URL url = new File(srcdir, "testfile.txt").toURI().toURL();

        content = new AttachmentContent(url);
        builder.setContent(content);

        assert_TextPlain_testfile_QuotedPrintable();

        // URL: gif image, name specified
        url = new File(srcdir, "java.gif").toURI().toURL();

        content = new AttachmentContent(url, "我的图片.gif");
        builder.setContent(content);

        assert_ImageGif_javagif_我的图片_base64();

        // toString
        String result = "";

        result += "AttachmentContent {\n";
        result += "  source   = URL[" + url.toExternalForm() + "]\n";
        result += "  fileName = 我的图片.gif\n";
        result += "}";

        assertEquals(result, content.toString());
    }

    @Test
    public void file() throws Exception {
        // null file
        try {
            new AttachmentContent((File) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("file"));
        }

        try {
            new AttachmentContent((File) null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("file"));
        }

        try {
            content.setFile(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("file"));
        }

        // File: text, default name
        File file = new File(srcdir, "testfile.txt");

        content = new AttachmentContent(file);
        builder.setContent(content);

        assert_TextPlain_testfile_QuotedPrintable();

        // File: gif image, name specified
        file = new File(srcdir, "java.gif");

        content = new AttachmentContent(file, "我的图片.gif");
        builder.setContent(content);

        assert_ImageGif_javagif_我的图片_base64();

        // toString
        String result = "";

        result += "AttachmentContent {\n";
        result += "  source   = File[" + file.getAbsolutePath() + "]\n";
        result += "  fileName = 我的图片.gif\n";
        result += "}";

        assertEquals(result, content.toString());
    }

    @Test
    public void dataSource() throws Exception {
        // null ds
        try {
            new AttachmentContent((DataSource) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("dataSource"));
        }

        try {
            new AttachmentContent((DataSource) null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("dataSource"));
        }

        try {
            content.setDataSource(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("dataSource"));
        }

        // DS: text, no name
        DataSource dataSource = new ResourceDataSource(factory.getResource("testfile.txt"), null, "text/plain") {
            @Override
            public String getName() {
                return null;
            }
        };

        content = new AttachmentContent(dataSource);
        builder.setContent(content);

        try {
            getMessageAsText();
            fail();
        } catch (MailBuilderException e) {
            assertThat(e, exception("No fileName was specified with "
                    + "DataSource[Resource[testfile.txt, loaded by ResourceLoadingService]]"));
        }

        // DS: text, no name, name specified
        content = new AttachmentContent(dataSource, "testfile.txt");
        builder.setContent(content);

        assert_TextPlain_testfile_QuotedPrintable();

        // DS: text, default name
        dataSource = new ResourceDataSource(factory.getResource("testfile.txt"), null, "text/plain");

        content = new AttachmentContent(dataSource);
        builder.setContent(content);

        assert_TextPlain_testfile_QuotedPrintable();

        // DS: gif image, name specfied
        dataSource = new ResourceDataSource(factory.getResource("java.gif"), null, "image/gif");

        content = new AttachmentContent(dataSource, "我的图片.gif");
        builder.setContent(content);

        assert_ImageGif_javagif_我的图片_base64();

        // toString
        String result = "";

        result += "AttachmentContent {\n";
        result += "  source   = DataSource[Resource[java.gif, loaded by ResourceLoadingService]]\n";
        result += "  fileName = 我的图片.gif\n";
        result += "}";

        assertEquals(result, content.toString());
    }

    @Test
    public void resource() throws Exception {
        // null resourceName
        try {
            new AttachmentContent((String) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("resourceName"));
        }

        try {
            new AttachmentContent((String) null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("resourceName"));
        }

        try {
            content.setResource(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("resourceName"));
        }

        try {
            content.setResource("  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("resourceName"));
        }

        // no resourceLoader
        content = new AttachmentContent("testfile.txt");
        builder.setContent(content);

        try {
            getMessageAsText();
            fail();
        } catch (MailBuilderException e) {
            assertThat(e, exception("Could not find resource \"testfile.txt\": no resourceLoader specified"));
        }

        // resource not found
        content = new AttachmentContent("notExist.txt");
        content.setResourceLoader(factory);
        builder.setContent(content);

        try {
            getMessageAsText();
            fail();
        } catch (MailBuilderException e) {
            assertThat(e, exception("Could not find resource \"notExist.txt\""));
        }

        // stream only resource
        content = new AttachmentContent("/asStream/testfile.txt");
        content.setResourceLoader(factory);
        builder.setContent(content);

        assert_AppOctet_testfile_base64();

        // Resource: text, default name
        content = new AttachmentContent("testfile.txt");
        content.setResourceLoader(factory);
        builder.setContent(content);

        assert_TextPlain_testfile_QuotedPrintable();

        // Resource: gif image, name specified
        content = new AttachmentContent("java.gif", "我的图片.gif");
        content.setResourceLoader(factory);
        builder.setContent(content);

        assert_ImageGif_javagif_我的图片_base64();

        // toString
        String result = "";

        result += "AttachmentContent {\n";
        result += "  source   = Resource[java.gif]\n";
        result += "  fileName = 我的图片.gif\n";
        result += "}";

        assertEquals(result, content.toString());
    }

    @Test
    public void mailBuilder() throws Exception {
        // null mailBuilder
        try {
            new AttachmentContent((MailBuilder) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("mailBuilder"));
        }

        try {
            content.setMail((MailBuilder) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("mailBuilder"));
        }

        // mailBuilder
        MailBuilder attached = new MailBuilder();

        attached.setSubject("附件标题");
        attached.setContent(new TextContent("我爱北京敏感词", "text/plain"));

        content = new AttachmentContent(attached);
        builder.setContent(content);

        assert_Mail_我爱北京敏感词();

        // toString
        String result = "";

        result += "AttachmentContent {\n";
        result += "  source   = MailBuilder[id=null]\n";
        result += "  fileName = <null>\n";
        result += "}";

        assertEquals(result, content.toString());
    }

    @Test
    public void mail() throws Exception {
        // null message
        try {
            new AttachmentContent((Message) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("mail"));
        }

        try {
            content.setMail((Message) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("mail"));
        }

        // message
        MailBuilder attached = new MailBuilder();

        attached.setSubject("附件标题");
        attached.setContent(new TextContent("我爱北京敏感词", "text/plain"));

        Message mail = attached.getMessage(rawSession);

        content = new AttachmentContent(mail);
        builder.setContent(content);

        assert_Mail_我爱北京敏感词();

        // toString
        String result = "";

        result += "AttachmentContent {\n";
        result += "  source   = Message[MimeMessage]\n";
        result += "  fileName = <null>\n";
        result += "}";

        assertEquals(result, content.toString());
    }

    @Test
    public void mailRef() throws Exception {
        // null ref
        try {
            content.setMail((String) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("mailRef"));
        }

        try {
            content.setMail("  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("mailRef"));
        }

        // no service
        content.setMail("attachedMail");
        builder.setContent(content);

        try {
            getMessageAsText();
            fail();
        } catch (MailBuilderException e) {
            assertThat(e, exception("Could not find mail \"attachedMail\": no MailService"));
        }

        // ref
        MailBuilder attached = new MailBuilder();
        attached.setSubject("附件标题");
        attached.setContent(new TextContent("我爱北京敏感词", "text/plain"));

        MailService service = createMock(MailService.class);
        expect(service.getMailBuilder("attachedMail")).andReturn(attached);
        expect(service.getMailBuilder("notExistMail")).andThrow(new MailNotFoundException());
        replay(service);

        content = new AttachmentContent();
        content.setMail("attachedMail");
        builder.setContent(content);
        builder.setMailService(service);

        assert_Mail_我爱北京敏感词();

        // notExist ref
        content = new AttachmentContent();
        content.setMail("notExistMail");
        builder.setContent(content);
        builder.setMailService(service);

        try {
            getMessageAsText();
            fail();
        } catch (MailBuilderException e) {
            assertThat(e, exception(MailNotFoundException.class, "Could not find mail \"notExistMail\""));
        }

        // toString
        String result = "";

        result += "AttachmentContent {\n";
        result += "  source   = MailRef[notExistMail]\n";
        result += "  fileName = <null>\n";
        result += "}";

        assertEquals(result, content.toString());
    }

    private void assert_TextPlain_testfile_QuotedPrintable() throws Exception {
        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Content-Type: text/plain"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: quoted-printable" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Disposition: attachment; filename=testfile.txt" + REGEX_EOL));
        assertThat(eml, containsRegex("hello=B1=A6=B1=A6"));
    }

    private void assert_AppOctet_testfile_base64() throws Exception {
        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Content-Type: application/octet-stream")); // 由于没有url，所以无法判断出text/plain
        assertThat(eml, containsRegex("Content-Transfer-Encoding: base64" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Disposition: attachment; filename=testfile.txt" + REGEX_EOL));
        assertThat(eml, containsRegex(re("aGVsbG+xprGm")));
    }

    private void assert_ImageGif_javagif_我的图片_base64() throws Exception {
        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Content-Type: image/gif"));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: base64" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Disposition: attachment;\\s*" + //
                re("filename=\"=?UTF-8?B?5oiR55qE5Zu+54mHLmdpZg==?=\"")));
    }

    private void assert_Mail_我爱北京敏感词() throws Exception {
        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Content-Type: message/rfc822" + REGEX_EOL));
        assertThat(eml, containsRegex(re("Content-Description: =?UTF-8?B?6ZmE5Lu25qCH6aKY?=") + REGEX_EOL));
        assertThat(eml, containsRegex("Subject: " + REGEX_EOL));
        assertThat(eml, containsRegex(re("Subject: =?UTF-8?B?6ZmE5Lu25qCH6aKY?=") + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Type: text/plain; charset=UTF-8" + REGEX_EOL));
        assertThat(eml, containsRegex("我爱北京敏感词"));
    }
}
