/*
 * Copyright 2010 Alibaba Group Holding Limited.
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
 *
 */
package com.alibaba.citrus.util.internal.templatelite;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.IOException;
import java.util.Map;

/**
 * 一个将template的内容输出到<code>Appendable</code>
 * 的visitor，且当遇到未定义的placeholder时，不会报错，而是从内部的context中取值。
 * 
 * @author Michael Zhou
 */
public class FallbackTextWriter<A extends Appendable> extends TextWriter<A> implements FallbackVisitor {
    private final Map<String, Object> context = createHashMap();

    public FallbackTextWriter() {
        super();
    }

    public FallbackTextWriter(A out) {
        super(out);
    }

    public Map<String, Object> context() {
        return context;
    }

    public void visitPlaceholder(String name, String[] params) throws IOException {
        if (context.containsKey(name)) {
            Object value = context.get(name);

            if (value != null) {
                out().append(value.toString());
            }
        } else {
            out().append("${").append(name).append("}");
        }
    }

    public void visitTemplate(String name, Template template) throws IOException {
        template.accept(this);
    }

    public void visitTemplateGroup(String name, Template[] templates) throws IOException {
        for (Template template : templates) {
            template.accept(this);
        }
    }
}
