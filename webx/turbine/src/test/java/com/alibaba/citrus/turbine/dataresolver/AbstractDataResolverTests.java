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
package com.alibaba.citrus.turbine.dataresolver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;

import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.citrus.turbine.AbstractWebTests;
import com.alibaba.citrus.turbine.TurbineRunDataInternal;
import com.alibaba.citrus.turbine.util.TurbineUtil;

public abstract class AbstractDataResolverTests extends AbstractWebTests {
    protected ModuleLoaderService moduleLoaderService;

    @BeforeClass
    public static void initServlet() throws Exception {
        prepareServlet();
        factory = createContext("dataresolver/services-dataresolver.xml");
    }

    @Before
    public void init() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");
    }

    protected void execute(String moduleType, String moduleName, String eventName) throws Exception {
        execute(moduleType, moduleName, eventName, null);
    }

    protected void execute(String moduleType, String moduleName, String eventName, String query) throws Exception {
        getInvocationContext("/app1?event_submit_" + eventName + "=yes" + (query == null ? "" : "&" + query));
        initRequestContext();

        moduleLoaderService.getModule(moduleType, moduleName).execute();
    }

    protected <T> T assertLog(String key, Class<T> type, T data) {
        assertEquals(data, newRequest.getAttribute(key));
        return data;
    }

    protected <T> T assertLog(String key, Class<T> type) {
        assertThat(newRequest.getAttribute(key), instanceOf(type));
        return type.cast(newRequest.getAttribute(key));
    }

    protected TurbineRunDataInternal getRunData() {
        return (TurbineRunDataInternal) TurbineUtil.getTurbineRunData(newRequest);
    }
}
