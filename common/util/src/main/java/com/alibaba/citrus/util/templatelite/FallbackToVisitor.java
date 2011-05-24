package com.alibaba.citrus.util.templatelite;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

/**
 * 调用指定visitor中的visitPlaceholder()方法。
 */
public class FallbackToVisitor {
    private final Object visitor;

    public FallbackToVisitor(Object visitor) {
        this.visitor = assertNotNull(visitor, "fallback to visitor");
    }

    public Object getVisitor() {
        return visitor;
    }

    public boolean visitPlaceholder(String name, Object[] params) throws Exception {
        try {
            visitor.getClass().getMethod("visit" + capitalize(name)).invoke(visitor);
        } catch (NoSuchMethodException e) {
            if (visitor instanceof FallbackVisitor) {
                return ((FallbackVisitor) visitor).visitPlaceholder(name, params);
            } else {
                return false;
            }
        }

        return true;
    }
}
