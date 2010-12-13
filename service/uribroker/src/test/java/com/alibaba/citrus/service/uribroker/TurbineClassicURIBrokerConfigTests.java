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
package com.alibaba.citrus.service.uribroker;

import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.uribroker.uri.TurbineClassicURIBroker;

public class TurbineClassicURIBrokerConfigTests extends AbstractURIBrokerServiceTests {
    private TurbineClassicURIBroker uri;

    @Test
    public void classicLink() {
        uri = (TurbineClassicURIBroker) service.getURIBroker("classicLink");
        assertEquals("http://localhost/myapp/myservlet", uri.render());
    }

    @Test
    public void classicLink2() {
        uri = (TurbineClassicURIBroker) service.getURIBroker("classicLink2");
        assertEquals("http://localhost/mycontext2/myservlet2/template/a%2Cb%2CC"
                + "/screen/a.B/action/a.b.C/aaa/111/bbb/222/ccc/333", uri.render());
    }

    @Test
    public void classicLink3_override() {
        uri = (TurbineClassicURIBroker) service.getURIBroker("classicLink3");
        assertEquals("http://localhost/template/null/screen/null/action/null/aaa/111/bbb/222/ccc/333", uri.render());
    }

    @Test
    public void turbineParams() {
        uri = (TurbineClassicURIBroker) service.getURIBroker("classicLink");

        // template
        uri.setServerName("www.alibaba.com");
        uri.setContextPath("myapp");
        uri.setServletPath("turbine");
        uri.setPage("product/ViewItem");
        assertEquals("http://www.alibaba.com/myapp/turbine/template/product%2CViewItem", uri.render());

        // screen
        uri.setServerName("www.alibaba.com");
        uri.setContextPath("myapp");
        uri.setServletPath("turbine");
        uri.setScreen("product.ViewItem");
        assertEquals("http://www.alibaba.com/myapp/turbine/screen/product.ViewItem", uri.render());

        // action
        uri.setServerName("www.alibaba.com");
        uri.setContextPath("myapp");
        uri.setServletPath("turbine");
        uri.setAction("product.ProductAction");
        assertEquals("http://www.alibaba.com/myapp/turbine/action/product.ProductAction", uri.render());

        // template + action
        uri.setServerName("www.alibaba.com");
        uri.setContextPath("myapp");
        uri.setServletPath("turbine");
        uri.setPage("product/ViewItem");
        uri.setAction("product.ProductAction");
        assertEquals("http://www.alibaba.com/myapp/turbine/template/product%2CViewItem/action/product.ProductAction",
                uri.render());
    }
}
