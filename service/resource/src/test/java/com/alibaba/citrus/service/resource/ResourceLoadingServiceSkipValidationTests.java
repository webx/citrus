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

package com.alibaba.citrus.service.resource;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.alibaba.citrus.service.resource.filter.XsltResourceFilter;
import com.alibaba.citrus.service.resource.impl.ResourceAlias;
import com.alibaba.citrus.service.resource.impl.ResourceFilterMapping;
import com.alibaba.citrus.service.resource.impl.ResourceLoaderMapping;
import com.alibaba.citrus.service.resource.impl.ResourceLoadingServiceImpl;
import com.alibaba.citrus.service.resource.loader.FileResourceLoader;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

@RunWith(Parameterized.class)
public class ResourceLoadingServiceSkipValidationTests {
    private final boolean                    skipValidation;
    private       ApplicationContext         factory;
    private       ResourceLoadingServiceImpl resourceLoadingService;

    public ResourceLoadingServiceSkipValidationTests(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    @Before
    public void init() {
        if (skipValidation) {
            System.setProperty("skipValidation", "true");
        }

        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "resources-skip-validation.xml")));
        resourceLoadingService = (ResourceLoadingServiceImpl) factory.getBean("resourceLoadingService");
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void resourceMappings() {
        Object[] mappings = getFieldValue(resourceLoadingService, "resourceMappings", Object[].class);

        assertEquals(2, mappings.length);

        for (Object mapping : mappings) {
            if (mapping instanceof ResourceAlias) {
                assertEquals(false, ((ResourceAlias) mapping).isInternal());
            } else if (mapping instanceof ResourceLoaderMapping) {
                assertEquals(false, ((ResourceLoaderMapping) mapping).isInternal());
            } else {
                fail();
            }
        }
    }

    @Test
    public void fileLoader() {
        FileResourceLoader fileLoader = null;

        for (Object mapping : getFieldValue(resourceLoadingService, "resourceMappings", Object[].class)) {
            if (mapping instanceof ResourceLoaderMapping) {
                fileLoader = (FileResourceLoader) ((ResourceLoaderMapping) mapping).getLoaders()[0];
            }
        }

        assertNotNull(fileLoader);
        assertEquals(true, getFieldValue(fileLoader.getPaths()[0], "relative", Boolean.class));
    }

    @Test
    public void xsltFilter() {
        XsltResourceFilter xsltFilter = null;

        for (Object mapping : getFieldValue(resourceLoadingService, "filterMappings", Object[].class)) {
            if (mapping instanceof ResourceFilterMapping) {
                xsltFilter = (XsltResourceFilter) ((ResourceFilterMapping) mapping).getFilters()[0];
            }
        }

        assertNotNull(xsltFilter);
        assertEquals(true, getFieldValue(xsltFilter, "failIfNotFound", Boolean.class));
    }
}
