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
package com.alibaba.citrus.service.moduleloader.impl.factory;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeansException;

import com.alibaba.citrus.service.moduleloader.AbstractModuleLoaderTests;
import com.alibaba.citrus.service.moduleloader.Module;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.citrus.service.moduleloader.impl.adapter.ActionEventAdapter;
import com.alibaba.citrus.service.moduleloader.impl.adapter.DataBindingAdapter;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;

public class ScriptModuleFactoryTests extends AbstractModuleLoaderTests {
    @BeforeClass
    public static void initFactory() {
        factory = createContext("factory/services-script-modules.xml", false);
    }

    @Test
    public void moduleClassNotFound() {
        try {
            createContext("factory/services-script-modules-wrong.xml", false);
            fail();
        } catch (BeansException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class,
                            "Specificly-defined module could not be found in search-folders or search-files",
                            "app1/Failure.groovy", "services-script-modules-wrong.xml"));
        }
    }

    @Test
    public void search_folders() throws Exception {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-folders");

        // types
        Set<String> types = moduleLoaderService.getModuleTypes();
        assertArrayEquals(new String[] { "action", "control", "screens" }, types.toArray(new String[types.size()]));

        // names
        Set<String> names = moduleLoaderService.getModuleNames("action");
        assertArrayEquals(new String[] { "myprod.MyAction" }, names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("control");
        assertArrayEquals(new String[] { "myprod.MyControl" }, names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("screens");
        assertArrayEquals(new String[] { "AbstractScreen", "MyScreen" }, names.toArray(new String[names.size()]));

        // modules
        Module module = moduleLoaderService.getModule(" screens ", "myScreen");

        assertNotNull(module);
        assertEquals("app1.module.screens.MyScreen", module.getClass().getName());
        assertEquals(null, module.getClass().getMethod("getName").invoke(module));

        module = moduleLoaderService.getModule("control", "myprod.MyControl");

        assertNotNull(module);
        assertEquals(DataBindingAdapter.class, module.getClass());

        module = moduleLoaderService.getModule("action", "myprod.MyAction");

        assertNotNull(module);
        assertEquals(ActionEventAdapter.class, module.getClass());
    }

    @Test
    public void search_files() throws Exception {
        search_files("search-files");
    }

    @Test
    public void search_files_2() throws Exception {
        search_files("search-files-2");
    }

    private void search_files(String name) throws Exception {
        moduleLoaderService = (ModuleLoaderService) factory.getBean(name);

        // types
        Set<String> types = moduleLoaderService.getModuleTypes();
        assertArrayEquals(new String[] { "action", "control", "screens" }, types.toArray(new String[types.size()]));

        // names
        Set<String> names = moduleLoaderService.getModuleNames("action");
        assertArrayEquals(new String[] { "app1.myprod.MyAction" }, names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("control");
        assertArrayEquals(new String[] { "app1.myprod.MyControl" }, names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("screens");
        assertArrayEquals(new String[] { "app1.AbstractScreen", "app1.MyScreen" },
                names.toArray(new String[names.size()]));

        // modules
        Module module = moduleLoaderService.getModule(" screens ", "app1/myScreen");

        assertNotNull(module);
        assertEquals("app1.module.screens.MyScreen", module.getClass().getName());
        assertEquals(null, module.getClass().getMethod("getName").invoke(module));

        module = moduleLoaderService.getModule("control", "app1.myprod.MyControl");

        assertNotNull(module);
        assertEquals(DataBindingAdapter.class, module.getClass());

        module = moduleLoaderService.getModule("action", "app1.myprod.MyAction");

        assertNotNull(module);
        assertEquals(ActionEventAdapter.class, module.getClass());
    }

    @Test
    public void search_multi() throws Exception {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-multi");

        // types
        Set<String> types = moduleLoaderService.getModuleTypes();
        assertArrayEquals(new String[] { "action", "control", "screen" }, types.toArray(new String[types.size()]));

        // names
        Set<String> names = moduleLoaderService.getModuleNames("action");
        assertArrayEquals(new String[] { "myprod.MyAction" }, names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("control");
        assertArrayEquals(new String[] { "app1.myprod.MyControl" }, names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("screen");
        assertArrayEquals(new String[] { "AbstractScreen", "MyScreen" }, names.toArray(new String[names.size()]));

        // modules
        Module module = moduleLoaderService.getModule(" screen ", "/myScreen");

        assertNotNull(module);
        assertEquals("app1.module.screens.MyScreen", module.getClass().getName());
        assertEquals("hello", module.getClass().getMethod("getName").invoke(module));

        module = moduleLoaderService.getModule("control", "app1.myprod.MyControl");

        assertNotNull(module);
        assertEquals(DataBindingAdapter.class, module.getClass());

        module = moduleLoaderService.getModule("action", "myprod.MyAction");

        assertNotNull(module);
        assertEquals(ActionEventAdapter.class, module.getClass());
    }

    @Test
    public void autowire() throws Exception {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-multi");

        Module module = moduleLoaderService.getModule(" action ", "/myprod/myAction");

        assertEquals(ActionEventAdapter.class, module.getClass());

        // adapter.request != null
        assertNotNull(getFieldValue(module, "request", HttpServletRequest.class));

        // adapter.moduleObject.rundata
        Object mobj = getFieldValue(module, "moduleObject", Object.class);
        assertNotNull(getFieldValue(mobj, "rundata", RunData.class));
    }
}
