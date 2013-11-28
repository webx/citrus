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

package com.alibaba.citrus.springext.export;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.io.StreamUtil.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.Schema.Transformer;
import com.alibaba.citrus.springext.Schemas;
import com.alibaba.citrus.springext.support.SchemaSet;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 装载和分析schemas，并输出到任意输出流。
 *
 * @author Michael Zhou
 */
public class SchemaExporter {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final Entries   entries;
    private final SchemaSet schemas;

    public SchemaExporter() {
        this(new SpringExtSchemaSet());
    }

    public SchemaExporter(Schemas... schemasList) {
        this.entries = new Entries();
        this.schemas = SchemaSet.getInstance(schemasList);

        for (Schema schema : schemas.getNamedMappings().values()) {
            this.entries.put(schema.getName(), new Entry(schema.getName(), schema));
        }
    }

    private Entries getEntries() {
        return entries;
    }

    public Entry getRootEntry() {
        return getEntries().getRoot();
    }

    public Entry getEntry(String path) {
        return getEntries().get(path);
    }

    public void writeTo(Writer out, Entry entry, String charset) throws IOException {
        writeTo(out, entry, charset, (String) null);
    }

    public void writeTo(Writer out, Entry entry, String charset, String uriPrefix) throws IOException {
        writeTo(out, entry, charset, uriPrefix == null ? null : getAddPrefixTransformer(schemas, uriPrefix));
    }

    private void writeTo(Writer out, Entry entry, String charset, Transformer transformer) throws IOException {
        writeText(entry.getSchema().getText(charset, transformer), out, true);
    }

    /** 代表一个schema文件结点。 */
    public static final class Entry {
        private final String             path;
        private final String             name;
        private final boolean            directory;
        private final boolean            root;
        private final Schema             schema;
        private final Map<String, Entry> subEntries;

        /** 创建特殊的root entry。 */
        private Entry() {
            this.path = "";
            this.name = "";
            this.directory = true;
            this.root = true;
            this.schema = null;
            this.subEntries = createTreeMap();
        }

        public Entry(String path) {
            this(path, null);
        }

        public Entry(String path, Schema schema) {
            this.path = assertNotNull(trimToNull(path), "path");
            this.directory = path.endsWith("/");

            int fromIndex = this.directory ? path.length() - 2 : path.length();

            this.name = path.substring(path.lastIndexOf("/", fromIndex) + 1);
            this.root = false;
            this.schema = schema;
            this.subEntries = createTreeMap();

            if (this.directory) {
                assertNull(schema, "schema");
            } else {
                assertNotNull(schema, "schema");
            }
        }

        public String getPath() {
            return path;
        }

        public String getId() {
            if (isRoot()) {
                return "ROOT";
            } else {
                return path.replaceFirst("\\.[^-\\./]*$|/+$", "").replace('/', '-').replace('.', '-');
            }
        }

        public String getName() {
            return name;
        }

        public boolean isDirectory() {
            return directory;
        }

        public boolean isRoot() {
            return root;
        }

        public Collection<Entry> getSubEntries() {
            return subEntries.values();
        }

        public Schema getSchema() {
            return schema;
        }

        public boolean containsSchemaWithTargetNamespace() {
            boolean hasNs = false;

            if (isDirectory()) {
                for (Entry subEntry : getSubEntries()) {
                    if (subEntry.containsSchemaWithTargetNamespace()) {
                        hasNs = true;
                        break;
                    }
                }
            } else if (getSchema() != null) {
                hasNs = getSchema().getTargetNamespace() != null;
            }

            return hasNs;
        }

        private final static String PREFIX_ITEM             = "+---";
        private final static String PREFIX_LAST_ITEM        = "\\---";
        private final static String INDENT_BEFORE_LAST_ITEM = "|   ";
        private final static String INDENT_AFTER_LAST_ITEM  = "    ";

        public String tree() {
            StringWriter sw = new StringWriter();

            try {
                tree(sw);
            } catch (IOException e) {
                unexpectedException(e);
            }

            return sw.toString();
        }

        public void tree(Appendable buf) throws IOException {
            tree(buf, "");
        }

        public void tree(Appendable buf, String prefix) throws IOException {
            tree(buf, prefix, prefix);
        }

        private void tree(Appendable buf, String prefix, String indent) throws IOException {
            buf.append(prefix).append(getName()).append("\n");

            for (Iterator<Entry> i = subEntries.values().iterator(); i.hasNext(); ) {
                Entry subEntry = i.next();
                String subPrefix;
                String subIndent;

                if (i.hasNext()) {
                    subPrefix = indent + PREFIX_ITEM;
                    subIndent = indent + INDENT_BEFORE_LAST_ITEM;
                } else {
                    subPrefix = indent + PREFIX_LAST_ITEM;
                    subIndent = indent + INDENT_AFTER_LAST_ITEM;
                }

                subEntry.tree(buf, subPrefix, subIndent);
            }
        }

        /** Files sort by name without extension, then directories. */
        private String getSortKey() {
            String name = getName();

            if (!isDirectory()) {
                int slashIndex = name.lastIndexOf("/");
                int dotIndex = name.lastIndexOf(".");

                if (slashIndex < dotIndex && dotIndex >= 0) {
                    return "0-" + name.substring(0, dotIndex);
                }

                return "0-" + name;
            }

            return "1-" + name;
        }

        @Override
        public String toString() {
            return root ? "/" : path;
        }
    }

    private final class Entries extends HashMap<String, SchemaExporter.Entry> {
        private static final long serialVersionUID = -4000525580274040823L;

        public Entries() {
            super.put("", new SchemaExporter.Entry());
        }

        public SchemaExporter.Entry getRoot() {
            return get("");
        }

        @Override
        public SchemaExporter.Entry put(String path, SchemaExporter.Entry entry) {
            assertTrue(path.equals(entry.getPath()));

            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            String parentPath = path.substring(0, path.lastIndexOf("/") + 1);
            SchemaExporter.Entry parentEntry = get(parentPath);

            if (parentEntry == null) {
                parentEntry = new SchemaExporter.Entry(parentPath);
                this.put(parentPath, parentEntry); // recursively
            }

            parentEntry.subEntries.put(entry.getSortKey(), entry);

            SchemaExporter.Entry old = super.put(entry.getPath(), entry);

            log.trace("Added entry: {}", entry.getPath());

            return old;
        }
    }
}
