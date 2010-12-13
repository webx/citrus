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

import static com.alibaba.citrus.service.resource.ResourceLoadingService.*;
import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.io.StreamUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.resource.impl.ResourceLoadingServiceImpl;
import com.alibaba.citrus.service.resource.support.InputStreamResource;

public class ResourceLoadingServiceTests extends AbstractResourceLoadingTests {
    @BeforeClass
    public static void initClass() throws Exception {
        initFactory("resources-root.xml");
        initSubFactory("WEB-INF/resources.xml");
    }

    @Before
    public void init() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("resourceLoadingService");

        ResourceLoadingService parentService = (ResourceLoadingService) parentFactory.getBean("resourceLoadingService");

        assertSame(parentService, resourceLoadingService.getParent());
    }

    @Test
    public void notInited() {
        // 在service初始化过程中，某个loader/filter通过spring resource loader间接递归调用该service时，报错。
        try {
            new ResourceLoadingServiceImpl().getResource("test");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Bean instance of " + ResourceLoadingService.class.getName()
                    + " has not been initialized yet"));
        }
    }

    @Test
    public void getResource_emptyName() throws Exception {
        try {
            resourceLoadingService.getResource(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("resourceName"));
        }

        try {
            resourceLoadingService.getResource("  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("resourceName"));
        }
    }

    @Test
    public void service_withParentRef() throws Exception {
        ResourceLoadingService parentService = (ResourceLoadingService) parentFactory
                .getBean("myParentResourceLoadingService");

        // myResourceLoadingService指定了parentRef=myParentResourceLoadingService
        resourceLoadingService = (ResourceLoadingService) factory.getBean("myResourceLoadingService");

        assertSame(parentService, resourceLoadingService.getParent());
        assertResourceService("/default/test.txt", "test.txt", true);
    }

    @Test
    public void service_defaultParentRef() throws Exception {
        ResourceLoadingService parentService = (ResourceLoadingService) parentFactory
                .getBean("resourceLoadingService_1");

        // resourceLoadingService_1未指定parentRef，但是parent context中包含同名的service
        resourceLoadingService = (ResourceLoadingService) factory.getBean("resourceLoadingService_1");

        assertSame(parentService, resourceLoadingService.getParent());
        assertResourceService("/default/test.txt", "test.txt", true);
    }

    @Test
    public void resourceAlias_bySuperLoader() throws Exception {
        ResourceLoadingService parentService = (ResourceLoadingService) parentFactory
                .getBean("resourceLoadingService_2");

        // resourceLoadingService_2未指定parentRef，但是parent context中包含同名的service
        resourceLoadingService = (ResourceLoadingService) factory.getBean("resourceLoadingService_2");

        assertSame(parentService, resourceLoadingService.getParent());

        // /myfolder/testres.txt 映射到<super-loader name="/webroot">
        // 和<resource-alias name="/webroot">等效
        assertEquals(new File(srcdir, "/myfolder/testres.txt"),
                resourceLoadingService.getResourceAsFile("/myfolder/testres.txt"));
    }

    @Test
    public void getResource_notFound() throws Exception {
        try {
            resourceLoadingService.getResourceAsURL("/not/found.txt");
            fail();
        } catch (ResourceNotFoundException e) {
            assertResourceNotFoundException(e, "/not/found.txt", "/webroot/not/found.txt");
        }
    }

    @Test
    public void getResource_parent_defaultMapping() throws Exception {
        // 当前resource loader中没找到，到parent中找，匹配/
        assertEquals(new File(srcdir, "/myfolder/testres.txt"),
                resourceLoadingService.getResourceAsFile("/myfolder/testres.txt"));
    }

    @Test
    public void getResource_alias_notFound() throws Exception {
        // Alias被匹配，但没找到resource mapping
        try {
            resourceLoadingService.getResourceAsURL("/my/alias1/testres.txt");
            fail();
        } catch (ResourceNotFoundException e) {
            assertResourceNotFoundException(e, "/my/alias1/testres.txt", "/not/found/testres.txt",
                    "/webroot/not/found/testres.txt");
        }
    }

    @Test
    public void getResource_alias_foundInParent() throws Exception {
        // Alias被匹配，从default resource loader中找到资源
        assertEquals(new File(srcdir, "/myfolder/testres.txt"),
                resourceLoadingService.getResourceAsFile("/my/alias3/testres.txt"));
    }

    @Test
    public void getResource_internal_found() throws Exception {
        // Alias被匹配，internal mapping被找到
        assertEquals(new File(srcdir, "/myfolder/testres.txt"),
                resourceLoadingService.getResourceAsFile("/my/alias4/testres.txt"));

        assertEquals(new File(srcdir, "/myfolder/testres.txt"),
                resourceLoadingService.getParent().getResourceAsFile("/myfolder/testres.txt"));

        // super-loader被匹配，internal mapping被找到
        assertEquals(new File(srcdir, "/myfolder/testres.txt"),
                resourceLoadingService.getResourceAsFile("/my/alias5/testres.txt"));
    }

    @Test
    public void getResource_internal_notFound() throws Exception {
        // 直接找internal mapping是不行的
        try {
            resourceLoadingService.getResourceAsURL("/my/internal/resource/testres.txt");
            fail();
        } catch (ResourceNotFoundException e) {
            assertResourceNotFoundException(e, "/my/internal/resource/testres.txt",
                    "/webroot/my/internal/resource/testres.txt");
        }

        try {
            resourceLoadingService.getParent().getResourceAsURL("/webroot/myfolder/testres.txt");
            fail();
        } catch (ResourceNotFoundException e) {
            assertResourceNotFoundException(e, "/webroot/myfolder/testres.txt", "/webroot/webroot/myfolder/testres.txt");
        }

        // alias映射到parent internal mapping，这样是不行的
        try {
            resourceLoadingService.getResourceAsURL("/my/alias6/testres.txt");
            fail();
        } catch (ResourceNotFoundException e) {
            assertResourceNotFoundException(e, "/my/alias6/testres.txt", "/webroot/myfolder/testres.txt",
                    "/webroot/webroot/myfolder/testres.txt");
        }

        // super-loader映射到parent internal mapping，这样是不行的
        try {
            resourceLoadingService.getResourceAsURL("/my/alias7/testres.txt");
            fail();
        } catch (ResourceNotFoundException e) {
            // 由于是loader的方式，故caused by丢失
            assertResourceNotFoundException(e, "/my/alias7/testres.txt", "/webroot/myfolder/testres.txt");
        }
    }

    @Test
    public void getResource_noLoaders() throws Exception {
        // 匹配，但没有loaders
        try {
            resourceLoadingService.getResourceAsURL("/my/resource/testres.txt");
            fail();
        } catch (ResourceNotFoundException e) {
            assertResourceNotFoundException(e, "/my/resource/testres.txt");
        }
    }

    /**
     * 无论resourceName是否以/开始，都可以匹配相应的资源。
     */
    @Test
    public void getResource_relativeResourceName() throws Exception {
        // resource.xml中为相对路径：pattern="relative/resource"
        assertEquals(new File(srcdir, "/WEB-INF/aaa/bbb/abc.txt"),
                resourceLoadingService.getResourceAsFile("/relative/resource/abc.txt"));
        assertEquals(new File(srcdir, "/WEB-INF/aaa/bbb/abc.txt"),
                resourceLoadingService.getResourceAsFile("relative/resource/abc.txt"));
        assertEquals(new File(srcdir, "/WEB-INF/aaa/bbb/abc.txt"),
                resourceLoadingService.getResourceAsFile("aaa/../relative/resource/abc.txt"));

        // aaa/(relative/resource)/abc.txt => aaa/(aaa/bbb)/abc.txt
        assertEquals(new File(srcdir, "/WEB-INF/aaa/aaa/bbb/abc.txt"),
                resourceLoadingService.getResourceAsFile("aaa/relative/resource/abc.txt", FOR_CREATE));

        // resource.xml中为绝对路径：pattern="/absolute/resource"
        assertEquals(new File(srcdir, "/WEB-INF/aaa/bbb/abc.txt"),
                resourceLoadingService.getResourceAsFile("/absolute/resource/abc.txt"));
        assertEquals(new File(srcdir, "/WEB-INF/aaa/bbb/abc.txt"),
                resourceLoadingService.getResourceAsFile("absolute/resource/abc.txt"));

        try {
            resourceLoadingService.getResourceAsFile("aaa/absolute/resource/abc.txt");
            fail();
        } catch (ResourceNotFoundException e) {
            assertResourceNotFoundException(e, "aaa/absolute/resource/abc.txt",
                    "/webroot/aaa/absolute/resource/abc.txt");
        }

        assertEquals(new File(srcdir, "/WEB-INF/aaa/bbb/abc.txt"),
                resourceLoadingService.getResourceAsFile("aaa/../absolute/resource/abc.txt"));
    }

    @Test
    public void relevancy() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("relevancy");

        // /aaa/bbb/ccc匹配：/, /aaa/**, /aaa/bbb/ccc, /**，
        // 但/aaa/bbb/ccc => /dir3最相关
        assertEquals(new File(srcdir, "/dir3"), resourceLoadingService.getResourceAsFile("/aaa/bbb/ccc", FOR_CREATE));

        // /aaa/bbb/ddd匹配：/, /aaa/**, /**
        // 但/aaa/** => /dir2最相关
        assertEquals(new File(srcdir, "/dir2"), resourceLoadingService.getResourceAsFile("/aaa/bbb/ddd", FOR_CREATE));

        // /bbb匹配：/, /**
        // 但/**的匹配长度较长，故选择/** => /dir4最相关
        assertEquals(new File(srcdir, "/dir4"), resourceLoadingService.getResourceAsFile("/bbb", FOR_CREATE));
    }

    @Test
    public void getResourceAsFile() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("getResourceAs");

        // file存在
        File f = new File(srcdir, "/myfolder/testres.txt");

        Resource resource = resourceLoadingService.getResource("/myfolder/testres.txt");
        assertEquals(f, resource.getFile());

        File resourceFile = resourceLoadingService.getResourceAsFile("/myfolder/testres.txt");
        assertEquals(f, resourceFile);

        // file可创建
        f = new File(srcdir, "/not/found");

        resource = resourceLoadingService.getResource("/basedir/not/found", FOR_CREATE);
        assertEquals(f, resource.getFile());

        resourceFile = resourceLoadingService.getResourceAsFile("/basedir/not/found", FOR_CREATE);
        assertEquals(f, resourceFile);

        // file不存在
        resource = resourceLoadingService.getResource("/classpath/java/lang/String.class");
        assertEquals(null, resource.getFile());

        try {
            resourceLoadingService.getResourceAsFile("/classpath/java/lang/String.class");
            fail();
        } catch (ResourceNotFoundException e) {
            assertThat(e, exception("Could not get File of resource", "/classpath/java/lang/String.class"));
        }
    }

    @Test
    public void getResourceAsURL() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("getResourceAs");

        // URL存在
        URL u = new File(srcdir, "/myfolder/testres.txt").toURI().toURL();

        Resource resource = resourceLoadingService.getResource("/myfolder/testres.txt");
        assertEquals(u, resource.getURL());

        URL resourceURL = resourceLoadingService.getResourceAsURL("/myfolder/testres.txt");
        assertEquals(u, resourceURL);

        // URL不存在
        resource = resourceLoadingService.getResource("/asURL/java/lang/String.class");
        assertEquals(null, resource.getURL());

        try {
            resourceLoadingService.getResourceAsURL("/asURL/java/lang/String.class");
            fail();
        } catch (ResourceNotFoundException e) {
            assertThat(e, exception("Could not get URL of resource", "/asURL/java/lang/String.class"));
        }
    }

    @Test
    public void getResourceAsStream() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("getResourceAs");

        String testresContent = readText(new FileInputStream(new File(srcdir, "/myfolder/testres.txt")), null, true);

        // Stream存在
        Resource resource = resourceLoadingService.getResource("/myfolder/testres.txt");
        assertEquals(testresContent, readText(resource.getInputStream(), null, true));

        assertEquals(testresContent,
                readText(resourceLoadingService.getResourceAsStream("/myfolder/testres.txt"), null, true));

        // Stream不存在
        resource = resourceLoadingService.getResource("/asStream/java/lang/String.class");
        assertEquals(null, resource.getInputStream());

        try {
            resourceLoadingService.getResourceAsStream("/asStream/java/lang/String.class");
            fail();
        } catch (ResourceNotFoundException e) {
            assertThat(e, exception("Could not get InputStream of resource", "/asStream/java/lang/String.class"));
        }
    }

    @Test
    public void exists() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("getResourceAs");

        assertTrue(resourceLoadingService.exists("/myfolder/testres.txt"));
        assertTrue(resourceLoadingService.exists("/classpath/java/lang/String.class"));
        assertTrue(resourceLoadingService.exists("/asURL/java/lang/String.class"));
        assertTrue(resourceLoadingService.exists("/asStream/java/lang/String.class"));

        assertFalse(resourceLoadingService.exists("/not/found"));
    }

    @Test
    public void getPatterns_noParent() {
        assertArrayEquals(new String[] {//
                "/my/alias1", //
                        "/my/alias2", //
                        "/my/alias3", //
                        "/my/alias4", //
                        "/my/alias5", //
                        "/my/alias6", //
                        "/my/alias7", //
                        "/my/resource", //
                        "relative/resource", //
                        "/absolute/resource", //
                }, resourceLoadingService.getPatterns(false));
    }

    @Test
    public void getPatterns_withParent() {
        assertArrayEquals(new String[] {//
                "/my/alias1", //
                        "/my/alias2", //
                        "/my/alias3", //
                        "/my/alias4", //
                        "/my/alias5", //
                        "/my/alias6", //
                        "/my/alias7", //
                        "/my/resource", //
                        "relative/resource", //
                        "/absolute/resource", //
                        "/classpath", // parent
                        "/", // parent
                }, resourceLoadingService.getPatterns(true));
    }

    /**
     * 当存在多个loaders，第一个loader找到的resource不存在（option=FOR_CREATE），
     * 第二个loader找到的resource已经存在，则返回第二个。
     */
    @Test
    public void getResource_priority() {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("resourcePriority");

        // 1. config/web.xml - not exists
        // 2. config/folder/web.xml - not exists
        // 3. config/WEB-INF/web.xml - exists
        // returns 3
        Resource resource = resourceLoadingService.getResource("/resourcePriority/web.xml", FOR_CREATE);

        assertTrue(resource.exists());
        assertEquals(new File(srcdir, "WEB-INF/web.xml"), resource.getFile());

        // 1. config/web2.xml - not exists
        // 2. config/folder/web2.xml - not exists
        // 3. config/WEB-INF/web2.xml - not exists
        // returns 1
        resource = resourceLoadingService.getResource("/resourcePriority/web2.xml", FOR_CREATE);

        assertFalse(resource.exists());
        assertEquals(new File(srcdir, "web2.xml"), resource.getFile());
    }

    private void assertResourceNotFoundException(Throwable e, String... messages) {
        for (String msg : messages) {
            assertThat(e, exception(ResourceNotFoundException.class, msg));
            e = e.getCause();
        }

        assertThat(e, nullValue());
    }

    /**
     * 除去resource URL的filter。
     */
    public static class NoURLFilter implements ResourceFilter {
        public void init(ResourceLoadingService resourceLoadingService) {
        }

        public Resource doFilter(ResourceMatchResult filterMatchResult, Set<ResourceLoadingOption> options,
                                 ResourceFilterChain chain) throws ResourceNotFoundException {
            Resource resource = chain.doFilter(filterMatchResult, options);

            try {
                return new InputStreamResource(resource.getInputStream());
            } catch (IOException e) {
                fail();
                return null;
            }
        }
    }

    /**
     * 除去resource Stream的filter。
     */
    public static class NoStreamFilter implements ResourceFilter {
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
                    return resource.getFile();
                }

                public InputStream getInputStream() throws IOException {
                    return null;
                }

                public URL getURL() {
                    return resource.getURL();
                }

                public long lastModified() {
                    return resource.lastModified();
                }
            };
        }
    }
}
