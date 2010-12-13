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
package com.alibaba.citrus.service.pipeline;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.service.pipeline.valve.LogAndBreakValve;
import com.alibaba.citrus.service.pipeline.valve.LogAndInvokeSubValve;
import com.alibaba.citrus.service.pipeline.valve.LogAndReturnValve;
import com.alibaba.citrus.service.pipeline.valve.LogValve;

/**
 * 测试pipeline本身的功能，不包括配置。
 * 
 * @author Michael Zhou
 */
public class PipelineTests extends AbstractPipelineTests {
    @Before
    public void init() {
        pipeline = createPipeline();
    }

    @Test
    public void pipeline_getLabel() throws Exception {
        // init is null
        assertNull(pipeline.getLabel());

        // set empty label
        pipeline.setLabel(null);
        assertNull(pipeline.getLabel());

        pipeline.setLabel(" ");
        assertNull(pipeline.getLabel());

        // set value
        pipeline.setLabel(" testLabel");
        assertEquals("testLabel", pipeline.getLabel());
    }

    @Test
    public void pipeline_getValves() throws Exception {
        // init value
        assertArrayEquals(new Valve[0], pipeline.getValves());

        // set null
        pipeline = createPipeline((Valve[]) null);
        assertArrayEquals(new Valve[0], pipeline.getValves());
    }

    @Test
    public void pipeline_newInvocation() throws Exception {
        pipeline = createPipeline(new LogValve(), new LogValve(), new LogValve());

        try {
            pipeline.newInvocation(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no parent PipelineContext"));
        }
    }

    @Test
    public void pipeline_toString() {
        String str;

        // no valves
        assertEquals("Pipeline[]", pipeline.toString());

        // with valves
        pipeline = createPipeline(new LogValve(), new LogValve(), new LogValve());

        str = "";
        str += "Pipeline [\n";
        str += "  [1/3] LogValve\n";
        str += "  [2/3] LogValve\n";
        str += "  [3/3] LogValve\n";
        str += "]";

        assertEquals(str, pipeline.toString());

        pipeline = createPipeline(new LogValve(), new LogAndBreakValve(), new LogValve());

        str = "";
        str += "Pipeline [\n";
        str += "  [1/3] LogValve\n";
        str += "  [2/3] LogAndBreakValve[<null>, 0]\n";
        str += "  [3/3] LogValve\n";
        str += "]";

        assertEquals(str, pipeline.toString());
    }

    @Test
    public void pipeline_init() throws Exception {
        try {
            createPipeline(new LogValve(), null, new LogValve());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("valves[1] == null"));
        }
    }

    @Test
    public void handle_getAttribute() throws Exception {
        pipeline = createPipeline(new LogValve(), new LogAndBreakValve(), new LogValve());
        PipelineInvocationHandle handle = assertInvoke(pipeline, true);

        // init is null
        assertNull(getFieldValue(handle, "attributes", null));

        // getAttribute，不会创建attributes
        assertNull(handle.getAttribute("test"));
        assertNull(getFieldValue(handle, "attributes", null));

        // setAttribute(null)，会创建attributes
        handle.setAttribute("test", null);
        assertNotNull(getFieldValue(handle, "attributes", null));
        assertNull(handle.getAttribute("test"));

        // setAttribute(value)，自动创建attributes
        handle.setAttribute("test", "value");
        assertNotNull(getFieldValue(handle, "attributes", null));
        assertEquals("value", handle.getAttribute("test"));

        // 再次invoke，context不同
        handle = assertInvoke(pipeline, true);
        assertNull(handle.getAttribute("test"));
    }

    @Test
    public void handle_getAttribute_withParents() throws Exception {
        final PipelineContext[] contexts = new PipelineContext[3];

        final PipelineImpl p3 = createPipeline(new Valve() {
            public void invoke(PipelineContext pipelineContext) throws Exception {
                contexts[2] = pipelineContext;
                pipelineContext.invokeNext();
            }
        });

        final PipelineImpl p2 = createPipeline(new Valve() {
            public void invoke(PipelineContext pipelineContext) throws Exception {
                contexts[1] = pipelineContext;
                p3.newInvocation(pipelineContext).invoke();
                pipelineContext.invokeNext();
            }
        });

        pipeline = createPipeline(new Valve() {
            public void invoke(PipelineContext pipelineContext) throws Exception {
                contexts[0] = pipelineContext;
                p2.newInvocation(pipelineContext).invoke();
                pipelineContext.invokeNext();
            }
        });

        assertInvoke(pipeline, false);

        PipelineContext c1 = contexts[0];
        PipelineContext c2 = contexts[1];
        PipelineContext c3 = contexts[2];

        assertSame(c2, getFieldValue(c3, "parentContext", null));
        assertSame(c1, getFieldValue(c2, "parentContext", null));
        assertSame(null, getFieldValue(c1, "parentContext", null));

        c1.setAttribute("count", 1);
        assertEquals(1, c1.getAttribute("count"));
        assertEquals(1, c2.getAttribute("count"));
        assertEquals(1, c3.getAttribute("count"));

        c2.setAttribute("count", null);
        assertEquals(1, c1.getAttribute("count"));
        assertEquals(null, c2.getAttribute("count"));
        assertEquals(null, c3.getAttribute("count"));

        c3.setAttribute("count", 3);
        assertEquals(1, c1.getAttribute("count"));
        assertEquals(null, c2.getAttribute("count"));
        assertEquals(3, c3.getAttribute("count"));

        c2.setAttribute("count", null);
        assertEquals(1, c1.getAttribute("count"));
        assertEquals(null, c2.getAttribute("count"));
        assertEquals(3, c3.getAttribute("count"));
    }

    @Test
    public void handle_toString() {
        pipeline = createPipeline(new LogValve(), new LogValve(), new LogValve());

        PipelineInvocationHandle handle = pipeline.newInvocation();
        assertEquals("Executing Pipeline Valve[#0/3, level 1]", handle.toString());
    }

    @Test
    public void handle_status() {
        pipeline = createPipeline(new LogValve(), new LogValve(), new LogValve());

        // init status
        PipelineInvocationHandle handle = pipeline.newInvocation();
        assertFalse(handle.isBroken());
        assertFalse(handle.isFinished());

        // finish status
        handle.invoke();
        assertFalse(handle.isBroken());
        assertTrue(handle.isFinished());

        // broken status
        pipeline = createPipeline(new LogValve(), new LogAndBreakValve(), new LogValve());
        handle = pipeline.newInvocation();

        handle.invoke();
        assertTrue(handle.isBroken());
        assertFalse(handle.isFinished());
    }

    @Test
    public void invoke_uninited() {
        try {
            new PipelineImpl().newInvocation().invoke();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("has not been initialized"));
        }
    }

    @Test
    public void invoke_noValves() throws Exception {
        // invoke
        assertInvoke(pipeline, false);
        assertLog();

        // invoke again
        assertInvoke(pipeline, false);
        assertLog();
    }

    @Test
    public void invoke_simple() throws Exception {
        pipeline = createPipeline(new LogValve(), new LogValve(), new LogValve());

        assertInvoke(pipeline, false);
        assertLog("1-1", "1-2", "1-3");
    }

    @Test
    public void invoke_alreadyInvoked() throws Exception {
        class InvokeMultipleTimes extends LogValve {
            @Override
            public void invoke(PipelineContext pipelineContext) {
                super.invoke(pipelineContext);

                // invoke 2nd times, catched IllegalStateException
                try {
                    pipelineContext.invokeNext();
                    fail();
                } catch (IllegalStateException e) {
                    assertThat(e, exception("Valve[#3/3, level 1] has already been invoked: LogValve"));
                }

                // invoke 3rd times, throws out PipelineException
                pipelineContext.invokeNext();
            }
        }

        pipeline = createPipeline(new LogValve(), new InvokeMultipleTimes(), new LogValve());

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e,
                    exception(IllegalStateException.class, "Valve[#3/3, level 1] has already been invoked: LogValve"));
        }

        assertLog("1-1", "1-2", "1-3");
    }

    @Test
    public void invoke_subPipeline() throws Exception {
        Pipeline p3 = createPipeline(new LogValve(), new LogValve(), new LogValve());
        Pipeline p2 = createPipeline(new LogValve(), new LogAndInvokeSubValve(p3), new LogValve());
        pipeline = createPipeline(new LogValve(), new LogAndInvokeSubValve(p2), new LogValve());

        // invoke
        assertInvoke(pipeline, false);
        assertLog("1-1", "1-2", "2-1", "2-2", "3-1", "3-2", "3-3", "2-3", "1-3");
    }

    @Test
    public void invoke_again() throws Exception {
        pipeline = createPipeline(new LogValve(), new LogValve(), new LogValve());

        PipelineInvocationHandle handle = pipeline.newInvocation();

        handle.invoke();
        handle.invoke(); // again

        assertLog("1-1", "1-2", "1-3", "1-1", "1-2", "1-3");
    }

    @Test
    public void invoke_brokenPipeline() throws Exception {
        pipeline = createPipeline(new LogValve(), new LogAndBreakValve(), new LogValve());

        PipelineInvocationHandle handle = assertInvoke(pipeline, true);

        try {
            handle.invoke();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("cannot reinvoke a broken pipeline"));
        }
    }

    @Test
    public void invoke_simpleReturn() {
        pipeline = createPipeline(new LogValve(), new LogAndReturnValve(), new LogValve());

        // invoke
        PipelineInvocationHandle handle = pipeline.newInvocation();
        handle.invoke();
        assertFalse(handle.isBroken());
        assertFalse(handle.isFinished());
        assertLog("1-1", "1-2");

        // invoke again
        handle = pipeline.newInvocation();
        handle.invoke();
        assertFalse(handle.isBroken());
        assertFalse(handle.isFinished());
        assertLog("1-1", "1-2");
    }

    @Test
    public void break_simple() throws Exception {
        pipeline = createPipeline(new LogValve(), new LogAndBreakValve(), new LogValve());

        // invoke
        assertInvoke(pipeline, true);
        assertLog("1-1", "1-2");

        // invoke again
        assertInvoke(pipeline, true);
        assertLog("1-1", "1-2");
    }

    @Test
    public void break_levels_outOfBounds() throws Exception {
        PipelineImpl p3 = createPipeline(new LogValve(), new LogValve(), new LogValve());
        PipelineImpl p2 = createPipeline(new LogValve(), new LogAndInvokeSubValve(p3), new LogValve());
        pipeline = createPipeline(new LogValve(), new LogAndInvokeSubValve(p2), new LogValve());

        // break levels=3
        p3.getValves()[1] = new LogAndBreakValve(3);

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class, "Failed to invoke Valve[#2/3, level 3]",
                            "invalid break levels: 3, should be in range of [0, 3)"));
        }

        // break levels=-1
        p3.getValves()[1] = new LogAndBreakValve(-1);

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class, "Failed to invoke Valve[#2/3, level 3]",
                            "invalid break levels: -1, should be in range of [0, 3)"));
        }
    }

    @Test
    public void break_levels() throws Exception {
        PipelineImpl p3 = createPipeline(new LogValve(), new LogValve(), new LogValve());
        PipelineImpl p2 = createPipeline(new LogValve(), new LogAndInvokeSubValve(p3), new LogValve());
        pipeline = createPipeline(new LogValve(), new LogAndInvokeSubValve(p2), new LogValve());

        // break levels=2
        p3.getValves()[1] = new LogAndBreakValve(2);
        assertInvoke(pipeline, true);
        assertLog("1-1", "1-2", "2-1", "2-2", "3-1", "3-2"/* break */);

        // break levels=1
        p3.getValves()[1] = new LogAndBreakValve(1);
        assertInvoke(pipeline, false);
        assertLog("1-1", "1-2", "2-1", "2-2", "3-1", "3-2"/* break */, "1-3");

        // break levels=0
        p3.getValves()[1] = new LogAndBreakValve(0);
        assertInvoke(pipeline, false);
        assertLog("1-1", "1-2", "2-1", "2-2", "3-1", "3-2"/* break */, "2-3", "1-3");
    }

    @Test
    public void break_label_empty() throws Exception {
        pipeline = createPipeline(new LogAndBreakValve("  "));

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(IllegalArgumentException.class, "no label"));
        }

        pipeline = createPipeline(new LogAndBreakValve(null));

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(IllegalArgumentException.class, "no label"));
        }
    }

    @Test
    public void break_label_NotFound() throws Exception {
        PipelineImpl p3 = createPipeline(new LogValve(), new LogAndBreakValve(" mylabel "), new LogValve());
        PipelineImpl p2 = createPipeline(new LogValve(), new LogAndInvokeSubValve(p3), new LogValve());
        pipeline = createPipeline(new LogValve(), new LogAndInvokeSubValve(p2), new LogValve());

        pipeline.setLabel("mylabel2");

        // invoke
        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (LabelNotDefinedException e) {
            assertThat(e, exception("Could not find pipeline or sub-pipeline with label \"mylabel\" "
                    + "in the pipeline invocation stack"));
        }
    }

    @Test
    public void break_label() throws Exception {
        PipelineImpl p3 = createPipeline(new LogValve(), new LogAndBreakValve(" mylabel "), new LogValve());
        PipelineImpl p2 = createPipeline(new LogValve(), new LogAndInvokeSubValve(p3), new LogValve());
        pipeline = createPipeline(new LogValve(), new LogAndInvokeSubValve(p2), new LogValve());

        // levels = 2
        pipeline.setLabel("mylabel");
        assertInvoke(pipeline, true);
        assertLog("1-1", "1-2", "2-1", "2-2", "3-1", "3-2"/* break */);

        // levels = 1
        p2.setLabel("mylabel");
        assertInvoke(pipeline, false);
        assertLog("1-1", "1-2", "2-1", "2-2", "3-1", "3-2"/* break */, "1-3");

        // levels = 0
        p3.setLabel("mylabel");
        assertInvoke(pipeline, false);
        assertLog("1-1", "1-2", "2-1", "2-2", "3-1", "3-2"/* break */, "2-3", "1-3");
    }

    @Test
    public void break_toTop() throws Exception {
        PipelineImpl p3 = createPipeline(new LogValve(), new LogAndBreakValve(" #TOP "), new LogValve());
        PipelineImpl p2 = createPipeline(new LogValve(), new LogAndInvokeSubValve(p3), new LogValve());
        pipeline = createPipeline(new LogValve(), new LogAndInvokeSubValve(p2), new LogValve());

        // levels = 2
        assertInvoke(pipeline, true);
        assertLog("1-1", "1-2", "2-1", "2-2", "3-1", "3-2"/* break */);
    }
}
