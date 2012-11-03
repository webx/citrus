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

package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.service.requestcontext.buffered.BufferedRequestContext;
import com.meterware.httpunit.WebResponse;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SetBufferingValveTests extends AbstractValveTests {
    private BufferedRequestContext brc;

    private void initPipeline(String id) {
        pipeline = (PipelineImpl) factory.getBean(id);
        assertNotNull(pipeline);
    }

    @Test
    public void setBuffering_default() throws Exception {
        assertBufferingEnabled("setBuffering_default");
    }

    @Test
    public void setBuffering_enabled() throws Exception {
        assertBufferingEnabled("setBuffering_enabled");
    }

    private void assertBufferingEnabled(String id) throws Exception {
        initPipeline(id);

        getInvocationContext("http://localhost/app1/myscreen");
        initRequestContext();

        brc = findRequestContext(newRequest, BufferedRequestContext.class);
        brc.setBuffering(false);
        assertEquals(false, brc.isBuffering());

        pipeline.newInvocation().invoke();
        assertEquals(true, brc.isBuffering());
    }

    @Test
    public void setBuffering_disabled() throws Exception {
        initPipeline("setBuffering_disabled");

        getInvocationContext("http://localhost/app1/myscreen");
        initRequestContext();

        brc = findRequestContext(newRequest, BufferedRequestContext.class);
        assertEquals(true, brc.isBuffering());

        pipeline.newInvocation().invoke();
        assertEquals(false, brc.isBuffering());

        WebResponse webResponse = commitRequestContext();
        assertEquals("hello", webResponse.getText());
    }

    @Test
    public void setBuffering_illegalState() throws Exception {
        initPipeline("setBuffering_disabled");

        getInvocationContext("http://localhost/app1/myscreen");
        initRequestContext();

        brc = findRequestContext(newRequest, BufferedRequestContext.class);
        assertEquals(true, brc.isBuffering());

        newResponse.getWriter();

        pipeline.newInvocation().invoke();
        assertEquals(true, brc.isBuffering()); // unchanged
    }

    public static class MyValve implements Valve {
        @Autowired
        private HttpServletResponse response;

        @Override
        public void invoke(PipelineContext pipelineContext) throws Exception {
            response.getWriter().print("hello");
            response.getWriter().flush();
            pipelineContext.invokeNext();
        }
    }
}
