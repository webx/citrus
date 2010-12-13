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
package com.alibaba.citrus.service.uribroker.impl;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;

import com.alibaba.citrus.service.uribroker.AbstractURIBrokerServiceTests;
import com.alibaba.citrus.service.uribroker.impl.URIBrokerServiceImpl.URIBrokerInfo;
import com.alibaba.citrus.service.uribroker.uri.ContentURIBroker;
import com.alibaba.citrus.service.uribroker.uri.GenericURIBroker;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;

public class URIBrokerServiceTests extends AbstractURIBrokerServiceTests {
    @Test
    public void requestProxy() {
        // 可以接受：no request
        new URIBrokerServiceImpl(null);

        // 不可以接受：非proxy的request
        HttpServletRequest mockRequest = createMock(HttpServletRequest.class);

        try {
            new URIBrokerServiceImpl(mockRequest);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("expects a proxy delegating to a real object, but got an object of type "
                    + mockRequest.getClass().getName()));
        }

        // 正常request proxy
        assertTrue(getFieldValue(service, "request", HttpServletRequest.class) instanceof ObjectFactory);
    }

    @Test
    public void isRequestAware() throws Exception {
        service = new URIBrokerServiceImpl(null);

        // default
        assertTrue(service.isRequestAware());
        assertNull(getFieldValue(service, "requestAware", Boolean.class));

        // 用default值初始化uribroker，uribroker.isRequestAware == true
        assertBrokerRequestAware(service, true);

        // set value
        service = new URIBrokerServiceImpl(null);

        service.setRequestAware(true);
        assertTrue(service.isRequestAware());

        service.setRequestAware(false);
        assertFalse(service.isRequestAware());

        assertBrokerRequestAware(service, false);
    }

    private void assertBrokerRequestAware(URIBrokerServiceImpl service, boolean expectedRequestAware) throws Exception {
        service.setBrokers(new URIBrokerInfo[] { new URIBrokerInfo("test", null, true, new GenericURIBroker()) });
        service.afterPropertiesSet();

        assertEquals(expectedRequestAware, service.getURIBrokerInternal("test").isRequestAware());
    }

    @Test
    public void getNames() {
        List<String> names = service.getNames();
        assertArrayEquals(new String[] { "link", "link2", "link3", "link4", "linkCharset", "linkWithInterceptor",
                "linkWithPathInterceptor", "clink", "clink2", "clink3", "clink4", "servletLink", "servletLink2",
                "servletLink3", "scontentLink", "scontentLink2", "scontentLink3", "classicLink", "classicLink2",
                "classicLink3", "mylink", "randomized" }, names.toArray(new String[0]));

        try {
            names.add(null); // unmodifiable
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void getExposedNames() {
        List<String> names = service.getExposedNames();
        assertArrayEquals(new String[] { "link2", "linkCharset" }, names.toArray(new String[0]));

        try {
            names.add(null); // unmodifiable
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void getURIBroker() {
        URIBroker broker = service.getURIBroker("link2");
        URIBroker parent = service.getURIBrokerInternal("link2");

        assertNotSame(parent, broker);
        assertSame(parent, broker.getParent());
        assertEquals(parent.toString(), broker.toString());

        assertNull(service.getURIBroker("notExist"));
    }

    @Test
    public void init_noBrokers() throws Exception {
        try {
            new URIBrokerServiceImpl(null).afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("brokers"));
        }
    }

    @Test
    public void init_noBroker() throws Exception {
        service = new URIBrokerServiceImpl(null);
        service.setBrokers(new URIBrokerInfo[] { null });

        try {
            service.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("broker"));
        }

        service = new URIBrokerServiceImpl(null);
        service.setBrokers(new URIBrokerInfo[] { new URIBrokerInfo("name", null, true, null) });

        try {
            service.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("broker"));
        }
    }

    @Test
    public void init_noBrokerName() throws Exception {
        service = new URIBrokerServiceImpl(null);
        service.setBrokers(new URIBrokerInfo[] { new URIBrokerInfo(null, null, true, new GenericURIBroker()) });

        try {
            service.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("broker ID"));
        }
    }

    @Test
    public void init_duplicatedBrokerName() throws Exception {
        service = new URIBrokerServiceImpl(null);
        service.setBrokers(new URIBrokerInfo[] { new URIBrokerInfo("name1", null, true, new GenericURIBroker()),
                new URIBrokerInfo("name1", null, true, new ContentURIBroker()) });

        try {
            service.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("duplicated broker ID: name1"));
        }
    }

    @Test
    public void init_cyclicDepends() throws Exception {
        service = new URIBrokerServiceImpl(null);
        service.setBrokers(new URIBrokerInfo[] { //
        new URIBrokerInfo("b4", "b1", true, new GenericURIBroker()), //
                new URIBrokerInfo("b1", "b2", true, new GenericURIBroker()), //
                new URIBrokerInfo("b2", "b3", true, new GenericURIBroker()), //
                new URIBrokerInfo("b3", "b1", true, new GenericURIBroker()), //
        });

        try {
            service.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Cyclic detected: b4->b1->b2->b3->b1"));
        }
    }

    @Test
    public void init_parentNotExists() throws Exception {
        service = new URIBrokerServiceImpl(null);
        service.setBrokers(new URIBrokerInfo[] { //
        new URIBrokerInfo("b4", "b1", true, new GenericURIBroker()), //
        });

        try {
            service.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("parent \"b1\" not found for broker \"b4\""));
        }
    }

    @Test
    public void init_parentNotSuperclass() throws Exception {
        service = new URIBrokerServiceImpl(null);
        service.setBrokers(new URIBrokerInfo[] { //
        new URIBrokerInfo("b4", "b1", true, new GenericURIBroker()), //
                new URIBrokerInfo("b1", null, true, new ContentURIBroker()), //
        });

        // 可继承非super class
        service.afterPropertiesSet();
    }

    @Test
    public void init_prerendered_initialized() throws Exception {
        for (String name : service.getNames()) {
            URIBroker broker = service.getURIBrokerInternal(name);
            assertTrue(getFieldValue(broker, "initialized", Boolean.class));

            Object renderer = getFieldValue(broker, "renderer", null);
            assertTrue(invokeMethod(renderer, "isServerRendered", null, null, Boolean.class));
        }
    }

    @Test
    public void init_request() {
        for (String name : service.getNames()) {
            URIBroker broker = service.getURIBrokerInternal(name);
            HttpServletRequest requestProxy = getFieldValue(broker, "request", HttpServletRequest.class);

            assertNotNull(requestProxy);
            assertTrue(requestProxy instanceof ObjectFactory);
            assertSame(request, ((ObjectFactory) requestProxy).getObject());
        }
    }

    @Test
    public void dump() throws IOException {
        String s = service.dump();
        assertThat(s, containsRegex("^  \\(GenericURIBroker\\)\\s+link\\s+= http://localhost/"));
        assertThat(s,
                containsRegex("\\* \\(GenericURIBroker\\)\\s+link2\\s+= http://myuser2:mypass2@myservername2:1234"
                        + "/aaa/a1/bbb/ccc/ddd\\?aaa=1111&bbb=2222&ccc=3333#myreference2"));
    }
}
