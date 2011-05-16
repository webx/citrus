package com.alibaba.citrus.dev.handler.util;

import com.alibaba.citrus.util.ClassUtil;

public class ClassValue extends StyledValue {
    private final String className;

    public ClassValue(String className) {
        super(className);
        this.className = className;
    }

    public String getPackageName() {
        return ClassUtil.getPackageName(className);
    }

    public String getSimpleName() {
        return ClassUtil.getSimpleClassName(className, false);
    }

    public String getClassName() {
        return className;
    }
}
