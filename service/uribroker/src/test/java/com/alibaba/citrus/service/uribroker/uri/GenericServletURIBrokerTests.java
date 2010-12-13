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

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class GenericServletURIBrokerTests extends AbstractURIBrokerFeaturesTests<GenericServletURIBroker> {
    @Override
    protected void setupParentBroker(GenericServletURIBroker parent) {
        super.setupParentBroker(parent);
        parent.setContextPath("myapp");
        parent.setServletPath("myservlet");
        parent.setPathInfoElements(null);
    }

    @Override
    protected void setupBroker(GenericServletURIBroker broker) {
        super.setupBroker(broker);
        broker.setContextPath("");
        broker.setServletPath("");
        broker.setPathInfoElements(createArrayList("path1", "path2"));
    }

    @Override
    protected void assertParentBroker(GenericServletURIBroker broker) {
        super.assertParentBroker(broker);
        assertEquals("/myapp", broker.getContextPath());
        assertEquals("/myservlet", broker.getServletPath());
        assertEquals("", broker.getPathInfo());
        assertEquals("/myapp/myservlet", broker.getPath());
        assertEquals("http:///myapp/myservlet", broker.toString());
    }

    @Override
    protected void assertBroker(GenericServletURIBroker broker) {
        super.assertBroker(broker);
        assertEquals("", broker.getContextPath());
        assertEquals("", broker.getServletPath());
        assertEquals("/path1/path2", broker.getPathInfo());
        assertEquals("/path1/path2", broker.getPath());
        assertEquals("http:///path1/path2", broker.toString());
    }

    @Override
    protected void assertAfterReset_noParent(GenericServletURIBroker broker) {
        super.assertAfterReset_noParent(broker);
        assertEquals(null, broker.getContextPath());
        assertEquals(null, broker.getServletPath());
    }

    @Test
    public void getContextPath() {
        assertNull(broker.getContextPath());
        assertEquals("http:///", broker.toString());

        broker.setContextPath("");
        assertEquals("", broker.getContextPath());
        assertEquals("http:///", broker.toString());

        broker.setContextPath("aaa/bbb");
        assertEquals("/aaa/bbb", broker.getContextPath());
        assertEquals("http:///aaa/bbb", broker.toString());
    }

    @Test
    public void getServletPath() {
        assertNull(broker.getServletPath());
        assertEquals("http:///", broker.toString());

        broker.setServletPath("");
        assertEquals("", broker.getServletPath());
        assertEquals("http:///", broker.toString());

        broker.setServletPath("aaa/bbb");
        assertEquals("/aaa/bbb", broker.getServletPath());
        assertEquals("http:///aaa/bbb", broker.toString());

        broker.setContextPath("app");
        assertEquals("http:///app/aaa/bbb", broker.toString());
    }

    @Test
    public void getScriptName() {
        broker.setServletPath("aaa/bbb");
        assertEquals("/aaa/bbb", broker.getScriptName());

        broker.setContextPath("app");
        assertEquals("/app/aaa/bbb", broker.getScriptName());
    }

    @Test
    public void reset_withRequest() {
        GenericServletURIBroker parent;

        // requestAware=false
        parent = newInstance();
        broker = newInstance();
        broker.setParent(parent);
        broker.init();
        request = getMockRequest("https", "taobao.com", 8888, "myapp", "myservlet", null);
        broker.setRequest(request);
        broker.setRequestAware(false);

        broker.reset();
        assertEquals(null, broker.getServerScheme());
        assertEquals(null, broker.getServerName());
        assertEquals(-1, broker.getServerPort());
        assertEquals(null, broker.getContextPath());
        assertEquals(null, broker.getServletPath());

        // requestAware=true
        parent = newInstance();
        broker = newInstance();
        broker.setParent(parent);
        broker.init();
        request = getMockRequest("https", "taobao.com", 8888, "myapp", "myservlet", null);
        broker.setRequest(request);
        broker.setRequestAware(true);

        broker.reset();
        assertEquals("https", broker.getServerScheme());
        assertEquals("taobao.com", broker.getServerName());
        assertEquals(8888, broker.getServerPort());
        assertEquals("/myapp", broker.getContextPath());
        assertEquals("/myservlet", broker.getServletPath());

        // requestAware=true, suffix mapping: servletPath="", pathInfo != null
        parent = newInstance();
        broker = newInstance();
        broker.setParent(parent);
        broker.init();
        request = getMockRequest("https", "taobao.com", 8888, "myapp", "", "myservlet.htm");
        broker.setRequest(request);
        broker.setRequestAware(true);

        broker.reset();
        assertEquals("https", broker.getServerScheme());
        assertEquals("taobao.com", broker.getServerName());
        assertEquals(8888, broker.getServerPort());
        assertEquals("/myapp", broker.getContextPath());
        assertEquals("", broker.getServletPath());
        assertEquals("", broker.getPathInfo());

        // requestAware=true, no overridden because context path is already set
        parent = newInstance();
        broker = newInstance();
        broker.setParent(parent);
        broker.init();
        request = getMockRequest("https", "taobao.com", 8888, "myapp", "myservlet", null);
        broker.setRequest(request);
        broker.setRequestAware(true);
        parent.setContextPath("myapp2");

        broker.reset();
        assertEquals("https", broker.getServerScheme());
        assertEquals("taobao.com", broker.getServerName());
        assertEquals(8888, broker.getServerPort());
        assertEquals("/myapp2", broker.getContextPath());
        assertEquals(null, broker.getServletPath());

        // requestAware=true, override servlet path
        parent = newInstance();
        broker = newInstance();
        broker.setParent(parent);
        broker.init();
        request = getMockRequest("https", "taobao.com", 8888, "myapp", "myservlet", null);
        broker.setRequest(request);
        broker.setRequestAware(true);
        parent.setServletPath("myservlet2");

        broker.reset();
        assertEquals("https", broker.getServerScheme());
        assertEquals("taobao.com", broker.getServerName());
        assertEquals(8888, broker.getServerPort());
        assertEquals("/myapp", broker.getContextPath());
        assertEquals("/myservlet2", broker.getServletPath());

        // requestAware=true, override both context path and servlet path
        parent = newInstance();
        broker = newInstance();
        broker.setParent(parent);
        broker.init();
        request = getMockRequest("https", "taobao.com", 8888, "myapp", "myservlet", null);
        broker.setRequest(request);
        broker.setRequestAware(true);
        parent.setContextPath("myapp2");
        parent.setServletPath("myservlet2");

        broker.reset();
        assertEquals("https", broker.getServerScheme());
        assertEquals("taobao.com", broker.getServerName());
        assertEquals(8888, broker.getServerPort());
        assertEquals("/myapp2", broker.getContextPath());
        assertEquals("/myservlet2", broker.getServletPath());
    }
}
