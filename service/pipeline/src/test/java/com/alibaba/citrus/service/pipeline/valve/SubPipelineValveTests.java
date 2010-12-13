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

import com.alibaba.citrus.service.pipeline.impl.valve.SubPipelineValve;

public class SubPipelineValveTests extends AbstractValveTests<SubPipelineValve> {
    @Test
    public void setSubPipeline() {
        // no subPipeline by default
        assertNull(valve.getSubPipeline());

        // set subPipeline
        pipeline = createPipeline(new LogValve(), new LogValve(), new LogValve());
        valve.setSubPipeline(pipeline);
        assertSame(pipeline, valve.getSubPipeline());
    }

    @Test
    public void init_() throws Exception {
        try {
            valve.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no sub-pipeline"));
        }

        valve.setSubPipeline(createPipeline());
        valve.afterPropertiesSet();
    }

    @Test
    public void toString_() {
        String str;

        // empty
        str = "";
        str += "SubPipelineValve {\n";
        str += "  <null>\n";
        str += "}";

        assertEquals(str, valve.toString());

        // with subPipeline
        valve.setSubPipeline(createPipeline(new LogValve(), new LogValve(), new LogValve()));

        str = "";
        str += "SubPipelineValve {\n";
        str += "  Pipeline [\n";
        str += "    [1/3] LogValve\n";
        str += "    [2/3] LogValve\n";
        str += "    [3/3] LogValve\n";
        str += "  ]\n";
        str += "}";

        assertEquals(str, valve.toString());
    }

    @Test
    public void invoke() throws Exception {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());
        valve.setSubPipeline(createPipeline(new LogValve(), new LogValve(), new LogValve()));
        valve.afterPropertiesSet();

        assertInvoke(pipeline, false);
        assertLog("1-1" /* sub-pipeline */, //
                "2-1", "2-2", "2-3", //
                "1-3");
    }

    @Test
    public void config() {
        // empty
        pipeline = getPipelineImplFromFactory("sub-pipeline-empty");
        assertInvoke(pipeline, false);
        assertLog("1-1" /* sub-pipeline */, "1-3");

        // nested pipelines
        pipeline = getPipelineImplFromFactory("sub-pipelines");
        assertInvoke(pipeline, false);
        assertLog("1-1" /* sub-pipeline */, //
                "2-1" /* sub-pipeline */, //
                "3-1" /* break to label1 */, //
                "1-3");

        // pipeline reference
        pipeline = getPipelineImplFromFactory("sub-pipeline-ref");
        assertInvoke(pipeline, false);
        assertLog("1-1" /* sub-pipeline */, //
                "2-1", //
                "1-3");
    }
}
