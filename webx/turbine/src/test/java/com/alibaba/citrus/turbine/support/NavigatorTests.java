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
package com.alibaba.citrus.turbine.support;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.uribroker.URIBrokerService;
import com.alibaba.citrus.turbine.AbstractWebxTests;
import com.alibaba.citrus.turbine.uribroker.uri.TurbineURIBroker;

public class NavigatorTests extends AbstractWebxTests {
    private URIBrokerService uris;

    @BeforeClass
    public static void initWebx() throws Exception {
        prepareServlet();
    }

    @Before
    public void init() throws Exception {
        getInvocationContext("http://localhost/app1/1.html");
        initRequestContext();

        uris = (URIBrokerService) factory.getBean("uris");
        assertNotNull(uris);
    }

    @Test
    public void forward() {
        assertFalse(rundata.isRedirected());
        assertTrue(rundata.getParameters().isEmpty());

        rundata.forwardTo("hello").withParameter("aaa", "111").withParameter("bbb", "222", "333")
                .withParameter("ccc", (String) null).withParameter("ddd", (String[]) null);

        assertTrue(rundata.isRedirected());
        assertEquals("hello", rundata.getRedirectTarget());
        assertEquals(3, rundata.getParameters().size());
        assertArrayEquals(new String[] { "111" }, rundata.getParameters().getStrings("aaa"));
        assertArrayEquals(new String[] { "222", "333" }, rundata.getParameters().getStrings("bbb"));
        assertEquals(null, rundata.getParameters().getString("ccc"));
    }

    @Test
    public void forward_toString() {
        Object params = rundata.forwardTo("hello").withParameter("aaa", "111");

        String str = "";
        str += "forwardTo(hello) {\n";
        str += "  aaa = 111\n";
        str += "}";

        assertEquals(str, params.toString());
    }

    @Test
    public void redirect_noName() {
        try {
            rundata.redirectTo(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no uriName"));
        }

        try {
            rundata.redirectTo("  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no uriName"));
        }
    }

    @Test
    public void redirect_uri_notFound() {
        try {
            rundata.redirectTo("notFound");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("could not find uri broker named \"notFound\""));
        }
    }

    @Test
    public void redirect_setTarget_notTurbineURI() {
        try {
            rundata.redirectTo("link1").withTarget("hello");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("URI is not a turbine-uri: link1"));
        }
    }

    @Test
    public void redirect_then_checkRedirected() {
        rundata.redirectTo("link2").withTarget("hello");

        // isRedirected会触发commit redirect uri
        assertTrue(rundata.isRedirected());
        assertEquals("http://www.taobao.com/hello", rundata.getRedirectLocation());
    }

    @Test
    public void redirect_then_getRedirectLocation() {
        rundata.redirectTo("link1").withParameter("aaa", "111").withParameter("bbb", "222", "333")
                .withParameter("ccc", (String) null).withParameter("ddd", (String[]) null);

        // getRedirectLocation会触发commit redirect uri
        assertEquals("http://www.taobao.com/?aaa=111&bbb=222&bbb=333&ccc=&ddd=", rundata.getRedirectLocation());
    }

    @Test
    public void redirect_getURI() {
        TurbineURIBroker uri = (TurbineURIBroker) rundata.redirectTo("link2").uri();
        uri.setTarget("hello").addQueryData("aaa", 1);

        assertEquals("http://www.taobao.com/hello?aaa=1", rundata.getRedirectLocation());
    }

    @Test
    public void redirect_toString() {
        Object params = rundata.redirectTo("link2").withTarget("hello").withParameter("aaa", "111");

        assertEquals("redirectTo(http://www.taobao.com/hello?aaa=111)", params.toString());
        assertEquals("http://www.taobao.com/hello?aaa=111", rundata.getRedirectLocation());
    }

    @Test
    public void redirect_location() {
        rundata.redirectToLocation("http://www.sina.com.cn/?hello");
        assertEquals("http://www.sina.com.cn/?hello", rundata.getRedirectLocation());
    }
}
