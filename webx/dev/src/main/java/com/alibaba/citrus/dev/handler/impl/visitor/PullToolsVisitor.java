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

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ClassUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ExceptionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.dev.handler.impl.ExplorerHandler.ExplorerVisitor;
import com.alibaba.citrus.service.pull.PullContext;
import com.alibaba.citrus.service.pull.PullService;
import com.alibaba.citrus.util.templatelite.Template;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;

public class PullToolsVisitor extends AbstractFallbackVisitor<ExplorerVisitor> {
    private final PullService tools;
    private final Entry       rootEntry;
    private       Entry       entry;

    public PullToolsVisitor(RequestHandlerContext context, ExplorerVisitor v, PullService tools) {
        super(context, v);
        this.tools = tools;
        this.rootEntry = new Entry(null);

        PullContext pullContext = tools.getContext();
        boolean parentEntry = false;

        String[] names = pullContext.getQualifiedToolNames().toArray(EMPTY_STRING_ARRAY);

        Arrays.sort(names, new Comparator<String>() {
            public int compare(String o1, String o2) {
                int p1 = o1.startsWith("/_parent/") ? 1 : 0;
                int p2 = o2.startsWith("/_parent/") ? 1 : 0;

                if (p1 == p2) {
                    return o1.compareTo(o2);
                } else {
                    return p1 - p2;
                }
            }
        });

        for (String path : names) {
            Entry entry = rootEntry;
            String[] pathsegs = split(path, "/");
            String name = null;

            for (String pathseg : pathsegs) {
                name = pathseg;
                Entry subEntry = entry.subEntries.get(name);

                if (subEntry == null) {
                    subEntry = new Entry(name);
                    entry.subEntries.put(name, subEntry);
                }

                entry = subEntry;

                if (!parentEntry) {
                    if ("_parent".equals(entry.name)) {
                        parentEntry = true;
                    }
                }

                entry.parentEntry = parentEntry;
            }

            try {
                entry.value = pullContext.pull(name);
            } catch (Exception e) {
                entry.value = e;
            }
        }
    }

    public void visitService(Template serviceNotAvailableTemplate, Template serviceTemplate) {
        if (tools == null) {
            serviceNotAvailableTemplate.accept(this);
        } else {
            entry = rootEntry;
            serviceTemplate.accept(this);
        }
    }

    public void visitTool(Template toolTemplate) {
        for (Iterator<Entry> i = entry.subEntries.values().iterator(); i.hasNext(); ) {
            entry = i.next();
            toolTemplate.accept(this);
        }
    }

    public void visitToolName(Template toolWithValueTemplate, Template toolWithoutValueTemplate) {
        if (entry.value != null) {
            toolWithValueTemplate.accept(this);
        } else {
            toolWithoutValueTemplate.accept(this);
        }
    }

    public void visitToolHidden(Template toolHiddenTemplate) {
        if (entry.parentEntry && entry.value != null && visitedName.contains(entry.name)) {
            toolHiddenTemplate.accept(this);
        }
    }

    private Set<String> visitedName = createHashSet();

    public void visitName() {
        String name = entry.name;

        if (entry.parentEntry) {
            if ("_parent".equals(entry.name)) {
                name = "Inherited from Parent Context";
            }
        } else {
            if (entry.value != null) {
                visitedName.add(name);
            }
        }

        out().print(escapeHtml(name));
    }

    public void visitSubTools(Template subToolsTemplate) {
        if (!entry.subEntries.isEmpty()) {
            subToolsTemplate.accept(this);
        }
    }

    public void visitToolValue(Template toolValueTemplate) {
        if (entry.value != null) {
            toolValueTemplate.accept(this);
        }
    }

    public void visitValueTypePackage() {
        try {
            if (entry.value != null) {
                out().print(entry.value.getClass().getPackage().getName() + ".");
            }
        } catch (NullPointerException e) {
        }
    }

    public void visitValueTypeName() {
        out().print(getSimpleClassName(entry.value.getClass(), false));
    }

    public void visitValue() {
        String s;

        if (entry.value instanceof Throwable) {
            s = getStackTrace(getRootCause((Throwable) entry.value));
        } else {
            s = entry.value.toString();
        }

        out().print(escapeHtml(s));
    }

    public void visitToolId() {
        out().print(identityHashCode(entry));
    }

    private class Entry {
        private final String name;
        private final Map<String, Entry> subEntries = createLinkedHashMap();

        private Object  value;
        private boolean parentEntry;

        public Entry(String name) {
            this.name = trimToNull(name);
        }
    }
}
