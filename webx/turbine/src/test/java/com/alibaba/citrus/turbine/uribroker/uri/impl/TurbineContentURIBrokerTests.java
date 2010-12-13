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
package com.alibaba.citrus.turbine.uribroker.uri.impl;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.uribroker.URIBrokerService;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.turbine.uribroker.uri.TurbineContentURIBroker;

public class TurbineContentURIBrokerTests {
    private TurbineContentURIBroker broker;

    @Before
    public void init() {
        broker = new TurbineContentURIBroker();
    }

    private TurbineContentURIBroker createParentBroker() {
        TurbineContentURIBroker parent = new TurbineContentURIBroker();

        parent.setComponentPath("myComponent");
        parent.setPrefixPath("myPrefix");
        parent.setContentPath("myContent.jpg");

        return parent;
    }

    private TurbineContentURIBroker createSubBroker(URIBroker parent) {
        TurbineContentURIBroker broker = new TurbineContentURIBroker();

        broker.setParent(parent);
        broker.setComponentPath("myComponent2");
        broker.setPrefixPath("myPrefix2");
        broker.setContentPath("myContent2.jpg");

        return broker;
    }

    @Test
    public void init_withParent() {
        TurbineContentURIBroker parent = createParentBroker();

        // empty broker
        broker.setParent(parent);
        broker.init();

        assertEquals("/myComponent", broker.getComponentPath());
        assertEquals("/myPrefix", broker.getPrefixPath());
        assertEquals("/myContent.jpg", broker.getContentPath());

        assertEquals("/myComponent/myPrefix/myContent.jpg", broker.getPathInfo());
        assertEquals("http:///myComponent/myPrefix/myContent.jpg", broker.toString());

        // override
        broker = createSubBroker(parent);
        broker.init();

        assertEquals("/myComponent2", broker.getComponentPath());
        assertEquals("/myPrefix2", broker.getPrefixPath());
        assertEquals("/myContent2.jpg", broker.getContentPath());

        assertEquals("/myComponent2/myPrefix2/myContent2.jpg", broker.getPathInfo());
        assertEquals("http:///myComponent2/myPrefix2/myContent2.jpg", broker.toString());

        broker.setComponentPath("");
        assertEquals("/myPrefix2/myContent2.jpg", broker.getPathInfo());
        assertEquals("http:///myPrefix2/myContent2.jpg", broker.toString());

        broker.setPrefixPath("");
        assertEquals("/myContent2.jpg", broker.getPathInfo());
        assertEquals("http:///myContent2.jpg", broker.toString());

        broker.setContentPath("");
        assertEquals("", broker.getPathInfo());
        assertEquals("http:///", broker.toString());
    }

    @Test
    public void reset_withParent() {
        TurbineContentURIBroker parent = createParentBroker();
        broker = createSubBroker(parent);
        broker.init();
        broker.reset();

        assertEquals("/myComponent", broker.getComponentPath());
        assertEquals("/myPrefix", broker.getPrefixPath());
        assertEquals("/myContent.jpg", broker.getContentPath());

        assertEquals("/myComponent/myPrefix/myContent.jpg", broker.getPathInfo());
        assertEquals("http:///myComponent/myPrefix/myContent.jpg", broker.toString());
    }

    @Test
    public void reset_withoutParent() {
        broker = createSubBroker(null);
        broker.init();
        broker.reset();

        assertEquals(null, broker.getComponentPath());
        assertEquals(null, broker.getPrefixPath());
        assertEquals(null, broker.getContentPath());

        assertEquals("", broker.getPathInfo());
        assertEquals("http:///", broker.toString());
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
    public void getComponentPath() {
        // init value
        assertNull(broker.getComponentPath());

        // set empty
        broker.setComponentPath(null);
        assertEquals("", broker.getComponentPath());
        assertEquals("", broker.getPathInfo());
        assertEquals("http:///", broker.toString());

        broker.setComponentPath(" ");
        assertEquals("", broker.getComponentPath());
        assertEquals("", broker.getPathInfo());
        assertEquals("http:///", broker.toString());

        // set value
        broker.setComponentPath(" mycomp ");
        assertEquals("/mycomp", broker.getComponentPath());
        assertEquals("/mycomp", broker.getPathInfo());
        assertEquals("http:///mycomp", broker.toString());
    }

    @Test
    public void getPrefixPath() {
        // init value
        assertNull(broker.getPrefixPath());

        // set empty
        broker.setPrefixPath(null);
        assertEquals("", broker.getPrefixPath());

        broker.setPrefixPath(" ");
        assertEquals("", broker.getPrefixPath());

        // set value
        broker.setPrefixPath("testPrefix");
        assertEquals("/testPrefix", broker.getPrefixPath());
    }

    @Test
    public void getContentPath() {
        assertNull(broker.getContentPath());
        assertEquals("http:///", broker.toString());

        broker.setContentPath("");
        assertEquals("", broker.getContentPath());
        assertEquals("http:///", broker.toString());

        broker.setPrefixPath("myprefix").setContentPath("aaa/bbb");
        assertEquals("/myprefix", broker.getPrefixPath());
        assertEquals("/aaa/bbb", broker.getContentPath());
        assertEquals("http:///myprefix/aaa/bbb", broker.toString());

        broker.setContextPath("app");
        assertEquals("http:///app/myprefix/aaa/bbb", broker.toString());

        broker.getURI("ccc/ddd/");
        assertEquals("http:///app/myprefix/ccc/ddd", broker.toString());
    }

    @Test
    public void config() {
        ApplicationContext factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir,
                "services-uris.xml")));

        URIBrokerService uris = (URIBrokerService) factory.getBean("uris");

        TurbineContentURIBroker link1 = (TurbineContentURIBroker) uris.getURIBroker("tclink4");
        assertEquals("http://taobao.com/mycontext/mycomponent/myprefix/mycontent.jpg?a=1&b=2", link1.toString());

        TurbineContentURIBroker link2 = (TurbineContentURIBroker) uris.getURIBroker("tclink5");
        assertEquals("http://taobao.com/?a=1&b=2", link2.toString());
    }
}
