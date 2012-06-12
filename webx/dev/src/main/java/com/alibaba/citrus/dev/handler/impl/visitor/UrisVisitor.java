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

import static com.alibaba.citrus.util.ClassUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.alibaba.citrus.dev.handler.impl.ExplorerHandler.ExplorerVisitor;
import com.alibaba.citrus.service.uribroker.URIBrokerService;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;
import com.alibaba.citrus.util.templatelite.Template;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;

public class UrisVisitor extends AbstractFallbackVisitor<ExplorerVisitor> {
    private final URIBrokerService uris;
    private       URIBroker        uri;
    private       boolean          exposed;
    private       String           name;

    public UrisVisitor(RequestHandlerContext context, ExplorerVisitor v, URIBrokerService uris) {
        super(context, v);
        this.uris = uris;
    }

    public void visitService(Template serviceNotAvailableTemplate, Template serviceTemplate) {
        if (uris == null) {
            serviceNotAvailableTemplate.accept(this);
        } else {
            serviceTemplate.accept(this);
        }
    }

    public void visitUri(Template uriTemplate) {
        final Set<String> exposedNames = createHashSet(uris.getExposedNames());
        List<String> names = createArrayList(uris.getNames());

        sort(names, new Comparator<String>() {
            public int compare(String o1, String o2) {
                int t1 = exposedNames.contains(o1) ? 0 : 1;
                int t2 = exposedNames.contains(o2) ? 0 : 1;

                if (t1 != t2) {
                    return t1 - t2;
                } else {
                    return o1.compareTo(o2);
                }
            }
        });

        for (String name : names) {
            this.uri = uris.getURIBroker(name);
            this.exposed = exposedNames.contains(name);
            this.name = name;

            uriTemplate.accept(this);
        }
    }

    public void visitUriName(Template exposedTemplate, Template hiddenTemplate) {
        if (exposed) {
            exposedTemplate.accept(this);
        } else {
            hiddenTemplate.accept(this);
        }
    }

    public void visitName() {
        out().print(name);
    }

    public void visitValueTypePackage() {
        out().print(uri.getClass().getPackage().getName() + ".");
    }

    public void visitValueTypeName() {
        out().print(getSimpleClassName(uri.getClass(), false));
    }

    public void visitValue() {
        out().print(uri.toString());
    }

    public void visitHidden(Template hiddenTemplate) {
        if (!exposed) {
            hiddenTemplate.accept(this);
        }
    }
}
