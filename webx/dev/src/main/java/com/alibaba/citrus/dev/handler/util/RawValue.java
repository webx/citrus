package com.alibaba.citrus.dev.handler.util;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

public class RawValue extends StyledValue {
    private final Class<?> rawType;
    private final String rawToString;

    public RawValue(Class<?> rawType, String rawToString) {
        super(rawToString);
        this.rawType = rawType;
        this.rawToString = defaultIfNull(rawToString, EMPTY_STRING);
    }

    public Class<?> getRawType() {
        return rawType == null ? Object.class : rawType;
    }

    public String getRawToString() {
        return rawToString;
    }
}
