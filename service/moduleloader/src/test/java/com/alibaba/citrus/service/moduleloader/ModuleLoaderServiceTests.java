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
package com.alibaba.citrus.service.moduleloader;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.moduleloader.impl.ModuleLoaderServiceImpl;
import com.alibaba.test.app1.module.control.InvalidControl;

public class ModuleLoaderServiceTests extends AbstractModuleLoaderTests {
    @BeforeClass
    public static void initFactory() {
        factory = createContext("services-module-loader.xml", false);
    }

    @Test
    public void notFound() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        assertNull(moduleLoaderService.getModuleQuiet("action", "NotFound"));

        try {
            moduleLoaderService.getModule("action", "NotFound");
            fail();
        } catch (ModuleNotFoundException e) {
            assertThat(e, exception("Module not found: type=action, name=NotFound"));
        }
    }

    @Test
    public void unadaptable() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        try {
            moduleLoaderService.getModuleQuiet("control", "InvalidControl");
            fail();
        } catch (UnadaptableModuleException e) {
            assertThat(
                    e,
                    exception("Could not adapt object to module: type=control, name=InvalidControl, class=",
                            InvalidControl.class.getName()));
        }

        try {
            moduleLoaderService.getModule("control", "InvalidControl");
            fail();
        } catch (UnadaptableModuleException e) {
            assertThat(
                    e,
                    exception("Could not adapt object to module: type=control, name=InvalidControl, class=",
                            InvalidControl.class.getName()));
        }
    }

    @Test
    public void failureLoading() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("failure");

        try {
            moduleLoaderService.getModuleQuiet("action", "Failure");
            fail();
        } catch (ModuleLoaderException e) {
            assertThat(e, exception("Failure loading module: action:Failure"));
            assertThat(e, exception(BeanCreationException.class, "module.action.Failure"));
            assertThat(e, exception(IllegalArgumentException.class, "[Assertion failed] - the expression must be true"));
        }

        try {
            moduleLoaderService.getModule("action", "Failure");
            fail();
        } catch (ModuleLoaderException e) {
            assertThat(e, exception("Failure loading module: action:Failure"));
            assertThat(e, exception(BeanCreationException.class, "module.action.Failure"));
            assertThat(e, exception(IllegalArgumentException.class, "[Assertion failed] - the expression must be true"));
        }
    }

    @Test
    public void no_cache() {
        // productionMode=false, cacheEnabled=default
        moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        Module action = moduleLoaderService.getModule("action", "myprod.MyAction"); // adapter

        assertNotNull(action);
        assertNotSame(action, moduleLoaderService.getModule("action", "myprod.MyAction"));

        // productionMode=true, cacheEnabled=false
        ApplicationContext factory = createContext("services-module-loader.xml", true);
        moduleLoaderService = (ModuleLoaderService) factory.getBean("noCache");

        action = moduleLoaderService.getModule("action", "myprod.MyAction"); // adapter

        assertNotNull(action);
        assertNotSame(action, moduleLoaderService.getModule("action", "myprod.MyAction"));
    }

    @Test
    public void with_cache() {
        // productionMode=true, cacheEnabled=default
        ApplicationContext factory = createContext("services-module-loader.xml", true);
        moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        Module action = moduleLoaderService.getModule("action", "myprod.MyAction"); // adapter

        assertNotNull(action);
        assertSame(action, moduleLoaderService.getModule("action", "myprod.MyAction"));
    }

    @Test
    public void productionMode_byDefault() {
        assertTrue(new ModuleLoaderServiceImpl().isProductionMode());
    }

    @Test
    public void toString_defaultAdapters() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        String str = moduleLoaderService.toString();

        assertThat(str, containsString("ModuleLoaderService {"));
        assertThat(str, containsString("factories"));
        assertThat(str, containsString("adapters"));
        assertThat(str, containsString("ClassModuleFactory"));
        assertThat(str, containsString("ActionEventAdapterFactory")); // 默认adapter
        assertThat(str, containsString("DataBindingAdapterFactory")); // 默认adapter
        assertThat(str, containsString("}"));

        ModuleAdapterFactory[] adapters = getFieldValue(moduleLoaderService, "adapters", ModuleAdapterFactory[].class);
        assertEquals(2, adapters.length);
    }

    @Test
    public void toString_specifiedAdapters_includeDefaultAdapters() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("includeDefaultAdapters");

        String str = moduleLoaderService.toString();

        assertThat(str, containsString("ModuleLoaderService {"));
        assertThat(str, containsString("factories"));
        assertThat(str, containsString("adapters"));
        assertThat(str, containsString("ClassModuleFactory"));
        assertThat(str, containsString("ActionEventAdapterFactory")); // 明确指定
        assertThat(str, containsString("MySimpleAdapterFactory")); // 明确指定
        assertThat(str, containsString("DataBindingAdapterFactory")); // 明确指定
        assertThat(str, containsString("}"));

        ModuleAdapterFactory[] adapters = getFieldValue(moduleLoaderService, "adapters", ModuleAdapterFactory[].class);
        assertEquals(3, adapters.length);
    }

    @Test
    public void toString_specifiedAdapters_dontIncludeDefaultAdapters() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("dontIncludeDefaultAdapters");

        String str = moduleLoaderService.toString();

        assertThat(str, containsString("ModuleLoaderService {"));
        assertThat(str, containsString("factories"));
        assertThat(str, containsString("adapters"));
        assertThat(str, containsString("ClassModuleFactory"));
        assertThat(str, containsString("ActionEventAdapterFactory")); // 明确指定
        assertThat(str, containsString("MySimpleAdapterFactory")); // 明确指定
        assertThat(str, not(containsString("DataBindingAdapterFactory"))); // 未指定
        assertThat(str, containsString("}"));

        ModuleAdapterFactory[] adapters = getFieldValue(moduleLoaderService, "adapters", ModuleAdapterFactory[].class);
        assertEquals(2, adapters.length);
    }

    public static class MySimpleAdapterFactory implements ModuleAdapterFactory {
        public Module adapt(String type, String name, Object moduleObject) throws ModuleLoaderException {
            return null;
        }
    }
}
