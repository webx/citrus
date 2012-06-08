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

package com.alibaba.citrus.service.uribroker.uri;

import static org.junit.Assert.*;

import org.junit.Test;

public class ContentURIBrokerTests extends AbstractURIBrokerFeaturesTests<ContentURIBroker> {
    @Override
    protected void setupParentBroker(ContentURIBroker parent) {
        super.setupParentBroker(parent);
        parent.setPrefixPath("path1/path2");
        parent.setContentPath("aaa/bbb");
    }

    @Override
    protected void setupBroker(ContentURIBroker broker) {
        super.setupBroker(broker);
        broker.setPrefixPath("");
        broker.setContentPath("");
    }

    @Override
    protected void assertParentBroker(ContentURIBroker broker) {
        super.assertParentBroker(broker);
        assertEquals("/path1/path2", broker.getPrefixPath());
        assertEquals("/aaa/bbb", broker.getContentPath());
        assertEquals("/path1/path2/aaa/bbb", broker.getPath());
        assertEquals("http:///path1/path2/aaa/bbb", broker.toString());
    }

    @Override
    protected void assertBroker(ContentURIBroker broker) {
        super.assertBroker(broker);
        assertEquals("", broker.getPrefixPath());
        assertEquals("", broker.getContentPath());
        assertEquals("", broker.getPath());
        assertEquals("http:///", broker.toString());
    }

    @Override
    protected void assertAfterReset_noParent(ContentURIBroker broker) {
        super.assertAfterReset_noParent(broker);
        assertEquals(null, broker.getContentPath());
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

        broker.setPrefixPath("prefix").setContentPath("aaa/bbb");
        assertEquals("/prefix", broker.getPrefixPath());
        assertEquals("/aaa/bbb", broker.getContentPath());
        assertEquals("http:///prefix/aaa/bbb", broker.toString());
    }

    @Test
    public void getURI() {
        broker.setPrefixPath("myprefix");

        broker.getURI(null);
        assertEquals("http:///myprefix", broker.toString());

        broker.getURI("ccc/ddd");
        assertEquals("http:///myprefix/ccc/ddd", broker.toString());

        broker.getURI("ccc/ddd?a=1&b=+");
        assertEquals("http:///myprefix/ccc/ddd?a=1&b=+", broker.toString());

        broker.reset();

        broker.getURI("ccc/ddd#ref");
        assertEquals("http:///ccc/ddd#ref", broker.toString());

        broker.getURI("ccc/ddd?a=1&b=+#ref2");
        assertEquals("http:///ccc/ddd?a=1&b=+#ref2", broker.render());
    }
}
