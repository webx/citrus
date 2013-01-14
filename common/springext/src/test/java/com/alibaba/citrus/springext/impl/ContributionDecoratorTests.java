/*
 * Copyright (c) 2002-2013 Alibaba Group Holding Limited.
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

package com.alibaba.citrus.springext.impl;

import static com.alibaba.citrus.test.TestEnvStatic.srcdir;
import static org.junit.Assert.*;

import java.io.File;

import com.alibaba.citrus.springext.contrib.deco.MyDecoratableClass;
import com.alibaba.citrus.springext.contrib.deco.MyDecorator;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

public class ContributionDecoratorTests {
    private static XmlApplicationContext factory;

    @BeforeClass
    public static void initFactory() {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "beans-decorators.xml")));
    }

    @Test
    public void decorator() {
        MyDecoratableClass deco1 = (MyDecoratableClass) factory.getBean("deco1");

        assertEquals("hello", deco1.sayHello());
        assertTrue(deco1 instanceof MyDecorator);
    }

    @Test
    public void decoratorAttr() {
        MyDecoratableClass deco2 = (MyDecoratableClass) factory.getBean("deco2");

        assertEquals("hello", deco2.sayHello());
        assertTrue(deco2 instanceof MyDecorator);
    }
}
