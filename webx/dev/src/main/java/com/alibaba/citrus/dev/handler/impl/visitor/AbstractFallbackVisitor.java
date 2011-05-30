package com.alibaba.citrus.dev.handler.impl.visitor;

import com.alibaba.citrus.util.templatelite.FallbackToVisitor;
import com.alibaba.citrus.util.templatelite.FallbackVisitor;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;

public class AbstractFallbackVisitor<V> extends AbstractVisitor implements FallbackVisitor {
    private final FallbackToVisitor ftv;

    public AbstractFallbackVisitor(RequestHandlerContext context, V fallback) {
        super(context);
        this.ftv = new FallbackToVisitor(fallback);
    }

    public boolean visitPlaceholder(String name, Object[] params) throws Exception {
        return ftv.visitPlaceholder(name, params);
    }

    @SuppressWarnings("unchecked")
    public V getFallbackVisitor() {
        return (V) ftv.getVisitor();
    }
}
