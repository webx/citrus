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
package com.alibaba.citrus.springext.support.context;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;

public abstract class AbstractBeanFactoryTests {
    private SimpleDateFormat format;
    private Object anyBean;

    protected abstract BeanFactory getFactory();

    @Before
    public void init() throws Exception {
        Map<?, ?> container = (Map<?, ?>) getFactory().getBean("container");

        format = (SimpleDateFormat) container.get("dateFormat");
        anyBean = container.get("anyBean");
    }

    @Test
    public void allowBeanDefinitionOverriding() {
        BeanFactory factory = getFactory();
        DefaultListableBeanFactory listableFactory = null;

        if (factory instanceof DefaultListableBeanFactory) {
            listableFactory = (DefaultListableBeanFactory) factory;
        } else if (factory instanceof AbstractApplicationContext) {
            listableFactory = (DefaultListableBeanFactory) ((AbstractApplicationContext) factory).getBeanFactory();
        }

        if (listableFactory != null) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(String.class);

            listableFactory.registerBeanDefinition("test", builder.getBeanDefinition());

            try {
                listableFactory.registerBeanDefinition("test", builder.getBeanDefinition());
            } catch (BeanDefinitionStoreException e) {
                assertThat(e, exception("test"));
            }
        }
    }

    @Test
    public void dateFormat() {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, 2009);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DAY_OF_MONTH, 12);

        Date date = cal.getTime();

        assertEquals("2009-03-12", format.format(date));
    }

    @Test
    public void anyBean() {
        assertEquals("testString", anyBean);
    }

    @Test
    public void defaultElement_standalone() {
        Date date = (Date) getFactory().getBean("anyBean2");
        assertEquals(0, date.getTime());
    }

    @Test
    public void defaultElement_ref() {
        Date date = (Date) getFactory().getBean("anyBean3");
        assertEquals(0, date.getTime());

        assertSame(getFactory().getBean("anyBean2"), date);
    }
}
