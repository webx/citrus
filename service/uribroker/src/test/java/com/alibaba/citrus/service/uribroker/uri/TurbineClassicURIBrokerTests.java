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
package com.alibaba.citrus.service.uribroker.uri;

import static org.junit.Assert.*;

import org.junit.Test;

public class TurbineClassicURIBrokerTests extends AbstractURIBrokerFeaturesTests<TurbineClassicURIBroker> {
    @Override
    protected void setupParentBroker(TurbineClassicURIBroker parent) {
        super.setupParentBroker(parent);
        parent.setContextPath("myapp");
        parent.setServletPath("myservlet");
        parent.setPage("user/adduser");
        parent.setScreen("myscreen");
        parent.setAction("myaction");
        parent.addPathInfo("a", 1);
        parent.addPathInfo("b", 2);
    }

    @Override
    protected void setupBroker(TurbineClassicURIBroker broker) {
        super.setupBroker(broker);
        broker.setContextPath("");
        broker.setServletPath("");
        broker.setPage("");
        broker.setScreen("");
        broker.setAction("");
        broker.addPathInfo("c", 3);
        broker.addPathInfo("d", 4);
    }

    @Override
    protected void assertParentBroker(TurbineClassicURIBroker broker) {
        super.assertParentBroker(broker);
        assertEquals("/myapp", broker.getContextPath());
        assertEquals("/myservlet", broker.getServletPath());
        assertEquals("user,adduser", broker.getPage());
        assertEquals("myscreen", broker.getScreen());
        assertEquals("myaction", broker.getAction());
        assertEquals("/template/user,adduser/screen/myscreen/action/myaction/a/1/b/2", broker.getPathInfo());
        assertEquals("/myapp/myservlet/template/user,adduser/screen/myscreen/action/myaction/a/1/b/2", broker.getPath());
        assertEquals("http:///myapp/myservlet/template/user%2Cadduser/screen/myscreen/action/myaction/a/1/b/2",
                broker.toString());
    }

    @Override
    protected void assertBroker(TurbineClassicURIBroker broker) {
        super.assertBroker(broker);
        assertEquals("", broker.getContextPath());
        assertEquals("", broker.getServletPath());
        assertEquals("null", broker.getPage());
        assertEquals("null", broker.getScreen());
        assertEquals("null", broker.getAction());
        assertEquals("/template/null/screen/null/action/null/a/1/b/2/c/3/d/4", broker.getPathInfo());
        assertEquals("/template/null/screen/null/action/null/a/1/b/2/c/3/d/4", broker.getPath());
        assertEquals("http:///template/null/screen/null/action/null/a/1/b/2/c/3/d/4", broker.toString());
    }

    @Override
    protected void assertAfterReset_noParent(TurbineClassicURIBroker broker) {
        super.assertAfterReset_noParent(broker);
        assertEquals(null, broker.getContextPath());
        assertEquals(null, broker.getServletPath());
    }

    @Test
    public void getPage() {
        assertNull(broker.getPage());
        assertEquals("http:///", broker.toString());

        broker.setPage("");
        assertEquals("null", broker.getPage());
        assertEquals("http:///template/null", broker.toString());

        broker.setPage("aaa/bbb");
        assertEquals("aaa,bbb", broker.getPage());
        assertEquals("http:///template/aaa%2Cbbb", broker.toString());
    }

    @Test
    public void getScreen() {
        assertNull(broker.getScreen());
        assertEquals("http:///", broker.toString());

        broker.setScreen("");
        assertEquals("null", broker.getScreen());
        assertEquals("http:///screen/null", broker.toString());

        broker.setScreen("aaa/bbb");
        assertEquals("aaa,bbb", broker.getScreen());
        assertEquals("http:///screen/aaa%2Cbbb", broker.toString());
    }

    @Test
    public void getAction() {
        assertNull(broker.getAction());
        assertEquals("http:///", broker.toString());

        broker.setAction("");
        assertEquals("null", broker.getAction());
        assertEquals("http:///action/null", broker.toString());

        broker.setAction("aaa/bbb");
        assertEquals("aaa,bbb", broker.getAction());
        assertEquals("http:///action/aaa%2Cbbb", broker.toString());
    }

    @Test
    public void addPathInfo() {
        broker.addPathInfo("aa", null);
        broker.addPathInfo("bb", "");
        broker.addPathInfo("cc", "haha");
        assertEquals("http:///aa/null/bb/null/cc/haha", broker.toString());

        broker.clearPathInfoParams();
        assertEquals("http:///", broker.toString());
    }
}
