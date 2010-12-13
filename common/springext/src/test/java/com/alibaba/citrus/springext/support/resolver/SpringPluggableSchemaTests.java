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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.test.TestEnvStatic;

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
    public void getUriToNameMappings() {
        Map<String, String> uris = sps.getUriToNameMappings();

        assertThat(uris.size(), greaterThan(0));

        assertEquals("www.springframework.org/schema/beans/spring-beans.xsd",
                uris.get("http://www.springframework.org/schema/beans/spring-beans.xsd"));
    }
}
