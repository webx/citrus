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
package com.alibaba.citrus.service.resource.support.context;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

/**
 * 测试从resource loading service中装载子context的配置文件。
 * 
 * @author Michael Zhou
 */
public class ResourceLoadingXmlApplicationContextTests {
    private static ApplicationContext parentContext;

    @BeforeClass
    public static void initParentContext() throws Exception {
        parentContext = new ResourceLoadingXmlApplicationContext(new FileSystemResource(new File(srcdir,
                "appcontext/services-parent.xml")));
    }

    protected static ApplicationContext createContext(String... configLocations) {
        return new ResourceLoadingXmlApplicationContext(configLocations, parentContext);
    }

    @Test
    public void subContext_withResourceLoading() throws Exception {
        ApplicationContext context = createContext("my/services1.xml");

        URL loadFromParent = context.getResource("/my/services1.xml").getURL();
        URL loadLocally = context.getResource("/my1/services1.xml").getURL();

        assertEquals(loadFromParent, loadLocally);
    }

    @Test
    public void subContext_withoutResourceLoading() throws Exception {
        ApplicationContext context = createContext("my/services2.xml");

        URL loadFromParent = context.getResource("/my/services2.xml").getURL();
        assertNotNull(loadFromParent);

        assertFalse(context.getResource("/my1/services2.xml").exists());
    }
}
