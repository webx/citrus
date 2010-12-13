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
package com.alibaba.citrus.springext.support.context;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.FileSystemResource;

public class XmlApplicationContextTests extends AbstractBeanFactoryTests {
    private static XmlApplicationContext factory;

    @BeforeClass
    public static void initFactory() {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "beans.xml")));
    }

    @Override
    protected BeanFactory getFactory() {
        return factory;
    }

    @Test
    public void autwireAnnotations() {
        MyClass myClass = (MyClass) getFactory().getBean("myClass");

        assertNotNull(myClass.getContainer());
    }
}
