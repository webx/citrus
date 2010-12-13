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

import static com.alibaba.citrus.springext.ContributionType.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.VersionableSchemas;
import com.alibaba.citrus.test.TestEnvStatic;
import com.alibaba.citrus.test.runner.TestNameAware;

@RunWith(TestNameAware.class)
public class SchemasTests {
    private ConfigurationPointsImpl cps;

    static {
        TestEnvStatic.init();
    }

    @Test
    public void test1_configurationPoint_noContributions() throws IOException {
        createConfigurationPoints("TEST-INF/test1/cps");

        ConfigurationPoint cp = cps.getConfigurationPointByName("cp1");
        VersionableSchemas schemas = cp.getSchemas();

        String str = schemas.toString();

        System.out.println("--");
        System.out.println(str);

        assertEquals("Schemas[cp1.xsd, targetNamespace=http://www.alibaba.com/test1/cp1, 0 versioned schemas]", str);

        String content = schemas.getMainSchema().getText();

        assertThat(content, containsString("UTF-8"));

        content = schemas.getMainSchema().getText("GB2312");

        assertThat(content, containsString("GB2312"));

        System.out.println("--");
        System.out.println(content);

        assertThat(
                content,
                containsAll("xmlns=\"http://www.alibaba.com/test1/cp1\"",
                        "targetNamespace=\"http://www.alibaba.com/test1/cp1\"", "elementFormDefault=\"qualified\""));
    }

    @Test
    public void test6_configurationPoint_noContributionSchemas() throws IOException {
        createConfigurationPoints("TEST-INF/test6/cps");

        ConfigurationPoint cp = cps.getConfigurationPointByName("cp1");
        VersionableSchemas schemas = cp.getSchemas();

        String str = schemas.toString();

        System.out.println("--");
        System.out.println(str);

        assertEquals("Schemas[cp1.xsd, targetNamespace=http://www.alibaba.com/test6/cp1, 0 versioned schemas]", str);

        String content = schemas.getMainSchema().getText();

        System.out.println("--");
        System.out.println(content);

        assertThat(
                content,
                containsAll("xmlns=\"http://www.alibaba.com/test6/cp1\"",
                        "targetNamespace=\"http://www.alibaba.com/test6/cp1\"", "elementFormDefault=\"qualified\""));
    }

    @Test
    public void test7_configurationPoint_nestedName_noContributionSchemas() throws IOException {
        createConfigurationPoints("TEST-INF/test7/cps");

        ConfigurationPoint cp = cps.getConfigurationPointByName("dir/cp1");
        VersionableSchemas schemas = cp.getSchemas();

        String str = schemas.toString();

        System.out.println("--");
        System.out.println(str);

        assertEquals("Schemas[dir-cp1.xsd, targetNamespace=http://www.alibaba.com/test7/dir/cp1, 0 versioned schemas]",
                str);

        String content = schemas.getMainSchema().getText();

        System.out.println("--");
        System.out.println(content);

        assertThat(
                content,
                containsAll("xmlns=\"http://www.alibaba.com/test7/dir/cp1\"",
                        "targetNamespace=\"http://www.alibaba.com/test7/dir/cp1\"", "elementFormDefault=\"qualified\""));
    }

    @Test
    public void test9_configurationPoint_nsPrefix() throws IOException {
        createConfigurationPoints("TEST-INF/test9/cps");

        ConfigurationPoint cp = cps.getConfigurationPointByName("my/services");
        VersionableSchemas schemas = cp.getSchemas();

        // cp - main schema
        Schema mainSchema = schemas.getMainSchema();

        assertEquals("http://www.alibaba.com/my/services", mainSchema.getTargetNamespace());
        assertEquals("svc", mainSchema.getNamespacePrefix()); // cps定义中指定了nsPrefix

        // cp - version 1.0 schema
        Schema versionedSchema_10 = schemas.getVersionedSchema("1.0");

        assertEquals("http://www.alibaba.com/my/services", versionedSchema_10.getTargetNamespace());
        assertEquals("svc", versionedSchema_10.getNamespacePrefix()); // cps定义中指定了nsPrefix

        // cp - version 2.0 schema
        Schema versionedSchema_20 = schemas.getVersionedSchema("2.0");

        assertEquals("http://www.alibaba.com/my/services", versionedSchema_20.getTargetNamespace());
        assertEquals("svc", versionedSchema_20.getNamespacePrefix()); // cps定义中指定了nsPrefix

        // cp - main schema, no nsPrefix specified
        cp = cps.getConfigurationPointByName("my/plugins");
        schemas = cp.getSchemas();
        mainSchema = schemas.getMainSchema();

        assertEquals("http://www.alibaba.com/my/plugins", mainSchema.getTargetNamespace());
        assertEquals("plugins", mainSchema.getNamespacePrefix()); // 根据targetNamespace生成
    }

    @Test
    public void test9_configurationPointSchemas() throws IOException {
        createConfigurationPoints("TEST-INF/test9/cps");

        ConfigurationPoint cp = cps.getConfigurationPointByName("my/services");
        VersionableSchemas schemas = cp.getSchemas();

        // schemas to string
        String str = schemas.toString();

        System.out.println("--");
        System.out.println(str);

        assertThat(
                str,
                containsAll(
                        "Schemas[my-services.xsd, targetNamespace=http://www.alibaba.com/my/services, 2 versioned schemas]",
                        "version 1.0: Schema[name=my-services-1.0.xsd, version=1.0, targetNamespace=http://www.alibaba.com/my/services, source=generated-content]",
                        "version 2.0: Schema[name=my-services-2.0.xsd, version=2.0, targetNamespace=http://www.alibaba.com/my/services, source=generated-content]"));

        // cp - main schema
        String content = schemas.getMainSchema().getText();

        System.out.println("--");
        System.out.println(content);

        assertThat(
                content,
                containsAll("xmlns=\"http://www.alibaba.com/my/services\"",
                        "targetNamespace=\"http://www.alibaba.com/my/services\"", "elementFormDefault=\"qualified\""));
        assertThat(
                content,
                containsAll("<xsd:include", "schemaLocation=\"my/services/service1.xsd\"",
                        "schemaLocation=\"my/services/service2.xsd\"", "schemaLocation=\"my/services/service3.xsd\""));

        // cp - version 1.0 schema
        content = schemas.getVersionedSchema("1.0").getText();

        System.out.println("--");
        System.out.println(content);

        assertThat(
                content,
                containsAll("xmlns=\"http://www.alibaba.com/my/services\"",
                        "targetNamespace=\"http://www.alibaba.com/my/services\"", "elementFormDefault=\"qualified\""));
        assertThat(
                content,
                containsAll("<xsd:include", "schemaLocation=\"my/services/service1-1.0.xsd\"",
                        "schemaLocation=\"my/services/service2.xsd\"", "schemaLocation=\"my/services/service3.xsd\""));

        // cp - version 2.0 schema
        content = schemas.getVersionedSchema("2.0").getText();

        System.out.println("--");
        System.out.println(content);

        assertThat(
                content,
                containsAll("xmlns=\"http://www.alibaba.com/my/services\"",
                        "targetNamespace=\"http://www.alibaba.com/my/services\"", "elementFormDefault=\"qualified\""));
        assertThat(
                content,
                containsAll("<xsd:include", "schemaLocation=\"my/services/service1.xsd\"",
                        "schemaLocation=\"my/services/service2-2.0.xsd\"",
                        "schemaLocation=\"my/services/service3.xsd\""));
    }

    @Test
    public void test9_contributionSchemas() throws IOException {
        createConfigurationPoints("TEST-INF/test9/cps");

        ConfigurationPoint cp = cps.getConfigurationPointByName("my/services");
        Iterator<Contribution> i = cp.getContributions().iterator();

        // contrib1 - schemas to string
        Contribution contrib = i.next();
        VersionableSchemas schemas = contrib.getSchemas();
        String str = schemas.toString();

        System.out.println("--");
        System.out.println(str);

        assertThat(
                str,
                containsAll("Schemas[my/services/service1.xsd, 1 versioned schemas]",
                        "version 1.0: Schema[name=my/services/service1-1.0.xsd, version=1.0, source=",
                        "my/services/service1-1.0.xsd]"));

        // contrib1 - main schema to string
        String content = schemas.getMainSchema().getText();
        assertThat(content, containsString("service1-main"));

        // contrib1 - version 1.0 schema to string
        content = schemas.getVersionedSchema("1.0").getText();
        assertThat(content, containsString("service1-1.0"));

        // contrib2 - schemas to string
        contrib = i.next();
        schemas = contrib.getSchemas();
        str = schemas.toString();

        System.out.println("--");
        System.out.println(str);

        assertThat(
                str,
                containsAll("Schemas[my/services/service2.xsd, 1 versioned schemas]",
                        "version 2.0: Schema[name=my/services/service2-2.0.xsd, version=2.0, source=",
                        "my/services/service2-2.0.xsd]"));

        // contrib2 - main schema to string
        content = schemas.getMainSchema().getText();
        assertThat(content, containsString("service2-main"));

        // contrib2 - version 2.0 schema to string
        content = schemas.getVersionedSchema("2.0").getText();
        assertThat(content, containsString("service2-2.0"));

        // contrib3 - schemas to string
        contrib = i.next();
        schemas = contrib.getSchemas();
        str = schemas.toString();

        System.out.println("--");
        System.out.println(str);

        assertThat(str, containsAll("Schemas[my/services/service3.xsd, 0 versioned schemas]"));

        // contrib3 - main schema to string
        content = schemas.getMainSchema().getText();
        assertThat(content, containsString("service3-main"));

        assertFalse(i.hasNext());
    }

    @Test
    public void test12_contributionSchemas_versionRecognition() {
        createConfigurationPoints("TEST-INF/test12/cps");

        ConfigurationPoint cp = cps.getConfigurationPointByName("my/services");
        assertEquals(3, cp.getContributions().size());

        Contribution myservice = cp.getContribution("myservice", BEAN_DEFINITION_PARSER);
        Contribution myservice_abc = cp.getContribution("myservice-abc", BEAN_DEFINITION_PARSER);
        Contribution myservice_abc_xyz = cp.getContribution("myservice-abc-xyz", BEAN_DEFINITION_DECORATOR);

        assertThat(myservice.getSchemas().toString(),
                containsAll("Schemas[my/services/myservice.xsd, 0 versioned schemas]"));

        assertThat(
                myservice_abc.getSchemas().toString(),
                containsAll("Schemas[missing main schema, 1 versioned schemas]",
                        "version 1.0: Schema[name=my/services/myservice-abc-1.0.xsd, version=1.0, source=",
                        "my/services/myservice-abc-1.0.xsd]"));

        assertThat(
                myservice_abc_xyz.getSchemas().toString(),
                containsAll("Schemas[my/services/myservice-abc-xyz.xsd, 1 versioned schemas]",
                        "version 2.0: Schema[name=my/services/myservice-abc-xyz-2.0.xsd, version=2.0, source=",
                        "my/services/myservice-abc-xyz-2.0.xsd]"));
    }

    private void createConfigurationPoints(String location) {
        cps = new ConfigurationPointsImpl(null, location);
    }
}
