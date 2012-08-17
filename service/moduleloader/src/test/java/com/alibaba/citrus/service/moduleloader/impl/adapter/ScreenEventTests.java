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

package com.alibaba.citrus.service.moduleloader.impl.adapter;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import com.alibaba.citrus.service.AbstractWebTests;
import com.alibaba.citrus.service.moduleloader.ModuleEvent;
import com.alibaba.citrus.service.moduleloader.ModuleEventException;
import com.alibaba.citrus.service.moduleloader.ModuleEventNotFoundException;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.citrus.util.internal.ScreenEventUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScreenEventTests extends AbstractWebTests {
    private ModuleLoaderService moduleLoader;

    @BeforeClass
    public static void initServlet() throws Exception {
        prepareServlet();
        factory = createContext("adapter/services.xml", false);
    }

    @Before
    public void init() {
        moduleLoader = (ModuleLoaderService) factory.getBean("moduleLoaderService_eventScreen");
        assertNotNull(moduleLoader);
    }

    @Test
    public void isModuleEvent() throws Exception {
        getInvocationContext("/app1");
        initRequestContext();

        ScreenEventAdapter screenEvent = (ScreenEventAdapter) moduleLoader.getModule("screen", "MyEventScreenWithDefaultHandler");

        assertTrue(screenEvent instanceof ModuleEvent);
    }

    @Test
    public void doPerform_noArgs() throws Exception {
        getInvocationContext("/app1");
        initRequestContext();

        ScreenEventAdapter screenEvent = (ScreenEventAdapter) moduleLoader.getModule("screen", "MyEventScreenWithDefaultHandler");

        screenEvent.execute();

        assertEquals("doPerform", rundata.getAttribute("handler"));
        assertEquals("yes", rundata.getAttribute("before"));
        assertEquals("yes", rundata.getAttribute("after"));
    }

    @Test
    public void doPerform_unknownEvent() throws Exception {
        getInvocationContext("/app1");
        initRequestContext();

        ScreenEventUtil.setEventName(newRequest, "xyz");

        ScreenEventAdapter screenEvent = (ScreenEventAdapter) moduleLoader.getModule("screen", "MyEventScreenWithDefaultHandler");

        screenEvent.execute();

        assertEquals("doPerform", rundata.getAttribute("handler"));
        assertEquals("yes", rundata.getAttribute("before"));
        assertEquals("yes", rundata.getAttribute("after"));
    }

    @Test
    public void doPerform_noDefaultHandler_noEvent() throws Exception {
        getInvocationContext("/app1");
        initRequestContext();

        ScreenEventAdapter screenEvent = (ScreenEventAdapter) moduleLoader.getModule("screen", "MyEventScreen");

        try {
            screenEvent.execute();
            fail();
        } catch (ModuleEventNotFoundException e) {
            assertThat(e, exception("Could not find handler method for event: null"));
        }
    }

    @Test
    public void doPerform_noDefaultHandler_unknownEvent() throws Exception {
        getInvocationContext("/app1");
        initRequestContext();

        ScreenEventUtil.setEventName(newRequest, "xyzAbc");

        ScreenEventAdapter screenEvent = (ScreenEventAdapter) moduleLoader.getModule("screen", "MyEventScreen");

        try {
            screenEvent.execute();
            fail();
        } catch (ModuleEventNotFoundException e) {
            assertThat(e, exception("Could not find handler method for event: xyzAbc"));
        }
    }

    @Test
    public void doSomethingInteresting() throws Exception {
        getInvocationContext("/app1");
        initRequestContext();

        ScreenEventUtil.setEventName(newRequest, "somethingInteresting");

        ScreenEventAdapter screenEvent = (ScreenEventAdapter) moduleLoader.getModule("screen", "MyEventScreen");

        screenEvent.execute();

        assertEquals("doSomethingInteresting", rundata.getAttribute("handler"));
        assertEquals(null, rundata.getAttribute("before"));
        assertEquals(null, rundata.getAttribute("after"));

        assertNull(screenEvent.executeAndReturn());
    }

    @Test
    public void doReturnValue() throws Exception {
        getInvocationContext("/app1");
        initRequestContext();

        ScreenEventUtil.setEventName(newRequest, "returnValue");

        ScreenEventAdapter screenEvent = (ScreenEventAdapter) moduleLoader.getModule("screen", "MyEventScreen");

        screenEvent.execute();

        assertEquals("doReturnValue", rundata.getAttribute("handler"));
        assertEquals(null, rundata.getAttribute("before"));
        assertEquals(null, rundata.getAttribute("after"));

        assertEquals("myresult", screenEvent.executeAndReturn());
    }

    @Test
    public void doSomethingFailed() throws Exception {
        getInvocationContext("/app1");
        initRequestContext();

        ScreenEventUtil.setEventName(newRequest, "somethingFailed");

        ScreenEventAdapter screenEvent = (ScreenEventAdapter) moduleLoader.getModule("screen", "MyEventScreenFailure");

        try {
            screenEvent.execute();
            fail();
        } catch (ModuleEventException e) {
            assertThat(e, exception(IllegalArgumentException.class, "failed"));
        }
    }

    /** 如果execute方法和doXxx方法同时存在，execute adapter将被返回。 */
    @Test
    public void screen_withExecute_andEventHandler() throws Exception {
        getInvocationContext("/app1");
        initRequestContext();

        ScreenEventUtil.setEventName(newRequest, "something");

        DataBindingAdapter screenAdapter = (DataBindingAdapter) moduleLoader.getModule("screen", "MyEventScreenHybrid");

        screenAdapter.execute();

        assertEquals("execute", rundata.getAttribute("handler"));
        assertEquals(null, rundata.getAttribute("before"));
        assertEquals(null, rundata.getAttribute("after"));
    }

    @Test
    public void toString_() {
        ScreenEventAdapter screenEvent = (ScreenEventAdapter) moduleLoader.getModule("screen", "MyEventScreenWithDefaultHandler");

        String s = "";

        s += "ScreenEventAdapter {\n";
        s += "  moduleClass = com.alibaba.test.app1.module2.screen.MyEventScreenWithDefaultHandler\n";
        s += "  handlers    = {\n";
        s += "                  [1/2] <null>    = public void com.alibaba.test.app1.module2.screen.MyEventScreenWithDefaultHandler.doPerform() throws java.lang.Exception\n";
        s += "                  [2/2] something = public void com.alibaba.test.app1.module2.screen.MyEventScreenWithDefaultHandler.doSomething() throws java.lang.Exception\n";
        s += "                }\n";
        s += "  preHandler  = public void com.alibaba.test.app1.module2.screen.MyEventScreenWithDefaultHandler.beforeExecution()\n";
        s += "  postHandler = public void com.alibaba.test.app1.module2.screen.MyEventScreenWithDefaultHandler.afterExecution()\n";
        s += "}";

        assertEquals(s, screenEvent.toString());
    }
}
