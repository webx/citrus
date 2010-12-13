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
package com.alibaba.citrus.springext.util;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.beans.factory.config.BeanDefinition.*;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.impl.ConfigurationPointImpl;
import com.alibaba.citrus.springext.impl.ConfigurationPointsImpl;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.springext.support.context.XmlBeanFactory;
import com.alibaba.citrus.springext.util.SpringExtUtil.ConstructorArg;

public class SpringExtUtilTests {
    private static BeanFactory factory;
    private ConfigurationPointsImpl cps;
    private HttpServletRequest request;

    @BeforeClass
    public static void initFactory() {
        factory = new XmlBeanFactory(new FileSystemResource(new File(srcdir, "beans.xml")));
    }

    @Test
    public void autowireAndInitialize() throws Exception {
        ApplicationContext context = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "beans.xml")));

        class Bean implements BeanNameAware, ApplicationContextAware, InitializingBean {
            @Autowired
            private com.alibaba.citrus.springext.support.context.MyClass myClass;
            private String beanName;
            private ApplicationContext context;
            private boolean inited;

            public void setBeanName(String name) {
                this.beanName = name;
            }

            public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
                this.context = applicationContext;
            }

            public void afterPropertiesSet() throws Exception {
                this.inited = true;
            }
        }

        Bean bean = new Bean();

        assertSame(bean,
                SpringExtUtil.autowireAndInitialize(bean, context, AutowireCapableBeanFactory.AUTOWIRE_NO, "myname"));

        assertNotNull(bean.myClass);
        assertEquals("myname", bean.beanName);
        assertSame(context, bean.context);
        assertTrue(bean.inited);
    }

    @Test
    public void getBeanByType() {
        Map<?, ?> container = SpringExtUtil.getBeanOfType(factory, Map.class);

        assertNotNull(container);
        assertEquals(4, container.size());

        String str = SpringExtUtil.getBeanOfType(factory, String.class);
        assertNull(str);

        List<Object> list = createArrayList(container.values());

        // 包含一个str, 一个dateformat，两个date
        for (Iterator<Object> i = list.iterator(); i.hasNext();) {
            Object o = i.next();

            if (o instanceof String) {
                i.remove();
            } else if (o instanceof DateFormat) {
                i.remove();
            } else if (o instanceof Date) {
                long t = ((Date) o).getTime();

                assertTrue(t == 1 || t == 0);
                i.remove();
            } else {
                fail();
            }
        }

        assertTrue(list.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCp_Contrib_Null() throws Exception {
        SpringExtUtil.getSiblingConfigurationPoint("cp1", (Contribution) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCp_Cp_Null() throws Exception {
        SpringExtUtil.getSiblingConfigurationPoint("cp1", (ConfigurationPoint) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCp_Name_Null() throws Exception {
        createConfigurationPoints("TEST-INF/test6/cps");
        ConfigurationPointImpl cp = (ConfigurationPointImpl) cps.getConfigurationPointByName("cp1");

        SpringExtUtil.getSiblingConfigurationPoint(null, cp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCp_Name_notFound() throws Exception {
        createConfigurationPoints("TEST-INF/test6/cps");
        ConfigurationPointImpl cp = (ConfigurationPointImpl) cps.getConfigurationPointByName("cp1");
        Contribution contrib = cp.getContributions().iterator().next();

        SpringExtUtil.getSiblingConfigurationPoint("not-found", contrib);
    }

    @Test
    public void test6_getCp() throws Exception {
        createConfigurationPoints("TEST-INF/test6/cps");
        ConfigurationPointImpl cp = (ConfigurationPointImpl) cps.getConfigurationPointByName("cp1");
        Contribution contrib = cp.getContributions().iterator().next();

        assertSame(cp, SpringExtUtil.getSiblingConfigurationPoint("cp1", contrib));
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateBeanName_null() throws Exception {
        SpringExtUtil.generateBeanName(null, (BeanDefinitionRegistry) factory);
    }

    @Test
    public void generateBeanName() throws Exception {
        // newName在container中尚不存在，直接返回。
        assertEquals("newName", SpringExtUtil.generateBeanName("newName", (BeanDefinitionRegistry) factory));
        assertEquals("newName",
                SpringExtUtil.generateBeanName("newName", (BeanDefinitionRegistry) factory, null, false));

        // testName和testName#0已经在container中存在，故返回testName#1。
        assertEquals("testName#1", SpringExtUtil.generateBeanName("testName", (BeanDefinitionRegistry) factory));
        assertEquals("testName#1",
                SpringExtUtil.generateBeanName("testName", (BeanDefinitionRegistry) factory, null, false));
    }

    @Test
    public void generateInnerBeanName() throws Exception {
        assertTrue(SpringExtUtil.generateBeanName("newName", (BeanDefinitionRegistry) factory,
                BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition(), true).matches("newName#[a-z0-9]+"));

        assertTrue(SpringExtUtil.generateBeanName("testName", (BeanDefinitionRegistry) factory,
                BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition(), true).matches("testName#[a-z0-9]+"));
    }

    private void createConfigurationPoints(String location) {
        cps = new ConfigurationPointsImpl(null, location);
    }

    @Test
    public void createConstructorArg_args() {
        // no beanType
        try {
            SpringExtUtil.createConstructorArg(null, true, 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("beanType"));
        }

        // argTypes not specified, but has more than 1 constructors
        try {
            SpringExtUtil.createConstructorArg(MyClass.class, true, 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("2 constructors found, please specify argTypes"));
        }

        // argTypes not specified
        SpringExtUtil.createConstructorArg(MyClass2.class, true, 0);
        SpringExtUtil.createConstructorArg(MyClass2.class, true, 1);

        // argIndex out of bound
        try {
            SpringExtUtil.createConstructorArg(MyClass.class, true, -1, HttpServletRequest.class, MyObject.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("argIndex is out of bound: -1"));
        }

        try {
            SpringExtUtil.createConstructorArg(MyClass.class, true, 2, HttpServletRequest.class, MyObject.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("argIndex is out of bound: 2"));
        }

        // constructor not found
        try {
            SpringExtUtil.createConstructorArg(MyClass.class, true, String.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(NoSuchMethodException.class, "Could not find constructor"));
        }
    }

    @Test
    public void createConstructorArg_BeanFactory() throws Exception {
        BeanFactory context = createMock(BeanFactory.class);

        // not ApplicationContext or ConfigurableBeanFactory, required
        ConstructorArg arg = new ConstructorArg(MyClass.class.getConstructors()[0], HttpServletRequest.class, 0, true);
        arg.setBeanFactory(context);

        try {
            arg.getObject();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("could not get object of " + HttpServletRequest.class.getName()
                    + ": no Application Context"));
        }

        // not ApplicationContext or ConfigurableBeanFactory, optional
        arg = new ConstructorArg(MyClass.class.getConstructors()[0], HttpServletRequest.class, 0, false);
        arg.setBeanFactory(context);
        assertNull(arg.getObject());
    }

    @Test
    public void createConstructorArg_ConfigurableBeanFactory() {
        createConstructorArg("configbf");
    }

    @Test
    public void createConstructorArg_ApplicationContext() {
        createConstructorArg("appcontext");
    }

    private void createConstructorArg(String contextType) {
        BeanFactory context;
        MyClass myClass;

        // optional arg, no value injected
        context = createApplicationContext_forConstructorArg(contextType, false, false, true);
        myClass = (MyClass) context.getBean("myClass");
        assertNull(myClass.getRequest());
        assertNull(myClass.getObject());

        // optional arg, with value injected
        context = createApplicationContext_forConstructorArg(contextType, false, true, true);
        myClass = (MyClass) context.getBean("myClass");
        assertSame(request, myClass.getRequest());
        assertNotNull(myClass.getObject());

        // required arg, no value injected
        try {
            context = createApplicationContext_forConstructorArg(contextType, true, false, true); // appcontext.refresh时报错
            context.getBean("myClass"); // beanfactory.getBean时报错
            fail();
        } catch (BeanCreationException e) {
            assertThat(e, exception(NoSuchBeanDefinitionException.class, "myClass"));

            @SuppressWarnings("unchecked")
            Matcher<Throwable> m = anyOf(exception(NoSuchBeanDefinitionException.class, "HttpServletRequest"),
                    exception(NoSuchBeanDefinitionException.class, "MyObject"));

            assertThat(e, m);
        }

        // required arg, with value injected
        context = createApplicationContext_forConstructorArg(contextType, true, true, true);
        myClass = (MyClass) context.getBean("myClass");
        assertSame(request, myClass.getRequest());
        assertNotNull(myClass.getObject());

        // required arg, with value injected
        context = createApplicationContext_forConstructorArg(contextType, true, true, true);
        myClass = (MyClass) context.getBean("myClass");
        assertSame(request, myClass.getRequest());
        assertNotNull(myClass.getObject());

        // required arg, with value injected, prototype
        context = createApplicationContext_forConstructorArg(contextType, true, true, false);
        MyClass myClass1 = (MyClass) context.getBean("myClass");
        MyClass myClass2 = (MyClass) context.getBean("myClass");
        assertSame(request, myClass1.getRequest());
        assertSame(request, myClass2.getRequest());
        assertNotNull(myClass1.getObject());
        assertNotNull(myClass2.getObject());
        assertNotSame(myClass1.getObject(), myClass2.getObject());
    }

    private BeanFactory createApplicationContext_forConstructorArg(String contextType, boolean required,
                                                                   boolean withValue, boolean singleton) {
        BeanDefinitionRegistry context = null;

        if ("appcontext".equals(contextType)) {
            context = new GenericApplicationContext();
        } else {
            context = new DefaultListableBeanFactory();
        }

        // create MyClass bean
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MyClass.class);
        builder.setScope(singleton ? SCOPE_SINGLETON : SCOPE_PROTOTYPE);

        // construct with an optional values of type HttpServletRequest and MyObject
        SpringExtUtil.addConstructorArg(builder, required, 0, HttpServletRequest.class, MyObject.class);

        builder.addConstructorArgValue(SpringExtUtil.createConstructorArg(MyClass.class, required, 1,
                HttpServletRequest.class, MyObject.class));

        // register bean
        context.registerBeanDefinition("myClass", builder.getBeanDefinition());

        if (withValue) {
            // register value as a resolvableDependency
            request = createMock(HttpServletRequest.class);

            try {
                ConfigurableListableBeanFactory clbf;

                try {
                    clbf = (ConfigurableListableBeanFactory) context;
                } catch (ClassCastException e) {
                    clbf = (ConfigurableListableBeanFactory) ((ApplicationContext) context)
                            .getAutowireCapableBeanFactory();
                }

                clbf.registerResolvableDependency(HttpServletRequest.class, request);
            } catch (ClassCastException e) {
            }

            // register value as a bean
            BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(MyObject.class);
            bean.setScope(SCOPE_PROTOTYPE);
            context.registerBeanDefinition("myObject", bean.getBeanDefinition());
        }

        // start context
        if (context instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) context).refresh();
        }

        return (BeanFactory) context;
    }

    @Test
    public void createPropertyRef_args() {
        // no beanName
        try {
            SpringExtUtil.createOptionalPropertyRef(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("beanName"));
        }

        try {
            SpringExtUtil.createOptionalPropertyRef("  ", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("beanName"));
        }

        // no beanType
        try {
            SpringExtUtil.createOptionalPropertyRef("myBean", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("beanType"));
        }
    }

    @Test
    public void createPropertyRef() {
        ApplicationContext context;
        MyClass myClass;

        // required, no Value
        try {
            createApplicationContext_forPropertyRef(true, false, true);
            fail();
        } catch (BeanCreationException e) {
            assertThat(e, exception(NoSuchBeanDefinitionException.class, "myClass", "myObject"));
        }

        // optional, no Value
        context = createApplicationContext_forPropertyRef(false, false, true);
        myClass = (MyClass) context.getBean("myClass");
        assertNull(myClass.getObject());

        // required, with Value
        context = createApplicationContext_forPropertyRef(true, true, true);
        myClass = (MyClass) context.getBean("myClass");
        assertNotNull(myClass.getObject());

        // optional, with Value
        context = createApplicationContext_forPropertyRef(false, true, true);
        myClass = (MyClass) context.getBean("myClass");
        assertNotNull(myClass.getObject());

        // prototype
        context = createApplicationContext_forPropertyRef(false, true, false);
        MyClass myClass1 = (MyClass) context.getBean("myClass");
        MyClass myClass2 = (MyClass) context.getBean("myClass");
        assertNotNull(myClass1.getObject());
        assertNotNull(myClass2.getObject());
        assertNotSame(myClass1.getObject(), myClass2.getObject());
    }

    private ApplicationContext createApplicationContext_forPropertyRef(boolean required, boolean withValue,
                                                                       boolean singleton) {
        GenericApplicationContext context = new GenericApplicationContext();

        // create MyClass bean
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MyClass.class);
        builder.setScope(singleton ? SCOPE_SINGLETON : SCOPE_PROTOTYPE);
        SpringExtUtil.addPropertyRef(builder, "object", "myObject", MyObject.class, required);
        context.registerBeanDefinition("myClass", builder.getBeanDefinition());

        if (withValue) {
            // register value as a bean
            BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(MyObject.class);
            bean.setScope(SCOPE_PROTOTYPE);
            context.registerBeanDefinition("myObject", bean.getBeanDefinition());
        }

        // start context
        context.refresh();

        return context;
    }

    public static class MyClass {
        private HttpServletRequest request;
        private MyObject object;

        public MyClass() {
        }

        public MyClass(HttpServletRequest request, MyObject object) {
            this.request = request;
            this.object = object;
        }

        public HttpServletRequest getRequest() {
            return request;
        }

        public void setRequest(HttpServletRequest request) {
            this.request = request;
        }

        public MyObject getObject() {
            return object;
        }

        public void setObject(MyObject object) {
            this.object = object;
        }
    }

    public static class MyClass2 extends MyClass {
        public MyClass2(HttpServletRequest request, MyObject object) {
            super(request, object);
        }
    }

    public static class MyObject {
    }
}
