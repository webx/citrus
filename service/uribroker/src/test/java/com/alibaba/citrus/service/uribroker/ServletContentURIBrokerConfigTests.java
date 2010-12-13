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

import com.alibaba.citrus.service.uribroker.uri.ServletContentURIBroker;

public class ServletContentURIBrokerConfigTests extends AbstractURIBrokerServiceTests {
    private ServletContentURIBroker uri;

    @Test
    public void scontentLink() {
        uri = (ServletContentURIBroker) service.getURIBroker("scontentLink");
        assertEquals("http://localhost/myapp", uri.render());
        assertEquals("http://localhost/myapp/a/b.jsp", uri.setContentPath("a/b.jsp").render());
    }

    @Test
    public void scontentLink2() {
        uri = (ServletContentURIBroker) service.getURIBroker("scontentLink2");
        assertEquals("http://localhost/mycontext2/myprefix2/mycontent2", uri.render());
        assertEquals("http://localhost/mycontext2/myprefix2/a/b.jsp", uri.setContentPath("a/b.jsp").render());
    }

    @Test
    public void scontentLink3_override() {
        uri = (ServletContentURIBroker) service.getURIBroker("scontentLink3");
        assertEquals("http://localhost/myprefix2", uri.render());
        assertEquals("http://localhost/myprefix2/a/b.jsp", uri.setContentPath("a/b.jsp").render());
    }
}
