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
package com.alibaba.citrus.service.mail.builder;

import static com.alibaba.citrus.service.mail.MailConstant.*;
import static com.alibaba.citrus.service.mail.builder.MailAddressType.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.alibaba.citrus.service.mail.MailService;
import com.alibaba.citrus.service.mail.util.MailUtil;
import com.alibaba.citrus.util.Assert.ExceptionType;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 创建一个javamail对象的工具类。
 * <p>
 * <code>MailBuilder</code>对象是有状态的，不能被多个线程同时使用。
 * </p>
 * 
 * @author Michael Zhou
 */
public class MailBuilder implements Cloneable {
    private MailService mailService;
    private final Set<InternetAddress>[] addresses;
    private final Map<String, Object> attributes;
    private String id;
    private String charset;
    private String subject;
    private Date sentDate;
    private MailContent content;
    private transient Session session;

    @SuppressWarnings("unchecked")
    public MailBuilder() {
        this.addresses = (Set<InternetAddress>[]) new Set<?>[MailAddressType.values().length];
        this.attributes = createHashMap();
    }

    /**
     * 深度复制一个mail builder。
     */
    @Override
    public MailBuilder clone() {
        MailBuilder copy = new MailBuilder();

        copy.mailService = mailService;
        copy.id = id;
        copy.charset = charset;

        for (int i = 0; i < addresses.length; i++) {
            Set<InternetAddress> addrSet = addresses[i];

            if (addrSet != null && !addrSet.isEmpty()) {
                copy.addresses[i] = createLinkedHashSet(addrSet);
            }
        }

        copy.attributes.putAll(attributes);
        copy.subject = subject;
        copy.sentDate = sentDate;

        if (content != null) {
            copy.setContent(content.clone());
        }

        return copy;
    }

    /**
     * 取得此mail builder所属的service。
     */
    public MailService getMailService() {
        return mailService;
    }

    /**
     * 设置此mail builder所属的service。
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * 取得mail builder的ID。
     */
    public String getId() {
        return id;
    }

    /**
     * 设置mail builder的ID。
     */
    public void setId(String id) {
        this.id = trimToNull(id);
    }

    /**
     * 取得当前mail builder的session。
     * <p>
     * 只有在build阶段方可得到此值。
     * </p>
     */
    public Session getSession() {
        return assertNotNull(session, ExceptionType.ILLEGAL_STATE, "Not in build time");
    }

    /**
     * 取得邮件的主题。
     */
    public String getSubject() {
        return subject;
    }

    /**
     * 设置邮件的主题。
     */
    public void setSubject(String subject) {
        this.subject = trimToNull(subject);
    }

    /**
     * 取得生成邮件时使用的编码字符集。如果未指定，则返回默认字符集<code>UTF-8</code>。
     */
    public String getCharacterEncoding() {
        return getDefaultCharsetIfNull(charset);
    }

    /**
     * 设置生成邮件时使用的编码字符集。
     */
    public void setCharacterEncoding(String javaCharset) {
        javaCharset = trimToNull(javaCharset);

        String oldCharset = getCharacterEncoding();
        String newCharset = getDefaultCharsetIfNull(javaCharset);

        if (!oldCharset.equals(newCharset)) {
            this.charset = javaCharset;
            updateAddressCharset(newCharset);
        }
    }

    private void updateAddressCharset(String newCharset) {
        String mimeCharset = MimeUtility.mimeCharset(newCharset);

        for (Set<InternetAddress> addrSet : addresses) {
            if (addrSet != null) {
                for (InternetAddress addr : addrSet) {
                    try {
                        addr.setPersonal(addr.getPersonal(), mimeCharset);
                    } catch (UnsupportedEncodingException e) {
                        invalidCharset(newCharset, e);
                    }
                }
            }
        }
    }

    private static String getDefaultCharsetIfNull(String charset) {
        return defaultIfNull(charset, DEFAULT_CHARSET);
    }

    /**
     * 取得指定类型的所有地址。如果未设置该类型的地址，则返回空数组。
     */
    public InternetAddress[] getAddresses(MailAddressType addrType) {
        Set<InternetAddress> addrSet = getAddressSet(addrType, false);

        if (addrSet == null) {
            return new InternetAddress[0];
        } else {
            return addrSet.toArray(new InternetAddress[addrSet.size()]);
        }
    }

    /**
     * 添加邮件地址。
     */
    public void addAddress(MailAddressType addrType, String addrList) throws InvalidAddressException {
        if (isEmpty(addrList)) {
            return;
        }

        InternetAddress[] addrs;
        String javaCharset = getCharacterEncoding();

        try {
            addrs = MailUtil.parse(addrList, javaCharset);
        } catch (AddressException e) {
            throw new InvalidAddressException("Invalid mail address: " + addrList, e);
        } catch (UnsupportedEncodingException e) {
            invalidCharset(javaCharset, e);
            return;
        }

        Set<InternetAddress> addrSet = getAddressSet(addrType, true);

        for (InternetAddress addr : addrs) {
            addrSet.add(addr);
        }
    }

    /**
     * 设置邮件地址。
     * <p>
     * 和<code>addAddress</code>方法不同，该方法清除原有的地址。
     * </p>
     */
    public void setAddress(MailAddressType addrType, String addr) throws InvalidAddressException {
        getAddressSet(addrType, true).clear();
        addAddress(addrType, addr);
    }

    /**
     * 取得邮件内容。
     */
    public MailContent getContent() {
        return content;
    }

    /**
     * 取得指定ID的content。
     * <p>
     * 如果content未指定ID，则无法找到。
     * </p>
     * <p>
     * 如果和ID对应的content实例未找到，则返回<code>null</code>。
     * </p>
     */
    public MailContent getContent(String id) {
        return findContent(trimToNull(id), content);
    }

    /**
     * 设置邮件内容。
     */
    public void setContent(MailContent content) {
        MailContent oldContent = this.content;

        this.content = content;
        this.content.setMailBuilder(this);

        if (oldContent != null) {
            oldContent.setMailBuilder(null);
        }
    }

    /**
     * 取得发信日期，如果未设置，则取得当前时间。
     */
    public Date getSentDate() {
        if (sentDate == null) {
            sentDate = new Date();
        }

        return sentDate;
    }

    /**
     * 设置发信日期。
     */
    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    /**
     * 取得绑定的对象。
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * 取得attributes的key集合。
     */
    public Set<String> getAttributeKeys() {
        return attributes.keySet();
    }

    /**
     * 绑定指定的对象。
     */
    public void setAttribute(String key, Object object) {
        if (object == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, object);
        }
    }

    /**
     * 批量绑定对象。
     */
    public void setAttributes(Map<String, Object> attrs) {
        attributes.putAll(attrs);
    }

    /**
     * 转换成javamail邮件对象。
     */
    public MimeMessage getMessage(Session session) throws MailBuilderException {
        this.session = assertNotNull(session, "session");

        MimeMessage message = new MimeMessage(session);

        try {
            if (content != null) {
                content.render(message);
            } else {
                message.setContent(EMPTY_STRING, "text/plain");
            }
        } catch (MessagingException e) {
            throw new MailBuilderException("Failed to render content", e);
        }

        try {
            // from addresses
            message.addFrom(getAddresses(FROM));

            // recipients addresses
            message.setRecipients(Message.RecipientType.TO, getAddresses(TO));
            message.setRecipients(Message.RecipientType.CC, getAddresses(CC));
            message.setRecipients(Message.RecipientType.BCC, getAddresses(BCC));

            // reply to addresses
            message.setReplyTo(getAddresses(REPLY_TO));

            // subject
            message.setSubject(MailUtil.encodeHeader(getSubject(), getCharacterEncoding()));

            // sent date
            message.setSentDate(getSentDate());
        } catch (MessagingException e) {
            throw new MailBuilderException("Failed to create javamail message", e);
        } catch (UnsupportedEncodingException e) {
            invalidCharset(getCharacterEncoding(), e);
            return null;
        }

        return message;
    }

    /**
     * 将javamail邮件对象转换成文本形式，其格式为标准的<code>.eml</code>格式。
     */
    public String getMessageAsString(Session session) throws MailBuilderException {
        Message message = getMessage(session);

        try {
            return MailUtil.toString(message, getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            invalidCharset(getCharacterEncoding(), e);
            return null;
        } catch (MessagingException e) {
            throw new MailBuilderException(e);
        }
    }

    /**
     * 将javamail邮件对象输出到指定流中。
     */
    public void writeTo(OutputStream ostream, Session session) throws MailBuilderException, IOException {
        Message message = getMessage(session);

        try {
            message.writeTo(ostream);
        } catch (MessagingException e) {
            throw new MailBuilderException(e);
        }
    }

    /**
     * 取得指定类型的邮件地址集合。
     * <p>
     * 如果该类型地址不存在，返回<code>null</code>。假如<code>create==true</code>，则会自动创建集合。
     * </p>
     */
    private Set<InternetAddress> getAddressSet(MailAddressType addrType, boolean create) {
        assertNotNull(addrType, "addressType");

        int index = addrType.ordinal();
        assertTrue(index < addresses.length, "internal state inconsistent");

        Set<InternetAddress> addrSet = addresses[index];

        if (addrSet == null && create) {
            addrSet = createLinkedHashSet();
            addresses[index] = addrSet;
        }

        return addrSet;
    }

    /**
     * 递归查找指定ID的content。
     */
    private MailContent findContent(String id, MailContent content) {
        MailContent result = null;

        if (id != null && content != null) {
            String contentId = content.getId();

            if (id.equals(contentId)) {
                result = content;
            } else if (content instanceof Multipart) {
                for (MailContent subcontent : ((Multipart) content).getContents()) {
                    result = findContent(id, subcontent);

                    if (result != null) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    private void invalidCharset(String charset, UnsupportedEncodingException e) {
        StringBuilder message = new StringBuilder();
        String id = getId();

        message.append("Invalid charset \"").append(charset).append("\"");

        if (!isEmpty(id)) {
            message.append(" specified at mail (id=\"").append(id).append("\")");
        }

        throw new MailBuilderException(message.toString(), e);
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        if (getId() != null) {
            mb.append("id", getId());
        }

        mb.append("subject", getSubject());
        mb.append("charset", getCharacterEncoding());
        mb.append("sentDate", sentDate); // don't use getSentDate()

        for (MailAddressType addrType : MailAddressType.values()) {
            InternetAddress[] addrs = getAddresses(addrType);

            if (isEmptyArray(addrs)) {
                mb.append(addrType.name(), EMPTY_STRING);
            } else if (addrs.length == 1) {
                mb.append(addrType.name(), addrs[0]);
            } else {
                mb.append(addrType.name(), addrs);
            }
        }

        mb.append("attributes", attributes);
        mb.append("content", content);

        return new ToStringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }
}
