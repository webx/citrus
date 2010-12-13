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
package com.alibaba.citrus.springext.export;

import java.io.IOException;
import java.io.PrintWriter;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.util.internal.templatelite.FallbackTextWriter;
import com.alibaba.citrus.util.internal.templatelite.Template;
import com.alibaba.citrus.util.internal.templatelite.TextWriter;
import com.alibaba.citrus.util.internal.webpagelite.RequestContext;
import com.alibaba.citrus.util.internal.webpagelite.RequestProcessor;

/**
 * 在WEB上调用<code>SchemaExporter</code>，可以把schema显示在WEB页面中。 *
 * <p>
 * 本类不依赖于servlet api。request和response创建适配器。
 * </p>
 * 
 * @author Michael Zhou
 */
public class SchemaExporterWEB extends SchemaExporter {
    private final Template listTemplate = new Template(getClass().getResource("list.htm"));
    private final RequestProcessor<RequestContext> processor = new RequestProcessor<RequestContext>() {
        @Override
        protected boolean resourceExists(String resourceName) {
            return getEntry(resourceName) != null;
        }

        @Override
        protected void renderPage(RequestContext request, String resourceName) throws IOException {
            Entry entry = getEntry(resourceName);

            if (entry.isDirectory()) {
                renderListPage(request, entry); // 渲染list页面
            } else {
                renderContentPage(request, entry); // 渲染schema内容页面
            }
        }
    };

    private final MenuProvider menuProvider;

    public SchemaExporterWEB() {
        this(null);
    }

    public SchemaExporterWEB(MenuProvider menuProvider) {
        this.menuProvider = menuProvider;
    }

    public MenuProvider getMenuProvider() {
        return menuProvider;
    }

    public interface MenuProvider {
        /**
         * 由子类在list页面中插入菜单。
         */
        void renderMenuHead(RequestContext request) throws Exception;

        /**
         * 由子类在list页面中插入菜单。
         */
        void renderMenu(RequestContext request) throws Exception;
    }

    /**
     * 处理请求。
     */
    public void processRequest(RequestContext request) throws IOException {
        processor.processRequest(request);
    }

    /**
     * 渲染列表页面。
     */
    private void renderListPage(final RequestContext request, final Entry rootEntry) throws IOException {
        PrintWriter out = request.getWriter("text/html; charset=UTF-8");
        listTemplate.accept(new ListPageVisitor(out, request, rootEntry, "text/html; charset=UTF-8"));
        out.flush();
    }

    /**
     * 渲染schema文件页面。
     */
    private void renderContentPage(RequestContext request, Entry entry) throws IOException {
        PrintWriter out = request.getWriter("text/xml; charset=UTF-8");
        writeTo(out, entry, "UTF-8", request.getResourceURL("/"));
        out.flush();
    }

    @SuppressWarnings("unused")
    private class AbstractEntryVisitor extends TextWriter<PrintWriter> {
        protected final RequestContext request;
        protected final String lastEntryPath;
        protected final Entry entry;

        public AbstractEntryVisitor(PrintWriter out, RequestContext request, Entry entry, String lastEntryPath) {
            super(out);
            this.request = request;
            this.entry = entry;
            this.lastEntryPath = lastEntryPath;
        }

        public void visitUrl(String relativeUrl) {
            out().print(request.getResourceURL(relativeUrl));
        }

        public void visitEntryUrl() {
            out().append(request.getResourceURL(entry.getPath()));
        }

        public void visitEntryName() {
            out().append(entry.getName());
        }

        public void visitEntryClass() {
            if (entry.isDirectory()) {
                out().append("directory");
            } else {
                out().append("file");
            }

            if (!entry.containsSchemaWithTargetNamespace()) {
                out().append(" no-target-namespace");
            }
        }

        public void visitEntryId() {
            out().append(entry.getId());
        }
    }

    @SuppressWarnings("unused")
    private class ListPageVisitor extends AbstractEntryVisitor {
        private String contentTypeAndCharset;

        public ListPageVisitor(PrintWriter out, RequestContext request, Entry firstEntry, String contentTypeAndCharset) {
            super(out, request, firstEntry, null);
            this.contentTypeAndCharset = contentTypeAndCharset;
        }

        public void visitEntry(Template dirTemplate, Template fileTemplate) {
            if (entry.isDirectory()) {
                dirTemplate.accept(new DirEntryVisitor(out(), request, entry, null, dirTemplate, fileTemplate));
            } else {
                fileTemplate.accept(new FileEntryVisitor(out(), request, entry, null));
            }
        }

        public void visitContentTypeAndCharset() {
            out().append(contentTypeAndCharset);
        }

        public void visitMenuHead() throws Exception {
            if (menuProvider != null) {
                menuProvider.renderMenuHead(request);
            }
        }

        public void visitMenu() throws Exception {
            if (menuProvider != null) {
                menuProvider.renderMenu(request);
            }
        }
    }

    @SuppressWarnings("unused")
    private class DirEntryVisitor extends AbstractEntryVisitor {
        private final Template dirTemplate;
        private final Template fileTemplate;

        public DirEntryVisitor(PrintWriter out, RequestContext request, Entry entry, String lastEntryPath,
                               Template dirTemplate, Template fileTemplate) {
            super(out, request, entry, lastEntryPath);
            this.dirTemplate = dirTemplate;
            this.fileTemplate = fileTemplate;
        }

        public void visitLink(Template link) {
            FallbackTextWriter<PrintWriter> v = new FallbackTextWriter<PrintWriter>(out());
            int lastEntryPathLength = 0;

            if (lastEntryPath == null) {
                v.context().put("entryUrl", request.getResourceURL("/"));
                v.context().put("entryName", "all");

                link.accept(v);
            } else {
                lastEntryPathLength = lastEntryPath.length();
            }

            String path = entry.getPath();

            for (int i = path.indexOf("/", lastEntryPathLength); i >= 0; i = path.indexOf("/", i + 1)) {
                Entry parentEntry = getEntry(path.substring(0, i + 1));

                v.context().put("entryUrl", request.getResourceURL(parentEntry.getPath()));
                v.context().put("entryName", parentEntry.getName().replaceFirst("/$", ""));

                link.accept(v);
            }
        }

        public void visitSubEntries(Template subEntriesTemplate) {
            if (!entry.getSubEntries().isEmpty()) {
                subEntriesTemplate.accept(this);
            }
        }

        public void visitSubEntryRecursive() {
            for (Entry subEntry : entry.getSubEntries()) {
                Entry activeEntry = subEntry;

                while (activeEntry.isDirectory() && activeEntry.getSubEntries().size() == 1) {
                    Entry theOnlySubEntry = activeEntry.getSubEntries().iterator().next();

                    if (theOnlySubEntry.isDirectory()) {
                        activeEntry = theOnlySubEntry;
                    } else {
                        break;
                    }
                }

                if (activeEntry.isDirectory()) {
                    dirTemplate.accept(new DirEntryVisitor(out(), request, activeEntry, entry.getPath(), dirTemplate,
                            fileTemplate));
                } else {
                    fileTemplate.accept(new FileEntryVisitor(out(), request, activeEntry, entry.getPath()));
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private class FileEntryVisitor extends AbstractEntryVisitor {
        private final Schema schema;
        private final String namespace;

        public FileEntryVisitor(PrintWriter out, RequestContext request, Entry entry, String lastEntryPath) {
            super(out, request, entry, lastEntryPath);
            this.schema = entry.getSchema();
            this.namespace = schema != null ? schema.getTargetNamespace() : null;
        }

        public void visitCheckbox(Template chkbox) {
            if (namespace != null) {
                chkbox.accept(this);
            }
        }

        public void visitNamespace(Template ns, Template noNs) {
            if (namespace != null) {
                ns.accept(this);
            } else {
                noNs.accept(this);
            }
        }

        public void visitNsPrefix() {
            out().append(schema.getNamespacePrefix());
        }

        public void visitNs() {
            out().append(namespace);
        }

        public void visitDescription(Template descTemplate) {
            if (schema != null && schema.getSourceDescription() != null) {
                descTemplate.accept(this);
            }
        }

        public void visitDesc() {
            out().append(schema.getSourceDescription());
        }
    }
}
