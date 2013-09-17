/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.service.resource.loader;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;

import com.alibaba.citrus.service.resource.AbstractResourceLoadingTests;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

public class SuperLoaderIntegratingTests extends AbstractResourceLoadingTests {
    @BeforeClass
    public static void initClass() throws Exception {
        initFactory("loader/super-loader-parent.xml");
        initSubFactory("loader/super-loader.xml");
    }

    @Before
    public void init() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("resourceLoadingService");
    }

    @Test
    public void noName() throws Exception {
        assertResourceServiceList("/no_name/webroot/test.txt", "test.txt", true, false);
        assertResourceServiceList("/no_name/webroot", "", true, true, "WEB-INF/", "app1/", "appcontext/", "beans.xml", "common/",
                                  "filter/", "loader/", "logback.xml", "myfolder/", "resources-root.xml",
                                  "resources-skip-validation.xml", "test.txt");
        assertResourceServiceList("/no_name", "loader", true, true, "classpath-loader.xml", "file-loader.xml",
                                  "super-loader-parent.xml", "super-loader.xml", "webapp-loader.xml");
    }

    @Test
    public void withName() throws Exception {
        assertResourceServiceList("/with_name/test.txt", "test.txt", true, false);
        assertResourceServiceList("/with_name", "loader", true, true, "classpath-loader.xml", "file-loader.xml",
                                  "super-loader-parent.xml", "super-loader.xml", "webapp-loader.xml");
    }

    @Test
    public void sameName() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("sameName");

        // resourceName == superLoader's resourceName
        assertResourceServiceList("/webroot/test.txt", "test.txt", true, false);
        assertResourceServiceList("/webroot/", "", true, true, "WEB-INF/", "app1/", "appcontext/", "beans.xml", "common/", "filter/",
                                  "loader/", "logback.xml", "myfolder/", "resources-root.xml", "resources-skip-validation.xml",
                                  "test.txt");
    }

    @Test
    public void defaultName() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("defaultName");

        // 默认映射“/”
        assertResourceServiceList("/test.txt", "test.txt", true, false);
        assertResourceServiceList("/", "", true, true, "WEB-INF/", "app1/", "appcontext/", "beans.xml", "common/", "filter/", "loader/",
                                  "logback.xml", "myfolder/", "resources-root.xml", "resources-skip-validation.xml", "test.txt");

        resourceLoadingService = (ResourceLoadingService) factory.getBean("defaultName1");

        // 默认映射“”
        assertResourceServiceList("/test.txt", "test.txt", true, false);
    }

    @Test
    public void noParent() throws Exception {
        ApplicationContext factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir,
                                                                                               "loader/super-loader.xml")));

        resourceLoadingService = (ResourceLoadingService) factory.getBean("resourceLoadingService");
        assertResourceServiceList("/no_name/webroot/test.txt", "test.txt", false, false);
        assertResourceServiceList("/with_name/test.txt", "test.txt", false, false);

        resourceLoadingService = (ResourceLoadingService) factory.getBean("sameName");
        assertResourceServiceList("/webroot/test.txt", "test.txt", false, false);

        resourceLoadingService = (ResourceLoadingService) factory.getBean("defaultName");
        assertResourceServiceList("/test.txt", "test.txt", false, false);

        resourceLoadingService = (ResourceLoadingService) factory.getBean("defaultName1");
        assertResourceServiceList("/test.txt", "test.txt", false, false);
    }

    @Test
    public void misc() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("misc");

        // abc.txt存在于parent的路径/aaa/bbb上，也存在于/myfolder上
        assertEquals(new File(srcdir, "/WEB-INF/aaa/bbb/abc.txt"),
                     resourceLoadingService.getResourceAsFile("/WEB-INF/aaa/bbb/abc.txt"));

        // def.txt只存在于/myfolder上
        assertEquals(new File(srcdir, "/myfolder/def.txt"),
                     resourceLoadingService.getResourceAsFile("/WEB-INF/aaa/bbb/def.txt"));

        // abc.txt存在于parent的路径/aaa/bbb上，也存在于/myfolder上
        assertEquals(new File(srcdir, "/WEB-INF/aaa/bbb/abc.txt"),
                     resourceLoadingService.getResourceAsFile("/WEB-INF2/aaa/bbb/abc.txt"));

        // def.txt只存在于/myfolder上
        assertEquals(new File(srcdir, "/myfolder/def.txt"),
                     resourceLoadingService.getResourceAsFile("/WEB-INF2/aaa/bbb/def.txt"));
    }

    @Test
    public void multipleSuperLoaders() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("multipleSuperLoaders");

        // loaded from <loaders:super-loader name="/webroot/$1/templates/layout" />
        assertEquals(new File(srcdir, "/app1/templates/layout/default.vm"),
                     resourceLoadingService.getResourceAsFile("/app1/templates/layout/default.vm"));

        // loaded from <loaders:super-loader name="/webroot/common/templates/layout" />
        assertEquals(new File(srcdir, "/common/templates/layout/login.vm"),
                     resourceLoadingService.getResourceAsFile("/app1/templates/layout/login.vm"));
    }

    @Test
    public void multipleSuperLoaders_noParent() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("multipleSuperLoaders");

        // 在同一个context中测试，visitedMappings在super-loader之后被恢复。
        // loaded from <loaders:super-loader name="/webroot2/$1/templates/layout" />
        assertEquals(new File(srcdir, "/app1/templates/layout/default.vm"),
                     resourceLoadingService.getResourceAsFile("/app1/templates/layout2/default.vm"));

        // loaded from <loaders:super-loader name="/webroot2/common/templates/layout" />
        assertEquals(new File(srcdir, "/common/templates/layout/login.vm"),
                     resourceLoadingService.getResourceAsFile("/app1/templates/layout2/login.vm"));
    }
}
