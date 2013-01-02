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

package com.alibaba.citrus.springext.impl;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.SourceInfo;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.citrus.springext.support.SpringSchemasSourceInfo;
import com.alibaba.citrus.test.TestEnvStatic;
import com.alibaba.citrus.util.io.StreamUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.Resource;

public class SpringPluggableSchemaTests {
    private static SpringPluggableSchemas sps;

    static {
        TestEnvStatic.init();
    }

    @BeforeClass
    public static void init() {
        sps = new SpringPluggableSchemas();
    }

    @Test
    public void getVersion() {
        Map<String, Schema> names = sps.getNamedMappings();

        Schema beansSchema = names.get("www.springframework.org/schema/beans/spring-beans.xsd");
        assertEquals(null, beansSchema.getVersion());

        Schema beansSchema25 = names.get("www.springframework.org/schema/beans/spring-beans-2.5.xsd");
        assertEquals("2.5", beansSchema25.getVersion());
    }

    @Test
    public void getNamedMappings() {
        Map<String, Schema> names = sps.getNamedMappings();

        assertThat(names.size(), greaterThan(0));
        assertThat(names.toString(), containsAll("www.springframework.org", "www.alibaba.com", "spring-beans.xsd"));

        // beans
        Schema beansSchema = names.get("www.springframework.org/schema/beans/spring-beans.xsd");

        assertNotNull(beansSchema);
        assertEquals("http://www.springframework.org/schema/beans", beansSchema.getTargetNamespace());
        assertEquals("http://www.springframework.org/schema/beans", beansSchema.getTargetNamespace()); // try hard

        // dummy
        Schema dummySchema = names.get("www.alibaba.com/schema/tests.xsd");

        assertNotNull(dummySchema);
        assertEquals(null, dummySchema.getTargetNamespace());
        assertEquals(null, dummySchema.getTargetNamespace()); // try hard
    }

    @Test
    public void unqualifiedStyle() throws Exception {
        Schema schema = sps.getNamedMappings().get("www.alibaba.com/schema/springext-base-types.xsd");

        String textTransformed = schema.getText();
        String textOriginal = StreamUtil.readText(((SourceInfo<?>) schema).getSource().getInputStream(), "UTF-8", true);

        String elementQualified = "elementFormDefault=\"qualified\"";

        assertThat(textOriginal, containsString(elementQualified)); // springext-base-types.xsd包含elementFormDefault
        assertThat(textTransformed, not(containsString(elementQualified))); // 转换后被强制去除。
    }

    @Test
    public void sourceInfo() throws Exception {
        Map<String, Schema> names = sps.getNamedMappings();
        Resource resource;

        Schema beansSchema = names.get("www.springframework.org/schema/beans/spring-beans-2.5.xsd");
        resource = assertSourceInfoAndGetResource(beansSchema, SpringPluggableSchemaSourceInfo.class);
        assertResource("org/springframework/beans/factory/xml/spring-beans-2.5.xsd", resource);

        SpringSchemasSourceInfo parent = ((SpringPluggableSchemaSourceInfo) beansSchema).getParent();
        resource = assertSourceInfoAndGetResource(parent, SpringSchemasSourceInfo.class);
        assertNull(parent.getParent());
        assertTrue(resource.getURL().toExternalForm().endsWith("META-INF/spring.schemas"));

        String content = StreamUtil.readText(resource.getInputStream(), "UTF-8", true);
        assertTrue(content.contains("org/springframework/beans/factory/xml/spring-beans-2.5.xsd"));
    }

    private void assertResource(String resourceName, Resource resource) throws Exception {
        if (resourceName == null) {
            assertNull(resource);
        } else {
            assertEquals(getClass().getClassLoader().getResource(resourceName), resource.getURL());
        }
    }

    private Resource assertSourceInfoAndGetResource(Object obj, Class<? extends SourceInfo<?>> expectedInterface) {
        SourceInfo<?> sourceInfo = expectedInterface.cast(obj);

        assertEquals(-1, sourceInfo.getLineNumber());

        return sourceInfo.getSource() == null ? null
                                              : getFieldValue(sourceInfo.getSource(), "springResource", Resource.class);
    }

    @Test
    public void getUriToNameMappings() {
        Map<String, String> uris = sps.getUriToNameMappings();

        assertThat(uris.size(), greaterThan(0));

        assertEquals("www.springframework.org/schema/beans/spring-beans.xsd",
                     uris.get("http://www.springframework.org/schema/beans/spring-beans.xsd"));
    }
}
