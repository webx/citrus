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
package com.alibaba.citrus.util.internal.webpagelite.simple;

import java.io.IOException;
import java.io.PrintWriter;

import com.alibaba.citrus.util.internal.templatelite.FallbackTextWriter;
import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.util.internal.webpagelite.RequestContext;

public class SimpleComponent extends PageComponent {
    public SimpleComponent(PageComponentRegistry registry, String componentPath) {
        super(registry, componentPath);
    }

    public void visitTemplate(PrintWriter out, RequestContext request) {
        getTemplate().accept(new SimpleVisitor(out, request));
    }

    @SuppressWarnings("unused")
    private class SimpleVisitor extends FallbackTextWriter<PrintWriter> {
        private final RequestContext request;

        private SimpleVisitor(PrintWriter out, RequestContext request) {
            super(out);
            this.request = request;
        }

        public void visitUrl(String relativeUrl) throws IOException {
            out().append(getComponentURL(request, relativeUrl));
        }
    }
}
