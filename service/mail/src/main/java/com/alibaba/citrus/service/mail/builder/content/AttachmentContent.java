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
import static com.alibaba.citrus.util.Assert.ExceptionType.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.alibaba.citrus.service.mail.MailNotFoundException;
import com.alibaba.citrus.service.mail.MailService;
import com.alibaba.citrus.service.mail.builder.MailBuilder;
import com.alibaba.citrus.service.mail.builder.MailBuilderException;
import com.alibaba.citrus.service.mail.support.ResourceDataSource;
import com.alibaba.citrus.service.mail.util.MailUtil;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 代表一个邮件的附件。
 * 
 * @author Michael Zhou
 */
public class AttachmentContent extends AbstractContent implements ResourceLoaderAware {
    private ResourceLoader resourceLoader;
    private AttachmentSource source;
    private String fileName;

    /**
     * 创建一个附件。
     */
    public AttachmentContent() {
    }

    /**
     * 从URL中创建一个附件。
     */
    public AttachmentContent(URL attachmentURL) {
        setURL(attachmentURL);
    }

    /**
     * 从URL中创建一个指定文件名的附件。
     */
    public AttachmentContent(URL attachmentURL, String fileName) {
        setURL(attachmentURL);
        setFileName(fileName);
    }

    /**
     * 从文件中创建一个附件。
     */
    public AttachmentContent(File attachmentFile) {
        setFile(attachmentFile);
    }

    /**
     * 从文件中创建一个指定文件名的附件。
     */
    public AttachmentContent(File attachmentFile, String fileName) {
        setFile(attachmentFile);
        setFileName(fileName);
    }

    /**
     * 从<code>DataSource</code>中创建一个附件。
     */
    public AttachmentContent(DataSource dataSource) {
        setDataSource(dataSource);
    }

    /**
     * 从<code>DataSource</code>中创建一个指定文件名的附件。
     */
    public AttachmentContent(DataSource dataSource, String fileName) {
        setDataSource(dataSource);
        setFileName(fileName);
    }

    /**
     * 从<code>ResourceLoader</code>中装载一个资源并创建附件。
     */
    public AttachmentContent(String resourceName) {
        setResource(resourceName);
    }

    /**
     * 从<code>ResourceLoader</code>中装载一个资源并创建指定文件名的附件。
     */
    public AttachmentContent(String resourceName, String fileName) {
        setResource(resourceName);
        setFileName(fileName);
    }

    /**
     * 将另一封邮件作为附件。
     */
    public AttachmentContent(MailBuilder mailBuilder) {
        setMail(mailBuilder);
    }

    /**
     * 将另一封邮件作为附件。
     */
    public AttachmentContent(Message mail) {
        setMail(mail);
    }

    /**
     * 以URL的内容为附件。
     */
    public void setURL(URL attachmentURL) {
        setSource(new URLSource(this, attachmentURL));
    }

    /**
     * 以文件的内容为附件。
     */
    public void setFile(File attachmentFile) {
        setSource(new FileSource(this, attachmentFile));
    }

    /**
     * 以数据源的内容为附件。
     */
    public void setDataSource(DataSource dataSource) {
        setSource(new DsSource(this, dataSource));
    }

    /**
     * 以resource loader装载的资源内容为附件。
     */
    public void setResource(String resourceName) {
        setSource(new ResourceSource(this, resourceName));
        source.containingContent = this;
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
     * 设置附件的文件名。
     * <p>
     * 该文件名只是用于显示，没有实际的功效。
     * </p>
     */
    public void setFileName(String fileName) {
        this.fileName = trimToNull(fileName);
    }

    /**
     * 以另一个邮件体为附件。
     */
    public void setMail(MailBuilder mailBuilder) {
        setSource(new MailBuilderSource(this, mailBuilder));
    }

    /**
     * 以另一个邮件体为附件。
     */
    public void setMail(Message mail) {
        setSource(new MailSource(this, mail));
    }

    /**
     * 以mail service中取得的另一个邮件体为附件。
     */
    public void setMail(String attachmentRefId) {
        setSource(new MailRefSource(this, attachmentRefId));
    }

    private void setSource(AttachmentSource source) {
        assertNull(this.source, ILLEGAL_STATE, "Attachment source already set: %s", this.source);
        this.source = assertNotNull(source, "source");
    }

    /**
     * 渲染邮件内容。
     */
    public void render(Part mailPart) throws MessagingException {
        assertNotNull(source, "No attachment source was specified");
        source.render(mailPart, fileName);
    }

    /**
     * 创建一个同类型的content。
     */
    @Override
    protected AttachmentContent newInstance() {
        return new AttachmentContent();
    }

    /**
     * 深度复制一个content。
     */
    @Override
    protected void copyTo(AbstractContent copy) {
        AttachmentContent copyContent = (AttachmentContent) copy;

        if (source != null) {
            copyContent.source = source.clone();
            copyContent.source.containingContent = copyContent;
        }

        copyContent.resourceLoader = resourceLoader;
        copyContent.fileName = fileName;
    }

    @Override
    protected void toString(MapBuilder mb) {
        mb.append("source", source);
        mb.append("fileName", fileName);
    }

    /**
     * 代表一个附件的类型。
     */
    private abstract static class AttachmentSource implements Cloneable {
        protected transient AttachmentContent containingContent;

        public AttachmentSource(AttachmentContent containingContent) {
            this.containingContent = assertNotNull(containingContent, "containingContent");
        }

        public final MailBuilder getMailBuilder() {
            return containingContent.getMailBuilder();
        }

        @Override
        public final String toString() {
            return name() + "[" + desc() + "]";
        }

        protected abstract String name();

        protected abstract String desc();

        protected abstract void render(Part mailPart, String fileName);

        /**
         * 渲染data source。
         */
        protected final void render(Part mailPart, String fileName, DataSource source) throws MailBuilderException {
            try {
                mailPart.setDataHandler(new DataHandler(source));

                if (isEmpty(fileName)) {
                    throw new MailBuilderException("No fileName was specified with " + this);
                }

                // 确保fileName中不包含/和\
                fileName = fileName.replace('\\', '/');
                fileName = StringUtil.defaultIfEmpty(StringUtil.substringAfterLast(fileName, "/"), fileName);

                mailPart.setFileName(MailUtil.encodeHeader(fileName, getMailBuilder().getCharacterEncoding()));
            } catch (MessagingException e) {
                throw new MailBuilderException("Failed to add attachment to the mail", e);
            } catch (UnsupportedEncodingException e) {
                throw new MailBuilderException("Failed to add attachment to the mail", e);
            }
        }

        /**
         * 以另一个邮件作为附件。
         */
        protected final void render(Part mailPart, Message mail) throws MailBuilderException {
            try {
                mailPart.setContent(mail, CONTENT_TYPE_MESSAGE);

                String subject = mail.getSubject();

                if (!StringUtil.isEmpty(subject)) {
                    mailPart.setDescription(MailUtil.encodeHeader(subject, getMailBuilder().getCharacterEncoding()));
                }
            } catch (MessagingException e) {
                throw new MailBuilderException("Failed to add attachment to the mail", e);
            } catch (UnsupportedEncodingException e) {
                throw new MailBuilderException("Failed to add attachment to the mail", e);
            }
        }

        @Override
        public final AttachmentSource clone() {
            try {
                return (AttachmentSource) super.clone();
            } catch (CloneNotSupportedException e) {
                unexpectedException(e);
                return null;
            }
        }
    }

    private static class URLSource extends AttachmentSource {
        private URL url;

        public URLSource(AttachmentContent containingContent, URL url) {
            super(containingContent);
            this.url = assertNotNull(url, "url");
        }

        @Override
        protected String name() {
            return "URL";
        }

        @Override
        protected String desc() {
            return url.toExternalForm();
        }

        @Override
        protected void render(Part mailPart, String fileName) {
            DataSource ds = new URLDataSource(url);

            if (fileName == null) {
                fileName = url.getPath();
            }

            render(mailPart, fileName, ds);
        }
    }

    private static class FileSource extends AttachmentSource {
        private File file;

        public FileSource(AttachmentContent containingContent, File file) {
            super(containingContent);
            this.file = assertNotNull(file, "file");
        }

        @Override
        protected String name() {
            return "File";
        }

        @Override
        protected String desc() {
            return file.getAbsolutePath();
        }

        @Override
        protected void render(Part mailPart, String fileName) {
            DataSource ds = new FileDataSource(file);

            if (fileName == null) {
                fileName = file.getAbsolutePath();
            }

            render(mailPart, fileName, ds);
        }
    }

    private static class DsSource extends AttachmentSource {
        private DataSource dataSource;

        public DsSource(AttachmentContent containingContent, DataSource dataSource) {
            super(containingContent);
            this.dataSource = assertNotNull(dataSource, "dataSource");
        }

        @Override
        protected String name() {
            return "DataSource";
        }

        @Override
        protected String desc() {
            return dataSource.toString();
        }

        @Override
        protected void render(Part mailPart, String fileName) {
            if (fileName == null) {
                fileName = dataSource.getName();
            }

            render(mailPart, fileName, dataSource);
        }
    }

    private static class ResourceSource extends AttachmentSource {
        private String resourceName;

        public ResourceSource(AttachmentContent containingContent, String resourceName) {
            super(containingContent);
            this.resourceName = assertNotNull(trimToNull(resourceName), "resourceName");
        }

        @Override
        protected String name() {
            return "Resource";
        }

        @Override
        protected String desc() {
            return resourceName;
        }

        @Override
        protected void render(Part mailPart, String fileName) {
            ResourceLoader resourceLoader = containingContent.getResourceLoader();

            if (resourceLoader == null) {
                throw new MailBuilderException("Could not find resource \"" + resourceName
                        + "\": no resourceLoader specified");
            }

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

            if (fileName == null) {
                fileName = resourceName;
            }

            render(mailPart, fileName, ds);
        }
    }

    private static class MailBuilderSource extends AttachmentSource {
        private MailBuilder mailBuilder;

        public MailBuilderSource(AttachmentContent containingContent, MailBuilder mailBuilder) {
            super(containingContent);
            this.mailBuilder = assertNotNull(mailBuilder, "mailBuilder");
        }

        @Override
        protected String name() {
            return "MailBuilder";
        }

        @Override
        protected String desc() {
            return "id=" + mailBuilder.getId();
        }

        @Override
        protected void render(Part mailPart, String fileName) {
            Message mail = mailBuilder.getMessage(getMailBuilder().getSession());

            render(mailPart, mail);
        }
    }

    private static class MailSource extends AttachmentSource {
        private Message mail;

        public MailSource(AttachmentContent containingContent, Message mail) {
            super(containingContent);
            this.mail = assertNotNull(mail, "mail");
        }

        @Override
        protected String name() {
            return "Message";
        }

        @Override
        protected String desc() {
            return mail.getClass().getSimpleName();
        }

        @Override
        protected void render(Part mailPart, String fileName) {
            render(mailPart, mail);
        }
    }

    private static class MailRefSource extends AttachmentSource {
        private String mailRef;

        public MailRefSource(AttachmentContent containingContent, String mailRef) {
            super(containingContent);
            this.mailRef = assertNotNull(trimToNull(mailRef), "mailRef");
        }

        @Override
        protected String name() {
            return "MailRef";
        }

        @Override
        protected String desc() {
            return mailRef;
        }

        @Override
        protected void render(Part mailPart, String fileName) {
            MailService mailService = getMailBuilder().getMailService();

            if (mailService == null) {
                throw new MailBuilderException("Could not find mail \"" + mailRef + "\": no MailService");
            }

            MailBuilder mailBuilder;

            try {
                mailBuilder = mailService.getMailBuilder(mailRef);
            } catch (MailNotFoundException e) {
                throw new MailBuilderException("Could not find mail \"" + mailRef + "\"", e);
            }

            Message mail = mailBuilder.getMessage(getMailBuilder().getSession());

            render(mailPart, mail);
        }
    }
}
