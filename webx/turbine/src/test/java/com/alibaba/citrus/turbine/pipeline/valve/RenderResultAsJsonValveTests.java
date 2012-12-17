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

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.meterware.httpunit.WebResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RenderResultAsJsonValveTests extends AbstractValveTests {
    private static ThreadLocal<List<Object>> resultsHolder = new ThreadLocal<List<Object>>() {
        @Override
        protected List<Object> initialValue() {
            return new LinkedList<Object>();
        }
    };

    @Before
    public void init() {
        pipeline = (PipelineImpl) factory.getBean("renderJson");
        assertNotNull(pipeline);
    }

    @After
    public void destroy() {
        resultsHolder.remove();
    }

    @Test
    public void outputAsJson_noResult() throws Exception {
        getInvocationContext("http://localhost/app1/myJsonScreen/noResult");
        initRequestContext();
        pipeline.newInvocation().invoke();
        commitRequestContext();

        WebResponse webResponse = client.getResponse(invocationContext);

        // 当result==null时，不执行valve
        assertEquals(200, webResponse.getResponseCode());
        assertEquals("text/html", webResponse.getContentType());
        assertEquals("", webResponse.getText());
    }

    @Test
    public void outputAsJson_withResult() throws Exception {
        getInvocationContext("http://localhost/app1/myJsonScreen/withResult");
        initRequestContext();
        pipeline.newInvocation().invoke();
        commitRequestContext();

        WebResponse webResponse = client.getResponse(invocationContext);

        assertEquals(200, webResponse.getResponseCode());
        assertEquals("application/json", webResponse.getContentType());
        assertEquals("{\"age\":100,\"name\":\"michael\"}", webResponse.getText());

        List<Object> results = resultsHolder.get();

        assertEquals(2, results.size());
        assertTrue(results.get(0) instanceof com.alibaba.test.app1.module.screen.MyJsonScreen.MyObject);
        assertNull(results.get(1)); // 确保运行完以后，清除result对象
    }

    @Test
    public void outputAsJson_withResult_specifiedContentType() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("renderJson_specifiedContentType");
        getInvocationContext("http://localhost/app1/myJsonScreen/withResult");
        initRequestContext();
        pipeline.newInvocation().invoke();
        commitRequestContext();

        WebResponse webResponse = client.getResponse(invocationContext);

        assertEquals(200, webResponse.getResponseCode());
        assertEquals("text/plain", webResponse.getContentType());
        assertEquals("{\"age\":100,\"name\":\"michael\"}", webResponse.getText());
    }

    @Test
    public void outputAsJs_noResult() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("renderJsonAsJs");
        getInvocationContext("http://localhost/app1/myJsonScreen/noResult");
        initRequestContext();
        pipeline.newInvocation().invoke();
        commitRequestContext();

        WebResponse webResponse = client.getResponse(invocationContext);

        // 当result==null时，不执行valve
        assertEquals(200, webResponse.getResponseCode());
        assertEquals("text/html", webResponse.getContentType());
        assertEquals("", webResponse.getText());
    }

    @Test
    public void outputAsJs_withResult() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("renderJsonAsJs");
        getInvocationContext("http://localhost/app1/myJsonScreen/withResult");
        initRequestContext();
        pipeline.newInvocation().invoke();
        commitRequestContext();

        WebResponse webResponse = client.getResponse(invocationContext);

        assertEquals(200, webResponse.getResponseCode());
        assertEquals("application/javascript", webResponse.getContentType());
        assertEquals("var myresult = {\"age\":100,\"name\":\"michael\"};", webResponse.getText());
    }

    @Test
    public void outputAsJs_withResult_specifiedContentType() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("renderJsonAsJs_specifiedContentType");
        getInvocationContext("http://localhost/app1/myJsonScreen/withResult");
        initRequestContext();
        pipeline.newInvocation().invoke();
        commitRequestContext();

        WebResponse webResponse = client.getResponse(invocationContext);

        assertEquals(200, webResponse.getResponseCode());
        assertEquals("text/js", webResponse.getContentType());
        assertEquals("var myresult = {\"age\":100,\"name\":\"michael\"};", webResponse.getText());
    }

    @Test
    public void outputAsJs_redirect() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("renderJsonAsJs");
        getInvocationContext("http://localhost/app1/myJsonScreen/withResult");
        initRequestContext();
        newResponse.sendRedirect("http://localhost/newlocation");
        pipeline.newInvocation().invoke();
        commitRequestContext();

        WebResponse webResponse = client.getResponse(invocationContext);

        assertEquals(302, webResponse.getResponseCode());
        assertEquals("http://localhost/newlocation", webResponse.getHeaderField("location"));
        assertEquals("", webResponse.getText());
    }

    public static class ResultCheck extends AbstractInputOutputValve {
        @Override
        public void invoke(PipelineContext pipelineContext) throws Exception {
            resultsHolder.get().add(getInputValue(pipelineContext));
            pipelineContext.invokeNext();
        }
    }
}
