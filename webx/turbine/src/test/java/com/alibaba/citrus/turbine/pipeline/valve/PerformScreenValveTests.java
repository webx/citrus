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

package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.moduleloader.ModuleNotFoundException;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.PipelineInvocationHandle;
import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PerformScreenValveTests extends AbstractValveTests {
    @Before
    public void init() {
        pipeline = (PipelineImpl) factory.getBean("performScreenValveTests");
        assertNotNull(pipeline);
    }

    @Test
    public void test_PerformScreenValveSuccess() throws Exception {
        getInvocationContext("http://localhost/app1/aaa/bbb/myOtherModule.jsp");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("execute success!", rundata.getRequest().getAttribute("module.screen.aaa.bbb.MyOtherModule"));
    }

    @Test
    public void test_PerformScreenValveUseDefault() throws Exception {
        getInvocationContext("http://localhost/app1/aaa/bbb/myOtherModule1.jsp");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("execute success!", rundata.getRequest().getAttribute("module.screen.aaa.bbb.Default"));
    }

    @Test
    public void test_PerformScreenValveError() throws Exception {
        getInvocationContext("http://localhost/app1/ddd/fff/myOtherModule1.jsp");
        initRequestContext();

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(ModuleNotFoundException.class, "ddd.fff.MyOtherModule1"));
        }
    }

    @Test
    public void event() throws Exception {
        // no event
        getInvocationContext("http://localhost/app1/ddd/eee/myEventScreen.jsp");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("perform", rundata.getRequest().getAttribute("module.screen.ddd.eee.MyEventScreen"));

        // unknown event
        getInvocationContext("http://localhost/app1/ddd/eee/myEventScreen/unknown.jsp");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("perform", rundata.getRequest().getAttribute("module.screen.ddd.eee.MyEventScreen"));

        // known event
        getInvocationContext("http://localhost/app1/ddd/eee/myEventScreen/test1.jsp");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("test1", rundata.getRequest().getAttribute("module.screen.ddd.eee.MyEventScreen"));

        // known event - without extension
        getInvocationContext("http://localhost/app1/ddd/eee/myEventScreen/test1");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("test1", rundata.getRequest().getAttribute("module.screen.ddd.eee.MyEventScreen"));

        // myScreen是普通screen，非event module
        getInvocationContext("http://localhost/app1/ddd/eee/myScreen.jsp");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("execute", rundata.getRequest().getAttribute("module.screen.ddd.eee.MyScreen"));

        // myScreen是普通screen，非event module，不可用来处理event
        getInvocationContext("http://localhost/app1/ddd/eee/myScreen/test1.jsp");
        initRequestContext();

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(ModuleNotFoundException.class, "ddd.eee.myScreen.Test1"));
        }
    }

    @Test
    public void result() throws Exception {
        getInvocationContext("http://localhost/app1/ddd/eee/myEventScreen/returnValue.jsp");
        initRequestContext();

        PipelineInvocationHandle invocation = pipeline.newInvocation();
        invocation.invoke();

        assertEquals("returnValue", rundata.getRequest().getAttribute("module.screen.ddd.eee.MyEventScreen"));
        assertEquals("myresult", invocation.getAttribute("result"));
    }

    @Test
    public void result_specifiedName() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("performScreenValveTests2");
        assertNotNull(pipeline);

        getInvocationContext("http://localhost/app1/ddd/eee/myEventScreen/returnValue.jsp");
        initRequestContext();

        PipelineInvocationHandle invocation = pipeline.newInvocation();
        invocation.invoke();

        assertEquals("returnValue", rundata.getRequest().getAttribute("module.screen.ddd.eee.MyEventScreen"));
        assertEquals("myresult", invocation.getAttribute("myresult"));
        assertEquals("myresult", rundata.getRequest().getAttribute("screenResult"));
    }

    public static class ReadResultValve implements Valve {
        @Autowired
        private HttpServletRequest request;

        public void invoke(PipelineContext pipelineContext) throws Exception {
            request.setAttribute("screenResult", pipelineContext.getAttribute("myresult"));
        }
    }
}
