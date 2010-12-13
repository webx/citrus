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

public class URIBrokerPrerenderingPerformanceTests {
    private final static int LOOP = 10000;
    private GenericURIBroker parent;
    private GenericURIBroker broker;

    private void setupBroker(GenericURIBroker broker) {
        broker.setCharset("GBK"); // 避免LocaleUtil初始化的时间

        broker.setServerScheme("https");
        broker.setServerName("localhost");
        broker.setServerPort(443);
        broker.setLoginUser("user");
        broker.setLoginPassword("pass");
        broker.setReference("ref");

        broker.addPath("/aa/bb");
        broker.addQueryData("a", 1);
        broker.addQueryData("b", 2);
    }

    @Test
    public void noPrerender() {
        parent = new GenericURIBroker();
        setupBroker(parent);

        // parent不是autoreset的，因此fork出来的broker不会prerendering
        broker = (GenericURIBroker) parent.fork();

        broker.addPath("cc");
        broker.addQueryData("a", "3");
        assertEquals("https://user:pass@localhost/aa/bb/cc?a=1&a=3&b=2#ref", broker.render());
        assertEquals(false, broker.renderer.isServerRendered());

        stress("No Prerendering");
    }

    @Test
    public void withPrerender() {
        parent = (GenericURIBroker) new GenericURIBroker().fork();
        setupBroker(parent);

        // parent是autoreset的，因此fork出来的broker会prerendering
        broker = (GenericURIBroker) parent.fork();

        broker.addPath("cc");
        broker.addQueryData("a", "3");
        assertEquals("https://user:pass@localhost/aa/bb/cc?a=1&b=2&a=3#ref", broker.render());
        assertEquals(true, broker.renderer.isServerRendered());

        stress("With Prerendering");
    }

    private void stress(String desc) {
        long start = System.currentTimeMillis();

        for (int i = 0; i < LOOP; i++) {
            broker.addPath("cc");
            broker.addQueryData("a", "3");
            broker.render();
        }

        long duration = System.currentTimeMillis() - start;

        System.out.printf("%s, Loop: %d, Duration: %,d ms%n", desc, LOOP, duration);
    }
}
