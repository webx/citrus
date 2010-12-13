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

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.moduleloader.ModuleNotFoundException;
import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;

public class PerformScreenValveTests extends AbstractValveTests {
    @Test
    public void test_PerformScreenValveSuccess() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("performScreenValveTests");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        getInvocationContext("http://localhost/app1/aaa/bbb/myOtherModule.jsp");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("execute success!", rundata.getRequest().getAttribute("module.screen.aaa.bbb.MyOtherModule"));
    }

    @Test
    public void test_PerformScreenValveUseDefault() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("performScreenValveTests");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        //use screen module: module.screen.aaa.bbb.Default
        //layout module: module.layout.aaa.bbb.Default
        //layout template:template/aaa/bbb/default.vm
        getInvocationContext("http://localhost/app1/aaa/bbb/myOtherModule1.jsp");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("execute success!", rundata.getRequest().getAttribute("module.screen.aaa.bbb.Default"));
    }

    @Test
    public void test_PerformScreenValveError() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("performScreenValveTests");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        try {
            //use screen module: module.screen.MyDefaultModule
            getInvocationContext("http://localhost/app1/ddd/fff/myOtherModule1.jsp");
            initRequestContext();
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(ModuleNotFoundException.class, "ddd.fff.MyOtherModule1"));
        }
    }
}
