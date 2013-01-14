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

package com.alibaba.citrus.springext.impl;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import com.alibaba.citrus.springext.ConfigurationPointException;
import com.alibaba.citrus.springext.Schema.Transformer;
import com.alibaba.citrus.springext.support.SchemaUtil;
import com.alibaba.citrus.test.TestEnvStatic;
import com.alibaba.citrus.util.io.ByteArrayInputStream;
import com.alibaba.citrus.util.io.StreamUtil;
import org.dom4j.Document;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;

public class SchemaBaseTests {
    private SchemaBaseImpl schema;

    // test data
    private InputStreamSource source;
    private InputStreamSource illegalSource;
    private Transformer       transformer;
    private Transformer       transformer2;
    private Document          document;

    private int analyzeCount;

    static {
        TestEnvStatic.init();
    }

    @Before
    public void init() throws Exception {
        source = new ByteArrayResource("<hello></hello>".getBytes());
        illegalSource = new ByteArrayResource("<hello>".getBytes());
        transformer = new Transformer() {
            public void transform(Document document, String systemId) {
                document.getRootElement().addElement("world");
            }
        };
        transformer2 = new Transformer() {
            public void transform(Document document, String systemId) {
                document.getRootElement().addElement("hi");
            }
        };
        document = SchemaUtil.readDocument(new ByteArrayInputStream("<doc></doc>".getBytes()), "", true);

        schema = new SchemaBaseImpl(source);
    }

    @Test
    public void constructor_inputStreamSource() {
        try {
            new SchemaBaseImpl((InputStreamSource) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no InputStreamSource provided"));
        }

        schema = new SchemaBaseImpl(source);
        assertFalse(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertFalse(schema.isEverTransformed());
        assertNull(schema.getDocumentField());
        assertNull(schema.getTransformedData());
    }

    @Test
    public void constructor_documentSource() {
        try {
            new SchemaBaseImpl((Document) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no Document provided"));
        }

        schema = new SchemaBaseImpl(document);
        assertTrue(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertFalse(schema.isEverTransformed());
        assertSame(document, schema.getDocumentField());
        assertNull(schema.getTransformedData());
    }

    @Test
    public void transform() {
        try {
            schema.transform(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no Transformer provided"));
        }

        // 按顺序执行transforms
        schema.transform(transformer2); // hi
        schema.transform(transformer); // world
        assertFalse(schema.isEverTransformed());

        assertInputStream("<hello><hi/><world/></hello>", schema.getInputStream());
        assertTrue(schema.isEverTransformed());
    }

    @Test
    public void transform_doNow() {
        try {
            schema.transform(null, true);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no Transformer provided"));
        }

        // 按顺序执行transforms
        schema.transform(transformer2, true); // hi
        schema.transform(transformer, true); // world
        assertTrue(schema.isEverTransformed());

        assertInputStream("<hello><hi/><world/></hello>", schema.getInputStream());
        assertTrue(schema.isEverTransformed());
    }

    @Test
    public void getOriginalInputStream_exception() {
        // 情况1. parse时调用
        schema = new SchemaBaseImpl(new InputStreamSource() {
            public InputStream getInputStream() throws IOException {
                throw new IOException("hi");
            }

            @Override
            public String toString() {
                return "mysource";
            }
        });

        schema.transform(transformer);

        try {
            schema.getInputStream();
            fail();
        } catch (ConfigurationPointException e) {
            assertThat(e, exception("Failed to read text of schema file: myschema, source=mysource"));
        }

        // 情况2. getInputStream时调用（无transformer）
        schema = new SchemaBaseImpl(new InputStreamSource() {
            public InputStream getInputStream() throws IOException {
                throw new IOException("hi");
            }

            @Override
            public String toString() {
                return "mysource";
            }
        });

        try {
            schema.getInputStream();
            fail();
        } catch (ConfigurationPointException e) {
            assertThat(e, exception("Failed to read text of schema file: myschema, source=mysource"));
        }
    }

    @Test
    public void inputStreamSource_noParse_noTransform() {
        schema = new SchemaBaseImpl(source);
        assertInputStream("<hello></hello>", schema.getInputStream());
        assertFalse(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertFalse(schema.isEverTransformed());
        assertNull(schema.getDocumentField());
        assertNull(schema.getTransformedData());
    }

    @Test
    public void inputStreamSource_noParse_noTransform_illegalSource() {
        schema = new SchemaBaseImpl(illegalSource);
        assertInputStream("<hello>", schema.getInputStream());
        assertFalse(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertFalse(schema.isEverTransformed());
        assertNull(schema.getDocumentField());
        assertNull(schema.getTransformedData());
    }

    @Test
    public void inputStreamSource_withParse_withTransform() {
        Document doc;
        byte[] data;

        schema.transform(transformer);
        assertFalse(schema.getTransformersQueue().isEmpty());

        assertInputStream("<hello><world/></hello>", schema.getInputStream());
        assertTrue(schema.isParsed());
        assertTrue(schema.isEverTransformed());
        assertNotNull(doc = schema.getDocumentField());
        assertTrue(schema.getTransformersQueue().isEmpty());
        assertNotNull(data = schema.getTransformedData());

        // 第二次取，不会再次生成data
        assertInputStream("<hello><world/></hello>", schema.getInputStream());
        assertSame(data, schema.getTransformedData());
        assertSame(doc, schema.getDocumentField());

        // 再次transform，不会重新parse
        schema.transform(transformer);
        assertInputStream("<hello><world/><world/></hello>", schema.getInputStream());
        assertSame(doc, schema.getDocumentField());
    }

    @Test
    public void inputStreamSource_withParse_withTransform_illegalSource() {
        schema = new SchemaBaseImpl(illegalSource);
        schema.transform(transformer);
        assertFalse(schema.getTransformersQueue().isEmpty());

        assertInputStream("<hello>", schema.getInputStream());
        assertTrue(schema.isParsed());
        assertTrue(schema.isEverTransformed());
        assertNull(schema.getDocumentField());
        assertNull(schema.getTransformedData());
        assertTrue(schema.getTransformersQueue().isEmpty());

        // 第二次取data
        assertInputStream("<hello>", schema.getInputStream());
        assertNull(schema.getTransformedData());
    }

    @Test
    public void documentSource_skipParse_noTransform() {
        byte[] data;

        schema = new SchemaBaseImpl(document);
        assertTrue(schema.getTransformersQueue().isEmpty());
        assertTrue(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertFalse(schema.isEverTransformed());

        assertInputStream("<doc/>", schema.getInputStream());
        assertTrue(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertFalse(schema.isEverTransformed());
        assertSame(document, schema.getDocumentField());
        assertNotNull(data = schema.getTransformedData());

        // 第二次取，不会再次生成data
        assertInputStream("<doc/>", schema.getInputStream());
        assertSame(document, schema.getDocumentField());
        assertSame(data, schema.getTransformedData());
    }

    @Test
    public void documentSource_skipParse_withTransform() {
        byte[] data;

        schema = new SchemaBaseImpl(document);
        schema.transform(transformer);
        assertFalse(schema.getTransformersQueue().isEmpty());
        assertTrue(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertFalse(schema.isEverTransformed());

        assertInputStream("<doc><world/></doc>", schema.getInputStream());
        assertTrue(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertTrue(schema.isEverTransformed());
        assertSame(document, schema.getDocumentField());
        assertTrue(schema.getTransformersQueue().isEmpty());
        assertNotNull(data = schema.getTransformedData());

        // 第二次取，不会再次生成data
        assertInputStream("<doc><world/></doc>", schema.getInputStream());
        assertSame(document, schema.getDocumentField());
        assertSame(data, schema.getTransformedData());

        // 再次transform，不会重新parse
        schema.transform(transformer);
        assertInputStream("<doc><world/><world/></doc>", schema.getInputStream());
        assertSame(document, schema.getDocumentField());
    }

    @Test
    public void inputStreamSource_noTransform_analyze() {
        schema.analyze();
        assertTrue(schema.isParsed());
        assertTrue(schema.isAnalyzed());
        assertFalse(schema.isEverTransformed());
        assertNotNull(schema.getDocumentField());
        assertTrue(schema.getTransformersQueue().isEmpty());
        assertNull(schema.getTransformedData());
        assertEquals(1, analyzeCount);

        // 不会重复调用analyze
        schema.analyze();
        assertEquals(1, analyzeCount);

        // 尽管document存在，由于没有经过transform，故inputStream仍然直接从inputStreamSource取得。
        assertInputStream("<hello></hello>", schema.getInputStream());
        assertNull(schema.getTransformedData());
    }

    @Test
    public void inputStreamSource_withTransform_analyze() {
        schema.transform(transformer);
        schema.analyze();
        assertTrue(schema.isParsed());
        assertTrue(schema.isAnalyzed());
        assertTrue(schema.isEverTransformed());
        assertNotNull(schema.getDocumentField());
        assertTrue(schema.getTransformersQueue().isEmpty());
        assertNull(schema.getTransformedData());
        assertEquals(1, analyzeCount);

        // 不会重复调用analyze
        schema.analyze();
        assertEquals(1, analyzeCount);

        assertInputStream("<hello><world/></hello>", schema.getInputStream());
        assertNotNull(schema.getTransformedData());
    }

    @Test
    public void documentSource_noTransform_analyze() {
        schema = new SchemaBaseImpl(document);
        schema.analyze();
        assertTrue(schema.isParsed());
        assertTrue(schema.isAnalyzed());
        assertFalse(schema.isEverTransformed());
        assertNotNull(schema.getDocumentField());
        assertTrue(schema.getTransformersQueue().isEmpty());
        assertNull(schema.getTransformedData());
        assertEquals(1, analyzeCount);

        // 不会重复调用analyze
        schema.analyze();
        assertEquals(1, analyzeCount);

        assertInputStream("<doc/>", schema.getInputStream());
        assertNotNull(schema.getTransformedData());
    }

    @Test
    public void documentSource_withTransform_analyze() {
        schema = new SchemaBaseImpl(document);
        schema.transform(transformer);
        schema.analyze();
        assertTrue(schema.isEverTransformed());
        assertTrue(schema.isParsed());
        assertTrue(schema.isAnalyzed());
        assertNotNull(schema.getDocumentField());
        assertTrue(schema.getTransformersQueue().isEmpty());
        assertNull(schema.getTransformedData());
        assertEquals(1, analyzeCount);

        // 不会重复调用analyze
        schema.analyze();
        assertEquals(1, analyzeCount);

        assertInputStream("<doc><world/></doc>", schema.getInputStream());
        assertNotNull(schema.getTransformedData());
    }

    private void assertInputStream(String bytes, InputStream inputStream) {
        String text = null;

        try {
            text = StreamUtil.readText(inputStream, "UTF-8", true);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        String decl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

        if (text.startsWith(decl)) {
            text = text.substring(decl.length()).trim().replaceAll(">\\s+<", "><");
        }

        assertEquals(bytes, text);
    }

    @Test
    public void inputStreamSource_noTransform_getDocument() {
        schema.getDocument();
        assertTrue(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertNotNull(schema.getDocumentField());
        assertTrue(schema.getTransformersQueue().isEmpty());
        assertNull(schema.getTransformedData());

        // 尽管document存在，由于没有经过transform，故inputStream仍然直接从inputStreamSource取得。
        assertInputStream("<hello></hello>", schema.getInputStream());
        assertNull(schema.getTransformedData());
    }

    @Test
    public void inputStreamSource_withTransform_getDocument() {
        schema.transform(transformer);
        schema.getDocument();
        assertTrue(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertNotNull(schema.getDocumentField());
        assertTrue(schema.getTransformersQueue().isEmpty());
        assertNull(schema.getTransformedData());

        assertInputStream("<hello><world/></hello>", schema.getInputStream());
        assertNotNull(schema.getTransformedData());
    }

    @Test
    public void documentSource_noTransform_getDocument() {
        schema = new SchemaBaseImpl(document);
        schema.getDocument();
        assertTrue(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertNotNull(schema.getDocumentField());
        assertTrue(schema.getTransformersQueue().isEmpty());
        assertNull(schema.getTransformedData());

        assertInputStream("<doc/>", schema.getInputStream());
        assertNotNull(schema.getTransformedData());
    }

    @Test
    public void documentSource_withTransform_getDocument() {
        schema = new SchemaBaseImpl(document);
        schema.transform(transformer);
        schema.getDocument();
        assertTrue(schema.isParsed());
        assertFalse(schema.isAnalyzed());
        assertNotNull(schema.getDocumentField());
        assertTrue(schema.getTransformersQueue().isEmpty());
        assertNull(schema.getTransformedData());

        assertInputStream("<doc><world/></doc>", schema.getInputStream());
        assertNotNull(schema.getTransformedData());
    }

    @Test
    public void inputStreamSource_toString() {
        assertEquals("resource loaded from byte array", schema.toString());
    }

    @Test
    public void documentSource_toString() {
        schema = new SchemaBaseImpl(document);
        assertEquals("generated-content", schema.toString());
    }

    private class SchemaBaseImpl extends SchemaBase {
        private SchemaBaseImpl(InputStreamSource originalSource) {
            super(originalSource);
        }

        private SchemaBaseImpl(Document originalDocument) {
            super(originalDocument);
        }

        public boolean isParsed() {
            return getFieldValue(this, "parsed", Boolean.class);
        }

        public boolean isEverTransformed() {
            return getFieldValue(this, "everTransformed", Boolean.class);
        }

        public boolean isAnalyzed() {
            return getFieldValue(this, "analyzed", Boolean.class);
        }

        public Document getDocumentField() {
            return getFieldValue(this, "document", Document.class);
        }

        public byte[] getTransformedData() {
            return getFieldValue(this, "transformedData", byte[].class);
        }

        public LinkedList<?> getTransformersQueue() {
            return getFieldValue(this, "transformersQueue", LinkedList.class);
        }

        @Override
        public String getName() {
            return "myschema";
        }

        @Override
        public String getVersion() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTargetNamespace() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPreferredNsPrefix() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getIncludes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Element> getElements() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getNamespacePrefix() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getSourceDescription() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getText() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getText(String charset) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getText(String charset, Transformer transformer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Element getElement(String elementName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setElements(Collection<Element> elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doAnalyze() {
            analyzeCount++;
        }
    }
}
