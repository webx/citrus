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

package com.alibaba.citrus.service.configuration;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

public class PropertyPlaceholderTests {
    private ApplicationContext factory;
    private Configuration      conf;

    @Test
    public void defaultValue() {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder.xml")));
        conf = (Configuration) factory.getBean("simpleConfiguration");

        // ${productionMode:false}
        assertEquals(false, conf.isProductionMode());
    }

    @Test
    public void systemPropertyValue() {
        System.setProperty("productionMode", "true");

        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder.xml")));
        conf = (Configuration) factory.getBean("simpleConfiguration");

        // ${productionMode:false}
        assertEquals(true, conf.isProductionMode());
    }

    @Test
    public void invalidValue() {
        System.setProperty("productionMode", "invalid");

        try {
            new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder.xml")));
            fail();
        } catch (Exception e) {
            assertThat(e, exception(IllegalArgumentException.class, "invalid"));
        } finally {
            System.clearProperty("productionMode");
        }
    }

    @Test
    public void location() {
        // with system property: -Dtest1=test1.props
        System.setProperty("test1", "test1.props");
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder-2.xml")));
        List<?> list = (List<?>) factory.getBean("list");
        assertArrayEquals(new Object[] { "111", "222", "defaultValue" }, list.toArray(EMPTY_OBJECT_ARRAY));

        // no system property: -Dtest1, but has default value
        System.clearProperty("test1");
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder-2.xml")));
        list = (List<?>) factory.getBean("list");
        assertArrayEquals(new Object[] { "111", "222", "defaultValue" }, list.toArray(EMPTY_OBJECT_ARRAY));

        // override default value
        System.setProperty("test1", "test3.props");
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder-2.xml")));
        list = (List<?>) factory.getBean("list");
        assertArrayEquals(new Object[] { "11111", "222", "defaultValue" }, list.toArray(EMPTY_OBJECT_ARRAY));

        // no system property and no default value
        System.clearProperty("test1");

        try {
            new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder-4.xml")));
            fail();
        } catch (BeansException e) {
            assertThat(e, exception("${test1}"));
        }

        // no system property and with empty default value
        System.clearProperty("test1");
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder-5.xml")));
        list = (List<?>) factory.getBean("list");

        // ${x:} with empty default value
        assertArrayEquals(new Object[] { "", "222", "defaultValue" }, list.toArray(EMPTY_OBJECT_ARRAY));
    }

    @Test
    public void propertiesRef() {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder-3.xml")));
        List<?> list = (List<?>) factory.getBean("list");
        assertArrayEquals(new Object[] { "aaa", "bbb", "defaultValue" }, list.toArray(EMPTY_OBJECT_ARRAY));
    }

    @Test
    public void propertiesRef_specificProperties() {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder-1.xml")));
        List<?> list = (List<?>) factory.getBean("list");
        assertArrayEquals(new Object[] { "aaa", "ccc", "defaultValue" }, list.toArray(EMPTY_OBJECT_ARRAY));
    }

    @Test
    public void unresolvable_ignored() {
        System.clearProperty("x");
        System.clearProperty("y");
        System.clearProperty("z");

        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder-6.xml")));
        List<?> list = (List<?>) factory.getBean("list");

        assertArrayEquals(new Object[] { "${x}", "${y}", "defaultValue" }, list.toArray(EMPTY_OBJECT_ARRAY));
    }

    @Test
    public void unresolvable_throwException() {
        System.clearProperty("x");
        System.clearProperty("y");
        System.clearProperty("z");

        try {
            new XmlApplicationContext(new FileSystemResource(new File(srcdir, "property-placeholder-7.xml")));
            fail();
        } catch (BeansException e) {
            assertThat(e, exception("Could not resolve placeholder 'x'"));
        }
    }
}
