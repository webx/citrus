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

/**
 * Mail相关常量。
 * 
 * @author Michael Zhou
 */
public final class MailConstant {
    /** 当没有指定text类型的content的charset时, 使用默认值。 */
    public final static String DEFAULT_CHARSET = "UTF-8";

    /** 传输编码方式的header名。 */
    public final static String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";

    /** 内容描述的header名。 */
    public final static String CONTENT_DESCRIPTION = "Content-Description";

    /** Content ID的header名。 */
    public final static String CONTENT_ID = "Content-ID";

    /** 默认的传输编码。 */
    public final static String DEFAULT_TRANSFER_ENCODING = "8bit";

    /** 设置在ContentType中的charset名称。 */
    public final static String CONTENT_TYPE_CHARSET = "charset";

    /** 纯文本的content type。 */
    public final static String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    /** 纯HTML的content type。 */
    public final static String CONTENT_TYPE_TEXT_HTML = "text/html";

    /** 附件message的content type。 */
    public final static String CONTENT_TYPE_MESSAGE = "message/rfc822";

    /** 内嵌资源的HTML的content type子类型。 */
    public final static String CONTENT_TYPE_MULTIPART_SUBTYPE_RELATED = "related";

    /** 选择型multipart的content type子类型。 */
    public final static String CONTENT_TYPE_MULTIPART_SUBTYPE_ALTERNATIVE = "alternative";

    /** 混合型multipart的content type子类型。 */
    public final static String CONTENT_TYPE_MULTIPART_SUBTYPE_MIXED = "mixed";

    /** 默认的mail-store协议。 */
    public final static String DEFAULT_MAIL_STORE_PROTOCOL = "pop3";

    /** 默认的mail-transport协议。 */
    public final static String DEFAULT_MAIL_TRANSPORT_PROTOCOL = "smtp";

    /** 设定需要验证的smtp的属性名称。 */
    public final static String SMTP_AUTH = "mail.smtp.auth";

    /** 默认的mail-store的folder名。 */
    public final static String DEFAULT_MAIL_STORE_FOLDER = "INBOX";

    /** 默认的store或transport的ID。 */
    public final static String DEFAULT_MAIL_SESSION_ID = "_DEFAULT_";

    /** 是否打开debug信息。 */
    public final static String MAIL_DEBUG = "mail.debug";
}
