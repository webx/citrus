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
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.pipeline.TooManyLoopsException;
import com.alibaba.citrus.service.pipeline.impl.valve.LoopValve;

public class LoopValveTests extends AbstractLoopValveTests<LoopValve> {
    @Test
    public void toString_() {
        String str = "";
        str += "LoopValve [\n";
        str += "  <null>\n";
        str += "]";

        // no body
        assertEquals(str, valve.toString());

        // with body
        Pipeline pipeline = createPipeline(new LogValve(), new LogValve(), new LogValve());
        valve.setLoopBody(pipeline);

        str = "";
        str += "LoopValve [\n";
        str += "  Pipeline [\n";
        str += "    [1/3] LogValve\n";
        str += "    [2/3] LogValve\n";
        str += "    [3/3] LogValve\n";
        str += "  ]\n";
        str += "]";

        assertEquals(str, valve.toString());
    }

    @Test
    public void config() {
        // loop and break
        ExecutionLog.counterName = "count";
        pipeline = getPipelineImplFromFactory("loop");
        assertEquals("count", ((LoopValve) pipeline.getValves()[1]).getLoopCounterName()); // specified name
        assertInvoke(pipeline, false);
        assertLog("1-1" /* 1-2 <loop/> */, "2-1-loop-0" /* 2-2 <break/> */, "1-3");
        ExecutionLog.counterName = "loopCount";

        // loop and break level2
        pipeline = getPipelineImplFromFactory("loop-break-up-to-level2");
        assertEquals("loopCount", ((LoopValve) pipeline.getValves()[1]).getLoopCounterName()); // default name
        assertInvoke(pipeline, false);
        assertLog("1-1" /* 1-2 <loop/> */, "2-1-loop-0" /* 2-2 <break/> */, "1-3");

        // loop and break level1
        pipeline = getPipelineImplFromFactory("loop-break-up-to-level1");
        assertInvoke(pipeline, true);
        assertLog("1-1" /* 1-2 <loop/> */, "2-1-loop-0" /* 2-2 <break/> */);

        // loop with maxLoopCount=2
        pipeline = getPipelineImplFromFactory("loop-max-loop-count");

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
        pipeline = getPipelineImplFromFactory("loop-ref");

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (TooManyLoopsException e) {
            assertThat(e, exception("Too many loops: exceeds the maximum count: 2"));
        }

        assertLog("1-1" /* 1-2 <loop/> */, //
                "2-1-loop-0", //
                "2-1-loop-1" //
        );
    }
}
