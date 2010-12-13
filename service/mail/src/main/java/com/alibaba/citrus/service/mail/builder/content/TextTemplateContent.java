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
package com.alibaba.citrus.service.mail.builder.content;

import static com.alibaba.citrus.service.mail.MailConstant.*;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;

import com.alibaba.citrus.service.mail.util.MailUtil;

/**
 * 用模板生成的纯文本的内容。
 * 
 * @author Michael Zhou
 */
public class TextTemplateContent extends TemplateContent {
    /**
     * 创建一个<code>TextTemplateContent</code>。
     */
    public TextTemplateContent() {
    }

    /**
     * 创建一个<code>TextTemplateContent</code>。
     */
    public TextTemplateContent(String templateName) {
        setTemplate(templateName);
    }

    /**
     * 创建一个<code>TextTemplateContent</code>。
     */
    public TextTemplateContent(String templateName, String contentType) {
        setTemplate(templateName);
        setContentType(contentType);
    }

    /**
     * 渲染邮件内容。
     */
    public void render(Part mailPart) throws MessagingException {
        String text = renderTemplate();
        String contentType = getContentType();
        ContentType contentTypeObject = MailUtil.getContentType(contentType, getMailBuilder().getCharacterEncoding());

        mailPart.setContent(text, contentTypeObject.toString());
        mailPart.setHeader(CONTENT_TRANSFER_ENCODING, DEFAULT_TRANSFER_ENCODING);
    }

    @Override
    protected TextTemplateContent newInstance() {
        return new TextTemplateContent();
    }
}
