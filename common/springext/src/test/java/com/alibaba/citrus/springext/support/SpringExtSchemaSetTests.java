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

package com.alibaba.citrus.springext.support;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.List;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.citrus.test.TestEnvStatic;
import org.junit.Before;
import org.junit.Test;

public class SpringExtSchemaSetTests {
    private SpringExtSchemaSet           schemas;
    private List<SpringPluggableItem>    springItems;
    private List<SpringPluggableItem>    noSchemaItems;
    private List<ConfigurationPointItem> configurationPointItems;

    static {
        TestEnvStatic.init();
    }

    @Before
    public void init() {
        schemas = new SpringExtSchemaSet("TEST-INF/test14/cps");

        NamespaceItem[] items = schemas.getIndependentItems();

        springItems = filter(SpringPluggableItem.class, items, true);
        noSchemaItems = filter(SpringPluggableItem.class, items, false);
        configurationPointItems = filter(ConfigurationPointItem.class, items, true);
    }

    @Test
    public void parentConfigurationPoints() {
        // 依赖关系：
        // <a1> -> b, c
        // <b1> -> c
        // <c1> -> a, h
        // <d1> -> d
        // <e1> -> f
        // <f1> -> g
        // <f2> -> h
        // <g1>
        assertArrayEquals(new String[] { "c" }, getParentConfigurationPoints("a"));
        assertArrayEquals(new String[] { "a" }, getParentConfigurationPoints("b"));
        assertArrayEquals(new String[] { "a", "b" }, getParentConfigurationPoints("c"));
        assertArrayEquals(new String[] { "d" }, getParentConfigurationPoints("d"));
        assertArrayEquals(new String[] { }, getParentConfigurationPoints("e"));
        assertArrayEquals(new String[] { "e" }, getParentConfigurationPoints("f"));
        assertArrayEquals(new String[] { "f" }, getParentConfigurationPoints("g"));
        assertArrayEquals(new String[] { "c", "f" }, getParentConfigurationPoints("h"));
    }

    @Test
    public void configurationPointItems() {
        assertEquals(3, configurationPointItems.size());

        assertEquals(true, configurationPointItems.get(0).hasChildren());
        assertEquals("http://localhost/b {\n" +
                     "  b1 {\n" +
                     "    http://localhost/c {\n" +     // a, b -> c -> a 循环引用被切断，变成 b -> c -> a
                     "      c1 {\n" +                   // 引用两个configuration points
                     "        http://localhost/a\n" +   // c -> a
                     "        http://localhost/h\n" +   // c, f -> h 被两个element引用
                     "      }\n" +
                     "    }\n" +
                     "  }\n" +
                     "}", configurationPointItems.get(0).dump());

        assertEquals(false, configurationPointItems.get(1).hasChildren());
        assertEquals("http://localhost/d",              // 自己引用自己，就当没引用
                     configurationPointItems.get(1).dump());

        assertEquals(true, configurationPointItems.get(2).hasChildren());
        assertEquals("http://localhost/e {\n" +         // e -> f
                     "  e1 {\n" +
                     "    http://localhost/f {\n" +     // f在不同的标签中分别引用g和h
                     "      f1 {\n" +
                     "        http://localhost/g\n" +
                     "      }\n" +
                     "\n" +
                     "      f2 {\n" +
                     "        http://localhost/h\n" +   // c, f -> h，h也被c引用
                     "      }\n" +
                     "    }\n" +
                     "  }\n" +
                     "}", configurationPointItems.get(2).dump());
    }

    @Test
    public void springItems() {
        boolean found = false;

        for (SpringPluggableItem item : springItems) {
            if (item.getNamespace().equals("http://www.springframework.org/schema/beans")) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void noSchemaItems() {
        boolean found = false;

        for (SpringPluggableItem item : noSchemaItems) {
            if (item.getNamespace().equals("http://www.springframework.org/schema/p")) {
                found = true;
            }
        }

        assertTrue(found);
    }

    private <I> List<I> filter(Class<I> type, NamespaceItem[] items, boolean withSchemas) {
        List<I> list = createLinkedList();

        for (NamespaceItem item : items) {
            if (type.isInstance(item) && withSchemas == !item.getSchemas().isEmpty()) {
                list.add(type.cast(item));
            }
        }

        return list;
    }

    private String[] getParentConfigurationPoints(String cpName) {
        ConfigurationPoint cp = schemas.getConfigurationPoints().getConfigurationPointByName(cpName);
        List<String> depList = createLinkedList();

        for (Contribution contribution : cp.getDependingContributions()) {
            depList.add(contribution.getConfigurationPoint().getName());
        }

        return depList.toArray(new String[0]);
    }
}
