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
package com.alibaba.citrus.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class UUIDTests {
    private final int loop = 10000;
    private final int concurrency = 20;
    private UUID uuid;
    private String instanceId;

    @Before
    public final void init() throws Exception {
        newUUID(null);
    }

    private void newUUID(Boolean noCase) {
        if (uuid == null) {
            uuid = new UUID();
        } else {
            uuid = new UUID(noCase);
        }

        instanceId = getFieldValue(uuid, "instanceId", String.class);
        assertNotNull(instanceId);
    }

    @Test
    public void generate() {
        newUUID(false);

        String sid = uuid.nextID();

        assertTrue(sid.length() > instanceId.length());
        assertTrue(sid.startsWith(instanceId));

        assertTrue(sid.matches("[A-Za-z0-9-]+"));
    }

    @Test
    public void generate_noCase() {
        newUUID(true);

        String sid = uuid.nextID();

        assertTrue(sid.length() > instanceId.length());
        assertTrue(sid.startsWith(instanceId));

        assertTrue(sid.matches("[A-Z0-9-]+"));
    }

    @Test
    public synchronized void performance() throws InterruptedException {
        final String[][] results = new String[concurrency][];
        Thread[] threads = new Thread[concurrency];

        for (int i = 0; i < concurrency; i++) {
            final String[] result = new String[loop];
            results[i] = result;
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < loop; i++) {
                        result[i] = uuid.nextID();
                    }
                }
            }, "t-" + (i + 1));
        }

        long start = System.currentTimeMillis();

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long duration = System.currentTimeMillis() - start;

        System.out.printf("%s: requests=%d, concurrency=%d%n", uuid.getClass().getSimpleName(), concurrency * loop,
                concurrency);

        System.out.printf("  Total time: %,d ms.%n", duration);
        System.out.printf("Average time: %,2.2f \u03BCs.%n", (double) duration / concurrency / loop * 1000);

        // ¼ì²éÖØ¸´
        Set<String> allIDs = createHashSet();

        for (String[] result : results) {
            for (String id : result) {
                assertNotNull(id);
                assertTrue(id, !id.contains("+") && !id.contains("/") && !id.contains("="));
                allIDs.add(id);
            }
        }

        assertEquals(concurrency * loop, allIDs.size());
    }
}
