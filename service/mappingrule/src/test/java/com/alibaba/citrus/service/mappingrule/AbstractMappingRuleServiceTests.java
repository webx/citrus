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

package com.alibaba.citrus.service.mappingrule;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.easymock.EasyMock.createMock;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

public abstract class AbstractMappingRuleServiceTests {
    protected static ApplicationContext factory;
    protected        MappingRuleService mappingRules;

    protected final static XmlApplicationContext createBeanFactory(String configLocation) throws Exception {
        return createBeanFactory(configLocation, null);
    }

    protected final static XmlApplicationContext createBeanFactory(String configLocation, ApplicationContext parent)
            throws Exception {
        return new XmlApplicationContext(new FileSystemResource(new File(srcdir, configLocation)), parent);
    }

    @BeforeClass
    public static void initFactory() throws Exception {
        factory = createBeanFactory("services.xml");
        registerRequestBean(factory);
    }
    
    protected static void registerRequestBean(ApplicationContext factory){
    	HttpServletRequest request = createMock(HttpServletRequest.class);

        // 注册mock request
        ((ConfigurableListableBeanFactory) factory.getAutowireCapableBeanFactory()).registerResolvableDependency(
                HttpServletRequest.class, request);
    }

    @Before
    public void init() throws Exception {
        mappingRules = (MappingRuleService) factory.getBean("mappingRuleService");
    }

    @After
    public void destroy() {
        System.clearProperty("productionMode");
        System.clearProperty("cacheEnabled");
    }
}
