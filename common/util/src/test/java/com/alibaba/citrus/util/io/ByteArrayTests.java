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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;

/**
 * ≤‚ ‘<code>ByteArray</code>¿‡°£
 * 
 * @author Michael Zhou
 */
public class ByteArrayTests {
    private byte[] data = "0123456789".getBytes();

    @Test(expected = IllegalArgumentException.class)
    public void constructor_wrong0() {
        new ByteArray(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_wrong1() {
        new ByteArray(null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_wrong2() {
        new ByteArray(data, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_wrong3() {
        new ByteArray(data, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_wrong4() {
        new ByteArray(data, 0, 11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_wrong5() {
        new ByteArray(data, 1, 10);
    }

    @Test
    public void toBytes() throws Exception {
        ByteArray ba;

        ba = new ByteArray(data);

        assertEquals(0, ba.getOffset());
        assertEquals(10, ba.getLength());
        assertSame(data, ba.getRawBytes());
        assertNotSame(data, ba.toByteArray());
        assertArrayEquals(data, ba.toByteArray());
        assertArrayEquals(data, readStream(ba.toInputStream()));

        ba = new ByteArray(data, 1, Integer.MIN_VALUE);

        assertEquals(1, ba.getOffset());
        assertEquals(9, ba.getLength());
        assertSame(data, ba.getRawBytes());
        assertNotSame(data, ba.toByteArray());
        assertArrayEquals("123456789".getBytes(), ba.toByteArray());
        assertArrayEquals("123456789".getBytes(), readStream(ba.toInputStream()));

        ba = new ByteArray(data, 0, 10);

        assertEquals(0, ba.getOffset());
        assertEquals(10, ba.getLength());
        assertSame(data, ba.getRawBytes());
        assertNotSame(data, ba.toByteArray());
        assertArrayEquals(data, ba.toByteArray());
        assertArrayEquals(data, readStream(ba.toInputStream()));

        ba = new ByteArray(data, 1, 9);

        assertEquals(1, ba.getOffset());
        assertEquals(9, ba.getLength());
        assertSame(data, ba.getRawBytes());
        assertNotSame(data, ba.toByteArray());
        assertArrayEquals("123456789".getBytes(), ba.toByteArray());
        assertArrayEquals("123456789".getBytes(), readStream(ba.toInputStream()));

        ba = new ByteArray(data, 1, 0);

        assertEquals(1, ba.getOffset());
        assertEquals(0, ba.getLength());
        assertSame(data, ba.getRawBytes());
        assertNotSame(data, ba.toByteArray());
        assertArrayEquals("".getBytes(), ba.toByteArray());
        assertArrayEquals("".getBytes(), readStream(ba.toInputStream()));
    }

    @Test
    public void writeTo() throws Exception {
        ByteArray ba;

        ba = new ByteArray(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ba.writeTo(baos);

        assertArrayEquals(data, baos.toByteArray());
    }

    @Test
    public void _toString() {
        ByteArray ba;

        ba = new ByteArray(data);

        assertEquals("byte[10]", ba.toString());

        ba = new ByteArray(data, 1, Integer.MIN_VALUE);

        assertEquals("byte[9]", ba.toString());

        ba = new ByteArray(data, 0, 10);

        assertEquals("byte[10]", ba.toString());

        ba = new ByteArray(data, 1, 9);

        assertEquals("byte[9]", ba.toString());

        ba = new ByteArray(data, 1, 0);

        assertEquals("byte[0]", ba.toString());
    }

    private byte[] readStream(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;

        while ((i = is.read()) >= 0) {
            baos.write(i);
        }

        return baos.toByteArray();
    }
}
