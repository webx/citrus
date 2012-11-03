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

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import org.junit.BeforeClass;
import org.junit.Test;

public class DoPerformRunnableValveTests extends AbstractAsyncTests {
    @BeforeClass
    public static void initClass() {
        defaultFactory = createApplicationContext("doPerformRunnableValve.xml");
    }

    @Test
    public void notInPerformRunnableAsync() {
        PipelineImpl pipeline = getPipeline("pipeline1");

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(IllegalStateException.class, "<doPerformRunnable> valve should be inside <performRunnableAsync>"));
        }
    }
}
