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
package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.turbine.util.TurbineUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.turbine.TurbineRunData;

public class PerformActionValveTests extends AbstractValveTests {
    @Test
    public void test_PerformActionValve_skipped() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("performActionValveTests");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        //has action and execute success
        getInvocationContext("http://localhost/app1/aaa/bbb/myOtherModule.jsp?action=aaa/bbb/MyOtherAction");
        initRequestContext();

        TurbineRunData rundata = getTurbineRunData(request);
        rundata.setRedirectTarget("other");

        pipeline.newInvocation().invoke();
        assertEquals(null, rundata.getRequest().getAttribute("module.action.aaa.bbb.MyOtherAction"));
    }

    @Test
    public void test_PerformActionValveSuccess() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("performActionValveTests");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        //has action and execute success
        getInvocationContext("http://localhost/app1/aaa/bbb/myOtherModule.jsp?action=aaa/bbb/MyOtherAction");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("execute success!", rundata.getRequest().getAttribute("module.action.aaa.bbb.MyOtherAction"));
    }

    @Test
    public void test_PerformActionValveFailedNoAction() {
        pipeline = (PipelineImpl) factory.getBean("performActionValveTests");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        try {
            //has not action , failed
            getInvocationContext("http://localhost/app1/aaa/bbb/myOtherModule.jsp?action=aaa/bbb1/MyOtherAction");
            initRequestContext();
            pipeline.newInvocation().invoke();
        } catch (Exception e) {
            assertEquals("Could not load action module: aaa.bbb1.MyOtherAction", e.getMessage());
        }
    }

    @Test
    public void test_PerformActionValveFailedActionExpection() {
        pipeline = (PipelineImpl) factory.getBean("performActionValveTests");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        try {
            //has action but execute failed
            getInvocationContext("http://localhost/app1/aaa/bbb/myOtherModule.jsp?action=aaa/bbb/Default");
            initRequestContext();
            pipeline.newInvocation().invoke();
        } catch (Exception e) {
            assertEquals("default action execute error!", e.getCause().getMessage());
        }
    }
}
