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
package com.alibaba.citrus.springext.support;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.springext.support.context.XmlBeanFactory;

/**
 * ≤‚ ‘<code>BeanSupport</code>ª˘¿‡°£
 * 
 * @author Michael Zhou
 */
public class BeanSupportTests {
    protected static BeanFactory factory;
    protected MyServiceImpl myService;

    @BeforeClass
    public static void initFactory() {
        factory = new XmlBeanFactory(new FileSystemResource(new File(srcdir, "services.xml")));
    }

    @Before
    public void init() {
        myService = (MyServiceImpl) factory.getBean("myFavoriteService", MyServiceImpl.class);
    }

    @Test
    public void getBeanInterface() {
        assertEquals(MyService.class, myService.getBeanInterface());
    }

    @Test
    public void getBeanInterface_raw() {
        assertEquals(Service.class, new MyServiceImpl_raw().getBeanInterface());
    }

    @Test
    public void getBeanName() {
        assertEquals("myFavoriteService", myService.getBeanName());
    }

    @Test
    public void assertInitialized() {
        myService.assertInitialized();
        assertTrue(myService.isInitialized());
    }

    @Test
    public void assertInitializedFailed() {
        try {
            new MyServiceImpl().assertInitialized();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Bean instance of " + MyService.class.getName() + " has not been initialized yet"));
        }
    }

    @Test
    public void dispose() throws Exception {
        BeanSupport service = new MyServiceImpl();
        assertFalse(service.isInitialized());

        service.afterPropertiesSet();
        assertTrue(service.isInitialized());

        service.destroy();
        assertFalse(service.isInitialized());
    }

    @Test
    public void toString_() {
        MyServiceImpl bean = new MyServiceImpl();

        // with beanName, simple
        assertEquals("myFavoriteService:MyService", myService.toString());
        assertEquals("myFavoriteService:MyService", myService.getBeanDescription());
        assertEquals("myFavoriteService:MyService", myService.getBeanDescription(true));

        // with beanName, long
        assertEquals("myFavoriteService:" + MyService.class.getName(), myService.getBeanDescription(false));

        // no beanName, simple
        assertEquals("MyService", bean.toString());
        assertEquals("MyService", bean.getBeanDescription());
        assertEquals("MyService", bean.getBeanDescription(true));

        // no beanName, long
        assertEquals(MyService.class.getName(), bean.getBeanDescription(false));

        // inner bean
        bean.setBeanName("(inner bean)111");
        assertEquals("MyService", bean.toString());
        assertEquals("MyService", bean.getBeanDescription());
        assertEquals("MyService", bean.getBeanDescription(true));
    }

    public static interface Service {
    }

    public static interface MyService extends Service {
    }

    public static class AbstractService<S extends Service> extends GenericBeanSupport<S> {
    }

    public static class MyServiceImpl extends AbstractService<MyService> implements MyService {
    }

    @SuppressWarnings("rawtypes")
    public static class MyServiceImpl_raw extends AbstractService implements MyService {
    }
}
