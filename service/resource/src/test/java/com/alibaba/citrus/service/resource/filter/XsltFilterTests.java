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
package com.alibaba.citrus.service.resource.filter;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.resource.AbstractResourceLoadingTests;
import com.alibaba.citrus.service.resource.Resource;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.util.io.StreamUtil;

public class XsltFilterTests extends AbstractResourceLoadingTests {
    @BeforeClass
    public static void initClass() throws Exception {
        initFactory("resources-root.xml");
        initSubFactory("filter/xslt-filter.xml");
    }

    @Before
    public void init() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("resourceLoadingService");

        ResourceLoadingService parentService = (ResourceLoadingService) parentFactory.getBean("resourceLoadingService");

        assertSame(parentService, resourceLoadingService.getParent());
    }

    @Test
    public void inMemory() throws Exception {
        Resource resource = resourceLoadingService.getResource("/myfolder/test.xml");

        // 因为没设置saveTo，因此不可以取得URL和File。
        assertNull(resource.getURL());
        assertNull(resource.getFile());

        // 检查转换的结果, 读两遍仍应正常
        String output = normalizeString(StreamUtil.readText(resource.getInputStream(), "GB2312", true));

        output = normalizeString(StreamUtil.readText(resource.getInputStream(), "GB2312", true));

        System.out.println(output);

        String expected = normalizeString(StreamUtil.readText(
                resourceLoadingService.getResourceAsStream("/myfolder/test.result"), "GB2312", true));

        assertEquals(expected, output);
    }

    @Test
    public void saveToFile() throws Exception {
        Resource resource = resourceLoadingService.getResource("/myfolder/test2.xml");

        // 因为设置了saveToDir，因此可以取得URL和File。
        assertNotNull(resource.getURL());
        assertNotNull(resource.getFile());

        // 检查转换的结果
        String output = normalizeString(StreamUtil.readText(resource.getInputStream(), "GB2312", true));

        output = normalizeString(StreamUtil.readText(resource.getInputStream(), "GB2312", true));

        System.out.println(output);

        String expected = normalizeString(StreamUtil.readText(
                resourceLoadingService.getResourceAsStream("/myfolder/test.result"), "GB2312", true));

        assertEquals(expected, output);
    }

    /**
     * 规格化字符串, 把"\r\n"和"\r"换成"\n", 便于字符串比较.
     * 
     * @param str 字符串.
     * @return 规格化后的字符串.
     */
    public final String normalizeString(String str) {
        return str.replaceAll("\\r\\n?", "\n").replaceAll(">\\s*<", ">\n<");
    }
}
