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
import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.PipelineInvocationHandle;
import com.alibaba.citrus.service.pipeline.impl.condition.JexlCondition;
import com.alibaba.citrus.service.pipeline.impl.valve.ChooseValve;

public class ChooseValveTests extends AbstractValveTests<ChooseValve> {
    private Condition condition;

    @Before
    public void initCondition() {
        condition = createMock(Condition.class);
    }

    @Test
    public void getWhenConditions() {
        valve.setWhenConditions(new Condition[] { condition, condition });
        assertArrayEquals(new Condition[] { condition, condition }, valve.getWhenConditions());
    }

    @Test
    public void getWhenBlocks() {
        Pipeline[] pipelines = new Pipeline[] { createPipeline(), createPipeline() };

        valve.setWhenBlocks(pipelines);
        assertArrayEquals(pipelines, valve.getWhenBlocks());
    }

    @Test
    public void getOtherwiseBlock() {
        assertNull(valve.getOtherwiseBlock());

        pipeline = createPipeline();
        valve.setOtherwiseBlock(pipeline);
        assertSame(pipeline, valve.getOtherwiseBlock());
    }

    @Test
    public void init_noConditions() throws Exception {
        valve.afterPropertiesSet();

        assertArrayEquals(new Condition[0], valve.getWhenConditions());
        assertArrayEquals(new Pipeline[0], valve.getWhenBlocks());
    }

    @Test
    public void init_condition_block_notMatch() throws Exception {
        valve.setWhenConditions(new Condition[] { condition, condition });

        try {
            valve.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("conditions and blocks not match: 2 conditions and 0 blocks"));
        }

        valve.setWhenBlocks(new Pipeline[] { createPipeline(), createPipeline(), createPipeline() });

        try {
            valve.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("conditions and blocks not match: 2 conditions and 3 blocks"));
        }

        valve.setWhenConditions(new Condition[] { condition, condition, condition });
        valve.afterPropertiesSet();
    }

    @Test
    public void init_condition_or_block_null() throws Exception {
        valve.setWhenConditions(new Condition[] { condition, null, condition });
        valve.setWhenBlocks(new Pipeline[] { createPipeline(), null, createPipeline() });

        try {
            valve.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("when[1].condition == null"));
        }

        valve.setWhenConditions(new Condition[] { condition, condition, condition });

        try {
            valve.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("when[1] == null"));
        }

        valve.setWhenBlocks(new Pipeline[] { createPipeline(), createPipeline(), createPipeline() });
        valve.afterPropertiesSet();
    }

    @Test
    public void toString_() {
        String str;

        // empty
        str = "";
        str += "ChooseValve {\n";
        str += "  whenConditions = <null>\n";
        str += "  whenBlocks     = <null>\n";
        str += "  otherwiseBlock = <null>\n";
        str += "}";

        assertEquals(str, valve.toString());

        // with content
        valve.setWhenConditions(new Condition[] { new JexlCondition("1==2"), new JexlCondition("2!=3") });
        valve.setWhenBlocks(new Pipeline[] { createPipeline(new LogValve(), new LogValve()),
                createPipeline(new LogValve(), new LogValve(), new LogValve()) });
        valve.setOtherwiseBlock(createPipeline(new LogValve()));

        str = "";
        str += "ChooseValve {\n";
        str += "  whenConditions = [\n";
        str += "                     [1/2] JexlCondition[1==2]\n";
        str += "                     [2/2] JexlCondition[2!=3]\n";
        str += "                   ]\n";
        str += "  whenBlocks     = [\n";
        str += "                     [1/2] Pipeline [\n";
        str += "                             [1/2] LogValve\n";
        str += "                             [2/2] LogValve\n";
        str += "                           ]\n";
        str += "                     [2/2] Pipeline [\n";
        str += "                             [1/3] LogValve\n";
        str += "                             [2/3] LogValve\n";
        str += "                             [3/3] LogValve\n";
        str += "                           ]\n";
        str += "                   ]\n";
        str += "  otherwiseBlock = Pipeline [\n";
        str += "                     [1/1] LogValve\n";
        str += "                   ]\n";
        str += "}";

        assertEquals(str, valve.toString());
    }

    @Test
    public void invoke_notInited() throws Exception {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(IllegalStateException.class, "not been initialized yet"));
        }
    }

    @Test
    public void invoke_empty() throws Exception {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());
        valve.afterPropertiesSet();

        assertInvoke(pipeline, false);
        assertLog("1-1", "1-3");
    }

    @Test
    public void invoke_otherwiseOnly() throws Exception {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());
        valve.setOtherwiseBlock(createPipeline(new LogValve()));
        valve.afterPropertiesSet();

        assertInvoke(pipeline, false);
        assertLog("1-1" /* choose */, //
                /* otherwise */"2-1", //
                "1-3");
    }

    @Test
    public void invoke_when_satisfied() throws Exception {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());

        valve.setWhenConditions(new Condition[] { new JexlCondition("1==2"), new JexlCondition("2!=3") });

        valve.setWhenBlocks(new Pipeline[] { createPipeline(new LogValve(), new LogValve()),
                createPipeline(new LogValve(), new LogValve(), new LogValve()) });

        valve.setOtherwiseBlock(createPipeline(new LogValve()));

        valve.afterPropertiesSet();

        assertInvoke(pipeline, false);
        assertLog("1-1" /* choose */, //
                /* when 1==2 *///
                /* when 2!=3 */"2-1", "2-2", "2-3", //
                /* otherwise *///
                "1-3");
    }

    @Test
    public void invoke_when_not_satisfied() throws Exception {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());

        valve.setWhenConditions(new Condition[] { new JexlCondition("1==2"), new JexlCondition("2==3") });

        valve.setWhenBlocks(new Pipeline[] { createPipeline(new LogValve(), new LogValve()),
                createPipeline(new LogValve(), new LogValve(), new LogValve()) });

        valve.setOtherwiseBlock(createPipeline(new LogValve()));

        valve.afterPropertiesSet();

        assertInvoke(pipeline, false);
        assertLog("1-1" /* choose */, //
                /* when 1==2 *///
                /* when 2==3 *///
                /* otherwise */"2-1", //
                "1-3");
    }

    @Test
    public void config() {
        // empty
        pipeline = getPipelineImplFromFactory("choose-empty");
        assertInvoke(pipeline, false);
        assertLog("1-1" /* choose */, "1-3");

        // otherwise only
        pipeline = getPipelineImplFromFactory("choose-otherwise-only");
        assertInvoke(pipeline, false);
        assertLog("1-1", /* choose *///
                /* otherwise */"2-1", //
                "1-3");

        // when satisfied, with label
        pipeline = getPipelineImplFromFactory("choose-when-satisfied");
        assertInvoke(pipeline, false);
        assertLog("1-1", /* choose *///
                /* when 1==2 *///
                /* when 2!=3 */"2-1" /* break */, //
                "1-3");

        // when not satisfied, with label
        pipeline = getPipelineImplFromFactory("choose-when-not-satisfied");
        assertInvoke(pipeline, false);
        assertLog("1-1", /* choose *///
                /* when 1==2 *///
                /* when 2==3 *///
                /* otherwise */"2-1", "2-2" /* break */, //
                "1-3");

        // pipeline reference
        pipeline = getPipelineImplFromFactory("choose-ref");
        PipelineInvocationHandle handle = pipeline.newInvocation();

        handle.setAttribute("value", 1);
        handle.invoke();
        assertLog("1-1", /* choose *///
                /* when value==1 */"2-1", //
                /* when value==2 *///
                /* otherwise *///
                "1-3");

        handle.setAttribute("value", 2);
        handle.invoke();
        assertLog("1-1", /* choose *///
                /* when value==1 *///
                /* when value==2 */"2-1", "2-2", //
                /* otherwise *///
                "1-3");

        handle.setAttribute("value", 3);
        handle.invoke();
        assertLog("1-1", /* choose *///
                /* when value==1 *///
                /* when value==2 *///
                /* otherwise */"2-1", "2-2", "2-3", //
                "1-3");
    }
}
