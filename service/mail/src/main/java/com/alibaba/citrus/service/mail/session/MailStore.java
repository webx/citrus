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
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Store;

import com.alibaba.citrus.service.mail.MailException;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 发送e-mail的类。
 * <p>
 * 该类被设计成“有状态的”，也就是说不能被多个线程共享。
 * </p>
 * 
 * @author Michael Zhou
 */
public class MailStore extends MailSession {
    private String storeProtocol;
    private String storeFolder;
    private MailStoreHandler handler;
    private Store store;

    /**
     * 创建一个mail store。
     */
    public MailStore() {
    }

    /**
     * 复制一个mail store。
     */
    public MailStore(MailStore store, Properties overrideProps) {
        super(store, overrideProps);
        this.storeProtocol = store.storeProtocol;
        this.storeFolder = store.storeFolder;
    }

    /**
     * 取得mail store的协议。
     */
    public String getProtocol() {
        return defaultIfNull(storeProtocol, DEFAULT_MAIL_STORE_PROTOCOL);
    }

    /**
     * 设置mail store的协议。
     */
    public void setProtocol(String protocol) {
        this.storeProtocol = trimToNull(protocol);
    }

    /**
     * 取得mail store的文件夹。
     */
    public String getFolder() {
        return defaultIfNull(storeFolder, DEFAULT_MAIL_STORE_FOLDER);
    }

    /**
     * 设置mail store的文件夹。
     */
    public void setFolder(String folder) {
        this.storeFolder = trimToNull(folder);
    }

    /**
     * 取得接收e-mail的处理程序。
     */
    public MailStoreHandler getHandler() {
        return handler;
    }

    /**
     * 设置接收e-mail的处理程序。
     */
    public void setHandler(MailStoreHandler newHandler) {
        if (newHandler != null) {
            this.handler = newHandler;
        }
    }

    /**
     * 判断是否已经连接上。
     */
    @Override
    public boolean isConnected() {
        return store != null && store.isConnected();
    }

    /**
     * 连接mail服务器。
     */
    @Override
    public void connect() throws MailException {
        if (!isConnected()) {
            try {
                store = getSession().getStore(getProtocol());
                store.connect(getHost(), getPort(), getUser(), getPassword());

                if (getHandler() != null) {
                    getHandler().prepareConnection(store);
                }
            } catch (NoSuchProviderException e) {
                store = null;
                throw new MailException("Could not find a provider of " + getProtocol() + " protocol", e);
            } catch (MessagingException me) {
                store = null;
                throw new MailException("Could not connect to the store", me);
            }
        }
    }

    /**
     * 关闭mail服务器的连接。
     */
    @Override
    public void close() {
        if (store != null) {
            try {
                store.close();
            } catch (MessagingException e) {
            } finally {
                store = null;
            }
        }
    }

    /**
     * 接收邮件。
     */
    public void receive() throws MailException {
        receive(null);
    }

    /**
     * 接收邮件。
     */
    public void receive(MailStoreHandler handler) throws MailException {
        Folder inbox = null;
        boolean autoClose = false;

        setHandler(handler);

        if (!isConnected()) {
            autoClose = true;
            connect();
        }

        try {
            inbox = store.getFolder(getFolder());
            inbox.open(Folder.READ_WRITE);

            int messageCount = inbox.getMessageCount();

            if (getHandler() != null) {
                int max = getHandler().getMessageCount(messageCount);

                if (max >= 0 && max <= messageCount) {
                    messageCount = max;
                }
            }

            for (int i = 1; i <= messageCount; i++) {
                Message message = inbox.getMessage(i);
                boolean deleteMessage = false;

                if (getHandler() != null) {
                    deleteMessage = getHandler().processMessage(message);
                }

                message.setFlag(Flags.Flag.DELETED, deleteMessage);
            }
        } catch (MessagingException me) {
            throw new MailException("Could not receive messages", me);
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(true);
                }
            } catch (MessagingException e) {
            }

            if (autoClose) {
                close();
            }
        }
    }

    @Override
    protected void toString(MapBuilder mb) {
        mb.append("protocol", getProtocol());
        mb.append("folder", getFolder());
    }
}
