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

package com.alibaba.citrus.service.uribroker.interceptor;

import static org.junit.Assert.*;

import java.util.Collections;

import com.alibaba.citrus.service.uribroker.uri.GenericURIBroker;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;
import org.junit.Test;

public class InterceptorForkTests {
    @Test
    public void test() {
        URIBroker u1 = new GenericURIBroker();
        u1.setServerURI("http://localhost:8080/");
        u1.setInterceptors(Collections.<URIBrokerInterceptor>singletonList(new MyInterceptor()));

        u1 = u1.fork();

        assertEquals(true, u1.isAutoReset());
        assertEquals("http://localhost:8080/", u1.toString());
        assertEquals("http://localhost:8080/?test=webx3", u1.render());

        // u1.toString以后，interceptor不被执行。
        u1.toString(); // http://localhost:8080/
        URIBroker u2 = u1.fork();

        assertEquals("http://localhost:8080/", u2.toString());
        assertEquals("http://localhost:8080/?test=webx3", u2.render());
        assertEquals("http://localhost:8080/", u2.toString());
    }

    public static class MyInterceptor implements URIBrokerInterceptor {
        public void perform(URIBroker broker) {
            broker.addQueryData("test", "webx3");
        }
    }
}
