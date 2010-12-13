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
package com.alibaba.citrus.springext.support;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static javax.xml.XMLConstants.*;
import static org.dom4j.DocumentHelper.*;
import static org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.ConfigurationPointException;
import com.alibaba.citrus.springext.ConfigurationPoints;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;

/**
 * 处理schema和xml的工具。
 * 
 * @author Michael Zhou
 */
public class SchemaUtil {
    private static final String SPRINGEXT_BASE_URI = "http://www.alibaba.com/schema/springext/base";
    private static final String SPRINGEXT_BASE_XSD = "http://www.alibaba.com/schema/springext/springext-base.xsd";

    private final static Namespace XSD = DocumentHelper.createNamespace("xsd", W3C_XML_SCHEMA_NS_URI);
    private final static QName XSD_ANY = DocumentHelper.createQName("any", XSD);
    private final static QName XSD_IMPORT = DocumentHelper.createQName("import", XSD);
    private final static QName XSD_CHOICE = DocumentHelper.createQName("choice", XSD);
    private final static QName XSD_ELEMENT = DocumentHelper.createQName("element", XSD);
    private final static QName XSD_INCLUDE = DocumentHelper.createQName("include", XSD);

    /**
     * 读取dom。
     */
    public static Document readDocument(InputStream istream, String systemId, boolean close) throws DocumentException {
        SAXReader reader = new SAXReader(false);
        Document doc = null;

        try {
            doc = reader.read(istream, systemId);
        } finally {
            if (close && istream != null) {
                try {
                    istream.close();
                } catch (Exception e) {
                }
            }
        }

        return doc;
    }

    /**
     * 输出DOM。
     */
    public static String getDocumentText(Document doc, String charset) throws IOException {
        StringWriter writer = new StringWriter();
        writeDocument(doc, writer, charset);
        return writer.toString();
    }

    /**
     * 输出DOM。
     */
    public static byte[] getDocumentContent(Document doc, String charset) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeDocument(doc, baos, null);
        return baos.toByteArray();
    }

    /**
     * 输出DOM。
     */
    public static void writeDocument(Document doc, OutputStream stream, String charset) throws IOException {
        charset = defaultIfEmpty(trimToNull(charset), "UTF-8");
        writeDocument(doc, new OutputStreamWriter(stream, charset), charset);
    }

    /**
     * 输出DOM。
     */
    public static void writeDocument(Document doc, Writer writer, String charset) throws IOException {
        charset = defaultIfEmpty(trimToNull(charset), "UTF-8");

        OutputFormat format = OutputFormat.createPrettyPrint();

        format.setEncoding(charset);
        format.setIndent(true);
        format.setIndentSize(4);

        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.write(doc);
        xmlWriter.flush();
    }

    public static byte[] getConfigurationPointSchemaContent(ConfigurationPoint configurationPoint, String version) {
        try {
            return getDocumentContent(createConfigurationPointSchema(configurationPoint, version), null);
        } catch (Exception e) {
            throw new ConfigurationPointException("Could not generate XML Schema for configuration point "
                    + configurationPoint.getName(), e);
        }
    }

    /**
     * 将所有contributions的schema汇总成一个schema。
     */
    private static Document createConfigurationPointSchema(ConfigurationPoint configurationPoint, String version) {
        Document doc = createDocument();

        // <xsd:schema>
        Element schemaRoot = doc.addElement("xsd:schema");

        schemaRoot.addNamespace("xsd", W3C_XML_SCHEMA_NS_URI);
        schemaRoot.addNamespace("beans", BEANS_NAMESPACE_URI);
        schemaRoot.addNamespace("springext", SPRINGEXT_BASE_URI);
        schemaRoot.addNamespace("", configurationPoint.getNamespaceUri());

        schemaRoot.addAttribute("targetNamespace", configurationPoint.getNamespaceUri());
        schemaRoot.addAttribute("elementFormDefault", "qualified");

        // <xsd:include schemaLocation="contribution schema" />
        Set<String> includings = createTreeSet();

        for (Contribution contrib : configurationPoint.getContributions()) {
            Schema contribSchema = contrib.getSchemas().getVersionedSchema(version);

            if (contribSchema == null) {
                contribSchema = contrib.getSchemas().getMainSchema();
            }

            if (contribSchema != null) {
                includings.add(contribSchema.getName());
            }
        }

        for (String including : includings) {
            Element includeElement = schemaRoot.addElement("xsd:include");

            includeElement.addAttribute("schemaLocation", including);
        }

        if (configurationPoint.getDefaultElementName() != null) {
            //  <xsd:import namespace="http://www.springframework.org/schema/beans"
            //              schemaLocation="http://www.springframework.org/schema/beans/spring-beans.xsd" />
            Element importBeans = schemaRoot.addElement("xsd:import");
            importBeans.addAttribute("namespace", BEANS_NAMESPACE_URI);
            importBeans.addAttribute("schemaLocation", BEANS_NAMESPACE_URI + "/spring-beans.xsd");

            //  <xsd:import namespace="http://www.alibaba.com/schema/springext/base"
            //              schemaLocation="http://www.alibaba.com/schema/springext/springext-base.xsd" />
            Element importSpringextBase = schemaRoot.addElement("xsd:import");
            importSpringextBase.addAttribute("namespace", SPRINGEXT_BASE_URI);
            importSpringextBase.addAttribute("schemaLocation", SPRINGEXT_BASE_XSD);

            // <xsd:element name="defaultElementName" type="springext:referenceableBeanType" />
            Element element = schemaRoot.addElement("xsd:element");
            element.addAttribute("name", configurationPoint.getDefaultElementName());
            element.addAttribute("type", "springext:referenceableBeanType");
        }

        return doc;
    }

    /**
     * 取得contribution schema的内容，将其中引用其它configuration
     * point的element修改成具体的element名称。例如，将如下定义：
     * 
     * <pre>
     * &lt;xsd:any namespace="http://www.alibaba.com/schema/services/template/engines" /&gt;
     * </pre>
     * <p>
     * 修改成：
     * </p>
     * 
     * <pre>
     * &lt;xsd:choose xmlns:tpl-engines="http://www.alibaba.com/schema/services/template/engines"&gt;
     *   &lt;xsd:element ref="tpl-engines:velocity-engine" /&gt;
     *   &lt;xsd:element ref="tpl-engines:freemarker-engine" /&gt;
     *   &lt;xsd:element ref="tpl-engines:jsp-engine" /&gt;
     * &lt;/xsd:choose&gt;
     * </pre>
     * <p>
     */
    public static byte[] getContributionSchemaContent(InputStream istream, String systemId, boolean close,
                                                      ConfigurationPoints cps, ConfigurationPoint thisCp)
            throws DocumentException, IOException {
        Document doc = readDocument(istream, systemId, close);

        new ContributionSchemaTransformer(doc, cps, thisCp).transform();

        return getDocumentContent(doc, null);
    }

    private static class ContributionSchemaTransformer {
        private final Document doc;
        private final ConfigurationPoints cps;
        private final ConfigurationPoint thisCp;
        private Element root;
        private Map<String, ConfigurationPoint> importings = createHashMap();

        public ContributionSchemaTransformer(Document doc, ConfigurationPoints cps, ConfigurationPoint thisCp) {
            this.doc = doc;
            this.cps = cps;
            this.thisCp = thisCp;
        }

        public void transform() {
            root = doc.getRootElement();

            if (!W3C_XML_SCHEMA_NS_URI.equals(root.getNamespaceURI()) || !"schema".equals(root.getName())) {
                return;
            }

            visitElement(root);

            // 避免加入重复的import
            @SuppressWarnings("unchecked")
            List<Element> importElements = root.elements(XSD_IMPORT);

            for (Element importElement : importElements) {
                if (importElement.attribute("namespace") != null) {
                    importings.remove(importElement.attribute("namespace").getValue());
                }
            }

            // 加入imports，但避免加入当前schema所在的configurtion point schema
            @SuppressWarnings("unchecked")
            List<Element> rootElements = root.elements();
            int importIndex = 0;

            for (ConfigurationPoint cp : importings.values()) {
                if (thisCp != cp) {
                    Element importElement = DocumentHelper.createElement(XSD_IMPORT);
                    importElement.addAttribute("namespace", cp.getNamespaceUri());
                    importElement.addAttribute("schemaLocation", cp.getSchemas().getMainSchema().getName()); // XXX main or versioned?

                    rootElements.add(importIndex++, importElement);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void visitElement(Element element) {
            List<Element> elements = element.elements();
            visitElements(elements);
        }

        private void visitElements(List<Element> elements) {
            List<Integer> indexes = createLinkedList();
            int index = 0;

            for (Element subElement : elements) {
                if (subElement.getQName().equals(XSD_ANY) && subElement.attribute("namespace") != null) {
                    String ns = subElement.attribute("namespace").getValue();
                    ConfigurationPoint cp = cps.getConfigurationPointByNamespaceUri(ns);

                    if (cp != null) {
                        indexes.add(index);
                        importings.put(ns, cp);
                    }
                } else {
                    visitElement(subElement);
                }

                index++;
            }

            for (Integer i : indexes) {
                visitAnyElement(elements, i);
            }
        }

        private void visitAnyElement(List<Element> elements, int index) {
            Element anyElement = elements.get(index);
            String ns = anyElement.attribute("namespace").getValue();
            ConfigurationPoint cp = cps.getConfigurationPointByNamespaceUri(ns);

            if (cp != null) {
                Element choiceElement = DocumentHelper.createElement(XSD_CHOICE);
                String nsPrefix = getNamespacePrefix(cp.getPreferredNsPrefix(), ns);

                // <xsd:schema xmlns:prefix="ns">
                // 注意：必须将ns定义加在顶层element，否则低版本的xerces会报错。
                root.addNamespace(nsPrefix, ns);

                // <xsd:choice minOccurs="?" maxOccurs="?" /> 
                if (anyElement.attribute("minOccurs") != null) {
                    choiceElement.addAttribute("minOccurs", anyElement.attribute("minOccurs").getValue());
                }

                if (anyElement.attribute("maxOccurs") != null) {
                    choiceElement.addAttribute("maxOccurs", anyElement.attribute("maxOccurs").getValue());
                }

                // <xsd:element ref="prefix:contrib" />
                for (Contribution contrib : cp.getContributions()) {
                    Element elementElement = DocumentHelper.createElement(XSD_ELEMENT);
                    elementElement.addAttribute("ref", nsPrefix + ":" + contrib.getName());
                    choiceElement.add(elementElement);
                }

                // <xsd:element ref="prefix:defaultName" />
                if (cp.getDefaultElementName() != null) {
                    Element elementElement = DocumentHelper.createElement(XSD_ELEMENT);
                    elementElement.addAttribute("ref", nsPrefix + ":" + cp.getDefaultElementName());
                    choiceElement.add(elementElement);
                }

                // 用choice取代any
                elements.set(index, choiceElement);
            }
        }
    }

    /**
     * 修改schema，除去所有的includes。
     */
    public static byte[] getSchemaContentWithoutIncludes(Schema schema) throws IOException {
        Document doc = schema.getDocument();
        Element root = doc.getRootElement();

        // <xsd:schema>
        if (W3C_XML_SCHEMA_NS_URI.equals(root.getNamespaceURI()) && "schema".equals(root.getName())) {
            // for each <xsd:include>
            for (Iterator<?> i = root.elementIterator(XSD_INCLUDE); i.hasNext();) {
                i.next();
                i.remove();
            }
        }

        return getDocumentContent(doc, null);
    }

    /**
     * 修改schema，添加间接依赖的includes。
     */
    public static byte[] getSchemaContentWithIndirectIncludes(Schema schema, Map<String, Schema> includes)
            throws IOException {
        Document doc = schema.getDocument();
        Element root = doc.getRootElement();

        root.addNamespace("xsd", W3C_XML_SCHEMA_NS_URI);

        // <xsd:schema>
        if (W3C_XML_SCHEMA_NS_URI.equals(root.getNamespaceURI()) && "schema".equals(root.getName())) {
            Namespace xsd = DocumentHelper.createNamespace("xsd", W3C_XML_SCHEMA_NS_URI);
            QName includeName = DocumentHelper.createQName("include", xsd);

            // for each <xsd:include>
            for (Iterator<?> i = root.elementIterator(includeName); i.hasNext();) {
                i.next();
                i.remove();
            }

            // 添加includes 
            @SuppressWarnings("unchecked")
            List<Node> nodes = root.elements();
            int i = 0;

            for (Schema includedSchema : includes.values()) {
                Element includeElement = DocumentHelper.createElement("xsd:include");
                nodes.add(i++, includeElement);

                includeElement.addAttribute("schemaLocation", includedSchema.getName());
            }
        }

        return getDocumentContent(doc, null);
    }

    public static String getNamespacePrefix(String preferredNsPrefix, String targetNamespace) {
        if (preferredNsPrefix != null) {
            return preferredNsPrefix;
        }

        String ns = targetNamespace;

        if (ns != null) {
            return ns.substring(ns.lastIndexOf("/") + 1);
        }

        return null;
    }
}
