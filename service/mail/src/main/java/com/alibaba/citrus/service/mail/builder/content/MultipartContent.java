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

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

import com.alibaba.citrus.service.mail.builder.MailContent;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 由多部分构成的邮件内容。
 * 
 * @author Michael Zhou
 */
public abstract class MultipartContent extends AbstractContent implements
        com.alibaba.citrus.service.mail.builder.Multipart {
    private final List<MailContent> contents = createLinkedList();

    /**
     * 批量添加contents。
     */
    public void setContents(MailContent[] contents) {
        if (contents != null) {
            this.contents.clear();

            for (MailContent content : contents) {
                addContent(content);
            }
        }
    }

    /**
     * 添加一个内容部分。
     */
    public void addContent(MailContent content) {
        if (content != null) {
            content.setParentContent(this);
            contents.add(content);
        }
    }

    /**
     * 取得所有的子contents。
     */
    public MailContent[] getContents() {
        return contents.toArray(new MailContent[contents.size()]);
    }

    /**
     * 渲染邮件内容。
     */
    public void render(Part mailPart) throws MessagingException {
        Multipart multipart = getMultipart();

        for (MailContent content : contents) {
            MimeBodyPart bodyPart = new MimeBodyPart();
            content.render(bodyPart);
            multipart.addBodyPart(bodyPart);
        }

        mailPart.setContent(multipart);
    }

    /**
     * 取得<code>Multipart</code>的实现。
     */
    protected abstract Multipart getMultipart();

    /**
     * 深度复制一个content。
     */
    @Override
    protected void copyTo(AbstractContent copy) {
        for (MailContent content : contents) {
            ((MultipartContent) copy).addContent(content.clone());
        }
    }

    @Override
    public void toString(ToStringBuilder buf) {
        buf.append(contents);
    }
}
