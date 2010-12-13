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
package com.alibaba.citrus.turbine.dataresolver;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;

import com.alibaba.citrus.service.dataresolver.DataResolverContext;
import com.alibaba.citrus.service.requestcontext.parser.CookieParser;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParser;
import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;
import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.turbine.dataresolver.impl.TurbineRunDataResolverFactory;
import com.alibaba.citrus.turbine.support.MappedContext;

public class TurbineRunDataResolverTests extends AbstractDataResolverTests {
    @Test
    public void nodeps() {
        TurbineRunDataResolverFactory resolverFactory = new TurbineRunDataResolverFactory(null);

        try {
            resolverFactory.getDataResolver(new DataResolverContext(String.class, null, null));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no HttpServletRequest proxy defined"));
        }
    }

    @Test
    public void rundata() throws Exception {
        execute("action", "rundata.myAction", "doGetRundata");
        assertLog("actionLog", TurbineRunData.class);
    }

    @Test
    public void navigator() throws Exception {
        execute("action", "rundata.myAction", "doGetNavigator");
        assertLog("actionLog", TurbineRunData.class);
    }

    @Test
    public void request() throws Exception {
        execute("action", "rundata.myAction", "doGetRequest");
        assertLog("actionLog", HttpServletRequest.class);
    }

    @Test
    public void response() throws Exception {
        execute("action", "rundata.myAction", "doGetResponse");
        assertLog("actionLog", HttpServletResponse.class);
    }

    @Test
    public void session() throws Exception {
        execute("action", "rundata.myAction", "doGetSession");
        assertLog("actionLog", HttpSession.class);
    }

    @Test
    public void servletContext() throws Exception {
        execute("action", "rundata.myAction", "doGetServletContext");
        assertLog("actionLog", ServletContext.class);
    }

    @Test
    public void params() throws Exception {
        execute("action", "rundata.myAction", "doGetParameters");
        assertLog("actionLog", ParameterParser.class);
    }

    @Test
    public void cookies() throws Exception {
        execute("action", "rundata.myAction", "doGetCookies");
        assertLog("actionLog", CookieParser.class);
    }

    @Test
    public void context() throws Exception {
        execute("action", "rundata.myAction", "doGetContext");
        Context context = assertLog("actionLog", Context.class);

        assertSame(context, getRunData().getContext());
    }

    @Test
    public void context_control() throws Exception {
        getInvocationContext("/app1");
        initRequestContext();

        Context controlContext = new MappedContext();
        getRunData().setContextForControl(controlContext);

        moduleLoaderService.getModule("control", "rundata.myControl").execute();

        Context context = assertLog("controlLog", Context.class);

        assertSame(context, controlContext);
    }

    @Test
    public void requestContext() throws Exception {
        execute("action", "rundata.myAction", "doGetRequestContext");
        assertLog("actionLog", RunData.class);
    }

    @Test
    public void requestContext2() throws Exception {
        execute("action", "rundata.myAction", "doGetRequestContext2");
        assertLog("actionLog", RunData.class);
    }

    @Test
    public void requestContext3() throws Exception {
        execute("action", "rundata.myAction", "doGetRequestContext3");
        assertLog("actionLog", ParserRequestContext.class);
    }
}
