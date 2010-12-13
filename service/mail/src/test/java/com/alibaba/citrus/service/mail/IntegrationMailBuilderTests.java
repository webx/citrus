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

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.mail.builder.MailBuilder;
import com.alibaba.citrus.service.mail.builder.MailContent;
import com.alibaba.citrus.service.mail.builder.Multipart;
import com.alibaba.citrus.service.mail.builder.content.AlternativeMultipartContent;
import com.alibaba.citrus.service.mail.builder.content.AttachmentContent;
import com.alibaba.citrus.service.mail.builder.content.HTMLTemplateContent;
import com.alibaba.citrus.service.mail.builder.content.MixedMultipartContent;
import com.alibaba.citrus.service.mail.builder.content.MultipartContent;
import com.alibaba.citrus.service.mail.builder.content.TextContent;
import com.alibaba.citrus.service.mail.builder.content.TextTemplateContent;
import com.alibaba.citrus.service.pull.PullService;
import com.alibaba.citrus.service.template.TemplateService;
import com.alibaba.citrus.util.StringUtil;

/**
 * 把各种content整合在一起测试。
 * 
 * @author Michael Zhou
 */
public class IntegrationMailBuilderTests extends AbstractMailBuilderTests {
    private TemplateService templateService;
    private PullService pullService;

    @Before
    public void initSuper() {
        templateService = (TemplateService) factory.getBean("templateService");
        pullService = (PullService) factory.getBean("pullService");

        assertNotNull(templateService);
        assertNotNull(pullService);
    }

    @Test
    public void build() throws Exception {
        builder = createVeryComplexMail();
        MailBuilder copy = builder.clone();

        // 检查content tree是否为类型相同，实例不同
        Object[] tree1 = getMailBuilderTree(builder);
        Object[] tree2 = getMailBuilderTree(copy);

        assertEquals(tree1.length, tree2.length);

        for (int i = 0; i < tree1.length; i++) {
            Object obj1 = tree1[i];
            Object obj2 = tree2[i];

            assertNotSame(obj1, obj2);
            assertEquals(obj1.getClass(), obj2.getClass());
        }

        // 检查copy生成的message内容
        String eml = getMessageAsText();

        assertEquals(1, StringUtil.countMatches(eml, "Content-Type: multipart/alternative"));
        assertEquals(1, StringUtil.countMatches(eml, "Content-Type: multipart/mixed"));
        assertEquals(2, StringUtil.countMatches(eml, "Content-Type: text/plain; charset=UTF-8"));
        assertEquals(3, StringUtil.countMatches(eml, "Content-Type: text/plain"));
        assertEquals(2, StringUtil.countMatches(eml, "Content-Type: text/html; charset=UTF-8"));
        assertEquals(1, StringUtil.countMatches(eml, "Content-Transfer-Encoding: quoted-printable"));
        assertEquals(1, StringUtil.countMatches(eml, "Content-Disposition: attachment; filename=testfile.txt"));
        assertEquals(1, StringUtil.countMatches(eml, "hello=B1=A6=B1=A6"));
        assertEquals(1, StringUtil.countMatches(eml, "Content-Type: multipart/related"));
        assertEquals(1, StringUtil.countMatches(eml, "Content-Type: image/gif"));
        assertEquals(2, StringUtil.countMatches(eml, "Content-Transfer-Encoding: base64"));
        assertEquals(1, StringUtil.countMatches(eml, "Content-Disposition: inline; filename=java.gif"));
        assertEquals(1, StringUtil.countMatches(eml, "Content-Disposition: inline; filename=bible.jpg"));
    }

    private MailBuilder createVeryComplexMail() {
        MailBuilder builder = new MailBuilder();

        // 创建所有contents。
        MultipartContent attachable = new MixedMultipartContent();
        attachable.setId("attachable");

        MultipartContent alternative = new AlternativeMultipartContent();
        alternative.setId("alternative");

        TextContent plainText = new TextContent("我爱北京敏感词", "text/plain");
        plainText.setId("plainText");

        TextContent htmlText = new TextContent("<爱北京敏感词>", "text/html");
        htmlText.setId("htmlText");

        TextTemplateContent plainTextTemplate = new TextTemplateContent("mail/mytemplate.vm", "text/plain");
        plainTextTemplate.setId("plainTextTemplate");

        AttachmentContent textAttachment = new AttachmentContent("testfile.txt");
        textAttachment.setId("textAttachment");

        HTMLTemplateContent htmlTemplate = new HTMLTemplateContent("mail/complexhtml.vm");
        htmlTemplate.setId("htmlTemplate");

        plainTextTemplate.setTemplateService(templateService);
        plainTextTemplate.setPullService(pullService);

        htmlTemplate.setTemplateService(templateService);
        htmlTemplate.setPullService(pullService);
        htmlTemplate.setResourceLoader(factory);
        htmlTemplate.addInlineResource("image", "/");

        textAttachment.setResourceLoader(factory);

        // 加入builder
        builder.setContent(attachable);
        {
            attachable.addContent(alternative);
            {
                alternative.addContent(plainText);
                alternative.addContent(htmlText);
            }

            attachable.addContent(plainTextTemplate);
            attachable.addContent(textAttachment);
            attachable.addContent(htmlTemplate);
        }

        // 检查getId()
        assertSame(attachable, builder.getContent("attachable"));
        assertSame(alternative, builder.getContent("alternative"));
        assertSame(plainText, builder.getContent("plainText"));
        assertSame(htmlText, builder.getContent("htmlText"));
        assertSame(plainTextTemplate, builder.getContent("plainTextTemplate"));
        assertSame(textAttachment, builder.getContent("textAttachment"));
        assertSame(htmlTemplate, builder.getContent("htmlTemplate"));

        return builder;
    }

    private Object[] getMailBuilderTree(MailBuilder builder) {
        List<Object> list = createArrayList();

        list.add(builder);
        getMailBuilderTree(builder.getContent(), list);

        return list.toArray();
    }

    private void getMailBuilderTree(MailContent content, List<Object> list) {
        list.add(content);

        if (content instanceof Multipart) {
            MailContent[] subcontents = ((Multipart) content).getContents();

            for (MailContent subcontent : subcontents) {
                getMailBuilderTree(subcontent, list);
            }
        }
    }
}
