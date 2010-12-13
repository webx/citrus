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
package com.alibaba.citrus.service.freemarker;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.freemarker.impl.SpringResourceLoaderAdapter;
import com.alibaba.citrus.service.freemarker.impl.SpringResourceLoaderAdapter.TemplateSource;
import com.alibaba.citrus.service.resource.ResourceFilter;
import com.alibaba.citrus.service.resource.ResourceFilterChain;
import com.alibaba.citrus.service.resource.ResourceLoadingOption;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.ResourceMatchResult;
import com.alibaba.citrus.service.resource.ResourceNotFoundException;
import com.alibaba.citrus.service.resource.support.InputStreamResource;
import com.alibaba.citrus.service.resource.support.context.ResourceLoadingXmlApplicationContext;
import com.alibaba.citrus.util.io.StreamUtil;

public class SpringResourceLoaderAdapterTests {
    private static ApplicationContext factory;
    private SpringResourceLoaderAdapter freemarkerLoader;

    @BeforeClass
    public static void initFactory() {
        factory = new ResourceLoadingXmlApplicationContext(new FileSystemResource(new File(srcdir, "services.xml")));
    }

    @Before
    public void init() throws Exception {
        freemarkerLoader = new SpringResourceLoaderAdapter(factory, "templates");
    }

    @Test
    public void findTemplateSource() throws IOException {
        // test.ftl
        TemplateSource source1 = (TemplateSource) freemarkerLoader.findTemplateSource("test.ftl");
        assertEquals("test", readText(source1.getInputStream()));
        freemarkerLoader.closeTemplateSource(source1);

        // test.ftl again
        TemplateSource source2 = (TemplateSource) freemarkerLoader.findTemplateSource("test.ftl");
        assertEquals("test", readText(source2.getInputStream()));
        freemarkerLoader.closeTemplateSource(source2);

        // test.ftl == test.ftl
        assertThat(source1.hashCode(), equalTo(source2.hashCode()));
        assertThat(source1, equalTo(source2));

        // test1.ftl
        TemplateSource source3 = (TemplateSource) freemarkerLoader.findTemplateSource("test2.ftl");
        assertEquals("test2", readText(source3.getInputStream()));
        freemarkerLoader.closeTemplateSource(source3);

        // test.ftl != test1.ftl
        assertThat(source1.hashCode(), not(equalTo(source3.hashCode())));
        assertThat(source1, not(equalTo(source3)));

        // 模板名为空
        try {
            freemarkerLoader.findTemplateSource(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("templateName"));
        }

        // 模板不存在
        assertNull(freemarkerLoader.findTemplateSource("notExist.ftl"));
    }

    @Test
    public void getLastModified() throws IOException {
        long lastModified = factory.getResource("/templates/test.ftl").lastModified();

        // 资源/templates/test.ftl支持lastModified
        assertEquals(lastModified, lastModified("test.ftl"));

        // 资源/templates/notExist.ftl不存在
        try {
            lastModified("/notExist.ftl");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("templateSource"));
        }

        // 资源/templates/test2.ftl存在，但不支持lastModified，返回-1
        assertEquals(0, factory.getResource("/templates/test2.ftl").lastModified());
        assertEquals(-1, lastModified("test2.ftl"));
    }

    private long lastModified(String name) throws IOException {
        return freemarkerLoader.getLastModified(freemarkerLoader.findTemplateSource(name));
    }

    @Test
    public void getReader() throws IOException {
        TemplateSource templateSource = (TemplateSource) freemarkerLoader.findTemplateSource("test3.ftl");
        Reader reader = freemarkerLoader.getReader(templateSource, "GBK");

        assertEquals("中国", StreamUtil.readText(reader, true));

        freemarkerLoader.closeTemplateSource(templateSource);

        // null
        try {
            freemarkerLoader.getReader(null, "GBK");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("templateSource"));
        }
    }

    @Test
    public void getInputStream() throws IOException {
        TemplateSource source = (TemplateSource) freemarkerLoader.findTemplateSource("test.ftl");
        InputStream istream = source.getInputStream();

        assertNotNull(istream);
        assertSame(istream, source.getInputStream()); // 两次返回同一个stream

        // 关闭后，再打开，将创建新的stream
        freemarkerLoader.closeTemplateSource(source);
        assertNotSame(istream, source.getInputStream());

        freemarkerLoader.closeTemplateSource(source);
    }

    protected final String readText(InputStream stream) throws IOException {
        return StreamUtil.readText(stream, null, true);
    }

    /**
     * 除去resource URL的filter。
     */
    public static class NoURLFilter implements ResourceFilter {
        public void init(ResourceLoadingService resourceLoadingService) {
        }

        public com.alibaba.citrus.service.resource.Resource doFilter(ResourceMatchResult filterMatchResult,
                                                                     Set<ResourceLoadingOption> options,
                                                                     ResourceFilterChain chain)
                throws ResourceNotFoundException {
            com.alibaba.citrus.service.resource.Resource resource = chain.doFilter(filterMatchResult, options);

            try {
                return new InputStreamResource(resource.getInputStream());
            } catch (IOException e) {
                fail();
                return null;
            }
        }
    }
}
