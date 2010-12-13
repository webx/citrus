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
package com.alibaba.citrus.util.io;

import static org.junit.Assert.*;

import org.junit.Test;

public class ByteArrayInputStreamTests {
    private byte[] data = "0123456789".getBytes();

    @Test
    public void read() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        bais.mark(100);

        byte[] readData = new byte[data.length];

        assertEquals(data.length, bais.read(readData));
        assertArrayEquals(data, readData);

        bais.reset();

        byte[] readData2 = new byte[data.length];

        assertEquals(data.length, bais.read(readData2));
        assertArrayEquals(data, readData2);

        bais.close();
    }
}
