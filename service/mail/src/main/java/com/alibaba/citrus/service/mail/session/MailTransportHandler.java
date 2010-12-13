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

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.event.TransportListener;

import com.alibaba.citrus.service.mail.MailException;
import com.alibaba.citrus.service.mail.builder.MailBuilder;

/**
 * 发送e-mail的处理器。
 * <p>
 * 需要注意的是，<code>TransportListener</code>是异步的。由于网络的时延，
 * <code>TransportListener</code>中的方法不一定会被立即调用。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface MailTransportHandler extends TransportListener {
    /**
     * 预处理连接。
     */
    void prepareConnection(Transport transport) throws MailException, MessagingException;

    /**
     * 预处理邮件。
     */
    void prepareMessage(MailBuilder builder) throws MailException;

    /**
     * 处理邮件。
     */
    void processMessage(Message message) throws MailException, MessagingException;
}
