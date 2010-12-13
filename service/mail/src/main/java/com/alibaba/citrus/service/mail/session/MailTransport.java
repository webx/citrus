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
package com.alibaba.citrus.service.mail.session;

import static com.alibaba.citrus.service.mail.MailConstant.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Transport;

import com.alibaba.citrus.service.mail.MailException;
import com.alibaba.citrus.service.mail.MailNotFoundException;
import com.alibaba.citrus.service.mail.MailService;
import com.alibaba.citrus.service.mail.MailStoreNotFoundException;
import com.alibaba.citrus.service.mail.builder.MailBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 用来发送e-mail的类。
 * <p>
 * 该类被设计成“有状态的”，也就是说不能被多个线程共享。
 * </p>
 * 
 * @author Michael Zhou
 */
public class MailTransport extends MailSession {
    private String transportProtocol;
    private String popBeforeSmtpId;
    private MailTransportHandler handler;
    private Transport transport;

    /**
     * 创建一个mail transport。
     */
    public MailTransport() {
    }

    /**
     * 复制一个mail transport。
     */
    public MailTransport(MailTransport transport, Properties overrideProps) {
        super(transport, overrideProps);
        this.transportProtocol = transport.transportProtocol;
        this.popBeforeSmtpId = transport.popBeforeSmtpId;
    }

    /**
     * 取得mail transport的协议。
     */
    public String getProtocol() {
        return defaultIfNull(transportProtocol, DEFAULT_MAIL_TRANSPORT_PROTOCOL);
    }

    /**
     * 设置mail transport的协议。
     */
    public void setProtocol(String protocol) {
        this.transportProtocol = trimToNull(protocol);
    }

    /**
     * 取得pop before smtp的store ID。
     */
    public String getPopBeforeSmtp() {
        return popBeforeSmtpId;
    }

    /**
     * 设置pop before smtp的store ID。
     */
    public void setPopBeforeSmtp(String popBeforeSmtpId) {
        this.popBeforeSmtpId = trimToNull(popBeforeSmtpId);
    }

    /**
     * 取得session properties。
     */
    @Override
    protected Properties getSessionProperties() {
        setProperty(SMTP_AUTH, String.valueOf(useAuth()), "false");
        return super.getSessionProperties();
    }

    /**
     * 取得发送e-mail的处理程序。
     */
    public MailTransportHandler getHandler() {
        return handler;
    }

    /**
     * 设置发送e-mail的处理程序。
     */
    public void setHandler(MailTransportHandler newHandler) {
        if (this.handler != null && this.transport != null) {
            this.transport.removeTransportListener(this.handler);
        }

        if (newHandler != null) {
            this.handler = newHandler;
        }

        if (this.handler != null && this.transport != null) {
            this.transport.addTransportListener(this.handler);
        }
    }

    /**
     * 判断是否已经连接上。
     */
    @Override
    public boolean isConnected() {
        return transport != null && transport.isConnected();
    }

    /**
     * 连接mail服务器。
     */
    @Override
    public void connect() throws MailException {
        if (!isConnected()) {
            try {
                transport = getSession().getTransport(getProtocol());
                setHandler(null);
                connectPopBeforeSmtp();
                transport.connect(getHost(), getPort(), getUser(), getPassword());

                if (getHandler() != null) {
                    getHandler().prepareConnection(transport);
                }
            } catch (NoSuchProviderException e) {
                transport = null;
                throw new MailException("Could not find a provider of " + getProtocol() + " protocol", e);
            } catch (MessagingException me) {
                transport = null;
                throw new MailException("Could not connect to the transport", me);
            }
        }
    }

    /**
     * 关闭mail服务器的连接。
     */
    @Override
    public void close() throws MailException {
        if (transport != null) {
            try {
                transport.close();
            } catch (MessagingException e) {
                // ignore
            } finally {
                transport = null;
            }
        }
    }

    /**
     * 发送一个email。
     */
    public void send(String mailId) throws MailException {
        send(mailId, null);
    }

    /**
     * 发送一个email。
     */
    public void send(String mailId, MailTransportHandler handler) throws MailException {
        MailService service = getMailService();

        if (service == null) {
            throw new MailNotFoundException("Could not find mail \"" + mailId + "\": mail service is not set");
        }

        MailBuilder builder = service.getMailBuilder(mailId);

        send(builder, handler);
    }

    /**
     * 发送一个email。
     */
    public void send(MailBuilder builder) throws MailException {
        send(builder, null);
    }

    /**
     * 发送一个email。
     */
    public void send(MailBuilder builder, MailTransportHandler handler) throws MailException {
        setHandler(handler);

        if (getHandler() != null) {
            getHandler().prepareMessage(builder);
        }

        send(builder.getMessage(getSession()), getHandler());
    }

    /**
     * 发送一个email。
     */
    public void send(Message message) throws MailException {
        send(message, null);
    }

    /**
     * 发送一个email。
     */
    public void send(Message message, MailTransportHandler handler) throws MailException {
        boolean autoClose = false;

        setHandler(handler);

        if (!isConnected()) {
            autoClose = true;
            connect();
        }

        try {
            message.setSentDate(new Date());

            if (getHandler() != null) {
                getHandler().processMessage(message);
            }

            message.saveChanges();

            Address[] recipients = message.getAllRecipients();

            if (isEmptyArray(recipients)) {
                throw new MailException("No recipient was specified in mail");
            }

            transport.sendMessage(message, recipients);
        } catch (MessagingException me) {
            throw new MailException("Could not send message", me);
        } finally {
            if (autoClose) {
                close();
            }
        }
    }

    /**
     * 有一些服务器要求在连smtp之前，到pop3服务器上去验证。
     */
    private void connectPopBeforeSmtp() throws MailException {
        if (popBeforeSmtpId != null) {
            MailService service = getMailService();

            if (service == null) {
                throw new MailStoreNotFoundException("Could not find mail store \"" + popBeforeSmtpId
                        + "\": mail service is not set");
            }

            MailStore popBeforeSmtpStore = assertNotNull(service.getMailStore(popBeforeSmtpId),
                    "popBeforeSmtpStore: %s", popBeforeSmtpId);

            try {
                popBeforeSmtpStore.connect();
            } finally {
                popBeforeSmtpStore.close();
            }
        }
    }

    @Override
    protected void toString(MapBuilder mb) {
        mb.append("protocol", getProtocol());
        mb.append("popBeforeSmtp", getPopBeforeSmtp());
    }
}
