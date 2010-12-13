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

import javax.mail.MessagingException;
import javax.mail.Part;

/**
 * 代表一个javamail的内容。
 * 
 * @author Michael Zhou
 */
public interface MailContent extends Cloneable {
    /**
     * 取得content的唯一ID，此ID在整个mail builder所包含的content中是唯一的。
     */
    String getId();

    /**
     * 深度复制一个content。
     */
    MailContent clone();

    /**
     * 取得content所属的mail builder。
     */
    MailBuilder getMailBuilder();

    /**
     * 设置content所属的mail builder。
     */
    void setMailBuilder(MailBuilder builder);

    /**
     * 设置包容此内容的父内容。
     */
    void setParentContent(MailContent parentContent);

    /**
     * 渲染邮件内容。
     */
    void render(Part mailPart) throws MessagingException;
}
