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
import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionType;
import com.alibaba.citrus.springext.contrib.MyBeanDefinitionDecorator;
import com.alibaba.citrus.springext.contrib.MyBeanDefinitionDecorator2;
import com.alibaba.citrus.springext.contrib.MyBeanDefinitionParser;
import com.alibaba.citrus.springext.contrib.MyBeanDefinitionParser2;
import com.alibaba.citrus.springext.contrib.simple.Test1;
import com.alibaba.citrus.springext.contrib.simple.Test2;
import com.alibaba.citrus.springext.contrib.simple.Test3;
import com.alibaba.citrus.springext.contrib.simple.Test4;
import com.alibaba.citrus.springext.support.SchemaUtil;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.test.TestEnvStatic;
import com.alibaba.citrus.test.runner.TestNameAware;

@RunWith(TestNameAware.class)
public class ContributionTests {
    private ConfigurationPointsImpl cps;

    static {
        TestEnvStatic.init();
    }

    @Test
    public void test6_contributionAware() throws Exception {
        createConfigurationPoints("TEST-INF/test6/cps");

        ConfigurationPointImpl cp = (ConfigurationPointImpl) cps.getConfigurationPointByName("cp1");

        assertEquals(6, cp.getContributions().size());
        assertSame(cps, cp.getConfigurationPoints());

        Iterator<Contribution> i = cp.getContributions().iterator();
        Contribution contrib;

        // cp1:my1, MyBeanDefinitionParser
        contrib = i.next();
        MyBeanDefinitionParser c1 = getContributionImplementation(cp, BEAN_DEFINITION_PARSER, "my1",
                MyBeanDefinitionParser.class);

        assertNotNull(c1);

        // cp1:my1, MyBeanDefinitionDecorator
        contrib = i.next();
        MyBeanDefinitionDecorator c2 = getContributionImplementation(cp, BEAN_DEFINITION_DECORATOR, "my1",
                MyBeanDefinitionDecorator.class);

        assertNotNull(c2);
        assertSame(cp, contrib.getConfigurationPoint());

        // cp1:my1, MyBeanDefinitionDecorator
        contrib = i.next();
        MyBeanDefinitionDecorator c3 = getContributionImplementation(cp, BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE,
                "my1", MyBeanDefinitionDecorator.class);

        assertNotNull(c3);
        assertSame(cp, contrib.getConfigurationPoint());

        // cp1:my2, MyBeanDefinitionParser2
        contrib = i.next();
        MyBeanDefinitionParser2 c4 = getContributionImplementation(cp, BEAN_DEFINITION_PARSER, "my2",
                MyBeanDefinitionParser2.class);

        assertSame(contrib, c4.getContribution());
        assertSame(cp, contrib.getConfigurationPoint());

        // cp1:my2, MyBeanDefinitionDecorator2
        contrib = i.next();
        MyBeanDefinitionDecorator2 c5 = getContributionImplementation(cp, BEAN_DEFINITION_DECORATOR, "my2",
                MyBeanDefinitionDecorator2.class);

        assertSame(contrib, c5.getContribution());
        assertSame(cp, contrib.getConfigurationPoint());

        // cp1:my2, MyBeanDefinitionDecorator2
        contrib = i.next();
        MyBeanDefinitionDecorator2 c6 = getContributionImplementation(cp, BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE,
                "my2", MyBeanDefinitionDecorator2.class);

        assertSame(contrib, c6.getContribution());
        assertSame(cp, contrib.getConfigurationPoint());
    }

    @Test
    public void expandConfigurationPointElements() throws Exception {
        createConfigurationPoints(null);

        ConfigurationPoint cp1 = cps.getConfigurationPointByName("my/cp1");
        ConfigurationPoint cp2 = cps.getConfigurationPointByName("my/cp2");

        String test1 = getSchemaText(cp1.getContribution("test1", BEAN_DEFINITION_PARSER));
        String test2 = getSchemaText(cp1.getContribution("test2", BEAN_DEFINITION_PARSER));
        String test3 = getSchemaText(cp2.getContribution("test3", BEAN_DEFINITION_PARSER));
        String test4 = getSchemaText(cp2.getContribution("test4", BEAN_DEFINITION_PARSER));

        // 假如ns已经import了，确保不重复import；确保不import自己所在的cp。
        assertSchemaText(test1, 1);
        assertSchemaText(test2, 2);
        assertSchemaText(test3, 3);
        assertSchemaText(test4, 4);
    }

    private void assertSchemaText(String text, int caseNo) {
        text = text.replaceAll("\\s+", " "); // 去除换行，便于检查

        String import_cp1 = "<xsd:import namespace=\"http://www.alibaba.com/schema/my/cp1\"";
        String import_cp1_location = "<xsd:import namespace=\"http://www.alibaba.com/schema/my/cp1\" schemaLocation=\"my-cp1.xsd\"/>";

        String import_cp2 = "<xsd:import namespace=\"http://www.alibaba.com/schema/my/cp2\"";
        String import_cp2_location = "<xsd:import namespace=\"http://www.alibaba.com/schema/my/cp2\" schemaLocation=\"my-cp2.xsd\"/>";
        String import_cp2_specificLocation = "<xsd:import namespace=\"http://www.alibaba.com/schema/my/cp2\" schemaLocation=\"http://localhost:8080/schema/my-cp2.xsd\"/>";

        String ns_cp1 = "<xsd:schema.+xmlns:cp1=\"http://www\\.alibaba\\.com/schema/my/cp1\"";
        String ns_cp2 = "<xsd:schema.+xmlns:cp2ns=\"http://www\\.alibaba\\.com/schema/my/cp2\"";

        String any_cp1 = "<xsd:choice>" // 
                + " <xsd:element ref=\"cp1:test1\"/>" //  
                + " <xsd:element ref=\"cp1:test2\"/>" // 
                + " <xsd:element ref=\"cp1:object1\"/>" // 
                + " </xsd:choice>";

        String any_cp1_optional = "<xsd:choice minOccurs=\"0\" maxOccurs=\"unbounded\">" // 
                + " <xsd:element ref=\"cp1:test1\"/>" //  
                + " <xsd:element ref=\"cp1:test2\"/>" // 
                + " <xsd:element ref=\"cp1:object1\"/>" // 
                + " </xsd:choice>";

        String any_cp2_optional = "<xsd:choice minOccurs=\"0\" maxOccurs=\"unbounded\">" // 
                + " <xsd:element ref=\"cp2ns:test3\"/>" //  
                + " <xsd:element ref=\"cp2ns:test4\"/>" // 
                + " </xsd:choice>";

        switch (caseNo) {
            case 1:
                assertThat(text, not(containsString(import_cp1)));
                assertThat(text, containsString(import_cp2_specificLocation));

                assertThat(text, containsRegex(ns_cp1));
                assertThat(text, containsRegex(ns_cp2));

                assertThat(text, containsString(any_cp1_optional));
                assertThat(text, containsString(any_cp2_optional));
                break;

            case 2:
                assertThat(text, not(containsString(import_cp1)));
                assertThat(text, containsString(import_cp2_location));

                assertThat(text, containsRegex(ns_cp1));
                assertThat(text, containsRegex(ns_cp2));

                assertThat(text, containsString(any_cp1_optional));
                assertThat(text, containsString(any_cp2_optional));
                break;

            case 3:
            case 4:
                assertThat(text, containsString(import_cp1_location));
                assertThat(text, not(containsString(import_cp2)));

                assertThat(text, containsRegex(ns_cp1));
                assertThat(text, not(containsRegex(ns_cp2)));

                assertThat(text, containsString(any_cp1));
                assertThat(text, not(containsString(any_cp2_optional)));
                break;

            default:
                fail();
                return;
        }
    }

    private String getSchemaText(Contribution contrib) throws IOException {
        return SchemaUtil.getDocumentText(contrib.getSchemas().getMainSchema().getDocument(), null);
    }

    @Test
    public void parse_import_each_other() throws Exception {
        ApplicationContext ctx = new XmlApplicationContext(new FileSystemResource(new File(srcdir,
                "test-import-each-other.xml")));

        Test1 test1 = new Test1(new Test3(new Test1()), new Test4(new Test2()));
        Test3 test3 = new Test3(new Test1());

        assertEquals(test1, ctx.getBean("test1"));
        assertEquals(test3, ctx.getBean("test3"));
    }

    private <T> T getContributionImplementation(ConfigurationPointImpl cp, ContributionType type, String name,
                                                Class<T> expectedClass) throws Exception {
        Map<?, ?> mapField;

        switch (type) {
            case BEAN_DEFINITION_PARSER:
                mapField = getFieldValue(cp, "parsers", Map.class);
                break;

            case BEAN_DEFINITION_DECORATOR:
                mapField = getFieldValue(cp, "decorators", Map.class);
                break;

            case BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE:
                mapField = getFieldValue(cp, "attributeDecorators", Map.class);
                break;

            default:
                fail();
                return null;
        }

        assertNotNull(mapField);

        return expectedClass.cast(mapField.get(name));
    }

    private void createConfigurationPoints(String location) {
        cps = new ConfigurationPointsImpl(null, location);
    }
}
