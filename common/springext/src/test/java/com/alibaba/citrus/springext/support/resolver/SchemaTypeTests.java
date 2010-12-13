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
package com.alibaba.citrus.springext.support.resolver;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.core.io.DefaultResourceLoader;

import com.alibaba.citrus.test.TestEnvStatic;

/**
 * 测试springext-base.xsd中的类型。
 * 
 * @author Michael Zhou
 */
public class SchemaTypeTests {
    private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";
    private static SpringPluggableSchemas sps;
    private static SchemaEntityResolver resolver;
    private SAXReader reader;

    static {
        TestEnvStatic.init();
    }

    @BeforeClass
    public static void initSchemas() {
        sps = new SpringPluggableSchemas();
        resolver = new SchemaEntityResolver(new ResourceEntityResolver(new DefaultResourceLoader()), sps);
    }

    @Before
    public void initReader() throws Exception {
        reader = new SAXReader(true);
        reader.setEntityResolver(resolver);
        reader.setProperty(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
    }

    @Test
    public void parseBoolean() throws Exception {
        assertBoolean("true");
        assertBoolean("false");
        assertBoolean("${placeholder:defaultValue}");

        try {
            assertBoolean("invalid");
            fail();
        } catch (DocumentException e) {
            assertThat(e, exception("invalid"));
        }
    }

    @Test
    public void parseInteger() throws Exception {
        assertInteger("123");
        assertInteger("456");
        assertInteger("${placeholder:defaultValue}");

        try {
            assertInteger("abc");
            fail();
        } catch (DocumentException e) {
            assertThat(e, exception("abc"));
        }
    }

    private void assertBoolean(String value) throws Exception {
        Document doc = reader.read(getXmlFile("<types:test-boolean>" + value + "</types:test-boolean>"));
        Element elem = doc.getRootElement().element("test-boolean");

        assertEquals(value, elem.getText());
    }

    private void assertInteger(String value) throws Exception {
        Document doc = reader.read(getXmlFile("<types:test-integer>" + value + "</types:test-integer>"));
        Element elem = doc.getRootElement().element("test-integer");

        assertEquals(value, elem.getText());
    }

    private InputStream getXmlFile(String content) throws IOException {
        StringBuilder buf = new StringBuilder();

        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n"
                + "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "       xmlns:types=\"http://www.alibaba.com/schema/springext/base-types\"\n"
                + "xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n"
                + "    http://localhost:8080/schema/www.springframework.org/schema/beans/spring-beans.xsd\n"
                + "    http://www.alibaba.com/schema/springext/base-types\n"
                + "    http://localhost:8080/schema/www.alibaba.com/schema/springext-base-types.xsd\">\n");

        buf.append(content);

        buf.append("\n</beans>\n");

        return new ByteArrayInputStream(buf.toString().getBytes("UTF-8"));
    }
}
