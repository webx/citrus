package com.alibaba.citrus.dev.handler.util;

public class Attribute {
    private final String key;
    private final StyledValue value;

    public Attribute(String key, StyledValue value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public StyledValue getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
