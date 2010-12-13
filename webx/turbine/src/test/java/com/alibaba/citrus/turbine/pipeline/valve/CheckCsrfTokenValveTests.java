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

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.turbine.util.CsrfToken;

public class CheckCsrfTokenValveTests extends AbstractValveTests {
    @Before
    public void initPipeline() {
        pipeline = (PipelineImpl) factory.getBean("checkCsrfToken_manually");
        assertNotNull(pipeline);
    }

    @Test
    public void checkCsrfToken_checkManually() throws Exception {
        getInvocationContext("http://localhost/app1/a.vm");
        initRequestContext();

        CheckCsrfTokenValve valve = (CheckCsrfTokenValve) pipeline.getValves()[0];

        assertEquals("token", valve.getTokenKey());
        assertEquals("error.vm", valve.getExpiredPage());
        assertEquals(1, valve.getMaxTokens());
        assertEquals("mySecurity", valve.getLogName());

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(IllegalArgumentException.class));
        }
    }

    @Test
    public void checkCsrfToken_noRequestToken() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("checkCsrfToken");
        assertNotNull(pipeline);

        getInvocationContext("http://localhost/app1/a.vm");
        initRequestContext();

        newRequest.getSession().setAttribute("token", "aaa");
        pipeline.newInvocation().invoke();
        assertEquals("aaa", newRequest.getSession().getAttribute("token"));

        assertEquals(null, rundata.getRedirectTarget()); // no error
    }

    @Test
    public void checkCsrfToken_notMatch() throws Exception {
        getInvocationContext("http://localhost/app1/a.vm?token=notMatch");
        initRequestContext();

        newRequest.getSession().setAttribute("token", "aaa");
        pipeline.newInvocation().invoke();
        assertEquals("aaa", newRequest.getSession().getAttribute("token"));

        assertEquals("error.vm", rundata.getRedirectTarget()); // redirect to error page
    }

    @Test
    public void checkCsrfToken_notMatch_tokens() throws Exception {
        getInvocationContext("http://localhost/app1/a.vm?token=notMatch");
        initRequestContext();

        newRequest.getSession().setAttribute("token", "aaa,bbb,ccc");
        pipeline.newInvocation().invoke();
        assertEquals("aaa,bbb,ccc", newRequest.getSession().getAttribute("token"));

        assertEquals("error.vm", rundata.getRedirectTarget()); // redirect to error page
    }

    @Test
    public void checkCsrfToken_matched_longLiveToken() throws Exception {
        getInvocationContext("http://localhost/app1/a.vm");
        initRequestContext();

        String token = CsrfToken.getLongLiveTokenInSession(newRequest.getSession());

        commitRequestContext();

        getInvocationContext("http://localhost/app1/a.vm?token=" + token);
        initRequestContext();

        pipeline.newInvocation().invoke();

        assertEquals(null, newRequest.getSession().getAttribute("token"));
        assertEquals(null, rundata.getRedirectTarget());
    }

    @Test
    public void checkCsrfToken_matched() throws Exception {
        getInvocationContext("http://localhost/app1/a.vm?token=bbb");
        initRequestContext();

        newRequest.getSession().setAttribute("token", "bbb");
        pipeline.newInvocation().invoke();
        assertEquals(null, newRequest.getSession().getAttribute("token")); // removed last token

        assertEquals(null, rundata.getRedirectTarget()); // redirect to error page
    }

    @Test
    public void checkCsrfToken_matched_tokens() throws Exception {
        getInvocationContext("http://localhost/app1/a.vm?token=bbb");
        initRequestContext();

        newRequest.getSession().setAttribute("token", "aaa/bbb/ccc");
        pipeline.newInvocation().invoke();
        assertEquals("aaa/ccc", newRequest.getSession().getAttribute("token"));

        assertEquals(null, rundata.getRedirectTarget()); // redirect to error page
    }

    public static class CheckCsrfManually implements Valve {
        @Autowired
        private HttpServletRequest request;

        public void invoke(PipelineContext pipelineContext) throws Exception {
            if (!CsrfToken.check(request)) {
                throw new IllegalArgumentException();
            }
        }
    }
}
