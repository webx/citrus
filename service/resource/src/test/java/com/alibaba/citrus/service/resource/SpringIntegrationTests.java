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
package com.alibaba.citrus.service.resource;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.io.StreamUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.ServletContextResource;

import com.alibaba.citrus.service.resource.support.ResourceAdapter;
import com.alibaba.citrus.service.resource.support.ResourceLoadingSupport;
import com.alibaba.citrus.service.resource.support.URLResource;
import com.alibaba.citrus.springext.support.context.XmlWebApplicationContext;

/**
 * 测试和spring容器的ResourceLoader整合的效果。
 * 
 * @author Michael Zhou
 */
public class SpringIntegrationTests extends AbstractResourceLoadingTests {
    private ResourceLoadingSupport support;
    private XmlWebApplicationContext context;

    @BeforeClass
    public static void initClass() throws Exception {
        initServlet();
    }

    @Before
    public void init() {
        context = new XmlWebApplicationContext();
        support = new ResourceLoadingSupport(context);

        context.setResourceLoadingExtender(support);
        context.setServletContext(servletContext);

        MyLoader.locationHolder.remove();
    }

    @Test
    public void resourceLoadingSupport_noFactory() {
        try {
            new ResourceLoadingSupport(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("beanFactory"));
        }
    }

    @Test
    public void resourceLoadingSupport_getResourceLoadingService() {
        initContext("beans.xml");
        assertSame(context.getBean("resourceLoadingService"), support.getResourceLoadingService());
        assertSame(context.getBean("resourceLoadingService"),
                new ResourceLoadingSupport(context).getResourceLoadingService());
    }

    @Test
    public void getResource_contextNotReady() throws Exception {
        // context未准备好，此时使用默认的机制装载资源。
        Resource resource = context.getResource("beans.xml");
        assertThat(resource, instanceOf(ServletContextResource.class));
        assertTrue(resource.exists());

        // context未准备好，此时使用默认的resource pattern resolver机制查找资源。
        Resource[] resources = context.getResources("WEB-INF/**/*.txt");

        assertEquals(2, resources.length);

        assertThat(resources[0], instanceOf(FileSystemResource.class)); // PathMatchingResourcePatternResolver
        assertTrue(resources[0].exists());

        assertThat(resources[1], instanceOf(FileSystemResource.class)); // PathMatchingResourcePatternResolver
        assertTrue(resources[1].exists());
    }

    @Test
    public void getResource_noResourceLoadingService() throws Exception {
        context.setResourceLoadingExtender(new ResourceLoadingSupport(context, "nonExistId"));
        initContext("beans.xml");

        // resourceLoadingService[id=nonExistId]不存在，使用默认的机制装载资源
        Resource resource = context.getResource("beans.xml");
        assertThat(resource, instanceOf(ServletContextResource.class));
        assertTrue(resource.exists());

        // resourceLoadingService[id=nonExistId]不存在，使用默认的resource pattern resolver机制查找资源
        Resource[] resources = context.getResources("WEB-INF/**/*.txt");

        assertEquals(2, resources.length);

        assertThat(resources[0], instanceOf(FileSystemResource.class)); // PathMatchingResourcePatternResolver
        assertTrue(resources[0].exists());

        assertThat(resources[1], instanceOf(FileSystemResource.class)); // PathMatchingResourcePatternResolver
        assertTrue(resources[1].exists());
    }

    @Test
    public void getResource_recursively() throws Exception {
        initContext("beans.xml");

        // 由于递归调用，myloader初始化时，resource loading service还没初始化完，此时使用默认的机制装载资源。
        Resource resource = MyLoader.locationHolder.get();
        assertThat(resource, instanceOf(ServletContextResource.class));
        assertTrue(resource.exists());

        // 通过myloader装载资源
        Resource resource2 = context.getResource("/test");
        assertThat(resource2, instanceOf(ResourceAdapter.class));
        assertTrue(resource2.exists());
        assertEquals("Resource[/test, loaded by ResourceLoadingService]", resource2.toString());
        assertThat(resource2.getURL().toString(), containsAll("file:", "test.txt"));
    }

    @Test
    public void getResource_notExist() throws Exception {
        initContext("beans.xml");

        // 对于不存在的资源，返回NonExistResource
        Resource resource = context.getResource("/classpath/not/exist");
        assertThat(resource.getClass().getName(), containsAll("NonExistResource"));
        assertFalse(resource.exists());
        assertEquals("NonExistResource[/classpath/not/exist]", resource.getDescription());
        assertEquals("/classpath/not/exist", ((ContextResource) resource).getPathWithinContext());

        try {
            resource.getFile();
            fail();
        } catch (IOException e) {
            assertThat(e, exception("Resource Not Found [/classpath/not/exist]"));
        }

        try {
            resource.getFilename();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("NonExistResource[/classpath/not/exist]"));
        }

        try {
            resource.getInputStream();
            fail();
        } catch (IOException e) {
            assertThat(e, exception("Resource Not Found [/classpath/not/exist]"));
        }

        try {
            resource.getURI();
            fail();
        } catch (IOException e) {
            assertThat(e, exception("Resource Not Found [/classpath/not/exist]"));
        }

        try {
            resource.getURL();
            fail();
        } catch (IOException e) {
            assertThat(e, exception("Resource Not Found [/classpath/not/exist]"));
        }

        try {
            resource.createRelative("test");
            fail();
        } catch (IOException e) {
            assertThat(e, exception("Resource Not Found [/classpath/not/exist]"));
        }
    }

    @Test
    public void getResource_notExistFile() throws Exception {
        initContext("beans.xml");

        // 以FOR_CREATE的方式调用resourceLoadingService，因此文件不存在还是能返回
        Resource resource = context.getResource("/basedir/not/exist");
        assertThat(resource, instanceOf(ResourceAdapter.class));
        assertFalse(resource.exists());
        assertEquals("Resource[/basedir/not/exist, loaded by ResourceLoadingService]", resource.toString());

        // 其它resource adapter的方法
        assertEquals("/basedir/not/exist", ((ContextResource) resource).getPathWithinContext());
        assertEquals(new File(srcdir, "not/exist"), resource.getFile());
        assertEquals(new File(srcdir, "not/exist").toURI(), resource.getURI());
        assertEquals(new File(srcdir, "not/exist").toURI().toURL(), resource.getURL());
        assertEquals("exist", resource.getFilename());
        assertEquals("test.txt", context.getResource("/test").getFilename());
        assertEquals("test", readText(context.getResource("/test").getInputStream(), null, true));
        assertEquals(new File(srcdir, "not/subdir"), resource.createRelative("subdir").getFile());
    }

    @Test
    public void getResource_inject() throws Exception {
        initContext("beans.xml");

        MyBean myBean = (MyBean) context.getBean("myBean");

        URL resource1 = (URL) myBean.get("resource1");
        assertThat(resource1.toString(), containsAll("file:", "config/test.txt"));

        Resource resource2 = (Resource) myBean.get("resource2");
        assertThat(resource2, instanceOf(ResourceAdapter.class));
    }

    @Test
    public void getResources_notFound() throws Exception {
        initContext("beans.xml");

        try {
            context.getResources("/WEB-INF/**/*.txt");
            fail();
        } catch (IOException e) {
            assertThat(e, exception("Resource Not Found [/WEB-INF/]"));
            assertThat(e, exception(ResourceNotFoundException.class, "Could not find resource \"/WEB-INF/\""));
        }

        // Resolver不保证返回的资源是存在的！
        Resource[] resources = context.getResources("/WEB-INF/notExist.txt");

        assertEquals(1, resources.length);
        assertEquals("NonExistResource", resources[0].getClass().getSimpleName());
        assertFalse(resources[0].exists());
    }

    @Test
    public void getResources() throws Exception {
        initContext("beans.xml");

        Resource[] resources = context.getResources("/webroot/WEB-INF/**/*.txt");

        assertEquals(2, resources.length);

        assertThat(resources[0], instanceOf(ResourceAdapter.class));
        assertTrue(resources[0].exists());

        assertThat(resources[1], instanceOf(ResourceAdapter.class));
        assertTrue(resources[1].exists());
    }

    @Test
    public void resourceAdapter_lastModified() {
        com.alibaba.citrus.service.resource.Resource resource = createMock(com.alibaba.citrus.service.resource.Resource.class);

        expect(resource.lastModified()).andReturn(123L);
        replay(resource);

        ResourceAdapter adapter = new ResourceAdapter("/test", resource);

        assertEquals(123L, adapter.lastModified());
    }

    @Test
    public void resourceAdapter_hashCodeAndEquals() {
        initContext("beans.xml");

        ResourceAdapter r1 = (ResourceAdapter) context.getResource("/webroot/beans.xml");
        ResourceAdapter r2 = (ResourceAdapter) context.getResource("/webroot/beans.xml");

        assertHashCodeAndEquals(r1, r2);

        r2 = (ResourceAdapter) context.getResource("/basedir/beans.xml"); // 不是同一类型

        assertNotSame(r1, r2);
        assertNotSame(r1.getResource(), r2.getResource());

        assertThat(r1.hashCode(), not(equalTo(r2.hashCode())));
        assertThat(r1.getResource().hashCode(), not(equalTo(r2.getResource().hashCode())));

        assertThat(r1, not(equalTo(r2)));
        assertThat(r1.getResource(), not(equalTo(r2.getResource())));
    }

    private void assertHashCodeAndEquals(ResourceAdapter r1, ResourceAdapter r2) {
        assertNotSame(r1, r2);
        assertNotSame(r1.getResource(), r2.getResource());

        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotSame(r1.getResource().hashCode(), r2.getResource().hashCode());

        assertEquals(r1, r2);
        assertEquals(r1.getResource(), r2.getResource());
    }

    private void initContext(String name) {
        context.setConfigLocation(name);
        context.refresh();
    }

    public static class MyLoader implements ResourceLoader {
        public final static ThreadLocal<Resource> locationHolder = new ThreadLocal<Resource>();

        public void init(ResourceLoadingService resourceLoadingService) {
        }

        public void setLocation(Resource location) {
            locationHolder.set(location);
        }

        public com.alibaba.citrus.service.resource.Resource getResource(ResourceLoaderContext context,
                                                                        Set<ResourceLoadingOption> options) {
            try {
                return new URLResource(locationHolder.get().getURL());
            } catch (IOException e) {
                fail();
                return null;
            }
        }
    }

    public static class MyBean extends HashMap<String, Object> {
        private static final long serialVersionUID = 6414842602692795852L;

        public void setResource1(URL resource1) {
            put("resource1", resource1);
        }

        public void setResource2(Resource resource2) {
            put("resource2", resource2);
        }
    }
}
