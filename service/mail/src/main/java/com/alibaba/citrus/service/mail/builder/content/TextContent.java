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
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;

import com.alibaba.citrus.service.mail.util.MailUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 基于文本的邮件内容，例如纯文本和HTML。
 * 
 * @author Michael Zhou
 */
public class TextContent extends AbstractContent {
    private String text;
    private String contentType;

    /**
     * 创建一个<code>TextContent</code>。
     */
    public TextContent() {
    }

    /**
     * 创建一个<code>TextContent</code>。
     */
    public TextContent(String text) {
        setText(text);
    }

    /**
     * 创建一个<code>TextContent</code>。
     */
    public TextContent(String text, String contentType) {
        setText(text);
        setContentType(contentType);
    }

    /**
     * 取得文本的内容。
     */
    public String getText() {
        return defaultIfNull(text, EMPTY_STRING);
    }

    /**
     * 设置文本的内容。
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * 取得文本的content type。
     */
    public String getContentType() {
        return defaultIfNull(this.contentType, CONTENT_TYPE_TEXT_PLAIN);
    }

    /**
     * 设置文本的content type。
     */
    public void setContentType(String contentType) {
        this.contentType = trimToNull(contentType);
    }

    /**
     * 渲染邮件内容。
     */
    public void render(Part mailPart) throws MessagingException {
        String text = getText();
        String contentType = getContentType();
        ContentType contentTypeObject = MailUtil.getContentType(contentType, getMailBuilder().getCharacterEncoding());

        mailPart.setContent(text, contentTypeObject.toString());
        mailPart.setHeader(CONTENT_TRANSFER_ENCODING, DEFAULT_TRANSFER_ENCODING);
    }

    /**
     * 创建一个同类型的content。
     */
    @Override
    protected TextContent newInstance() {
        return new TextContent(text, contentType);
    }

    @Override
    protected void toString(MapBuilder mb) {
        mb.append("contentType", getContentType());
        mb.append("text", text);
    }
}
