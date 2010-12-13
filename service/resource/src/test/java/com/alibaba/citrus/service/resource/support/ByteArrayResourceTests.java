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
package com.alibaba.citrus.service.resource.support;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.io.StreamUtil.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 测试<code>ByteArrayResource</code>。
 * 
 * @author Michael Zhou
 */
public class ByteArrayResourceTests extends AbstractResourceTests {
    private static byte[] data;

    @BeforeClass
    public static void initData() {
        data = "abcdefghijklmnopqrstuvwxyz".getBytes();
    }

    @Test
    public void nullBytes() throws Exception {
        try {
            new ByteArrayResource(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("bytes"));
        }

        try {
            new ByteArrayResource(null, 0, 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("bytes"));
        }
    }

    @Test
    public void indexOutOfBounds() throws Exception {
        try {
            new ByteArrayResource(data, -1, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertThat(e, exception());
        }

        try {
            new ByteArrayResource(data, 0, 27);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertThat(e, exception());
        }

        try {
            new ByteArrayResource(data, 5, -1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertThat(e, exception());
        }
    }

    @Test
    public void fullSet() throws Exception {
        resource = new ByteArrayResource(data);

        assertNull(resource.getURL());
        assertNull(resource.getFile());
        assertNotNull(resource.getInputStream());
        assertTrue(resource.exists());
        assertEquals(0, resource.lastModified());

        // 读两次依然正确,不像InputStreamResource
        assertEquals("abcdefghijklmnopqrstuvwxyz", readText(resource.getInputStream(), null, true));
        assertEquals("abcdefghijklmnopqrstuvwxyz", readText(resource.getInputStream(), null, true));

        assertHashAndEquals(resource, new ByteArrayResource(data.clone()));

        String str = "";
        str += "ByteArrayResource [\n";
        str += "  [01-10/26] 61 62 63 64 65 66 67 68 69 6a\n";
        str += "  [11-20/26] 6b 6c 6d 6e 6f 70 71 72 73 74\n";
        str += "  [21-26/26] 75 76 77 78 79 7a\n";
        str += "]";

        assertEquals(str, resource.toString());
    }

    @Test
    public void subSet() throws Exception {
        resource = new ByteArrayResource(data, 10, 16);

        assertNull(resource.getURL());
        assertNull(resource.getFile());
        assertNotNull(resource.getInputStream());
        assertTrue(resource.exists());
        assertEquals(0, resource.lastModified());

        // 读两次依然正确,不像InputStreamResource
        assertEquals("klmnopqrstuvwxyz", readText(resource.getInputStream(), null, true));
        assertEquals("klmnopqrstuvwxyz", readText(resource.getInputStream(), null, true));

        assertHashAndEquals(resource, new ByteArrayResource(data.clone(), 10, 16));

        String str = "";
        str += "ByteArrayResource [\n";
        str += "  [11-20/26] 6b 6c 6d 6e 6f 70 71 72 73 74\n";
        str += "  [21-26/26] 75 76 77 78 79 7a\n";
        str += "]";

        assertEquals(str, resource.toString());
    }

    @Test
    public void largeData() throws Exception {
        byte[] data = new byte[1024];

        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i & 0xFF);
        }

        resource = new ByteArrayResource(data);

        String str = readText(getClass().getResource("largeData.txt").openStream(), null, true);

        str = str.replaceAll("\\r|\\n|\\r\\n", "\n");

        assertEquals(str, resource.toString());
    }
}
