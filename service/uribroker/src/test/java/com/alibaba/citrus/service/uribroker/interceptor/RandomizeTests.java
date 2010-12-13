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
package com.alibaba.citrus.service.uribroker.interceptor;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.uribroker.AbstractURIBrokerServiceTests;
import com.alibaba.citrus.service.uribroker.uri.GenericURIBroker;

public class RandomizeTests extends AbstractURIBrokerServiceTests {
    private GenericURIBroker broker;
    private Randomize random;

    @Before
    public void init() {
        broker = new GenericURIBroker();
        broker.setServerURI("http://taobao.com/hello");

        random = new Randomize();
        broker.addInterceptor(random);
    }

    @Test
    public void render() {
        Set<String> results = createLinkedHashSet();

        for (int i = 0; i < 10; i++) {
            results.add(getRandomResult(broker.toString(), "http://taobao.com/hello?r="));
        }

        assertEquals(10, results.size());
    }

    @Test
    public void setKey() {
        random.setKey("otherKey");
        getRandomResult(broker.toString(), "http://taobao.com/hello?otherKey=");
    }

    @Test
    public void setChars() {
        random.setChars("123");

        for (int i = 0; i < 10; i++) {
            String result = getRandomResult(broker.toString(), "http://taobao.com/hello?r=");
            assertTrue(result.matches("[123]+"));
        }
    }

    @Test
    public void setPath() {
        random.setPath("^/aaa/bbb.*");

        String result = broker.toString();
        assertEquals("http://taobao.com/hello", result);

        broker.clearPath();
        broker.addPath("aaa/bbb/ccc");
        getRandomResult(broker.toString(), "http://taobao.com/aaa/bbb/ccc?r=");
    }

    @Test
    public void setRange() {
        random.setRange(3);

        for (int i = 0; i < 10; i++) {
            long r = random.random();
            assertTrue(r >= 0 && r < 3);
        }
    }

    @Test
    public void longToString() {
        random.init();

        String[] results = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e",
                "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y",
                "z", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1a", "1b", "1c", "1d", "1e", "1f",
                "1g", "1h", "1i", "1j", "1k", "1l", "1m", "1n", "1o", "1p", "1q", "1r", "1s", "1t", "1u", "1v", "1w",
                "1x", "1y", "1z", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d",
                "2e", "2f", "2g", "2h", "2i", "2j", "2k", "2l", "2m", "2n", "2o", "2p", "2q", "2r" };

        for (int i = 0; i < results.length; i++) {
            assertEquals(results[i], random.longToString(i));
        }
    }

    private String getRandomResult(String result, String prefix) {
        assertThat(result, startsWith(prefix));
        result = result.substring(prefix.length());
        assertTrue(!isEmpty(result));
        return result;
    }

    @Test
    public void configuration() {
        broker = (GenericURIBroker) service.getURIBroker("randomized");

        // path not match
        assertEquals("http://localhost/", broker.toString());
        assertEquals("http://localhost/", broker.toString());

        // path matched
        broker.addPath("abc/def");

        Set<String> results = createLinkedHashSet();

        for (int i = 0; i < 10; i++) {
            String result = getRandomResult(broker.toString(), "http://localhost/abc/def?rd=");
            assertTrue(result.matches("\\d+"));
            results.add(result);
        }

        assertTrue(results.size() > 1);
    }
}
