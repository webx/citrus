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

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.resource.AbstractResourceLoadingTests;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.ResourceNotFoundException;
import com.alibaba.citrus.springext.support.context.XmlWebApplicationContext;

public class ClasspathLoaderIntegratingTests extends AbstractResourceLoadingTests {
    @BeforeClass
    public static void initClass() throws Exception {
        initFactory("loader/classpath-loader.xml");

        ClassLoader cl = new URLClassLoader(new URL[] { srcdir.toURI().toURL() }, factory.getClassLoader());
        ((XmlWebApplicationContext) factory).setClassLoader(cl);
        ((XmlWebApplicationContext) factory).refresh();
    }

    @Before
    public void init() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("resourceLoadingService");
    }

    @Test
    public void getResource() throws Exception {
        assertResourceService("/classpath/WEB-INF", "WEB-INF/", true); // dir
        assertResourceService("/classpath/WEB-INF/web.xml", "WEB-INF/web.xml", true);

        // classpath-loader不支持for_create选项
        try {
            resourceLoadingService.getResource("/classpath/WEB-INF/not/found", FOR_CREATE);
            fail();
        } catch (ResourceNotFoundException e) {
        }

    }
}
