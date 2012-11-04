/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.async;

import static org.junit.Assert.*;

import com.alibaba.citrus.async.pipeline.valve.DoPerformRunnableValve;
import com.alibaba.citrus.async.pipeline.valve.PerformRunnableAsyncValve;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import org.junit.BeforeClass;
import org.junit.Test;

public class DoPerformRunnableValveConfigTests extends AbstractAsyncTests {
    private PerformRunnableAsyncValve performRunnableAsyncValve;
    private DoPerformRunnableValve    valve;
    private PipelineImpl              pipeline;

    @BeforeClass
    public static void initClass() {
        defaultFactory = createApplicationContext("doPerformRunnableValveConfig.xml");
    }

    @Test
    public void subPipelineNotSpecified() {
        performRunnableAsyncValve = getValve("pipeline1", 0, PerformRunnableAsyncValve.class);
        valve = getDoPerformRunnableValve(0);

        assertEquals("result", valve.getOutputKey());
    }

    @Test
    public void resultNameNotSpecified() {
        performRunnableAsyncValve = getValve("pipeline2", 0, PerformRunnableAsyncValve.class);
        valve = getDoPerformRunnableValve(1);

        assertEquals("result", valve.getOutputKey());
    }

    @Test
    public void resultNameSpecified() {
        performRunnableAsyncValve = getValve("pipeline3", 0, PerformRunnableAsyncValve.class);
        valve = getDoPerformRunnableValve(1);

        assertEquals("myresult", valve.getOutputKey());
    }

    private DoPerformRunnableValve getDoPerformRunnableValve(int index) {
        PipelineImpl asyncPipeline = (PipelineImpl) performRunnableAsyncValve.getAsyncPipeline();
        return (DoPerformRunnableValve) asyncPipeline.getValves()[index];
    }
}
