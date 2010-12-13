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
package com.alibaba.citrus.springext.util;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DomUtilTests {
    @Test
    public void _convertElement() throws Exception {
        org.dom4j.Element dom4jElement = DomUtil.convertElement(createSampleElement());

        StringWriter out = new StringWriter();
        XMLWriter writer = new XMLWriter(out, OutputFormat.createCompactFormat());

        writer.write(dom4jElement);

        assertEquals("<root><hello name=\"world\"/></root>", out.toString());
    }

    @Test
    public void _convertElementToSAX() throws Exception {
        StringWriter out = new StringWriter();
        XMLWriter writer = new XMLWriter(out, OutputFormat.createCompactFormat());

        convertElement(createSampleElement(), writer);

        assertEquals("<root><hello name=\"world\"></hello></root>", out.toString());
    }

    private Element createSampleElement() throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("root");
        doc.appendChild(root);

        Element helloTag = doc.createElement("hello");
        root.appendChild(helloTag);

        helloTag.setAttribute("name", "world");

        return root;
    }

    @Test
    public void _subElements() throws Exception {
        Element root = createSampleElement2();

        // all elements
        assertElements(subElements(root), "ns1Tag@ns1", "ns2Tag@ns2",
                "beans@http://www.springframework.org/schema/beans", "noNsTag@null");
        assertElements(subElements(root, null), "ns1Tag@ns1", "ns2Tag@ns2",
                "beans@http://www.springframework.org/schema/beans", "noNsTag@null");

        // ns==root.ns
        assertElements(subElements(root, sameNs(root)), "ns1Tag@ns1");

        // ns==ns2
        assertElements(subElements(root, ns("ns2")), "ns2Tag@ns2");

        // ns==null
        assertElements(subElements(root, ns((String) null)), "noNsTag@null");

        // ns==beans
        assertElements(subElements(root, beansNs()), "beans@http://www.springframework.org/schema/beans");

        // name==ns1Tag
        assertElements(subElements(root, name("ns1Tag")), "ns1Tag@ns1");

        // name==noNsTag
        assertElements(subElements(root, name("noNsTag")), "noNsTag@null");

        // 多重选择：and
        assertElements(subElements(root, and(ns(null), name("noNsTag"))), "noNsTag@null");

        // 多重选择：or
        assertElements(subElements(root, or(sameNs(root), ns("ns2"))), "ns1Tag@ns1", "ns2Tag@ns2");

        // 反向选择：not
        assertElements(subElements(root, not(sameNs(root))), "ns2Tag@ns2",
                "beans@http://www.springframework.org/schema/beans", "noNsTag@null");

        // 接受或报错
        try {
            subElements(root, or(sameNs(root), ns("ns2"), error()));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Unexpected element", "beans"));
        }
    }

    private void assertElements(List<Element> elements, String... names) {
        assertEquals(names.length, elements.size());

        int i = 0;
        for (Element element : elements) {
            assertEquals(names[i++], element.getNodeName() + "@" + element.getNamespaceURI());
        }
    }

    private Element createSampleElement2() throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElementNS("ns1", "root");
        doc.appendChild(root);

        Element ns1Tag = doc.createElementNS("ns1", "ns1Tag");
        root.appendChild(ns1Tag);

        Element ns2Tag = doc.createElementNS("ns2", "ns2Tag");
        root.appendChild(ns2Tag);

        Element beansTag = doc.createElementNS("http://www.springframework.org/schema/beans", "beans");
        root.appendChild(beansTag);

        Element noNsTag = doc.createElement("noNsTag");
        root.appendChild(noNsTag);

        return root;
    }
}
