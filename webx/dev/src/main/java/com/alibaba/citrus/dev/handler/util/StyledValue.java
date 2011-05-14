package com.alibaba.citrus.dev.handler.util;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

public abstract class StyledValue {
    private final String text;

    public StyledValue(String text) {
        this.text = defaultIfNull(text, EMPTY_STRING);
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
