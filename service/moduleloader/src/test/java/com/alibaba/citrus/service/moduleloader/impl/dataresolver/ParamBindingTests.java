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
package com.alibaba.citrus.service.moduleloader.impl.dataresolver;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.moduleloader.AbstractModuleLoaderTests;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.citrus.service.moduleloader.UnadaptableModuleException;
import com.alibaba.citrus.service.moduleloader.impl.adapter.ActionEventAdapter;
import com.alibaba.citrus.service.moduleloader.impl.adapter.DataBindingAdapter;
import com.alibaba.test2.module.action.MyParameterizedAction;
import com.alibaba.test2.module.screen.MyParameterizedScreen;

public class ParamBindingTests extends AbstractModuleLoaderTests {
    @BeforeClass
    public static void initServlet() throws Exception {
        prepareServlet();
        factory = createContext("dataresolver/services-with-dataresolver-default-adapters.xml", false);
    }

    @Test
    public void noDataResolver() throws Exception {
        ApplicationContext factory = createContext("dataresolver/services-no-dataresolver.xml", false);
        moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        getInvocationContext("/app1?event_submit_do_my_event=yes");
        initRequestContext(factory);

        // screen.execute(无参数) - 正常执行
        DataBindingAdapter dbAdapter = (DataBindingAdapter) moduleLoaderService.getModule("screen", "myScreen");
        dbAdapter.execute();

        // screen.execute(带参数)
        try {
            moduleLoaderService.getModule("screen", "myParameterizedScreen");
            fail();
        } catch (UnadaptableModuleException e) {
            assertThat(e, exception("Could not adapt object to module: type=screen, name=MyParameterizedScreen, class="
                    + MyParameterizedScreen.class.getName()
                    + ": method execute has 2 parameters, but no DataResolvers defined."));
        }

        // action.doMyEvent(无参数) - 正常执行
        ActionEventAdapter aeAdapter = (ActionEventAdapter) moduleLoaderService.getModule("action", "myAction");
        aeAdapter.execute();

        // action.doMyEvent(带参数)
        try {
            moduleLoaderService.getModule("action", "myParameterizedAction");
            fail();
        } catch (UnadaptableModuleException e) {
            assertThat(e, exception("Could not adapt object to module: type=action, name=MyParameterizedAction, class="
                    + MyParameterizedAction.class.getName()
                    + ": method doMyEvent has 2 parameters, but no DataResolvers defined."));
        }
    }

    @Test
    public void withDataResolver_default_adapters() throws Exception {
        assertWithDataResolver(factory);
    }

    @Test
    public void withDataResolver_defined_adapters() throws Exception {
        assertWithDataResolver(createContext("dataresolver/services-with-dataresolver-defined-adapters.xml", false));
    }

    @Test
    public void withDataResolver_defined_adapters_default_resolverRef() throws Exception {
        assertWithDataResolver(createContext(
                "dataresolver/services-with-dataresolver-defined-adapters-default-resolver-ref.xml", false));
    }

    private void assertWithDataResolver(ApplicationContext factory) throws Exception {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        getInvocationContext("/app1?event_submit_do_my_event=yes");
        initRequestContext(factory);

        // screen.execute(无参数)
        DataBindingAdapter dbAdapter = (DataBindingAdapter) moduleLoaderService.getModule("screen", "myScreen");
        dbAdapter.execute();
        assertEquals("MyScreen.execute()", request.getAttribute("screenLog"));

        // screen.execute(带参数)
        dbAdapter = (DataBindingAdapter) moduleLoaderService.getModule("screen", "myParameterizedScreen");
        dbAdapter.execute();
        assertEquals("MyParameterizedScreen.execute(request, 111)", request.getAttribute("screenLog"));

        // action.doMyEvent(无参数) - 正常执行
        ActionEventAdapter aeAdapter = (ActionEventAdapter) moduleLoaderService.getModule("action", "myAction");
        aeAdapter.execute();
        assertEquals("MyAction.doMyEvent()", request.getAttribute("actionLog"));

        // action.doMyEvent(带参数)
        aeAdapter = (ActionEventAdapter) moduleLoaderService.getModule("action", "myParameterizedAction");
        aeAdapter.execute();
        assertEquals("MyParameterizedAction.doMyEvent(request, 222)", request.getAttribute("actionLog"));

        // action.doMyEvent(带primitive参数)
        getInvocationContext("/app1?event_submit_do_my_event_primitive=yes");
        initRequestContext(factory);

        aeAdapter = (ActionEventAdapter) moduleLoaderService.getModule("action", "myParameterizedAction");
        aeAdapter.execute();
        assertEquals("MyParameterizedAction.doMyEventPrimitive(request, 0)", request.getAttribute("actionLog"));
    }

    @Test
    public void skipScreen() throws Exception {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        getInvocationContext("/app1?event_submit_do_my_event=yes");
        initRequestContext(factory);

        // screen.execute(@skip) - 不会真的skip，只是参数为null
        DataBindingAdapter dbAdapter = (DataBindingAdapter) moduleLoaderService
                .getModule("screen", "mySkippableScreen");
        dbAdapter.execute();

        assertEquals("result is haha", request.getAttribute("screenLog"));

        // screen.execute(@skip primitive type) - 不会真的skip，只是参数为false
        dbAdapter = (DataBindingAdapter) moduleLoaderService.getModule("screen", "mySkippableScreen2");
        dbAdapter.execute();

        assertEquals("result is false", request.getAttribute("screenLog"));
    }

    @Test
    public void skipActionEvent() throws Exception {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        getInvocationContext("/app1?event_submit_do_my_event=yes");
        initRequestContext(factory);

        // action.doMyEvent(@skip) - 被skip后，不执行，但before/afterExecution仍执行。
        ActionEventAdapter aeAdapter = (ActionEventAdapter) moduleLoaderService
                .getModule("action", "mySkippableAction");
        aeAdapter.execute();

        assertEquals(null /* 不是"result is null" */, request.getAttribute("actionLog"));
        assertEquals("result is haha", request.getAttribute("actionLog.before"));
        assertEquals("result is haha", request.getAttribute("actionLog.after"));
    }

    @Test
    public void skipAction() throws Exception {
        moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        getInvocationContext("/app1?event_submit_do_my_event=yes");
        initRequestContext(factory);

        // action.execute(@skip) - 被skip后，不执行。
        DataBindingAdapter dbAdapter = (DataBindingAdapter) moduleLoaderService.getModule("action",
                "mySkippableAction2");
        dbAdapter.execute();

        assertEquals(null /* 不是"result is null" */, request.getAttribute("actionLog"));
    }
}
