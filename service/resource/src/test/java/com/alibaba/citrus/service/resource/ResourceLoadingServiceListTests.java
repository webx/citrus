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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResourceLoadingServiceListTests extends AbstractResourceLoadingTests {
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
    public void list() throws Exception {
        assertResourceServiceList("/my/alias1", "", false, false);
        assertResourceServiceList("/my/alias3", "myfolder", true, true, "abc.txt", "def.txt", "test.result",
                "test.xml", "test.xsl", "test2.xml", "testres.txt");
        assertResourceServiceList("/my/alias4", "myfolder", true, true, "abc.txt", "def.txt", "test.result",
                "test.xml", "test.xsl", "test2.xml", "testres.txt");
        assertResourceServiceList("/my/alias5", "myfolder", true, true, "abc.txt", "def.txt", "test.result",
                "test.xml", "test.xsl", "test2.xml", "testres.txt");
        assertResourceServiceList("/my/alias6", "", false, false);
        assertResourceServiceList("/my/alias7", "", false, false);
        assertResourceServiceList("/my/resource", "", false, false);
    }
}
