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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.FatalBeanException;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.test.TestEnvStatic;
import com.alibaba.citrus.test.runner.TestNameAware;

@RunWith(TestNameAware.class)
public class ConfigurationPointTests {
    private static final Method instantiateContributionImplementationMethod;
    private ConfigurationPointsImpl cps;

    static {
        TestEnvStatic.init();
        instantiateContributionImplementationMethod = getAccessibleMethod(ConfigurationPointImpl.class,
                "instantiateContributionImplementation", new Class<?>[] { Contribution.class });
    }

    private Object instantiateContributionImplementation(ConfigurationPoint cp, Contribution contrib) throws Exception {
        try {
            return instantiateContributionImplementationMethod.invoke(cp, contrib);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getTargetException();
        }
    }

    @Test
    public void test1_getContributions_noContributions() {
        createConfigurationPoints("TEST-INF/test1/cps");

        ConfigurationPoint cp = cps.getConfigurationPointByName("cp1");

        assertTrue(cp.getContributions().isEmpty());
    }

    @Test
    public void test2_NoClass() throws Exception {
        createConfigurationPoints("TEST-INF/test2/cps");

        // quietly failed
        ConfigurationPoint cp = cps.getConfigurationPointByName("cp1");
        Contribution contrib = cp.getContribution("no-class", BEAN_DEFINITION_PARSER);

        try {
            instantiateContributionImplementation(cp, contrib);
            fail();
        } catch (FatalBeanException e) {
            assertThat(
                    e,
                    exception("not defined", "contributionType=BEAN_DEFINITION_PARSER", "contributionName=no-class",
                            "configurationPoint=cp1", "namespaceUri=http://www.alibaba.com/test2/cp1"));
        }
    }

    @Test
    public void test2_ClassNotFound() throws Exception {
        createConfigurationPoints("TEST-INF/test2/cps");

        // quietly failed
        ConfigurationPoint cp = cps.getConfigurationPointByName("cp1");
        Contribution contrib = cp.getContribution("not-exist-class", BEAN_DEFINITION_PARSER);

        try {
            instantiateContributionImplementation(cp, contrib);
            fail();
        } catch (FatalBeanException e) {
            assertThat(
                    e,
                    exception(ClassNotFoundException.class, "not found", "contributionType=BEAN_DEFINITION_PARSER",
                            "contribuitionClass=com.alibaba.NotExistClass", "contributionName=not-exist-class",
                            "configurationPoint=cp1", "namespaceUri=http://www.alibaba.com/test2/cp1"));
        }
    }

    @Test
    public void test2_WrongClass_beanDefinitionParsers() throws Exception {
        createConfigurationPoints("TEST-INF/test2/cps");

        // quietly failed
        ConfigurationPoint cp = cps.getConfigurationPointByName("cp1");
        Contribution contrib = cp.getContribution("wrong-class", BEAN_DEFINITION_PARSER);

        try {
            instantiateContributionImplementation(cp, contrib);
            fail();
        } catch (FatalBeanException e) {
            assertThat(
                    e,
                    exception("Contribution class does not implement the BeanDefinitionParser interface",
                            "contributionType=BEAN_DEFINITION_PARSER", "contribuitionClass=java.lang.String",
                            "contributionName=wrong-class", "configurationPoint=cp1",
                            "namespaceUri=http://www.alibaba.com/test2/cp1"));
        }
    }

    @Test
    public void test2_WrongClass_beanDefinitionDecorators() throws Exception {
        createConfigurationPoints("TEST-INF/test2/cps");

        // quietly failed
        ConfigurationPoint cp = cps.getConfigurationPointByName("cp1");
        Contribution contrib = cp.getContribution("wrong-class", BEAN_DEFINITION_DECORATOR);

        try {
            instantiateContributionImplementation(cp, contrib);
            fail();
        } catch (FatalBeanException e) {
            assertThat(
                    e,
                    exception("Contribution class does not implement the BeanDefinitionDecorator interface",
                            "contributionType=BEAN_DEFINITION_DECORATOR", "contribuitionClass=java.lang.String",
                            "contributionName=wrong-class", "configurationPoint=cp1",
                            "namespaceUri=http://www.alibaba.com/test2/cp1"));
        }
    }

    @Test
    public void test2_WrongClass_beanDefinitionDecoratorsForAttribute() throws Exception {
        createConfigurationPoints("TEST-INF/test2/cps");

        // quietly failed
        ConfigurationPoint cp = cps.getConfigurationPointByName("cp1");
        Contribution contrib = cp.getContribution("wrong-class", BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE);

        try {
            instantiateContributionImplementation(cp, contrib);
            fail();
        } catch (FatalBeanException e) {
            assertThat(
                    e,
                    exception("Contribution class does not implement the BeanDefinitionDecorator interface",
                            "contributionType=BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE",
                            "contribuitionClass=java.lang.String", "contributionName=wrong-class",
                            "configurationPoint=cp1", "namespaceUri=http://www.alibaba.com/test2/cp1"));
        }
    }

    @Test
    public void test6_toString_and_getContribution() {
        createConfigurationPoints("TEST-INF/test6/cps");

        ConfigurationPointImpl cp = (ConfigurationPointImpl) cps.getConfigurationPointByName("cp1");

        assertEquals(6, cp.getContributions().size());

        String str = cp.toString();

        System.out.println("--");
        System.out.println(str);

        assertThat(str, containsString("ConfigurationPoint[cp1=http://www.alibaba.com/test6/cp1"));
        assertThat(str, containsString("loaded contributions from TEST-INF/test6/cp1."));

        int i = 0;
        String[] strs = {
                "Contribution[toConfigurationPoint=cp1, name=my1, type=BEAN_DEFINITION_PARSER, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionParser]",
                "Contribution[toConfigurationPoint=cp1, name=my1, type=BEAN_DEFINITION_DECORATOR, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionDecorator]",
                "Contribution[toConfigurationPoint=cp1, name=my1, type=BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionDecorator]",
                "Contribution[toConfigurationPoint=cp1, name=my2, type=BEAN_DEFINITION_PARSER, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionParser2]",
                "Contribution[toConfigurationPoint=cp1, name=my2, type=BEAN_DEFINITION_DECORATOR, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionDecorator2]",
                "Contribution[toConfigurationPoint=cp1, name=my2, type=BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionDecorator2]" };

        for (Contribution contrib : cp.getContributions()) {
            assertThat(contrib.toString(), containsString(strs[i++]));
        }

        i = 0;
        assertThat(cp.getContribution("my1", BEAN_DEFINITION_PARSER).toString(), containsString(strs[i++]));
        assertThat(cp.getContribution("my1", BEAN_DEFINITION_DECORATOR).toString(), containsString(strs[i++]));
        assertThat(cp.getContribution("my1", BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE).toString(),
                containsString(strs[i++]));

        assertThat(cp.getContribution("my2", BEAN_DEFINITION_PARSER).toString(), containsString(strs[i++]));
        assertThat(cp.getContribution("my2", BEAN_DEFINITION_DECORATOR).toString(), containsString(strs[i++]));
        assertThat(cp.getContribution("my2", BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE).toString(),
                containsString(strs[i++]));

        assertEquals(null, cp.getContribution("my3", BEAN_DEFINITION_PARSER));

        try {
            cp.getContribution(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("name"));
        }

        try {
            cp.getContribution("name", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("type"));
        }
    }

    @Test
    public void test7_toString_nestedName() {
        createConfigurationPoints("TEST-INF/test7/cps");

        ConfigurationPointImpl cp = (ConfigurationPointImpl) cps.getConfigurationPointByName("dir/cp1");

        assertEquals(6, cp.getContributions().size());

        String str = cp.toString();

        System.out.println("--");
        System.out.println(str);

        assertThat(str, containsString("ConfigurationPoint[dir/cp1=http://www.alibaba.com/test7/dir/cp1"));
        assertThat(str, containsString("loaded contributions from TEST-INF/test7/dir-cp1."));

        int i = 0;
        String[] strs = {
                "Contribution[toConfigurationPoint=dir/cp1, name=my1, type=BEAN_DEFINITION_PARSER, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionParser]",
                "Contribution[toConfigurationPoint=dir/cp1, name=my1, type=BEAN_DEFINITION_DECORATOR, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionDecorator]",
                "Contribution[toConfigurationPoint=dir/cp1, name=my1, type=BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionDecorator]",
                "Contribution[toConfigurationPoint=dir/cp1, name=my2, type=BEAN_DEFINITION_PARSER, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionParser]",
                "Contribution[toConfigurationPoint=dir/cp1, name=my2, type=BEAN_DEFINITION_DECORATOR, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionDecorator]",
                "Contribution[toConfigurationPoint=dir/cp1, name=my2, type=BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE, class=com.alibaba.citrus.springext.contrib.MyBeanDefinitionDecorator]" };

        for (Contribution contrib : cp.getContributions()) {
            assertThat(contrib.toString(), containsString(strs[i++]));
        }
    }

    @Test
    public void test10_defaultElement() {
        createConfigurationPoints("TEST-INF/test10/cps");

        ConfigurationPointImpl cp1 = (ConfigurationPointImpl) cps.getConfigurationPointByName("cp1");
        ConfigurationPointImpl cp2 = (ConfigurationPointImpl) cps.getConfigurationPointByName("cp2");

        assertEquals(null, cp1.getDefaultElementName()); // no default element
        assertEquals("mybean", cp2.getDefaultElementName());
    }

    @Test
    public void test10_contribNameSameAsDefaultElementName() {
        createConfigurationPoints("TEST-INF/test10/cps2");

        try {
            cps.getConfigurationPointByName("cp1");
            fail();
        } catch (FatalBeanException e) {
            assertThat(
                    e,
                    exception("Contribution has a same name as the default element name for configuration point",
                            "contributionType=BEAN_DEFINITION_PARSER",
                            "contribuitionClass=com.alibaba.citrus.springext.contrib.MyBeanDefinitionParser",
                            "contributionName=mybean", "configurationPoint=cp1",
                            "namespaceUri=http://www.alibaba.com/test10/cp1"));
        }
    }

    @Test
    public void test11_illegal_ns_format() {
        createConfigurationPoints("TEST-INF/test11/cps1");

        try {
            cps.getConfigurationPointByName("cp1");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Illegal namespace URI", "http://www.alibaba.com/test11/cp1, illegal"));
        }

        createConfigurationPoints("TEST-INF/test11/cps2");

        try {
            cps.getConfigurationPointByName("cp2");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Illegal namespace URI", "http://www.alibaba.com/test11/cp2, =mybean"));
        }

        createConfigurationPoints("TEST-INF/test11/cps3");

        try {
            cps.getConfigurationPointByName("cp3");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Illegal namespace URI", ";defaultElement=mybean"));
        }
    }

    private void createConfigurationPoints(String location) {
        cps = new ConfigurationPointsImpl(null, location);
    }
}
