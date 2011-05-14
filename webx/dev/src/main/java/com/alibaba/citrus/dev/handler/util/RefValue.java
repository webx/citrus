package com.alibaba.citrus.dev.handler.util;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

public class RefValue extends StyledValue {
    private final String[] names;

    public RefValue(String... names) {
        super(join(names, ", "));
        this.names = defaultIfNull(names, EMPTY_STRING_ARRAY);
    }

    public String[] getNames() {
        return names;
    }
}
