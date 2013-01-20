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

package com.alibaba.citrus.springext.support;

import static com.alibaba.citrus.util.Assert.*;
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

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.ConfigurationPoints;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ResourceResolver;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.Schema.Transformer;
import com.alibaba.citrus.springext.impl.ConfigurationPointImpl;
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

/**
 * 处理schema和xml的工具。
 *
 * @author Michael Zhou
 */
public class SchemaUtil {
    private static final String SPRINGEXT_BASE_URI = "http://www.alibaba.com/schema/springext/base";
    private static final String SPRINGEXT_BASE_XSD = "http://www.alibaba.com/schema/springext/springext-base.xsd";

    private final static Namespace XSD         = DocumentHelper.createNamespace("xsd", W3C_XML_SCHEMA_NS_URI);
    private final static QName     XSD_ANY     = DocumentHelper.createQName("any", XSD);
    private final static QName     XSD_IMPORT  = DocumentHelper.createQName("import", XSD);
    private final static QName     XSD_CHOICE  = DocumentHelper.createQName("choice", XSD);
    private final static QName     XSD_ELEMENT = DocumentHelper.createQName("element", XSD);
    private final static QName     XSD_INCLUDE = DocumentHelper.createQName("include", XSD);

    /** 读取dom。 */
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
                    // ignored
                }
            }
        }

        return doc;
    }

    /** 输出DOM。 */
    public static String getDocumentText(Document doc, String charset) {
        StringWriter writer = new StringWriter();

        try {
            writeDocument(doc, writer, charset);
        } catch (IOException e) {
            // 不会发生
            unexpectedException(e);
        }

        return writer.toString();
    }

    /** 输出DOM。 */
    public static byte[] getDocumentContent(Document doc) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            writeDocument(doc, baos, null);
        } catch (IOException e) {
            unexpectedException(e); // 此调用不太可能报IO错
        }

        return baos.toByteArray();
    }

    /** 输出DOM。 */
    public static void writeDocument(Document doc, OutputStream stream, String charset) throws IOException {
        charset = defaultIfEmpty(trimToNull(charset), "UTF-8");
        writeDocument(doc, new OutputStreamWriter(stream, charset), charset);
    }

    /** 输出DOM。 */
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

    /** 将所有contributions的schema汇总成一个schema。 */
    public static Document createConfigurationPointSchema(ConfigurationPoint configurationPoint, String version) {
        Document doc = createDocument();

        // <xsd:schema>
        Element schemaRoot = doc.addElement("xsd:schema", W3C_XML_SCHEMA_NS_URI);

        schemaRoot.addNamespace("xsd", W3C_XML_SCHEMA_NS_URI);
        schemaRoot.addNamespace("beans", BEANS_NAMESPACE_URI);
        schemaRoot.addNamespace("springext", SPRINGEXT_BASE_URI);
        schemaRoot.addNamespace("", configurationPoint.getNamespaceUri());

        schemaRoot.addAttribute("targetNamespace", configurationPoint.getNamespaceUri());

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
     * <p/>
     * <pre>
     * &lt;xsd:any namespace="http://www.alibaba.com/schema/services/template/engines" /&gt;
     * </pre>
     * <p>
     * 修改成：
     * </p>
     * <p/>
     * <pre>
     * &lt;xsd:choose xmlns:tpl-engines="http://www.alibaba.com/schema/services/template/engines"&gt;
     *   &lt;xsd:element ref="tpl-engines:velocity-engine" /&gt;
     *   &lt;xsd:element ref="tpl-engines:freemarker-engine" /&gt;
     *   &lt;xsd:element ref="tpl-engines:jsp-engine" /&gt;
     * &lt;/xsd:choose&gt;
     * </pre>
     * <p/>
     */
    private static abstract class AnyElementTransformer implements Transformer {
        protected final ConfigurationPoints cps;
        protected       Element             root;

        private final Map<String, ConfigurationPoint> importings = createHashMap();

        protected AnyElementTransformer(ConfigurationPoints cps) {
            this.cps = cps;
        }

        public final void transform(Document document, String systemId) {
            root = document.getRootElement();

            if (!W3C_XML_SCHEMA_NS_URI.equals(root.getNamespaceURI()) || !"schema".equals(root.getName())) {
                return;
            }

            visitRootElement();
            visitElement(root);

            // 避免加入重复的import
            @SuppressWarnings("unchecked")
            List<Element> importElements = root.elements(XSD_IMPORT);

            for (Element importElement : importElements) {
                if (importElement.attribute("namespace") != null) {
                    importings.remove(importElement.attribute("namespace").getValue());
                }
            }

            // 加入imports
            @SuppressWarnings("unchecked")
            List<Element> rootElements = root.elements();
            int importIndex = 0;

            for (ConfigurationPoint cp : importings.values()) {
                if (canAddImportFor(cp)) {
                    Element importElement = DocumentHelper.createElement(XSD_IMPORT);
                    importElement.addAttribute("namespace", cp.getNamespaceUri());
                    importElement.addAttribute("schemaLocation", cp.getSchemas().getMainSchema().getName()); // XXX main or versioned?

                    rootElements.add(importIndex++, importElement);
                }
            }
        }

        protected void visitRootElement() {
        }

        protected boolean canAddImportFor(ConfigurationPoint cp) {
            return true;
        }

        protected abstract void visitDependedConfigurationPoint(ConfigurationPoint cp);

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

                    visitDependedConfigurationPoint(cp);
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

    public static Transformer getContributionSchemaTransformer(ConfigurationPoints cps, final Contribution thisContrib) {
        return new AnyElementTransformer(cps) {
            private final ConfigurationPoint thisCp = thisContrib.getConfigurationPoint();

            @Override
            protected void visitRootElement() {
                // 为contribution schema添加默认namespace和targetNamespace的声明。
                // 由于contribution schema是被include到configuration point schema中的，所以这些声明不是必须的。
                // 但是Intellij IDEA似乎不能正确学习到contribution schema的namespace，除非加上这些声明。
                root.addNamespace("", thisCp.getNamespaceUri());
                root.addAttribute("targetNamespace", thisCp.getNamespaceUri());
            }

            /** 避免import当前schema所在的configurtion point schema。 */
            @Override
            protected boolean canAddImportFor(ConfigurationPoint cp) {
                return thisCp != cp;
            }

            @Override
            protected void visitDependedConfigurationPoint(ConfigurationPoint cp) {
                if (cp instanceof ConfigurationPointImpl) {
                    ((ConfigurationPointImpl) cp).addDependingContribution(thisContrib);
                }
            }
        };
    }

    public static interface AnyElementVisitor {
        void visitAnyElement(ConfigurationPoint cp);
    }

    public static Transformer getAnyElementTransformer(ConfigurationPoints cps, final AnyElementVisitor visitor) {
        return new AnyElementTransformer(cps) {
            @Override
            protected void visitDependedConfigurationPoint(ConfigurationPoint cp) {
                visitor.visitAnyElement(cp);
            }
        };
    }

    /** 修改schema，除去所有的includes。 */
    public static Transformer getTransformerWhoRemovesIncludes() {
        return new Transformer() {
            public void transform(Document document, String systemId) {
                Element root = document.getRootElement();

                // <xsd:schema>
                if (W3C_XML_SCHEMA_NS_URI.equals(root.getNamespaceURI()) && "schema".equals(root.getName())) {
                    // for each <xsd:include>
                    for (Iterator<?> i = root.elementIterator(XSD_INCLUDE); i.hasNext(); ) {
                        i.next();
                        i.remove();
                    }
                }
            }
        };
    }

    /** 修改schema，添加间接依赖的includes。 */
    public static Transformer getTransformerWhoAddsIndirectIncludes(final Map<String, Schema> includes) {
        return new Transformer() {
            public void transform(Document document, String systemId) {
                Element root = document.getRootElement();

                root.addNamespace("xsd", W3C_XML_SCHEMA_NS_URI);

                // <xsd:schema>
                if (W3C_XML_SCHEMA_NS_URI.equals(root.getNamespaceURI()) && "schema".equals(root.getName())) {
                    Namespace xsd = DocumentHelper.createNamespace("xsd", W3C_XML_SCHEMA_NS_URI);
                    QName includeName = DocumentHelper.createQName("include", xsd);

                    // for each <xsd:include>
                    for (Iterator<?> i = root.elementIterator(includeName); i.hasNext(); ) {
                        i.next();
                        i.remove();
                    }

                    // 添加includes
                    @SuppressWarnings("unchecked")
                    List<Node> nodes = root.elements();
                    int i = 0;

                    for (Schema includedSchema : includes.values()) {
                        Element includeElement = DocumentHelper.createElement(includeName);
                        nodes.add(i++, includeElement);

                        includeElement.addAttribute("schemaLocation", includedSchema.getName());
                    }
                }
            }
        };
    }

    /** 在所有可识别的URI上，加上指定前缀。 */
    public static Transformer getAddPrefixTransformer(final SchemaSet schemas, String prefix) {
        if (prefix != null) {
            if (!prefix.endsWith("/")) {
                prefix += "/";
            }
        }

        final String normalizedPrefix = prefix;

        return new Transformer() {
            public void transform(Document document, String systemId) {
                if (normalizedPrefix != null) {
                    Element root = document.getRootElement();

                    // <xsd:schema>
                    if (W3C_XML_SCHEMA_NS_URI.equals(root.getNamespaceURI()) && "schema".equals(root.getName())) {
                        Namespace xsd = DocumentHelper.createNamespace("xsd", W3C_XML_SCHEMA_NS_URI);
                        QName includeName = DocumentHelper.createQName("include", xsd);
                        QName importName = DocumentHelper.createQName("import", xsd);

                        // for each <xsd:include>
                        for (Iterator<?> i = root.elementIterator(includeName); i.hasNext(); ) {
                            Element includeElement = (Element) i.next();
                            String schemaLocation = trimToNull(includeElement.attributeValue("schemaLocation"));

                            if (schemaLocation != null) {
                                schemaLocation = getNewSchemaLocation(schemaLocation, null, systemId);

                                if (schemaLocation != null) {
                                    includeElement.addAttribute("schemaLocation", schemaLocation);
                                }
                            }
                        }

                        // for each <xsd:import>
                        for (Iterator<?> i = root.elementIterator(importName); i.hasNext(); ) {
                            Element importElement = (Element) i.next();
                            String schemaLocation = importElement.attributeValue("schemaLocation");
                            String namespace = trimToNull(importElement.attributeValue("namespace"));

                            if (schemaLocation != null || namespace != null) {
                                schemaLocation = getNewSchemaLocation(schemaLocation, namespace, systemId);

                                if (schemaLocation != null) {
                                    importElement.addAttribute("schemaLocation", schemaLocation);
                                }
                            }
                        }
                    }
                }
            }

            private String getNewSchemaLocation(String schemaLocation, String namespace, String systemId) {
                // 根据指定的schemaLocation判断
                if (schemaLocation != null) {
                    Schema schema = schemas.findSchema(schemaLocation);

                    if (schema != null) {
                        return normalizedPrefix + schema.getName();
                    } else {
                        return schemaLocation; // 返回原本的location，但可能是错误的！
                    }
                }

                // 再根据namespace判断
                if (namespace != null) {
                    Set<Schema> nsSchemas = schemas.getNamespaceMappings().get(namespace);

                    if (nsSchemas != null && !nsSchemas.isEmpty()) {
                        // 首先，在所有相同ns的schema中查找版本相同的schema。
                        String versionedExtension = getVersionedExtension(systemId);

                        if (versionedExtension != null) {
                            for (Schema schema : nsSchemas) {
                                if (schema.getName().endsWith(versionedExtension)) {
                                    return normalizedPrefix + schema.getName();
                                }
                            }
                        }

                        // 其次，选择第一个默认的schema，其顺序是：beans.xsd、beans-2.5.xsd、beans-2.0.xsd
                        return normalizedPrefix + nsSchemas.iterator().next().getName();
                    }
                }

                return null;
            }

            /** 对于spring-aop-2.5.xsd取得-2.5.xsd。 */
            private String getVersionedExtension(String systemId) {
                if (systemId != null) {
                    int dashIndex = systemId.lastIndexOf("-");
                    int slashIndex = systemId.lastIndexOf("/");

                    if (dashIndex > slashIndex) {
                        return systemId.substring(dashIndex);
                    }
                }

                return null;
            }
        };
    }

    /** 将内部element/attribute设置了不需要namespace。 */
    public static Transformer getUnqualifiedStyleTransformer(ResourceResolver resourceResolver) {
        if (isUnqualifiedStyle(resourceResolver)) {
            return new Transformer() {
                public void transform(Document document, String systemId) {
                    Element root = document.getRootElement();

                    if (root.attribute("elementFormDefault") != null) {
                        root.remove(root.attribute("elementFormDefault"));
                    }

                    if (root.attribute("attributeFormDefault") != null) {
                        root.remove(root.attribute("attributeFormDefault"));
                    }
                }
            };
        } else {
            return getNoopTransformer();
        }
    }

    /** 判断是否为unqualified style。对于IDE plugins，这个判断可以让同一个plugin工作于不同的webx版本上。 */
    private static boolean isUnqualifiedStyle(ResourceResolver resourceResolver) {
        // 支持unqualifed style的webx包含converter类，早期的版本则没有这个类。
        // 因此可用这个类来区分webx的版本。
        return resourceResolver.getResource("com/alibaba/citrus/springext/util/ConvertToUnqualifiedStyle.class") != null;
    }

    public static Transformer getNoopTransformer() {
        return new Transformer() {
            public void transform(Document document, String systemId) {
                // donothing
            }
        };
    }

    public static String getNamespacePrefix(String preferredNsPrefix, String targetNamespace) {
        if (preferredNsPrefix != null) {
            return preferredNsPrefix;
        }

        if (targetNamespace != null) {
            return targetNamespace.substring(targetNamespace.lastIndexOf("/") + 1);
        }

        return null;
    }

    public static Map<String, String> parseSchemaLocation(String value) {
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
}
