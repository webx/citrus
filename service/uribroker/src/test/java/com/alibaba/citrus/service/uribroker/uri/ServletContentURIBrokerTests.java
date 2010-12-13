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

public class ServletContentURIBrokerTests extends AbstractURIBrokerFeaturesTests<ServletContentURIBroker> {
    @Override
    protected void setupParentBroker(ServletContentURIBroker parent) {
        super.setupParentBroker(parent);
        parent.setContextPath("myapp");
        parent.setPrefixPath("myprefix");
        parent.setContentPath("image/test.gif");
    }

    @Override
    protected void setupBroker(ServletContentURIBroker broker) {
        super.setupBroker(broker);
        broker.setContextPath("");
        broker.setPrefixPath("");
        broker.setContentPath("");
    }

    @Override
    protected void assertParentBroker(ServletContentURIBroker broker) {
        super.assertParentBroker(broker);
        assertEquals("/myapp", broker.getContextPath());
        assertEquals("/myprefix", broker.getPrefixPath());
        assertEquals("/image/test.gif", broker.getContentPath());
        assertEquals("/myapp/myprefix/image/test.gif", broker.getPath());
        assertEquals("http:///myapp/myprefix/image/test.gif", broker.toString());
    }

    @Override
    protected void assertBroker(ServletContentURIBroker broker) {
        super.assertBroker(broker);
        assertEquals("", broker.getContextPath());
        assertEquals("", broker.getPrefixPath());
        assertEquals("", broker.getContentPath());
        assertEquals("", broker.getPath());
        assertEquals("http:///", broker.toString());
    }

    @Override
    protected void assertAfterReset_noParent(ServletContentURIBroker broker) {
        super.assertAfterReset_noParent(broker);
        assertEquals(null, broker.getContextPath());
        assertEquals(null, broker.getContentPath());
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
    public void getPrefixPath() {
        assertNull(broker.getPrefixPath());
        assertEquals("http:///", broker.toString());

        broker.setPrefixPath("");
        assertEquals("", broker.getPrefixPath());
        assertEquals("http:///", broker.toString());

        broker.setPrefixPath("aaa/bbb");
        assertEquals("/aaa/bbb", broker.getPrefixPath());
        assertEquals("http:///aaa/bbb", broker.toString());
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
}
