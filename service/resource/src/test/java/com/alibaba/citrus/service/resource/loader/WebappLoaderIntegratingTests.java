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
package com.alibaba.citrus.service.resource.loader;

import static com.alibaba.citrus.service.resource.ResourceLoadingService.*;
import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.citrus.service.resource.AbstractResourceLoadingTests;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.ResourceNotFoundException;
import com.alibaba.citrus.util.io.StreamUtil;

public class WebappLoaderIntegratingTests extends AbstractResourceLoadingTests {
    @BeforeClass
    public static void initClass() throws Exception {
        initFactory("loader/webapp-loader.xml");
    }

    @Before
    public void init() throws Exception {
        resourceLoadingService = (ResourceLoadingService) factory.getBean("resourceLoadingService");
    }

    @Test
    public void getResource() throws Exception {
        assertResourceServiceList("/webroot", "", true, true, "WEB-INF/", "appcontext/", "beans.xml", "filter/",
                "loader/", "logback.xml", "myfolder/", "resources-root.xml", "test.txt");
        assertResourceServiceList("/webroot/test.txt", "test.txt", true, false);
        assertResourceServiceList("/webroot/WEB-INF/", "WEB-INF", true, true, "aaa/", "resources.xml", "web.xml");
        assertResourceServiceList("/webroot/WEB-INF/web.xml", "WEB-INF/web.xml", true, false);

        assertResourceServiceList("/webroot/notexist.txt", "notexist.txt", false, false);

        // webapp-loader不支持for_create选项
        try {
            resourceLoadingService.getResource("/webroot/not/found", FOR_CREATE);
            fail();
        } catch (ResourceNotFoundException e) {
        }
    }

    @Test
    public void factoryBean_init_recursively() {
        // 解决了如下的问题：
        // ---------------
        // 原先，webapp-loader通过inject by Type，将servletContext注入constructor。
        // 这会触发所有FactoryBean的创建，以便取得factoryBean.getObjectType()。
        // 假如getObjectType()依赖于初始化才能返回结果（例如webx2 serviceFactoryBean），那么spring会继续初始化factoryBean。
        // 假如factoryBean的初始化又触发了resource loading机制，由于此时resource loading还没初始化完，因此不能工作，导致找不到资源文件，
        // 或只能找到Spring默认的资源（如ServletResource）。
        // ---------------
        // 现在，我们通过ServletContextAware接口注入servletContext，这样便不会触发factoryBean的初始化。
        // 从而确保factoryBean在初始化前，Resource loading机制已经初始化并可用，可以取得资源。
        assertEquals("test", factory.getBean("myTest"));
    }

    public static class FactoryBeanUsingResource implements FactoryBean, InitializingBean {
        private Class<?> type;
        private URL location;
        private String text;

        public void setLocation(URL location) {
            this.location = location;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }

        public void afterPropertiesSet() throws Exception {
            this.text = StreamUtil.readText(location.openStream(), "UTF-8", true);
        }

        public Object getObject() throws Exception {
            return text;
        }

        public Class<?> getObjectType() {
            return type;
        }

        public boolean isSingleton() {
            return true;
        }
    }
}
