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

import java.io.InputStream;

import org.junit.Test;

/**
 * ≤‚ ‘<code>ByteArrayOutputStream</code>¿‡°£
 * 
 * @author Michael Zhou
 */
public class ByteArrayOutputStreamTests {
    private byte[] data = "0123456789".getBytes();

    @Test
    public void toBytes() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(data);

        assertArrayEquals(data, readStream(baos.toInputStream()));
        assertArrayEquals(data, baos.toByteArray().toByteArray());
    }

    private byte[] readStream(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;

        while ((i = is.read()) >= 0) {
            baos.write(i);
        }

        return baos.toByteArray().toByteArray();
    }
}
