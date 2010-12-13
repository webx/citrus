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

import com.alibaba.citrus.service.uribroker.uri.ContentURIBroker;

public class ContentURIBrokerConfigTests extends AbstractURIBrokerServiceTests {
    private ContentURIBroker uri;

    @Test
    public void clink() {
        uri = (ContentURIBroker) service.getURIBroker("clink");
        assertEquals("http://localhost/", uri.render());
    }

    @Test
    public void clink2() {
        uri = (ContentURIBroker) service.getURIBroker("clink2");
        assertEquals("http://localhost/myprefix2/mycontent2", uri.render());
        assertEquals("http://localhost/myprefix2/aaa/bbb/ccc.jpg", uri.setContentPath("/aaa/bbb/ccc.jpg").render());
    }

    @Test
    public void clink3_overrideClink2() {
        uri = (ContentURIBroker) service.getURIBroker("clink3");
        assertEquals("http://localhost/myprefix2", uri.render());
        assertEquals("http://localhost/myprefix2/aaa/bbb/ccc.jpg", uri.getURI("/aaa/bbb/ccc.jpg").render());
    }

    @Test
    public void clink4_extendsURIBroker() {
        uri = (ContentURIBroker) service.getURIBroker("clink4");

        // 只继承serverInfo和query，不会继承path
        assertEquals("http://myuser2:mypass2@myservername2:1234/?aaa=1111&bbb=2222&ccc=3333#myreference2", uri.render());
        assertEquals("http://myuser2:mypass2@myservername2:1234/a.jpg?aaa=1111&bbb=2222&ccc=3333#myreference2", uri
                .setContentPath("a.jpg").render());
    }
}
