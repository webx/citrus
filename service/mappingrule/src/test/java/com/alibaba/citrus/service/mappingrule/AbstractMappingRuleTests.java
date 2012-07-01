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

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

public abstract class AbstractMappingRuleTests extends AbstractMappingRuleServiceTests {
    @Test
    public void cacheAuto_productionMode() throws Exception {
        System.setProperty("productionMode", "true");
        ApplicationContext factory = createBeanFactory("services.xml");
        registerRequestBean(factory);
        
        mappingRules = (MappingRuleService) factory.getBean("mappingRules");

        String result = getMappedName_forCacheTest();
        assertNotNull(result);

        if (isProductionModeSensible()) {
            assertSame(result, getMappedName_forCacheTest());
        } else {
            assertNotSame(result, getMappedName_forCacheTest());
        }
    }

    @Test
    public void cacheAuto_devMode() throws Exception {
        System.setProperty("productionMode", "false");
        ApplicationContext factory = createBeanFactory("services.xml");
        registerRequestBean(factory);
        mappingRules = (MappingRuleService) factory.getBean("mappingRules");

        String result = getMappedName_forCacheTest();
        assertNotNull(result);
        assertNotSame(result, getMappedName_forCacheTest());
    }

    @Test
    public void cacheOn() throws Exception {
        System.setProperty("productionMode", "false");
        System.setProperty("cacheEnabled", "true");
        ApplicationContext factory = createBeanFactory("services.xml");
        registerRequestBean(factory);
        mappingRules = (MappingRuleService) factory.getBean("cache");
        

        String result = getMappedName_forCacheTest();
        assertNotNull(result);
        assertSame(result, getMappedName_forCacheTest());
    }

    @Test
    public void cacheOff() throws Exception {
        System.setProperty("productionMode", "true");
        System.setProperty("cacheEnabled", "false");
        ApplicationContext factory = createBeanFactory("services.xml");
        registerRequestBean(factory);
        mappingRules = (MappingRuleService) factory.getBean("cache");

        String result = getMappedName_forCacheTest();
        assertNotNull(result);
        assertNotSame(result, getMappedName_forCacheTest());
    }

    protected abstract String getMappedName_forCacheTest();

    protected abstract boolean isProductionModeSensible();
}
