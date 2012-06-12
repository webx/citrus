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

package com.alibaba.citrus.dev.handler.util;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Element {
    private final static AtomicLong counter = new AtomicLong();
    private final String id;
    private final String name;
    private final String ns;
    final         List<Element>          subElements = createLinkedList();
    private final Map<String, Attribute> attrs       = createArrayHashMap();
    private StyledValue text;

    public Element(String name) {
        this(name, null);
    }

    public Element(String name, String ns) {
        this.name = assertNotNull(trimToNull(name), "element name");
        this.ns = trimToNull(ns);
        this.id = name + "-" + counter.addAndGet(1L);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        int index = name.indexOf(":");

        if (index >= 0) {
            return trimToNull(name.substring(0, index));
        } else {
            return null;
        }
    }

    public String getLocalName() {
        int index = name.indexOf(":");

        if (index >= 0) {
            return name.substring(index + 1);
        } else {
            return name;
        }
    }

    public String getNs() {
        return ns;
    }

    public Element addAttribute(String key, String value) {
        attrs.put(key, new Attribute(key, new TextValue(value)));
        return this;
    }

    public Element addAttribute(String key, StyledValue value) {
        attrs.put(key, new Attribute(key, value));
        return this;
    }

    public Element setText(String text) {
        return setText(new TextValue(text));
    }

    public Element setText(StyledValue text) {
        assertTrue(subElements.isEmpty(), "subElements is not empty");
        this.text = text;
        return this;
    }

    public StyledValue getText() {
        return text;
    }

    public Element newSubElement(String name) {
        Element subElement = new Element(name);
        addSubElement(subElement);
        return subElement;
    }

    public void addSubElement(Element subElement) {
        assertNull(text, "text is not null");

        if (subElement != null) {
            subElements.add(subElement);
        }
    }

    public boolean hasSubElements() {
        return !subElements.isEmpty();
    }

    public Iterable<Element> subElements() {
        return subElements;
    }

    public Iterable<Attribute> attributes() {
        return attrs.values();
    }

    public boolean hasAttribute(String key) {
        return attrs.get(key) != null;
    }

    public StyledValue getAttribute(String key) {
        if (hasAttribute(key)) {
            return attrs.get(key).getValue();
        }

        return null;
    }
}
