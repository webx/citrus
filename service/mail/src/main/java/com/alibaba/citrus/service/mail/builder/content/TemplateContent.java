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
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.util.Set;

import com.alibaba.citrus.service.mail.builder.MailBuilder;
import com.alibaba.citrus.service.mail.builder.MailBuilderException;
import com.alibaba.citrus.service.pull.PullContext;
import com.alibaba.citrus.service.pull.PullService;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.service.template.TemplateService;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 从模板生成的内容。
 * 
 * @author Michael Zhou
 */
public abstract class TemplateContent extends AbstractContent {
    private TemplateService templateService;
    private PullService pullService;
    private String templateName;
    private String contentType;
    private transient TemplateContext templateContext;

    /**
     * 取得template服务。
     */
    public TemplateService getTemplateService() {
        return assertNotNull(getService(TemplateService.class, "templateService", templateService), "templateService");
    }

    /**
     * 设置template服务。
     */
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * 取得pull服务。
     */
    public PullService getPullService() {
        return getService(PullService.class, "pullService", pullService);
    }

    /**
     * 设置pull服务。
     */
    public void setPullService(PullService pullService) {
        this.pullService = pullService;
    }

    /**
     * 取得模板的名称。
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * 设置模板的名称。
     */
    public void setTemplate(String templateName) {
        this.templateName = trimToNull(templateName);
    }

    /**
     * 取得文本的content type。
     */
    public String getContentType() {
        return defaultIfNull(contentType, getDefaultContentType());
    }

    protected String getDefaultContentType() {
        return CONTENT_TYPE_TEXT_PLAIN;
    }

    /**
     * 设置文本的content type。
     */
    public void setContentType(String contentType) {
        this.contentType = trimToNull(contentType);
    }

    /**
     * 渲染模板。
     */
    protected final String renderTemplate() throws MailBuilderException {
        try {
            return getTemplateService().getText(templateName, getTemplateContext());
        } catch (TemplateException e) {
            throw new MailBuilderException("Failed to render template: " + templateName, e);
        } catch (IOException e) {
            throw new MailBuilderException("Failed to render template: " + templateName, e);
        }
    }

    /**
     * 取得template context。
     */
    private TemplateContext getTemplateContext() {
        if (templateContext == null) {
            PullService pullService = getPullService();
            PullContext pullContext = null;

            if (pullService != null) {
                pullContext = pullService.getContext();
            }

            templateContext = new TemplateContextAdapter(getMailBuilder(), pullContext);

            populateTemplateContext(templateContext);
        }

        return templateContext;
    }

    /**
     * 给子类一个机会来初始化context。
     */
    protected void populateTemplateContext(TemplateContext templateContext) {
    }

    /**
     * 深度复制一个content。
     */
    @Override
    protected void copyTo(AbstractContent copy) {
        TemplateContent copyContent = (TemplateContent) copy;

        copyContent.templateService = templateService;
        copyContent.pullService = pullService;
        copyContent.templateName = templateName;
        copyContent.contentType = contentType;
    }

    @Override
    protected void toString(MapBuilder mb) {
        mb.append("contentType", getContentType());
        mb.append("templateName", templateName);
    }

    /**
     * 将mail builder适配到<code>TemplateContext</code>。
     */
    private static class TemplateContextAdapter implements TemplateContext {
        private final MailBuilder builder;
        private final PullContext pullContext;

        public TemplateContextAdapter(MailBuilder builder, PullContext pullContext) {
            this.builder = assertNotNull(builder, "mailBuilder");
            this.pullContext = pullContext;
        }

        public boolean containsKey(String key) {
            return builder.getAttribute(key) != null || pull(key) != null;
        }

        public Object get(String key) {
            Object object = builder.getAttribute(key);

            if (object == null) {
                return pull(key);
            } else {
                return object;
            }
        }

        public Set<String> keySet() {
            if (pullContext == null) {
                return builder.getAttributeKeys();
            } else {
                Set<String> keys = createHashSet(builder.getAttributeKeys());
                keys.addAll(pullContext.getToolNames());
                return keys;
            }
        }

        public void put(String key, Object value) {
            builder.setAttribute(key, value);
        }

        public void remove(String key) {
            builder.setAttribute(key, null);
        }

        private Object pull(String key) {
            if (pullContext != null) {
                return pullContext.pull(key);
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder().setSortKeys(true).setPrintCount(true);

            for (String key : builder.getAttributeKeys()) {
                mb.append(key, builder.getAttribute(key));
            }

            return mb.toString();
        }
    }
}
