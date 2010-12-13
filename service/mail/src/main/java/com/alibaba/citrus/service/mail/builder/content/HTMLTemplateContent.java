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
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.alibaba.citrus.service.mail.builder.MailBuilderException;
import com.alibaba.citrus.service.mail.support.ResourceDataSource;
import com.alibaba.citrus.service.mail.util.MailUtil;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.util.FileUtil;
import com.alibaba.citrus.util.SystemUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 用模板生成的HTML的内容。
 * 
 * @author Michael Zhou
 */
public class HTMLTemplateContent extends TemplateContent implements ResourceLoaderAware {
    private ResourceLoader resourceLoader;
    private Map<String, String> inlineResourceMap = createHashMap();
    private Map<String, InlineResource> inlineResources = createHashMap();

    /**
     * 创建一个<code>HTMLTemplateContent</code>。
     */
    public HTMLTemplateContent() {
    }

    /**
     * 创建一个<code>HTMLTemplateContent</code>。
     */
    public HTMLTemplateContent(String templateName) {
        setTemplate(templateName);
    }

    /**
     * 创建一个<code>HTMLTemplateContent</code>。
     */
    public HTMLTemplateContent(String templateName, String contentType) {
        setTemplate(templateName);
        setContentType(contentType);
    }

    /**
     * 取得用来装载资源的<code>ResourceLoader</code>。
     */
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    /**
     * 设置用来装载资源的<code>ResourceLoader</code>。
     */
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 设置一组内置资源的tool。
     */
    public void setInlineResources(Map<String, String> resourceMap) {
        if (resourceMap != null) {
            inlineResourceMap.clear();

            for (Map.Entry<String, String> entry : resourceMap.entrySet()) {
                addInlineResource(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 添加一个在模板中代表内置资源的tool。其中，<code>id</code>为在模板中引用该tool的key，
     * <code>prefix</code>为该tool在查找资源时，自动加上指定前缀。
     */
    public void addInlineResource(String id, String prefix) {
        id = assertNotNull(trimToNull(id), "The ID of inline resource was not specified");
        prefix = assertNotNull(trimToNull(prefix), "The prefix of inline resource was not specified");

        assertTrue(!inlineResourceMap.containsKey(id), "Duplicated ID \"%s\" of inline resource", id);

        inlineResourceMap.put(id, prefix);
    }

    /**
     * 渲染邮件内容。
     */
    public void render(Part mailPart) throws MessagingException {
        // 设置内联资源的helper对象, 以便模板产生resource的引用.
        inlineResources.clear();

        // 渲染模板.
        String text = renderTemplate();

        // 设置message part.
        // 如果包括一个以上的内嵌资源, 则使用multipart/related类型, 否则使用text/html类型.
        try {
            if (inlineResources.isEmpty()) {
                renderHTMLContent(mailPart, text);
            } else {
                MimeMultipart multipartRelated = new MimeMultipart(CONTENT_TYPE_MULTIPART_SUBTYPE_RELATED);
                MimeBodyPart bodyPart = new MimeBodyPart();

                renderHTMLContent(bodyPart, text);
                multipartRelated.addBodyPart(bodyPart);

                // 取得所有内嵌的资源
                Set<String> fileNames = createHashSet();

                for (InlineResource inlineResource : inlineResources.values()) {
                    renderInlineResource(multipartRelated, inlineResource, fileNames);
                }

                mailPart.setContent(multipartRelated);
            }
        } finally {
            inlineResources.clear();
        }
    }

    /**
     * 渲染HTML内容。
     */
    private void renderHTMLContent(Part mailPart, String text) throws MessagingException {
        String contentType = getContentType();
        ContentType contentTypeObject = MailUtil.getContentType(contentType, getMailBuilder().getCharacterEncoding());

        mailPart.setContent(text, contentTypeObject.toString());
        mailPart.setHeader(CONTENT_TRANSFER_ENCODING, DEFAULT_TRANSFER_ENCODING);
    }

    private void renderInlineResource(Multipart multipart, InlineResource inlineResource, Set<String> fileNames)
            throws MessagingException {
        assertNotNull(resourceLoader, "no resourceLoader");

        String resourceName = inlineResource.getResourceName();
        Resource resource = resourceLoader.getResource(resourceName);

        if (!resource.exists()) {
            throw new MailBuilderException("Could not find resource \"" + resourceName + "\"");
        }

        DataSource ds;

        try {
            ds = new URLDataSource(resource.getURL());
        } catch (IOException e) {
            ds = new ResourceDataSource(resource);
        }

        MimeBodyPart bodyPart = new MimeBodyPart();

        bodyPart.setDataHandler(new DataHandler(ds));
        bodyPart.setHeader(CONTENT_ID, "<" + inlineResource.getContentId() + ">");
        bodyPart.setFileName(inlineResource.getUniqueFilename(fileNames));
        bodyPart.setDisposition("inline");

        multipart.addBodyPart(bodyPart);
    }

    /**
     * 组装templateContext中的内容。
     */
    @Override
    protected void populateTemplateContext(TemplateContext templateContext) {
        for (Map.Entry<String, String> entry : inlineResourceMap.entrySet()) {
            String key = entry.getKey();
            String prefix = entry.getValue();

            templateContext.put(key, new InlineResourceHelper(prefix));
        }
    }

    /**
     * 深度复制一个content。
     */
    @Override
    protected void copyTo(AbstractContent copy) {
        super.copyTo(copy);

        HTMLTemplateContent copyContent = (HTMLTemplateContent) copy;

        copyContent.resourceLoader = resourceLoader;
        copyContent.inlineResourceMap.clear();
        copyContent.inlineResourceMap.putAll(inlineResourceMap);
        copyContent.inlineResources.clear();
    }

    @Override
    protected HTMLTemplateContent newInstance() {
        return new HTMLTemplateContent();
    }

    @Override
    protected String getDefaultContentType() {
        return CONTENT_TYPE_TEXT_HTML;
    }

    @Override
    protected void toString(MapBuilder mb) {
        super.toString(mb);
        mb.append("inlineResources", inlineResourceMap);
    }

    /**
     * 记录在模板中使用到的所有内联资源的信息。
     */
    private static class InlineResource {
        private static MessageFormat formatter = new MessageFormat("{0,time,yyyyMMdd.HHmmss}.{1}@{2}");
        private static int count = 0;
        private static String hostname = SystemUtil.getHostInfo().getName();
        private String resourceName;
        private String contentId;
        private String filename;

        public InlineResource(String resourceName) {
            this.resourceName = resourceName;

            synchronized (getClass()) {
                count = (count + 1) % (2 << 20);
                this.contentId = formatter.format(new Object[] { new Date(), String.valueOf(count), hostname });
            }

            this.filename = getFileName(resourceName);
        }

        private static String getFileName(String name) {
            if (name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }

            return name.substring(name.lastIndexOf("/") + 1);
        }

        public String getResourceName() {
            return resourceName;
        }

        public String getContentId() {
            return contentId;
        }

        /**
         * 取得唯一的文件名。
         */
        public String getUniqueFilename(Set<String> fileNames) {
            String name = filename;
            int dotIndex = filename.lastIndexOf(".");

            for (int i = 1; fileNames.contains(name); i++) {
                if (dotIndex >= 0) {
                    name = filename.substring(0, dotIndex) + i + filename.substring(dotIndex);
                } else {
                    name = filename + i;
                }
            }

            fileNames.add(name);

            return name;
        }
    }

    /**
     * 在模板中嵌入内置的资源的辅助类。
     */
    public class InlineResourceHelper {
        private String prefix;

        public InlineResourceHelper(String prefix) {
            this.prefix = FileUtil.normalizeAbsolutePath(prefix + "/");
        }

        public String getURI(String path) {
            String resourceName = FileUtil.normalizeAbsolutePath(prefix + path);
            InlineResource inlineResource = inlineResources.get(resourceName);

            if (inlineResource == null) {
                inlineResource = new InlineResource(resourceName);
                inlineResources.put(resourceName, inlineResource);
            }

            return "cid:" + inlineResource.getContentId();
        }
    }
}
