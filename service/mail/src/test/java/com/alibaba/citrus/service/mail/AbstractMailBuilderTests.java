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
package com.alibaba.citrus.service.mail;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Set;

import javax.mail.Session;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.mail.builder.MailBuilder;
import com.alibaba.citrus.service.mail.builder.MailContent;
import com.alibaba.citrus.service.mail.impl.MailServiceImpl;
import com.alibaba.citrus.service.resource.Resource;
import com.alibaba.citrus.service.resource.ResourceFilter;
import com.alibaba.citrus.service.resource.ResourceFilterChain;
import com.alibaba.citrus.service.resource.ResourceLoadingOption;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.ResourceMatchResult;
import com.alibaba.citrus.service.resource.ResourceNotFoundException;
import com.alibaba.citrus.service.resource.support.ResourceLoadingSupport;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.test.runner.TestNameAware;

/**
 * 和mail builder相关的测试基类。
 * 
 * @author Michael Zhou
 */
@RunWith(TestNameAware.class)
public abstract class AbstractMailBuilderTests {
    protected final static String REGEX_EOL = "(\\r|\\n|\\r\\n)";
    protected static final String 我爱北京敏感词_I_LOVE_THE_PRESERVED_KEYWORDS = "我爱北京敏感词 I love the preserved keywords.";
    protected static final String 中国_CHINA_EARTH_COM = "中国 <china@earth.com>";
    protected static final String 美国_CHINA_EARTH_COM = "美国 <us@earth.com>";
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected static XmlApplicationContext factory;
    protected MailServiceImpl mailService;
    protected Session rawSession;
    protected MailBuilder builder;

    @BeforeClass
    public static void initFactory() {
        factory = initFactory("services.xml");
        System.setProperty("user.name", "baobao"); // javamail Service类会去取user.name，此处明确设定该值。
    }

    protected static final XmlApplicationContext initFactory(String configFile) {
        XmlApplicationContext factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, configFile)));
        factory.setResourceLoadingExtender(new ResourceLoadingSupport(factory));
        return factory;
    }

    @Before
    public final void initDefault() {
        rawSession = Session.getDefaultInstance(System.getProperties());
        builder = new MailBuilder();
    }

    /**
     * 取得mail的文本格式。
     */
    protected final String getMessageAsText() throws Exception {
        MailBuilder builderCopy = builder.clone();

        assertNotSame(builder, builderCopy);
        assertNotSame(builder.getContent(), builderCopy.getContent());

        return save(builderCopy.getMessageAsString(rawSession)); // 顺便检查clone的效果
    }

    protected final synchronized String save(String message) throws IOException {
        File base = new File(destdir, getClass().getSimpleName());

        base.mkdirs();

        String name = defaultIfNull(getTestName(), "unknwonTest");

        File saveFile = new File(base, name + ".eml");
        int count = 1;

        while (saveFile.exists()) {
            saveFile = new File(base, name + "_" + count++ + ".eml");
        }

        Writer writer = new OutputStreamWriter(new FileOutputStream(saveFile), "GBK");

        writer.write(message);
        writer.flush();
        writer.close();

        log.debug("Saved mail: {}", saveFile.getCanonicalPath().substring(basedir.getCanonicalPath().length() + 1));

        return message;
    }

    protected final String re(String s) {
        if (s != null) {
            s = s.replace("?", "\\?").replace("+", "\\+").replace("*", "\\*");
        }

        return s;
    }

    protected final void assertNoMailBuilder(MailContent content) {
        try {
            content.getMailBuilder();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no mailBuilder"));
        }
    }

    /**
     * 除去resource URL的filter。
     */
    public static class NoURLFilter implements ResourceFilter {
        public void init(ResourceLoadingService resourceLoadingService) {
        }

        public Resource doFilter(ResourceMatchResult filterMatchResult, Set<ResourceLoadingOption> options,
                                 ResourceFilterChain chain) throws ResourceNotFoundException {
            final Resource resource = chain.doFilter(filterMatchResult, options);

            return new Resource() {
                public boolean exists() {
                    return resource.exists();
                }

                public File getFile() {
                    return null;
                }

                public InputStream getInputStream() throws IOException {
                    return resource.getInputStream();
                }

                public URL getURL() {
                    return null;
                }

                public long lastModified() {
                    return resource.lastModified();
                }
            };
        }
    }
}
