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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.springext.support.context.XmlApplicationContext;

public class MyConfigurationTests {
    private static ApplicationContext globalFactory;
    private static ApplicationContext factory;
    private MyConfiguration globalConf;
    private MyConfiguration conf1;
    private MyConfiguration conf2;

    @BeforeClass
    public static void initFactory() throws Exception {
        globalFactory = new XmlApplicationContext(new FileSystemResource(
                new File(srcdir, "my-configuration-global.xml")));

        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "my-configuration.xml")),
                globalFactory);
    }

    @Before
    public void init() {
        globalConf = (MyConfiguration) globalFactory.getBean("myConfiguration");
        conf1 = (MyConfiguration) factory.getBean("conf1");
        conf2 = (MyConfiguration) factory.getBean("myConfiguration");

        assertNotNull(globalConf);
        assertNotNull(conf1);
        assertNotNull(conf2);
    }

    @Test
    public void wrong_disable_productionMode() {
        try {
            new XmlApplicationContext(new FileSystemResource(new File(srcdir, "my-configuration-wrong.xml")),
                    globalFactory);
            fail();
        } catch (BeanCreationException e) {
            assertThat(e,
                    exception(IllegalArgumentException.class, "productionMode cannot be disabled at App's Context"));
        }
    }

    @Test
    public void globalConf() {
        assertEquals(true, globalConf.isProductionMode()); // 指定
        assertEquals("globalStringValue", globalConf.getStringValue()); // 指定
        assertSameBean(globalFactory.getBean("myBean1"), globalConf.getMyBean1()); // 默认
        assertSameBean(globalFactory.getBean("myBean2Global"), globalConf.getMyBean2()); // 指定
        assertNull(globalConf.getMyBean3()); // 可选bean，默认bean找不到

        String str = globalConf.toString();

        assertThat(str, containsRegex("productionMode\\s+="));
        assertThat(str, containsRegex("stringValue\\s+="));
        assertThat(str, not(containsRegex("myBean1\\s+=")));
        assertThat(str, containsRegex("myBean2\\s+="));
        assertThat(str, not(containsRegex("myBean3\\s+=")));
    }

    @Test
    public void conf1() {
        assertEquals(true, conf1.isProductionMode()); // 覆盖
        assertEquals("stringValue", conf1.getStringValue()); // 覆盖
        assertSameBean(globalFactory.getBean("myBean1"), conf1.getMyBean1()); // 指定，但从parent取bean
        assertSameBean(globalFactory.getBean("myBean2Global"), conf1.getMyBean2()); // 继承
        assertNull(conf1.getMyBean3()); // 可选bean，默认bean找不到

        String str = conf1.toString();

        assertThat(str, containsString("productionMode (overrided)"));
        assertThat(str, containsString("stringValue (overrided)"));
        assertThat(str, containsRegex("myBean1\\s+="));
        assertThat(str, containsString("myBean2 (inherited)"));
        assertThat(str, not(containsRegex("myBean3\\s+=")));
    }

    @Test
    public void conf2() {
        assertEquals(true, conf2.isProductionMode()); // 继承
        assertEquals("globalStringValue", conf2.getStringValue()); // 继承
        assertSameBean(globalFactory.getBean("myBean1"), conf2.getMyBean1()); // 默认值
        assertSameBean(globalFactory.getBean("myBean2Global"), conf2.getMyBean2()); // 继承
        assertSameBean(factory.getBean("myBean3_haha"), conf2.getMyBean3()); // 指定

        String str = conf2.toString();

        assertThat(str, containsString("productionMode (inherited)"));
        assertThat(str, containsString("stringValue (inherited)"));
        assertThat(str, not(containsRegex("myBean1\\s+=")));
        assertThat(str, containsString("myBean2 (inherited)"));
        assertThat(str, containsRegex("myBean3\\s+="));
    }

    @Test
    public void productionModeSensible() {
        MyBean myBean = (MyBean) factory.getBean("myBean");

        assertEquals(Boolean.TRUE, myBean.productionMode);
    }

    private void assertSameBean(Object targetBean, Object confBean) {
        assertNotNull(confBean);
        assertSame(targetBean, confBean);
    }
}
