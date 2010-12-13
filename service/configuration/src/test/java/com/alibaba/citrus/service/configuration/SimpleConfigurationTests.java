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
package com.alibaba.citrus.service.configuration;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.springext.support.context.XmlApplicationContext;

public class SimpleConfigurationTests {
    private static ApplicationContext factory;
    private Configuration conf;

    @BeforeClass
    public static void initFactory() throws Exception {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "simple-configuration.xml")));
    }

    @Before
    public void init() {
        conf = (Configuration) factory.getBean("simpleConfiguration");
        assertNotNull(conf);
    }

    @Test
    public void conf() {
        assertEquals(true, conf.isProductionMode());

        String str = conf.toString();

        assertThat(str, containsRegex("productionMode\\s+="));
    }

    @Test
    public void productionModeSensible() {
        MyBean myBean = (MyBean) factory.getBean("myBean");

        assertEquals(Boolean.TRUE, myBean.productionMode);
    }
}
