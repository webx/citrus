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

import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;

public class PerformTemplateScreenValveTests extends AbstractValveTests {
    @Test
    public void test_PerformTemplateScreenValveHasScreen() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("performTemplateScreenValveTests");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        //use screen module: module.screen.aaa.bbb.MyOtherModule
        //screen template:template/aaa/bbb/myOtherModule.vm
        //layout module: module.layout.aaa.bbb.MyOtherModule
        //layout template:template/aaa/bbb/myOtherModule.vm
        getInvocationContext("http://localhost/app1/aaa/bbb/myOtherModule.jsp");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("execute success!", rundata.getRequest().getAttribute("module.screen.aaa.bbb.MyOtherModule"));
        // assertEquals("execute success!", rundata.getRequest().getAttribute("module.layout.aaa.bbb.MyOtherModule"));
    }

    @Test
    public void test_PerformTemplateScreenValveUseDefault() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("performTemplateScreenValveTests");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        //use screen module: module.screen.aaa.bbb.Default
        //screen template:template/aaa/bbb/default.vm
        //layout module: module.layout.aaa.bbb.Default
        //layout template:template/aaa/bbb/default.vm
        getInvocationContext("http://localhost/app1/aaa/bbb/myOtherModule1.jsp");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("execute success!", rundata.getRequest().getAttribute("module.screen.aaa.bbb.Default"));
        //assertEquals("execute success!", rundata.getRequest().getAttribute("module.layout.aaa.bbb.Default"));
    }

    @Test
    public void test_PerformTemplateScreenValveError() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("performTemplateScreenValveTests");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        //use screen module: module.screen.MyDefaultModule
        //screen template is not exists
        //layout module (ddd.fff.MyOtherModule1) is not exists
        //layout template is not exists
        getInvocationContext("http://localhost/app1/ddd/fff/myOtherModule1.jsp");
        initRequestContext();
        pipeline.newInvocation().invoke();
    }
}
