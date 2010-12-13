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

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.service.mail.MailService;
import com.alibaba.citrus.service.mail.builder.MailBuilder;
import com.alibaba.citrus.service.mail.builder.MailContent;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 一个<code>MailContent</code>的基类。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractContent implements MailContent {
    private String id;
    private MailBuilder builder;
    private MailContent parentContent;

    /**
     * 取得content的唯一ID，此ID在整个mail builder所包含的content中是唯一的。
     */
    public String getId() {
        return id;
    }

    /**
     * 设置content的唯一ID，此ID在整个mail builder所包含的content中是唯一的。
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 取得此content所属的mail builder。假如不存在，则抛<code>IllegalArgumentException</code>
     * 异常。
     */
    public MailBuilder getMailBuilder() {
        return getMailBuilder(true);
    }

    /**
     * 取得此content所属的mail builder。
     */
    protected final MailBuilder getMailBuilder(boolean required) {
        if (builder != null) {
            return builder;
        }

        if (parentContent != null) {
            return parentContent.getMailBuilder();
        }

        if (required) {
            throw new IllegalArgumentException("no mailBuilder");
        }

        return null;
    }

    /**
     * 设置mail builder。
     */
    public void setMailBuilder(MailBuilder builder) {
        this.builder = builder;
    }

    /**
     * 取得包容此内容的父内容。
     */
    public MailContent getParentContent() {
        return parentContent;
    }

    /**
     * 设置包容此内容的父内容。
     */
    public void setParentContent(MailContent parentContent) {
        this.parentContent = parentContent;
    }

    /**
     * 深度复制一个content。
     */
    @Override
    public final AbstractContent clone() {
        String className = getClass().getSimpleName();

        // new instance
        AbstractContent copy = assertNotNull(newInstance(), "%s.newInstance() returned null", className);
        assertTrue(copy.getClass().equals(getClass()), "%s.newInstance() returned an object of wrong class", className);

        // copy to new instance
        copyTo(copy);
        copy.id = id;
        return copy;
    }

    /**
     * 创建一个同类型的content。
     */
    protected abstract AbstractContent newInstance();

    /**
     * 深度复制一个content。
     */
    protected void copyTo(AbstractContent copy) {
    }

    /**
     * 用来取得指定类型的service的辅助方法。假如<code>defaultInstance</code>非空，则直接返回之，否则调用
     * <code>getMailBuilder().getMailService().getService()</code>。
     */
    protected final <T> T getService(Class<T> serviceType, String defaultId, T defaultInstance) {
        if (defaultInstance != null) {
            return defaultInstance;
        }

        MailBuilder builder = getMailBuilder(false);

        if (builder != null) {
            MailService mailService = builder.getMailService();

            if (mailService != null) {
                return mailService.getService(serviceType, defaultId);
            }
        }

        return null;
    }

    @Override
    public final String toString() {
        ToStringBuilder buf = new ToStringBuilder();

        buf.append(getClass().getSimpleName());
        toString(buf);

        return buf.toString();
    }

    protected void toString(ToStringBuilder buf) {
        MapBuilder mb = new MapBuilder();
        toString(mb);
        buf.append(mb);
    }

    protected void toString(MapBuilder mb) {
    }
}
