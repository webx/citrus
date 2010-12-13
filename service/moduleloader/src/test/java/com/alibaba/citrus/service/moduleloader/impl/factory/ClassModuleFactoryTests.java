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
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.alibaba.citrus.service.moduleloader.AbstractModuleLoaderTests;
import com.alibaba.citrus.service.moduleloader.Module;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.citrus.service.moduleloader.UnadaptableModuleException;
import com.alibaba.citrus.service.moduleloader.impl.adapter.ActionEventAdapter;
import com.alibaba.citrus.service.moduleloader.impl.adapter.DataBindingAdapter;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;
import com.alibaba.test.app1.module.screens.MyScreen;

public class ClassModuleFactoryTests extends AbstractModuleLoaderTests {
    @BeforeClass
    public static void initFactory() {
        factory = createContext("factory/services-class-modules.xml", false);
    }

    @Test
    public void moduleClassNotFound() {
        try {
            createContext("factory/services-class-modules-wrong.xml", false);
            fail();
        } catch (BeansException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class,
                            "Specificly-defined module could not be found in search-packages or search-classes",
                            "screens.NotFound", "services-class-modules-wrong.xml"));
        }
    }

    @Test
    public void search_packages() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-packages");

        // types
        Set<String> types = moduleLoaderService.getModuleTypes();
        assertArrayEquals(new String[] { "action", "control", "screens" }, types.toArray(new String[types.size()]));

        // names
        Set<String> names = moduleLoaderService.getModuleNames("action");
        assertArrayEquals(new String[] { "myprod.MyAction", "myprod.MyActionFailure", "myprod.MyActionNoRunData" },
                names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("control");
        assertArrayEquals(new String[] { "InvalidControl", "myprod.MyControl" },
                names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("screens");
        assertArrayEquals(new String[] { "MyScreen" }, names.toArray(new String[names.size()]));

        // modules
        Module module = moduleLoaderService.getModule(" screens ", "myScreen");

        assertNotNull(module);
        assertEquals(MyScreen.class, module.getClass());
        assertEquals(null, ((MyScreen) module).getName());

        module = moduleLoaderService.getModule("control", "myprod.MyControl");

        assertNotNull(module);
        assertEquals(DataBindingAdapter.class, module.getClass());

        module = moduleLoaderService.getModule("action", "myprod.MyAction");

        assertNotNull(module);
        assertEquals(ActionEventAdapter.class, module.getClass());
    }

    @Test
    public void search_classes() {
        search_classes("search-classes");
    }

    @Test
    public void search_classes_2() {
        search_classes("search-classes-2");
    }

    private void search_classes(String name) {
        moduleLoaderService = (ModuleLoaderService) factory.getBean(name);

        // types
        Set<String> types = moduleLoaderService.getModuleTypes();
        assertArrayEquals(new String[] { "action", "control", "screens" }, types.toArray(new String[types.size()]));

        // names
        Set<String> names = moduleLoaderService.getModuleNames("action");
        assertArrayEquals(new String[] { "app1.myprod.MyAction", "app1.myprod.MyActionFailure",
                "app1.myprod.MyActionNoRunData" }, names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("control");
        assertArrayEquals(new String[] { "app1.InvalidControl", "app1.myprod.MyControl" },
                names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("screens");
        assertArrayEquals(new String[] { "app1.MyScreen" }, names.toArray(new String[names.size()]));

        // modules
        Module module = moduleLoaderService.getModule(" screens ", "app1/myScreen");

        assertNotNull(module);
        assertEquals(MyScreen.class, module.getClass());
        assertEquals(null, ((MyScreen) module).getName());

        module = moduleLoaderService.getModule("control", "app1.myprod.MyControl");

        assertNotNull(module);
        assertEquals(DataBindingAdapter.class, module.getClass());

        module = moduleLoaderService.getModule("action", "app1.myprod.MyAction");

        assertNotNull(module);
        assertEquals(ActionEventAdapter.class, module.getClass());
    }

    @Test
    public void search_multi() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-multi");

        // types
        Set<String> types = moduleLoaderService.getModuleTypes();
        assertArrayEquals(new String[] { "action", "control", "screen" }, types.toArray(new String[types.size()]));

        // names
        Set<String> names = moduleLoaderService.getModuleNames("action");
        assertArrayEquals(new String[] { "myprod.MyAction", "myprod.MyActionFailure", "myprod.MyActionNoRunData" },
                names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("control");
        assertArrayEquals(new String[] { "app1.InvalidControl", "app1.myprod.MyControl" },
                names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("screen");
        assertArrayEquals(new String[] { "MyScreen" }, names.toArray(new String[names.size()]));

        // modules
        Module module = moduleLoaderService.getModule(" screen ", "/myScreen");

        assertNotNull(module);
        assertEquals(MyScreen.class, module.getClass());
        assertEquals("hello", ((MyScreen) module).getName());

        module = moduleLoaderService.getModule("control", "app1.myprod.MyControl");

        assertNotNull(module);
        assertEquals(DataBindingAdapter.class, module.getClass());

        module = moduleLoaderService.getModule("action", "myprod.MyAction");

        assertNotNull(module);
        assertEquals(ActionEventAdapter.class, module.getClass());
    }

    @Test
    public void autowire_failed() throws Exception {
        try {
            createContext("factory/services-class-modules.xml", null);
            fail();
        } catch (BeanCreationException e) {
            assertThat(e, exception(NoSuchBeanDefinitionException.class, "RunData"));
        }
    }

    @Test
    public void autowire_no() throws Exception {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-multi");

        Module module = moduleLoaderService.getModule(" action ", "/myprod/myAction");

        assertEquals(ActionEventAdapter.class, module.getClass());

        // adapter.request != null
        assertNotNull(getFieldValue(module, "request", HttpServletRequest.class));

        // adapter.moduleObject.rundata
        Object mobj = getFieldValue(module, "moduleObject", Object.class);
        assertNull(getFieldValue(mobj, "rundata", RunData.class));
    }

    @Test
    public void autowire_byType() throws Exception {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-autowire");

        Module module = moduleLoaderService.getModule(" action ", "/myprod/myAction");

        assertEquals(ActionEventAdapter.class, module.getClass());

        // adapter.request != null
        assertNotNull(getFieldValue(module, "request", HttpServletRequest.class));

        // adapter.moduleObject.rundata
        Object mobj = getFieldValue(module, "moduleObject", Object.class);
        assertNotNull(getFieldValue(mobj, "rundata", RunData.class));
    }

    @Test
    public void search_abstract() throws Exception {
        // search-packages
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-packages-abstract");

        Set<String> names = moduleLoaderService.getModuleNames("screens");
        assertArrayEquals(new String[] { "AbstractScreen", "MyScreen" }, names.toArray(new String[names.size()]));

        try {
            moduleLoaderService.getModule("screens", "AbstractScreen");
        } catch (UnadaptableModuleException e) {
            assertThat(e, exception("name=AbstractScreen", "java.lang.Class"));
        }

        // search-classes
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-classes-abstract");

        names = moduleLoaderService.getModuleNames("screens");
        assertArrayEquals(new String[] { "app1.AbstractScreen", "app1.MyScreen" },
                names.toArray(new String[names.size()]));

        try {
            moduleLoaderService.getModule("screens", "app1.AbstractScreen");
        } catch (UnadaptableModuleException e) {
            assertThat(e, exception("name=app1.AbstractScreen", "java.lang.Class"));
        }
    }

    @Test
    public void search_withFilterByCustom() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-classes-with-filter-custom");

        doSearch_withFilter();
    }

    @Test
    public void search_withFilterByWildcard() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-classes-with-filter-wildcard");

        doSearch_withFilter();
    }

    private void doSearch_withFilter() {
        // types
        Set<String> types = moduleLoaderService.getModuleTypes();
        assertArrayEquals(new String[] { "action", "screen" }, types.toArray(new String[types.size()]));

        // names
        Set<String> names = moduleLoaderService.getModuleNames("action");
        assertArrayEquals(new String[] { "app2.FirstAction", "app2.ThirdAction" },
                names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("screen");
        assertArrayEquals(new String[] { "app2.FirstScreen", "app2.ThirdScreen" },
                names.toArray(new String[names.size()]));

        // modules
        Module module = moduleLoaderService.getModule("action", "/app2/firstAction");

        assertNotNull(module);
        assertEquals(ActionEventAdapter.class, module.getClass());

        module = moduleLoaderService.getModule(" screen ", "app2.ThirdScreen");

        assertNotNull(module);
        assertEquals(DataBindingAdapter.class, module.getClass());
    }

    @Test
    public void search_withIncludeFilter() {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("search-classes-with-includeFilter");

        // types
        Set<String> types = moduleLoaderService.getModuleTypes();
        assertArrayEquals(new String[] { "action", "screen" }, types.toArray(new String[types.size()]));

        // names
        Set<String> names = moduleLoaderService.getModuleNames("action");
        assertArrayEquals(new String[] { "app2.SecondAction" }, names.toArray(new String[names.size()]));

        names = moduleLoaderService.getModuleNames("screen");
        assertArrayEquals(new String[] { "app2.FirstScreen", "app2.SecondScreen" },
                names.toArray(new String[names.size()]));

        // modules
        Module module = moduleLoaderService.getModule("action", "/app2/secondAction");

        assertNotNull(module);
        assertEquals(ActionEventAdapter.class, module.getClass());

        module = moduleLoaderService.getModule(" screen ", "app2.FirstScreen");

        assertNotNull(module);
        assertEquals(DataBindingAdapter.class, module.getClass());
    }
}
