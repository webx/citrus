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

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.service.pipeline.valve.ExecutionLog;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.test.TestEnvStatic;

public abstract class AbstractPipelineTests {
    protected static BeanFactory factory;
    protected PipelineImpl pipeline;

    static {
        TestEnvStatic.init();
    }

    @Before
    public final void initLog() {
        ExecutionLog.reset();
    }

    protected static void createFactory(String location) {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, location)));
    }

    protected final Pipeline getPipelineFromFactory(String beanName) {
        return (Pipeline) factory.getBean(beanName);
    }

    protected final PipelineImpl getPipelineImplFromFactory(String beanName) {
        return (PipelineImpl) factory.getBean(beanName);
    }

    protected final PipelineImpl createPipeline(Valve... valves) {
        PipelineImpl pipeline = new PipelineImpl();
        pipeline.setValves(valves);

        try {
            pipeline.afterPropertiesSet();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        return pipeline;
    }

    protected final PipelineInvocationHandle assertInvoke(Pipeline pipeline, boolean broken) {
        PipelineInvocationHandle handle = pipeline.newInvocation();

        handle.invoke();

        if (broken) {
            assertTrue(handle.isBroken());
            assertFalse(handle.isFinished());
        } else {
            assertFalse(handle.isBroken());
            assertTrue(handle.isFinished());
        }

        return handle;
    }

    protected final void assertLog(String... indexes) {
        assertArrayEquals(indexes, ExecutionLog.toArray());
        ExecutionLog.reset();
    }
}
