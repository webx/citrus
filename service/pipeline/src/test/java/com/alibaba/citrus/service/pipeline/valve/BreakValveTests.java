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

import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.pipeline.impl.valve.BreakValve;

public class BreakValveTests extends AbstractBreakValveTests<BreakValve> {
    @Test
    public void toString_() {
        valve = newInstance();
        valve.setToLabel(" mylabel ");
        assertEquals("BreakValve[toLabel=mylabel]", valve.toString());

        valve = newInstance();
        valve.setLevels(10);
        assertEquals("BreakValve[levels=10]", valve.toString());
    }

    @Test
    public void break_config() {
        // break simple
        pipeline = getPipelineImplFromFactory("break-simple");
        assertInvoke(pipeline, true);
        assertLog("1-1" /* 1-2 <break/> */);

        // break levels
        pipeline = getPipelineImplFromFactory("break-levels");
        assertInvoke(pipeline, false);
        assertLog("1-1", "1-2", "2-1", "2-2", //
                "3-1" /* 3-2 <break levels=1/> */, "1-3");

        // break levels
        pipeline = getPipelineImplFromFactory("break-up-to-label");
        assertInvoke(pipeline, false);
        assertLog("1-1", "1-2", "2-1", "2-2", //
                "3-1" /* 3-2 <break levels=1/> */, "1-3");
    }
}
