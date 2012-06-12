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

package com.alibaba.citrus.webx.config;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.util.CollectionUtil;
import com.alibaba.citrus.webx.WebxRootController;
import com.alibaba.citrus.webx.config.WebxConfiguration.ComponentConfig;
import com.alibaba.citrus.webx.config.impl.WebxConfigurationImpl.ComponentsConfigImpl;
import com.alibaba.citrus.webx.impl.WebxControllerImpl;
import com.alibaba.citrus.webx.support.AbstractWebxController;
import com.alibaba.citrus.webx.support.AbstractWebxRootController;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

public class WebxConfigurationTests {
    private static ApplicationContext   factory;
    private        WebxConfiguration    conf;
    private        ComponentsConfigImpl config;

    @BeforeClass
    public static void initFactory() throws Exception {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "webx-configuration.xml")));
    }

    @Before
    public void init() {
        conf = (WebxConfiguration) factory.getBean("webxConfiguration");
        assertNotNull(conf);

        config = (ComponentsConfigImpl) conf.getComponentsConfig();
        assertNotNull(config);
    }

    @Test
    public void conf() {
        assertEquals(true, conf.isProductionMode());
        assertEquals("internal", conf.getInternalPathPrefix());
        assertSameBean(factory.getBean("requestContexts"), conf.getRequestContexts());
        assertSameBean(factory.getBean("pipeline"), conf.getPipeline());
        assertNull(conf.getExceptionPipeline());

        String str = conf.toString();

        assertThat(str, containsRegex("productionMode\\s+="));
        assertThat(str, not(containsRegex("internalPathPrefix\\s+=")));
        assertThat(str, not(containsRegex("requestContexts\\s+=")));
        assertThat(str, not(containsRegex("pipeline\\s+=")));
        assertThat(str, not(containsRegex("exceptionPipeline\\s+=")));
    }

    @Test
    public void productionModeSensible() {
        MyBean myBean = (MyBean) factory.getBean("myBean");

        assertEquals(Boolean.TRUE, myBean.productionMode);
    }

    private void assertSameBean(Object targetBean, Object confBean) {
        assertNotNull(confBean);
        assertSame(targetBean, confBean);
    }

    @Test
    public void components_default() {
        assertTrue(config.isAutoDiscoverComponents());
        assertEquals("/WEB-INF/webx-*.xml", config.getComponentConfigurationLocationPattern());
        assertNull(config.getDefaultComponent());
        assertEquals(WebxControllerImpl.class, config.getDefaultControllerClass());
        assertTrue(config.getComponents().isEmpty());
    }

    @Test
    public void components_setValues() {
        // isAutoDiscoverComponents
        config.setAutoDiscoverComponents(true);
        assertTrue(config.isAutoDiscoverComponents());

        config.setAutoDiscoverComponents(false);
        assertFalse(config.isAutoDiscoverComponents());

        // getAutoDiscoveryPattern
        config.setComponentConfigurationLocationPattern(" ");
        assertEquals("/WEB-INF/webx-*.xml", config.getComponentConfigurationLocationPattern());

        config.setComponentConfigurationLocationPattern(null);
        assertEquals("/WEB-INF/webx-*.xml", config.getComponentConfigurationLocationPattern());

        config.setComponentConfigurationLocationPattern(" /test*.xml ");
        assertEquals("/test*.xml", config.getComponentConfigurationLocationPattern());

        // getDefaultComponent
        config.setDefaultComponent(null);
        assertNull(config.getDefaultComponent());

        config.setDefaultComponent(" ");
        assertNull(config.getDefaultComponent());

        config.setDefaultComponent(" test ");
        assertEquals("test", config.getDefaultComponent());

        // getDefaultControllerClass
        config.setDefaultControllerClass(null);
        assertEquals(WebxControllerImpl.class, config.getDefaultControllerClass());

        config.setDefaultControllerClass(String.class);
        assertEquals(String.class, config.getDefaultControllerClass());

        // getComponents
        config.setComponents(CollectionUtil.<String, ComponentConfig>createHashMap());
        assertNotNull(config.getComponents());
    }

    @Test
    public void components_conf() {
        conf = (WebxConfiguration) factory.getBean("components");
        config = (ComponentsConfigImpl) conf.getComponentsConfig();

        assertFalse(config.isAutoDiscoverComponents());
        assertEquals("test-*.xml", config.getComponentConfigurationLocationPattern());
        assertEquals("main", config.getDefaultComponent());
        assertEquals(MyController.class, config.getDefaultControllerClass());

        WebxRootController rootController = config.getRootController();
        assertEquals("test1", ((MyRootController) rootController).getName());

        Map<String, ComponentConfig> components = config.getComponents();

        assertEquals(2, components.size());
        assertEquals("test", ((MyController) components.get("test").getController()).getName());
        assertEquals("test1", ((MyController) components.get("test1").getController()).getName());

        assertEquals(null, components.get("test").getPath());
        assertEquals("/test/111", components.get("test1").getPath());
    }

    public static class MyController extends AbstractWebxController {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean service(RequestContext requestContext) throws Exception {
            return true;
        }
    }

    public static class MyRootController extends AbstractWebxRootController {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        protected boolean handleRequest(RequestContext requestContext) throws Exception {
            return true;
        }
    }
}
