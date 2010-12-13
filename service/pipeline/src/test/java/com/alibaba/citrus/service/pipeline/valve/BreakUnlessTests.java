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
import com.alibaba.citrus.service.pipeline.impl.condition.JexlCondition;
import com.alibaba.citrus.service.pipeline.impl.valve.BreakUnlessValve;

public class BreakUnlessTests extends AbstractBreakValveTests<BreakUnlessValve> {
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
    public void init_() throws Exception {
        try {
            valve.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no condition"));
        }

        valve.setCondition(condition);
        valve.afterPropertiesSet();
    }

    @Test
    public void invoke_conditionSatisfied() throws Exception {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());
        valve.setCondition(condition);
        valve.afterPropertiesSet();

        expect(condition.isSatisfied(EasyMock.<PipelineContext> anyObject())).andReturn(true);
        replay(condition);

        assertInvoke(pipeline, false);
        assertLog("1-1" /* 1-2 */, "1-3");
    }

    @Test
    public void invoke_conditionNotSatisfied() throws Exception {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());
        valve.setCondition(condition);
        valve.afterPropertiesSet();

        expect(condition.isSatisfied(org.easymock.EasyMock.<PipelineContext> anyObject())).andReturn(false);
        replay(condition);

        assertInvoke(pipeline, true);
        assertLog("1-1");
    }

    @Test
    public void toString_() {
        valve = newInstance();
        valve.setToLabel(" mylabel ");
        assertEquals("BreakUnlessValve[toLabel=mylabel, unless null]", valve.toString());

        JexlCondition condition = new JexlCondition();
        condition.setExpression("1==2");

        valve = newInstance();
        valve.setLevels(10);
        valve.setCondition(condition);

        assertEquals("BreakUnlessValve[levels=10, unless JexlCondition[1==2]]", valve.toString());
    }

    @Test
    public void config() {
        // break-unless simple
        pipeline = getPipelineImplFromFactory("break-unless-simple");
        assertInvoke(pipeline, false);
        assertLog("1-1" /* loop */, //
                "2-1-loop-0" /* break-unless */, "2-3-loop-0", //
                "2-1-loop-1" /* break-unless */, "2-3-loop-1", //
                "2-1-loop-2" /* break-unless */, //
                "1-3");

        // break-unless levels=1
        pipeline = getPipelineImplFromFactory("break-unless-levels");
        assertInvoke(pipeline, true);
        assertLog("1-1" /* loop */, //
                "2-1-loop-0" /* break-unless */, "2-3-loop-0", //
                "2-1-loop-1" /* break-unless */, "2-3-loop-1", //
                "2-1-loop-2" /* break-unless *///
        );

        // break-unless tolabel(levels=0), with condition tag
        pipeline = getPipelineImplFromFactory("break-unless-tolabel");
        assertInvoke(pipeline, false);
        assertLog("1-1" /* loop */, //
                "2-1-loop-0" /* break-unless */, "2-3-loop-0", //
                "2-1-loop-1" /* break-unless */, //
                "1-3");
    }
}
