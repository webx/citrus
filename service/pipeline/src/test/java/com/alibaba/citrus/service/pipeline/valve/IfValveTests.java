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
package com.alibaba.citrus.service.pipeline.valve;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.pipeline.Condition;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.PipelineInvocationHandle;
import com.alibaba.citrus.service.pipeline.impl.condition.JexlCondition;
import com.alibaba.citrus.service.pipeline.impl.valve.IfValve;

public class IfValveTests extends AbstractValveTests<IfValve> {
    private Condition condition;

    @Before
    public void initCondition() {
        condition = createMock(Condition.class);
    }

    @Test
    public void setCondition() {
        assertNull(valve.getCondition());

        valve.setCondition(condition);
        assertSame(condition, valve.getCondition());
    }

    @Test
    public void setBlock() {
        // no block by default
        assertNull(valve.getBlock());

        // set block
        pipeline = createPipeline(new LogValve(), new LogValve(), new LogValve());
        valve.setBlock(pipeline);
        assertSame(pipeline, valve.getBlock());
    }

    @Test
    public void init_() throws Exception {
        try {
            valve.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no condition"));
        }

        valve.setCondition(condition);

        try {
            valve.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no if-block"));
        }

        valve.setBlock(createPipeline());
        valve.afterPropertiesSet();
    }

    @Test
    public void toString_() {
        String str;

        // empty
        str = "";
        str += "IfValve {\n";
        str += "  condition = <null>\n";
        str += "  block     = <null>\n";
        str += "}";

        assertEquals(str, valve.toString());

        // with condition & block
        valve.setCondition(new JexlCondition("1==2"));
        valve.setBlock(createPipeline(new LogValve(), new LogValve(), new LogValve()));

        str = "";
        str += "IfValve {\n";
        str += "  condition = JexlCondition[1==2]\n";
        str += "  block     = Pipeline [\n";
        str += "                [1/3] LogValve\n";
        str += "                [2/3] LogValve\n";
        str += "                [3/3] LogValve\n";
        str += "              ]\n";
        str += "}";

        assertEquals(str, valve.toString());
    }

    @Test
    public void invoke_conditionSatisfied() throws Exception {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());
        valve.setCondition(condition);
        valve.setBlock(createPipeline(new LogValve(), new LogValve(), new LogValve()));
        valve.afterPropertiesSet();

        expect(condition.isSatisfied(EasyMock.<PipelineContext> anyObject())).andReturn(true);
        replay(condition);

        assertInvoke(pipeline, false);
        assertLog("1-1" /* if */, //
                "2-1", "2-2", "2-3", //
                "1-3");
    }

    @Test
    public void invoke_conditionNotSatisfied() throws Exception {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());
        valve.setCondition(condition);
        valve.setBlock(createPipeline(new LogValve(), new LogValve(), new LogValve()));
        valve.afterPropertiesSet();

        expect(condition.isSatisfied(EasyMock.<PipelineContext> anyObject())).andReturn(false);
        replay(condition);

        assertInvoke(pipeline, false);
        assertLog("1-1" /* if */, //
                "1-3");
    }

    @Test
    public void config() {
        // empty
        pipeline = getPipelineImplFromFactory("if-empty");
        assertInvoke(pipeline, false);
        assertLog("1-1" /* if */, "1-3");

        // satisfied
        pipeline = getPipelineImplFromFactory("if-satisfied");
        assertInvoke(pipeline, false);
        assertLog("1-1" /* if */, //
                "2-1", "2-2", "2-3", //
                "1-3");

        // not satisfied
        pipeline = getPipelineImplFromFactory("if-not-satisfied");
        assertInvoke(pipeline, false);
        assertLog("1-1" /* if */, //
                "1-3");

        pipeline = getPipelineImplFromFactory("if-condition-label");
        assertInvoke(pipeline, false);
        assertLog("1-1" /* if-block1 */, //
                /* loop *///
                "3-1-loop-0" /* if loopCount==2 */, "3-3-loop-0", //
                "3-1-loop-1" /* if loopCount==2 */, "3-3-loop-1", //
                "3-1-loop-2" /* if loopCount==2 */, //
                /* 4-1 break to if-block1 *///
                "1-3");

        // pipeline reference
        pipeline = getPipelineImplFromFactory("if-ref");
        PipelineInvocationHandle handle = pipeline.newInvocation();
        handle.setAttribute("value", 1);
        handle.invoke();
        assertLog("1-1", //
                /* if value==1 */"2-1", //
                "1-3");
    }
}
