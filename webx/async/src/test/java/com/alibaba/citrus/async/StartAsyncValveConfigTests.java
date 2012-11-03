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

import com.alibaba.citrus.async.pipeline.valve.InvokeCallableValve;
import com.alibaba.citrus.async.pipeline.valve.StartAsyncValve;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.task.AsyncTaskExecutor;

public class StartAsyncValveConfigTests extends AbstractAsyncTests {
    private AsyncTaskExecutor executor1;
    private AsyncTaskExecutor executor2;
    private StartAsyncValve   valve;
    private PipelineImpl      pipeline;

    @BeforeClass
    public static void initClass() {
        defaultFactory = createApplicationContext("startAsyncValveConfig.xml");
    }

    @Before
    public void init() {
        executor1 = (AsyncTaskExecutor) factory.getBean("executor1");
        assertNotNull(executor1);

        executor2 = (AsyncTaskExecutor) factory.getBean("executor2");
        assertNotNull(executor2);
    }

    @Test
    public void noExecutor() {
        try {
            createApplicationContext("startAsyncValveConfig-noExecutor.xml");
            fail();
        } catch (BeanCreationException e) {
            assertThat(e, exception(NoSuchBeanDefinitionException.class, AsyncTaskExecutor.class.getName()));
        }
    }

    @Test
    public void autowireExecutor() {
        valve = getValve("pipeline1", 0, StartAsyncValve.class);
        assertSame(executor1, valve.getExecutor());
    }

    @Test
    public void specifyingExecutorRef() {
        valve = getValve("pipeline2", 0, StartAsyncValve.class);
        assertSame(executor2, valve.getExecutor());
    }

    @Test
    public void defaultSubPipeline() {
        valve = getValve("pipeline1", 0, StartAsyncValve.class);

        PipelineImpl asyncPipeline = (PipelineImpl) valve.getAsyncPipeline();

        assertEquals(1, asyncPipeline.getValves().length);
        assertTrue(asyncPipeline.getValves()[0] instanceof InvokeCallableValve);
    }

    @Test
    public void specifyingSubPipelineRef() {
        valve = getValve("pipeline3", 0, StartAsyncValve.class);

        PipelineImpl asyncPipeline = (PipelineImpl) valve.getAsyncPipeline();

        assertSame(factory.getBean("subpipeline1"), asyncPipeline);

        assertEquals(2, asyncPipeline.getValves().length);
        assertTrue(asyncPipeline.getValves()[1] instanceof InvokeCallableValve);
    }

    @Test
    public void inlineSubPipeline() {
        valve = getValve("pipeline4", 0, StartAsyncValve.class);

        PipelineImpl asyncPipeline = (PipelineImpl) valve.getAsyncPipeline();

        assertNotSame(factory.getBean("subpipeline1"), asyncPipeline);

        assertEquals(2, asyncPipeline.getValves().length);
        assertTrue(asyncPipeline.getValves()[1] instanceof InvokeCallableValve);
    }

    @Test
    public void defaultTimeoutNotSpecified() {
        valve = getValve("pipeline1", 0, StartAsyncValve.class);
        assertEquals(0L, valve.getDefaultTimeout());
    }

    @Test
    public void defaultTimeoutSpecified() {
        valve = getValve("pipeline5", 0, StartAsyncValve.class);
        assertEquals(1000L, valve.getDefaultTimeout());
    }

    @Test
    public void resultNameNotSpecified() {
        valve = getValve("pipeline1", 0, StartAsyncValve.class);
        assertEquals("screenResult", valve.getResultName());
    }

    @Test
    public void resultNameSpecified() {
        valve = getValve("pipeline6", 0, StartAsyncValve.class);
        assertEquals("myresult", valve.getResultName());
    }
}
