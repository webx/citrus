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
