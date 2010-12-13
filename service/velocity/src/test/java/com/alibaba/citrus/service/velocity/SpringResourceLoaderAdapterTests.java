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
package com.alibaba.citrus.service.velocity;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.apache.velocity.runtime.RuntimeConstants.*;
import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.resource.support.context.ResourceLoadingXmlApplicationContext;
import com.alibaba.citrus.service.velocity.impl.Slf4jLogChute;
import com.alibaba.citrus.service.velocity.impl.SpringResourceLoaderAdapter;

public class SpringResourceLoaderAdapterTests extends AbstractResourceLoaderTests {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static ApplicationContext factory;
    private SpringResourceLoaderAdapter velocityLoader;

    @BeforeClass
    public static void initFactory() {
        factory = new ResourceLoadingXmlApplicationContext(new FileSystemResource(new File(srcdir, "services.xml")));
    }

    @Before
    public void init() throws Exception {
        ExtendedProperties config = new ExtendedProperties();
        RuntimeServices rsvc = new RuntimeInstance();

        config.setProperty("path", "/templates");
        config.setProperty("resource.loader", "");

        rsvc.setConfiguration(config);
        rsvc.setProperty(RUNTIME_LOG_LOGSYSTEM, new Slf4jLogChute(log));
        rsvc.setApplicationAttribute(SpringResourceLoaderAdapter.SPRING_RESOURCE_LOADER_KEY, factory);

        rsvc.init();

        velocityLoader = new SpringResourceLoaderAdapter();
        velocityLoader.commonInit(rsvc, config);
        velocityLoader.init(config);
    }

    @Test
    public void getResourceStream() throws Exception {
        String text = readText(velocityLoader.getResourceStream("test.vm"));
        assertEquals("test", text);

        text = readText(velocityLoader.getResourceStream("test2.vm"));
        assertEquals("test2", text);

        // 模板名为空
        try {
            velocityLoader.getResourceStream(null);
            fail();
        } catch (org.apache.velocity.exception.ResourceNotFoundException e) {
            assertThat(e, exception("Need to specify a template name"));
        }

        // 模板不存在
        try {
            velocityLoader.getResourceStream("notExist.vm");
            fail();
        } catch (org.apache.velocity.exception.ResourceNotFoundException e) {
            assertThat(e, exception("SpringResourceLoaderAdapter", "could not find template: notExist.vm"));
        }
    }

    @Test
    public void isSourceModified() throws Exception {
        Resource templateResource = new Template();
        long lastModified = factory.getResource("/templates/test.vm").lastModified();

        // 资源/templates/test.vm支持lastModified，但时间不同
        templateResource.setLastModified(1);
        templateResource.setName("/test.vm");

        assertTrue(lastModified != templateResource.getLastModified());
        assertTrue(velocityLoader.isSourceModified(templateResource));

        // 资源/templates/test.vm支持lastModified，时间相同
        templateResource.setLastModified(lastModified);
        templateResource.setName("/test.vm");

        assertEquals(lastModified, templateResource.getLastModified());
        assertFalse(velocityLoader.isSourceModified(templateResource));

        // 资源/templates/notExist.vm不存在，看作被修改了
        templateResource.setLastModified(1);
        templateResource.setName("/notExist.vm");

        assertFalse(factory.getResource("/templates/notExist.vm").exists());
        assertTrue(velocityLoader.isSourceModified(templateResource));

        // 资源/templates/test2.vm存在，但不支持lastModified，看作未修改
        templateResource.setLastModified(1);
        templateResource.setName("/test2.vm");

        lastModified = factory.getResource("/templates/test2.vm").lastModified();

        assertEquals(0, lastModified);
        assertFalse(velocityLoader.isSourceModified(templateResource));

        // 模板名为空
        templateResource.setName(null);

        try {
            velocityLoader.isSourceModified(templateResource);
            fail();
        } catch (org.apache.velocity.exception.ResourceNotFoundException e) {
            assertThat(e, exception("Need to specify a template name"));
        }
    }

    @Test
    public void getLastModified() throws Exception {
        Resource templateResource = new Template();
        long lastModified = factory.getResource("/templates/test.vm").lastModified();

        // 资源/templates/test.vm支持lastModified
        templateResource.setName("/test.vm");
        assertEquals(lastModified, velocityLoader.getLastModified(templateResource));

        // 资源/templates/notExist.vm不存在，返回0
        templateResource.setName("/notExist.vm");
        assertEquals(0, velocityLoader.getLastModified(templateResource));

        // 资源/templates/test2.vm存在，但不支持lastModified，返回0
        templateResource.setName("/test2.vm");
        assertEquals(0, factory.getResource("/templates/test2.vm").lastModified());
        assertEquals(0, velocityLoader.getLastModified(templateResource));

        // 模板名为空
        templateResource.setName(null);

        try {
            velocityLoader.getLastModified(templateResource);
            fail();
        } catch (org.apache.velocity.exception.ResourceNotFoundException e) {
            assertThat(e, exception("Need to specify a template name"));
        }
    }

    @Test
    public void _toString() {
        assertEquals("SpringResourceLoaderAdapter[/templates/]", velocityLoader.toString());
    }
}
