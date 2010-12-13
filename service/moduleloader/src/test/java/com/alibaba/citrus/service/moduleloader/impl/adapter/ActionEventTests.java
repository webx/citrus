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
package com.alibaba.citrus.service.moduleloader.impl.adapter;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.AbstractWebTests;
import com.alibaba.citrus.service.moduleloader.ActionEventException;
import com.alibaba.citrus.service.moduleloader.ActionEventNotFoundException;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderException;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;

public class ActionEventTests extends AbstractWebTests {
    private ModuleLoaderService moduleLoader;

    @BeforeClass
    public static void initServlet() throws Exception {
        prepareServlet();
        factory = createContext("adapter/services.xml", false);
    }

    @Before
    public void init() {
        moduleLoader = (ModuleLoaderService) factory.getBean("moduleLoaderService");
        assertNotNull(moduleLoader);
    }

    /**
     * 当指定了defaultLazyInit=false（默认值）时，module创建的失败会在context初始化的时候抛出。
     */
    @Test
    public void actionEvent_eagerInitFailure() {
        try {
            createContext("adapter/services.xml", null); // 无parent，故取不到RunData
            fail();
        } catch (BeansException e) {
            assertThat(e, exception(NoSuchBeanDefinitionException.class, "RunData"));
        }
    }

    /**
     * 当指定了defaultLazyInit=true时，module创建的失败会在取得module时抛出。
     */
    @Test
    public void actionEvent_noRequest() {
        // 创建不包含request-contexts对象的spring container
        ApplicationContext factory = createContext("adapter/services-lazyinit.xml", null);
        moduleLoader = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        try {
            moduleLoader.getModule("action", "class.myprod.MyActionNoRunData");
            fail();
        } catch (ModuleLoaderException e) {
            assertThat(
                    e,
                    exception(NoSuchBeanDefinitionException.class, "Failed to configure module adapter",
                            "HttpServletRequest"));
        }
    }

    @Test
    public void actionEvent_requestIsNotProxy() {
        // 创建不包含request-contexts对象的spring container
        ApplicationContext factory = createContext("adapter/services-lazyinit.xml", null);
        HttpServletRequest request = createMock(HttpServletRequest.class);

        // 注册mock request
        ((ConfigurableListableBeanFactory) factory.getAutowireCapableBeanFactory()).registerResolvableDependency(
                HttpServletRequest.class, request);

        moduleLoader = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        try {
            moduleLoader.getModule("action", "class.myprod.MyActionNoRunData");
            fail();
        } catch (ModuleLoaderException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class, "Failed to configure module adapter",
                            "expects a proxy delegating to a real object, but got an object of type "
                                    + request.getClass().getName()));
        }
    }

    @Test
    public void doPerform_noArgs() throws Exception {
        getInvocationContext("/app1");
        initRequestContext();

        ActionEventAdapter actionEvent = (ActionEventAdapter) moduleLoader
                .getModule("action", "groovy.myprod.MyAction");

        actionEvent.execute();

        assertEquals("doPerform", rundata.getAttribute("handler"));
        assertEquals("yes", rundata.getAttribute("before"));
        assertEquals("yes", rundata.getAttribute("after"));
    }

    @Test
    public void doPerform_unknownArgs() throws Exception {
        getInvocationContext("/app1?event_submit_do_xyz=true");
        initRequestContext();

        ActionEventAdapter actionEvent = (ActionEventAdapter) moduleLoader
                .getModule("action", "groovy.myprod.MyAction");

        actionEvent.execute();

        assertEquals("doPerform", rundata.getAttribute("handler"));
        assertEquals("yes", rundata.getAttribute("before"));
        assertEquals("yes", rundata.getAttribute("after"));
    }

    @Test
    public void doPerform_noDefaultHandler_emptyArgs() throws Exception {
        getInvocationContext("/app1?event_submit_do_xyz=");
        initRequestContext();

        ActionEventAdapter actionEvent = (ActionEventAdapter) moduleLoader.getModule("action", "class.myprod.MyAction");

        try {
            actionEvent.execute();
            fail();
        } catch (ActionEventNotFoundException e) {
            assertThat(e, exception("Could not find handler method for action event: null"));
        }
    }

    @Test
    public void doPerform_noDefaultHandler_unknownArgs() throws Exception {
        getInvocationContext("/app1?event_submit_do_xyzAbc=true");
        initRequestContext();

        ActionEventAdapter actionEvent = (ActionEventAdapter) moduleLoader.getModule("action", "class.myprod.MyAction");

        try {
            actionEvent.execute();
            fail();
        } catch (ActionEventNotFoundException e) {
            assertThat(e, exception("Could not find handler method for action event: xyz_abc"));
        }
    }

    @Test
    public void doSomethingInteresting() throws Exception {
        getInvocationContext("/app1?eventSubmitDoSomethingInteresting=true");
        initRequestContext();

        ActionEventAdapter actionEvent = (ActionEventAdapter) moduleLoader.getModule("action", "class.myprod.MyAction");

        actionEvent.execute();

        assertEquals("doSomethingInteresting", rundata.getAttribute("handler"));
        assertEquals(null, rundata.getAttribute("before"));
        assertEquals(null, rundata.getAttribute("after"));
    }

    @Test
    public void doSomethingFailed() throws Exception {
        getInvocationContext("/app1?eventSubmitDoSomethingFailed=true");
        initRequestContext();

        ActionEventAdapter actionEvent = (ActionEventAdapter) moduleLoader.getModule("action",
                "class.myprod.MyActionFailure");

        try {
            actionEvent.execute();
            fail();
        } catch (ActionEventException e) {
            assertThat(e, exception(IllegalArgumentException.class, "failed"));
        }
    }

    @Test
    public void doSomething_imageButton() throws Exception {
        doSomething_imageButton("x");
        doSomething_imageButton("y");
        doSomething_imageButton("X");
        doSomething_imageButton("Y");
    }

    private void doSomething_imageButton(String axis) throws Exception {
        getInvocationContext("/app1?eventSubmit_Do_Something." + axis + "=11");
        initRequestContext();

        ActionEventAdapter actionEvent = (ActionEventAdapter) moduleLoader
                .getModule("action", "groovy.myprod.MyAction");

        actionEvent.execute();

        assertEquals("doSomething", rundata.getAttribute("handler"));
        assertEquals("yes", rundata.getAttribute("before"));
        assertEquals("yes", rundata.getAttribute("after"));
    }

    @Test
    public void toString_() {
        ActionEventAdapter actionEvent = (ActionEventAdapter) moduleLoader
                .getModule("action", "groovy.myprod.MyAction");

        String s = "";

        s += "ActionEventAdapter {\n";
        s += "  moduleClass = app1.module.action.myprod.MyAction\n";
        s += "  handlers    = {\n";
        s += "                  [1/2] <null>    = public void app1.module.action.myprod.MyAction.doPerform() throws java.lang.Exception\n";
        s += "                  [2/2] something = public void app1.module.action.myprod.MyAction.doSomething() throws java.lang.Exception\n";
        s += "                }\n";
        s += "  preHandler  = public void app1.module.action.myprod.MyAction.beforeExecution()\n";
        s += "  postHandler = public void app1.module.action.myprod.MyAction.afterExecution()\n";
        s += "}";

        assertEquals(s, actionEvent.toString());
    }
}
