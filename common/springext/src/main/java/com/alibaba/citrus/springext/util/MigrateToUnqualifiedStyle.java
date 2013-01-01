package com.alibaba.citrus.springext.util;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
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
import java.util.Map;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.impl.SpringExtSchemaSet;
import com.alibaba.citrus.springext.support.SchemaUtil;
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
public class MigrateToUnqualifiedStyle {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private File[]             sources;
    private SpringExtSchemaSet schemas;

    /** 必须运行在适当的classpath下，否则不能取得configuration points。 */
    public static void main(String[] args) {
        File[] sources = new File[args.length];

        for (int i = 0; i < args.length; i++) {
            sources[i] = new File(args[i]).getAbsoluteFile();
        }

        MigrateToUnqualifiedStyle m = new MigrateToUnqualifiedStyle();
        m.setSources(sources);
        m.convert();
    }

    public void setSources(File[] sources) {
        this.sources = sources;
    }

    public void convert() {
        if (!isEmptyArray(sources)) {
            schemas = new SpringExtSchemaSet();

            for (File source : sources) {
                convert(source);
            }
        }
    }

    private void convert(File source) {
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

        boolean modified = new Converter(doc).doConvert();

        if (modified) {
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
                tmpFile = File.createTempFile(fileName, ext, dir);
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
                File backupFile;

                for (int i = 0; ; i++) {
                    backupFile = new File(dir, fileName + ".backup" + (i == 0 ? "" : "_" + i) + ext);

                    if (!backupFile.exists()) {
                        break;
                    }
                }

                source.renameTo(backupFile);
                tmpFile.renameTo(source);

                log.info("Converted file: {}", getRelativePath(source));
            }
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
        return "http://www.springframework.org/schema/beans".equals(root.getNamespace().getURI())
               && "beans".equals(root.getName());
    }

    private boolean isConfigurationPointNamespace(String namespaceURI) {
        return schemas.getConfigurationPoints().getConfigurationPointByNamespaceUri(namespaceURI) != null;
    }

    private String getRelativePath(File f) {
        return FileUtil.getRelativePath(new File("").getAbsolutePath(), f.getAbsolutePath());
    }

    private class Converter {
        private final Element root;
        private final LinkedList<String>     namespaceURIStack = createLinkedList();
        private final Map<String, Namespace> namespaces        = createTreeMap();
        private       boolean                modified          = false;

        private Converter(Document doc) {
            this.root = doc.getRootElement();
        }

        public boolean doConvert() {
            accept(root);
            return modified;
        }

        private void accept(Element element) {
            String namespaceURI = trimToNull(element.getNamespace().getURI());

            // 先删除所有的ns声明，到最后再加回去。
            for (Iterator<?> i = element.declaredNamespaces().iterator(); i.hasNext(); ) {
                Namespace declaredNs = (Namespace) i.next();

                if (isConfigurationPointNamespace(declaredNs.getURI())) {
                    i.remove();

                    if (element != root) {
                        modified = true;
                    }

                    if (!namespaces.containsKey(declaredNs.getURI())) {
                        Namespace ns = declaredNs;
                        String uri = ns.getURI();

                        if (isEmpty(ns.getPrefix())) {
                            String prefix = getNamespacePrefix(
                                    schemas.getConfigurationPoints().getConfigurationPointByNamespaceUri(uri).getPreferredNsPrefix(), uri);

                            ns = Namespace.get(prefix, uri);
                            modified = true;
                        }

                        namespaces.put(uri, ns);
                    }
                }
            }

            // 如果当前没有指定ns，或不是configuration point ns
            if (!isConfigurationPointNamespace(namespaceURI)) {
                acceptSubElements(element);
            }

            // 如果当前ns和context ns不同
            else if (!isEquals(namespaceURI, getContextNamespaceURI())) {
                try {
                    namespaceURIStack.push(namespaceURI);
                    setNamespacePrefix(element, namespaceURI);
                    acceptSubElements(element);
                } finally {
                    namespaceURIStack.pop();
                }
            }

            // 如果当前ns和context ns相同
            else {
                setNamespacePrefix(element, null);
                acceptSubElements(element);
                modified = true;
            }

            // 将所有ns加回到root element。
            if (element == root) {
                for (Namespace ns : namespaces.values()) {
                    element.add(ns);
                }

                formatSchemaLocations();
            }
        }

        private void formatSchemaLocations() {
            Namespace xsi = getXsiNs();

            QName schemaLocationQName = QName.get("schemaLocation", xsi);
            Attribute attr = root.attribute(schemaLocationQName);
            String value = attr != null ? attr.getText() : null;
            Map<String, String> schemaLocations = parseSchemaLocation(value);
            String locationPrefix = getLocationPrefix(schemaLocations);

            for (String namespaceURI : namespaces.keySet()) {
                if (!schemaLocations.containsKey(namespaceURI)) {
                    try {
                        schemaLocations.put(namespaceURI, locationPrefix + schemas.getNamespaceMappings().get(namespaceURI).iterator().next().getName());
                        modified = true;
                    } catch (Exception ignored) {
                    }
                }
            }

            StringBuilder buf = new StringBuilder();
            String leadingSpaces = "             ";
            String indent = "    ";
            String newLine = "\n";

            buf.append(newLine);

            for (Map.Entry<String, String> entry : schemaLocations.entrySet()) {
                buf.append(leadingSpaces).append(indent).append(entry.getKey()).append(" ").append(entry.getValue()).append(newLine);
            }

            buf.append(leadingSpaces);

            value = buf.toString();

            if (attr != null) {
                root.remove(attr);
            }

            root.addAttribute(schemaLocationQName, value);
        }

        private String getLocationPrefix(Map<String, String> schemaLocations) {
            for (Map.Entry<String, String> entry : schemaLocations.entrySet()) {
                String uri = entry.getKey();
                String location = entry.getValue();

                for (Schema schema : schemas.getNamespaceMappings().get(uri)) {
                    if (location.endsWith(schema.getName())) {
                        return location.substring(0, location.length() - schema.getName().length());
                    }
                }
            }

            return "http://localhost:8080/schema/";
        }

        private Map<String, String> parseSchemaLocation(String value) {
            Map<String, String> locations = createTreeMap();
            value = trimToNull(value);

            if (value != null) {
                String[] values = value.split("\\s+");

                for (int i = 0; i < values.length - 1; i += 2) {
                    String uri = trimToNull(values[i]);
                    String location = trimToNull(values[i + 1]);

                    if (uri != null && location != null) {
                        locations.put(uri, location);
                    }
                }
            }

            return locations;
        }

        private Namespace getXsiNs() {
            Namespace xsi = null;

            for (Object o : root.declaredNamespaces()) {
                Namespace ns = (Namespace) o;

                if ("http://www.w3.org/2001/XMLSchema-instance".equals(ns.getURI())) {
                    xsi = ns;
                    break;
                }
            }

            if (xsi == null) {
                xsi = DocumentHelper.createNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                root.add(xsi);
            }

            return xsi;
        }

        private void setNamespacePrefix(Element element, String namespaceURI) {
            String prefix = null;

            if (namespaceURI != null && namespaces.containsKey(namespaceURI)) {
                prefix = namespaces.get(namespaceURI).getPrefix();
            }

            if (prefix == null || !prefix.equals(element.getNamespacePrefix())) {
                element.setQName(QName.get(element.getName(), prefix, namespaceURI));
                modified = true;
            }
        }

        private void acceptSubElements(Element element) {
            for (Object subElement : element.elements()) {
                accept((Element) subElement);
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
