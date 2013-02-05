/*
 * Copyright (c) 2002-2013 Alibaba Group Holding Limited.
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

package com.alibaba.citrus.springext.util;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.alibaba.citrus.logconfig.LogConfigurator;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.ContributionType;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SchemaUtil;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet;
import com.alibaba.citrus.util.FileUtil;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将一个webx配置文件转换成unqualifed风格。
 *
 * @author Michael Zhou
 */
public class ConvertToUnqualifiedStyle {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final File[]             sources;
    private final SpringExtSchemaSet schemas;
    private final boolean            forceConvert;
    private final boolean            backup;
    private       int                convertedCount;

    /** 必须运行在适当的classpath下，否则不能取得configuration points。 */
    public static void main(String[] args) {
        File[] sources = new File[args.length];

        for (int i = 0; i < args.length; i++) {
            sources[i] = new File(args[i]).getAbsoluteFile();
        }

        new ConvertToUnqualifiedStyle(sources).convert();
    }

    public ConvertToUnqualifiedStyle(File[] sources) {
        this(sources, false, true);
    }

    public ConvertToUnqualifiedStyle(File[] sources, boolean forceConvert, boolean backup) {
        LogConfigurator.getConfigurator().configureDefault();

        this.sources = sources;
        this.schemas = new SpringExtSchemaSet();
        this.forceConvert = forceConvert;
        this.backup = backup;
    }

    public void convert() {
        if (!isEmptyArray(sources)) {
            for (File source : sources) {
                convert(source);
            }
        }

        log.info("Converted {} files.", convertedCount);
    }

    private final Pattern backupFilePattern = Pattern.compile("\\.bak$");

    private void convert(File source) {
        if (backupFilePattern.matcher(source.getName()).find()) {
            return; // skip backup file
        }

        Document doc;

        try {
            doc = SchemaUtil.readDocument(new FileInputStream(source), source.getAbsolutePath(), true);
        } catch (Exception e) {
            log.warn("Failed to read file {}: {}", getRelativePath(source), e.getMessage());
            return;
        }

        if (!isSpringConfigurationFile(doc)) {
            return;
        }

        log.info("Converting file: {}", getRelativePath(source));

        boolean modified = new Converter(doc, schemas).convert();

        if (modified || forceConvert) {
            File dir = source.getParentFile();
            String fileName = source.getName();
            int index = fileName.lastIndexOf(".");
            String ext = "";

            if (index >= 0) {
                ext = fileName.substring(index);
                fileName = fileName.substring(0, index);
            }

            File tmpFile = null;
            FileOutputStream fos = null;
            boolean failed = false;

            try {
                tmpFile = File.createTempFile(fileName + "_tmp_", ext, dir);
                fos = new FileOutputStream(tmpFile);

                writeDocument(doc, fos);
            } catch (IOException e) {
                log.warn("Failed to write to {}: {}", getRelativePath(tmpFile), e.getMessage());
                failed = true;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ignored) {
                    }
                }
            }

            if (failed) {
                tmpFile.delete();
            } else {
                File backupFile = null;

                if (backup) {
                    for (int i = 0; ; i++) {
                        backupFile = new File(dir, fileName + (i == 0 ? "" : "_" + i) + ext + ".bak");

                        if (!backupFile.exists()) {
                            break;
                        }
                    }

                    source.renameTo(backupFile);
                    log.info("  ... converted, original content saved as {}", getRelativePath(backupFile));
                } else {
                    source.delete();
                    log.info("  ... converted");
                }

                tmpFile.renameTo(source);

                convertedCount++;
            }
        } else {
            log.info("  ... skipped");
        }
    }

    private static void writeDocument(Document doc, OutputStream stream) throws IOException {
        String charset = "UTF-8";
        Writer writer = new OutputStreamWriter(stream, charset);

        OutputFormat format = new OutputFormat();

        format.setEncoding(charset);

        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.write(doc);
        xmlWriter.flush();
    }

    /** Root element是否为<code>&lt;beans:bean&gt;</code>？ */
    private boolean isSpringConfigurationFile(Document doc) {
        Element root = doc.getRootElement();
        return "http://www.springframework.org/schema/beans".equals(root.getNamespaceURI())
               && "beans".equals(root.getName());
    }

    private String getRelativePath(File f) {
        return FileUtil.getRelativePath(new File("").getAbsolutePath(), f.getAbsolutePath());
    }

    public static class Converter {
        private final SpringExtSchemaSet schemas;
        private final Element            root;
        private final LinkedList<String>     namespaceURIStack            = createLinkedList();
        private final Map<String, Namespace> configurationPointNamespaces = createTreeMap();
        private final Map<String, Namespace> allSchemaNamespaces          = createTreeMap();
        private       boolean                modified                     = false;

        public Converter(Document doc, SpringExtSchemaSet schemas) {
            this.root = doc.getRootElement();
            this.schemas = schemas;
        }

        public boolean convert() {
            visit(root);
            return modified;
        }

        private void visit(Element element) {
            String namespaceURI = trimToNull(element.getNamespaceURI());
            List<Namespace> nsToBeRemoved = createLinkedList();

            for (Iterator<?> i = element.declaredNamespaces().iterator(); i.hasNext(); ) {
                Namespace ns = (Namespace) i.next();

                if (isConfigurationPointNamespace(ns.getURI())) {
                    nsToBeRemoved.add(ns);

                    if (element != root) {
                        modified = true;
                    }

                    if (!configurationPointNamespaces.containsKey(ns.getURI())) {
                        if (isEmpty(ns.getPrefix())) {
                            String prefix = getNamespacePrefix(
                                    schemas.getConfigurationPoints().getConfigurationPointByNamespaceUri(ns.getURI()).getPreferredNsPrefix(), ns.getURI());

                            ns = Namespace.get(prefix, ns.getURI());
                            modified = true;
                        }

                        configurationPointNamespaces.put(ns.getURI(), ns);
                        allSchemaNamespaces.put(ns.getURI(), ns);
                    }
                } else if (schemas.getNamespaceMappings().containsKey(ns.getURI())) {
                    if (!allSchemaNamespaces.containsKey(ns.getURI())) {
                        allSchemaNamespaces.put(ns.getURI(), ns);
                    }
                }
            }

            // 如果当前没有指定ns，或不是configuration point ns
            if (!isConfigurationPointNamespace(namespaceURI)) {
                visitSubElements(element);
            }

            // 从这里开始，ns不可能是null，并且代表一个configuration point ns。
            // 如果当前ns和context ns不同
            else if (!namespaceURI.equals(getContextNamespaceURI())
                     || isContributionElement(namespaceURI, element.getName())) {
                // 此namespaceURI应该已经在某处被定义了，所以namespaces.get(namespaceURI)不应为空。
                Namespace ns = assertNotNull(configurationPointNamespaces.get(namespaceURI), "xmlns not defined for %s", namespaceURI);

                try {
                    namespaceURIStack.push(namespaceURI);
                    setNamespacePrefix(element, ns.getPrefix());
                    visitSubElements(element);
                } finally {
                    namespaceURIStack.pop();
                }
            }

            // 如果当前ns和context ns相同
            else {
                removeNamespace(element);
                visitSubElements(element);
                modified = true;
            }

            // 先删除所有的ns声明、attributes，一会儿排序以后再加回去。
            for (Namespace ns : nsToBeRemoved) {
                element.remove(ns);
            }

            // 将所有ns加回到root element。
            if (element == root) {
                Map<String, Namespace> otherNamespaces = createTreeMap();
                Map<String, Attribute> otherAttrs = createTreeMap();
                Namespace xsi = null;
                String schemaLocation = null;

                for (Iterator<?> i = element.declaredNamespaces().iterator(); i.hasNext(); ) {
                    Namespace ns = (Namespace) i.next();

                    if ("http://www.w3.org/2001/XMLSchema-instance".equals(ns.getURI())) {
                        xsi = ns;
                    } else if (!allSchemaNamespaces.containsKey(ns.getURI())) {
                        otherNamespaces.put(ns.getURI(), ns);
                    }

                    i.remove();
                }

                QName schemaLocationQName = QName.get("schemaLocation", xsi);

                for (Iterator<?> i = element.attributes().iterator(); i.hasNext(); ) {
                    Attribute attr = (Attribute) i.next();

                    if (schemaLocationQName.equals(attr.getQName())) {
                        schemaLocation = attr.getText();
                    } else {
                        otherAttrs.put(attr.getQualifiedName(), attr);
                    }

                    i.remove();
                }

                // xmlns:xsi
                if (xsi == null) {
                    xsi = DocumentHelper.createNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                }

                element.add(xsi);

                // namespaces in defined schemas
                for (Namespace ns : allSchemaNamespaces.values()) {
                    element.add(ns);
                }

                // other xmlns attrs
                for (Namespace ns : otherNamespaces.values()) {
                    element.add(ns);
                }

                // schema location
                element.addAttribute(schemaLocationQName, reformatSchemaLocations(schemaLocation));

                // other attrs
                for (Attribute attr : otherAttrs.values()) {
                    element.add(attr);
                }
            }
        }

        private boolean isContributionElement(String uri, String name) {
            ConfigurationPoint cp = schemas.getConfigurationPoints().getConfigurationPointByNamespaceUri(uri);

            if (cp != null) {
                return name.equals(cp.getDefaultElementName()) // default element
                       || cp.getContribution(name, ContributionType.BEAN_DEFINITION_PARSER) != null // contribution
                       || cp.getContribution(name, ContributionType.BEAN_DEFINITION_DECORATOR) != null; // contribution
            }

            return false;
        }

        private boolean isConfigurationPointNamespace(String namespaceURI) {
            return schemas.getConfigurationPoints().getConfigurationPointByNamespaceUri(namespaceURI) != null;
        }

        private String reformatSchemaLocations(String schemaLocation) {
            Map<String, String> schemaLocations = parseSchemaLocation(schemaLocation);
            String locationPrefix = guessLocationPrefix(schemaLocations, schemas);

            // 将缺少的schema location补充完整。
            for (String namespaceURI : allSchemaNamespaces.keySet()) {
                if (!schemaLocations.containsKey(namespaceURI)) {
                    try {
                        Set<Schema> schemaSet = schemas.getNamespaceMappings().get(namespaceURI);

                        if (schemaSet != null && schemaSet.size() > 0) {
                            schemaLocations.put(namespaceURI, locationPrefix + schemaSet.iterator().next().getName());
                            modified = true;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            return formatSchemaLocations(schemaLocations, root.getQualifiedName());
        }

        /** 将element的prefix改成统一的值，但不改变其namespace。 */
        private void setNamespacePrefix(Element element, String prefix) {
            assertNotNull(prefix, "prefix is null");

            if (!prefix.equals(element.getNamespacePrefix())) {
                element.setQName(QName.get(element.getName(), prefix, element.getNamespaceURI()));
                modified = true;
            }
        }

        /** 将element变成unqualified。 */
        private void removeNamespace(Element element) {
            if (!isEmpty(element.getNamespaceURI())) {
                element.setQName(QName.get(element.getName()));
                modified = true;
            }
        }

        private void visitSubElements(Element element) {
            for (Object subElement : element.elements()) {
                visit((Element) subElement);
            }
        }

        private String getContextNamespaceURI() {
            if (namespaceURIStack.isEmpty()) {
                return null;
            }

            return namespaceURIStack.peek();
        }
    }
}
