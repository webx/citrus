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
import static com.alibaba.citrus.util.io.StreamUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.core.io.DefaultResourceLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.alibaba.citrus.springext.impl.ConfigurationPointsImpl;
import com.alibaba.citrus.test.TestEnvStatic;

public class EntityResolverTests {
    private ConfigurationPointsImpl cps;
    private SpringPluggableSchemas sps;
    private SchemaEntityResolver resolver;

    static {
        TestEnvStatic.init();
    }

    @Before
    public void init() {
        cps = new ConfigurationPointsImpl();
        sps = new SpringPluggableSchemas();
        resolver = new SchemaEntityResolver(new ResourceEntityResolver(new DefaultResourceLoader()), cps, sps);
    }

    @Test
    public void resolveDefault_resourceLoader() throws SAXException, IOException {
        InputSource source = resolver.resolveEntity(null, "classpath:dummy.txt");

        assertEquals("dummy", getContent(source));
    }

    @Test
    public void resolveDefault_springSchemas() throws SAXException, IOException {
        InputSource source = resolver.resolveEntity(null,
                "http://www.springframework.org/schema/beans/spring-beans-2.5.xsd");

        String str = getContent(source);

        assertThat(
                str,
                containsAll("xmlns=\"http://www.springframework.org/schema/beans\"",
                        "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"",
                        "targetNamespace=\"http://www.springframework.org/schema/beans\""));
    }

    @Test
    public void resolve_springSchemas_withShortName() throws SAXException, IOException {
        InputSource source = resolver.resolveEntity(null,
                "http://any.domain.com/any/prefix/www.springframework.org/schema/beans/spring-beans-2.5.xsd");

        String str = getContent(source);

        assertThat(
                str,
                containsAll("xmlns=\"http://www.springframework.org/schema/beans\"",
                        "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"",
                        "targetNamespace=\"http://www.springframework.org/schema/beans\""));
    }

    @Test
    public void resolve_otherSchemas_withShortName() throws SAXException, IOException {
        InputSource source = resolver.resolveEntity(null,
                "http://any.domain.com/any/prefix/www.alibaba.com/schema/tests.xsd");

        String str = getContent(source);

        assertEquals("dummy", str);
    }

    @Test
    public void resolve_notFound() throws SAXException, IOException {
        InputSource source = resolver.resolveEntity(null, "http://localhost/not-found.xsd");

        assertThat(source, nullValue());
    }

    @Test
    public void resolve_configurationPoint_noDefaultElement() throws SAXException, IOException {
        InputSource source = resolver.resolveEntity(null, "http://localhost/schema/services/services.xsd");

        String str = getContent(source);

        assertThat(
                str,
                containsAll("xmlns=\"http://www.alibaba.com/schema/services\"",
                        "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"",
                        "targetNamespace=\"http://www.alibaba.com/schema/services\"",
                        "schemaLocation=\"services/container.xsd\""));

        assertThat(str, not(containsString("xsd:element")));
    }

    @Test
    public void resolve_configurationPoint_withDefaultElement() throws SAXException, IOException {
        InputSource source = resolver.resolveEntity(null, "http://localhost/schema/services/services-tools.xsd");

        String str = getContent(source);

        assertThat(
                str,
                containsAll("xmlns=\"http://www.alibaba.com/schema/services/tools\"",
                        "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"",
                        "targetNamespace=\"http://www.alibaba.com/schema/services/tools\"",
                        "schemaLocation=\"services/tools/dateformat.xsd\"",
                        "xsd:element name=\"tool\" type=\"springext:referenceableBeanType\""));
    }

    @Test
    public void resolve_configurationPoint_hybridSlash() throws SAXException, IOException {
        InputSource source = resolver.resolveEntity(null, "c:\\schema\\\\services/services.xsd");

        String str = getContent(source);

        assertThat(
                str,
                containsAll("xmlns=\"http://www.alibaba.com/schema/services\"",
                        "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"",
                        "targetNamespace=\"http://www.alibaba.com/schema/services\"",
                        "schemaLocation=\"services/container.xsd\""));
    }

    private String getContent(InputSource source) throws IOException {
        Reader reader = source.getCharacterStream();
        InputStream stream = source.getByteStream();

        if (reader == null) {
            assertThat(stream, notNullValue());
            return readText(stream, "UTF-8", true);
        } else {
            assertThat(stream, nullValue());
            return readText(reader, true);
        }
    }
}
