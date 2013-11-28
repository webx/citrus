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
import static java.util.Collections.*;
import static javax.xml.XMLConstants.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.TreeItem;
import com.alibaba.citrus.test.TestEnvStatic;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.junit.Before;
import org.junit.Test;

public class SpringExtSchemaSetTests {
    private SpringExtSchemaSet           schemas;
    private List<SpringPluggableItem>    springItems;
    private List<SpringPluggableItem>    noSchemaItems;
    private List<ConfigurationPointItem> configurationPointItems;
    private List<ConfigurationPointItem> configurationPointAllItems;

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
        configurationPointAllItems = filter(ConfigurationPointItem.class, schemas.getAllItems(), true);
    }

    @Test
    public void test14_parentConfigurationPoints() {
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
    public void test14_configurationPointAllItems() {
        assertEquals(8, configurationPointAllItems.size());

        Iterator<ConfigurationPointItem> i = configurationPointAllItems.iterator();
        assertEquals("http://localhost/a", i.next().getNamespace());
        assertEquals("http://localhost/b", i.next().getNamespace());
        assertEquals("http://localhost/c", i.next().getNamespace());
        assertEquals("http://localhost/d", i.next().getNamespace());
        assertEquals("http://localhost/e", i.next().getNamespace());
        assertEquals("http://localhost/f", i.next().getNamespace());
        assertEquals("http://localhost/g", i.next().getNamespace());
        assertEquals("http://localhost/h", i.next().getNamespace());

        assertFalse(i.hasNext());
    }

    @Test
    public void test14_configurationPointItems() {
        assertEquals(3, configurationPointItems.size());

        assertEquals(true, configurationPointItems.get(0).hasChildren());
        assertEquals("http://localhost/b {\n" +
                     "  b1 {\n" +
                     "    http://localhost/c {\n" +       // a, b -> c -> a 循环引用被切断，变成 b -> c -> a
                     "      c1 {\n" +                     // 引用两个configuration points
                     "        http://localhost/a\n" +     // c -> a
                     "        http://localhost/h\n" +     // c, f -> h 被两个element引用
                     "      }\n" +
                     "    }\n" +
                     "  }\n" +
                     "}", configurationPointItems.get(0).dump());

        assertEquals(false, configurationPointItems.get(1).hasChildren());
        assertEquals("http://localhost/d",                // 自己引用自己，就当没引用
                     configurationPointItems.get(1).dump());

        assertEquals(true, configurationPointItems.get(2).hasChildren());
        assertEquals("http://localhost/e {\n" +           // e -> f
                     "  e1 {\n" +
                     "    http://localhost/f {\n" +       // f在不同的标签中分别引用g和h
                     "      f1 {\n" +
                     "        http://localhost/g\n" +
                     "      }\n" +
                     "\n" +
                     "      f2 {\n" +
                     "        http://localhost/h\n" +     // c, f -> h，h也被c引用
                     "      }\n" +
                     "    }\n" +
                     "  }\n" +
                     "}", configurationPointItems.get(2).dump());
    }

    @Test
    public void test14_configurationPointItems_includingAllContributions() {
        configurationPointItems = filter(ConfigurationPointItem.class, schemas.getIndependentItems(true), true);

        assertEquals(3, configurationPointItems.size());

        assertEquals(true, hasGrandChildren(configurationPointItems.get(0)));
        assertEquals("http://localhost/b {\n" +
                     "  b1 {\n" +
                     "    http://localhost/c {\n" +       // a, b -> c -> a 循环引用被切断，变成 b -> c -> a
                     "      c1 {\n" +                     // 引用两个configuration points
                     "        http://localhost/a {\n" +   // c -> a
                     "          a1\n" +
                     "        }\n" +
                     "\n" +
                     "        http://localhost/h {\n" +   // c, f -> h 被两个element引用
                     "          h1\n" +
                     "        }\n" +
                     "      }\n" +
                     "    }\n" +
                     "  }\n" +
                     "}", configurationPointItems.get(0).dump());

        assertEquals(false, hasGrandChildren(configurationPointItems.get(1)));
        assertEquals("http://localhost/d {\n" +           // 自己引用自己，就当没引用
                     "  d1\n" +
                     "}",
                     configurationPointItems.get(1).dump());

        assertEquals(true, hasGrandChildren(configurationPointItems.get(2)));
        assertEquals("http://localhost/e {\n" +           // e -> f
                     "  e1 {\n" +
                     "    http://localhost/f {\n" +       // f在不同的标签中分别引用g和h
                     "      f1 {\n" +
                     "        http://localhost/g {\n" +
                     "          g1\n" +
                     "        }\n" +
                     "      }\n" +
                     "\n" +
                     "      f2 {\n" +
                     "        http://localhost/h {\n" +   // c, f -> h，h也被c引用
                     "          h1\n" +
                     "        }\n" +
                     "      }\n" +
                     "    }\n" +
                     "  }\n" +
                     "}", configurationPointItems.get(2).dump());
    }

    @Test
    public void test14_with_or_without_allContributions() {
        NamespaceItem[] itemsAll = schemas.getAllItems();
        NamespaceItem[] items = schemas.getIndependentItems();
        NamespaceItem[] itemsWithAllContributions = schemas.getIndependentItems(true);

        // 多次调用值不变
        assertSame(items, schemas.getIndependentItems());
        assertSame(items, schemas.getIndependentItems(false));

        assertSame(itemsWithAllContributions, schemas.getIndependentItems(true));

        assertSame(itemsAll, schemas.getAllItems());
    }

    @Test
    public void test14_springItems() {
        boolean found = false;

        for (SpringPluggableItem item : springItems) {
            if (item.getNamespace().equals("http://www.springframework.org/schema/beans")) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void test14_noSchemaItems() {
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

    @Test
    public void test15_parentConfigurationPoints() {
        // services/s1 includes included-schema.xsd
        // services/s2 includes included-schema.xsd
        // services/s3 no includes
        schemas = new SpringExtSchemaSet("TEST-INF/test15/cps");

        // 测试interceptors是否被s1和s2依赖
        ConfigurationPoint interceptors = schemas.getConfigurationPoints().getConfigurationPointByName("interceptors");
        Collection<Contribution> contributions = interceptors.getDependingContributions();

        assertEquals(2, contributions.size());

        for (Contribution contribution : contributions) {
            assertEquals("services", contribution.getConfigurationPoint().getName());
            assertTrue("s1".equals(contribution.getName()) || "s2".equals(contribution.getName()));
        }

        // 测试included-schema.xsd中的anyElement是否被替换。
        Schema includedSchema = schemas.getNamedMappings().get("localhost/included-schema.xsd");
        Document doc = includedSchema.getDocument();
        Namespace xsd = DocumentHelper.createNamespace("xsd", W3C_XML_SCHEMA_NS_URI);
        Element choice = doc.getRootElement()
                            .element(QName.get("group", xsd))
                            .element(QName.get("sequence", xsd))
                            .element(QName.get("choice", xsd));

        assertEquals("0", choice.attributeValue("minOccurs"));
        assertEquals("unbounded", choice.attributeValue("maxOccurs"));

        Set<String> refs = createTreeSet();

        for (Object e : choice.elements()) {
            refs.add(((Element) e).attributeValue("ref"));
        }

        assertArrayEquals(new String[] { "interceptors:i1", "interceptors:i2" }, refs.toArray());
    }

    @Test
    public void test15_includeWithRelativePath() {
        schemas = new SpringExtSchemaSet("TEST-INF/test15/cps");

        Schema schema = schemas.getNamedMappings().get("localhost/transports/http/configuration/http-conf.xsd");

        List<Schema.Element> elements = createArrayList(schema.getElements());

        sort(elements, new Comparator<Schema.Element>() {
            @Override
            public int compare(Schema.Element o1, Schema.Element o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        assertEquals("[Element[client], Element[server]]", elements.toString());
    }

    private boolean hasGrandChildren(TreeItem item) {
        for (TreeItem child : item.getChildren()) {
            if (child.hasChildren()) {
                return true;
            }
        }

        return false;
    }
}
