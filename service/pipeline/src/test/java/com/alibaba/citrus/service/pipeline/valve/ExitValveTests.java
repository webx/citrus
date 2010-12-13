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

import com.alibaba.citrus.service.pipeline.impl.valve.ExitValve;

public class ExitValveTests extends AbstractValveTests<ExitValve> {
    @Test
    public void toString_() {
        valve = newInstance();
        assertEquals("ExitValve", valve.toString());
    }

    @Test
    public void config() {
        pipeline = getPipelineImplFromFactory("exit-whole-pipeline");
        assertInvoke(pipeline, true);
        assertLog("1-1", "1-2", "2-1", "2-2", //
                "3-1" /* 3-2 <exit/> */);
    }
}
