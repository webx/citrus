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

public class AnalyzeURLValveTests extends AbstractValveTests {
    @Test
    public void test_AnalyzeURL() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("analyzeURL");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        getInvocationContext("http://localhost/app1/TEst.jhtml");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("/tEst.jsp", rundata.getTarget());

        getInvocationContext("http://localhost/app1/helloWorld/TEst.vhtml");
        initRequestContext();
        pipeline.newInvocation().invoke();
        assertEquals("/helloWorld/tEst.vm", rundata.getTarget());
    }
}
