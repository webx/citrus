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

package com.alibaba.citrus.webx.config;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.webx.config.impl.WebxConfigurationImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

@RunWith(Parameterized.class)
public class WebxConfigurationSkipValidationTests {
    private final boolean               skipValidation;
    private       ApplicationContext    factory;
    private       WebxConfigurationImpl config;

    public WebxConfigurationSkipValidationTests(boolean skipValidation) {
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

        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir,
                                                                            "webx-configuration-skip-validation.xml")));
        config = (WebxConfigurationImpl) factory.getBean("webxConfiguration");
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void test() {
        assertEquals(true, config.isProductionMode());
        assertEquals("internal", config.getInternalPathPrefix());
        assertSame(factory.getBean("requestContexts"), config.getRequestContexts());
        assertSame(factory.getBean("pipeline"), config.getPipeline());
        assertEquals(null, config.getExceptionPipeline());
        assertEquals("/WEB-INF/webx-*.xml", config.getComponentsConfig().getComponentConfigurationLocationPattern());
    }
}
