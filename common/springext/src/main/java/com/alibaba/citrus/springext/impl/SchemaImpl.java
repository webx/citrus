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
package com.alibaba.citrus.springext.impl;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.io.StreamUtil.*;
import static javax.xml.XMLConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;

import com.alibaba.citrus.springext.ConfigurationPointException;
import com.alibaba.citrus.springext.DocumentFilter;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SchemaInternal;
import com.alibaba.citrus.springext.support.SchemaUtil;

public class SchemaImpl implements SchemaInternal {
    private final static Logger log = LoggerFactory.getLogger(Schema.class);
    private final String name;
    private final String version;
    private final String sourceDesc;
    private final InputStreamSource source;
    private String targetNamespace;
    private String preferredNsPrefix;
    private String[] includes;
    private String[] elements;
    private boolean parsed;
    private boolean parsingTargetNamespace;

    /**
     * 创建 configuration point 的 main schema 和 versioned schema。
     */
    public SchemaImpl(String name, String version, String targetNamespace, String preferredNsPrefix, String sourceDesc,
                      InputStreamSource source) {
        this(name, version, targetNamespace, preferredNsPrefix, false, sourceDesc, source);
    }

    /**
     * 创建 contribution 的 main schema 和 versioned schema。
     */
    public SchemaImpl(String name, String version, String sourceDesc, InputStreamSource source) {
        this(name, version, null, null, false, sourceDesc, source);
    }

    /**
     * 创建spring.schemas中定义的schema。
     */
    public SchemaImpl(String name, String version, boolean parsingTargetNamespace, String sourceDesc,
                      InputStreamSource source) {
        this(name, version, null, null, parsingTargetNamespace, sourceDesc, source);
    }

    /**
     * 创建spring.schemas中定义的schema。
     * <p>
     * 如果<code>parsingTargetNamespace</code>为 <code>true</code>，则试图通过解析xml来取得ns。
     * </p>
     */
    private SchemaImpl(String name, String version, String targetNamespace, String preferredNsPrefix,
                       boolean parsingTargetNamespace, String sourceDesc, InputStreamSource source) {
        this.name = name;
        this.version = version;
        this.targetNamespace = trimToNull(targetNamespace);
        this.preferredNsPrefix = trimToNull(preferredNsPrefix);
        this.parsingTargetNamespace = parsingTargetNamespace;
        this.sourceDesc = sourceDesc;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getTargetNamespace() {
        if (parsingTargetNamespace) {
            parse();
        }

        return targetNamespace;
    }

    public String getPreferredNsPrefix() {
        return preferredNsPrefix;
    }

    public String[] getIncludes() {
        parse();
        return includes == null ? EMPTY_STRING_ARRAY : includes;
    }

    public String[] getElements() {
        if (elements != null) {
            return elements;
        }

        parse();
        return elements == null ? EMPTY_STRING_ARRAY : elements;
    }

    /**
     * 由schemaSet来设置。
     */
    public void setElements(String[] elements) {
        this.elements = elements;
    }

    public String getNamespacePrefix() {
        return SchemaUtil.getNamespacePrefix(getPreferredNsPrefix(), getTargetNamespace());
    }

    public String getSourceDescription() {
        return sourceDesc;
    }

    public InputStream getInputStream() throws IOException {
        return source.getInputStream();
    }

    public Document getDocument() {
        try {
            return readDocument(source.getInputStream(), name, true);
        } catch (DocumentException e) {
            log.warn("Not a valid XML doc: {}, source={},\n{}", new Object[] { name, source, e.getMessage() });
            return null;
        } catch (IOException e) {
            throw new ConfigurationPointException("Failed to read text of schema file: " + name + ", source=" + source,
                    e);
        }
    }

    public String getText() throws IOException {
        return getText(null, null);
    }

    public String getText(String charset) throws IOException {
        return getText(charset, null);
    }

    public String getText(String charset, DocumentFilter filter) throws IOException {
        String content;

        // parse
        Document doc = getDocument();

        // read text if it's an invalid XML doc.
        if (doc == null) {
            return readText(getInputStream(), "ISO-8859-1", true);
        }

        // filter the doc
        if (filter != null) {
            doc = assertNotNull(filter.filter(doc, name), "doc filter returned null");
        }

        // output 
        try {
            content = SchemaUtil.getDocumentText(doc, charset);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationPointException("Failed to read text of schema file: " + name + ", source=" + source,
                    e);
        }

        return content;
    }

    /**
     * 解析schema，取得以下信息：
     * <ol>
     * <li>targetNamespace</li>
     * <li>include name</li>
     * </ol>
     */
    private void parse() {
        if (parsed) {
            return;
        }

        parsed = true;
        Document doc = getDocument();

        // return if it's not a valid XML file
        if (doc == null) {
            return;
        }

        Element root = doc.getRootElement();

        // return if not a schema file
        if (!W3C_XML_SCHEMA_NS_URI.equals(root.getNamespaceURI()) || !"schema".equals(root.getName())) {
            return;
        }

        // parse targetNamespace
        if (parsingTargetNamespace) {
            Attribute attr = root.attribute("targetNamespace");

            if (attr != null) {
                targetNamespace = attr.getStringValue();
            }
        }

        // parse include
        Namespace xsd = DocumentHelper.createNamespace("xsd", W3C_XML_SCHEMA_NS_URI);
        QName includeName = DocumentHelper.createQName("include", xsd);
        List<String> includeNames = createLinkedList();

        // for each <xsd:include>
        for (Iterator<?> i = root.elementIterator(includeName); i.hasNext();) {
            Element includeElement = (Element) i.next();
            String schemaLocation = trimToNull(includeElement.attributeValue("schemaLocation"));

            if (schemaLocation != null) {
                includeNames.add(schemaLocation);
            }
        }

        includes = includeNames.toArray(new String[includeNames.size()]);

        // parse xsd:element
        if (elements == null) {
            QName elementName = DocumentHelper.createQName("element", xsd);
            List<String> elementNames = createLinkedList();

            // for each <xsd:element>
            for (Iterator<?> i = root.elementIterator(elementName); i.hasNext();) {
                Element elementElement = (Element) i.next();
                String name = trimToNull(elementElement.attributeValue("name"));

                if (name != null) {
                    elementNames.add(name);
                }
            }

            elements = elementNames.toArray(new String[elementNames.size()]);
        }
    }

    @Override
    public String toString() {
        if (targetNamespace == null) {
            return String.format("Schema[name=%s, version=%s, source=%s]", name, version, source);
        } else {
            return String.format("Schema[name=%s, version=%s, targetNamespace=%s, source=%s]", name, version,
                    targetNamespace, source);
        }
    }
}
