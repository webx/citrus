package com.alibaba.citrus.dev.handler.util;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Element {
    private final static AtomicLong counter = new AtomicLong();
    private final String id;
    private final String name;
    final List<Element> subElements = createLinkedList();
    private final Map<String, Attribute> attrs = createArrayHashMap();
    private StyledValue text;

    public Element(String name) {
        this.name = name;
        this.id = name + "-" + counter.addAndGet(1L);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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
        assertNull(text, "text is not null");
        Element subElement = new Element(name);
        subElements.add(subElement);
        return subElement;
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
