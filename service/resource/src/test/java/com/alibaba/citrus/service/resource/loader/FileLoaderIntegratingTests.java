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
package com.alibaba.citrus.service.resource.loader;

import static com.alibaba.citrus.service.resource.ResourceLoadingService.*;
import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.resource.AbstractResourceLoadingTests;
import com.alibaba.citrus.service.resource.ResourceLoadingService;

public class FileLoaderIntegratingTests extends AbstractResourceLoadingTests {
    @BeforeClass
    public static void initClass() throws Exception {
        initFactory("loader/file-loader.xml");
    }

    @Before
    public void init() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("resourceLoadingService");
    }

    @Test
    public void defaultPath() throws Exception {
        // basedir=.., no path specified
        assertResourceServiceList("/defaultPath/test.txt", "test.txt", true, false);
        assertResourceServiceList("/defaultPath/", "", true, true, "WEB-INF/", "appcontext/", "beans.xml", "filter/",
                "loader/", "logback.xml", "myfolder/", "resources-root.xml", "test.txt");
    }

    @Test
    public void defaultBasedir() throws Exception {
        // no basedir specified
        assertResourceServiceList("/defaultBasedir/file-loader.xml", "loader/file-loader.xml", true, false);
        assertResourceServiceList("/defaultBasedir", "loader", true, true, "classpath-loader.xml", "file-loader.xml",
                "super-loader-parent.xml", "super-loader.xml", "webapp-loader.xml");
    }

    @Test
    public void absBasedir() throws Exception {
        // basedir=absolute srcdir
        assertResourceServiceList("/absBasedir/test.txt", "test.txt", true, false);
        assertResourceServiceList("/absBasedir/", "", true, true, "WEB-INF/", "appcontext/", "beans.xml", "filter/",
                "loader/", "logback.xml", "myfolder/", "resources-root.xml", "test.txt");
    }

    @Test
    public void searchPaths() throws Exception {
        // path1: rel path
        assertResourceServiceList("/approot/bbb/abc.txt", "WEB-INF/aaa/bbb/abc.txt", true, false);
        assertResourceServiceList("/approot/bbb/", "WEB-INF/aaa/bbb/", true, true, "abc.txt");

        // path2: abs path
        assertResourceServiceList("/approot/web.xml", "WEB-INF/web.xml", true, false);
        assertResourceServiceList("/approot/", "WEB-INF/aaa", true, true, "bbb/", "ccc/");
    }

    @Test
    public void dirNotExist() throws Exception {
        assertResourceServiceList("/approot/not/found/", "", false, false);
    }

    @Test
    public void misc() throws Exception {
        // 从resource.xml所在的目录，查找相对路径
        assertEquals(new File(srcdir, "/WEB-INF/aaa/bbb/abc.txt"),
                resourceLoadingService.getResourceAsFile("/file/resource/abc.txt"));

        // 从指定的basedir目录，查找相对路径
        assertEquals(new File(srcdir, "/myfolder/testres.txt"),
                resourceLoadingService.getResourceAsFile("/file/resource/testres.txt"));

        // 从指定的绝对路径查找
        assertEquals(new File(srcdir, "/WEB-INF/aaa/bbb/abc.txt"),
                resourceLoadingService.getResourceAsFile("/file/resource/WEB-INF/aaa/bbb/abc.txt"));

        // 从默认的path查找
        assertEquals(new File(srcdir, "/myfolder/testres.txt"),
                resourceLoadingService.getResourceAsFile("/file/resource2/myfolder/testres.txt"));
    }

    @Test
    public void substitution() throws Exception {
        assertEquals(new File(srcdir, "/myfolder/aaa/bbb/ccc.jsp"),
                resourceLoadingService.getResourceAsFile("/my/substitution/aaa/bbb/ccc.jhtml", FOR_CREATE));
    }
}
