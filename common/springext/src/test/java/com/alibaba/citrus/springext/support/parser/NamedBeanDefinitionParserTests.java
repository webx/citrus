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
package com.alibaba.citrus.springext.support.parser;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.springext.support.context.XmlApplicationContext;

/**
 * 测试<code>AbstractNamedBeanDefinitionParser</code>。
 * 
 * @author Michael Zhou
 */
public class NamedBeanDefinitionParserTests {
    private static XmlApplicationContext factory;

    @BeforeClass
    public static void initFactory() {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "my-beans.xml")));
    }

    /**
     * 未指定id，且<code>getDefaultName()</code>返回空。
     */
    @Test
    public void noDefaultName_And_NoIdSpecified() {
        try {
            new XmlApplicationContext(new FileSystemResource(new File(srcdir, "my-beans-no-default-name.xml")));
            fail();
        } catch (FatalBeanException e) {
            assertThat(e, exception(IllegalArgumentException.class, "neither id nor defaultName was specified"));
        }
    }

    /**
     * 指定id，且<code>getDefaultName()</code>返回空。
     */
    @Test
    public void noDefaultName() {
        MyBean myBean = (MyBean) factory.getBean("noDefaultName");
        assertNotNull(myBean);
    }

    @Test
    public void withDefaultName() {
        MyBean myBean1 = (MyBean) factory.getBean("myBean");
        MyBean myBean2 = (MyBean) factory.getBean("myBean#0");
        MyBean myBean3 = (MyBean) factory.getBean("myBean#1");

        // not null
        assertNotNull(myBean1);
        assertNotNull(myBean2);
        assertNotNull(myBean3);

        // not same
        assertNotSame(myBean1, myBean2);
        assertNotSame(myBean1, myBean3);
        assertNotSame(myBean2, myBean3);

        // aliases
        assertSame(myBean1, factory.getBean("aaa"));
        assertSame(myBean1, factory.getBean("bbb"));
    }
}
