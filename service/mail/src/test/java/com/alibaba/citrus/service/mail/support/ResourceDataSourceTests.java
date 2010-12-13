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
package com.alibaba.citrus.service.mail.support;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.service.resource.support.ResourceLoadingSupport;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;

public class ResourceDataSourceTests {
    private static XmlApplicationContext factory;
    private Resource resource;
    private Resource resourceNotExist;
    private ResourceDataSource rds;

    @BeforeClass
    public static void initFactory() {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "services.xml")));
        factory.setResourceLoadingExtender(new ResourceLoadingSupport(factory));
    }

    @Before
    public void init() {
        resource = factory.getResource("testfile.txt");
        resourceNotExist = factory.getResource("classpath:notExist.txt");

        try {
            resourceNotExist.getURL();
            fail();
        } catch (IOException e) {
        }
    }

    @Test
    public void resource_null() {
        try {
            new ResourceDataSource(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("resource"));
        }

        try {
            new ResourceDataSource(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("resource"));
        }

        try {
            new ResourceDataSource(null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("resource"));
        }
    }

    @Test
    public void getResource() {
        rds = new ResourceDataSource(resource);
        assertSame(resource, rds.getResource());

        rds = new ResourceDataSource(resource, null);
        assertSame(resource, rds.getResource());

        rds = new ResourceDataSource(resource, null, null);
        assertSame(resource, rds.getResource());
    }

    @Test
    public void getName() {
        // default name
        rds = new ResourceDataSource(resource);
        assertTrue(rds.getName().endsWith("testfile.txt"));

        rds = new ResourceDataSource(resource, null);
        assertTrue(rds.getName().endsWith("testfile.txt"));

        rds = new ResourceDataSource(resource, null, null);
        assertTrue(rds.getName().endsWith("testfile.txt"));

        // name specified
        rds = new ResourceDataSource(resource, "  hello.txt  ");
        assertTrue(rds.getName().endsWith("hello.txt"));

        rds = new ResourceDataSource(resource, "  hello.txt  ", null);
        assertTrue(rds.getName().endsWith("hello.txt"));

        // could not get name
        rds = new ResourceDataSource(resourceNotExist);
        assertNull(rds.getName());

        rds = new ResourceDataSource(resourceNotExist, null);
        assertNull(rds.getName());

        rds = new ResourceDataSource(resourceNotExist, null, null);
        assertNull(rds.getName());
    }

    @Test
    public void getContentType() {
        // default contentType
        rds = new ResourceDataSource(resource);
        assertEquals("application/octet-stream", rds.getContentType());

        rds = new ResourceDataSource(resource, null);
        assertEquals("application/octet-stream", rds.getContentType());

        rds = new ResourceDataSource(resource, null, null);
        assertEquals("application/octet-stream", rds.getContentType());

        // contentType specified
        rds = new ResourceDataSource(resource, null, "text/plain");
        assertEquals("text/plain", rds.getContentType());
    }

    @Test
    public void getInputStream() {
        rds = new ResourceDataSource(resource);
        assertInputStream(true);

        rds = new ResourceDataSource(resource, null);
        assertInputStream(true);

        rds = new ResourceDataSource(resource, null, null);
        assertInputStream(true);

        // not exist
        rds = new ResourceDataSource(resourceNotExist);
        assertInputStream(false);

        rds = new ResourceDataSource(resourceNotExist, null);
        assertInputStream(false);

        rds = new ResourceDataSource(resourceNotExist, null, null);
        assertInputStream(false);
    }

    private void assertInputStream(boolean succ) {
        InputStream is = null;

        try {
            is = rds.getInputStream();
            assertNotNull(is);

            if (!succ) {
                fail();
            }
        } catch (IOException e) {
            if (succ) {
                fail();
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Test
    public void toString_() {
        rds = new ResourceDataSource(resource);
        assertEquals("Resource[testfile.txt, loaded by ResourceLoadingService]", rds.toString());
    }
}
