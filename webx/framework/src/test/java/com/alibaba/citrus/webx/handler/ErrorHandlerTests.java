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
package com.alibaba.citrus.webx.handler;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;
import com.alibaba.citrus.webx.AbstractWebxTests;
import com.alibaba.citrus.webx.ResourceNotFoundException;
import com.alibaba.citrus.webx.pipeline.TestExceptionValve;
import com.alibaba.citrus.webx.pipeline.TestValve;
import com.alibaba.citrus.webx.pipeline.ValveRunner;

public class ErrorHandlerTests extends AbstractWebxTests {
    @Before
    public void init() throws Exception {
        System.setProperty("productionModeFromSystemProperties", "true");
        prepareWebClient(null);
    }

    @After
    public void destroy() throws Exception {
        System.clearProperty("productionModeFromSystemProperties");
        System.clearProperty("exceptionPipelineNameFromSystemProperties");
    }

    @Test
    public void dev_detail() throws Exception {
        System.setProperty("productionModeFromSystemProperties", "false");
        prepareWebClient(null);

        TestValve.runnerHolder.set(new ValveRunner() {
            public void run(RunData rundata, PipelineContext pipelineContext) throws Exception {
                throw new ResourceNotFoundException("not found!");
            }
        });

        invokeServlet("/app1/test.htm");

        assertEquals(404, clientResponseCode);
        assertEquals("text/html", clientResponse.getContentType());
        assertThat(clientResponseContent,
                containsAll("NOT_FOUND", "/app1/test.htm", ResourceNotFoundException.class.getName(), "not found!"));
    }

    @Test
    public void prod_sendError() throws Exception {
        TestValve.runnerHolder.set(new ValveRunner() {
            public void run(RunData rundata, PipelineContext pipelineContext) throws Exception {
                throw new ResourceNotFoundException("not found!");
            }
        });

        invokeServlet("/app2/test.htm");

        assertEquals(404, clientResponseCode);
        assertEquals("text/html", clientResponse.getContentType());

        // http unit sendError的实现，真实服务器将返回web.xml中的error-page
        assertThat(clientResponseContent, containsAll("<html><head><title></title></head><body></body></html>"));
    }

    @Test
    public void prod_ExceptionPipeline() throws Exception {
        System.setProperty("exceptionPipelineNameFromSystemProperties", "myExceptionPipeline");
        prepareWebClient(null);

        TestValve.runnerHolder.set(new ValveRunner() {
            public void run(RunData rundata, PipelineContext pipelineContext) throws Exception {
                throw new IllegalStateException("wrong!");
            }
        });

        TestExceptionValve.runnerHolder.set(new ValveRunner() {
            public void run(RunData rundata, PipelineContext pipelineContext) throws Exception {
                rundata.setContentType("text/plain");
                rundata.getResponse().getWriter().println("error page from valve");
            }
        });

        invokeServlet("/app3/test.htm");

        assertEquals(500, clientResponseCode);
        assertEquals("text/plain", clientResponse.getContentType());
        assertThat(clientResponseContent, containsAll("error page from valve"));
    }

    @Test
    public void prod_ExceptionPipeline_noSchema() throws Exception {
        System.setProperty("exceptionPipelineNameFromSystemProperties", "myExceptionPipeline");
        prepareWebClient(null);

        TestExceptionValve.runnerHolder.set(new ValveRunner() {
            public void run(RunData rundata, PipelineContext pipelineContext) throws Exception {
                rundata.setContentType("text/plain");
                rundata.getResponse().getWriter().println("error page from valve");
            }
        });

        invokeServlet("/internal/schema/");

        assertEquals(404, clientResponseCode);
        assertEquals("text/plain", clientResponse.getContentType());
        assertThat(clientResponseContent, containsAll("error page from valve"));
    }
}
