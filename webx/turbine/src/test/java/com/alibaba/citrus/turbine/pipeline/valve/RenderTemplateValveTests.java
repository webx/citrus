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
package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.service.requestcontext.buffered.BufferedRequestContext;
import com.alibaba.citrus.service.template.TemplateNotFoundException;
import com.alibaba.citrus.turbine.TurbineConstant;

public class RenderTemplateValveTests extends AbstractValveTests {
    @Test
    public void test_RenderTemplateValve() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("renderTemplate");
        assertNotNull(pipeline);
        assertNotNull(rundata);

        getInvocationContext("http://localhost/app1/aaa/bbb/myOtherModule.vm");
        initRequestContext();
        rundata.setLayoutEnabled(true);
        pipeline.newInvocation().invoke();
        String text = (String) rundata.getContext().get(TurbineConstant.SCREEN_PLACEHOLDER_KEY);
        assertEquals("hello", text);
        text = findRequestContext(newRequest, BufferedRequestContext.class).popCharBuffer();
        assertEquals("hello", text);

        try {
            getInvocationContext("http://localhost/app1/myOtherModule.vm");
            initRequestContext();
            pipeline.newInvocation().invoke();
        } catch (PipelineException e) {
            assertThat(e, exception(TemplateNotFoundException.class, "/myprefix/myOtherModule.vm"));
        }
    }
}
