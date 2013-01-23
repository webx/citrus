/*
 * Copyright (c) 2002-2013 Alibaba Group Holding Limited.
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

import static org.junit.Assert.*;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.util.regex.Substitution;
import org.junit.Test;

public class TargetConditionTests extends AbstractValveTests {
    private static Substitution subst1;
    private static Substitution subst2;
    private static Substitution subst3;

    @Test
    public void test_targetCondition_match() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("targetCondition");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        getInvocationContext("http://localhost/app1/helloworld.jhtml");
        initRequestContext();
        pipeline.newInvocation().invoke();

        assertNotNull(subst1);
        assertNotNull(subst2);
        assertNull(subst3);

        assertEquals("world", subst1.substitute("$1"));
        assertEquals("app1/world", subst2.substitute("$1/$2"));
    }

    @Test
    public void test_targetCondition_negativeMatch() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("targetCondition");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        getInvocationContext("http://localhost/app1/xyz.jhtml");
        initRequestContext();
        pipeline.newInvocation().invoke();

        assertNull(subst1);
        assertNull(subst2);
        assertNotNull(subst3);

        assertEquals("$1", subst3.substitute("$1"));
    }

    public static class MyClass implements Valve {
        public void invoke(PipelineContext pipelineContext) throws Exception {
            subst1 = (Substitution) pipelineContext.getAttribute("v1");
            subst2 = (Substitution) pipelineContext.getAttribute("v2");
            subst3 = (Substitution) pipelineContext.getAttribute("v3");
        }
    }
}
