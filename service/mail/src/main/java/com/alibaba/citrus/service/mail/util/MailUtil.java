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

import static com.alibaba.citrus.service.mail.MailConstant.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static javax.mail.internet.MimeUtility.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import com.alibaba.citrus.util.io.ByteArray;
import com.alibaba.citrus.util.io.ByteArrayOutputStream;

/**
 * 有关javamail的工具类。
 * 
 * @author Michael Zhou
 */
public class MailUtil {
    /**
     * 如果指定<code>javaCharset</code>为空白，则返回默认charset，否则返回原值。
     */
    public static String getJavaCharset(String javaCharset) {
        javaCharset = trimToNull(javaCharset);
        return defaultIfNull(javaCharset, DEFAULT_CHARSET);
    }

    /**
     * 将javamail邮件对象转换成文本形式，其格式为标准的<code>.eml</code>格式。
     */
    public static String toString(Message message) throws MessagingException {
        try {
            return toString(message, null);
        } catch (UnsupportedEncodingException e) {
            unexpectedException(e);
            return null;
        }
    }

    /**
     * 将javamail邮件对象转换成文本形式，其格式为标准的<code>.eml</code>格式。
     */
    public static String toString(Message message, String javaCharset) throws MessagingException,
            UnsupportedEncodingException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

        try {
            message.writeTo(ostream);
        } catch (IOException e) {
            unexpectedException(e);
        } finally {
            ostream.close();
        }

        ByteArray bytes = ostream.toByteArray();

        javaCharset = getJavaCharset(javaCharset);

        return new String(bytes.getRawBytes(), bytes.getOffset(), bytes.getLength(), javaCharset);
    }

    /**
     * 根据RFC822/MIME，编码邮件header，使之符合RFC2047。
     * <p>
     * 如果大部分字符为ASCII字符，则以<code>"Q"</code>方式编码，否则以<code>"B"</code>方式编码。
     * </p>
     * <p>
     * 如果<code>javaCharset</code>（有别于<code>mimeCharset</code>）为空，则取默认值；如果
     * <code>header</code>值为空，则取空白。
     * </p>
     */
    public static String encodeHeader(String header, String javaCharset) throws UnsupportedEncodingException {
        return encodeHeader(header, javaCharset, null);
    }

    /**
     * 根据RFC822/MIME，编码邮件header，使之符合RFC2047。
     * <p>
     * 如果<code>javaCharset</code>（有别于<code>mimeCharset</code>）为空，则取默认值；如果
     * <code>header</code>值为空，则取空白。
     * </p>
     * <p>
     * encoding编码方式，可以是<code>"B"</code>或<code>"Q"</code>，如果该值为 <code>null</code>
     * ，并且大部分字符为ASCII字符，则默认为<code>"Q"</code>，否则默认为 <code>"B"</code>。
     * </p>
     */
    public static String encodeHeader(String header, String javaCharset, String encoding)
            throws UnsupportedEncodingException {
        header = defaultIfNull(header, EMPTY_STRING);
        String mimeCharset = MimeUtility.mimeCharset(getJavaCharset(javaCharset));
        return MimeUtility.encodeText(header, mimeCharset, encoding);
    }

    /**
     * 解析一组以逗号或空格分隔的mail地址。
     * <p>
     * 支持格式：<code>"My Name" &lt;name@addr.com&gt;</code>，其中名称部分
     * <code>My Name</code>会以指定 <code>javaCharset</code>来编码。
     * </p>
     * <p>
     * 当以空格分隔时，仅支持简单mail地址格式，不包含名字。例如：<code>name1@addr.com name2@addr.com</code>
     * 。
     * </p>
     */
    public static InternetAddress[] parse(String addrList, String javaCharset) throws AddressException,
            UnsupportedEncodingException {
        return parse(addrList, javaCharset, false);
    }

    /**
     * 解析一组以逗号或空格分隔的mail地址。
     * <p>
     * 支持格式：<code>"My Name" &lt;name@addr.com&gt;</code>，其中名称部分
     * <code>My Name</code>会以指定 <code>javaCharset</code>来编码。
     * </p>
     * <p>
     * 当<code>strict==true</code>时，支持以空格分隔的简单mail地址格式，不包含名字。例如：
     * <code>name1@addr.com name2@addr.com</code> 。
     * </p>
     */
    public static InternetAddress[] parse(String addrList, String javaCharset, boolean strict) throws AddressException,
            UnsupportedEncodingException {
        InternetAddress[] addrs = InternetAddress.parse(defaultIfNull(addrList, EMPTY_STRING), strict);

        if (!isEmptyArray(addrs)) {
            String mimeCharset = MimeUtility.mimeCharset(getJavaCharset(javaCharset));

            for (InternetAddress addr : addrs) {
                addr.setPersonal(trimToNull(addr.getPersonal()), mimeCharset);
            }
        }

        return addrs;
    }

    /**
     * 取得<code>ContentType</code>对象。
     */
    public static ContentType getContentType(String contentType, String javaCharset) throws ParseException {
        assertNotNull(contentType, "contentType");

        ContentType contentTypeObject = new ContentType(contentType);

        javaCharset = trimToNull(javaCharset);

        if (javaCharset != null) {
            contentTypeObject.setParameter(CONTENT_TYPE_CHARSET, mimeCharset(javaCharset));
        }

        return contentTypeObject;
    }
}
