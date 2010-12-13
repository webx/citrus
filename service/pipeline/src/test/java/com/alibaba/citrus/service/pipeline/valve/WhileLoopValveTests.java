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
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.pipeline.Condition;
import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.pipeline.TooManyLoopsException;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.service.pipeline.impl.condition.JexlCondition;
import com.alibaba.citrus.service.pipeline.impl.valve.WhileLoopValve;

public class WhileLoopValveTests extends AbstractLoopValveTests<WhileLoopValve> {
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
    @Override
    public void init_() throws Exception {
        super.init_(); // test no loop body

        valve.setLoopBody(createPipeline());

        try {
            valve.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no condition"));
        }
    }

    @Test
    public void toString_() {
        String str = "";
        str += "WhileLoopValve {\n";
        str += "  condition = <null>\n";
        str += "  loopBody  = <null>\n";
        str += "}";

        // no body/conditon
        assertEquals(str, valve.toString());

        // with body/condition
        Pipeline pipeline = createPipeline(new LogValve(), new LogValve(), new LogValve());
        valve.setCondition(new JexlCondition("1==2"));
        valve.setLoopBody(pipeline);

        str = "";
        str += "WhileLoopValve {\n";
        str += "  condition = JexlCondition[1==2]\n";
        str += "  loopBody  = Pipeline [\n";
        str += "                [1/3] LogValve\n";
        str += "                [2/3] LogValve\n";
        str += "                [3/3] LogValve\n";
        str += "              ]\n";
        str += "}";

        assertEquals(str, valve.toString());
    }

    @Test
    @Override
    public void loop_exceedsMax() throws Exception {
        valve.setCondition(new JexlCondition("1==1"));
        super.loop_exceedsMax();
    }

    @Test
    @Override
    public void loop_withBreak() throws Exception {
        valve.setCondition(new JexlCondition("1==1"));
        super.loop_withBreak();
    }

    @Test
    public void loop_conditionNotSatisfied() throws Exception {
        PipelineImpl pipeline = createPipeline(new LogValve(), valve, new LogValve());

        valve.setCondition(new JexlCondition("loopCount<=2"));
        valve.setLoopBody(createPipeline(new LogValve(), new LogValve(), new LogValve()));
        valve.afterPropertiesSet();

        assertInvoke(pipeline, false);
        assertLog("1-1", //
                "2-1-loop-0", "2-2-loop-0", "2-3-loop-0", //
                "2-1-loop-1", "2-2-loop-1", "2-3-loop-1", //
                "2-1-loop-2", "2-2-loop-2", "2-3-loop-2", //
                "1-3");

        // set maxLoopCount = 1
        valve.setMaxLoopCount(1);

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (TooManyLoopsException e) {
            assertThat(e, exception("Too many loops: exceeds the maximum count: 1"));
        }

        assertLog("1-1", //
                "2-1-loop-0", "2-2-loop-0", "2-3-loop-0");
    }

    @Test
    public void config() throws Exception {
        // normal process
        pipeline = getPipelineImplFromFactory("while-loop");
        ExecutionLog.counterName = "count";

        assertInvoke(pipeline, false);
        assertLog("1-1", //
                "2-1-loop-0", "2-2-loop-0", "2-3-loop-0", //
                "2-1-loop-1", "2-2-loop-1", "2-3-loop-1", //
                "2-1-loop-2", "2-2-loop-2", "2-3-loop-2", //
                "1-3");

        // break
        pipeline = getPipelineImplFromFactory("while-loop-break-to-label");
        ExecutionLog.counterName = "loopCount";

        assertInvoke(pipeline, false);
        assertLog("1-1", //
                "2-1-loop-0" /* break-unless */, "2-3-loop-0", //
                "2-1-loop-1" /* break-unless */, "2-3-loop-1", //
                "2-1-loop-2" /* break-unless */, "2-3-loop-2", //
                "2-1-loop-3" /* break-unless */, "2-3-loop-3", //
                "2-1-loop-4", /* break *///
                "1-3");

        // loop with maxLoopCount=2
        pipeline = getPipelineImplFromFactory("while-loop-max-loop-count");

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (TooManyLoopsException e) {
            assertThat(e, exception("Too many loops: exceeds the maximum count: 2"));
        }

        assertLog("1-1" /* 1-2 <loop/> */, //
                "2-1-loop-0", "2-2-loop-0", "2-3-loop-0", //
                "2-1-loop-1", "2-2-loop-1", "2-3-loop-1" //
        );

        // pipeline reference
        pipeline = getPipelineImplFromFactory("while-ref");
        assertInvoke(pipeline, false);
        assertLog("1-1", //
                "2-1-loop-0", "2-2-loop-0", //
                "2-1-loop-1", "2-2-loop-1", //
                "1-3");
    }
}
