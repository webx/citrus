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
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.TooManyLoopsException;
import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.service.pipeline.impl.valve.LoopValve;

public class AbstractLoopValveTests<V extends LoopValve> extends AbstractValveTests<V> {
    @Test
    public void getLoopBody() {
        // no body by default
        assertNull(valve.getLoopBody());

        // set body
        Pipeline pipeline = createPipeline(new LogValve(), new LogValve(), new LogValve());
        valve.setLoopBody(pipeline);
        assertSame(pipeline, valve.getLoopBody());
    }

    @Test
    public void getMaxLoopCount() {
        // default value
        assertEquals(10, valve.getMaxLoopCount());

        // value <= 0, no limit
        valve.setMaxLoopCount(0);
        assertEquals(0, valve.getMaxLoopCount());

        valve.setMaxLoopCount(-1);
        assertEquals(0, valve.getMaxLoopCount());

        // set positive value
        valve.setMaxLoopCount(20);
        assertEquals(20, valve.getMaxLoopCount());
    }

    @Test
    public void getLoopCounterName() {
        // default value 
        assertEquals("loopCount", valve.getLoopCounterName());

        // set empty
        valve.setLoopCounterName(null);
        assertEquals("loopCount", valve.getLoopCounterName());

        valve.setLoopCounterName("  ");
        assertEquals("loopCount", valve.getLoopCounterName());

        // set value
        valve.setLoopCounterName(" test ");
        assertEquals("test", valve.getLoopCounterName());
    }

    @Test
    public void init_() throws Exception {
        try {
            valve.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no loop body"));
        }
    }

    @Test
    public void loop_notInited() throws Exception {
        PipelineImpl pipeline = createPipeline(new LogValve(), valve, new LogValve());

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(IllegalStateException.class, "not been initialized yet"));
        }
    }

    @Test
    public void loop_exceedsMax() throws Exception {
        PipelineImpl pipeline = createPipeline(new LogValve(), valve, new LogValve());

        // default maxLoopCount = 10
        valve.setLoopBody(createPipeline(new LogValve(), new LogValve(), new LogValve()));
        valve.afterPropertiesSet();

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (TooManyLoopsException e) {
            assertThat(e, exception("Too many loops: exceeds the maximum count: 10"));
        }

        assertLog("1-1", //
                "2-1-loop-0", "2-2-loop-0", "2-3-loop-0", //
                "2-1-loop-1", "2-2-loop-1", "2-3-loop-1", //
                "2-1-loop-2", "2-2-loop-2", "2-3-loop-2", //
                "2-1-loop-3", "2-2-loop-3", "2-3-loop-3", //
                "2-1-loop-4", "2-2-loop-4", "2-3-loop-4", //
                "2-1-loop-5", "2-2-loop-5", "2-3-loop-5", //
                "2-1-loop-6", "2-2-loop-6", "2-3-loop-6", //
                "2-1-loop-7", "2-2-loop-7", "2-3-loop-7", //
                "2-1-loop-8", "2-2-loop-8", "2-3-loop-8", //
                "2-1-loop-9", "2-2-loop-9", "2-3-loop-9" //
        );

        // set maxLoopCount = 1
        valve.setLoopBody(createPipeline(new LogValve(), new LogValve(), new LogValve()));
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
    public void loop_withBreak() throws Exception {
        PipelineImpl pipeline = createPipeline(new LogValve(), valve, new LogValve());

        final int[] counter = new int[] { 2 };

        class CountDownValve implements Valve {
            private int levels;

            public CountDownValve(int levels) {
                this.levels = levels;
            }

            public void invoke(PipelineContext pipelineContext) {
                ExecutionLog.add(pipelineContext);

                if (--counter[0] == 0) {
                    pipelineContext.breakPipeline(levels);
                }

                pipelineContext.invokeNext();
            }
        }

        // break levels=0, loops=2
        valve.setLoopBody(createPipeline(new LogValve(), new CountDownValve(0), new LogValve()));
        valve.afterPropertiesSet();

        counter[0] = 2;
        assertInvoke(pipeline, false);
        assertLog("1-1", //
                "2-1-loop-0", "2-2-loop-0", "2-3-loop-0", //
                "2-1-loop-1", "2-2-loop-1", /* broken *///
                "1-3");

        // break levels=1, loops=3
        counter[0] = 3;
        valve.setLoopBody(createPipeline(new LogValve(), new CountDownValve(1), new LogValve()));

        assertInvoke(pipeline, true);
        assertLog("1-1", //
                "2-1-loop-0", "2-2-loop-0", "2-3-loop-0", //
                "2-1-loop-1", "2-2-loop-1", "2-3-loop-1", //
                "2-1-loop-2", "2-2-loop-2" /* broken *///
        );

        // break levels=1, loops=20, maxLoopCount=unlimited
        counter[0] = 20;
        valve.setLoopBody(createPipeline(new LogValve(), new CountDownValve(1), new LogValve()));
        valve.setMaxLoopCount(0);

        assertInvoke(pipeline, true);
        assertLog("1-1", //
                "2-1-loop-0", "2-2-loop-0", "2-3-loop-0", //
                "2-1-loop-1", "2-2-loop-1", "2-3-loop-1", //
                "2-1-loop-2", "2-2-loop-2", "2-3-loop-2", //
                "2-1-loop-3", "2-2-loop-3", "2-3-loop-3", //
                "2-1-loop-4", "2-2-loop-4", "2-3-loop-4", //
                "2-1-loop-5", "2-2-loop-5", "2-3-loop-5", //
                "2-1-loop-6", "2-2-loop-6", "2-3-loop-6", //
                "2-1-loop-7", "2-2-loop-7", "2-3-loop-7", //
                "2-1-loop-8", "2-2-loop-8", "2-3-loop-8", //
                "2-1-loop-9", "2-2-loop-9", "2-3-loop-9", //
                "2-1-loop-10", "2-2-loop-10", "2-3-loop-10", //
                "2-1-loop-11", "2-2-loop-11", "2-3-loop-11", //
                "2-1-loop-12", "2-2-loop-12", "2-3-loop-12", //
                "2-1-loop-13", "2-2-loop-13", "2-3-loop-13", //
                "2-1-loop-14", "2-2-loop-14", "2-3-loop-14", //
                "2-1-loop-15", "2-2-loop-15", "2-3-loop-15", //
                "2-1-loop-16", "2-2-loop-16", "2-3-loop-16", //
                "2-1-loop-17", "2-2-loop-17", "2-3-loop-17", //
                "2-1-loop-18", "2-2-loop-18", "2-3-loop-18", //
                "2-1-loop-19", "2-2-loop-19" /* broken *///
        );
    }
}
